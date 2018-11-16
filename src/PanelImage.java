/*
 * PanelImage.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing image options.
 */

public class PanelImage extends PanelOptions {

// --- fields ---

   private JComboBox background;
   private JCheckBox invertColors;
   private JComboBox convertColors;
   private JTextField lineWidth;
   private JTextField oneInch;

// --- constants ---

   private static Object[] backgroundNames = new Object[] { App.getString("PanelImage.s8"),
                                                            App.getString("PanelImage.s9")  };

   private static int[] backgroundValues = new int[] { OptionsImage.BACKGROUND_BLACK,
                                                       OptionsImage.BACKGROUND_WHITE  };

   private static Object[] convertColorsNames = new Object[] { App.getString("PanelImage.s10"),
                                                               App.getString("PanelImage.s11"),
                                                               App.getString("PanelImage.s12")  };

   private static int[] convertColorsValues = new int[] { OptionsImage.CONVERT_NORMAL,
                                                          OptionsImage.CONVERT_GRAY_SCALE,
                                                          OptionsImage.CONVERT_B_AND_W     };

// --- construction ---

   public PanelImage() {

      background = new JComboBox(backgroundNames);
      invertColors = new JCheckBox();
      convertColors = new JComboBox(convertColorsNames);
      lineWidth = new JTextField(5);
      oneInch = new JTextField(5);

      GridBagHelper helper = new GridBagHelper(this);

      helper.add(0,0,label("s1"));
      helper.add(0,1,label("s2"));
      helper.add(0,2,label("s3"));
      helper.add(0,3,label("s4"));
      helper.add(0,4,label("s5"));

      helper.addSpan(1,0,2,background);
      helper.addSpan(1,1,2,invertColors);
      helper.addSpan(1,2,2,convertColors);
      helper.add(1,3,lineWidth);
      helper.add(1,4,oneInch);

      String pixels      = " " + App.getString("PanelImage.s6");
      String stereoUnits = " " + App.getString("PanelImage.s7");

      helper.add(2,3,new JLabel(pixels));
      helper.add(2,4,new JLabel(stereoUnits));

      helper.addBlank(0,5);

      helper.setRowWeight(5,1);
      helper.setColumnWeight(0,1);
      helper.setColumnWeight(2,2); // unequal weight
   }

// --- helpers ---

   private static JLabel label(String key) {
      return label(key,true);
   }

   private static JLabel label(String key, boolean pad) {
      String s = App.getString("PanelImage." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   private void get(OptionsImage oi) throws ValidationException {
      oi.background = Field.getEnumerated(background,backgroundValues);
      oi.invertColors = Field.getBoolean(invertColors);
      oi.convertColors = Field.getEnumerated(convertColors,convertColorsValues);
      oi.lineWidth = Field.getDouble(lineWidth);
      oi.oneInch = Field.getDouble(oneInch);

      oi.validate();
   }

   public void get(Options opt) throws ValidationException {
      get(opt.oi);
   }

   private void put(OptionsImage oi) {
      Field.putEnumerated(background,backgroundValues,oi.background);
      Field.putBoolean(invertColors,oi.invertColors);
      Field.putEnumerated(convertColors,convertColorsValues,oi.convertColors);
      Field.putDouble(lineWidth,oi.lineWidth);
      Field.putDouble(oneInch,oi.oneInch);
   }

   public void put(Options opt) {
      put(opt.oi);
   }

}

