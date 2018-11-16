/*
 * OptionsKeys.java
 */

/**
 * Options for keys that control motion.
 */

public class OptionsKeys implements IValidate {

// --- fields ---

   private int dim;

   public Key[] key;
   public boolean startAlignMode;

// --- constants ---

   public static final int KEY_FORWARD = 0;
   public static final int KEY_BACK    = 1;

   public static final int KEY_TURN_LEFT  = 2;
   public static final int KEY_TURN_RIGHT = 3;
   public static final int KEY_TURN_UP    = 4;
   public static final int KEY_TURN_DOWN  = 5;
   public static final int KEY_TURN_IN    = 6;
   public static final int KEY_TURN_OUT   = 7;

   public static final int KEY_SLIDE_LEFT  = 8;
   public static final int KEY_SLIDE_RIGHT = 9;
   public static final int KEY_SLIDE_UP    = 10;
   public static final int KEY_SLIDE_DOWN  = 11;
   public static final int KEY_SLIDE_IN    = 12;
   public static final int KEY_SLIDE_OUT   = 13;

   public static final int KEY_SPIN_UP_LEFT  = 14;
   public static final int KEY_SPIN_UP_RIGHT = 15;
   public static final int KEY_SPIN_IN_LEFT  = 16;
   public static final int KEY_SPIN_IN_RIGHT = 17;
   public static final int KEY_SPIN_IN_UP    = 18;
   public static final int KEY_SPIN_IN_DOWN  = 19;

   public static final int KEY_ALIGN             = 20;
   public static final int KEY_CHANGE_ALIGN_MODE = 21;

   public static final int NKEY = 22;

// --- construction ---

   public OptionsKeys(int dim) {
      this.dim = dim;
      key = new Key[NKEY];
      for (int i=0; i<NKEY; i++) {
         key[i] = new Key();
      }
   }

// --- implementation of IValidate ---

   private void requireNotDefined(int i) throws ValidationException {
      if (key[i].isDefined()) throw App.getException("OptionsKeys.e1");
   }

   public void validate() throws ValidationException {
      if (dim == 3) {

         requireNotDefined(KEY_TURN_IN);
         requireNotDefined(KEY_TURN_OUT);

         requireNotDefined(KEY_SLIDE_IN);
         requireNotDefined(KEY_SLIDE_OUT);

         requireNotDefined(KEY_SPIN_IN_LEFT);
         requireNotDefined(KEY_SPIN_IN_RIGHT);
         requireNotDefined(KEY_SPIN_IN_UP);
         requireNotDefined(KEY_SPIN_IN_DOWN);
      }
   }

}

