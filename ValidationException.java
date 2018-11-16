/*
 * ValidationException.java
 */

/**
 * An exception subclass used for validation in the user interface and in I/O routines.
 */

public class ValidationException extends Exception {

   public ValidationException(String message) {
      super(message);
   }

}

