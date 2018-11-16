/*
 * PanelKeysMove.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing keys options.
 */

public class PanelKeysMove extends PanelKeys {

// --- fields ---

   private JCheckBox[] startAlignMode;

// --- constants ---

   private static int[] myindex = new int[] { OptionsKeys.KEY_FORWARD,
                                              OptionsKeys.KEY_BACK,
                                              OptionsKeys.KEY_ALIGN,
                                              OptionsKeys.KEY_CHANGE_ALIGN_MODE };

// --- construction ---

   public PanelKeysMove() {
      super(myindex);

      startAlignMode = new JCheckBox[2];

      for (int i=0; i<2; i++) {
         startAlignMode[i] = new JCheckBox();
      }

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(2,0,label("s1",false));
      helper.addCenter(4,0,label("s2",false));

      helper.add(0,1,label("s3"));
      helper.add(1,1,label("s4"));
      helper.add(1,2,label("s5"));
      helper.add(0,3,Box.createVerticalStrut(5));
      helper.addSpan(0,4,2,label("s43"));
      helper.addSpan(0,5,2,label("s6"));
      helper.addSpan(0,6,2,label("s7"));

      for (int i=0; i<2; i++) {
         int x = 2 + i*2;
         helper.add(x,1,key[i][0]);
         helper.add(x,2,key[i][1]);
         helper.add(x,4,startAlignMode[i]);
         helper.add(x,5,key[i][2]);
         helper.add(x,6,key[i][3]);
      }

      helper.addBlank(0,7);
      helper.addBlank(5,0);

      helper.setRowWeight(7,1);
      helper.setColumnWeight(1,1);
      helper.setColumnWeight(3,1);
      helper.setColumnWeight(5,1);
   }

// --- transfer functions ---

   protected void get(OptionsKeys ok, int i) throws ValidationException {
      ok.startAlignMode = Field.getBoolean(startAlignMode[i]);
      super.get(ok,i);
   }

   protected void put(OptionsKeys ok, int i) {
      Field.putBoolean(startAlignMode[i],ok.startAlignMode);
      super.put(ok,i);
   }

}

