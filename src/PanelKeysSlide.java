/*
 * PanelKeysSlide.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing keys options.
 */

public class PanelKeysSlide extends PanelKeys {

// --- constants ---

   private static int[] myindex = new int[] { OptionsKeys.KEY_SLIDE_LEFT,
                                              OptionsKeys.KEY_SLIDE_RIGHT,
                                              OptionsKeys.KEY_SLIDE_UP,
                                              OptionsKeys.KEY_SLIDE_DOWN,
                                              OptionsKeys.KEY_SLIDE_IN,
                                              OptionsKeys.KEY_SLIDE_OUT };

// --- construction ---

   public PanelKeysSlide() {
      super(myindex);

      // inefficient to create and hide, but simple
      key[0][4].setVisible(false);
      key[0][5].setVisible(false);

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(2,0,label("s1",false));
      helper.addCenter(4,0,label("s2",false));

      helper.add(0,1,label("s15"));
      helper.add(1,1,label("s9"));
      helper.add(1,2,label("s10"));
      helper.add(1,3,label("s11"));
      helper.add(1,4,label("s12"));
      helper.add(1,5,label("s13"));
      helper.add(1,6,label("s14"));

      for (int i=0; i<2; i++) {
         int x = 2 + i*2;
         for (int j=0; j<6; j++) {
            int y = 1 + j;
            helper.add(x,y,key[i][j]);
         }
      }

      helper.addBlank(0,7);
      helper.addBlank(5,0);

      helper.setRowWeight(7,1);
      helper.setColumnWeight(1,1);
      helper.setColumnWeight(3,1);
      helper.setColumnWeight(5,1);
   }

}

