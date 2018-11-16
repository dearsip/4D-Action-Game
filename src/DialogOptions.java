/*
 * DialogOptions.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A big tabbed dialog for editing all the options.
 */

public class DialogOptions extends JDialog {

// --- fields ---

   private JTabbedPane tabs;

   private PanelMap panelMap;
   private PanelColor panelColor;
   private PanelColorEnable panelColorEnable;
   private PanelSeed panelSeed;
   private PanelView panelView;
   private PanelFisheye panelFisheye;

   private Options optDefault;

   // these change with every invocation
   private int dim;
   private boolean hasCurrentColor;
   private boolean hasCurrentView;
   private OptionsAll oaResult; // null if canceled or closed

// --- construction ---

   public DialogOptions(Frame owner, Options optDefault) {
      super(owner,App.getString("DialogOptions.s1"),true);

      this.optDefault = optDefault;

      setResizable(false);

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener( new WindowAdapter() { public void windowClosing(WindowEvent e) { doCancel(); } } );

   // create tabs

      tabs = new JTabbedPane();

      addTab(panelMap = new PanelMap(),"s2");
      addTab(panelColor = new PanelColor(),"s3");
      addTab(panelColorEnable = new PanelColorEnable(),"s4");
      addTab(panelSeed = new PanelSeed(),"s6");
      addTab(panelView = new PanelView(),"s7");
      addTab(new PanelStereo(),"s8");
      addTab(panelFisheye = new PanelFisheye(),"s21");
      addTab(new PanelKeysMove(),"s9");
      addTab(new PanelKeysTurn(),"s10");
      addTab(new PanelKeysSlide(),"s11");
      addTab(new PanelKeysSpin(),"s12");
      addTab(new PanelKeysShow(),"s13");
      addTab(new PanelKeysParam(),"s14");
      addTab(new PanelMotion(),"s15");
      addTab(new PanelImage(),"s20");

   // create buttons

      JPanel panel = new JPanel();
      JButton button;

      panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      button = new JButton(App.getString("DialogOptions.s16"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doReset(); } } );
      panel.add(button);

      panel.add(Box.createHorizontalGlue());

      button = new JButton(App.getString("DialogOptions.s17"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doOK(); } } );
      panel.add(button);

      panel.add(Box.createHorizontalStrut(5));

      button = new JButton(App.getString("DialogOptions.s18"));
      button.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doCancel(); } } );
      panel.add(button);

      panel.add(Box.createHorizontalGlue());

   // add to content pane

      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());

      contentPane.add(tabs,BorderLayout.CENTER);
      contentPane.add(panel,BorderLayout.SOUTH);

   // finish up

      pack();
      setLocationRelativeTo(owner);
   }

// --- helpers ---

   private void addTab(JPanel panel, String key) {
      panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
      tabs.addTab(App.getString("DialogOptions." + key),panel);
   }

// --- commands ---

   private void doReset() {

      Object[] options = new Object[] { App.getString("DialogOptionsReset.s3"),   // yes = this tab
                                        App.getString("DialogOptionsReset.s4"),   // no  = all tabs
                                        App.getString("DialogOptionsReset.s5") }; // cancel

      int result = JOptionPane.showOptionDialog(this,
                                                App.getString("DialogOptionsReset.s2"),
                                                App.getString("DialogOptionsReset.s1"),
                                                JOptionPane.YES_NO_CANCEL_OPTION,
                                                JOptionPane.QUESTION_MESSAGE,
                                                null,
                                                options,
                                                options[0]);

      switch (result) {
      case JOptionPane.YES_OPTION:
         put(optDefault,tabs.getSelectedIndex(),true);
         break;
      case JOptionPane.NO_OPTION:
         put(optDefault,true);
         break;
      case JOptionPane.CANCEL_OPTION:
      case JOptionPane.CLOSED_OPTION:
         // do nothing
         break;
      }
   }

   private void doOK() {

      // we have to get the field data now, for two reasons.
      // one, we need to check that the data is valid before closing the window
      // two, the field contents might be destroyed along with the window

      try {
         oaResult = getResultInternal();
         // on failure, oaResult will remain null
      } catch (ValidationException e) {
         JOptionPane.showMessageDialog(this,e.getMessage(),App.getString("DialogOptions.s19"),JOptionPane.ERROR_MESSAGE);
         return;
      }
      setVisible(false);
      KeyField.owner = null; // release reference
   }

   private void doCancel() {
      setVisible(false);
      KeyField.owner = null; // release reference
   }

// --- external interface ---

   public void prepare(int dim, OptionsAll oa) {

      this.dim = dim;
      hasCurrentColor = (oa.ocCurrent != null);
      hasCurrentView  = (oa.ovCurrent != null); // should equal hasCurrentColor, but don't assume
      // oaResult starts out null

      put(oa);

      KeyField.owner = this;
   }

   /**
    * Get the result options.
    * @return The options, or null if the dialog was canceled or closed.
    */
   public OptionsAll getResult() {
      OptionsAll oa = oaResult;
      oaResult = null; // release reference, since dialog continues to exist
      return oa;
   }

   private OptionsAll getResultInternal() throws ValidationException {

      OptionsAll oa = new OptionsAll();

      oa.opt = new Options();
      // omCurrent is read-only
      if (hasCurrentColor) oa.ocCurrent = new OptionsColor();
      if (hasCurrentView ) oa.ovCurrent = new OptionsView();
      oa.oeCurrent = new OptionsSeed();
      oa.oeNext = new OptionsSeed();

      get(oa);

      OptionsFisheye ofTemp = new OptionsFisheye();
      panelFisheye.get(ofTemp);

      // validation is built into the panel get functions,
      // except for option sets that are spread across multiple tabs.
      // for those we have to wait until all the data is available.

      oa.opt.oc3.validate();
      oa.opt.oc4.validate();
      if (hasCurrentColor) oa.ocCurrent.validate();
      // no hasCurrentView, view is a single tab

      oa.opt.ok3.validate();
      oa.opt.ok4.validate();
      oa.opt.okc.validate();

      oa.opt.validate();

      // now we know we're going to set the config,
      // so just poke the fisheye ones directly
      OptionsFisheye.copy(OptionsFisheye.of,ofTemp);
      OptionsFisheye.recalculate();

      return oa;
   }

// --- transfer functions ---

   private void get(Options opt, int i) throws ValidationException {
      Component panel = tabs.getComponentAt(i);
      ((PanelOptions) panel).get(opt);
   }

   private void get(Options opt) throws ValidationException {
      for (int i=0; i<tabs.getTabCount(); i++) {
         get(opt,i);
      }
   }

   private void get(OptionsAll oa) throws ValidationException {
      get(oa.opt);

      // omCurrent is read-only
      panelColor.getCurrent(oa.ocCurrent);
      panelColorEnable.getCurrent(oa.ocCurrent);
      panelSeed.getCurrent(oa.oeCurrent);
      panelSeed.getNext(oa.oeNext);
      panelView.getCurrent(oa.ovCurrent);
   }

   private void put(Options opt, int i, boolean reset) {

      Component panel = tabs.getComponentAt(i);
      if (panel == panelFisheye) {
         panelFisheye.put(reset ? OptionsFisheye.ofDefault : OptionsFisheye.of); // ignore opt
      } else {
         ((PanelOptions) panel).put(opt);
      }

      if (reset) {
         // special handling for resetting current color options
              if (panel == panelColor       && hasCurrentColor) panelColor      .putCurrent( (dim == 3) ? opt.oc3 : opt.oc4 );
         else if (panel == panelColorEnable && hasCurrentColor) panelColorEnable.putCurrent( (dim == 3) ? opt.oc3 : opt.oc4 );

         // ditto
              if (panel == panelView        && hasCurrentView ) panelView       .putCurrent( (dim == 3) ? opt.ov3 : opt.ov4 );
      }
   }

   private void put(Options opt, boolean reset) {
      for (int i=0; i<tabs.getTabCount(); i++) {
         put(opt,i,reset);
      }
   }

   private void put(OptionsAll oa) {
      put(oa.opt,false);

      panelMap.putCurrent(oa.omCurrent);
      panelColor.putCurrent(oa.ocCurrent);
      panelColorEnable.putCurrent(oa.ocCurrent);
      panelSeed.putCurrent(oa.oeCurrent);
      panelSeed.putNext(oa.oeNext);
      panelView.putCurrent(oa.ovCurrent);
   }

}

