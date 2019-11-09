/*
 * Geom.java
 */

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Classes that are used to represent geometric shapes.
 * The same representation works for both 3D and 4D since we don't (currently) care about 2D faces in 4D.
 */

public class Geom {

// --- helpers ---

   public static double[] clone1(double[] d) {
      return (double[]) d.clone();
   }

   public static double[][] clone2(double[][] d) {
      d = (double[][]) d.clone();
      for (int i=0; i<d.length; i++) d[i] = (double[]) d[i].clone();
      return d;
   }

   public static Face[] clone2(Face[] f) {
      f = (Face[]) f.clone();
      for (int i=0; i<f.length; i++) f[i] = f[i].copy();
      return f;
   }

   public static Edge[] clone2(Edge[] e) {
      e = (Edge[]) e.clone();
      for (int i=0; i<e.length; i++) e[i] = e[i].copy();
      return e;
   }

   public static int[][] clone2(int[][] j) {
      j = (int[][]) j.clone();
      for (int i=0; i<j.length; i++) j[i] = (int[]) j[i].clone();
      return j;
   }

   private static void vatranslate(double[][] vertex, double[] d) {
      for (int i=0; i<vertex.length; i++) Vec.add(vertex[i],vertex[i],d);
   }

   private static void vascale(double[][] vertex, double[] d) {
      for (int i=0; i<vertex.length; i++) Vec.scaleMultiCo(vertex[i],vertex[i],d);
   }

   private static void varotate(double[][] vertex, int dir1, int dir2, double theta, double[] origin) {
      for (int i=0; i<vertex.length; i++) vrotate(vertex[i],dir1,dir2,theta,origin);
   }

   private static void vrotate(double[] vec, int dir1, int dir2, double theta, double[] origin) {
      Vec.sub(vec,vec,origin);
      Vec.rotateAbsoluteAngleDir(vec,vec,dir1,dir2,theta);
      Vec.add(vec,vec,origin);
   }

   public static Color getColor(Color c1) {
      if (c1 != null) return c1;
      return Color.green; // the default color
   }

   public static Color getColor(Color c1, Color c2) {
      if (c1 != null) return c1;
      if (c2 != null) return c2;
      return Color.green; // the default color
   }

// --- move interface ---

   public interface MoveInterface {

      boolean noUserMove();
      void translate(double[] d);
      void scale(double[] d);
      void rotate(int dir1, int dir2, double theta, double[] origin);
   }

// --- shape interface ---

   // shared interface for Shape and CompositeShape

   public interface ShapeInterface extends MoveInterface, IDimensionMultiSrc {

      void unglue(Collection c);
      ShapeInterface copySI();
      void setNoUserMove();
      // move interface functions go here
      double[] getAlignCenter();
      void glass();
      void setShapeColor(Color color);
      int setFaceColor(int j, Color color, boolean xor);
      int setEdgeColor(int j, Color color);
      int setFaceTexture(int j, Texture texture, int mode, double[] value);

      ShapeInterface prism  (            int a, double min, double max            );
      ShapeInterface frustum(double[] p, int a, double min, double max, double tip);
      ShapeInterface cone   (double[] p, int a, double min, double max            );
   }

// --- composite shape ---

   public static class CompositeShape implements ShapeInterface {

      public ShapeInterface[] component;

      public CompositeShape(ShapeInterface[] component) {
         this.component = component;
      }

      public ShapeInterface getComponent(int i) { return component[i]; } // usually not OK

      public void unglue(Collection c) {
         for (int i=0; i<component.length; i++) component[i].unglue(c);
      }

      public ShapeInterface copySI() {
         ShapeInterface[] cnew = new ShapeInterface[component.length];
         for (int i=0; i<component.length; i++) cnew[i] = component[i].copySI();
         return new CompositeShape(cnew);
      }

      public void setNoUserMove() {
         for (int i=0; i<component.length; i++) component[i].setNoUserMove();
      }

      public boolean noUserMove() {
         for (int i=0; i<component.length; i++) { if (component[i].noUserMove()) return true; }
         return false;
      }

      public void translate(double[] d) {
         for (int i=0; i<component.length; i++) component[i].translate(d);
      }

      public void scale(double[] d) {
         for (int i=0; i<component.length; i++) component[i].scale(d);
      }

      public double[] getAlignCenter() {
         return component[0].getAlignCenter(); // stupid but it'll do for now
      }

      public void rotate(int dir1, int dir2, double theta, double[] origin) {
         if (origin == null) origin = clone1(getAlignCenter()); // use same origin for all rotations
         for (int i=0; i<component.length; i++) component[i].rotate(dir1,dir2,theta,origin);
      }

      public void glass() {
         for (int i=0; i<component.length; i++) component[i].glass();
      }

      public void setShapeColor(Color color) {
         for (int i=0; i<component.length; i++) component[i].setShapeColor(color);
      }

      public int setFaceColor(int j, Color color, boolean xor) {
         for (int i=0; i<component.length; i++) { j = component[i].setFaceColor(j,color,xor); if (j < 0) break; }
         return j;
      }

      public int setEdgeColor(int j, Color color) {
         for (int i=0; i<component.length; i++) { j = component[i].setEdgeColor(j,color); if (j < 0) break; }
         return j;
      }

      public int setFaceTexture(int j, Texture texture, int mode, double[] value) {
         for (int i=0; i<component.length; i++) { j = component[i].setFaceTexture(j,texture,mode,value); if (j < 0) break; }
         return j;
      }

      public void getDimension(IDimensionMultiDest dest) {
         for (int i=0; i<component.length; i++) component[i].getDimension(dest);
      }

      public ShapeInterface prism(int a, double min, double max) {
         ShapeInterface[] cnew = new ShapeInterface[component.length];
         for (int i=0; i<component.length; i++) cnew[i] = component[i].prism(a,min,max);
         return new CompositeShape(cnew);
      }

      public ShapeInterface frustum(double[] p, int a, double min, double max, double tip) {
         ShapeInterface[] cnew = new ShapeInterface[component.length];
         for (int i=0; i<component.length; i++) cnew[i] = component[i].frustum(p,a,min,max,tip);
         return new CompositeShape(cnew);
      }

      public ShapeInterface cone(double[] p, int a, double min, double max) {
         ShapeInterface[] cnew = new ShapeInterface[component.length];
         for (int i=0; i<component.length; i++) cnew[i] = component[i].cone(p,a,min,max);
         return new CompositeShape(cnew);
      }
   }

// --- hint interface ---

   public interface HintInterface {
      /**
       * @param invert 1 if the owner is S1, or -1 if the owner is S2.
       */
      Separator getHint(Shape owner, Shape target, int invert);
   }

   // these are little things that get attached to shapes to tell how to
   // find separators in tough cases.  "hint" is a good name except that
   // they're more like commands than suggestions.

// --- place helper ---

   // could generalize and use this same interface for rotations
   // and other things, but for now it's just for the place call

   public static class PlaceHelper {

      private double[][] axis;
      private double[] originNew;
      private double[] originOld;
      private double[] reg1;

      public PlaceHelper(double[][] axis, double[] originNew, double[] originOld) {
         this.axis = axis;
         this.originNew = originNew;
         this.originOld = originOld;
         reg1 = new double[axis.length];
      }

      /**
       * Change originNew to make src map to dest.
       */
      public void solvePos(double[] dest, double[] src) {
         Vec.sub(reg1,src,originOld);
         Vec.fromAxisCoordinates(originNew,reg1,axis);
         Vec.addScaled(originNew,dest,originNew,-1);
      }

      public void placePos(double[] dest, double[] src) {
         Vec.sub(reg1,src,originOld);
         Vec.fromAxisCoordinates(dest,reg1,axis);
         Vec.add(dest,dest,originNew);
      }

      public void placeDir(double[] dest, double[] src) {
         Vec.fromAxisCoordinates(dest,src,axis);
      }

      public void idealPos(double[] dest, double[] src) {
         Vec.sub(reg1,src,originNew);
         Vec.toAxisCoordinates(dest,reg1,axis);
         Vec.add(dest,dest,originOld);
      }
   }

// --- shape ---

   public static class Shape implements ShapeInterface, Clip.BoundaryList, IDimension {

      public Face[] face;
      public Subface[] subface;
      public Edge[] edge;
      public double[][] vertex;
      public int[][] nbv;
      public double[] shapecenter; // for size testing; connected to radius
      public double[] aligncenter; // for alignment, snapping, and rotation
      public double radius;
      public double[][] axis; // for block motion only
      public Shape ideal;
      public boolean systemMove;
      public boolean noUserMove;
      public HintInterface hint;
      public Face bottomFace; // for railcars only

      private Shape() {}
      public Shape(Face[] face, Edge[] edge, double[][] vertex) {
         this.face = face;
         this.edge = edge;
         this.vertex = vertex;
         calculate();
         ideal = copy(); // so, the only difference right now is that the ideal has no ideal
      }

      public void idealize() {
         Vec.unitMatrix(axis);
         ideal = null; // so new ideal won't point to old ideal
         ideal = copy();
      }

      public void editIdeal() {
         ideal = ideal.copy(); // no way to tell if it's shared, so we have to copy
      }

      public int getSize() { return face.length; }
      public Clip.Boundary getBoundary(int i) { return face[i]; }

      public void unglue(Collection c) {
         c.add(this);
      }

      public ShapeInterface copySI() {
         return copy();
      }

      public Shape copy() {
         Shape s = new Shape();

         s.face = clone2(face);
         s.subface = subface; // share
         s.edge = clone2(edge);
         s.vertex = clone2(vertex);
         s.nbv = clone2(nbv);
         s.shapecenter = clone1(shapecenter);
         s.aligncenter = clone1(aligncenter);
         s.radius = radius;
         s.axis = clone2(axis);

         s.ideal = ideal; // share ideals!  we'll make a new copy
         // whenever we do anything that needs to alter the ideal.
         // also note, ideals rarely change, it's just memory.

         // systemMove and noUserMove don't need to be copied.
         // systemMove is for train cars, and the copy won't be one.
         // noUserMove is for platform textures
         // (and ramps by analogy), and the copy will lose that.

         return s;
      }

      public void setNoUserMove() {
         noUserMove = true;
      }

      public boolean noUserMove() {
         return noUserMove;
      }

      public void reset() {
         // use this to avoid accumulation of FP error in trains
         for (int i=0; i<face.length; i++) face[i].reset(ideal.face[i]);
         for (int i=0; i<vertex.length; i++) Vec.copy(vertex[i],ideal.vertex[i]);
         Vec.copy(shapecenter,ideal.shapecenter);
         Vec.copy(aligncenter,ideal.aligncenter);
         // radius doesn't change under these transformations
         for (int i=0; i<axis.length; i++) Vec.copy(axis[i],ideal.axis[i]);
      }

      /**
       * Place the rest of the shape after you've modified aligncenter and axis.
       */
      public void place() {
         place(/* useShapeCenter = */ false);
      }
      public void place(boolean useShapeCenter) {
         PlaceHelper helper = new PlaceHelper(axis,aligncenter,ideal.aligncenter);

         if (useShapeCenter) {
            helper.solvePos(shapecenter,ideal.shapecenter);
            // must go first since it changes aligncenter!
         } else {
            helper.placePos(shapecenter,ideal.shapecenter);
         }

         for (int i=0; i<face.length; i++) face[i].place(ideal.face[i],helper);
         for (int i=0; i<vertex.length; i++) helper.placePos(vertex[i],ideal.vertex[i]);
         // we took care of shapecenter and aligncenter above
         // radius doesn't change under these transformations
         // axis is fixed
      }

      public void place(double[] d, double[][] a) {
         Vec.copy(aligncenter,d);
         Vec.copyMatrix(axis,a);
         // note, copyMatrix uses dest size to copy,
         // so it's OK if matrix vectors are larger
         place();
      }

      public void translate(double[] d) {
         for (int i=0; i<face.length; i++) face[i].translate(d);
         vatranslate(vertex,d);
         Vec.add(shapecenter,shapecenter,d);
         Vec.add(aligncenter,aligncenter,d);
         // radius doesn't change
         // the axes don't change
      }

      public void translateFrame(double[] d) {
         // do aligncenter and axes now, update the rest later
         Vec.add(aligncenter,aligncenter,d);
      }

      public void scale(double[] d) {
         for (int i=0; i<face.length; i++) face[i].scale(d);
         vascale(vertex,d);
         Vec.scaleMultiCo(shapecenter,shapecenter,d);
         Vec.scaleMultiCo(aligncenter,aligncenter,d);
         if (isUniform(d)) radius *= d[0]; else calcRadius();

         // what about the axes?  in general they're not well defined,
         // but it doesn't matter here.  scaling is a change of shape,
         // so we have to idealize and reset the axes anyway.
         idealize();
      }

      private static boolean isUniform(double[] d) {
         for (int i=1; i<d.length; i++) {
            if (d[i] != d[0]) return false;
         }
         return true;
      }

      public double[] getAlignCenter() {
         return aligncenter;
      }

      public void setAlignCenter(double[] aligncenter) {
         Vec.copy(this.aligncenter,aligncenter);

         // we have to make a copy of the ideal anyway since we can't
         // tell whether it's shared, so keep it simple and idealize.
         // the only difference is that the axes get reset, and you probably
         // shouldn't set the align center after inexact rotations anyway.
         idealize();
      }

      public void rotate(int dir1, int dir2, double theta, double[] origin) {
         if (origin == null) origin = clone1(getAlignCenter());
         // since rotations are orthogonal, covariant vs. contravariant doesn't matter
         for (int i=0; i<face.length; i++) face[i].rotate(dir1,dir2,theta,origin);
         varotate(vertex,dir1,dir2,theta,origin);
         vrotate(shapecenter,dir1,dir2,theta,origin); // the clone1 call prevents this step from going awry
         vrotate(aligncenter,dir1,dir2,theta,origin);
         // radius doesn't change
         for (int i=0; i<axis.length; i++) Vec.rotateAbsoluteAngleDir(axis[i],axis[i],dir1,dir2,theta);
            // no origin shift for axes!
      }

      public void rotateFrame(int dir1, int dir2, double theta, double[] origin) {
         // do aligncenter and axes now, update the rest later
         if (origin == null) origin = clone1(getAlignCenter());
         vrotate(aligncenter,dir1,dir2,theta,origin);
         for (int i=0; i<axis.length; i++) Vec.rotateAbsoluteAngleDir(axis[i],axis[i],dir1,dir2,theta);
      }

      public void glass() {
         glassImpl();

         // this is something you might reasonably do in inexact coordinates, so
         // don't idealize, just make corresponding changes to the current ideal.
         editIdeal();
         ideal.glassImpl();
      }

      private void glassImpl() {
         for (int i=0; i<face.length; i++) face[i].normal = null;
      }

      public void calculate() {
         calcCenters();
         calcSubfaces();
         calcNeighbors();

         int dim = getDimension();

         shapecenter = new double[dim];
         for (int i=0; i<vertex.length; i++) Vec.add(shapecenter,shapecenter,vertex[i]);
         Vec.scale(shapecenter,shapecenter,1/(double) vertex.length);
         // not always the best center value, but we only use it for separators
         aligncenter = clone1(shapecenter);

         calcRadius(); // uses shapecenter

         axis = new double[dim][dim];
         Vec.unitMatrix(axis);
      }

      public void calcRadius() {
         double[] temp = new double[getDimension()];
         double r = 0;
         for (int i=0; i<vertex.length; i++) {
            Vec.sub(temp,vertex[i],shapecenter);
            double d = Vec.norm(temp);
            if (d > r) r = d;
         }
         radius = r;
      }

      public void calcCenters() {
         for (int i=0; i<face.length; i++) calcCenter(face[i]);
      }

      public boolean[] getFaceVertices(Face f) {
         boolean[] b = new boolean[vertex.length];
         for (int i=0; i<f.ie.length; i++) {
            Edge e = edge[f.ie[i]];
            b[e.iv1] = true;
            b[e.iv2] = true;
         }
         return b;
      }

      public double vmin(boolean[] b, int axis) {
         double d = 0;
         boolean first = true;
         for (int i=0; i<vertex.length; i++) {
            if ( ! b[i] ) continue;
            double temp = vertex[i][axis];
            if (first) {
               d = temp;
               first = false;
            } else {
               if (temp < d) d = temp;
            }
         }
         return d;
      }

      public double vmax(boolean[] b, int axis) {
         double d = 0;
         boolean first = true;
         for (int i=0; i<vertex.length; i++) {
            if ( ! b[i] ) continue;
            double temp = vertex[i][axis];
            if (first) {
               d = temp;
               first = false;
            } else {
               if (temp > d) d = temp;
            }
         }
         return d;
      }

      public void calcCenter(Face f) {

         // find what vertices are involved and average them

         boolean[] b = getFaceVertices(f);

         double[] d = null;
         int n = 0;

         for (int i=0; i<vertex.length; i++) {
            if ( ! b[i] ) continue;
            if (d == null) d = clone1(vertex[i]);
            else Vec.add(d,d,vertex[i]);
            n++;
         }

         Vec.scale(d,d,1/(double) n);
         f.center = d;
         f.calcThreshold();
      }

      public void getDimension(IDimensionMultiDest dest) {
         dest.putDimension(getDimension());
      }

      public int getDimension() {
         return vertex[0].length; // ugly but it will do for now
      }

      public void calcSubfaces() {

         // in 3D, we get a subface when two faces have an edge in common.
         // in 4D, we get a subface when two faces have two or more edges
         // in common.

         LinkedList list = new LinkedList();
         int nGoal = (getDimension() == 3) ? 1 : 2; // only place we test dimension!

         for (int i1=0; i1<face.length-1; i1++) {
            for (int i2=i1+1; i2<face.length; i2++) {
               if (countEdgesInCommon(face[i1].ie,face[i2].ie) >= nGoal) {
                  Subface sf = new Subface();
                  sf.if1 = i1;
                  sf.if2 = i2;
                  list.add(sf);
               }
            }
         }

         subface = (Subface[]) list.toArray(new Subface[list.size()]);
      }

      public int countEdgesInCommon(int[] ie1, int[] ie2) {

         // if we knew the arrays were sorted we could do
         // some kind of fast walk-through, but since the
         // calculation only happens once it hardly matters.

         int n = 0;
         for (int i1=0; i1<ie1.length; i1++) {
            for (int i2=0; i2<ie2.length; i2++) {
               if (ie1[i1] == ie2[i2]) n++;
            }
         }
         return n;
      }

      public void calcNeighbors() {
         LinkedList[] list = new LinkedList[vertex.length];
         for (int i = 0; i < vertex.length; i++) {
            list[i] = new LinkedList();
         }
         for (int i = 0; i < edge.length; i++) {
            list[edge[i].iv1].add(edge[i].iv2);
            list[edge[i].iv2].add(edge[i].iv1);
         }
         nbv = new int[vertex.length][];
         for (int i = 0; i < vertex.length; i++) {
            nbv[i] = new int[list[i].size()];
            for (int j = 0; j < nbv[i].length; j++) {
               nbv[i][j] = (int) list[i].get(j);
            }
         }
      }

      public void setShapeColor(Color color) {
         for (int i=0; i<face.length; i++) {
            face[i].color = color;
         }
         for (int i=0; i<edge.length; i++) { // a bit faster than updateEdgeColor per face
            edge[i].color = color;
         }
      }

      public int setFaceColor(int j, Color color, boolean xor) {
         if (j < face.length) {
            if (xor && color != null && color.equals(face[j].color)) color = null;
            face[j].color = color;
            updateEdgeColor(face[j]); // resolve conflicts by overwriting
         }
         return j - face.length;
      }

      public int setEdgeColor(int j, Color color) {
         if (j < edge.length) {
            edge[j].color = color;
         }
         return j - edge.length;
      }

      public int setFaceTexture(int j, Texture texture, int mode, double[] value) {
         if (j < face.length) {

            if (    texture != null
                 && mode != Vec.PROJ_NONE
                 && face[j].normal != null ) {

               texture.project(face[j].normal,face[j].threshold,mode,value);
               texture.clip(this,face[j]);
               // clip after project is right order
            }
            // what about the other cases?
            // 1. don't project null
            // 2. PROJ_NONE is debug mode, want to see the whole texture
            // 3. if we don't have a face normal, project makes no sense

            face[j].customTexture = texture;

            // this is something you might reasonably do in inexact coordinates, so
            // don't idealize, just make corresponding changes to the current ideal.
            // we aren't too sensitive to FP error in texture vertices, so transform
            // should be no problem.
            editIdeal();
            if (texture != null) {
               PlaceHelper helper = new PlaceHelper(axis,aligncenter,ideal.aligncenter);
               ideal.face[j].customTexture = texture.toIdeal(helper);
            } else {
               ideal.face[j].customTexture = null;
            }
         }
         return j - face.length;
      }

      public void updateEdgeColor(Face f) {
         for (int i=0; i<f.ie.length; i++) {
            edge[f.ie[i]].color = f.color;
         }
      }

      public int findFace(int axis, int sign) {
         for (int i=0; i<face.length; i++) {
            if (match(face[i].normal,axis,sign)) return i;
         }
         return -1;
      }

      private static boolean match(double[] normal, int axis, int sign) {
         if (normal == null) return false;
         final double e1 = 0.1; // epsilons, use two so there's no ambiguity
         final double e2 = 0.000001;
         for (int i=0; i<normal.length; i++) {
            if (i == axis) {
               if (normal[i]*sign < e1) return false;
            } else {
               if (Math.abs(normal[i]) > e2) return false;
            }
         }
         return true;
      }

      public ShapeInterface prism  (            int a, double min, double max            ) { return GeomUtil.prism  (this,  a,min,max    ); }
      public ShapeInterface frustum(double[] p, int a, double min, double max, double tip) { return GeomUtil.frustum(this,p,a,min,max,tip); }
      public ShapeInterface cone   (double[] p, int a, double min, double max            ) { return GeomUtil.cone   (this,p,a,min,max    ); }
   }

// --- texture interface ---

   // originally I imagined having all kinds of custom texture objects,
   // but in practice there are only two kinds: Geom.Texture instances
   // that the user can manipulate and elevated platform textures
   // that have limited functionality and are poked into place by code.
   // don't overgeneralize!

   public interface CustomTexture {
      void draw(Shape shape, Face face, IDraw currentDraw, double[] origin);
   }

// --- texture ---

   // vertices and edges with no other structure,
   // also useful for scenery.
   // edge colors might not be used in that case.

   public static class Texture implements CustomTexture, MoveInterface {

      public Edge[] edge;
      public double[][] vertex;

      public Texture() {}
      public Texture(Edge[] edge, double[][] vertex) {
         this.edge = edge;
         this.vertex = vertex;
      }

      public Texture copy() {
         Texture t = new Texture();
         t.edge = clone2(edge);
         t.vertex = clone2(vertex);
         return t;
      }

      public Texture toIdeal(PlaceHelper helper) {
         if (vertex.length == 0) return copy(); // avoid error from empty texture and getDimension
         Texture t = new Texture();
         t.edge = clone2(edge);
         t.vertex = new double[vertex.length][getDimension()];
         for (int i=0; i<vertex.length; i++) {
            helper.idealPos(t.vertex[i],vertex[i]);
         }
         return t;
      }

      public void setTextureColor(Color color) {
         for (int i=0; i<edge.length; i++) {
            edge[i].color = color;
         }
      }

      public int getDimension() {
         return vertex[0].length; // ugly but it will do for now
      }

      public Texture normalize() {
         for (int i=0; i<vertex.length; i++) {
            Vec.normalize(vertex[i],vertex[i]);
         }
         return this; // convenience
      }

      public void project(double[] normal, double threshold, int mode, double[] value) {
         for (int i=0; i<vertex.length; i++) {
            Vec.project(vertex[i],vertex[i],normal,threshold,mode,value);
         }
      }

      public void draw(Shape shape, Face face, IDraw currentDraw, double[] origin) {
         for (int i=0; i<edge.length; i++) {
            Edge e = edge[i];
            currentDraw.drawLine(vertex[e.iv1],vertex[e.iv2],getColor(e.color,face.color),origin);
         }
      }

      public void reset(Texture ideal) {
         for (int i=0; i<vertex.length; i++) Vec.copy(vertex[i],ideal.vertex[i]);
      }

      public void place(Texture ideal, PlaceHelper helper) {
         for (int i=0; i<vertex.length; i++) helper.placePos(vertex[i],ideal.vertex[i]);
      }

      public boolean noUserMove() {
         return false;
      }

      public void translate(double[] d) {
         vatranslate(vertex,d);
      }

      public void scale(double[] d) {
         vascale(vertex,d);
      }

      public void rotate(int dir1, int dir2, double theta, double[] origin) {
         if (origin == null) origin = clone1(vertex[0]); // no shapeCenter
         varotate(vertex,dir1,dir2,theta,origin);
      }

      public Texture union(Texture texture) { return union(texture,/* preadded = */ false); }
      public Texture union(Texture texture, boolean preadded) {

         Texture t1 = this;
         Texture t2 = texture;

         // we do want to unify equal vertices so that for example we don't
         // draw the same vertical rays several times, but that should wait
         // until all the unions are done.

         double[][] vNew = new double[t1.vertex.length+t2.vertex.length][];
         System.arraycopy(t1.vertex,0,vNew,0,               t1.vertex.length);
         System.arraycopy(t2.vertex,0,vNew,t1.vertex.length,t2.vertex.length);

         Edge[] eNew = new Edge[t1.edge.length+t2.edge.length];
         System.arraycopy(t1.edge,0,eNew,0,             t1.edge.length);
         if (preadded) {
         System.arraycopy(t2.edge,0,eNew,t1.edge.length,t2.edge.length);
         } else {
            // that's the idea, but actually we have to adjust
            // the t2 edges so they point at the new t2 vertices
            for (int i=0; i<t2.edge.length; i++) {
               eNew[t1.edge.length+i] = new Edge(t2.edge[i].iv1 + t1.vertex.length,
                                                 t2.edge[i].iv2 + t1.vertex.length,
                                                 t2.edge[i].color);
            }
         }

         return new Texture(eNew,vNew);
      }

      public Texture merge() {

         Builder b = new Builder(true,true,false,-1);
         int[] vmap = new int[vertex.length];

         for (int i=0; i<vertex.length; i++) {
            vmap[i] = b.addVertex(vertex[i]);
         }

         for (int i=0; i<edge.length; i++) {
            b.addEdge(new Edge(vmap[edge[i].iv1],vmap[edge[i].iv2],edge[i].color));
         }

         edge = b.toEdgeArray();
         vertex = b.toVertexArray();

         return this; // convenience
      }

      public boolean clip(Clip.BoundaryList boundaryList, Clip.Boundary exceptBoundary, double[] p1, double[] p2) {
         for (int i=0; i<boundaryList.getSize(); i++) {
            Clip.Boundary b = boundaryList.getBoundary(i);
            if (b == exceptBoundary) continue;
            double[] normal = b.getNormal();
            if (normal == null) continue;
            if (Vec.clip(p1,p2,normal,b.getThreshold(),/* sign = */ -1)) return true;
         }
         return false;
      }

      public void clip(Clip.BoundaryList boundaryList, Clip.Boundary exceptBoundary) {
         if (vertex.length == 0) return; // avoid error from empty texture and getDimension

         Builder b = new Builder(true,true,false,-1);

         int dim = getDimension();
         double[] p1 = new double[dim];
         double[] p2 = new double[dim];

         for (int i=0; i<edge.length; i++) {
            Edge e = edge[i];

            Vec.copy(p1,vertex[e.iv1]);
            Vec.copy(p2,vertex[e.iv2]);
            if (clip(boundaryList,exceptBoundary,p1,p2)) continue; // fully clipped

            // there's no real correspondence between vertices,
            // just add whatever new ones we need
            int iv1 = b.addVertex((double[]) p1.clone());
            int iv2 = b.addVertex((double[]) p2.clone());
            b.addEdge(new Edge(iv1,iv2,e.color));
         }

         edge = b.toEdgeArray();
         vertex = b.toVertexArray();
      }
   }

// --- builder ---

   // can use this to build both shapes and textures

   public static class Builder {

      private Vector vnew;
      private Vector enew;
      private Vector fnew;

      // face vertex accumulator
      private int[] aiv;
      private int   alen;

      public Builder(boolean v, boolean e, boolean f, int amax) {
         if (v) vnew = new Vector();
         if (e) enew = new Vector();
         if (f) fnew = new Vector();
         if (amax != -1) aiv = new int[amax];
      }

   // vertex functions

      private int indexOf(double[] v) {
         final double epsilon = 0.001;
         for (int i=0; i<vnew.size(); i++) {
            if (Vec.approximatelyEquals((double[]) vnew.get(i),v,epsilon)) return i;
         }
         return -1;
      }

      public int addVertex(double[] v) {
         int index = indexOf(v);
         if (index == -1) {
            index = vnew.size();
            vnew.add(v);
         }
         return index;
      }

      public double[][] toVertexArray() {
         return (double[][]) vnew.toArray(new double[vnew.size()][]);
      }

   // edge functions (that don't call vertex functions at all)

      private int indexOf(int iv1, int iv2) {
         for (int i=0; i<enew.size(); i++) {
            if (((Edge) enew.get(i)).sameVertices(iv1,iv2)) return i;
            // can't do anything about the color;
            // it's the same edge, one color has to win out.
         }
         return -1;
      }

      public int addEdge(int iv1, int iv2) {
         int index = indexOf(iv1,iv2);
         if (index == -1) {
            index = enew.size();
            enew.add(new Edge(iv1,iv2));
         }
         return index;
      }

      public int addEdge(Edge e) {
         int index = indexOf(e.iv1,e.iv2);
         if (index == -1) {
            index = enew.size();
            enew.add(e);
         }
         return index;
      }

      public Edge[] toEdgeArray() {
         return (Edge[]) enew.toArray(new Edge[enew.size()]);
      }

   // face functions (that do call edge functions)

      // of course there's some similarity to GeomUtil.edgeRing,
      // but there are big differences too.  here we're making
      // one face of a three-dimensional object, not a complete
      // two-dimensional object, so there's just one face, not
      // one per edge, and the normals aren't in the face plane.

      public Face makeFace(int[] iv) {
         return makeFace(iv,iv.length);
      }

      /**
       * @param len The number of elements of iv to use.
       */
      public Face makeFace(int[] iv, int len) {
         Face f = new Face();

         f.ie = new int[len];
         int n = 0;
         for (int i=0; i<len; i++) {
            int iv1 = iv[i];
            int iv2 = iv[(i+1)%len];
            if (iv2 != iv1) {
               f.ie[n++] = addEdge(iv1,iv2);
            }
            // else it was duplicate
         }

         if (n != len) {
            int[] temp = new int[n];
            System.arraycopy(f.ie,0,temp,0,n);
            f.ie = temp;
            // rare, so OK to be inefficient
         }

         f.normal = new double[3];

         fnew.add(f);
         return f;
      }

      public Face[] toFaceArray() {
         return (Face[]) fnew.toArray(new Face[fnew.size()]);
      }

   // face vertex functions (that call face, edge, and maybe vertex functions)

      public void addFaceVertex(double[] v) {
         addFaceVertex(addVertex(v));
      }

      public void addFaceVertex(int iv) {
         aiv[alen++] = iv;
      }

      public Face makeFace() {
         Face f = makeFace(aiv,alen);
         alen = 0;
         return f;
      }
   }

// --- face ---

   public static class Face implements Clip.Boundary {

      public int[] ie; // indices of edges
      public double[] center; // same as vertex, but we can compute them.
      public double[] normal; // different from vertex under translation;
      //                      // direction can be computed but sign is a pain, not worth it.
      public double threshold;
      public Color color;
      public CustomTexture customTexture;

      // runtime fields
      public boolean visible;

      public double[] getNormal() { return normal; }
      public double getThreshold() { return threshold; }
      public void calcThreshold() { threshold = (normal != null) ? Vec.dot(center,normal) : 0; }

      public Face copy() {
         Face f = new Face();
         f.ie = ie; // share
         f.center = clone1(center); // don't copy before adding to shape
         f.normal = (normal != null) ? clone1(normal) : null;
         f.threshold = threshold;
         f.color = color;
         if (customTexture instanceof Geom.Texture) {
            f.customTexture = ((Geom.Texture) customTexture).copy();
         }
         // visible gets recalculated as needed
         return f;
      }

      public int[] getVertices(Edge[] e, int nv) {
         // stupid but just get it done
         boolean[] b = new boolean[nv];
         for (int i=0; i<ie.length; i++) {
            b[e[ie[i]].iv1] = true;
            b[e[ie[i]].iv2] = true;
         }
         int fv = 0;
         for (int i=0; i<nv; i++) {
            if (b[i]) fv++;
         }
         int[] iv = new int[fv];
         int j = 0;
         for (int i=0; i<nv; i++) {
            if (b[i]) iv[j++] = i;
         }
         return iv;
      }

      public void reset(Face ideal) {
         Vec.copy(center,ideal.center);
         if (ideal.normal != null) {
            Vec.copy(normal,ideal.normal);
         } else {
            normal = null;
         }
         threshold = ideal.threshold;
         if (customTexture instanceof Geom.Texture) {
            ((Geom.Texture) customTexture).reset((Geom.Texture) ideal.customTexture);
         }
      }

      public void place(Face ideal, PlaceHelper helper) {
         helper.placePos(center,ideal.center);
         if (ideal.normal != null) {
            helper.placeDir(normal,ideal.normal);
         } else {
            normal = null;
         }
         calcThreshold(); // can't transform it
         if (customTexture instanceof Geom.Texture) {
            ((Geom.Texture) customTexture).place((Geom.Texture) ideal.customTexture,helper);
         }
      }

      public void translate(double[] d) {
         Vec.add(center,center,d);
         calcThreshold();
         if (customTexture instanceof Geom.Texture) {
            ((Geom.Texture) customTexture).translate(d);
         }
      }

      public void scale(double[] d) {
         Vec.scaleMultiCo(center,center,d);
         if (normal != null) Vec.scaleMultiContra(normal,normal,d);
         calcThreshold();
         if (customTexture instanceof Geom.Texture) {
            ((Geom.Texture) customTexture).scale(d);
         }
      }

      public void rotate(int dir1, int dir2, double theta, double[] origin) {
         vrotate(center,dir1,dir2,theta,origin);
         if (normal != null) Vec.rotateAbsoluteAngleDir(normal,normal,dir1,dir2,theta);
            // no origin shift for normals!
         calcThreshold(); // threshold changes if origin isn't coordinate origin
         if (customTexture instanceof Geom.Texture) {
            ((Geom.Texture) customTexture).rotate(dir1,dir2,theta,origin);
         }
      }
   }

// --- subface ---

   // in 3D subfaces are the same things as edges, but it doesn't matter
   // since the information we're storing is different.

   public static class Subface {

      public int if1; // index of face 1
      public int if2; // index of face 2
   }

// --- edge ---

   public static class Edge {

      public int iv1; // index of vertex 1
      public int iv2; // index of vertex 2
      public Color color;

      public Edge() {}
      public Edge(int iv1, int iv2) {
         this.iv1 = iv1;
         this.iv2 = iv2;
      }
      public Edge(int iv1, int iv2, Color color) {
         this.iv1 = iv1;
         this.iv2 = iv2;
         this.color = color;
      }

      public Edge copy() {
         return new Edge(iv1,iv2,color);
      }

      public boolean sameVertices(int iv1, int iv2) {
         return    (this.iv1 == iv1 && this.iv2 == iv2)
                || (this.iv1 == iv2 && this.iv2 == iv1);
      }

      public boolean sameVertices(Edge edge) {
         return    (iv1 == edge.iv1 && iv2 == edge.iv2)
                || (iv1 == edge.iv2 && iv2 == edge.iv1);
      }
   }

// --- separator ---

   public interface Separator {

      int apply(double[] origin);

      int S1_FRONT = -1;
      int NO_FRONT =  0;
      int S2_FRONT =  1;
      int UNKNOWN  =  2; // only Clip.dynamicSeparate returns this
   }

   public static class NormalSeparator implements Separator {

      public double[] normal;
      public double threshMin;
      public double threshMax;
      public int invert;

      public NormalSeparator(double[] normal, double threshMin, double threshMax, int invert) {
         this.normal = normal;
         this.threshMin = threshMin;
         this.threshMax = threshMax;
         this.invert = invert;
      }

      public NormalSeparator(Face face, int invert) { // convenience
         this.normal = face.normal;
         this.threshMin = face.threshold;
         this.threshMax = face.threshold;
         this.invert = invert;
      }

      public int apply(double[] origin) {
         double value = Vec.dot(origin,normal);
         int result;
         if      (value < threshMin) result = S1_FRONT;
         else if (value > threshMax) result = S2_FRONT;
         else                        result = NO_FRONT;
         // if value is in between, neither is in front!
         return result*invert;
      }
   }

   public static class NullSeparator implements Separator {
      public int apply(double[] origin) { return NO_FRONT; }
   }

   public static Separator nullSeparator = new NullSeparator();

}

