/*
 * DialogWelcome.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A dialog for doing what needs to be done at startup.
 */

public class DialogWelcome extends JDialog {

// --- fields ---

   private KeyField[] key;

   private JTextField screenWidth;
   private JTextField screenDistance;
   private JTextField eyeSpacing;
   private JTextField oneInch;

   public OptionsStereo os;
   public OptionsImage oi;

// --- construction ---

   public DialogWelcome(Frame owner) {
      super(owner,s("s1"),true);
      setResizable(false);

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener( new WindowAdapter() { public void windowClosing(WindowEvent e) { doOK(); } } );

   // create fields

      key = new KeyField[8];
      for (int i=0; i<8; i++) key[i] = new KeyField(2,false); // not editable

      screenWidth = new JTextField(5);
      screenDistance = new JTextField(5);
      eyeSpacing = new JTextField(5);
      oneInch = new JTextField(5);

      os = new OptionsStereo();
      oi = new OptionsImage();

   // create key panel

      JPanel panelKey = new JPanel();
      GridBagHelper helper = new GridBagHelper(panelKey);

      helper.add(0,0,lp("s6"));
      helper.add(1,0,lp("s7"));
      helper.add(1,1,lp("s8"));
      helper.add(0,2,lp("s9"));
      helper.add(1,2,lp("s10"));
      helper.add(1,3,lp("s11"));

      helper.add(3,0,Box.createHorizontalStrut(10));

      helper.add(4,0,lp("s9"));
      helper.add(5,0,lp("s12"));
      helper.add(5,1,lp("s13"));
      helper.add(5,2,lp("s14"));
      helper.add(5,3,lp("s15"));

      for (int i=0; i<8; i++) {
         int x = 2 + (i / 4)*4;
         int y =      i % 4;
         helper.add(x,y,key[i]);
      }

   // create field panel

      JPanel panelField = new JPanel();
      helper = new GridBagHelper(panelField);

      helper.add(0,0,lp("s24"));
      helper.add(0,1,lp("s25"));
      helper.add(0,2,lp("s26"));
      helper.add(0,3,lp("s27"));

      helper.add(1,0,screenWidth);
      helper.add(1,1,screenDistance);
      helper.add(1,2,eyeSpacing);
      helper.add(1,3,oneInch);

      String units = " " + s("s28");

      helper.add(2,0,new JLabel(units));
      helper.add(2,1,new JLabel(units));
      helper.add(2,2,new JLabel(units));
      helper.add(2,3,new JLabel(units));

   // create main panel

      // BoxLayout would be the logical choice here,
      // but it looks at the component's alignmentX values,
      // and those are set all wrong by default

      JPanel panelMain = new JPanel();
      panelMain.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      helper = new GridBagHelper(panelMain);
      int y = 0;

      helper.addCenter(0,y++,ls("s2"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.add(0,y++,ls("s34"));
      helper.add(0,y++,ls("s35"));
      helper.add(0,y++,ls("s36"));
      helper.add(0,y++,ls("s3"));
      helper.add(0,y++,ls("s4"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.add(0,y++,ls("s5"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.addCenter(0,y++,panelKey);
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.add(0,y++,ls("s16"));
      helper.add(0,y++,ls("s17"));
      helper.add(0,y++,ls("s18"));
      helper.add(0,y++,ls("s19"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.add(0,y++,ls("s20"));
      helper.add(0,y++,ls("s21"));
      helper.add(0,y++,ls("s22"));
      helper.add(0,y++,ls("s23"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.addCenter(0,y++,panelField);
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.add(0,y++,ls("s29"));
      helper.add(0,y++,ls("s30"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.addCenter(0,y++,ls("s31"));

   // create buttons

      JPanel panelButton = new JPanel();
      JButton button;

      button = new JButton(s("s32"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doOK(); } } );
      panelButton.add(button);

   // add to content pane

      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());

      contentPane.add(panelMain,  BorderLayout.CENTER);
      contentPane.add(panelButton,BorderLayout.SOUTH);

   // finish up

      pack();
      setLocationRelativeTo(owner);
   }

// --- helpers ---

   private static String s(String key) {
      return App.getString("DialogWelcome." + key);
   }

   private static String p(String key) {
      return s(key) + " "; // pad
   }

   private static JLabel l(String text) {
      return new JLabel(text);
   }

   private static JLabel ls(String key) {
      return l(s(key));
   }

   private static JLabel lp(String key) {
      return l(p(key));
   }

// --- commands ---

   private void doOK() {
      try {
         get();
      } catch (ValidationException e) {
         JOptionPane.showMessageDialog(this,e.getMessage(),App.getString("DialogWelcome.s33"),JOptionPane.ERROR_MESSAGE);
         return;
      }
      dispose();
   }

// --- transfer functions ---

   private void get() throws ValidationException {
      os.screenWidth = Field.getDouble(screenWidth);
      os.screenDistance = Field.getDouble(screenDistance);
      os.eyeSpacing = Field.getDouble(eyeSpacing);
      oi.oneInch = Field.getDouble(oneInch);

      os.validate();
      oi.validate();
      // skip options-level validations
   }

   public void put(OptionsKeys ok, OptionsStereo os, OptionsImage oi) {

      key[0].put(ok.key[OptionsKeys.KEY_FORWARD]);
      key[1].put(ok.key[OptionsKeys.KEY_BACK]);
      key[2].put(ok.key[OptionsKeys.KEY_TURN_LEFT]);
      key[3].put(ok.key[OptionsKeys.KEY_TURN_RIGHT]);
      key[4].put(ok.key[OptionsKeys.KEY_TURN_UP]);
      key[5].put(ok.key[OptionsKeys.KEY_TURN_DOWN]);
      key[6].put(ok.key[OptionsKeys.KEY_TURN_IN]);
      key[7].put(ok.key[OptionsKeys.KEY_TURN_OUT]);

      Field.putDouble(screenWidth,os.screenWidth);
      Field.putDouble(screenDistance,os.screenDistance);
      Field.putDouble(eyeSpacing,os.eyeSpacing);
      Field.putDouble(oneInch,oi.oneInch);

      // save non-displayed fields
      OptionsStereo.copy(this.os,os);
      OptionsImage.copy(this.oi,oi);
   }

}

