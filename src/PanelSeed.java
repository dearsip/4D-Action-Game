/*
 * PanelSeed.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing seed options.
 */

public class PanelSeed extends PanelOptions {

// --- fields ---

   private JTextField[] mapSeed;
   private JTextField[] colorSeed;

// --- construction ---

   public PanelSeed() {

      mapSeed = new JTextField[2];
      colorSeed = new JTextField[2];

      for (int i=0; i<2; i++) {
         mapSeed[i] = new JTextField(15);
         colorSeed[i] = new JTextField(15);

         // the seed values will often have lots of digits,
         // so make the text fields extra-wide
      }

      // the current mapSeed is for display only
      //
      mapSeed[0].setEnabled(false);

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(1,0,label("s1",false));
      helper.addCenter(3,0,label("s2",false));

      helper.add(0,1,label("s3"));
      helper.add(0,2,label("s4"));

      for (int i=0; i<2; i++) {
         int x = 1 + i*2;
         helper.add(x,1,mapSeed[i]);
         helper.add(x,2,colorSeed[i]);
      }

      helper.addBlank(0,3);
      helper.addBlank(4,0);

      helper.setRowWeight(3,1);
      helper.setColumnWeight(0,1);
      helper.setColumnWeight(2,1);
      helper.setColumnWeight(4,1);
   }

// --- helpers ---

   private static JLabel label(String key) {
      return label(key,true);
   }

   private static JLabel label(String key, boolean pad) {
      String s = App.getString("PanelSeed." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   private void get(OptionsSeed oe, int i) throws ValidationException {

      if (Field.isBlank(mapSeed[i])) {
         oe.mapSeedSpecified = false;
         oe.mapSeed = 0;
      } else {
         oe.mapSeedSpecified = true;
         oe.mapSeed = Field.getLong(mapSeed[i]);
      }

      if (Field.isBlank(colorSeed[i])) {
         oe.colorSeedSpecified = false;
         oe.colorSeed = 0;
      } else {
         oe.colorSeedSpecified = true;
         oe.colorSeed = Field.getLong(colorSeed[i]);
      }

      oe.validate();
   }

   public void get(Options opt) throws ValidationException {
      // this panel displays nothing from the options file
   }

   public void getCurrent(OptionsSeed oeCurrent) throws ValidationException {
      get(oeCurrent,0);
   }

   public void getNext(OptionsSeed oeNext) throws ValidationException {
      get(oeNext,1);
   }

   private void put(OptionsSeed oe, int i) {

      if (oe.mapSeedSpecified) {
         Field.putLong(mapSeed[i],oe.mapSeed);
      } else {
         Field.putBlank(mapSeed[i]);
      }

      if (oe.colorSeedSpecified) {
         Field.putLong(colorSeed[i],oe.colorSeed);
      } else {
         Field.putBlank(colorSeed[i]);
      }
   }

   public void put(Options opt) {
      // this panel displays nothing from the options file
   }

   public void putCurrent(OptionsSeed oeCurrent) {
      put(oeCurrent,0);
   }

   public void putNext(OptionsSeed oeNext) {
      put(oeNext,1);
   }

}

