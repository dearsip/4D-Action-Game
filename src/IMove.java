/*
 * IMove.java
 */

/**
 * An interface for moving objects.
 */

public interface IMove {

   boolean canMove(int a, double d);
   void move(int a, double d);
   void rotateAngle(int a1, int a2, double theta);
   Align align();
   boolean isAligned();

   /**
    * Propagate changes to origin and axis objects.
    * The saveOrigin and saveAxis arguments are
    * just for reference, restore is handled below.
    */
   boolean update(double[] saveOrigin, double[][] saveAxis, double[] viewOrigin);

   void save   (double[] saveOrigin, double[][] saveAxis);
   void restore(double[] saveOrigin, double[][] saveAxis);

}

