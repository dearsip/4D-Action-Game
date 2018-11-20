/*
 * Core.java
 */

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

/**
 * The game core, mostly detached from the user interface.
 */

public class Core implements IOptions, IStorable, KeyListener, FocusListener {

// --- fields ---

   private int dim;
   private OptionsAll oa;
   private Engine engine;

   private KeyBuffer keyBuffer;
   private KeyMapper keyMapper3;
   private KeyMapper keyMapper4;
   private Controller controller;
   private Clock clock;

// --- option accessors ---

   // some of these also implement IOptions

   private OptionsMap om() {
      // omCurrent is always non-null, so can be used directly
      return (dim == 3) ? oa.opt.om3 : oa.opt.om4;
   }

   public OptionsColor oc() {
      if (oa.ocCurrent != null) return oa.ocCurrent;
      return (dim == 3) ? oa.opt.oc3 : oa.opt.oc4;
   }

   public OptionsView ov() {
      if (oa.ovCurrent != null) return oa.ovCurrent;
      return (dim == 3) ? oa.opt.ov3 : oa.opt.ov4;
   }

   public OptionsStereo os() {
      return oa.opt.os;
   }

   private OptionsKeys ok() {
      return (dim == 3) ? oa.opt.ok3 : oa.opt.ok4;
   }

   private OptionsMotion ot() {
      return (dim == 3) ? oa.opt.ot3 : oa.opt.ot4;
   }

   public OptionsImage oi() {
      return oa.opt.oi;
   }

   private KeyMapper keyMapper() {
      return (dim == 3) ? keyMapper3 : keyMapper4;
   }

// --- construction ---

   public Core(Options opt, IDisplay displayInterface, IMenu menu) {

      // dim and rest of oa are initialized when new game started

      oa = new OptionsAll();
      oa.opt = opt;
      oa.omCurrent = new OptionsMap(0); // blank for copying into
      oa.oeNext = new OptionsSeed();

      engine = new Engine(displayInterface);

      keyBuffer = new KeyBuffer();
      keyMapper3 = new KeyMapper(keyBuffer,opt.ok3,opt.okc);
      keyMapper4 = new KeyMapper(keyBuffer,opt.ok4,opt.okc);
      controller = new Controller(keyBuffer,this,menu,engine);
      clock = new Clock(controller);
   }

// --- external interface ---

   public void newGame(int dim) {
      if (dim != 0) this.dim = dim; // allow zero to mean "keep the same"

      OptionsMap.copy(oa.omCurrent,om());
      oa.ocCurrent = null; // use standard colors for dimension
      oa.ovCurrent = null; // ditto
      oa.oeCurrent = oa.oeNext;
      oa.oeCurrent.forceSpecified();
      oa.oeNext = new OptionsSeed();

      IModel model = new MapModel(this.dim,oa.omCurrent,oc(),oa.oeCurrent,ov());
      engine.newGame(this.dim,model,ov(),oa.opt.os,true);

      controller.setOptions(oa.opt.okc,ot());
      controller.setKeysNew(null);
      controller.setAlwaysRun(model.isAnimated());
      clock.setFrameRate(ot().frameRate);

      boolean hack = keyBuffer.down[KeyBuffer.getKeyConfigID(OptionsKeysConfig.KEY_NEW_GAME)];
      keyMapper().releaseAll(); // sync up key mapper, which may have changed with dim
      if (hack) keyMapper().unrelease(oa.opt.okc.key[OptionsKeysConfig.KEY_NEW_GAME]);

      // the purpose of the above hack is to prevent auto-repeat of the new-game key.
      // normally the key mapper prevents auto-repeat by seeing that the key is already down,
      // but here in newGame we release the keys, so the next auto-repeat counts as a press.
      //
      // it is important that the new-game key does not change between mappers,
      // otherwise we would be putting the mapper into a factually incorrect state.
      //
      // a better solution would be to track the state of all the keys,
      // not just ones that are mapped, then we wouldn't need to release the keys,
      // we could just transfer the state from one mapper to the other.

      controller.reset(this.dim,ok().startAlignMode);
      // clock will stop when controller reports idle
   }

   public void resetWin() {
      engine.resetWin();
   }

   public void restartGame() {
      engine.restartGame();

      keyMapper().releaseAll(); // sync up key mapper, which may have changed with dim
      controller.reset(dim,ok().startAlignMode);
      // clock will stop when controller reports idle
   }

   public int getDim() {
      return dim;
   }

   /**
    * Get options.  Ownership is not transferred, so the object should not be modified.
    */
   public Options getOptions() {
      return oa.opt;
   }

   /**
    * Get options.  Ownership is not transferred, so the object should not be modified.
    */
   public OptionsAll getOptionsAll() {
      return oa;
   }

   /**
    * Set options.  Ownership is transferred.
    */
   public void setOptions(OptionsStereo os, OptionsImage oi) {
      oa.opt.os = os;
      oa.opt.oi = oi;

      engine.setOptions(oc(),ov(),os,oa.oeCurrent);
   }

   /**
    * Set options.  Ownership is transferred.
    */
   public void setOptionsAll(OptionsAll oa) {
      this.oa.opt = oa.opt;
      // omCurrent is read-only
      this.oa.ocCurrent = oa.ocCurrent; // nullness should remain the same
      this.oa.ovCurrent = oa.ovCurrent; // ditto
      this.oa.oeCurrent = oa.oeCurrent;
      this.oa.oeCurrent.forceSpecified(); // make blanks into new random seeds
      this.oa.oeNext = oa.oeNext;

      engine.setOptions(oc(),ov(),this.oa.opt.os,this.oa.oeCurrent);

      keyMapper3.setOptions(this.oa.opt.ok3,this.oa.opt.okc);
      keyMapper4.setOptions(this.oa.opt.ok4,this.oa.opt.okc);
      controller.setOptions(this.oa.opt.okc,ot());
      clock.setFrameRate(ot().frameRate);
   }

   public void setEdge(int edge) {
      engine.setEdge(edge);
   }

   public void forceClockStart() {
      clock.start();
   }

   public static GeomModel buildModel(Context c) throws Exception {

      DimensionAccumulator da = new DimensionAccumulator();
      Track track = null;
      LinkedList tlist = new LinkedList();
      LinkedList scenery = new LinkedList();
      LinkedList slist = new LinkedList();
      Struct.ViewInfo viewInfo = null;
      Struct.DrawInfo drawInfo = null;

      Struct.FinishInfo finishInfo = null;

   // scan for items

      ListIterator li = c.stack.listIterator();
      while (li.hasNext()) {
         Object o = li.next();

         if (o instanceof IDimension) {
            da.putDimension(((IDimension) o).getDimension());
         } else if (o instanceof IDimensionMultiSrc) {
            ((IDimensionMultiSrc) o).getDimension(da);
         }
         // this is just a quick check to catch the most obvious mistakes.
         // I can't be bothered to add dimension accessors to the scenery.

         if (o == null) {
            throw new Exception("Unused null object on stack.");
         } else if (o instanceof Track) {
            if (track != null) throw new Exception("Only one track object allowed (but it can have disjoint loops).");
            track = (Track) o;
         } else if (o instanceof Train) {
            tlist.add(o);
         } else if (o instanceof IScenery) {
            scenery.add(o);
         } else if (o instanceof Geom.ShapeInterface) { // Shape or CompositeShape
            ((Geom.ShapeInterface) o).unglue(slist);
         } else if (o instanceof Struct.ViewInfo) {
            if (viewInfo != null) throw new Exception("Only one viewinfo command allowed.");
            viewInfo = (Struct.ViewInfo) o;
         } else if (o instanceof Struct.DrawInfo) {
            if (drawInfo != null) throw new Exception("Only one drawinfo command allowed.");
            drawInfo = (Struct.DrawInfo) o;
         } else if (o instanceof Struct.DimensionMarker) {
            // ignore, we're done with it
         } else if (o instanceof Struct.FinishInfo) {
            if (finishInfo != null) throw new Exception("Only one finishInfo command allowed.");
            finishInfo = (Struct.FinishInfo) o;
         } else {
            throw new Exception("Unused object on stack (" + o.getClass().getName() + ").");
         }
      }

   // use items to make model

      if (da.first) throw new Exception("Scene doesn't contain any objects.");
      if (da.error) throw new Exception("The number of dimensions is not consistent.");
      int dtemp = da.dim; // we shouldn't change the Core dim variable yet

      Geom.Shape[] shapes = (Geom.Shape[]) slist.toArray(new Geom.Shape[slist.size()]);
      Train[] trains = (Train[]) tlist.toArray(new Train[tlist.size()]);

      if (track != null) TrainModel.init(track,trains); // kluge needed for track scale

      if (scenery.size() == 0) scenery.add((dtemp == 3) ? new Mat.Mat3() : (IScenery) new Mat.Mat4());
      if (track != null) scenery.add(track); // add last so it draws over other scenery

      GeomModel model;
      if (finishInfo != null) model = new ActionModel(dtemp,shapes,drawInfo,viewInfo,finishInfo); 
      else model = (track != null) ? new TrainModel(dtemp,shapes,drawInfo,viewInfo,track,trains)
                                             : new GeomModel (dtemp,shapes,drawInfo,viewInfo);
      model.addAllScenery(scenery);

   // gather dictionary info

      LinkedList availableColors = new LinkedList();
      LinkedList availableShapes = new LinkedList();
      HashMap colorNames = new HashMap();
      HashMap idealNames = new HashMap();

      Iterator i = c.dict.entrySet().iterator();
      while (i.hasNext()) {
         Map.Entry entry = (Map.Entry) i.next();
         Object o = entry.getValue();
         if (o instanceof Color) {

            availableColors.add(new NamedObject(entry));

            String name = (String) entry.getKey();
            if ( ! c.topLevelDef.contains(name) ) {
               colorNames.put((Color) o,name);
            }

         } else if (o instanceof Geom.Shape) { // not ShapeInterface, at least for now
            Geom.Shape shape = (Geom.Shape) o;
            if (shape.getDimension() == dtemp) {

               availableShapes.add(new NamedObject(entry));

               String name = (String) entry.getKey();
               if ( ! c.topLevelDef.contains(name) ) {
                  idealNames.put(shape.ideal,name);
               }
            }
         }
         // else it's not something we're interested in
      }

      Collections.sort(availableColors);
      Collections.sort(availableShapes);

      model.setAvailableColors(availableColors);
      model.setAvailableShapes(availableShapes);

      model.setSaveInfo(c.topLevelInclude,colorNames,idealNames);

   // done

      return model;
   }

   public void loadGeom(File file) throws Exception {

   // read file

      Context c = DefaultContext.create();
      c.libDirs.add(new File("data" + File.separator + "lib"));
      Language.include(c,file);

   // build the model

      GeomModel model = buildModel(c);
      // run this before changing anything since it can fail

   // switch to geom

      dim = model.getDimension();

      // no need to modify omCurrent, just leave it with previous maze values
      oa.ocCurrent = null;
      oa.ovCurrent = null;
      // no need to modify oeCurrent or oeNext

      boolean[] texture = model.getDesiredTexture();
      if (texture != null) { // model -> ov
         OptionsView ovLoad = new OptionsView();
         OptionsView.copy(ovLoad,ov(),texture);
         oa.ovCurrent = ovLoad;
         // careful, if you set ovCurrent earlier
         // then ov() will return the wrong thing
      } else { // ov -> model
         texture = ov().texture;
      }
      model.setTexture(texture);

      model.setDepth(ov().depth);

      // model already constructed
      engine.newGame(dim,model,ov(),oa.opt.os,true);

      controller.setOptions(oa.opt.okc,ot());
      controller.setKeysNew(model);
      controller.setAlwaysRun(model.isAnimated());
      clock.setFrameRate(ot().frameRate);

      keyMapper().releaseAll(); // sync up key mapper, which may have changed with dim

      controller.reset(dim,model.getAlignMode(/* defaultAlignMode = */ ok().startAlignMode));
      // clock will stop when controller reports idle
   }

   public static void writeIntVec(IToken t, int[] d) throws IOException {
      t.putSymbol("[");
      for (int i=0; i<d.length; i++) {
         t.putInteger(d[i]);
      }
      t.putSymbol("]");
   }

   public static void writeVec(IToken t, double[] d) throws IOException {
      t.putSymbol("[");
      for (int i=0; i<d.length; i++) {
         t.putDouble(d[i]);
      }
      t.putSymbol("]");
   }

   private static final String[] unitPos = new String[] { "X+", "Y+", "Z+", "W+" };
   private static final String[] unitNeg = new String[] { "X-", "Y-", "Z-", "W-" };

   public static void writeAxis(IToken t, double[] d) throws IOException {

      // we don't want approximations here.  if we're in align mode,
      // we'll snap to the axes, so they'll be exact.
      // also note, we want both [1 0 0] and [1 0 0 0] to map to X+.

      int count = 0;
      int index = 0;
      double value = 0;

      for (int i=0; i<d.length; i++) {
         if (d[i] != 0) {
            count++;
            index = i;
            value = d[i];
         }
      }

      if (count == 1 && Math.abs(value) == 1) {
         String[] unit = (value == 1) ? unitPos : unitNeg;
         t.putWord(unit[index]);
      } else {
         writeVec(t,d);
      }
   }

   public static void writeAxisArray(IToken t, double[][] axis) throws IOException {
      t.putSymbol("[");
      for (int i=0; i<axis.length; i++) {
         if (i != 0) t.space();
         writeAxis(t,axis[i]);
      }
      t.putSymbol("]");
   }

   public static String format(Color color, HashMap colorNames) {
      if (color == null) return "null";
      String s = (String) colorNames.get(color);
      if (s == null) {
         s = Field.putColor(color);
         colorNames.put(color,s);
         // sure, why not cache it
      }
      return s;
   }

   public static void writeVertices(IToken t, double[][] vertex) throws IOException {
      for (int i=0; i<vertex.length; i++) {
         writeVec(t,vertex[i]);
         t.newLine();
      }
   }

   /**
    * @param colorNames The color name dictionary, or null if you don't want colors.
    */
   public static void writeEdges(IToken t, Geom.Edge[] edge, HashMap colorNames) throws IOException {
      for (int i=0; i<edge.length; i++) {
         t.putInteger(edge[i].iv1);
         t.putInteger(edge[i].iv2);
         if (colorNames != null && edge[i].color != null) {
            t.putWord(format(edge[i].color,colorNames)).putWord("cedge");
         } else {
            t.putWord("edge");
         }
         t.newLine();
      }
   }

   public static void writeTexture(IToken t, int face, Geom.Texture texture, HashMap colorNames) throws IOException {
      t.putInteger(face).newLine();
      t.putSymbol("[").newLine();
      writeVertices(t,texture.vertex);
      t.putSymbol("]").putSymbol("[").newLine();
      writeEdges(t,texture.edge,colorNames);
      t.putSymbol("]").newLine();
      t.putWord("texture").putWord("PROJ_NONE").putWord("null").putWord("facetexture").newLine();
   }

   public static void writeShapeDef(IToken t, Geom.Shape shape, String name, HashMap colorNames) throws IOException {
      t.putSymbol("[").newLine();
      writeVertices(t,shape.vertex);
      t.putSymbol("]").putSymbol("[").newLine();
      writeEdges(t,shape.edge,/* colorNames = */ null); // colors handled later
      t.putSymbol("]").putSymbol("[").newLine();
      for (int i=0; i<shape.face.length; i++) {
         writeIntVec(t,shape.face[i].ie);
         t.space();
         if (shape.face[i].normal != null) {
            writeVec(t,shape.face[i].normal);
            t.space();
         } else {
            t.putWord("null");
         }
         t.putWord("face").newLine();
      }
      t.putSymbol("]").newLine();
      t.putWord("shape");
      if ( ! Vec.exactlyEquals(shape.aligncenter,shape.shapecenter) ) {
         // default is exactly equal, so this is a reasonable test
         t.space();
         writeVec(t,shape.aligncenter);
         t.space().putWord("aligncenter");
      }

      boolean first = true; // avoid newline here, usually not needed

      for (int i=0; i<shape.face.length; i++) {
         if (shape.face[i].customTexture instanceof Geom.Texture) {

            if (first) { t.newLine(); first = false; }
            writeTexture(t,i,(Geom.Texture) shape.face[i].customTexture,colorNames);
         }
      }

      ShapeColor.writeColors(t,new ShapeColor.NullState(),shape,colorNames,first);

      t.putString(name).putWord("def").newLine();
   }

   public static String getNextName(HashMap map, String prefix) {
      for (int i=1; ; i++) { // terminates since map is finite
         String name = prefix + i;
         if ( ! map.containsValue(name) ) return name;
         // not efficient to scan values, but oh well
      }
   }

   public static void writeModel(IToken t, GeomModel model, double[] origin, double[][] axis) throws IOException {

   // includes

      Iterator j = model.retrieveTopLevelInclude().iterator();
      while (j.hasNext()) {
         String filename = (String) j.next();
         t.putString(filename).putWord("include").newLine();
      }
      // note, we want to include all the same files,
      // not just the ones containing shapes that were used,
      // both because it's easier
      // and because we want to have the same the UI shapes.

      t.newLine();

   // viewinfo

      writeVec(t,origin);
      t.space();
      writeAxisArray(t,axis);
      t.space().putWord("viewinfo").newLine();

   // drawinfo

      boolean[] texture = model.retrieveTexture();
      t.putSymbol("[");
      for (int i=0; i<texture.length; i++) {
         t.putInteger(texture[i] ? 1 : 0);
      }
      t.putSymbol("]").space().putBoolean(model.retrieveUseEdgeColor()).putWord("drawinfo").newLine();

      t.newLine();

   // shape definitions

      Geom.Shape[] shapes = model.retrieveShapes();
      HashMap colorNames = model.retrieveColorNames();
      HashMap idealNames = model.retrieveIdealNames();

      // we modify these, so we have to make copies so that second saves
      // will work correctly.  actually colorNames is harmless, but it's
      // still good form.
      colorNames = (HashMap) colorNames.clone();
      idealNames = (HashMap) idealNames.clone();

      // note, it's entirely possible that the context dictionary listed
      // the same color or shape under different names.  in that case,
      // which one you get here is random (determined by hash map order).
      // it's also possible the dictionary will list different shapes
      // that have the same ideal, and the same thing happens in that case.

      for (int i=0; i<shapes.length; i++) {
         Geom.Shape shape = shapes[i];
         if (shape == null) continue;

         if (idealNames.get(shape.ideal) != null) continue; // got it

         String name = getNextName(idealNames,"shape");
         idealNames.put(shape.ideal,name);

         writeShapeDef(t,shape.ideal,name,colorNames);

         t.newLine();
      }

   // shapes

      for (int i=0; i<shapes.length; i++) {
         Geom.Shape shape = shapes[i];
         if (shape == null) continue;

         t.putWord((String) idealNames.get(shape.ideal)).space();
         writeVec(t,shape.aligncenter);
         t.space();
         writeAxisArray(t,shape.axis);
         t.space().putWord("place").newLine();

         if (shape.noUserMove) {
            t.putWord("nomove").newLine();
         }

         ShapeColor.writeColors(t,new ShapeColor.ShapeState(shape.ideal),shape,colorNames,/* first = */ false);

         t.newLine();
      }
   }

   public void saveGeom(File file) throws Exception {

      TokenFile t = new TokenFile(file);
      try {

         GeomModel model = (GeomModel) engine.retrieveModel();
         // cast OK since we've already checked getSaveType

         writeModel(t,model,engine.getOrigin(),engine.getAxisArray());

      } finally {
         t.close();
         // intentionally not cleaning up partial file on error
      }
   }

// --- implementation of IStorable ---

   private static final String VALUE_CHECK       = "Maze";

   private static final String KEY_CHECK         = "game";
   private static final String KEY_DIM           = "dim";
   private static final String KEY_OPTIONS_MAP   = "om";
   private static final String KEY_OPTIONS_COLOR = "oc";
   private static final String KEY_OPTIONS_VIEW  = "ov";
   private static final String KEY_OPTIONS_SEED  = "oe";
   private static final String KEY_ALIGN_MODE    = "align";

   public void load(IStore store) throws ValidationException {

   // produce a more helpful message when the file type isn't even close

      try {
         if ( ! store.getString(KEY_CHECK).equals(VALUE_CHECK) ) throw App.getEmptyException();
      } catch (ValidationException e) {
         throw App.getException("Core.e1");
      }

   // read file, but don't modify existing objects until we're sure of success

      int dimLoad = store.getInteger(KEY_DIM);
      if ( ! (dimLoad == 3 || dimLoad == 4) ) throw App.getException("Core.e2");

      OptionsMap omLoad = new OptionsMap(dimLoad);
      OptionsColor ocLoad = new OptionsColor();
      OptionsView ovLoad = new OptionsView();
      OptionsSeed oeLoad = new OptionsSeed();

      store.getObject(KEY_OPTIONS_MAP,omLoad);
      store.getObject(KEY_OPTIONS_COLOR,ocLoad);
      store.getObject(KEY_OPTIONS_VIEW,ovLoad);
      store.getObject(KEY_OPTIONS_SEED,oeLoad);
      if ( ! oeLoad.isSpecified() ) throw App.getException("Core.e3");
      boolean alignModeLoad = store.getBoolean(KEY_ALIGN_MODE);

   // ok, we know enough ... even if the engine parameters turn out to be invalid,
   // we can still start a new game

      // and, we need to initialize the engine before it can validate its parameters

      dim = dimLoad;

      oa.omCurrent = omLoad; // may as well transfer as copy
      oa.ocCurrent = ocLoad;
      oa.ovCurrent = ovLoad;
      oa.oeCurrent = oeLoad;
      // oeNext is not modified by loading a game

      IModel model = new MapModel(dim,oa.omCurrent,oc(),oa.oeCurrent,ov());
      engine.newGame(dim,model,ov(),oa.opt.os,false);

      controller.setOptions(oa.opt.okc,ot());
      controller.setKeysNew(null);
      controller.setAlwaysRun(model.isAnimated());
      clock.setFrameRate(ot().frameRate);

      keyMapper().releaseAll(); // sync up key mapper, which may have changed with dim

      controller.reset(dim,alignModeLoad);
      // clock will stop when controller reports idle

   // now let the engine load its parameters

      engine.load(store,alignModeLoad);
   }

   public int getSaveType() {
      return engine.getSaveType();
   }

   public void save(IStore store) throws ValidationException {

      store.putString(KEY_CHECK,VALUE_CHECK);

      store.putInteger(KEY_DIM,dim);
      store.putObject(KEY_OPTIONS_MAP,oa.omCurrent);
      store.putObject(KEY_OPTIONS_COLOR,oc()); // ocCurrent may be null
      store.putObject(KEY_OPTIONS_VIEW,ov());  // ditto
      store.putObject(KEY_OPTIONS_SEED,oa.oeCurrent);
      store.putBoolean(KEY_ALIGN_MODE,controller.getAlignMode());

      engine.save(store);
   }

// --- implementation of KeyListener ---

   public void keyPressed(KeyEvent e) {
      keyMapper().keyChanged(e.getKeyCode(),true);
      clock.start();

      if (e.getKeyCode() == KeyEvent.VK_ALT) e.consume();
      // fix for keys getting stuck.
      // if you press a key and then press and release alt,
      // you never get the release event for the other key,
      // probably because the Java menu system intercepts it somehow.
      // consuming the key fixes it.  probably the right way
      // is to consume other keys too, but let's keep change minimal.
   }

   public void keyReleased(KeyEvent e) {
      keyMapper().keyChanged(e.getKeyCode(),false);
   }

   public void keyTyped(KeyEvent e) {
   }

// --- implementation of FocusListener ---

   public void focusGained(FocusEvent e) {
   }

   public void focusLost(FocusEvent e) {
      keyMapper().releaseAll();

      // there were a bunch of ways you could make keys stick,
      // and this fixes all of them!
      //
      // 1. click on the menu while keys down
      // 2. switch to a different application
      // 3. use any key command that popped a window
      //    a. options dialog
      //    b. add-shape dialog
      //    c. fail on reload
      //
      // this doesn't cover the new-game hack, but that's different.
   }

}

