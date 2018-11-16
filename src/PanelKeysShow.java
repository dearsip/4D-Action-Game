/*
 * PanelKeysShow.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing keys options.
 */

public class PanelKeysShow extends PanelKeysConfig {

// --- constants ---

   private static int[] myindex = new int[] { OptionsKeysConfig.KEY_NEW_GAME,
                                              OptionsKeysConfig.KEY_OPTIONS,
                                              OptionsKeysConfig.KEY_EXIT_GAME,
                                              OptionsKeysConfig.KEY_TEXTURE+0,
                                              OptionsKeysConfig.KEY_TEXTURE+1,
                                              OptionsKeysConfig.KEY_TEXTURE+2,
                                              OptionsKeysConfig.KEY_TEXTURE+3,
                                              OptionsKeysConfig.KEY_TEXTURE+4,
                                              OptionsKeysConfig.KEY_TEXTURE+5,
                                              OptionsKeysConfig.KEY_TEXTURE+6,
                                              OptionsKeysConfig.KEY_TEXTURE+7,
                                              OptionsKeysConfig.KEY_TEXTURE+8,
                                              OptionsKeysConfig.KEY_TEXTURE+9 };

// --- construction ---

   public PanelKeysShow() {
      super(myindex);

      GridBagHelper helper = new GridBagHelper(this);

      helper.addSpan(0,0,2,label("s23"));
      helper.addSpan(0,1,2,label("s41"));
      helper.addSpan(0,2,2,label("s42"));
      helper.add(0,3,Box.createVerticalStrut(5));
      helper.addSpan(0,4,2,label("s24"));
      helper.add(0,5,label("s25"));
      helper.add(1,5,label("s26"));
      helper.add(1,6,label("s27"));
      helper.add(1,7,label("s28"));

      helper.add(2,0,key[0]);
      helper.add(2,1,key[1]);
      helper.add(2,2,key[2]);
      helper.add(2,4,key[3]);
      for (int j=1; j<10; j++) {
         int dx = (j-1) % 3;
         int dy = (j-1) / 3;
         helper.add(2+dx,5+dy,key[3+j]);
      }

      helper.addBlank(0,8);

      helper.setRowWeight(8,1);
      helper.setColumnWeight(1,1);
      helper.setColumnWeight(2,1);
      helper.setColumnWeight(3,1);
      helper.setColumnWeight(4,1);
   }

}

