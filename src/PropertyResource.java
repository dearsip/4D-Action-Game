/*
 * PropertyResource.java
 */

import java.io.*;
import java.util.Properties;

/**
 * A utility class for loading properties from resources.
 * The code is very similar to the code in PropertyFile.
 */

public class PropertyResource {

   private static void loadProperties(String name, Properties p) throws IOException, ValidationException {
      InputStream stream = null;
      try {
         stream = PropertyResource.class.getClassLoader().getResourceAsStream(name);
         if (stream == null) throw App.getException("PropertyResource.e3",new Object[] { name });
         p.load(stream);
      } finally {
         if (stream != null) stream.close();
      }
   }

   public static void load(String name, IStorable storable) throws ValidationException {
      Properties p = new Properties();

      try {
         loadProperties(name,p);
      } catch (IOException e) {
         throw App.getException("PropertyResource.e1",new Object[] { name, e.getMessage() });
      }

      try {
         PropertyStore store = new PropertyStore(p);
         storable.load(store);
      } catch (ValidationException e) {
         throw App.getException("PropertyResource.e2",new Object[] { name, e.getMessage() });
      }
   }

}

