/*
 * BlockModel.java
 */

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

/**
 * A model that lets the user build architecture with blocks.
 */

public class BlockModel implements IModel, IKeysNew, IMove {

// --- fields ---

   private int dim;
   private Geom.Shape[] shapes;
   private DynamicArray.OfBoolean map;
   private DynamicArray.Iterator visShape;
   private DynamicArray.OfFace visFace;
   private Vector scenery;
   protected LineBuffer buf;
   private Clip.Draw[] clipUnits;
   private boolean[][] inFront;

   private Engine engine;
   private int depth;
   private double[] origin;
   protected double[] reg1;
   protected double[] reg2;
   protected Clip.Result clipResult;
   private IDraw currentDraw;

   private LinkedList topLevelInclude;
   private HashMap colorNames;
   private HashMap idealNames;

// --- construction ---

   public BlockModel(int dim, int[] size, int depth) throws Exception {

      this.dim = dim;
      map = new DynamicArray.OfBoolean(dim,size);
      this.depth = depth;
      visShape = new DynamicArray.Iterator(dim,2*depth+1);
      visFace = new DynamicArray.OfFace(dim,2*depth+1);
      shapes = new Geom.Shape[Math.pow(2*depth+1,dim)];
      clipUnits = new Clip.Draw[shapes.length];
      for (int i=0; i<shapes.length; i++) clipUnits[i] = new Clip.Draw(dim);
      inFront = new boolean[shapes.length][shapes.length];
      separators = new Geom.Separator[shapes.length][shapes.length];
      axisDirection = new int[dim];
      useSeparation = true;

      origin = new double[dim];
      reg1 = new double[dim];
      reg2 = new double[dim];
      clipResult = new Clip.Result();
   }

   public int getDimension() { return dim; }

   private void calcInFront() {
      for (int i=0; i<shapes.length-1; i++) {
         Geom.Shape s1 = shapes[i];
         if (s1 == null) continue;
         for (int j=i+1; j<shapes.length; j++) {
            Geom.Shape s2 = shapes[j];
            if (s2 == null) continue;

            // prefer dynamic separation even when we know a static
            // separator because dynamic ones are better at finding
            // the desired value NO_FRONT.
            int result = Clip.dynamicSeparate(s1,s2,origin,reg1,reg2);
            if (result == Geom.Separator.UNKNOWN) {
               Geom.Separator sep;

               if (isMobile(s1) || isMobile(s2)) {
                  sep = Clip.staticSeparate(s1,s2,/* any = */ false);
                  // don't remember the separator
               } else {
                  sep = separators[i][j];
                  if (sep == null) {
                     sep = Clip.staticSeparate(s1,s2,/* any = */ false);
                     separators[i][j] = sep;
                  }
               }

               result = sep.apply(origin);
            }

            inFront[i][j] = (result == Geom.Separator.S1_FRONT);
            inFront[j][i] = (result == Geom.Separator.S2_FRONT);
         }
      }
      // note that in general inFront is not transitive.  with long blocks
      // you can easily construct cycles where one is in front of the next
      // all the way around.
   }

   private boolean isMobile(Geom.Shape shape) {
      return (shape.systemMove || shape == selectedShape);
   }

   private int indexOf(Geom.Shape shape) {
      // not worth checking for removed shapes
      for (int i=0; i<shapes.length; i++) {
         if (shapes[i] == shape) return i;
      }
      throw new RuntimeException("Shape not in table.");
   }

   public void setEngine(Engine engine) {
      this.engine = engine;
   }

// --- implementation of IKeysNew ---

   public void adjustSpeed(int dv) {}
   public void toggleTrack() {}

   public void toggleEdgeColor() {
   }

   public IMove click(double[] origin, double[] viewAxis, double[][] axisArray) {
      return null;
   }

   public void scramble(boolean alignMode, double[] origin) {
   }

   public void toggleSeparation() {
   }

   public boolean canAddShapes() {
      return false;
   }

   public void addShapes(int quantity, boolean alignMode, double[] origin, double[] viewAxis) {
   }

   public void removeShape(double[] origin, double[] viewAxis) {
   }

   public void toggleNormals() {
   }

   public void toggleHideSel() {
   }

   public boolean canPaint() {
      return false;
   }

   public void paint(double[] origin, double[] viewAxis) {
   }

   public void jump() {
      engine.jump();
   }

// --- implementation of IModel ---

   public void initPlayer(double[] origin, double[][] axis) {
   }

   public void testOrigin(double[] origin, int[] reg1, int[] reg2) throws ValidationException {
   }

   public void setColorMode(int colorMode) {
   }

   public void setDepth(int depth) {
     this.depth = depth;
   }

   public void setTexture(boolean[] texture) {
   }

   public void setOptions(OptionsColor oc, long seed, int depth, boolean[] texture) {
      setDepth(depth);
   }

   public boolean isAnimated() {
      return true;
   }

   public int getSaveType() {
      return IModel.SAVE_CRAFT;
   }

   public boolean canMove(double[] p1, double[] p2, int[] reg1, double[] reg2) {

      if ( ! useSeparation ) return true;

      if (p2[1] < 0 && p2[1] < p1[1]) return false;

      return true;
   }

   public boolean atFinish(double[] origin, int[] reg1, int[] reg2) {
      return false;
   }

   public void setBuffer(LineBuffer buf) {
      this.buf  = buf;
   }

   public void animate() {
      engine.fall();
   }

   public void render(double[] origin) {
      buf.clear();
      Vec.copy(this.origin,origin);
      for (int i=0; i<shapes.length; i++) {
         shapesVis[i] = true;
         if (shapes[i] == null) shapesVis[i] = false;
         Vec.sub(reg1,origin,shapes[i].shapecenter);
         if (Vec.norm(reg1) > depth) shapesVis[i] = false;
      }

      // calcViewBoundaries is expensive, but we always need the boundaries
      // to draw the scenery correctly
      currentDraw = buf;
      for (int i=0; i<shapes.length; i++) {
         if (!shapesVis[i]) continue;
         calcVisShape(shapes[i]);
         clipUnits[i].setBoundaries(Clip.calcViewBoundaries(origin,shapes[i]));
         currentDraw = clipUnits[i].chain(currentDraw); // set up for floor drawing
      }

      // currentDraw includes all objects, scenery must be distant
      for (int i=0; i<scenery.size(); i++) {
         ((IScenery) scenery.get(i)).draw(currentDraw,origin);
      }

      calcInFront();

      for (int i=0; i<shapes.length; i++) {
         if (!shapesVis[i]) continue;
         currentDraw = buf;
         for (int h=0; h<shapes.length; h++) {
            if (!shapesVis[h]) continue;
            if (inFront[h][i]) currentDraw = clipUnits[h].chain(currentDraw);
         }
         drawShape(shapes[i]);
      }
   }

   private void calcVisShape(Geom.Shape shape) {
      for (int i=0; i<shape.face.length; i++) calcVisFace(shape.face[i]);
   }

   private void calcVisFace(Geom.Face face) {
      if (face.normal != null) {

         // late to be adding an epsilon here, but without this you can sometimes
         // see a flat face between two other faces that ought to be in contact.
         // the example is geom3-project4b (delete the front face and slide right).
         final double epsilon = -1e-9;

         Vec.sub(reg1,face.center,origin); // vector from origin to face center
         face.visible = (Vec.dot(reg1,face.normal) < epsilon) ^ invertNormals;
      } else {
         face.visible = true; // glass
      }
   }

   private void drawShape(Geom.Shape shape) {
      for (int i=0; i<shape.face.length; i++) drawFace(shape,shape.face[i]);
   }

   private void drawFace(Geom.Shape shape, Geom.Face face) {

      if ( ! face.visible ) return;

      if (texture[0]) {
         if (useEdgeColor) drawEdgeColor(shape,face,1);
         else drawTexture(shape,face,Color.white,1);
      }

      boolean selected = (shape == selectedShape) && ! hideSel;
      Color faceColor = Geom.getColor(face.color);

      if (face.customTexture != null) {
         face.customTexture.draw(shape,face,currentDraw,origin);
      } else {
         for (int i=1; i<10; i++) {
            if (i == 5 && selected) continue;
            if (texture[i]) drawTexture(shape,face,faceColor,0.1*i);
         }
      }

      if (selected) {
         Color color = faceColor.equals(COLOR_SELECTED) ? COLOR_SELECTED_ALTERNATE : COLOR_SELECTED;
         drawTexture(shape,face,color,0.5);
         // slightly different behavior than in RenderAbsolute:
         // change to alternate color even if texture 5 not on.
      }
   }

   private static Color COLOR_SELECTED           = Color.yellow;
   private static Color COLOR_SELECTED_ALTERNATE = Color.red;

   private void drawEdgeColor(Geom.Shape shape, Geom.Face face, double scale) {
      for (int i=0; i<face.ie.length; i++) {
         Geom.Edge edge = shape.edge[face.ie[i]];
         Vec.mid(reg1,face.center,shape.vertex[edge.iv1],scale);
         Vec.mid(reg2,face.center,shape.vertex[edge.iv2],scale);
         currentDraw.drawLine(reg1,reg2,Geom.getColor(edge.color,face.color),origin);
      }
   }

   private void drawTexture(Geom.Shape shape, Geom.Face face, Color color, double scale) {
      for (int i=0; i<face.ie.length; i++) {
         Geom.Edge edge = shape.edge[face.ie[i]];
         Vec.mid(reg1,face.center,shape.vertex[edge.iv1],scale);
         Vec.mid(reg2,face.center,shape.vertex[edge.iv2],scale);
         currentDraw.drawLine(reg1,reg2,color,origin);
      }
   }

}

