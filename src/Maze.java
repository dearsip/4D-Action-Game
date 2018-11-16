/*
 * Maze.java
 */

import java.io.File;
import java.util.Arrays;
import javax.swing.filechooser.FileFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The entry point and main frame window for the maze application.
 */

public class Maze extends JFrame implements SquareLayout.Observer, IDisplay, IMenu, IStorable {

// --- main ---

   public static void main(String[] args) {

      if (args.length != 0) {
         System.out.println(App.getString("Maze.s13"));
         return;
      }

      Maze maze = new Maze();                    // starts event dispatching
      if ( ! maze.doInit() ) { System.exit(1); } // must exit to stop event dispatching
      if ( ! fileCurrent.exists() ) maze.doWelcomeStartup();
      maze.construct();
      maze.setVisible(true);
   }

// --- fields ---

   private SquareLayout layout;
   private LineBuffer[] lineBuffer;
   private PanelLine[] panelLine;
   private int active;

   private Options optDefault;
   private Options opt; // the next three are used only during load
   private int dim;
   private Rectangle bounds;
   private File gameDirectory;
   private File imageDirectory;

   private DialogOptions dialogOptions;
   private Core core;

   private File reloadFile;

// --- construction ---

   /**
    * Construct a new main frame window without making it visible.
    */
   public Maze() {
      super(App.getString("Maze.s1"));

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener( new WindowAdapter() { public void windowClosing(WindowEvent e) { doExit(); } } );

      setJMenuBar(constructMenuBar());

      Container contentPane = getContentPane();
      contentPane.setLayout(layout = new SquareLayout(this));
      contentPane.setBackground(Color.black);

   // field initialization

      lineBuffer = new LineBuffer[2];
      // null until core callback

      panelLine = new PanelLine[2];
      for (int i=0; i<2; i++) {
         panelLine[i] = new PanelLine();
         contentPane.add(panelLine[i]);
      }

      // active is zero until core callback

      optDefault = new Options();
      opt = new Options();
      bounds = new Rectangle();

      // dialogOptions is null until used
      // rest is handled in construct, after properties loaded
   }

   private void construct() {

      core = new Core(opt,this,this);
      addKeyListener(core);
      addFocusListener(core);

      core.newGame(dim);

      setBounds(bounds); // do not pack, size is not controlled by components

      opt = null; // done with these (and with dim)
      bounds = null;
   }

// --- implementation of SquareLayout.Observer ---

   public void update(int edge) {
      core.setEdge(edge);
   }

// --- implementation of IDisplay ---

   public void setMode3D(LineBuffer buf) {
      lineBuffer[0] = buf;
      lineBuffer[1] = null;
      panelLine[0].setVisible(true);
      panelLine[1].setVisible(false);
      active = 1;
   }

   public void setMode4DMono(LineBuffer buf) {
      lineBuffer[0] = buf;
      lineBuffer[1] = null;
      panelLine[0].setVisible(true);
      panelLine[1].setVisible(false);
      active = 1;
   }

   public void setMode4DStereo(LineBuffer buf1, LineBuffer buf2) {
      lineBuffer[0] = buf1;
      lineBuffer[1] = buf2;
      panelLine[0].setVisible(true);
      panelLine[1].setVisible(true);
      active = 2;
   }

   public void nextFrame() {
      for (int i=0; i<active; i++) {
         panelLine[i].setLines(lineBuffer[i]);
      }
   }

// --- implementation of IStorable ---

   private static String nameDefault =          "default.properties";
   private static File   fileCurrent = new File("current.properties");

   private static final String KEY_OPTIONS = "opt";
   private static final String KEY_DIM     = "dim";
   private static final String KEY_BOUNDS  = "bounds";
   private static final String KEY_GAME_DIRECTORY  = "dir.game";
   private static final String KEY_IMAGE_DIRECTORY = "dir.image";
   private static final String KEY_VERSION = "version";
   private static final String KEY_FISHEYE = "opt.of"; // not part of opt (yet)

   // here we don't have to be careful about modifying an existing object,
   // because if any of the load process fails, the program will exit

   public void loadDefault(IStore store) throws ValidationException {
      store.getObject(KEY_OPTIONS,optDefault);

      if (fileCurrent.exists()) return;

      store.getObject(KEY_OPTIONS,opt);
      dim = 4;
      bounds = getCenteredRectangle(500,300); // nice reasonable size
      gameDirectory  = null;
      imageDirectory = null;
   }

   private static File toFile(String s) {
      return (s.equals("")) ? null : new File(s);
   }

   private static String toString(File file) {
      return (file == null) ? "" : file.getPath();
   }

   public void load(IStore store) throws ValidationException {

      store.getObject(KEY_OPTIONS,opt);
      dim = store.getInteger(KEY_DIM);
      if ( ! (dim == 3 || dim == 4) ) throw App.getException("Maze.e1");
      store.getObject(KEY_BOUNDS,bounds);
      gameDirectory  = toFile(store.getString(KEY_GAME_DIRECTORY ));
      imageDirectory = toFile(store.getString(KEY_IMAGE_DIRECTORY));

      Integer temp = store.getNullableInteger(KEY_VERSION);
      int version = (temp == null) ? 1 : temp.intValue();

      if (version >= 2) {
         store.getObject(KEY_FISHEYE,OptionsFisheye.of);
         OptionsFisheye.recalculate();
      }
   }

   public void save(IStore store) throws ValidationException {

      store.putObject(KEY_OPTIONS,core.getOptions());
      store.putInteger(KEY_DIM,core.getDim());
      store.putObject(KEY_BOUNDS,getBounds());
      store.putString(KEY_GAME_DIRECTORY, toString(gameDirectory ));
      store.putString(KEY_IMAGE_DIRECTORY,toString(imageDirectory));

      store.putInteger(KEY_VERSION,2);

      // version 2
      store.putObject(KEY_FISHEYE,OptionsFisheye.of);
   }

// --- image files ---

   private static final String suffix = ".eps";

   private static boolean hasSuffix(File f) {
      return f.isDirectory() || f.getName().endsWith(suffix);
   }

   private static File forceSuffix(File f) {
      return hasSuffix(f) ? f : new File(f.getParentFile(),f.getName() + suffix);
   }

   private static class ImageFileFilter extends FileFilter {
      public boolean accept(File f) {
         return hasSuffix(f);
      }
      public String getDescription() {
         return App.getString("Maze.s20");
      }
   }

// --- menu commands ---

   private void doWelcomeStartup() {
      DialogWelcome dialog = new DialogWelcome(this);

      dialog.put(opt.ok4,opt.os,opt.oi);
      dialog.setVisible(true);

      opt.os = dialog.os;
      opt.oi = dialog.oi;
   }

   private void doWelcome() {
      DialogWelcome dialog = new DialogWelcome(this);

      Options opt = core.getOptions();
      dialog.put(opt.ok4,opt.os,opt.oi);
      dialog.setVisible(true);

      core.setOptions(dialog.os,dialog.oi);
   }

   private void doAbout() {
      DialogAbout dialog = new DialogAbout(this);
      dialog.setVisible(true);
   }

   public void doNew() {
      core.newGame(0);
   }

   private void doNew3D() {
      core.newGame(3);
   }

   private void doNew4D() {
      core.newGame(4);
   }

   private File getGameDirectory() {
      return (gameDirectory  != null) ? gameDirectory  : imageDirectory;
   }

   private File getImageDirectory() {
      return (imageDirectory != null) ? imageDirectory : gameDirectory;
   }

   private boolean confirmOverwrite(File file, String title) {
      String message = App.getString("Maze.s21",new Object[] { file.getName() });
      int result = JOptionPane.showConfirmDialog(this,message,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
      return (result == JOptionPane.OK_OPTION);
   }

   private void doLoad() {

      JFileChooser chooser = new JFileChooser(getGameDirectory());
      chooser.setDialogTitle(App.getString("Maze.s14"));
      int result = chooser.showOpenDialog(this);
      if (result != JFileChooser.APPROVE_OPTION) return;

      gameDirectory = chooser.getCurrentDirectory();
      File file = chooser.getSelectedFile();

      try {
         if (PropertyFile.test(file)) {
            PropertyFile.load(file,core);
         } else {
            reloadFile = file; // remember it before loading, so if there are errors you can still retry with reload
            doLoadGeom(file);
            // this handles exceptions internally, but no harm in having an extra layer here
         }
      } catch (ValidationException e) {
         JOptionPane.showMessageDialog(this,e.getMessage(),App.getString("Maze.s15"),JOptionPane.ERROR_MESSAGE);
      }
   }

   private static class NonDirectoryFileFilter implements java.io.FileFilter { // can't import, name collision
      public boolean accept(File f) {
         return ! f.isDirectory();
      }
   }

   private static int findName(String[] name, String goal) {
      for (int i=0; i<name.length; i++) {
         if (name[i].equals(goal)) return i;
      }
      return -1;
   }

   public void doReload(int delta) {
      if (reloadFile == null) return;

      if (delta != 0) {
         File[] f = reloadFile.getParentFile().listFiles(new NonDirectoryFileFilter());

         // results of listFiles have same parent directory so names are sufficient
         // (and probably faster for sorting)
         String[] name = new String[f.length];
         for (int i=0; i<f.length; i++) name[i] = f[i].getName();
         String[] sortedName = name.clone();
         Arrays.sort(sortedName);

         int i = findName(sortedName,reloadFile.getName());
         if (i != -1) {
            i += delta;
            if (i >= 0 && i < f.length) reloadFile = f[findName(name,sortedName[i])];
            else return; // we're at the end, don't do a reload
         }
         // else not found, fall through and report that error
      }

      doLoadGeom(reloadFile);
   }

   private void doLoadGeom(File file) {
      try {
         core.loadGeom(file);
      } catch (Throwable t) {
         String s = "";
         if (t instanceof LanguageException) {
            LanguageException e = (LanguageException) t;
            t = e.getCause();
            s = e.getFile() + "\n" + e.getDetail() + "\n";
         }
         t.printStackTrace();
         JOptionPane.showMessageDialog(this,s + t.getClass().getName() + "\n" + t.getMessage(),App.getString("Maze.s25"),JOptionPane.ERROR_MESSAGE);
      }
   }

   private void doSaveGeom(File file) {
      try {
         core.saveGeom(file);
      } catch (Throwable t) {
         // no language exceptions here, but I want to have good reporting
         // so I can fix things if someone finds a state that doesn't work
         t.printStackTrace();
         JOptionPane.showMessageDialog(this,t.getClass().getName() + "\n" + t.getMessage(),App.getString("Maze.s28"),JOptionPane.ERROR_MESSAGE);
      }
   }

   public int doSelectShape(ISelectShape iss) {
      int quantity = new DialogSelectShape(this,iss).run();
      if (quantity != -1) core.forceClockStart();
      // otherwise there's no tick to continue the command.
      // this is too convoluted but let's just get it done.
      return quantity;
   }

   public boolean doSelectPaint(ISelectShape iss) {
      boolean b = new DialogSelectPaint(this,iss).run();
      if (b) core.forceClockStart(); // comment above
      return b;
   }

   private void doSave() {

      int saveType = core.getSaveType();
      if (saveType == IModel.SAVE_NONE) { // train model
         JOptionPane.showMessageDialog(this,App.getString("Maze.e2"),App.getString("Maze.s26"),JOptionPane.ERROR_MESSAGE);
         return;
      }

      JFileChooser chooser = new JFileChooser(getGameDirectory());
      String title = App.getString("Maze.s16");
      chooser.setDialogTitle(title);
      int result = chooser.showSaveDialog(this);
      if (result != JFileChooser.APPROVE_OPTION) return;

      gameDirectory = chooser.getCurrentDirectory();
      File file = chooser.getSelectedFile();
      if (file.exists() && ! confirmOverwrite(file,title)) return;

      try {
         if (saveType == IModel.SAVE_MAZE) {
            PropertyFile.save(file,core);
         } else {
            doSaveGeom(file);
            // this handles exceptions internally, but no harm in having an extra layer here
         }
      } catch (Exception e) {
         JOptionPane.showMessageDialog(this,e.getMessage(),App.getString("Maze.s17"),JOptionPane.ERROR_MESSAGE);
      }
   }

   private void doSaveImage() {

      JFileChooser chooser = new JFileChooser(getImageDirectory());
      String title = App.getString("Maze.s18");
      chooser.setDialogTitle(title);
      chooser.setFileFilter(new ImageFileFilter());
      int result = chooser.showSaveDialog(this);
      if (result != JFileChooser.APPROVE_OPTION) return;

      imageDirectory = chooser.getCurrentDirectory();
      File file = forceSuffix(chooser.getSelectedFile());
      if (file.exists() && ! confirmOverwrite(file,title)) return;

      try {
         PostScriptFile.save(file,active,lineBuffer,
                             layout.getEdge(),layout.getGap(),core.os().screenWidth,core.oi());
      } catch (ValidationException e) {
         JOptionPane.showMessageDialog(this,e.getMessage(),App.getString("Maze.s19"),JOptionPane.ERROR_MESSAGE);
      }
   }

   private void doResetWin() {
      core.resetWin();
   }

   private void doRestart() {
      core.restartGame();
   }

   public void doOptions() {
      if (dialogOptions == null) {
         dialogOptions = new DialogOptions(this,optDefault);
      }
      dialogOptions.prepare(core.getDim(),core.getOptionsAll());
      dialogOptions.setVisible(true);

      OptionsAll oaResult = dialogOptions.getResult();
      if (oaResult != null) core.setOptionsAll(oaResult);
   }

   private boolean doInit() {
      try {
         PropertyResource.load(nameDefault,new IStorable() {
               public void load(IStore store) throws ValidationException { loadDefault(store); }
               public void save(IStore store) throws ValidationException {}
            });
         if (fileCurrent.exists()) PropertyFile.load(fileCurrent,this);
      } catch (ValidationException e) {
         System.out.println(e.getMessage());
         return false;
      }
      return true;
   }

   public void doExit() {
      try {
         PropertyFile.save(fileCurrent,this);
      } catch (ValidationException e) {
         System.out.println(e.getMessage());
      }
      System.exit(0);
   }

// --- UI helpers ---

   private JMenuBar constructMenuBar() {

      JMenuBar menuBar = new JMenuBar();
      JMenu menu;
      JMenuItem item;

      menu = new JMenu(App.getString("Maze.s2"));
      menuBar.add(menu);

      item = new JMenuItem(App.getString("Maze.s22"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doWelcome(); } } );
      menu.add(item);

      item = new JMenuItem(App.getString("Maze.s10"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doAbout(); } } );
      menu.add(item);

      menu.addSeparator();

      item = new JMenuItem(App.getString("Maze.s3"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doNew3D(); } } );
      menu.add(item);

      item = new JMenuItem(App.getString("Maze.s4"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doNew4D(); } } );
      menu.add(item);

      item = new JMenuItem(App.getString("Maze.s5"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doLoad(); } } );
      menu.add(item);

      item = new JMenuItem(App.getString("Maze.s27"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doReload(0); } } );
      menu.add(item);

      item = new JMenuItem(App.getString("Maze.s6"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doSave(); } } );
      menu.add(item);

      item = new JMenuItem(App.getString("Maze.s11"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doSaveImage(); } } );
      menu.add(item);

      menu.addSeparator();

      item = new JMenuItem(App.getString("Maze.s7"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doResetWin(); } } );
      menu.add(item);

      item = new JMenuItem(App.getString("Maze.s8"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doRestart(); } } );
      menu.add(item);

      menu.addSeparator();

      item = new JMenuItem(App.getString("Maze.s9"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doOptions(); } } );
      menu.add(item);

      menu.addSeparator();

      item = new JMenuItem(App.getString("Maze.s12"));
      item.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { doExit(); } } );
      menu.add(item);

      return menuBar;
   }

   private static Rectangle getCenteredRectangle(int width, int height) {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      if (width  > screenSize.width ) width  = screenSize.width;
      if (height > screenSize.height) height = screenSize.height;

      int x = (screenSize.width  - width ) / 2;
      int y = (screenSize.height - height) / 2;

      return new Rectangle(x,y,width,height);
   }

}

