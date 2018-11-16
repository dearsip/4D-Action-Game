/*
 * Scramble.java
 */

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * Utility functions for generation random positions and orientations.
 */

public class Scramble {

// --- general mode (non-aligned) ---

   /**
    * Generate a random position on a mat of the given width.
    * (The standard mats have width 5.)
    */
   public static void generalPosition(double[] p, double width) {
      double offset = (width-1) / 2;
      for (int i=0; i<p.length; i++) {
         p[i] = (i == 1) ? 0 : (width*Math.random() - offset);
      }
   }

   /**
    * Generate a random point in a ball of radius 1.
    */
   public static void randomSpotInUnitBall(double[] p) {
      while (true) {
         for (int i=0; i<p.length; i++) {
            p[i] = 2*Math.random() - 1;
         }
         if (Vec.dot(p,p) <= 1) break;
      }
      // looping isn't very efficient (52% hit rate in 3D, 31% in 4D)
      // but it's easy to code and clearly produces unbiased results
   }

   /**
    * Remove any component in the axis direction.
    * @param axis A normalized vector.
    */
   public static void remove(double[] p, double[] axis) {
      double d = Vec.dot(p,axis);
      Vec.addScaled(p,p,axis,-d);
   }

   /**
    * Remove any components in the axis directions.
    * @param axis An array of normalized vectors.
    */
   public static void remove(double[] p, double[][] axis, int n) {
      for (int i=0; i<n; i++) {
         remove(p,axis[i]);
      }
   }

   /**
    * Generate a random orientation.
    */
   public static void generalOrientation(double[][] axis) {
      int dim = axis.length;
      for (int i=0; i<dim-1; i++) {
         while (true) {
            randomSpotInUnitBall(axis[i]);
            remove(axis[i],axis,i);
            if (Vec.normalizeTry(axis[i],axis[i])) break;
            // this isn't really much of a loop,
            // not much chance of gettting exactly zero
         }
      }
      finishOrientation(dim,axis);
   }

   private static void finishOrientation(int dim, double[][] axis) {

      // because we're building an orthogonal matrix,
      // cofactors^t = M^-1 = M^t, so we can get
      // the elements of the last row by taking cofactors.
      // I like this better than generating the last axis
      // randomly and then checking the full determinant.

      if (dim == 3) {
         axis[2][0] =  det2(axis,1,2);
         axis[2][1] =  det2(axis,2,0);
         axis[2][2] =  det2(axis,0,1);
      } else {
         axis[3][0] = -det3(axis,1,2,3);
         axis[3][1] =  det3(axis,2,3,0);
         axis[3][2] = -det3(axis,3,0,1);
         axis[3][3] =  det3(axis,0,1,2);
      }
   }

   public static double det2(double[][] axis, int i1, int i2) {
      return   axis[0][i1]*axis[1][i2]
             - axis[0][i2]*axis[1][i1];
   }

   public static double det3(double[][] axis, int i1, int i2, int i3) {
      return   axis[0][i1]*axis[1][i2]*axis[2][i3]
             + axis[0][i2]*axis[1][i3]*axis[2][i1]
             + axis[0][i3]*axis[1][i1]*axis[2][i2]
             - axis[0][i1]*axis[1][i3]*axis[2][i2]
             - axis[0][i2]*axis[1][i1]*axis[2][i3]
             - axis[0][i3]*axis[1][i2]*axis[2][i1];
      // could write in terms of det2 but I think this is clearer
   }

   public static void generalScramble(Geom.Shape shape, double width) {

      generalPosition(shape.shapecenter,width);
      generalOrientation(shape.axis);

      shape.shapecenter[1] = shape.radius;
      // using shapecenter because it guarantees y >= 0

      shape.place(/* useShapeCenter = */ true);

      // that works, but now that I've got aligned code to translate
      // to the ground, let's do that here too

      double ymin = Clip.vmin(shape,/* axis = */ 1);

      double[] d = new double[shape.getDimension()];
      d[1] = -ymin;
      shape.translate(d); // faster than place
   }

// --- aligned mode ---

   public static void alignedPosition(double[] p, double width, Random random) {

      // include new points when the centers come in range,
      // so for example we get the next ones entering at width 6.

      int offset = (int) (width/2);
      int iwidth = 2*offset + 1;
      for (int i=0; i<p.length; i++) {
         p[i] = (i == 1) ? 0.5 : random.nextInt(iwidth) - offset + 0.5;
      }
      // add 0.5 because not worth going through Grid.fromCell
   }

   public static void alignedOrientation(double[][] axis, Random random) {
      int dim = axis.length;
      int[] picked = Permute.permute(dim,random);
      for (int i=0; i<dim-1; i++) {
         int dir = Dir.forAxis(picked[i],/* opposite = */ random.nextBoolean());
         Vec.zero(axis[i]);
         Dir.apply(dir,axis[i],1);
      }
      finishOrientation(dim,axis); // easier than figuring out whether the
      // permutation is even or odd and then tracking all the random signs
   }

   public static void alignedScramble(Geom.Shape shape, double width) {

      Random random = new Random(); // convenient for alignedPosition;
      // necessary for alignedOrientation because of the Permute call.

      alignedPosition(shape.aligncenter,width,random);
      alignedOrientation(shape.axis,random);

      // unfortunately we need to place the shape to find out the ymin value
      shape.place();

      double ymin = Clip.vmin(shape,/* axis = */ 1);
      // now we want to raise or lower the shape by an integer
      // so ymin is as small as possible but still nonnegative.
      // lower can only happens if aligncenter is well outside
      // the bounding box of the shape.

      double[] d = new double[shape.getDimension()];
      d[1] = -Math.floor(ymin);
      shape.translate(d); // faster than place
   }

// --- control function ---

   public static void scramble(LinkedList todo, LinkedList done, boolean alignMode, double[] origin) {
      Clip.Result clipResult = new Clip.Result();

      /*
      alignMode = isAligned(todo) && isAligned(done);
      // new plan, do aligned scramble and insert if the blocks are all aligned.
      // having it depend on the user's align mode was unintuitive and annoying.
      */
      // interesting idea but too unpredictable

      Collections.shuffle(todo); // else there's bias

      double width = 5;
      double increment = 0.1;

      while (todo.size() > 0) {
         Geom.Shape shape = (Geom.Shape) todo.removeFirst();

         while (true) {
            if (alignMode) {
               alignedScramble(shape,width);
            } else {
               generalScramble(shape,width);
            }
            if (isSeparated(shape,done,origin,clipResult)) break;
            width += increment;
            // gradually expand to get more room
         }

         done.add(shape);
      }
   }

   public static boolean isAligned(LinkedList list) {
      Iterator i = list.iterator();
      while (i.hasNext()) {
         Geom.Shape shape = (Geom.Shape) i.next();
         if ( ! isAligned(shape) ) return false;
      }
      return true; // so, insert into empty scene will be aligned
   }

   public static boolean isAligned(Geom.Shape shape) {
      return Align.isAligned(shape.aligncenter,shape.axis);
   }

   public static boolean isSeparated(Geom.Shape shape, LinkedList done, double[] origin, Clip.Result clipResult) {

      // what can you collide with?

   // 1. the ground

      // algorithm guarantees this won't happen

   // 2. other blocks

      if ( ! Clip.isSeparated(shape,done) ) return false;

   // 3. user viewpoint (fast but rare, check last)

      if (Clip.clip(origin,origin,shape,clipResult) != Clip.KEEP_LINE) return false;

   // looks good!

      return true;
   }

}

