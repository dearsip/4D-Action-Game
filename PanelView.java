/*
 * PanelView.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing view options.
 */

public class PanelView extends PanelOptions {

// --- fields ---

   private JLabel[] header;
   private JTextField[] depth;
   private JCheckBox[][] texture;
   private JTextField[] retina;
   private JTextField[] scale;

// --- construction ---

   public PanelView() {

      header = new JLabel[3];
      depth = new JTextField[3];
      texture = new JCheckBox[3][10];
      retina = new JTextField[3];
      scale = new JTextField[3];

      for (int i=0; i<3; i++) {
         depth[i] = new JTextField(5);
         for (int j=0; j<10; j++) {
            texture[i][j] = new JCheckBox();
         }
         retina[i] = new JTextField(5);
         scale[i] = new JTextField(5);
      }

      GridBagHelper helper = new GridBagHelper(this);

      helper.addSpanCenter( 2,0,3,header[0] = label("s11",false));
      helper.addSpanCenter( 6,0,3,header[1] = label("s1",false));
      helper.addSpanCenter(10,0,3,header[2] = label("s2",false));

      helper.addSpan(0,1,2,label("s3"));
      helper.addSpan(0,2,2,label("s4"));
      helper.add(0,3,label("s5"));
      helper.add(1,3,label("s6"));
      helper.add(1,4,label("s7"));
      helper.add(1,5,label("s8"));
      helper.addSpan(0,6,2,label("s9"));
      helper.addSpan(0,7,2,label("s10"));

      for (int i=0; i<3; i++) {
         int x = 2 + i*4;
         helper.addSpan(x,1,3,depth[i]);
         helper.addSpan(x,2,3,texture[i][0]);
         for (int j=1; j<10; j++) {
            int dx = (j-1) % 3;
            int dy = (j-1) / 3;
            helper.add(x+dx,3+dy,texture[i][j]);
         }
         helper.addSpan(x,6,3,retina[i]);
         helper.addSpan(x,7,3,scale[i]);
      }

      helper.addBlank(0,8);
      helper.addBlank(13,0);

      helper.setRowWeight(8,1);
      helper.setColumnWeight(1,1);
      helper.setColumnWeight(5,1);
      helper.setColumnWeight(9,1);
      helper.setColumnWeight(13,1);
   }

// --- helpers ---

   private static JLabel label(String key) {
      return label(key,true);
   }

   private static JLabel label(String key, boolean pad) {
      String s = App.getString("PanelView." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   private void get(OptionsView ov, int i) throws ValidationException {
      ov.depth = Field.getInteger(depth[i]);
      for (int j=0; j<10; j++) {
         ov.texture[j] = Field.getBoolean(texture[i][j]);
      }
      ov.retina = Field.getDouble(retina[i]);
      ov.scale = Field.getDouble(scale[i]);

      ov.validate();
   }

   public void get(Options opt) throws ValidationException {
      get(opt.ov3,1);
      get(opt.ov4,2);
   }

   public void getCurrent(OptionsView ovCurrent) throws ValidationException {
      if (ovCurrent != null) {
         get(ovCurrent,0);
      }
      // else do nothing
   }

   private void setVisible(int i, boolean b) {
      header[i].setVisible(b);
      depth[i].setVisible(b);
      for (int j=0; j<10; j++) {
         texture[i][j].setVisible(b);
      }
      retina[i].setVisible(b);
      scale[i].setVisible(b);
   }

   private void put(OptionsView ov, int i) {
      Field.putInteger(depth[i],ov.depth);
      for (int j=0; j<10; j++) {
         Field.putBoolean(texture[i][j],ov.texture[j]);
      }
      Field.putDouble(retina[i],ov.retina);
      Field.putDouble(scale[i],ov.scale);
   }

   public void put(Options opt) {
      put(opt.ov3,1);
      put(opt.ov4,2);
   }

   public void putCurrent(OptionsView ovCurrent) {
      if (ovCurrent != null) {
         setVisible(0,true);
         put(ovCurrent,0);
      } else {
         setVisible(0,false);
      }
   }

}

