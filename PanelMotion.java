/*
 * PanelMotion.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing motion options.
 */

public class PanelMotion extends PanelOptions {

// --- fields ---

   private JTextField[] frameRate;
   private JTextField[] timeMove;
   private JTextField[] timeRotate;
   private JTextField[] timeAlignMove;
   private JTextField[] timeAlignRotate;

// --- construction ---

   public PanelMotion() {

      frameRate = new JTextField[2];
      timeMove = new JTextField[2];
      timeRotate = new JTextField[2];
      timeAlignMove = new JTextField[2];
      timeAlignRotate = new JTextField[2];

      for (int i=0; i<2; i++) {
         frameRate[i] = new JTextField(5);
         timeMove[i] = new JTextField(5);
         timeRotate[i] = new JTextField(5);
         timeAlignMove[i] = new JTextField(5);
         timeAlignRotate[i] = new JTextField(5);
      }

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(1,0,label("s8",false));
      helper.addCenter(3,0,label("s9",false));

      helper.add(0,1,label("s1"));
      helper.add(0,2,label("s2"));
      helper.add(0,3,label("s3"));
      helper.add(0,4,label("s4"));
      helper.add(0,5,label("s5"));

      for (int i=0; i<2; i++) {
         int x = 1 + i*2;

         helper.add(x,1,frameRate[i]);
         helper.add(x,2,timeMove[i]);
         helper.add(x,3,timeRotate[i]);
         helper.add(x,4,timeAlignMove[i]);
         helper.add(x,5,timeAlignRotate[i]);
      }

      String perSecond = " " + App.getString("PanelMotion.s6");
      String seconds   = " " + App.getString("PanelMotion.s7");

      helper.add(4,1,new JLabel(perSecond));
      helper.add(4,2,new JLabel(seconds));
      helper.add(4,3,new JLabel(seconds));
      helper.add(4,4,new JLabel(seconds));
      helper.add(4,5,new JLabel(seconds));

      helper.addBlank(0,6);

      helper.setRowWeight(6,1);
      helper.setColumnWeight(0,1);
      helper.setColumnWeight(2,1);
      helper.setColumnWeight(4,1);
   }

// --- helpers ---

   private static JLabel label(String key) {
      return label(key,true);
   }

   private static JLabel label(String key, boolean pad) {
      String s = App.getString("PanelMotion." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   private void get(OptionsMotion ot, int i) throws ValidationException {
      ot.frameRate = Field.getDouble(frameRate[i]);
      ot.timeMove = Field.getDouble(timeMove[i]);
      ot.timeRotate = Field.getDouble(timeRotate[i]);
      ot.timeAlignMove = Field.getDouble(timeAlignMove[i]);
      ot.timeAlignRotate = Field.getDouble(timeAlignRotate[i]);

      ot.validate();
   }

   public void get(Options opt) throws ValidationException {
      get(opt.ot3,0);
      get(opt.ot4,1);
   }

   private void put(OptionsMotion ot, int i) {
      Field.putDouble(frameRate[i],ot.frameRate);
      Field.putDouble(timeMove[i],ot.timeMove);
      Field.putDouble(timeRotate[i],ot.timeRotate);
      Field.putDouble(timeAlignMove[i],ot.timeAlignMove);
      Field.putDouble(timeAlignRotate[i],ot.timeAlignRotate);
   }

   public void put(Options opt) {
      put(opt.ot3,0);
      put(opt.ot4,1);
   }

}

