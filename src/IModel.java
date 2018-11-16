/*
 * IModel.java
 */

/**
 * An interface for defining the model that an engine runs.
 */

public interface IModel {

   void initPlayer(double[] origin, double[][] axis);
   void testOrigin(double[] origin, int[] reg1, int[] reg2) throws ValidationException;

   void setColorMode(int colorMode);
   void setDepth(int depth);
   void setTexture(boolean[] texture);
   void setOptions(OptionsColor oc, long seed, int depth, boolean[] texture);

   boolean isAnimated();
   int getSaveType();
   boolean canMove(double[] p1, double[] p2, int[] reg1, double[] reg2);
   boolean atFinish(double[] origin, int[] reg1, int[] reg2);

   void setBuffer(LineBuffer buf);
   void animate();
   void render(double[] origin);

   final int SAVE_NONE = 0;
   final int SAVE_MAZE = 1;
   final int SAVE_GEOM = 2;
   final int SAVE_ACTION = 3;

}

