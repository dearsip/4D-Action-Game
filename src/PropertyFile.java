/*
 * PropertyFile.java
 */

import java.io.*;
import java.util.Properties;

/**
 * A utility class for loading and saving property files.<p>
 *
 * When storing, the properties end up in the file in hash table order.
 * That's slightly unfortunate, but not worth correcting.
 */

public class PropertyFile {

// --- stream encapsulation ---

   private static boolean testProperties(File file) throws IOException {
      InputStream stream = null;
      try {
         stream = new FileInputStream(file);
         return (stream.read() == '#');
         // look for timestamp header from Properties.store
      } finally {
         if (stream != null) stream.close();
      }
   }

   private static void loadProperties(File file, Properties p) throws IOException {
      InputStream stream = null;
      try {
         stream = new FileInputStream(file);
         p.load(stream);
      } finally {
         if (stream != null) stream.close();

         // note: if we get here because of an exception,
         // and then the close throws an exception,
         // that exception replaces the original one.
         // it does not immediately terminate the program (as in C++).
      }
   }

   private static void storeProperties(File file, Properties p) throws IOException {
      OutputStream stream = null;
      try {
         stream = new FileOutputStream(file);
         p.store(stream,null);
      } finally {
         if (stream != null) stream.close();
      }
   }

// --- main functions ---

   public static boolean test(File file) throws ValidationException {
      try {
         return testProperties(file);
      } catch (IOException e) {
         throw App.getException("PropertyFile.e5",new Object[] { file.getName(), e.getMessage() });
      }
   }

   public static void load(File file, IStorable storable) throws ValidationException {
      Properties p = new Properties();

      try {
         loadProperties(file,p);
      } catch (IOException e) {
         throw App.getException("PropertyFile.e1",new Object[] { file.getName(), e.getMessage() });
      }

      try {
         PropertyStore store = new PropertyStore(p);
         storable.load(store);
      } catch (ValidationException e) {
         throw App.getException("PropertyFile.e2",new Object[] { file.getName(), e.getMessage() });
      }
   }

   public static void save(File file, IStorable storable) throws ValidationException {
      Properties p = new Properties();

      try {
         PropertyStore store = new PropertyStore(p);
         storable.save(store);
      } catch (ValidationException e) {
         throw App.getException("PropertyFile.e3",new Object[] { file.getName(), e.getMessage() });
      }

      try {
         storeProperties(file,p);
      } catch (IOException e) {
         throw App.getException("PropertyFile.e4",new Object[] { file.getName(), e.getMessage() });
      }
   }

}

