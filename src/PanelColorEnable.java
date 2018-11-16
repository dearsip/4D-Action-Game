/*
 * PanelColorEnable.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing color options.
 */

public class PanelColorEnable extends PanelOptions {

// --- fields ---

   private JLabel[][] header;
   private JCheckBox[][] enable;

// --- construction ---

   public PanelColorEnable() {

      header = new JLabel[3][2];
      enable = new JCheckBox[3][12];

      for (int i=0; i<3; i++) {
         for (int j=0; j<12; j++) {
            enable[i][j] = new JCheckBox();
         }
      }

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(2,0,header[0][0] = label("s1",false));
      helper.addCenter(4,0,header[1][0] = label("s2",false));
      helper.addCenter(6,0,header[2][0] = label("s3",false));

      helper.add(0,1,label("s8"));
      helper.add(1,1,label("s9"));
      helper.add(1,2,label("s10"));
      helper.add(1,3,label("s11"));
      helper.add(1,4,label("s12"));
      helper.add(1,5,label("s13"));
      helper.add(1,6,label("s14"));

      helper.addCenter(10,0,header[0][1] = label("s1",false));
      helper.addCenter(12,0,header[1][1] = label("s2",false));
      helper.addCenter(14,0,header[2][1] = label("s3",false));

      helper.add(8,1,label("s8"));
      helper.add(9,1,label("s15"));
      helper.add(9,2,label("s16"));
      helper.add(9,3,label("s17"));
      helper.add(9,4,label("s18"));
      helper.add(9,5,label("s19"));
      helper.add(9,6,label("s20"));

      for (int i=0; i<3; i++) {
         int x = 2 + i*2;
         for (int j=0; j<12; j++) {
            int dx = 8 * (j / 6);
            int dy =     (j % 6);
            helper.add(x+dx,1+dy,enable[i][j]);
         }
      }

      helper.addBlank(0,7);
      helper.addBlank(15,0);

      helper.setRowWeight(7,1);
      helper.setColumnWeight(1,1);
      helper.setColumnWeight(3,1);
      helper.setColumnWeight(5,1);
      helper.setColumnWeight(7,1);
      helper.setColumnWeight(9,1);
      helper.setColumnWeight(11,1);
      helper.setColumnWeight(13,1);
      helper.setColumnWeight(15,1);
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
      for (int j=0; j<12; j++) {
         oc.enable[j] = Field.getBoolean(enable[i][j]);
      }

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
      header[i][0].setVisible(b);
      header[i][1].setVisible(b);
      for (int j=0; j<12; j++) {
         enable[i][j].setVisible(b);
      }
   }

   private void put(OptionsColor oc, int i) {
      for (int j=0; j<12; j++) {
         Field.putBoolean(enable[i][j],oc.enable[j]);
      }
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

