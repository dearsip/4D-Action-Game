/*
 * Key.java
 */

import java.awt.event.KeyEvent;

/**
 * An object that represents a key that can be pressed.
 * The class is similar to javax.swing.KeyStroke for a key press,
 * but it avoids the possibility of having other kinds of events,
 * and also works with PropertyStore persistence.
 */

public class Key implements IValidate {

// --- fields ---

   public int code;
   public int modifiers;

// --- construction ---

   public Key() {
      this(0,0);
   }

   public Key(int code) {
      this(code,0);
   }

   public Key(int code, int modifiers) {
      this.code = code;
      this.modifiers = modifiers;
   }

// --- methods ---

   public boolean isDefined() {
      return ! (code == 0 && modifiers == 0);
   }

   public boolean equals(Key key) {
      return (    code == key.code
               && modifiers == key.modifiers );
   }

   public String toString() {
      String s = "";
      if (isDefined()) {
         s  = KeyEvent.getKeyModifiersText(modifiers);
         if (s.length() > 0) s += " ";
         s += KeyEvent.getKeyText(code);
      }
      return s;
   }

// --- implementation of IValidate ---

   public void validate() throws ValidationException {
      // this first line isn't necessary right now
      // if ( ! isDefined() ) return;
      if (modifiers != restrictToAllowedModifiers(modifiers)) throw App.getException("Key.e1");
   }

// --- static helpers ---

   public static boolean isModifier(int code) {

      // I thought there was some predefined isModifier function, but I can't find it.
      // so, these are all the key-based modifiers defined in java.awt.event.InputEvent,
      // plus all the lock keys, just because.

      return (    code == KeyEvent.VK_SHIFT
               || code == KeyEvent.VK_CONTROL
               || code == KeyEvent.VK_META
               || code == KeyEvent.VK_ALT

               || code == KeyEvent.VK_ALT_GRAPH

               || code == KeyEvent.VK_CAPS_LOCK
               || code == KeyEvent.VK_NUM_LOCK
               || code == KeyEvent.VK_SCROLL_LOCK );
   }

   public static int[] getAllowedModifiers() {
      return new int[] { KeyEvent.SHIFT_MASK,
                         KeyEvent.CTRL_MASK,
                         KeyEvent.META_MASK,
                         KeyEvent.ALT_MASK    };
   }

   public static int restrictToAllowedModifiers(int modifiers) {
      final int mask =   KeyEvent.SHIFT_MASK
                       | KeyEvent.CTRL_MASK
                       | KeyEvent.META_MASK
                       | KeyEvent.ALT_MASK;
      return modifiers & mask;
   }

   public static int translateAllowedModifier(int code) {
      int modifier = 0;
      switch (code) {
      case KeyEvent.VK_SHIFT:   modifier = KeyEvent.SHIFT_MASK; break;
      case KeyEvent.VK_CONTROL: modifier = KeyEvent.CTRL_MASK;  break;
      case KeyEvent.VK_META:    modifier = KeyEvent.META_MASK;  break;
      case KeyEvent.VK_ALT:     modifier = KeyEvent.ALT_MASK;   break;
      }
      return modifier;
   }

}

