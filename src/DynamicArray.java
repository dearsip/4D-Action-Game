/*
 * DynamicArray.java
 */

import java.awt.Color;
import java.util.Random;

/**
 * A utility class for manipulating arrays with variable numbers of dimensions.
 */

public class DynamicArray {

// --- helpers ---

   public static int[] makeLimits(int dimSpace, int dimMap, int size) {
      int[] limits = new int[dimSpace];
      int i = 0;
      for ( ; i<dimMap;   i++) limits[i] = size + 2;
      for ( ; i<dimSpace; i++) limits[i] = 3;
      return limits;
   }

   public static int[] makeLimits(int[] size) {

      // we don't need dimSpace and dimMap as arguments,
      // we know by construction that dimSpace = size.length
      // and by validation that size[i] = 1 for i >= dimMap

      int[] limits = new int[size.length];
      for (int i=0; i<size.length; i++) limits[i] = size[i] + 2;
      return limits;
   }

   /**
    * Check whether a cell is in the interior of an array.
    */
   public static boolean inBounds(int[] p, int[] limits) {
      for (int i=0; i<limits.length; i++) {
         if (p[i] < 1 || p[i] > limits[i]-2) return false;
      }
      return true;
   }

   /**
    * Pick a random cell in the interior of an array.
    */
   public static int[] pick(int[] limits, Random random) {
      int[] p = new int[limits.length];
      for (int i=0; i<limits.length; i++) p[i] = 1 + random.nextInt(limits[i]-2);
      return p;
   }

// --- iterator ---

   /**
    * An object that iterates over an arbitrary-dimensional subspace of an array.
    */
   public static class Iterator {

   // --- fields ---

      private int[] a;
      private int[] i;
      private int[] limits;
      private boolean done;

   // --- construction ---

      /**
       * @param a The axes that define the subspace to iterate over.
       * @param i The initial point.
       *          i[a] should be zero for all elements of the array a.
       * @param limits The limits that define the size of the array.
       */
      public Iterator(int[] a, int[] i, int[] limits) {
         this.a = a;
         this.i = (int[]) i.clone();
         this.limits = limits;
         done = false;
      }

   // --- methods ---

      public boolean hasCurrent() {
         return ( ! done );
      }

      public int[] current() {
         return i; // caller shouldn't modify
      }

      public void increment() {

         // this is just adding one to a number with digits in different bases

         for (int j=0; j<a.length; j++) {
            if (++i[a[j]] < limits[a[j]]) return; // no carry
            i[a[j]] = 0;
         }
         // overflow

         // when iterating over no dimensions, there is exactly one iteration;
         // iterating over any number of dimensions when the limits are 1 is the same

         done = true;
      }
   }

// --- boolean ---

   public static class OfBoolean {

   // --- fields ---

      private int dim;
      private int[] limits;
      private Object data;

   // --- construction ---

      public OfBoolean(int dim, int[] limits) {
         this.dim = dim;
         this.limits = limits;
         if (dim == 3) {
            data = new boolean[limits[0]][limits[1]][limits[2]];
         } else {
            data = new boolean[limits[0]][limits[1]][limits[2]][limits[3]];
         }
      }

   // --- accessors ---

      public boolean get(int[] p) {
         if (dim == 3) {
            return ((boolean[][][]) data)[p[0]][p[1]][p[2]];
         } else {
            return ((boolean[][][][]) data)[p[0]][p[1]][p[2]][p[3]];
         }
      }

      public void set(int[] p, boolean b) {
         if (dim == 3) {
            ((boolean[][][]) data)[p[0]][p[1]][p[2]] = b;
         } else {
            ((boolean[][][][]) data)[p[0]][p[1]][p[2]][p[3]] = b;
         }
      }

      public boolean inBounds(int[] p) {
         return DynamicArray.inBounds(p,limits);
      }
   }

// --- Color ---

   public static class OfColor {

   // --- fields ---

      private int dim;
      private int[] limits;
      private Object data;

   // --- construction ---

      public OfColor(int dim, int[] limits) {
         this.dim = dim;
         this.limits = limits;
         if (dim == 3) {
            data = new Color[limits[0]][limits[1]][limits[2]];
         } else {
            data = new Color[limits[0]][limits[1]][limits[2]][limits[3]];
         }
      }

   // --- accessors ---

      public Color get(int[] p) {
         if (dim == 3) {
            return ((Color[][][]) data)[p[0]][p[1]][p[2]];
         } else {
            return ((Color[][][][]) data)[p[0]][p[1]][p[2]][p[3]];
         }
      }

      public void set(int[] p, Color color) {
         if (dim == 3) {
            ((Color[][][]) data)[p[0]][p[1]][p[2]] = color;
         } else {
            ((Color[][][][]) data)[p[0]][p[1]][p[2]][p[3]] = color;
         }
      }

      public boolean inBounds(int[] p) {
         return DynamicArray.inBounds(p,limits);
      }
   }

// --- Faces (for CraftModel) ---

   public static class OfFace {

   // --- fields ---

      private int dim;
      private int[] limits;
      private Object data;

   // --- construction ---

      public OfFace(int dim, int[] limits) {
         this.dim = dim;
         this.limits = limits;
         if (dim == 3) {
            data = new boolean[limits[0]][limits[1]][limits[2]][2*dim];
         } else {
            data = new boolean[limits[0]][limits[1]][limits[2]][limits[3]][2*dim];
         }
      }

   // --- accessors ---

      public boolean get(int[] p, int d) {
         if (dim == 3) {
            return ((boolean[][][][]) data)[p[0]][p[1]][p[2]][d];
         } else {
            return ((boolean[][][][][]) data)[p[0]][p[1]][p[2]][p[3]][d];
         }
      }

      public void set(int[] p, int d, boolean b) {
         if (dim == 3) {
            ((boolean[][][]) data)[p[0]][p[1]][p[2]][d] = b;
         } else {
            ((boolean[][][][]) data)[p[0]][p[1]][p[2]][p[3]][d] = b;
         }
      }
   }

}

