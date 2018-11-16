/*
 * PanelStereo.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing stereo options.
 */

public class PanelStereo extends PanelOptions {

// --- fields ---

   private JCheckBox enable;
   private JTextField screenWidth;
   private JTextField screenDistance;
   private JTextField eyeSpacing;
   private JCheckBox cross;
   private JTextField tiltVertical;
   private JTextField tiltHorizontal;

// --- construction ---

   public PanelStereo() {

      enable = new JCheckBox();
      screenWidth = new JTextField(5);
      screenDistance = new JTextField(5);
      eyeSpacing = new JTextField(5);
      cross = new JCheckBox();
      tiltVertical = new JTextField(5);
      tiltHorizontal = new JTextField(5);

      GridBagHelper helper = new GridBagHelper(this);

      helper.addSpan(0,0,2,label("s1"));
      helper.addSpan(0,1,2,label("s2"));
      helper.addSpan(0,2,2,label("s3"));
      helper.addSpan(0,3,2,label("s4"));
      helper.addSpan(0,4,2,label("s5"));
      helper.add(0,5,label("s6"));
      helper.add(1,5,label("s7"));
      helper.add(1,6,label("s8"));

      helper.add(2,0,enable);
      helper.add(2,1,screenWidth);
      helper.add(2,2,screenDistance);
      helper.add(2,3,eyeSpacing);
      helper.add(2,4,cross);
      helper.add(2,5,tiltVertical);
      helper.add(2,6,tiltHorizontal);

      String units   = " " + App.getString("PanelStereo.s9");
      String degrees = " " + App.getString("PanelStereo.s10");

      helper.add(3,1,new JLabel(units));
      helper.add(3,2,new JLabel(units));
      helper.add(3,3,new JLabel(units));
      helper.add(3,5,new JLabel(degrees));
      helper.add(3,6,new JLabel(degrees));

      helper.addBlank(0,7);

      helper.setRowWeight(7,1);
      helper.setColumnWeight(1,1);
      helper.setColumnWeight(3,2); // unequal weight
   }

// --- helpers ---

   private static JLabel label(String key) {
      return label(key,true);
   }

   private static JLabel label(String key, boolean pad) {
      String s = App.getString("PanelStereo." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   private void get(OptionsStereo os) throws ValidationException {
      os.enable = Field.getBoolean(enable);
      os.screenWidth = Field.getDouble(screenWidth);
      os.screenDistance = Field.getDouble(screenDistance);
      os.eyeSpacing = Field.getDouble(eyeSpacing);
      os.cross = Field.getBoolean(cross);
      os.tiltVertical = Field.getDouble(tiltVertical);
      os.tiltHorizontal = Field.getDouble(tiltHorizontal);

      os.validate();
   }

   public void get(Options opt) throws ValidationException {
      get(opt.os);
   }

   private void put(OptionsStereo os) {
      Field.putBoolean(enable,os.enable);
      Field.putDouble(screenWidth,os.screenWidth);
      Field.putDouble(screenDistance,os.screenDistance);
      Field.putDouble(eyeSpacing,os.eyeSpacing);
      Field.putBoolean(cross,os.cross);
      Field.putDouble(tiltVertical,os.tiltVertical);
      Field.putDouble(tiltHorizontal,os.tiltHorizontal);
   }

   public void put(Options opt) {
      put(opt.os);
   }

}

