/*
 * PanelKeysConfig.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel superclass for editing keys options.
 */

public class PanelKeysConfig extends PanelOptions {

// --- fields ---

   private int[] index;
   protected KeyField[] key;

// --- construction ---

   public PanelKeysConfig(int[] index) {
      this.index = index;

      key = new KeyField[index.length];

      for (int j=0; j<index.length; j++) {
         key[j] = new KeyField(7);
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

   protected void get(OptionsKeysConfig okc) throws ValidationException {
      for (int j=0; j<index.length; j++) {
         key[j].get(okc.key[index[j]]);
      }

      // multi-tab options validated in DialogOptions
   }

   public void get(Options opt) throws ValidationException {
      get(opt.okc);
   }

   protected void put(OptionsKeysConfig okc) {
      for (int j=0; j<index.length; j++) {
         key[j].put(okc.key[index[j]]);
      }
   }

   public void put(Options opt) {
      put(opt.okc);
   }

}

