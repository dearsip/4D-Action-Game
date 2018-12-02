/*
 * PanelColor.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing color options.
 */

public class PanelColor extends PanelOptions {

// --- fields ---

   private JLabel[] header;
   private JComboBox[] colorMode;
   private JTextField[] dimSameParallel;
   private JTextField[] dimSamePerpendicular;

// --- constants ---

   private static Object[] colorModeNames = new Object[] { App.getString("PanelColor.s21"),
                                                           App.getString("PanelColor.s22"),
                                                           App.getString("PanelColor.s23"),
                                                           App.getString("PanelColor.s24"),
                                                           App.getString("PanelColor.s25")  };

   private static int[] colorModeValues = new int[] { OptionsColor.COLOR_MODE_EXTERIOR,
                                                      OptionsColor.COLOR_MODE_INTERIOR,
                                                      OptionsColor.COLOR_MODE_BY_ORIENTATION,
                                                      OptionsColor.COLOR_MODE_BY_DIRECTION,
                                                      OptionsColor.COLOR_MODE_BY_TRACE};

// --- construction ---

   public PanelColor() {

      header = new JLabel[3];
      colorMode = new JComboBox[3];
      dimSameParallel = new JTextField[3];
      dimSamePerpendicular = new JTextField[3];

      for (int i=0; i<3; i++) {
         colorMode[i] = new JComboBox(colorModeNames);
         dimSameParallel[i] = new JTextField(5);
         dimSamePerpendicular[i] = new JTextField(5);
      }

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(1,0,header[0] = label("s1",false));
      helper.addCenter(3,0,header[1] = label("s2",false));
      helper.addCenter(5,0,header[2] = label("s3",false));

      helper.add(0,1,label("s4"));
      helper.add(0,2,Box.createVerticalStrut(5));
      helper.add(0,3,label("s5"));
      helper.add(0,4,label("s6"));
      helper.add(0,5,label("s7"));

      for (int i=0; i<3; i++) {
         int x = 1 + i*2;
         helper.add(x,1,colorMode[i]);
         helper.add(x,4,dimSameParallel[i]);
         helper.add(x,5,dimSamePerpendicular[i]);
      }

      helper.addBlank(0,6);
      helper.addBlank(6,0);

      helper.setRowWeight(6,1);
      helper.setColumnWeight(0,1);
      helper.setColumnWeight(2,1);
      helper.setColumnWeight(4,1);
      helper.setColumnWeight(6,1);
   }

// --- helpers ---

   private static JLabel label(String key) {
      return label(key,true);
   }

   private static JLabel label(String key, boolean pad) {
      String s = App.getString("PanelColor." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   private void get(OptionsColor oc, int i) throws ValidationException {
      oc.colorMode = Field.getEnumerated(colorMode[i],colorModeValues);
      oc.dimSameParallel = Field.getInteger(dimSameParallel[i]);
      oc.dimSamePerpendicular = Field.getInteger(dimSamePerpendicular[i]);

      // multi-tab options validated in DialogOptions
   }

   public void get(Options opt) throws ValidationException {
      get(opt.oc3,1);
      get(opt.oc4,2);
   }

   public void getCurrent(OptionsColor ocCurrent) throws ValidationException {
      if (ocCurrent != null) {
         get(ocCurrent,0);
      }
      // else do nothing
   }

   private void setVisible(int i, boolean b) {
      header[i].setVisible(b);
      colorMode[i].setVisible(b);
      dimSameParallel[i].setVisible(b);
      dimSamePerpendicular[i].setVisible(b);
   }

   private void put(OptionsColor oc, int i) {
      Field.putEnumerated(colorMode[i],colorModeValues,oc.colorMode);
      Field.putInteger(dimSameParallel[i],oc.dimSameParallel);
      Field.putInteger(dimSamePerpendicular[i],oc.dimSamePerpendicular);
   }

   public void put(Options opt) {
      put(opt.oc3,1);
      put(opt.oc4,2);
   }

   public void putCurrent(OptionsColor ocCurrent) {
      if (ocCurrent != null) {
         setVisible(0,true);
         put(ocCurrent,0);
      } else {
         setVisible(0,false);
      }
   }

}

