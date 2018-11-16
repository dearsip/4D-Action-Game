/*
 * IToken.java
 */

import java.io.IOException;

/**
 * An interface for writing tokens into a file.
 * Sort of like {@link IStore}.
 * The read side is handled by StreamTokenizer
 * in {@link Language}.
 */

public interface IToken {

   IToken putBoolean(boolean b) throws IOException;
   IToken putInteger(int i) throws IOException;
   IToken putDouble(double d) throws IOException;

   IToken putWord(String s) throws IOException;
   IToken putSymbol(String s) throws IOException; // one-character word that doesn't require adjacent spaces
   IToken putString(String s) throws IOException;

   IToken space() throws IOException; // for putting optional spaces between symbols
   IToken newLine() throws IOException;

}

