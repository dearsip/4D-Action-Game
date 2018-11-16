/*
 * OptionsView.java
 */

/**
 * Options for how the maze is viewed.
 */

public class OptionsView implements IValidate {

// --- fields ---

   public int depth;
   public boolean[] texture; // 0 is for cell boundaries, 1-9 for wall texture
   public double retina;
   public double scale;

// --- construction ---

   public OptionsView() {
      texture = new boolean[10];
   }

// --- structure methods ---

   public static void copy(OptionsView dest, OptionsView src) {
      copy(dest,src,src.texture);
   }

   public static void copy(OptionsView dest, OptionsView src, boolean[] texture) {
      dest.depth = src.depth;
      for (int i=0; i<10; i++) dest.texture[i] = texture[i];
      dest.retina = src.retina;
      dest.scale = src.scale;
   }

// --- implementation of IValidate ---

   public static final int DEPTH_MIN = 0;
   public static final int DEPTH_MAX = 10;

   private static final double SCALE_MIN = 0;
   private static final double SCALE_MAX = 1;

   public void validate() throws ValidationException {

      if (depth < DEPTH_MIN || depth > DEPTH_MAX) throw App.getException("OptionsView.e1",new Object[] { new Integer(DEPTH_MIN), new Integer(DEPTH_MAX) });

      if (retina <= 0) throw App.getException("OptionsView.e2");

      if (scale <= SCALE_MIN || scale > SCALE_MAX) throw App.getException("OptionsView.e3",new Object[] { new Double(SCALE_MIN), new Double(SCALE_MAX) });
   }

}

