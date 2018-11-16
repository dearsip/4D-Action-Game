/*
 * App.java
 */

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * A utility class for retrieving and using strings from the application resource bundle.
 */

public class App {

// --- the resource bundle ---

   private static ResourceBundle bundle = ResourceBundle.getBundle("AppData");

// --- getString functions ---

   /**
    * Get a fixed string from the resource bundle.
    */
   public static String getString(String key) {
      return bundle.getString(key);

      // getBundle can throw MissingResourceException,
      // but we're not required to catch it ... and we shouldn't, either,
      // because it only happens as a result of programmer error
   }

   /**
    * Get a string from the resource bundle and fill in the blanks.
    */
   public static String getString(String key, Object[] args) {
      return MessageFormat.format(getString(key),args);
   }

// --- getException functions ---

   // have these return the exception instead of throwing it
   // so that in the calling code, the compiler can recognize that an exception is always thrown

   /**
    * Get a fixed string from the resource bundle and put it into an exception.
    */
   public static ValidationException getException(String key) {
      return new ValidationException(getString(key));
   }

   /**
    * Get a string from the resource bundle, fill in the blanks, and put it into an exception.
    */
   public static ValidationException getException(String key, Object[] args) {
      return new ValidationException(getString(key,args));
   }

   /**
    * Get an exception with an empty message, for use in flow of control.
    */
   public static ValidationException getEmptyException() {
      return new ValidationException("");
   }

}

