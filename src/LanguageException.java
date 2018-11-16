/*
 * LanguageException.java
 */

import java.io.File;

/**
 * A wrapper exception that carries extra information
 * from the scene language interpreter up to the user.
 */

public class LanguageException extends Exception {

   private File file;
   private String detail; // includes line number

   public LanguageException(Throwable t, File file, String detail) {
      super(t);
      this.file = file;
      this.detail = detail;
   }

   public File getFile() { return file; }
   public String getDetail() { return detail; }

}

