/*
 * PanelFisheye.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing fisheye options.
 */

public class PanelFisheye extends PanelOptions {

// --- fields ---

   private JCheckBox fisheye;
   private JCheckBox adjust;
   private JCheckBox rainbow;
   private JTextField width;
   private JTextField flare;
   private JTextField rainbowGap;

// --- construction ---

   public PanelFisheye() {

      fisheye = new JCheckBox();
      adjust = new JCheckBox();
      rainbow = new JCheckBox();
      width = new JTextField(5);
      flare = new JTextField(5);
      rainbowGap = new JTextField(5);

      JTextField width0 = new JTextField(5);
      JTextField flare0 = new JTextField(5);
      JTextField rainbowGap0 = new JTextField(5);
      //
      Field.putDouble(width0,OptionsFisheye.UA_WIDTH);
      Field.putDouble(flare0,OptionsFisheye.UA_FLARE);
      Field.putDouble(rainbowGap0,OptionsFisheye.UA_RGAP);
      //
      width0.setEnabled(false);
      flare0.setEnabled(false);
      rainbowGap0.setEnabled(false);

      GridBagHelper helper = new GridBagHelper(this);

      helper.add(0,1,label("s1"));
      helper.add(0,2,label("s2"));
      helper.add(0,3,label("s3"));

      helper.add(1,1,fisheye);
      helper.add(1,2,adjust);
      helper.add(1,3,rainbow);

      helper.add(2,3,label("s4",false));

      helper.add(3,1,label("s7"));
      helper.add(3,2,label("s8"));
      helper.add(3,3,label("s9"));

      helper.addCenter(4,0,label("s5",false));
      helper.add(4,1,width0);
      helper.add(4,2,flare0);
      helper.add(4,3,rainbowGap0);

      helper.addCenter(6,0,label("s6",false));
      helper.add(6,1,width);
      helper.add(6,2,flare);
      helper.add(6,3,rainbowGap);

      helper.addBlank(0,4);
      helper.addBlank(7,0);

      helper.setRowWeight(4,1);
      helper.setColumnWeight(0,1);
      helper.setColumnWeight(2,1);
      helper.setColumnWeight(3,1);
      helper.setColumnWeight(5,1);
      helper.setColumnWeight(7,1);
   }

// --- helpers ---

   private static JLabel label(String key) {
      return label(key,true);
   }

   private static JLabel label(String key, boolean pad) {
      String s = App.getString("PanelFisheye." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   public void get(OptionsFisheye of) throws ValidationException {
      of.fisheye = Field.getBoolean(fisheye);
      of.adjust = Field.getBoolean(adjust);
      of.rainbow = Field.getBoolean(rainbow);
      of.width = Field.getDouble(width);
      of.flare = Field.getDouble(flare);
      of.rainbowGap = Field.getDouble(rainbowGap);

      of.validate();
   }

   public void put(OptionsFisheye of) {
      Field.putBoolean(fisheye,of.fisheye);
      Field.putBoolean(adjust,of.adjust);
      Field.putBoolean(rainbow,of.rainbow);
      Field.putDouble(width,of.width);
      Field.putDouble(flare,of.flare);
      Field.putDouble(rainbowGap,of.rainbowGap);
   }

   public void get(Options opt) throws ValidationException {
   }
   public void put(Options opt) {
   }
   // only including these so we don't break DialogOptions

}

