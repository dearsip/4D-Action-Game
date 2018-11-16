/*
 * OptionsStereo.java
 */

/**
 * Options for how stereo pairs are displayed.
 */

public class OptionsStereo implements IValidate {

// --- fields ---

   public boolean enable;
   public double screenWidth;
   public double screenDistance;
   public double eyeSpacing;
   public boolean cross;
   public double tiltVertical;
   public double tiltHorizontal;

// --- structure methods ---

   public static void copy(OptionsStereo dest, OptionsStereo src) {
      dest.enable = src.enable;
      dest.screenWidth = src.screenWidth;
      dest.screenDistance = src.screenDistance;
      dest.eyeSpacing = src.eyeSpacing;
      dest.cross = src.cross;
      dest.tiltVertical = src.tiltVertical;
      dest.tiltHorizontal = src.tiltHorizontal;
   }

// --- implementation of IValidate ---

   private static final double TILT_VERTICAL_MAX   = 45;
   private static final double TILT_HORIZONTAL_MAX = 45;

   public void validate() throws ValidationException {

      if (screenWidth    <= 0) throw App.getException("OptionsStereo.e1");
      if (screenDistance <= 0) throw App.getException("OptionsStereo.e2");
      if (eyeSpacing     <= 0) throw App.getException("OptionsStereo.e3");

      if (Math.abs(tiltVertical  ) > TILT_VERTICAL_MAX  ) throw App.getException("OptionsStereo.e4",new Object[] { new Double(TILT_VERTICAL_MAX  ) });
      if (Math.abs(tiltHorizontal) > TILT_HORIZONTAL_MAX) throw App.getException("OptionsStereo.e5",new Object[] { new Double(TILT_HORIZONTAL_MAX) });
   }

}

