/*
 * DialogSelectShape.java
 */

import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A dialog for selecting what shape(s) to add.
 */

public class DialogSelectShape extends JDialog {

// --- fields ---

   private ISelectShape iss;

   private JTextField quantity;
   private JComboBox color;
   private JComboBox shape;

   private int result;

// --- construction ---

   public DialogSelectShape(Frame owner, ISelectShape iss) {
      super(owner,s("s1"),true);

      this.iss = iss;

      setResizable(false);

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener( new WindowAdapter() { public void windowClosing(WindowEvent e) { doCancel(); } } );

   // create fields

      quantity = new JTextField(5);
      color = new JComboBox();
      shape = new JComboBox();

      color.setEditable(true);

      load(color,s("s5"),iss.getAvailableColors());
      load(shape,s("s6"),iss.getAvailableShapes());

      color.insertItemAt(new NamedObject(s("s10"),ISelectShape.RANDOM_COLOR),1);

   // create main panel

      JPanel panelMain = new JPanel();
      panelMain.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      GridBagHelper helper = new GridBagHelper(panelMain);
      int y = 0;

      helper.add(0,y,Box.createHorizontalStrut(20));
      helper.add(3,y,Box.createHorizontalStrut(20));

      helper.add(1,y,lp("s2"));
      helper.add(2,y,quantity);
      y++;

      helper.add(1,y,lp("s3"));
      helper.addFill(2,y,color);
      y++;

      helper.add(1,y,lp("s4"));
      helper.addFill(2,y,shape);
      y++;

   // create buttons

      JPanel panelButton = new JPanel();
      JButton button;

      button = new JButton(s("s7"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doOK(); } } );
      panelButton.add(button);
      getRootPane().setDefaultButton(button);

      button = new JButton(s("s8"));
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
      return App.getString("DialogSelectShape." + key);
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

// --- combo helpers ---

   public static void load(JComboBox combo, String nullName, Vector items) {
      combo.addItem(new NamedObject(nullName,null));
      Iterator i = items.iterator();
      while (i.hasNext()) combo.addItem(i.next());
   }

   public static int find(JComboBox combo, Object object) {
      for (int i=0; i<combo.getItemCount(); i++) {
         NamedObject nobj = (NamedObject) combo.getItemAt(i);
         if (nobj.object == object) return i;
      }
      return -1;
   }

// --- commands ---

   private void doOK() {
      try {
         result = get();
      } catch (ValidationException e) {
         JOptionPane.showMessageDialog(this,e.getMessage(),App.getString("DialogSelectShape.s9"),JOptionPane.ERROR_MESSAGE);
         return;
      }
      dispose();
   }

   private void doCancel() {
      result = -1;
      dispose();
   }

   public int run() {
      put();
      setVisible(true);
      return result;
   }

// --- transfer functions ---

   private int get() throws ValidationException {
      Object o;
      Color c;
      Geom.Shape s;

      int q = Field.getInteger(quantity);
      if (q <= 0) throw App.getException("DialogSelectShape.e1");

      o = color.getSelectedItem();
      if (o instanceof String) {
         c = Field.getColor((String) o);
      } else {
         c = (Color) ((NamedObject) o).object;
      }

      o = shape.getSelectedItem();
      s = (Geom.Shape) ((NamedObject) o).object;

      // now that we know everything will be successful
      // we can update the values through the interface
      iss.setSelectedColor(c);
      iss.setSelectedShape(s);

      return q;
   }

   public void put() {
      int i;

      Field.putInteger(quantity,1);

      Color c = iss.getSelectedColor();
      i = find(color,c);
      if (i != -1) {
         color.setSelectedIndex(i);
      } else {
         color.setSelectedItem(Field.putColor(c));
      }

      Geom.Shape s = iss.getSelectedShape();
      i = find(shape,s);
      if (i != -1) {
         shape.setSelectedIndex(i);
      }
      // else nothing we can do, don't select
   }

}

