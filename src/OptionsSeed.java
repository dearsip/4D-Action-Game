/*
 * OptionsSeed.java
 */

/**
 * Options for random-number generation.
 */

public class OptionsSeed implements IValidate {

// --- fields ---

   public boolean mapSeedSpecified;
   public boolean colorSeedSpecified;
   public long mapSeed;
   public long colorSeed;

// --- helpers ---

   public boolean isSpecified() {
      return mapSeedSpecified && colorSeedSpecified;
   }

   public void forceSpecified() {

      long l = System.currentTimeMillis();

      if ( ! mapSeedSpecified ) {
         mapSeed = l * 137;
         mapSeedSpecified = true;
      }

      if ( ! colorSeedSpecified ) {
         colorSeed = l * 223;
         colorSeedSpecified = true;
      }

      // multiplying by the extra numbers accomplishes two things:
      //
      //  * it expands the seed from the approximately 40 bits in currentTimeMillis
      //    to the 48 bits used by java.util.Random ... but that doesn't matter much,
      //    because zero bits are as good as any others.
      //
      //  * it makes the two seeds different,
      //    the two random number sequences will still be related,
      //    but not in any way that could be noticed in the game.
   }

// --- implementation of IValidate ---

   public void validate() throws ValidationException {

      if (mapSeed   != 0 && ! mapSeedSpecified  ) throw App.getException("OptionsSeed.e1");
      if (colorSeed != 0 && ! colorSeedSpecified) throw App.getException("OptionsSeed.e2");
   }

}

