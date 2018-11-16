/*
 * OptionsKeysConfig.java
 */

/**
 * Options for keys that control configuration, i.e., other option settings.
 */

public class OptionsKeysConfig implements IValidate {

// --- fields ---

   public Key[] key;
   public int[] param;

// --- constants ---

   public static final int KEY_NEW_GAME  =  0;
   public static final int KEY_OPTIONS   = 23;
   public static final int KEY_EXIT_GAME = 24;

   public static final int KEY_TEXTURE = 1; // there are ten of these, add 0-9

   public static final int KEY_PARAM_DECREASE = 11; // there are six of these, add 0-5
   public static final int KEY_PARAM_INCREASE = 17;

   public static final int NKEY = 25;

   public static final int PARAM_NONE            = 0;
   public static final int PARAM_COLOR_MODE      = 1;
   public static final int PARAM_DEPTH           = 2;
   public static final int PARAM_RETINA          = 3;
   public static final int PARAM_SCALE           = 4;
   public static final int PARAM_SCREEN_WIDTH    = 5;
   public static final int PARAM_SCREEN_DISTANCE = 6;
   public static final int PARAM_EYE_SPACING     = 7;
   public static final int PARAM_TILT_VERTICAL   = 8;
   public static final int PARAM_TILT_HORIZONTAL = 9;
   public static final int PARAM_FISHEYE_WIDTH   = 10;
   public static final int PARAM_FISHEYE_FLARE   = 11;
   public static final int PARAM_FISHEYE_RGAP    = 12;

   public static final int NPARAM = 13;

// --- construction ---

   public OptionsKeysConfig() {
      key = new Key[NKEY];
      for (int i=0; i<NKEY; i++) {
         key[i] = new Key();
      }
      param = new int[6]; // NPARAM applies to the array values, not the array length
   }

// --- implementation of IValidate ---

   public void validate() throws ValidationException {
      for (int i=0; i<6; i++) {
         if (param[i] < 0 || param[i] >= NPARAM) throw App.getException("OptionsKeysConfig.e1");
      }
   }

}

