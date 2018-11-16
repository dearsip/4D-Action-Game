/*
 * PanelKeysSpin.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing keys options.
 */

public class PanelKeysSpin extends PanelKeys {

// --- constants ---

   private static int[] myindex = new int[] { OptionsKeys.KEY_SPIN_UP_LEFT,
                                              OptionsKeys.KEY_SPIN_UP_RIGHT,
                                              OptionsKeys.KEY_SPIN_IN_LEFT,
                                              OptionsKeys.KEY_SPIN_IN_RIGHT,
                                              OptionsKeys.KEY_SPIN_IN_UP,
                                              OptionsKeys.KEY_SPIN_IN_DOWN };

// --- construction ---

   public PanelKeysSpin() {
      super(myindex);

      // inefficient to create and hide, but simple
      key[0][2].setVisible(false);
      key[0][3].setVisible(false);
      key[0][4].setVisible(false);
      key[0][5].setVisible(false);

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(2,0,label("s1",false));
      helper.addCenter(4,0,label("s2",false));

      helper.add(0,1,label("s16"));
      helper.add(1,1,label("s17"));
      helper.add(1,2,label("s18"));
      helper.add(1,3,label("s19"));
      helper.add(1,4,label("s20"));
      helper.add(1,5,label("s21"));
      helper.add(1,6,label("s22"));

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

