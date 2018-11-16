/*
 * PanelOptions.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * An abstract panel superclass for editing options.
 */

public abstract class PanelOptions extends JPanel {

   /**
    * Get the options from the UI (into an existing object).
    */
   public abstract void get(Options opt) throws ValidationException;

   /**
    * Put the options into the UI.
    */
   public abstract void put(Options opt);

}

