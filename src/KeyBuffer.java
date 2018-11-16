/*
 * KeyBuffer.java
 */

/**
 * A buffer that keeps track of mapped keyboard state.
 */

public class KeyBuffer {

// --- fields ---

   public boolean[] pressed;
   public boolean[] down;

// --- ids ---

   // map OptionsKeys and OptionsKeysConfig numbers into a single ID space
   // and also IKeysNew keys

   public static final int NID = OptionsKeys.NKEY + OptionsKeysConfig.NKEY + IKeysNew.NKEY;

   public static int getKeyID      (int i) { return i; }
   public static int getKeyConfigID(int i) { return OptionsKeys.NKEY + i; }
   public static int getKeyNew     (int i) { return OptionsKeys.NKEY + OptionsKeysConfig.NKEY + i; }

   public static final int ID_NONE = -1;

// --- construction ---

   public KeyBuffer() {
      pressed = new boolean[NID];
      down = new boolean[NID];
   }

// --- methods ---

   public void clearPressed() {
      for (int i=0; i<NID; i++) pressed[i] = false;
   }

   public void clearDown() {
      for (int i=0; i<NID; i++) down[i] = false;
   }

}

