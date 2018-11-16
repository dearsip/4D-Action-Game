/*
 * Field.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A utility class for transferring data to and from UI fields.
 * Many of the functions are just simple wrappers,
 * but they are still worthwhile because they provide uniformity function names.
 */

public class Field {

// --- string ---

   public static String getString(JTextField f) {
      return f.getText().trim(); // remove whitespace
   }

   public static void putString(JTextField f, String s) {
      f.setText(s);
      f.getCaret().moveDot(0);

      // setText deletes the existing text and inserts the new text,
      // leaving the caret at the end, where it would cause annoying scroll behavior.
      // so, move it to the beginning ... and, as a bonus, leave the text selected.
   }

// --- blank ---

   public static boolean isBlank(JTextField f) {
      return (getString(f).length() == 0);
   }

   public static void putBlank(JTextField f) {
      putString(f,"");
   }

// --- boolean ---

   public static boolean getBoolean(JCheckBox f) {
      return f.isSelected();
   }

   public static void putBoolean(JCheckBox f, boolean b) {
      f.setSelected(b);
   }

// --- integer ---

   private static int getInteger(String s) throws ValidationException {
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException e) {
         throw App.getException("Field.e1",new Object[] { s });
      }
   }

   public static int getInteger(JTextField f) throws ValidationException {
      return getInteger(getString(f));
   }

   public static void putInteger(JTextField f, int i) {
      putString(f,String.valueOf(i));
   }

// --- long ---

   public static long getLong(JTextField f) throws ValidationException {
      String s = getString(f);
      try {
         return Long.parseLong(s);
      } catch (NumberFormatException e) {
         throw App.getException("Field.e2",new Object[] { s });
      }
   }

   public static void putLong(JTextField f, long l) {
      putString(f,String.valueOf(l));
   }

// --- double ---

   public static double getDouble(JTextField f) throws ValidationException {
      String s = getString(f);
      try {
         return Double.parseDouble(s);
      } catch (NumberFormatException e) {
         throw App.getException("Field.e3",new Object[] { s });
      }
   }

   public static void putDouble(JTextField f, double d) {

      // the new parameter adjustment code produces nice numbers,
      // so I don't need a formatter to fix .000000001's.

      // I thought I wanted to make integers show as "N" instead of "N.0",
      // but actually that is a nice clue as to which fields can accept FP values
      //
      // long l = (long) d;
      // if (l == d) { putLong(f,l); return; }

      putString(f,String.valueOf(d));
   }

// --- color ---

   // not tied directly to any kind of field, but it's the same kind of thing

   private static final char[] HEX = new char[] { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
   private static final char   ESC = '#';

   public static Color getColor(String s) throws ValidationException {
      try {
         if (s.length() == 7 && s.charAt(0) == ESC) { // decode accepts wider range
            return Color.decode(s);
         }
         // else fall through and error
      } catch (Exception e) {
      }
      throw App.getException("Field.e6",new Object[] { s });
   }

   private static void toHex(char[] ch, int i, int n) {
      ch[i++] = HEX[(n>>4)&0xF];
      ch[i++] = HEX[ n    &0xF];
   }

   public static String putColor(Color c) {
      char[] ch = new char[7];
      ch[0] = ESC;
      toHex(ch,1,c.getRed());
      toHex(ch,3,c.getGreen());
      toHex(ch,5,c.getBlue());
      return new String(ch);
   }

// --- enumerated ---

   public static int getEnumerated(JComboBox f, int[] values) {
      return values[f.getSelectedIndex()];
   }

   public static void putEnumerated(JComboBox f, int[] values, int i) {
      for (int j=0; j<values.length; j++) {
         if (values[j] == i) {
            f.setSelectedIndex(j);
            return;
         }
      }
      throw new IndexOutOfBoundsException();
   }

// --- map size ---

   // here is the desired behavior, using dimSpace = 6, dimMap = 3
   //
   //    "N"     <-> { N,N,N,1,1,1 }
   //    "P,Q,R" <-> { P,Q,R,1,1,1 }
   //
   // all other string forms are invalid

   private static char DELIMITER = ',';

   public static void getMapSize(JTextField f, int[] size, int dimMap) throws ValidationException {

      int i = 0;

      String s = getString(f);
      char[] c = s.toCharArray();
      int base = 0;

      for (int j=0; j<c.length; j++) {
         if (c[j] != DELIMITER) continue;

         size[i++] = getInteger(s.substring(base,j));
         base = j+1; // skip delimiter

         // there will be one more at the end, so if we reach dimMap now, it is bad
         if (i == dimMap) throw App.getException("Field.e4");
      }

      size[i++] = getInteger(s.substring(base,c.length));

      if (i == 1) {
         int sameValue = size[0];
         for ( ; i<dimMap; i++) size[i] = sameValue;
      } else if (i == dimMap) {
         // everything is fine
      } else {
         throw App.getException("Field.e5");
      }

      for ( ; i<size.length; i++) size[i] = 1;
   }

   public static void putMapSize(JTextField f, int[] size, int dimMap) {

      // we know by validation that the unused elements are 1, so just ignore them

      boolean same = true;
      int sameValue = size[0];

      for (int i=1; i<dimMap; i++) {
         if (size[i] != sameValue) { same = false; break; }
      }

      if (same) {

         putInteger(f,sameValue);

      } else {

         StringBuffer s = new StringBuffer();
         s.append(size[0]);
         for (int i=1; i<dimMap; i++) {
            s.append(DELIMITER);
            s.append(size[i]);
         }
         putString(f,s.toString());
      }
   }

}

