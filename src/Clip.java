/*
 * Clip.java
 */

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

/**
 * A helper class to hold the line-clipping algorithm.
 */

public class Clip {

// --- constants and structures ---

   // we have a line from p0 to p1 parametrized by a number from 0 to 1.
   // when clipping occurs, the result might include the interval [0,a],
   // the interval [b,1], or both.

   public static final int KEEP_NONE = 0;
   public static final int KEEP_A    = 1; // note, these two can also be used as masks
   public static final int KEEP_B    = 2; //
   public static final int KEEP_AB   = 3;
   public static final int KEEP_LINE = 4;

   public static class Result {

      public double a;
      public double b;
      public int clip;

      public int ia; // index of boundary on "a" side, valid if KEEP_A
      public int ib; //                      "b"                KEEP_B

      // must test for KEEP_LINE first
      public boolean hasSegA() { return ((clip & KEEP_A) != 0); }
      public boolean hasSegB() { return ((clip & KEEP_B) != 0); }

      // computation of the actual points isn't always needed, so don't
      // do it unless requested.
      public void getPointA(double[] dest, double[] p0, double[] p1) { Vec.mid(dest,p0,p1,a); }
      public void getPointB(double[] dest, double[] p0, double[] p1) { Vec.mid(dest,p0,p1,b); }
   }

   public interface BoundaryList {
      int getSize();
      Boundary getBoundary(int i);
   }

   public interface Boundary {
      double[] getNormal();
      double getThreshold();
   }

// --- clipping ---

   /**
    * See if the line from p0 to p1 is clipped by the poly-whatever.
    * @return A copy of the result.clip value.
    */
   public static int clip(double[] p0, double[] p1, BoundaryList boundaryList, Result result) {

      if (boundaryList.getSize() == 0) { // result of glass in calcViewBoundaries
         result.clip = KEEP_LINE;
         return result.clip;
      }

      // the theory here is, a point p is blocked by the poly-whatever
      // if for every boundary, p dot n is less than the threshold;
      // but here it's easier to think in terms of what's safe, not clipped.
      // once we find p dot n greater than the threshold, that part is safe.
      // the safe parts encroach from the two ends of the line.

      final double epsilon = 0.000001;
      // when two cubes are perfectly stacked, I want the top face of the bottom cube
      // to be hidden even if there's FP error/rounding, so expand the clip region
      // by epsilon.  the normals ought to be unit vectors to get consistent epsilons,
      // but it's not a big deal either way.

      // these will expand out as things become safe
      result.a = 0;
      result.b = 1;
      result.clip = KEEP_NONE;

      int size = boundaryList.getSize();
      for (int i=0; i<size; i++) {
         Boundary boundary = boundaryList.getBoundary(i);

         double[] n = boundary.getNormal();
         double t = boundary.getThreshold();

         if (n == null) { // glass, can't clip
            result.clip = KEEP_LINE;
            return result.clip;
         }

         double v0 = Vec.dot(p0,n) - t - epsilon;
         double v1 = Vec.dot(p1,n) - t - epsilon;

         if (v0 > 0) {
            if (v1 > 0) { // all safe, we're done
               result.a = 1;
               result.b = 0;
            } else { // add to safe left part
               double x = v0/(v0-v1);
               if (x > result.a) { result.a = x; result.ia = i; }
            }
         } else {
            if (v1 > 0) { // add to safe right part
               double x = v0/(v0-v1);
               if (x < result.b) { result.b = x; result.ib = i; }
            }
            // else nothing is safe, keep checking
         }

         if (result.a >= result.b) { // all safe
            result.clip = KEEP_LINE;
            return result.clip;
         }
      }

      if (result.a > 0) result.clip |= KEEP_A;
      if (result.b < 1) result.clip |= KEEP_B;
      return result.clip;
   }

// --- view boundaries ---

   public static class CustomBoundary implements Boundary {

      private double[] n;
      private double t;

      public CustomBoundary(double[] n, double t) {
         this.n = n;
         this.t = t;
      }

      public double[] getNormal() { return n; }
      public double getThreshold() { return t; }
   }

   public static CustomBoundary calcViewBoundary(double[] origin, Boundary b1, Boundary b2) {

      // the theory here is, b1 and b2 are faces (d-1), their intersection is a subface (d-2)
      // and then we want to find the boundary that includes the subface and the origin (d-1).
      // I think this way (clipping up front) is simpler than trying to clip after projection.

      // normals n1 and n2 define a plane, and we just solve for a new normal (1-x) n1 + x n2
      // that includes the origin in the perpendicular (d-1)-space through the subface.

      double[] n1 = b1.getNormal();
      double[] n2 = b2.getNormal();
      double t1 = b1.getThreshold();
      double t2 = b2.getThreshold();

      if (n1 == null || n2 == null) return null;

      double k1 = Vec.dot(n1,origin)-t1;
      double k2 = Vec.dot(n2,origin)-t2;
      double x = k1/(k1-k2);
      // we get sign change issues when k1 crosses k2, but we should only be calling this
      // when b1 is visible and b2 isn't, in which case k1 > 0 and k2 < 0 and all is well.
      // also works when b1 and b2 are swapped.

      // new normal is weighted sum of old normals,
      // new thresh is weighted sum of old threshs
      //
      double[] n3 = new double[origin.length];
      Vec.mid(n3,n1,n2,x);
      double t3 = (1-x)*t1 + x*t2;

      return new CustomBoundary(n3,t3);
   }

   public static class CustomBoundaryList implements BoundaryList {

      private Vector boundaries;

      public CustomBoundaryList() {
         boundaries = new Vector();
      }

      public void addBoundary(Boundary b) { boundaries.add(b); }

      public int getSize() { return boundaries.size(); }
      public Boundary getBoundary(int i) { return (Boundary) boundaries.get(i); }
   }

   public static CustomBoundaryList calcViewBoundaries(double[] origin, Geom.Shape shape) {

      CustomBoundaryList list = new CustomBoundaryList();

      // clip by subfaces where one face is visible and the other not
      for (int i=0; i<shape.subface.length; i++) {
         Geom.Subface sf = shape.subface[i];
         Geom.Face f1 = shape.face[sf.if1];
         Geom.Face f2 = shape.face[sf.if2];
         if (f1.visible != f2.visible) {
            Boundary b = calcViewBoundary(origin,f1,f2);
            if (b != null) list.addBoundary(b); // only null if glass
         }
      }

      return list;
   }

// --- clip unit ---

   public static class Draw implements IDraw {

      public BoundaryList bl;
      public IDraw next;
      public Result clipResult;
      public double[] temp;

      public Draw(int dim) {
         // bl and next vary now
         clipResult = new Result();
         temp = new double[dim];
      }

      public void setBoundaries(BoundaryList bl) {
         this.bl = bl;
      }

      public IDraw chain(IDraw next) {
         this.next = next;
         return this; // convenience
      }

      public void drawLine(double[] p1, double[] p2, Color color, double[] origin) {
         if (clip(p1,p2,bl,clipResult) == Clip.KEEP_LINE) {
            next.drawLine(p1,p2,color,origin);
         } else {
            if (clipResult.hasSegA()) {
               clipResult.getPointA(temp,p1,p2);
               next.drawLine(p1,temp,color,origin);
            }
            if (clipResult.hasSegB()) {
               clipResult.getPointB(temp,p1,p2);
               next.drawLine(temp,p2,color,origin);
            }
         }
      }
   }

// --- static separation ---

   public static double vmin(Geom.Shape s, int axis) {
      double d = s.vertex[0][axis];
      for (int i=1; i<s.vertex.length; i++) {
         double temp = s.vertex[i][axis];
         if (temp < d) d = temp;
      }
      return d;
   }

   public static double vmax(Geom.Shape s, int axis) {
      double d = s.vertex[0][axis];
      for (int i=1; i<s.vertex.length; i++) {
         double temp = s.vertex[i][axis];
         if (temp > d) d = temp;
      }
      return d;
   }

   public static double nmin(Geom.Shape s, double[] normal) {
      double d = Vec.dot(s.vertex[0],normal);
      for (int i=1; i<s.vertex.length; i++) {
         double temp = Vec.dot(s.vertex[i],normal);
         if (temp < d) d = temp;
      }
      return d;
   }

   public static double nmax(Geom.Shape s, double[] normal) {
      double d = Vec.dot(s.vertex[0],normal);
      for (int i=1; i<s.vertex.length; i++) {
         double temp = Vec.dot(s.vertex[i],normal);
         if (temp > d) d = temp;
      }
      return d;
   }

   public static final double overlap = 0.000001;
   // allow a teeny bit of overlap, otherwise it'll be too hard
   // to move shapes until they're touching in non-aligned mode

   public static Geom.Separator tryNormal(Geom.Shape s1, Geom.Shape s2, double[] normal) {

      double nmin1 = nmin(s1,normal);
      double nmax1 = nmax(s1,normal);
      double nmin2 = nmin(s2,normal);
      double nmax2 = nmax(s2,normal);

      if (nmin2-nmax1 >= -overlap) return new Geom.NormalSeparator(normal,nmax1,nmin2, 1);
      if (nmin1-nmax2 >= -overlap) return new Geom.NormalSeparator(normal,nmax2,nmin1,-1);

      return null;
   }

   public static Geom.Separator tryFaceNormals(Geom.Shape shape, Geom.Shape s1, Geom.Shape s2, double[] normal) {
      Geom.Separator sep;

      for (int i=0; i<shape.face.length; i++) {
         if (shape.face[i].normal == null) continue;

         Vec.copy(normal,shape.face[i].normal);
         // could maybe share the vector
         // with the shape face, but it seems flaky

         sep = tryNormal(s1,s2,normal);
         if (sep != null) return sep;
      }

      return null;
   }

   /**
    * @param any True if you just want any separator to show that there's no collision.
    */
   public static Geom.Separator staticSeparate(Geom.Shape s1, Geom.Shape s2, boolean any) {
      Geom.Separator sep = null;

   // first check for any hints

      if (s1.hint != null) {
         sep = s1.hint.getHint(s1,s2, 1);
         if (sep != null) return sep;
      }

      if (s2.hint != null) {
         sep = s2.hint.getHint(s2,s1,-1);
         if (sep != null) return sep;
      }

   // try separating along axis between centers.
   // if that works it's probably the best plan.

      double[] normal = new double[s1.getDimension()]; // must be new so we can hand off to separator object
      Vec.sub(normal,s2.shapecenter,s1.shapecenter);

      // if the normal vector is near zero it's no good
      final double epsilon = 0.000001;
      if (Vec.dot(normal,normal) > epsilon) {

         sep = tryNormal(s1,s2,normal);
         if (sep != null) return sep;
      }

   // search along axes for one that gives greatest distance

      double q; // quality of separator
      double qsep = 0;

      for (int axis=0; axis<s1.getDimension(); axis++) {

         double vmin1 = vmin(s1,axis);
         double vmax1 = vmax(s1,axis);
         double vmin2 = vmin(s2,axis);
         double vmax2 = vmax(s2,axis);

         q = vmin2-vmax1; // 2 above 1
         if (q >= -overlap && (sep == null || q > qsep)) {
            Vec.unitVector(normal,axis);
            sep = new Geom.NormalSeparator(normal,vmax1,vmin2, 1);
            qsep = q;
            if (any) return sep;
         }

         q = vmin1-vmax2; // 1 above 2
         if (q >= -overlap && (sep == null || q > qsep)) {
            Vec.unitVector(normal,axis);
            sep = new Geom.NormalSeparator(normal,vmax2,vmin1,-1);
            qsep = q;
            if (any) return sep;
         }
      }

      if (sep != null) return sep;

   // try face normals

      // this seems like it ought to be true collision detection,
      // but it's not - think of the example of cubes with edges
      // touching crosswise.  but, it's pretty good!

      sep = tryFaceNormals(s1,s1,s2,normal);
      if (sep != null) return sep;

      sep = tryFaceNormals(s2,s1,s2,normal);
      if (sep != null) return sep;

   // can't find separator

      // System.out.println("Unable to find separator for shapes " + is1 + " and " + is2 + ".");
      return Geom.nullSeparator;
   }

   public static boolean isSeparated(Geom.Shape s1, Geom.Shape s2, GJKTester gjk) {

      // super-fast test, even better than the test in dynamicSeparate
      double d = s1.radius + s2.radius;
      if (Vec.dist2(s1.shapecenter,s2.shapecenter) >= d*d) return true;

      // if we get here, the spheres are in contact, so dynamicSeparate
      // will also fail and any separator we find would be useful there.
      // I thought about setting up some communication, but no, it'd be
      // too fragile.  for example: we might find some separators and then
      // have to cancel the motion because of something else.  we might
      // find a separator for a railcar, but railcars haven't animated yet.
      // it's just not worth getting into it.

      return (gjk.separate(s1, s2) != Geom.nullSeparator);
   }

   public static boolean isSeparated(Geom.Shape shape, LinkedList list, GJKTester gjk) {
      Iterator i = list.iterator();
      while (i.hasNext()) {
         if ( ! isSeparated(shape,(Geom.Shape) i.next(),gjk) ) return false;
      }
      return true;
   }

// --- dynamic separation ---

   /**
    * Do some geometry with spheres and the origin to quickly catch
    * a bunch of cases where neither shape is in front of the other.
    * It's quick because there's no iterating over vertices.
    *
    * @param normal A register, just with a different name for readability.
    * @return A value from the enumeration in Geom.Separator.
    */
   public static int dynamicSeparate(Geom.Shape s1, Geom.Shape s2, double[] origin, double[] reg1, double[] normal) {

      Vec.sub(normal,s2.shapecenter,s1.shapecenter);

      double d = Vec.norm(normal);
      double r1 = s1.radius;
      double r2 = s2.radius;
      if (d <= r1+r2) return Geom.Separator.UNKNOWN; // spheres are in contact

      double ratio = r1/(r1+r2);
      double dist1 = d*ratio;
      Vec.addScaled(reg1,s1.shapecenter,normal,ratio); // cone point
      Vec.sub(reg1,origin,reg1);

      double adj = Vec.dot(reg1,normal)/d;
      double neg = r1 - dist1;
      double pos = d - r2 - dist1;
      if (adj >= neg && adj <= pos) return Geom.Separator.NO_FRONT;

      // calc triangle parts for vec from cone point to origin
      double hyp2 = Vec.dot(reg1,reg1);
      double adj2 = adj*adj;
      double opp2 = hyp2-adj2;

      // the cone triangle has hyp = dist1 and opp = r1, so ...
      // (working with squares because sqrt is relatively slow)

      double rcone = r1/dist1;
      if (opp2 >= hyp2*rcone*rcone) return Geom.Separator.NO_FRONT;

      return (adj > 0) ? Geom.Separator.S2_FRONT : Geom.Separator.S1_FRONT;
      // save this for last since we want to detect NO_FRONT when possible
   }

// --- misc ---

   // these aren't exactly clipping functions, but they solve similar geometric problems

   public static double closestApproach(double[] p, double[] origin, double[] axis, double[] reg1) {
      // assume axis is normalized, then here's the answer
      Vec.sub(reg1,p,origin);
      double d = Vec.dot(reg1,axis);
      Vec.addScaled(reg1,reg1,axis,-d); // subScaled
      return Vec.dot(reg1,reg1);
   }

   /**
    * A fast test that you can use before the main clip function.
    */
   public static boolean outsideRadius(double[] p1, double[] p2, Geom.Shape shape) {
      double d = Vec.dist(p2,shape.shapecenter) - shape.radius; // distance from sphere
      if (d < 0) return false; // definitely not outside
      double s = Vec.dist(p1,p2);
      return (d >= s/2); // check this to prevent flythroughs
      // the furthest you can get on a flythrough is actually root(r^2+s^2)-r ~ s^2/2r,
      // but this is supposed to be a fast test, and s/2 is so small it hardly matters.
   }

   public static boolean projectToPlane(double[] dest, double[] origin, double[] viewAxis, double height) {
      Vec.copy(dest,origin);
      if (dest[1] < height) return false;
      if (dest[1] > height) {
         if (viewAxis[1] >= 0) return false; // pointing sideways or up
         Vec.addScaled(dest,dest,viewAxis,(height-dest[1])/viewAxis[1]);
      }
      // now y = height
      return true;
   }

// --- GJK algorithm ---

   public static class GJKTester {

      Geom.Shape[] s;
      double[][] p, reg;
      int[][] v;
      double[] t, n;
      final static double epsilon = 0.000001;

      public GJKTester(int dim) {
         s = new Geom.Shape[2];
         p = new double[dim+1][];
         v = new int[dim+1][];
         for (int i = 0; i <= dim; i++) {
            p[i] = new double[dim];
            v[i] = new int[2];
         }
         t = new double[2];
         n = new double[dim];
         reg = new double[2][];
         for (int i = 0; i < 2; i++) {
            reg[i] = new double[dim];
         }
         Geom.Shape[] s = new Geom.Shape[2];
      }

      public Geom.Separator separate(Geom.Shape s0, Geom.Shape s1) {
         s[0] = s0;
         s[1] = s1;
         n = new double[s0.getDimension()];
         Vec.sub(n, s[1].shapecenter, s[0].shapecenter);
         if (!Vec.normalizeTry(n, n)) return Geom.nullSeparator;
         v[0][0] = 0; v[0][1] = 0;
         minkSupport(0, 0);
         Vec.scale(n, n, -1);
         minkSupport(0, 1);
         if (Vec.dot(n, p[1]) < epsilon) {System.out.println(t[1] + t[0]); return new Geom.NormalSeparator(n, t[1], -t[0], -1);}
         Vec.cross(n, p[0], p[1]);
         if (!Vec.normalizeTry(n, n)) return Geom.nullSeparator;
         if (Vec.dot(n, p[0]) > 0) Vec.scale(n, n, -1);
         minkSupport(0, 2);
         if (Vec.dot(n, p[2]) < epsilon) {System.out.println(t[1] + t[0]); return new Geom.NormalSeparator(n, t[1], -t[0], -1);}
         Vec.sub(reg[0], p[1], p[0]);
         Vec.sub(reg[1], p[2], p[0]);
         Vec.cross(n, reg[0], reg[1]);
         Vec.normalize(n, n);
         if (Vec.dot(n, p[0]) > 0) {
            Vec.swap(p[1], p[2], reg[0]);
            int i = v[1][0]; v[1][0] = v[2][0]; v[2][0] = i;
                i = v[1][1]; v[1][1] = v[2][1]; v[2][1] = i;
            Vec.scale(n, n, -1);
         }
         minkSupport(1, 3);
         if (Vec.dot(n, p[3]) < epsilon) {System.out.println(t[1] + t[0]); return new Geom.NormalSeparator(n, t[1], -t[0], -1);}
         label: for (int count = 0; count < 20; count++) {
            for (int i = 0; i < 3; i++) {
               int a = (i + 1) % 3; int b = (i + 2) % 3;
               Vec.sub(reg[0], p[a], p[3]);
               Vec.sub(reg[1], p[b], p[3]);
               Vec.cross(n, reg[0], reg[1]);
               Vec.normalize(n, n);
               if (Vec.dot(n, p[3]) < epsilon) {
                  Vec.copy(p[i], p[3]); v[1][0] = v[3][0]; v[1][1] = v[3][0];
                  minkSupport(i, 3);
                  if (Vec.dot(n, p[3]) < epsilon) {System.out.println(t[1] + t[0]); return new Geom.NormalSeparator(n, t[1], -t[0], -1);}
                  continue label;
               }
            }
            return Geom.nullSeparator;
         }
         {System.out.println(t[1] + t[0]); return new Geom.NormalSeparator(n, t[1], -t[0], -1);}
      }

      private void minkSupport(int from, int to) {
         support(p[to], false, from, to, 1);
         support(reg[0], true, from, to, 0);
         Vec.sub(p[to], p[to], reg[0]);
      }

      private void support(double[] dest, boolean inv, int from, int to, int r) {
         Vec.scale(reg[1], n, (inv) ? -1 : 1);
         int next = v[from][r];
         int now = -1;
         int prev;
         double m = Vec.dot(s[r].vertex[next], reg[1]);
         while (next != now) {
            prev = now;
            now = next;
            for (int i : s[r].nbv[now]) {
               if (i == prev) continue;
               double d = Vec.dot(s[r].vertex[i], reg[1]);
               if (d > m) {
                  m = d;
                  next = i;
               }
            }
         }
         Vec.copy(dest, s[r].vertex[now]);
         t[r] = m;
         v[to][r] = now;
      }
   }

// --- test code ---

   public static void main(String[] args) {

      double x0 = Double.parseDouble(args[0]);
      double x1 = Double.parseDouble(args[1]);
      double yStart = Double.parseDouble(args[2]);
      double yStep  = Double.parseDouble(args[3]);
      double yEnd   = Double.parseDouble(args[4]);

      final double[][] normal = new double[][] { {-1,0}, {-1,1}, {1,1}, {1,0}, {1,-1}, {-1,-2} };
      final double[] threshold = new double[] { 2, 3, 3, 1, 1, 2 };
      BoundaryList boundaryList = new BoundaryList() {
         public int getSize() { return normal.length; }
         public Boundary getBoundary(final int i) { return new Boundary() {
            public double[] getNormal() { return normal[i]; }
            public double getThreshold() { return threshold[i]; }
         }; }
      };

      double[] p0 = new double[2];
      double[] p1 = new double[2];
      p0[0] = x0;
      p1[0] = x1;
      Clip.Result result = new Clip.Result();

      for (double y=yStart; y<=yEnd; y += yStep) {
         p0[1] = y;
         p1[1] = y;
         clip(p0,p1,boundaryList,result);
         System.out.print(y + " : ");
         if      (result.clip == KEEP_LINE) System.out.println("all safe");
         else if (result.clip == KEEP_NONE) System.out.println("all clipped");
         else {
            if ((result.clip & KEEP_A) != 0) System.out.print("[" + x0 + "," + (x0+result.a*(x1-x0)) + "] ");
            if ((result.clip & KEEP_B) != 0) System.out.print("[" + (x0+result.b*(x1-x0)) + "," + x1 + "]" );
            System.out.println();
         }
      }
   }

}

