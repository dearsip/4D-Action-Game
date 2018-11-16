/*
 * TokenFile.java
 */

import java.io.*;
import java.text.DecimalFormat;

/**
 * A utility class for writing tokens into a file.
 */

public class TokenFile implements IToken {

// --- setup ---

   private Writer w;
   private DecimalFormat decimalFormat;
   private boolean needSpace;

   public TokenFile(File file) throws IOException {
      w = new FileWriter(file);

      decimalFormat = new DecimalFormat("0.############");
      // the point is to remove annoying FP effects.
      // you could use up to 15 digits if you wanted,
      // but I think 12 is plenty.  it's a lot more
      // precise than any of the epsilons in the code.

      needSpace = false;
   }

   public void close() throws IOException {
      w.close();
   }

// --- implementation of IToken ---

   private void spaceIfNeeded() throws IOException {
      if (needSpace) w.write(' ');
   }

   public IToken putBoolean(boolean b) throws IOException {
      spaceIfNeeded();
      w.write(b ? "true" : "false");
      needSpace = true;
      return this;
   }

   public IToken putInteger(int i) throws IOException {
      spaceIfNeeded();
      w.write(String.valueOf(i));
      needSpace = true;
      return this;
   }

   public IToken putDouble(double d) throws IOException {
      spaceIfNeeded();
      w.write(decimalFormat.format(d));
      needSpace = true;
      return this;
   }

   public IToken putWord(String s) throws IOException {
      spaceIfNeeded();
      w.write(s);
      needSpace = true;
      return this;
   }

   public IToken putSymbol(String s) throws IOException {
      w.write(s);
      needSpace = false;
      return this;
   }

   public IToken putString(String s) throws IOException {
      spaceIfNeeded();
      w.write('\"');
      w.write(s.replaceAll("\"","\\\\\""));
      w.write('\"');
      needSpace = true;
      return this;
   }

   public IToken space() throws IOException {
      w.write(' ');
      needSpace = false;
      return this;
   }

   public IToken newLine() throws IOException {
      w.write(lineSeparator);
      needSpace = false;
      return this;
   }

	private static String lineSeparator = System.getProperty("line.separator");

}

