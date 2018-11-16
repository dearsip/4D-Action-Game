/*
 * DialogKey.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A dialog for entering key codes and modifiers.
 */

public class DialogKey extends JDialog implements KeyListener {

// --- fields ---

   private JComboBox keyField;
   private JCheckBox[] modifierField;

   private Key result;

// --- construction ---

   public DialogKey(Dialog owner) {
      super(owner,App.getString("DialogKey.s1"),true);
      setResizable(false);

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener( new WindowAdapter() { public void windowClosing(WindowEvent e) { doCancel(); } } );

   // create buttons

      JPanel panel = new JPanel();
      JButton button;

      button = new JButton(App.getString("DialogKey.s3"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doSet(); } } );
      panel.add(button);

      button = new JButton(App.getString("DialogKey.s4"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doClear(); } } );
      panel.add(button);

      button = new JButton(App.getString("DialogKey.s5"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doCancel(); } } );
      panel.add(button);

   // add to content pane

      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());

      JLabel label = new JLabel(App.getString("DialogKey.s2"));
      label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      contentPane.add(label,BorderLayout.NORTH);
      contentPane.add(constructFieldPanel(),BorderLayout.CENTER);
      contentPane.add(panel,BorderLayout.SOUTH);

   // finish up

      pack();
      setLocationRelativeTo(owner);
   }

// --- field panel ---

   private static Object[] specialKeys = { new Key(KeyEvent.VK_ENTER),
                                           new Key(KeyEvent.VK_ESCAPE),
                                           new Key(KeyEvent.VK_SPACE),
                                           new Key(KeyEvent.VK_TAB)     };

   private JPanel constructFieldPanel() {
      int[] allowed = Key.getAllowedModifiers();

      keyField = new JComboBox(specialKeys); // using Key.toString to get item text

      modifierField = new JCheckBox[allowed.length];
      for (int i=0; i<allowed.length; i++) {
         modifierField[i] = new JCheckBox();
         modifierField[i].addKeyListener(this);
      }
      // I used to listen on the JDialog itself, but that didn't work
      // with the Java 1.4 focus system, and also it makes more sense
      // to restrict listening to the checkboxes.

      JPanel panel = new JPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      GridBagHelper helper = new GridBagHelper(panel);

      int left = (allowed.length + 1) / 2;

      int i = 0;
      for ( ; i<left; i++) {
         helper.add(0,i,modifierField[i]);
         helper.add(1,i,new JLabel(KeyEvent.getKeyModifiersText(allowed[i])));
      }

      helper.add(2,0,Box.createHorizontalStrut(5));

      for ( ; i<allowed.length; i++) {
         helper.add(3,i-left,modifierField[i]);
         helper.add(4,i-left,new JLabel(KeyEvent.getKeyModifiersText(allowed[i])));
      }

      helper.add(5,0,Box.createHorizontalStrut(10));

      GridBagConstraints constraints = new GridBagConstraints();
      constraints.anchor = GridBagConstraints.WEST;
      constraints.gridheight = left;

      helper.add(6,0,keyField,constraints);

      return panel;
   }

   private Key getFromFieldPanel() {
      int[] allowed = Key.getAllowedModifiers();

      int code = ((Key) keyField.getSelectedItem()).code;

      int modifiers = 0;
      for (int i=0; i<allowed.length; i++) {
         if (modifierField[i].isSelected()) modifiers |= allowed[i];
      }

      return new Key(code,modifiers);
   }

// --- commands ---

   private void doSet() {
      result = getFromFieldPanel();
      dispose();
   }

   private void doClear() {
      result = new Key();
      dispose();
   }

   private void doCancel() {
      // leave result null
      dispose();
   }

   public Key run() {
      setVisible(true);
      return result;
   }

// --- implementation of KeyListener ---

   public void keyTyped(KeyEvent e) {
   }

   public void keyPressed(KeyEvent e) {

      int code = e.getKeyCode();
      if (Key.isModifier(code)) return;

      int modifiers = Key.restrictToAllowedModifiers(e.getModifiers());

      result = new Key(code,modifiers);
      dispose();
   }

   public void keyReleased(KeyEvent e) {
   }

}

