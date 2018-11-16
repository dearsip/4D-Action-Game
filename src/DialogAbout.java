/*
 * DialogAbout.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A dialog for showing information about the game.
 */

public class DialogAbout extends JDialog {

// --- construction ---

   public DialogAbout(Frame owner) {
      super(owner,s("s1"),true);
      setResizable(false);

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener( new WindowAdapter() { public void windowClosing(WindowEvent e) { doOK(); } } );

   // create main panel

      JPanel panelMain = new JPanel();
      panelMain.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      GridBagHelper helper = new GridBagHelper(panelMain);
      int y = 0;

      helper.addCenter(0,y++,ls("s9"));
      helper.addCenter(0,y++,l(App.getString("URL2")));
      helper.addCenter(0,y++,ls("s10"));
      helper.addCenter(0,y++,ls("s11"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.addCenter(0,y++,ls("s3"));
      helper.addCenter(0,y++,ls("s4"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.addCenter(0,y++,ls("s12"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.addCenter(0,y++,ls("s2"));
      helper.addCenter(0,y++,l(App.getString("URL1")));
      helper.addCenter(0,y++,ls("s5"));
      helper.addCenter(0,y++,ls("s8"));
      helper.add(0,y++,Box.createVerticalStrut(10));
      helper.addCenter(0,y++,ls("s6"));

   // create buttons

      JPanel panelButton = new JPanel();
      JButton button;

      button = new JButton(s("s7"));
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
      return App.getString("DialogAbout." + key);
   }

   private static JLabel l(String text) {
      return new JLabel(text);
   }

   private static JLabel ls(String key) {
      return l(s(key));
   }

// --- commands ---

   private void doOK() {
      dispose();
   }

}

