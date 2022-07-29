/*
 * MapModel.java
 */

/**
 * A model that lets the user move through a maze.
 */

public class MapModel implements IModel {

// --- fields ---

   private Map map;
   private Colorizer colorizer;
   private RenderAbsolute renderAbsolute;

// --- construction ---

   public MapModel(int dimSpace, OptionsMap om, OptionsColor oc, OptionsSeed oe, OptionsView ov) {

      map = new Map(dimSpace,om,oe.mapSeed);
      colorizer = new Colorizer(dimSpace,om.dimMap,om.size,oc,oe.colorSeed);
      renderAbsolute = new RenderAbsolute(dimSpace,map,colorizer,ov);
   }

// --- implementation of IModel ---

   public void initPlayer(double[] origin, double[][] axis) {

      Grid.fromCell(origin,map.getStart());

      // cycle the axes so that we're correctly oriented when dimMap < dimSpace
      // axis[dimSpace-1] points in the forward direction, which should be unitVector(0) ... etc.
      // everything else is random, so it's OK for the axes to be deterministic
      //
      for (int a=0; a<axis.length; a++) Vec.unitVector(axis[a],(a + 1) % axis.length);
   }

   public void testOrigin(double[] origin, int[] reg1, int[] reg2) throws ValidationException {

      // check that origin is in bounds and open
      // this is clumsy, because normally the walls keep us in bounds.

      // we might be on multiple boundaries, in which case toCell can't return all cells,
      // but that doesn't matter here.
      // the cells are all adjacent, so if any one is strictly in bounds,
      // the rest are enough in bounds not to cause an array fault in isOpen.

      Grid.toCell(reg1,reg2,origin); // ignore result
      if ( ! map.inBounds(reg1) ) throw App.getEmptyException();

      if ( ! Grid.isOpen(origin,map,reg1) ) throw App.getEmptyException();
   }

   public void setColorMode(int colorMode) {
      colorizer.setColorMode(colorMode);
   }

   public void setDepth(int depth) {
      renderAbsolute.setDepth(depth);
   }

   public void setTexture(boolean[] texture) {
      renderAbsolute.setTexture(texture);
   }

   public void setOptions(OptionsColor oc, long seed, int depth, boolean[] texture) {
      colorizer.setOptions(oc,seed);
      renderAbsolute.setDepth(depth);
      renderAbsolute.setTexture(texture);
   }

   public boolean isAnimated() {
      return false;
   }

   public int getSaveType() {
      return IModel.SAVE_MAZE;
   }

   public boolean canMove(double[] p1, double[] p2, int[] reg1, double[] reg2) {
      return Grid.isOpenMove(p1,p2,map,reg1,reg2);
   }

   public boolean atFinish(double[] origin, int[] reg1, int[] reg2) {
      int dir = Grid.toCell(reg1,reg2,origin);
      return (                            Grid.equals(reg1,map.getFinish())
               || (dir != Dir.DIR_NONE && Grid.equals(reg2,map.getFinish()) ) );
   }

   public boolean dead() { return false; }

   public void setBuffer(LineBuffer buf) {
      renderAbsolute.setBuffer(buf);
   }

   public void animate() {
   }

   public void render(double[] origin) {
      renderAbsolute.run(origin);
   }

   public void save(IStore store, OptionsMap om) throws ValidationException {
      map.save(store, om);
   }
}

