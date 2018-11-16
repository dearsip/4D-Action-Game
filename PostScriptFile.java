/*
 * PostScriptFile.java
 */

import java.io.*;

/**
 * A utility class for loading and saving PostScript files.
 */

public class PostScriptFile {

   public static void save(File file, int n, LineBuffer[] buf,
                           int edge, int gap, double screenWidth, OptionsImage oi) throws ValidationException {
      try {

         OutputStream stream = null;
         try {

            stream = new FileOutputStream(file);
            PostScriptPrinter printer = new PostScriptPrinter(new PrintStream(stream),edge,gap,screenWidth,oi);

            printer.printHeader(n);
            for (int i=0; i<n; i++) {
               printer.print(buf[i]);
            }

         } finally {
            if (stream != null) stream.close();
         }

      } catch (IOException e) {
         throw App.getException("PostScriptFile.e1",new Object[] { file.getName(), e.getMessage() });
      }
   }

}

