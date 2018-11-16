/*
 * PlatformArray.java
 */

/**
 * Another utility class for manipulating arrays with variable numbers of dimensions.
 * There are many similarities to {@link DynamicArray}, but also several differences.
 */

public class PlatformArray {

// --- fields ---

   private int dim;
   private int[] min;
   private int[] max;
   private Object data; // array of user objects

// --- construction ---

   public PlatformArray(int[] min, int[] max) {
      dim = min.length;
      this.min = min;
      this.max = max;
      if (dim == 2) { // no y dimension here
         data = new Object[max[0]-min[0]+1][max[1]-min[1]+1];
      } else {
         data = new Object[max[0]-min[0]+1][max[1]-min[1]+1][max[2]-min[2]+1];
      }
   }

// --- accessors ---

   public Object get(int[] p) {
      if (dim == 2) {
         return ((Object[][]) data)[p[0]-min[0]][p[1]-min[1]];
      } else {
         return ((Object[][][]) data)[p[0]-min[0]][p[1]-min[1]][p[2]-min[2]];
      }
   }

   public void set(int[] p, Object o) {
      if (dim == 2) {
         ((Object[][]) data)[p[0]-min[0]][p[1]-min[1]] = o;
      } else {
         ((Object[][][]) data)[p[0]-min[0]][p[1]-min[1]][p[2]-min[2]] = o;
      }
   }

// --- iterator ---

   public static class Iterator {

   // --- fields ---

      private int[] pmin;
      private int[] pmax;
      private int[] i;
      private boolean done;

   // --- construction ---

      public Iterator(int[] pmin, int[] pmax) {
         this.pmin = pmin;
         this.pmax = pmax;
         i = (int[]) pmin.clone();
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
         for (int j=0; j<i.length; j++) {
            if (++i[j] <= pmax[j]) return; // no carry
            i[j] = pmin[j];
         }
         done = true;
      }
   }

// --- methods ---

   public void clear(int[] pmin, int[] pmax) {
      Iterator i = new Iterator(pmin,pmax);
      while (i.hasCurrent()) {
         set(i.current(),null);
         i.increment();
      }
   }

   public void clearAll() {
      clear(min,max);
   }

}

