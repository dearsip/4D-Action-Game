/*
 * DialogSelectPaint.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A dialog for selecting the paint color, etc.
 */

public class DialogSelectPaint extends JDialog {

// --- fields ---

   private ISelectShape iss;

   private JComboBox color;
   private JRadioButton thisShape;
   private JRadioButton thisFace;
   private JRadioButton nextShape;
   private JRadioButton nextFace;

   private boolean result;

// --- construction ---

   public DialogSelectPaint(Frame owner, ISelectShape iss) {
      super(owner,s("s1"),true);

      this.iss = iss;

      setResizable(false);

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener( new WindowAdapter() { public void windowClosing(WindowEvent e) { doCancel(); } } );

   // create fields

      color = new JComboBox();
      color.setEditable(true);

      DialogSelectShape.load(color,s("s7"),iss.getAvailableColors());
      color.insertItemAt(new NamedObject(s("s8"),ISelectShape.RANDOM_COLOR),1);
      color.insertItemAt(new NamedObject(s("s9"),ISelectShape.REMOVE_COLOR),1);

      String s5 = s("s5");
      String s6 = s("s6");

      thisShape = new JRadioButton(s5);
      thisFace  = new JRadioButton(s6);
      nextShape = new JRadioButton(s5);
      nextFace  = new JRadioButton(s6);

      ButtonGroup thisGroup = new ButtonGroup();
      thisGroup.add(thisShape);
      thisGroup.add(thisFace);

      ButtonGroup nextGroup = new ButtonGroup();
      nextGroup.add(nextShape);
      nextGroup.add(nextFace);

   // create main panel

      JPanel panelMain = new JPanel();
      panelMain.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      GridBagHelper helper = new GridBagHelper(panelMain);
      int y = 0;

      helper.add(0,y,Box.createHorizontalStrut(20));
      helper.add(4,y,Box.createHorizontalStrut(20));

      helper.add(1,y,lp("s2"));
      helper.addSpan(2,y,2,color);
      y++;

      helper.add(1,y,Box.createVerticalStrut(5));
      y++;

      helper.add(1,y,lp("s3"));
      helper.add(2,y,thisShape);
      helper.add(3,y,thisFace);
      y++;

      helper.add(1,y,lp("s4"));
      helper.add(2,y,nextShape);
      helper.add(3,y,nextFace);
      y++;

   // create buttons

      JPanel panelButton = new JPanel();
      JButton button;

      button = new JButton(s("s10"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doOK(); } } );
      panelButton.add(button);
      getRootPane().setDefaultButton(button);

      button = new JButton(s("s11"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doCancel(); } } );
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
      return App.getString("DialogSelectPaint." + key);
   }

   private static String p(String key) {
      return s(key) + " "; // pad
   }

   private static JLabel l(String text) {
      return new JLabel(text);
   }

   private static JLabel lp(String key) {
      return l(p(key));
   }

// --- commands ---

   private void doOK() {
      try {
         get();
         result = true;
      } catch (ValidationException e) {
         JOptionPane.showMessageDialog(this,e.getMessage(),App.getString("DialogSelectPaint.s12"),JOptionPane.ERROR_MESSAGE);
         return;
      }
      dispose();
   }

   private void doCancel() {
      result = false;
      dispose();
   }

   public boolean run() {
      put();
      setVisible(true);
      return result;
   }

// --- transfer functions ---

   private void get() throws ValidationException {

      Object o = color.getSelectedItem();
      Color c;
      if (o instanceof String) {
         c = Field.getColor((String) o);
      } else {
         c = (Color) ((NamedObject) o).object;
      }

      int mode =   (thisShape.isSelected() ?  1 : 0)
                 + (nextShape.isSelected() ? -2 : 0);

      // now that we know everything will be successful
      // we can update the values through the interface
      iss.setPaintColor(c);
      iss.setPaintMode(mode);
   }

   public void put() {

      Color c = iss.getPaintColor();
      int i = DialogSelectShape.find(color,c);
      if (i != -1) {
         color.setSelectedIndex(i);
      } else {
         color.setSelectedItem(Field.putColor(c));
      }

      int mode = iss.getPaintMode();
      (((mode & 1) == 1) ? thisShape : thisFace).setSelected(true);
      (((mode & 2) == 2) ? nextShape : nextFace).setSelected(true);
   }

}

