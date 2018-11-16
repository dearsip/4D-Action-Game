/*
 * PanelMap.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing map options.
 */

public class PanelMap extends PanelOptions {

// --- fields ---

   private JTextField[] dimMap;
   private JTextField[] size;
   private JTextField[] density;
   private JTextField[] twistProbability;
   private JTextField[] branchProbability;
   private JCheckBox[] allowLoops;
   private JTextField[] loopCrossProbability;

// --- construction ---

   public PanelMap() {

      dimMap = new JTextField[3];
      size = new JTextField[3];
      density = new JTextField[3];
      twistProbability = new JTextField[3];
      branchProbability = new JTextField[3];
      allowLoops = new JCheckBox[3];
      loopCrossProbability = new JTextField[3];

      for (int i=0; i<3; i++) {
         dimMap[i] = new JTextField(5);
         size[i] = new JTextField(5);
         density[i] = new JTextField(5);
         twistProbability[i] = new JTextField(5);
         branchProbability[i] = new JTextField(5);
         allowLoops[i] = new JCheckBox();
         loopCrossProbability[i] = new JTextField(5);
      }

      // omCurrent is for display only
      setEnabled(0,false);

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(1,0,label("s1",false));
      helper.addCenter(3,0,label("s2",false));
      helper.addCenter(5,0,label("s3",false));

      helper.add(0,1,label("s4"));
      helper.add(0,2,label("s5"));
      helper.add(0,3,label("s6"));
      helper.add(0,4,label("s7"));
      helper.add(0,5,label("s8"));
      helper.add(0,6,label("s9"));
      helper.add(0,7,label("s10"));

      for (int i=0; i<3; i++) {
         int x = 1 + i*2;
         helper.add(x,1,dimMap[i]);
         helper.add(x,2,size[i]);
         helper.add(x,3,density[i]);
         helper.add(x,4,twistProbability[i]);
         helper.add(x,5,branchProbability[i]);
         helper.add(x,6,allowLoops[i]);
         helper.add(x,7,loopCrossProbability[i]);
      }

      helper.addBlank(0,8);
      helper.addBlank(6,0);

      helper.setRowWeight(8,1);
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
      String s = App.getString("PanelMap." + key);
      if (pad) s += " ";
      return new JLabel(s);
   }

// --- transfer functions ---

   private void get(OptionsMap om, int i) throws ValidationException {
      om.dimMap = Field.getInteger(dimMap[i]);
      om.validateDimMap(); // otherwise getMapSize might do strange things
      Field.getMapSize(size[i],om.size,om.dimMap);
      om.density = Field.getDouble(density[i]);
      om.twistProbability = Field.getDouble(twistProbability[i]);
      om.branchProbability = Field.getDouble(branchProbability[i]);
      om.allowLoops = Field.getBoolean(allowLoops[i]);
      om.loopCrossProbability = Field.getDouble(loopCrossProbability[i]);

      om.validate();
   }

   public void get(Options opt) throws ValidationException {
      get(opt.om3,1);
      get(opt.om4,2);
   }

   // there is no getCurrent, omCurrent is for display only

   private void setEnabled(int i, boolean b) {
      dimMap[i].setEnabled(b);
      size[i].setEnabled(b);
      density[i].setEnabled(b);
      twistProbability[i].setEnabled(b);
      branchProbability[i].setEnabled(b);
      allowLoops[i].setEnabled(b);
      loopCrossProbability[i].setEnabled(b);
   }

   private void put(OptionsMap om, int i) {
      Field.putInteger(dimMap[i],om.dimMap);
      Field.putMapSize(size[i],om.size,om.dimMap);
      Field.putDouble(density[i],om.density);
      Field.putDouble(twistProbability[i],om.twistProbability);
      Field.putDouble(branchProbability[i],om.branchProbability);
      Field.putBoolean(allowLoops[i],om.allowLoops);
      Field.putDouble(loopCrossProbability[i],om.loopCrossProbability);
   }

   public void put(Options opt) {
      put(opt.om3,1);
      put(opt.om4,2);
   }

   public void putCurrent(OptionsMap omCurrent) {
      put(omCurrent,0);
   }

}

