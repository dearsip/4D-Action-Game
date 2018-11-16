/*
 * KeyField.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A custom field for entering key codes and modifiers.
 */

public class KeyField extends JTextField {

// --- fields ---

   public static Dialog owner;

   private Key key;

// --- construction ---

   public KeyField(int columns) {
      this(columns,true);
   }

   public KeyField(int columns, boolean enabled) {
      super(columns);
      setEnabled(false);

      if (enabled) addMouseListener( new MouseAdapter() { public void mouseClicked(MouseEvent e) { doClick(); } } );
   }

// --- key dialog ---

   private void doClick() {
      Key result = new DialogKey(owner).run();
      if (result != null) {
         put(result);
      }
   }

// --- transfer functions ---

   public void get(Key key) {
      key.code      = this.key.code;
      key.modifiers = this.key.modifiers;
   }

   public void put(Key key) {
      this.key = key;
      Field.putString(this,key.toString());
   }

}

