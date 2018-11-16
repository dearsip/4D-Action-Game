/*
 * OptionsImage.java
 */

/**
 * Options for image file generation.
 */

public class OptionsImage implements IValidate {

// --- fields ---

   public int background;
   public boolean invertColors;
   public int    convertColors;
   public double lineWidth;
   public double oneInch;

// --- constants ---

   public static final int BACKGROUND_BLACK = 0;
   public static final int BACKGROUND_WHITE = 1;

   public static final int CONVERT_NORMAL     = 0;
   public static final int CONVERT_GRAY_SCALE = 1;
   public static final int CONVERT_B_AND_W    = 2;

// --- structure methods ---

   public static void copy(OptionsImage dest, OptionsImage src) {
      dest.background = src.background;
      dest.invertColors = src.invertColors;
      dest.convertColors = src.convertColors;
      dest.lineWidth = src.lineWidth;
      dest.oneInch = src.oneInch;
   }

// --- implementation of IValidate ---

   public void validate() throws ValidationException {

      if (    background != BACKGROUND_BLACK
           && background != BACKGROUND_WHITE ) throw App.getException("OptionsImage.e1");

      if (    convertColors != CONVERT_NORMAL
           && convertColors != CONVERT_GRAY_SCALE
           && convertColors != CONVERT_B_AND_W    ) throw App.getException("OptionsImage.e2");

      if (lineWidth < 0) throw App.getException("OptionsImage.e3");
      if (oneInch <= 0) throw App.getException("OptionsImage.e4");
   }

}

