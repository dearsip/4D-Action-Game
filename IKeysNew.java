/*
 * IKeysNew.java
 */

/**
 * An interface for controlling trains and other new things.
 * Some day these should be configurable
 * and the states should be saved as option settings.
 */

public interface IKeysNew {

   final int NKEY = 19;

   void adjustSpeed(int dv);
   void toggleTrack();
   void toggleEdgeColor();
   IMove click(double[] origin, double[] viewAxis, double[][] axisArray);
   void scramble(boolean alignMode, double[] origin);
   void toggleSeparation();
   void addShapes(int quantity, boolean alignMode, double[] origin, double[] viewAxis);
   void removeShape(double[] origin, double[] viewAxis);
   void toggleNormals();
   void toggleHideSel();
   void paint(double[] origin, double[] viewAxis);

   boolean canAddShapes(); // not a command
   boolean canPaint();

}

