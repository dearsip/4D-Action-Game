/*
 * PanelKeysParam.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for editing keys options.
 */

public class PanelKeysParam extends PanelKeysConfig {

// --- fields ---

   private JComboBox[] param;

// --- constants ---

   private static int[] myindex = new int[] { OptionsKeysConfig.KEY_PARAM_DECREASE+0,
                                              OptionsKeysConfig.KEY_PARAM_DECREASE+1,
                                              OptionsKeysConfig.KEY_PARAM_DECREASE+2,
                                              OptionsKeysConfig.KEY_PARAM_DECREASE+3,
                                              OptionsKeysConfig.KEY_PARAM_DECREASE+4,
                                              OptionsKeysConfig.KEY_PARAM_DECREASE+5,
                                              OptionsKeysConfig.KEY_PARAM_INCREASE+0,
                                              OptionsKeysConfig.KEY_PARAM_INCREASE+1,
                                              OptionsKeysConfig.KEY_PARAM_INCREASE+2,
                                              OptionsKeysConfig.KEY_PARAM_INCREASE+3,
                                              OptionsKeysConfig.KEY_PARAM_INCREASE+4,
                                              OptionsKeysConfig.KEY_PARAM_INCREASE+5 };

   private static Object[] paramNames = new Object[] { "",
                                                       App.getString("PanelKeys.s32"),
                                                       App.getString("PanelKeys.s33"),
                                                       App.getString("PanelKeys.s34"),
                                                       App.getString("PanelKeys.s35"),
                                                       App.getString("PanelKeys.s36"),
                                                       App.getString("PanelKeys.s37"),
                                                       App.getString("PanelKeys.s38"),
                                                       App.getString("PanelKeys.s39"),
                                                       App.getString("PanelKeys.s40"),
                                                       App.getString("PanelKeys.s44"),
                                                       App.getString("PanelKeys.s45"),
                                                       App.getString("PanelKeys.s46") };

   private static int[] paramValues = new int[] { OptionsKeysConfig.PARAM_NONE,
                                                  OptionsKeysConfig.PARAM_COLOR_MODE,
                                                  OptionsKeysConfig.PARAM_DEPTH,
                                                  OptionsKeysConfig.PARAM_RETINA,
                                                  OptionsKeysConfig.PARAM_SCALE,
                                                  OptionsKeysConfig.PARAM_SCREEN_WIDTH,
                                                  OptionsKeysConfig.PARAM_SCREEN_DISTANCE,
                                                  OptionsKeysConfig.PARAM_EYE_SPACING,
                                                  OptionsKeysConfig.PARAM_TILT_VERTICAL,
                                                  OptionsKeysConfig.PARAM_TILT_HORIZONTAL,
                                                  OptionsKeysConfig.PARAM_FISHEYE_WIDTH,
                                                  OptionsKeysConfig.PARAM_FISHEYE_FLARE,
                                                  OptionsKeysConfig.PARAM_FISHEYE_RGAP   };

// --- construction ---

   public PanelKeysParam() {
      super(myindex);

      param = new JComboBox[6];

      for (int i=0; i<6; i++) {
         param[i] = new JComboBox(paramNames);
         param[i].setMaximumRowCount(paramNames.length); // avoid scrolling
      }

      GridBagHelper helper = new GridBagHelper(this);

      helper.addCenter(2,0,label("s29"));
      helper.addCenter(4,0,label("s30"));

      helper.add(0,1,label("s31"));

      for (int i=0; i<6; i++) {
         helper.add(1,1+i,param[i]);
      }

      for (int j=0; j<12; j++) {
         int dx = 2 * (j / 6);
         int dy =      j % 6;
         helper.add(2+dx,1+dy,key[j]);
      }

      helper.addBlank(0,7);
      helper.addBlank(5,0);

      helper.setRowWeight(7,1);
      helper.setColumnWeight(1,1);
      helper.setColumnWeight(3,1);
      helper.setColumnWeight(5,1);
   }

// --- transfer functions ---

   protected void get(OptionsKeysConfig okc) throws ValidationException {
      for (int i=0; i<6; i++) {
         okc.param[i] = Field.getEnumerated(param[i],paramValues);
      }
      super.get(okc);
   }

   protected void put(OptionsKeysConfig okc) {
      for (int i=0; i<6; i++) {
         Field.putEnumerated(param[i],paramValues,okc.param[i]);
      }
      super.put(okc);
   }

}

