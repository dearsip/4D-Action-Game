/*
 * Language.java
 */

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.Iterator;

/**
 * The heart of the scene language interpreter.
 */

public class Language {

   public static void include(Context c, String filename) throws Exception {
      if (include(c,resolve(c,filename))) {
         if (c.isTopLevel()) c.topLevelInclude.add(filename);
      }
   }

   public static boolean include(Context c, File file) throws Exception {

      file = file.getCanonicalFile();
      if ( ! c.included.add(file) ) return false;

      FileReader fr = new FileReader(file);
      try {
         StreamTokenizer st = createTokenizer(fr);

         c.dirStack.push(file.getParentFile());
         try {
            doFile(c,st);
         } catch (Throwable t) {
            throw (t instanceof LanguageException) ? (Exception) t : new LanguageException(t,file,st.toString());
         } finally {
            c.dirStack.pop();
         }

      } finally {
         fr.close();
      }

      return true;
   }

   public static File resolve(Context c, String filename) throws Exception {

      File file = new File(filename);
      if (file.isAbsolute()) return file;

      file = new File((File) c.dirStack.peek(),filename);
      if (file.exists()) return file;

      Iterator i = c.libDirs.iterator();
      while (i.hasNext()) {
         file = new File((File) i.next(),filename);
         if (file.exists()) return file;
      }

      throw new Exception("Unable to resolve filename '" + filename + "'.");
   }

   public static StreamTokenizer createTokenizer(FileReader fr) {
      StreamTokenizer st = new StreamTokenizer(fr);

      // customize tokenizer
      st.wordChars('#','#');
      st.wordChars('%','%');
      st.wordChars('+','+');
      st.wordChars('-','-');
      st.wordChars('_','_');
      st.slashSlashComments(true);
      st.slashStarComments (true);

      return st;
   }

   public static void doFile(Context c, StreamTokenizer st) throws Exception {
      while (true) {
         int t = st.nextToken();
         if (t == StreamTokenizer.TT_EOF) break;
         switch (t) {
         case StreamTokenizer.TT_NUMBER:
            c.stack.push(new Double(st.nval));
            break;
         case '\'':
         case '"':
            c.stack.push(st.sval);
            break;
         case StreamTokenizer.TT_WORD:
            doWord(c,st.sval);
            break;
         default: // ordinary chars, treat as words of length 1
            doWord(c,String.valueOf((char) t));
            break;
         case StreamTokenizer.TT_EOL:
            throw new Exception("Unexpected token type.");
         }
      }
   }

   public static void doWord(Context c, String s) throws Exception {
      if (s.charAt(0) == '#') { // color literal
         c.stack.push(Color.decode(s));
         return;
      }
      if (s.charAt(0) == '%') { // binary number
         c.stack.push(new Double(Integer.parseInt(s.substring(1),2)));
         return;
      }
      Object o = c.dict.get(s);
      if (o == null) throw new Exception("Undefined token '" + s + "'.");
      if (o instanceof ICommand) {
         ((ICommand) o).exec(c);
      } else {
         c.stack.push(tryCopy(o));
         // the normal plan is, you include some files that define shapes,
         // then you use modified forms of those shapes to set up a scene.
         // so, to avoid messing up the original, copy shapes when they come
         // out of the dictionary.
      }
   }

   public static Object tryCopy(Object o) {
      if (o instanceof Geom.ShapeInterface) {
         return ((Geom.ShapeInterface) o).copySI();
      } else if (o instanceof Geom.Texture) {
         return ((Geom.Texture) o).copy();
      } else {
         return o;
      }
   }

}

