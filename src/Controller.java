/*
 * Controller.java
 */

import java.awt.EventQueue;

/**
 * An object that watches a key buffer and controls an engine based on which keys are down.
 */

public class Controller implements IClock {

// --- fields ---

   private KeyBuffer buf;
   private IOptions options;
   private IMenu menu;
   private Engine engine;
   private IKeysNew keysNew;

   private Command[] command;
   private Parameter[] parameter;
   private Parameter[] textureParameter;

   private int[] param;
   private int nMove;
   private int nRotate;
   private int nAlignMove; // these two only used inside setOptions
   private int nAlignRotate;
   private double dMove;
   private double dRotate;
   private double dAlignMove;
   private double dAlignRotate;
   private boolean alwaysRun;

   private int dim;
   private boolean alignMode;
   private IMove target;
   private boolean engineAlignMode; // only valid when target is not engine

   private Command commandActive;
   private int nActive;
   private double dActive;
   private Align alignActive;

   private Command[] reg1; // temporary register for tick
   private Command commandChain;

   private double[] saveOrigin;
   private double[][] saveAxis;

// --- construction ---

   public Controller(KeyBuffer buf, IOptions options, IMenu menu, Engine engine) {
      this.buf = buf;
      this.options = options;
      this.menu = menu;
      this.engine = engine;

      constructCommands();
      constructParameters();

      param = new int[6];
      // options fields initialized via setOptions

      // dim and align mode can wait 'til reset

      // active command info starts out null

      reg1 = new Command[KeyBuffer.NID]; // won't use that many in real life
   }

// --- methods ---

   public void setOptions(OptionsKeysConfig okc, OptionsMotion ot) {

      for (int i=0; i<6; i++) {
         param[i] = okc.param[i];
      }

      // the frame rate and command times are all positive,
      // so the number of steps will always be at least 1 ...

      nMove        = (int) Math.ceil(ot.frameRate * ot.timeMove       );
      nRotate      = (int) Math.ceil(ot.frameRate * ot.timeRotate     );
      nAlignMove   = (int) Math.ceil(ot.frameRate * ot.timeAlignMove  );
      nAlignRotate = (int) Math.ceil(ot.frameRate * ot.timeAlignRotate);

      // ... therefore, the distances will never exceed 1,
      // and the angles will never exceed 90 degrees

      dMove        =  1 / (double) nMove;
      dRotate      = 90 / (double) nRotate;
      dAlignMove   =  1 / (double) nAlignMove;
      dAlignRotate = 90 / (double) nAlignRotate;
   }

   public void setKeysNew(IKeysNew keysNew) {
      this.keysNew = keysNew;
   }

   public void setAlwaysRun(boolean alwaysRun) {
      this.alwaysRun = alwaysRun;
   }

   public boolean getAlignMode() {
      return alignMode;
      // this is the value that gets saved in files, so it ought to be
      // the engine align mode.  but ... (1) engineAlignMode isn't the
      // desired value, since it's only set while a block is moving, and
      // (2) we can't even get here in geom mode, no save allowed there
   }

   /**
    * Immediately stop any operation in progress.<p>
    *
    * This is only called when the game state has been reset (new, load, restart),
    * so you don't need to worry about leaving the engine out of alignment.
    */
   public void reset(int dim, boolean alignMode) {
      this.dim = dim;
      this.alignMode = alignMode;
      target = engine;

      buf.clearPressed(); // in case there is a leftover press
      // the key mapper is responsible for the down flags

      commandActive = null;
      alignActive = null;

      saveOrigin = new double[dim];
      saveAxis = new double[dim][dim];
   }

// --- implementation of IClock ---

   public boolean tick() {

   // figure out which commands are being invoked

      int n = 0;
      for (int i=0; i<KeyBuffer.NID; i++) {
         Command com = command[i];

         if (com.isOnPress()) {
            if ( ! buf.pressed[i] ) continue;
         } else {
            if ( ! buf.down[i] ) continue;
         }

         if ( ! com.canRun() ) continue;

         if (com.isImmediate()) {
            com.run();
            continue; // don't add to list
         }
         // the point here is to prevent menu commands from causing re-render

         if (com.isExclusive()) {
            if (commandActive != null) continue; // already got one, maybe even already running

            commandActive = com;
            com.activate();
            continue; // don't add to list, will handle separately
         }

         reg1[n++] = com; // found a nonexclusive command
      }

      buf.clearPressed(); // ok, we've looked at that

      if (commandChain != null) {
         reg1[n++] = commandChain;
         commandChain = null;
         // so, commandChain must not be immediate or exclusive
      }
      // this is flimsy in several ways, but it will do for now

   // check idleness now, simplifies later code

      if (n == 0 && commandActive == null) {
         if (alwaysRun) {
            engine.renderAbsolute();
            return true;
         } else {
            return false; // idle
         }
      }

   // save state

      IMove saveTarget = target;
      target.save(saveOrigin,saveAxis);

   // run the commands

      for (int i=0; i<n; i++) {

         if (commandActive != null && reg1[i].isExcluded()) continue;
         // exclusion doesn't alter the idleness result above.
         // if commandActive is null, nothing is excluded,
         // and if it's not, commandActive itself keeps us from becoming idle

         reg1[i].run(); // ignore result, nothing we can do with it
      }

      // we don't need to set reg1 back to null,
      // all it does is point to shared objects that live in the command array

      if (commandActive != null) {
         if ( ! commandActive.run() ) commandActive = null;
      }

   // update state

      // the click command is exclusive, so if the target changed,
      // no update needed.
      if (      target == saveTarget
           && ! target.update(saveOrigin,saveAxis,engine.getOrigin()) ) { // bonk

         target.restore(saveOrigin,saveAxis);

         if (commandActive != null) {
            commandActive = null;
            alignActive = null; // not a big deal but let's do it
         }

         if (alignMode && ! target.isAligned()) {
            alignMode = false;
         }
      }

   // finish up

      engine.renderAbsolute();
      // if we didn't do anything at all, we would have exited above,
      // so we know that we need to re-render at some level.
      // it's not worth figuring out exactly what level, this is fast enough

      return true;
   }

// --- commands ---

   // a command is exclusive if it requires exclusive control of the user's position
   //
   // only exclusive commands can become active and request more time;
   // non-exclusive commands must always complete in a single step

   // a command is excluded if it can't run at the same time as an exclusive command.
   //
   // exclusive commands are excluded by definition; for such commands,
   // isExcluded is not called, but should return true for consistency.

   private abstract class Command {
      public          boolean isOnPress() { return false; }
      public          boolean canRun() { return true; }
      public          boolean isImmediate() { return false; }
      public abstract boolean isExclusive();
      public          boolean isExcluded() { return true; }
      public          void    activate() {};
      public abstract boolean run(); // like IClock, true if want more time
   }

   private class CommandMove extends Command {
      private int a;
      private int sign;
      public CommandMove(int a, int sign) { this.a = a; this.sign = sign; }

      public boolean canRun() {
         int a = (this.a == -1) ? dim-1 : this.a;
         if (alignMode) {
            return target.canMove(a,sign);
         } else {
            return true;
         }
      }
      public boolean isExclusive() {
         return alignMode;
      }
      public void activate() {
         // only called in align mode
         nActive = nMove;
         dActive = dMove;
      }
      public boolean run() {
         int a = (this.a == -1) ? dim-1 : this.a;
         if (alignMode) {
            target.move(a,sign*dActive);
            if (--nActive > 0) {
               return true;
            } else {
               target.align().snap();
               return false;
            }
         } else {
            target.move(a,sign*dMove);
            return false;
         }
      }
   }

   private class CommandRotate extends Command {
      private int a1;
      private int a2;
      private int sign;
      public CommandRotate(int a1, int a2, int sign) { this.a1 = a1; this.a2 = a2; this.sign = sign; }

      public boolean isExclusive() {
         return alignMode;
      }
      public void activate() {
         // only called in align mode
         nActive = nRotate;
         dActive = dRotate;
      }
      public boolean run() {
         int a1 = (this.a1 == -1) ? dim-1 : this.a1;
         int a2 = (this.a2 == -1) ? dim-1 : this.a2;
         if (alignMode) {
            target.rotateAngle(a1,a2,sign*dActive);
            if (--nActive > 0) {
               return true;
            } else {
               target.align().snap();
               return false;
            }
         } else {
            target.rotateAngle(a1,a2,sign*dRotate);
            return false;
         }
      }
   }

   private class CommandAlign extends Command {
      public boolean isOnPress() {
         return true;
      }
      public boolean canRun() {
         return ! alignMode && engine.getSaveType() != IModel.SAVE_ACTION;
      }
      public boolean isExclusive() {
         return true;
         // when it can run, it is always exclusive
         // (the functions are called in order)
      }
      public void activate() {
         // we don't need to cache the distances,
         // the alignment will turn out correctly even if they change
         alignActive = target.align();
      }
      public boolean run() {
         if ( ! alignActive.align(dAlignMove,dAlignRotate) ) {
            return true;
         } else {
            alignActive = null; // done, release reference
            return false;
         }
      }
   }

   private class CommandChangeAlignMode extends Command {
      public boolean isOnPress() {
         return true;
      }
      public boolean canRun() {
         return engine.getSaveType() != IModel.SAVE_ACTION;
      }
      public boolean isExclusive() {
         return ! alignMode;
      }
      public void activate() {
         // only called in non-align mode
         alignActive = target.align();
      }
      public boolean run() {
         if (alignMode) {
            alignMode = false;
            return false;
         } else {
            if ( ! alignActive.align(dAlignMove,dAlignRotate) ) {
               return true;
            } else {
               alignActive = null; // done, release reference

               // it's important that we don't set align mode until the end.
               // among other things, this stops the user from obtaining a bad state
               // by saving the game while an alignment is in progress.

               alignMode = true;
               return false;
            }
         }
      }
   }

   private class CommandMenu extends Command {
      private Runnable r;
      public CommandMenu(Runnable r) { this.r = r; }

      public boolean isOnPress() {
         return true;
      }
      public boolean isImmediate() {
         return true;
      }
      public boolean isExclusive() {
         return false;
         // you might think you'd want exclusive access,
         // but the real menus don't have that either.
         // actually, this is academic because of isImmediate
      }
      public boolean run() {
         EventQueue.invokeLater(r); // see note below
         return false;
      }
   }

   // menu commands must run later, otherwise there would be confusion.
   // in practice only newGame is a problem, but I think the principle is valid.
   //
   // there are two problems with calling newGame earlier.
   //
   // on the one hand, it would lead to setOptions and reset getting called,
   // modifying the object while we're in the middle of execution.
   // commandActive would get set to null, and alignMode might change.
   //
   // on the other hand, after it returned, we would continue executing,
   // so we might apply some random commands to the new game.
   // we might, for example, run a single step of non-aligned movement.

   private class CommandChangeParameter extends Command {
      private int i;
      private int sign;
      public CommandChangeParameter(int i, int sign) { this.i = i; this.sign = sign; }

      public boolean isOnPress() {
         return (    param[i] == OptionsKeysConfig.PARAM_COLOR_MODE
                  || param[i] == OptionsKeysConfig.PARAM_DEPTH      );
      }
      public boolean canRun() {
         return (param[i] != OptionsKeysConfig.PARAM_NONE);
      }
      public boolean isExclusive() {
         return false;
      }
      public boolean isExcluded() {
         return false;
      }
      public boolean run() {
         changeParameter(param[i],sign);
         return false;
      }
   }

   private class CommandChangeTexture extends Command {
      private int i;
      public CommandChangeTexture(int i) { this.i = i; }

      public boolean isOnPress() {
         return true;
      }
      public boolean isExclusive() {
         return false;
      }
      public boolean isExcluded() {
         return false;
      }
      public boolean run() {
         changeTexture(i);
         return false;
      }
   }

   private void constructCommands() {

      command = new Command[KeyBuffer.NID];

      command[KeyBuffer.getKeyID(OptionsKeys.KEY_FORWARD)] = new CommandMove(-1,+1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_BACK   )] = new CommandMove(-1,-1);

      command[KeyBuffer.getKeyID(OptionsKeys.KEY_TURN_LEFT )] = new CommandRotate(-1,0,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_TURN_RIGHT)] = new CommandRotate(-1,0,+1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_TURN_UP   )] = new CommandRotate(-1,1,+1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_TURN_DOWN )] = new CommandRotate(-1,1,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_TURN_IN   )] = new CommandRotate(-1,2,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_TURN_OUT  )] = new CommandRotate(-1,2,+1);

      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SLIDE_LEFT )] = new CommandMove(0,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SLIDE_RIGHT)] = new CommandMove(0,+1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SLIDE_UP   )] = new CommandMove(1,+1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SLIDE_DOWN )] = new CommandMove(1,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SLIDE_IN   )] = new CommandMove(2,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SLIDE_OUT  )] = new CommandMove(2,+1);

      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SPIN_UP_LEFT )] = new CommandRotate(0,1,+1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SPIN_UP_RIGHT)] = new CommandRotate(0,1,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SPIN_IN_LEFT )] = new CommandRotate(2,0,+1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SPIN_IN_RIGHT)] = new CommandRotate(2,0,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SPIN_IN_UP   )] = new CommandRotate(2,1,-1);
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_SPIN_IN_DOWN )] = new CommandRotate(2,1,+1);

      command[KeyBuffer.getKeyID(OptionsKeys.KEY_ALIGN            )] = new CommandAlign();
      command[KeyBuffer.getKeyID(OptionsKeys.KEY_CHANGE_ALIGN_MODE)] = new CommandChangeAlignMode();

      command[KeyBuffer.getKeyConfigID(OptionsKeysConfig.KEY_NEW_GAME )] = new CommandMenu(new Runnable() { public void run() { menu.doNew();     } });
      command[KeyBuffer.getKeyConfigID(OptionsKeysConfig.KEY_OPTIONS  )] = new CommandMenu(new Runnable() { public void run() { menu.doOptions(); } });
      command[KeyBuffer.getKeyConfigID(OptionsKeysConfig.KEY_EXIT_GAME)] = new CommandMenu(new Runnable() { public void run() { menu.doExit();    } });

      for (int i=0; i<10; i++) {
         command[KeyBuffer.getKeyConfigID(OptionsKeysConfig.KEY_TEXTURE+i)] = new CommandChangeTexture(i);
      }

      for (int i=0; i<6; i++) {
         command[KeyBuffer.getKeyConfigID(OptionsKeysConfig.KEY_PARAM_DECREASE+i)] = new CommandChangeParameter(i,-1);
         command[KeyBuffer.getKeyConfigID(OptionsKeysConfig.KEY_PARAM_INCREASE+i)] = new CommandChangeParameter(i,+1);
      }

      command[KeyBuffer.getKeyNew(0)] = new CommandAdjustSpeed(-1);
      command[KeyBuffer.getKeyNew(1)] = new CommandAdjustSpeed( 0);
      command[KeyBuffer.getKeyNew(2)] = new CommandAdjustSpeed(+1);
      command[KeyBuffer.getKeyNew(3)] = new CommandToggleTrack();
      command[KeyBuffer.getKeyNew(4)] = new CommandToggleEdgeColor();
      command[KeyBuffer.getKeyNew(5)] = new CommandClick();
      command[KeyBuffer.getKeyNew(6)] = new CommandMenu(new Runnable() { public void run() { menu.doReload(0); } });
      command[KeyBuffer.getKeyNew(7)] = new CommandToggleFisheye();
      command[KeyBuffer.getKeyNew(8)] = new CommandScramble();
      command[KeyBuffer.getKeyNew(9)] = new CommandToggleSeparation();
      command[KeyBuffer.getKeyNew(10)] = new CommandAddShapes(/* quantity = */ 1);
      command[KeyBuffer.getKeyNew(11)] = new CommandRemoveShape();
      command[KeyBuffer.getKeyNew(12)] = new CommandMenu(new Runnable() { public void run() { doSelectShape(); } });
      command[KeyBuffer.getKeyNew(13)] = new CommandMenu(new Runnable() { public void run() { menu.doReload(-1); } });
      command[KeyBuffer.getKeyNew(14)] = new CommandMenu(new Runnable() { public void run() { menu.doReload(+1); } });
      command[KeyBuffer.getKeyNew(15)] = new CommandToggleNormals();
      command[KeyBuffer.getKeyNew(16)] = new CommandToggleHideSel();
      command[KeyBuffer.getKeyNew(17)] = new CommandPaint();
      command[KeyBuffer.getKeyNew(18)] = new CommandMenu(new Runnable() { public void run() { doSelectPaint(); } });
   }

   private abstract class NewCommand extends Command {
      public boolean isOnPress() {
         return true;
      }
      public boolean isExclusive() {
         return false;
      }
      public boolean isExcluded() {
         return false;
      }
   }

   private class CommandAdjustSpeed extends NewCommand {
      private int dv;
      public CommandAdjustSpeed(int dv) { this.dv = dv; }

      public boolean run() {
         if (keysNew != null) keysNew.adjustSpeed(dv);
         return false;
      }
   }

   private class CommandToggleTrack extends NewCommand {
      public boolean run() {
         if (keysNew != null) keysNew.toggleTrack();
         return false;
      }
   }

   private class CommandToggleEdgeColor extends NewCommand {
      public boolean run() {
         if (keysNew != null) keysNew.toggleEdgeColor();
         return false;
      }
   }

   private class CommandClick extends NewCommand { // also used as commandjump
      public boolean isExclusive() {
         return engine.getSaveType() != IModel.SAVE_ACTION;
         // make this into a one-step exclusive command, keeps the tick logic simple
      }
      public boolean isExcluded() {
         return engine.getSaveType() != IModel.SAVE_ACTION;
         // since click can change the motion target, don't do it while in motion
      }
      public boolean run() {
         if (engine.getSaveType() == IModel.SAVE_ACTION) keysNew.jump();
         else if (keysNew != null) {
            target = keysNew.click(engine.getOrigin(),engine.getViewAxis(),engine.getAxisArray());
            if (target != null) {
               engineAlignMode = alignMode; // save
               alignMode = target.isAligned(); // reasonable default
            } else {
               target = engine;
               alignMode = engineAlignMode; // restore
            }
         }
         return false;
      }
   }

   private class CommandToggleFisheye extends NewCommand {
      public boolean run() {
         engine.toggleFisheye();
         return false;
      }
   }

   private class CommandScramble extends NewCommand {
      public boolean isExcluded() {
         return true;
         // scramble would interfere with motion in progress
      }
      public boolean run() {
         if (keysNew != null) keysNew.scramble(alignMode,engine.getOrigin());
         return false;
      }
   }

   private class CommandToggleSeparation extends NewCommand {
      public boolean run() {
         if (keysNew != null) keysNew.toggleSeparation();
         return false;
      }
   }

   private void doSelectShape() {
      if (keysNew != null && keysNew.canAddShapes()) {
         if (keysNew instanceof ISelectShape) { // always

            int quantity = menu.doSelectShape((ISelectShape) keysNew);
            // other options feed back through ISelectShape directly

            if (quantity != -1) commandChain = new CommandAddShapes(quantity);
            // the Maze class makes sure that tick will run
         }
      }
   }

   private class CommandAddShapes extends NewCommand {
      private int quantity;
      public CommandAddShapes(int quantity) { this.quantity = quantity; }

      public boolean run() {
         if (keysNew != null && keysNew.canAddShapes()) {
            keysNew.addShapes(quantity,alignMode,engine.getOrigin(),engine.getViewAxis());
         }
         return false;
      }
   }

   private class CommandRemoveShape extends NewCommand {
      public boolean run() {
         if (keysNew != null) keysNew.removeShape(engine.getOrigin(),engine.getViewAxis());
         return false;
      }
   }

   private class CommandToggleNormals extends NewCommand {
      public boolean run() {
         if (keysNew != null) keysNew.toggleNormals();
         return false;
      }
   }

   private class CommandToggleHideSel extends NewCommand {
      public boolean run() {
         if (keysNew != null) keysNew.toggleHideSel();
         return false;
      }
   }

   private void doSelectPaint() {
      if (keysNew != null && keysNew.canPaint()) {
         if (keysNew instanceof ISelectShape) { // always
            boolean b = menu.doSelectPaint((ISelectShape) keysNew);
            if (b) commandChain = new CommandPaint();
            // the process is the same as with doSelectShape
         }
      }
   }

   private class CommandPaint extends NewCommand {
      public boolean run() {
         if (keysNew != null && keysNew.canPaint()) {
            keysNew.paint(engine.getOrigin(),engine.getViewAxis());
         }
         return false;
      }
   }

// --- parameter changes ---

   private abstract class Parameter {

      private int grain;
      public Parameter(int grain) { this.grain = grain; }

      public IValidate getOptions() { return options.os(); } // correct for five of nine

      public abstract double get();
      public abstract void set(double d);

      public double change(double d, int sign) {
         return (Math.rint(d * grain) + sign) / grain;
         // the reason for this slightly peculiar method
         // is that it produces nice round FP numbers
      }
      public abstract void apply(double d);
   }

   private class TextureParameter extends Parameter {

      private int i;
      public TextureParameter(int i) { super(0); this.i = i; }

      public IValidate getOptions() { return options.ov(); }

      public double get() { return options.ov().texture[i] ? 1 : 0; }
      public void set(double d) { options.ov().texture[i] = (d != 0); }

      public double change(double d, int sign) { return (d != 0) ? 0 : 1; }
      public void apply(double d) { engine.setTexture(options.ov().texture); }
   }

   private void constructParameters() {

      parameter = new Parameter[OptionsKeysConfig.NPARAM];

      parameter[OptionsKeysConfig.PARAM_COLOR_MODE] = new Parameter(1) {
            public IValidate getOptions() { return options.oc(); }
            public double get() { return options.oc().colorMode; }
            public void set(double d) { options.oc().colorMode = (int) d; }
            public double change(double d, int sign) { // cycle
               return (d + sign + OptionsColor.NCOLOR_MODE) % OptionsColor.NCOLOR_MODE;
            }
            public void apply(double d) { engine.setColorMode((int) d); }
         };

      parameter[OptionsKeysConfig.PARAM_DEPTH] = new Parameter(1) {
            public IValidate getOptions() { return options.ov(); }
            public double get() { return options.ov().depth; }
            public void set(double d) { options.ov().depth = (int) d; }
            public void apply(double d) { engine.setDepth((int) d); }
         };
      parameter[OptionsKeysConfig.PARAM_RETINA] = new Parameter(100) {
            public IValidate getOptions() { return options.ov(); }
            public double get() { return options.ov().retina; }
            public void set(double d) { options.ov().retina = d; }
            public void apply(double d) { engine.setRetina(d); }
         };
      parameter[OptionsKeysConfig.PARAM_SCALE] = new Parameter(100) {
            public IValidate getOptions() { return options.ov(); }
            public double get() { return options.ov().scale; }
            public void set(double d) { options.ov().scale = d; }
            public void apply(double d) { engine.setScale(d); }
         };

      parameter[OptionsKeysConfig.PARAM_SCREEN_WIDTH] = new Parameter(10) {
            public double get() { return options.os().screenWidth; }
            public void set(double d) { options.os().screenWidth = d; }
            public void apply(double d) { engine.setScreenWidth(d); }
         };
      parameter[OptionsKeysConfig.PARAM_SCREEN_DISTANCE] = new Parameter(10) {
            public double get() { return options.os().screenDistance; }
            public void set(double d) { options.os().screenDistance = d; }
            public void apply(double d) { engine.setScreenDistance(d); }
         };
      parameter[OptionsKeysConfig.PARAM_EYE_SPACING] = new Parameter(10) {
            public double get() { return options.os().eyeSpacing; }
            public void set(double d) { options.os().eyeSpacing = d; }
            public void apply(double d) { engine.setEyeSpacing(d); }
         };
      parameter[OptionsKeysConfig.PARAM_TILT_VERTICAL] = new Parameter(10) {
            public double get() { return options.os().tiltVertical; }
            public void set(double d) { options.os().tiltVertical = d; }
            public void apply(double d) { engine.setTiltVertical(d); }
         };
      parameter[OptionsKeysConfig.PARAM_TILT_HORIZONTAL] = new Parameter(10) {
            public double get() { return options.os().tiltHorizontal; }
            public void set(double d) { options.os().tiltHorizontal = d; }
            public void apply(double d) { engine.setTiltHorizontal(d); }
         };

      parameter[OptionsKeysConfig.PARAM_FISHEYE_WIDTH] = new Parameter(100) {
            public IValidate getOptions() { return OptionsFisheye.of; }
            public double get() { return OptionsFisheye.of.width; }
            public void set(double d) { OptionsFisheye.of.width = d; }
            public void apply(double d) { OptionsFisheye.recalculate(); }
         };
      parameter[OptionsKeysConfig.PARAM_FISHEYE_FLARE] = new Parameter(100) {
            public IValidate getOptions() { return OptionsFisheye.of; }
            public double get() { return OptionsFisheye.of.flare; }
            public void set(double d) { OptionsFisheye.of.flare = d; }
            public void apply(double d) { OptionsFisheye.recalculate(); }
         };
      parameter[OptionsKeysConfig.PARAM_FISHEYE_RGAP] = new Parameter(100) {
            public IValidate getOptions() { return OptionsFisheye.of; }
            public double get() { return OptionsFisheye.of.rainbowGap; }
            public void set(double d) { OptionsFisheye.of.rainbowGap = d; }
            public void apply(double d) { OptionsFisheye.recalculate(); }
         };

      textureParameter = new Parameter[10];
      for (int i=0; i<10; i++) {
         textureParameter[i] = new TextureParameter(i);
      }
   }

   private void changeParameter(int param, int sign) {
      // caller must prevent PARAM_NONE
      changeParameter(parameter[param],sign);
   }

   private void changeTexture(int i) {
      changeParameter(textureParameter[i],0); // sign is ignored
   }

   private void changeParameter(Parameter p, int sign) {

      double dSave = p.get();
      double dNew  = p.change(dSave,sign);

      p.set(dNew);

      boolean isValid = false;
      try {
         p.getOptions().validate();
         isValid = true;
      } catch (ValidationException e) {
      }

      if (isValid) p.apply(dNew);
      else         p.set(dSave);
   }
}

