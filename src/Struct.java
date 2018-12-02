/*
 * Struct.java
 */

/**
 * Some small structure classes.
 */

public class Struct {

// --- view info ---

   public static class ViewInfo implements IDimension {

      public double[] origin;
      public double[][] axis;

      public int getDimension() {
         return origin.length; // ugly but it will do for now
         // note, not axis.length, since the axis vectors
         // might have come from the 4D enumeration constants
      }
   }

// --- draw info ---

   public static class DrawInfo {

      public boolean[] texture;
      public boolean useEdgeColor;
   }

// --- dimension marker ---

   public static class DimensionMarker implements IDimension {

      private int dim;
      public DimensionMarker(int dim) { this.dim = dim; }

      public int getDimension() { return dim; }
   }

// --- finish info ---
   public static class FinishInfo {

     public int[] finish;
   }

// --- block info ---
   public static class BlockInfo {}
}
