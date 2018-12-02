/*
 * Vec.java
 */

import java.util.Random;

/**
 * A utility class containing various operations on points and lines.
 * Except as noted, it is safe for the source and destination arrays to be the same.
 */

public class Vec {

// --- non-mathematical operations ---

   public static void zero(double[] dest) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = 0;
      }
   }

   public static void copy(double[] dest, double[] src) {
      // could use System.arraycopy
      for (int i=0; i<dest.length; i++) {
         dest[i] = src[i];
      }
   }

   public static void copyMatrix(double[][] dest, double[][] src) {
      for (int i=0; i<dest.length; i++) {
         Vec.copy(dest[i],src[i]);
      }
   }

   // equals not recommended for double because of FP error
   // but this is OK
   public static boolean approximatelyEquals(double[] p1, double[] p2, double epsilon) {
      for (int i=0; i<p1.length; i++) {
         if (Math.abs(p1[i]-p2[i]) >= epsilon) return false;
      }
      return true;
   }

   public static boolean exactlyEquals(double[] p1, double[] p2) {
      for (int i=0; i<p1.length; i++) {
         if (p1[i] != p2[i]) return false;
      }
      return true;
   }

// --- unit vectors ---

   public static void unitVector(double[] dest, int a) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = (i == a) ? 1 : 0;
      }
   }

   public static void unitMatrix(double[][] dest) {
      for (int i=0; i<dest.length; i++) {
         unitVector(dest[i],i);
      }
   }

// --- simple arithmetic ---

   public static void add(double[] dest, double[] src1, double[] src2) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = src1[i] + src2[i];
      }
   }

   public static void sub(double[] dest, double[] src1, double[] src2) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = src1[i] - src2[i];
      }
   }

   public static void scale(double[] dest, double[] src, double scale) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = scale * src[i];
      }
   }

   public static void addScaled(double[] dest, double[] src1, double[] src2, double scale) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = src1[i] + scale * src2[i];
      }
   }

   public static void addScaledFloat(double[] dest, double[] src1, float[] src2, double scale) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = src1[i] + scale * src2[i];
      }
   }

   public static void scaleMultiCo(double[] dest, double[] src, double[] scale) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = src[i] * scale[i];
      }
   }

   public static void scaleMultiContra(double[] dest, double[] src, double[] scale) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = src[i] / scale[i];
      }
   }

// --- vector arithmetic ---

   public static double dot(double[] p1, double[] p2) {
      double sum = 0;
      for (int i=0; i<p1.length; i++) {
         sum += p1[i] * p2[i];
      }
      return sum;
   }

   public static double norm2(double[] p) {
      return dot(p,p);
   }

   public static double norm(double[] p) {
      return Math.sqrt(dot(p,p));
   }

   public static void normalize(double[] dest, double[] src) {
      scale(dest,src,1/norm(src));
   }

   /**
    * @return False if the vector is zero.
    */
   public static boolean normalizeTry(double[] dest, double[] src) {
      double d = norm(src);
      if (d == 0) return false;
      scale(dest,src,1/d);
      return true;
   }

   public static double dist2(double[] p1, double[] p2) {
      double sum = 0;
      for (int i=0; i<p1.length; i++) {
         double d = p1[i] - p2[i];
         sum += d * d;
      }
      return sum;
   }

   public static double dist(double[] p1, double[] p2) {
      return Math.sqrt(dist2(p1,p2));
   }

// --- rotation ---

   /**
    * Rotate src1 toward src2 using the given cosine and sine.
    * The vectors src1 and src2 should be orthogonal and have the same length.
    */
   public static void rotateCosSin(double[] dest1, double[] dest2, double[] src1, double[] src2, double cos, double sin) {
      for (int i=0; i<dest1.length; i++) {
         double s1 = src1[i];
         double s2 = src2[i];
         dest1[i] = cos*s1 + sin*s2;
         dest2[i] = cos*s2 - sin*s1;
      }
   }

   /**
    * Rotate src1 toward src2 by theta degrees.
    * The vectors src1 and src2 should be orthogonal and have the same length.
    */
   public static void rotateAngle(double[] dest1, double[] dest2, double[] src1, double[] src2, double theta) {
      theta = Math.toRadians(theta);
      rotateCosSin(dest1,dest2,src1,src2,Math.cos(theta),Math.sin(theta));
   }

   /**
    * Rotate src1 toward src2 so that src1 points at the point with coordinates (x1,x2).
    * The vectors src1 and src2 should be orthogonal and have the same length.
    */
   public static void rotatePoint(double[] dest1, double[] dest2, double[] src1, double[] src2, double x1, double x2) {
      double r = Math.sqrt(x1*x1 + x2*x2);
      rotateCosSin(dest1,dest2,src1,src2,x1/r,x2/r);
   }

   /**
    * The functions above are for shuffling axis vectors around, and therefore are
    * relative to the viewer, but the ones here and below use absolute coordinates.
    * They rotate axis a1 toward axis a2.
    */
   public static void rotateAbsoluteCosSin(double[] dest, double[] src, int a1, int a2, double cos, double sin) {
      copy(dest,src);
      double s1 = src[a1];
      double s2 = src[a2];
      dest[a1] = cos*s1 - sin*s2; // yes, sign is different than in rotateCosSin
      dest[a2] = cos*s2 + sin*s1;
   }

   public static void rotateAbsoluteAngle(double[] dest, double[] src, int a1, int a2, double theta) {

      // make sure multiples of 90 degrees are exact so that for example bottom
      // faces will still be detectable for railcars.  this isn't needed for
      // in-game operations since those rotations are normally through small angles.
      int temp = (int) theta;
      boolean exact = (temp == theta && temp % 90 == 0);

      theta = Math.toRadians(theta);
      double cos = Math.cos(theta);
      double sin = Math.sin(theta);

      if (exact) {
         cos = Math.rint(cos);
         sin = Math.rint(sin);
      }
      // maybe not the best way but it'll do

      rotateAbsoluteCosSin(dest,src,a1,a2,cos,sin);
   }

   public static void rotateAbsoluteAngleDir(double[] dest, double[] src, int dir1, int dir2, double theta) {
      int a1 = Dir.getAxis(dir1);
      int a2 = Dir.getAxis(dir2);
      if (Dir.isPositive(dir1) != Dir.isPositive(dir2)) theta = -theta;
      rotateAbsoluteAngle(dest,src,a1,a2,theta);
   }

// --- projection ---

   /**
    * Project a vector onto a screen at a given distance,
    * using lines radiating from the origin.
    * The result vector has lower dimension than the original.
    */
   public static void projectDistance(double[] dest, double[] src, double distance) {
      double scale = distance / src[dest.length];

      // same as scale, but with different-sized arrays
      for (int i=0; i<dest.length; i++) {
         dest[i] = scale * src[i];
      }
   }

   /**
    * Project a vector onto a retina at distance 1,
    * using lines radiating from the origin,
    * and then scale so the retina has size 1.
    * The result vector has lower dimension than the original.
    */
   public static void projectRetina(double[] dest, double[] src, double retina) {
      projectDistance(dest,src,1/retina);
   }

   public static final int PROJ_NONE    = 0;
   public static final int PROJ_NORMAL  = 1;
   public static final int PROJ_ORTHO   = 2; // value is vector
   public static final int PROJ_PERSPEC = 3; // value is point

   /**
    * Project into a plane without reducing the dimension.
    */
   public static void project(double[] dest, double[] src, double[] normal, double threshold, int mode, double[] value) {
      double a, sn, vn;
      switch (mode) {
      case PROJ_NORMAL:
         value = normal;
         // fall through
      case PROJ_ORTHO:
         sn = dot(src,  normal);
         vn = dot(value,normal);
         a = (threshold - sn)/vn;
         addScaled(dest,src,value,a);
         break;
      case PROJ_PERSPEC:
         // this is the same as an orthographic projection
         // with value = value-src, but I can't find a way
         // to unify the paths without a register.
         sn = dot(src,  normal);
         vn = dot(value,normal);
         a = (threshold - sn)/(vn - sn);
         // careful, this has to work when dest equals src
         scale(dest,src,1-a);
         addScaled(dest,dest,value,a);
         break;
      default: // PROJ_NONE
         break;
      }
   }

// --- coordinate conversion ---

   /**
    * Express a vector in terms of a set of axes.
    * The vectors src and dest must be different objects.
    */
   public static void toAxisCoordinates(double[] dest, double[] src, double[][] axis) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = dot(axis[i],src);
      }
   }

   /**
    * Take a vector expressed in terms of a set of axes
    * and convert it back to the original coordinate system.
    * The vectors src and dest must be different objects.
    */
   public static void fromAxisCoordinates(double[] dest, double[] src, double[][] axis) {
      zero(dest);
      for (int i=0; i<dest.length; i++) {
         addScaled(dest,dest,axis[i],src[i]);
      }
   }

// --- clipping ---

   /**
    * Find the point that is a fraction f of the way from src1 to src2.
    */
   public static void mid(double[] dest, double[] src1, double[] src2, double f) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = src1[i] + f * (src2[i] - src1[i]);
      }
   }

   /**
    * Clip the line from p1 to p2 into the half-space that the vector n points into.
    * The vector n does not need to be normalized.<p>
    * Since clipping often does nothing, the vectors p1 and p2 are modified in place.
    *
    * @return True if the line is entirely removed by clipping.
    */
   public static boolean clip(double[] p1, double[] p2, double[] n) {
      return clip(p1,p2,n,/* t = */ 0,/* sign = */ 1);
   }
   public static boolean clip(double[] p1, double[] p2, double[] n, double t, double sign) {

      double d1 = sign * (dot(p1,n) - t);
      double d2 = sign * (dot(p2,n) - t);

      if (d1 >= 0) {
         if (d2 >= 0) {
            // not clipped
         } else {
            mid(p2,p2,p1,d2/(d2-d1));
         }
      } else {
         if (d2 >= 0) {
            mid(p1,p1,p2,d1/(d1-d2));
         } else {
            // completely clipped
            return true;
         }
      }

      return false;
   }

// --- random ---

   public static void randomNormalized(double[] dest, Random random) {
      for (int i=0; i<dest.length; i++) {
         dest[i] = 2*random.nextDouble()-1;
      }
      if (! normalizeTry(dest,dest)) unitVector(dest,0);
   }
}

