/*
 * PanelKeys.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel superclass for editing keys options.
 */

public class PanelKeys extends PanelOptions {

// --- fields ---

   private int[] index;
   protected KeyField[][] key;

// --- construction ---

   public PanelKeys(int[] index) {
      this.index = index;

      key = new KeyField[2][index.length];

      for (int i=0; i<2; i++) {
         for (int j=0; j<index.length; j++) {
            key[i][j] = new KeyField(7);
         }
      }
   }

// --- helpers ---

   protected static JLabel label(String key) {
      return label(key,true);
   }

   protected static JLabel label(String key, boolean pad) {
      String s = App.getString("PanelKeys." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   protected void get(OptionsKeys ok, int i) throws ValidationException {
      for (int j=0; j<index.length; j++) {
         key[i][j].get(ok.key[index[j]]);
      }

      // multi-tab options validated in DialogOptions
   }

   public void get(Options opt) throws ValidationException {
      get(opt.ok3,0);
      get(opt.ok4,1);
   }

   protected void put(OptionsKeys ok, int i) {
      for (int j=0; j<index.length; j++) {
         key[i][j].put(ok.key[index[j]]);
      }
   }

   public void put(Options opt) {
      put(opt.ok3,0);
      put(opt.ok4,1);
   }

}

