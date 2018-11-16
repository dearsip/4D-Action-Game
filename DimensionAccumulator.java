/*
 * DimensionAccumulator.java
 */

/**
 * The one implementation of IDimensionMultiDest.
 * It has three states: first, normal, and error.
 */

public class DimensionAccumulator implements IDimensionMultiDest {

   public boolean first;
   public int dim;
   public boolean error;

   public DimensionAccumulator() {
      first = true;
      dim = 0;
      error = false;
   }

   public void putDimension(int dim) {
      if (first) {
         this.dim = dim;
         first = false;
      } else {
         if (dim != this.dim) error = true;
      }
   }

}

