/*
 * ActionModel.java
 */

import java.awt.Color;

/**
 * A model that lets the user walk around geometric shapes.
 */

public class ActionModel extends GeomModel {

   protected double[] reg3;
   protected Engine engine;
   private int[] finish;
   private Geom.Texture foot;
   protected boolean bfoot;
   protected boolean bcompass;
   protected Color[] compassColor;

   public ActionModel(int dim, Geom.Shape[] shapes, Struct.DrawInfo drawInfo, Struct.ViewInfo viewInfo, Struct.FootInfo footInfo, Struct.FinishInfo finishInfo) throws Exception {
      super(dim, shapes, drawInfo, viewInfo);
      if (finishInfo != null) this.finish = finishInfo.finish;
      if (footInfo != null) {
         this.bfoot = footInfo.foot;
         this.bcompass = footInfo.compass;
      }
      reg3 = new double[dim];

      if (bfoot || bcompass) foot = (dim==3) ? setFoot3() : setFoot4();
      if (bcompass) compassColor = (dim==3) ? cColor3 : cColor4;
   }

   public void setEngine(Engine engine) {
      this.engine = engine;
   }

   public int[] retrieveFinish() {
      return finish;
   }

   public int retrieveFoot() {
      int i=0;
      if (bfoot) i+=1;
      if (bcompass) i+=2;
      return i;
   }

   // --- implementation of IKeysNew ---

   public void scramble(boolean alignMode, double[] origin) {}
   public void toggleSeparation() {}
   public void addShapes(int quantity, boolean alignMode, double[] origin, double[] viewAxis) {}
   public void removeShape(double[] origin, double[] viewAxis) {}
   public void toggleNormals() {}
   public void toggleHideSel() {}
   public void paint(double[] origin, double[] viewAxis) {}

   public boolean canAddShapes() { return false; }
   public boolean canPaint() { return false; }

   public void jump() {
      engine.jump();
   }

   // --- implementation of IModel ---

   public boolean getAlignMode(boolean defaultAlignMode) {
      return false;
   }

   public boolean isAnimated() {
      return true;
   }

   public int getSaveType() {
      return IModel.SAVE_ACTION;
   }

   public boolean atFinish(double[] origin, int[] reg1, int[] reg2) {
      int dir = Grid.toCell(reg1, reg2, origin);
      return (                            Grid.equals(reg1,finish)
            || (dir != Dir.DIR_NONE && Grid.equals(reg2,finish) ) );
   }

   public void animate() {
      engine.fall();
   }

   public void render(double[] origin) {
      Vec.unitVector(reg1,1);
      Vec.addScaled(reg3,origin,reg1,0.5);
      renderer(reg3);

      if (bfoot) { drawPoint(origin,Color.green,0.001); }
      if (bcompass) {
         origin[1] += 0.5;
         for (int i=0; i<2*(dim-1); i++) {
            int j = (i<2) ? i : i+2;
            Dir.apply(j,origin,0.5);
            drawPoint(origin,compassColor[i],0.05);
            Dir.apply(j,origin,-0.5);
         }
         origin[1] -= 0.5;
      }
   }

   private void drawPoint(double[] center, Color c, double s) {
      for (int i=0; i<foot.edge.length; i++) {
         Geom.Edge edge = foot.edge[i];

         Vec.addScaled(reg1,center,foot.vertex[edge.iv1],s);
         Vec.addScaled(reg2,center,foot.vertex[edge.iv2],s);

         buf.drawLine(reg1,reg2,c,reg3);
      }
   }


   public Clip.Result getResult() {
      return clipResult;
   }

   private Geom.Texture setFoot3() {
      Geom.Edge[] edge = new Geom.Edge[fedge3.length];
      for (int i=0;i<edge.length;i++) edge[i] = new Geom.Edge(fedge3[i][0],fedge3[i][1]);
      return new Geom.Texture(edge,fvertex3);
   }

   private Geom.Texture setFoot4() {
      Geom.Edge[] edge = new Geom.Edge[fedge4.length];
      for (int i=0;i<edge.length;i++) edge[i] = new Geom.Edge(fedge4[i][0],fedge4[i][1]);
      return new Geom.Texture(edge,fvertex4);
   }

   private Color[] cColor3 = { Color.magenta, new Color(128,0,128), Color.cyan, new Color(0,128,128) };
   private Color[] cColor4 = { Color.magenta, new Color(128,0,128), Color.yellow, new Color(128,128,0), Color.cyan, new Color(0,128,128) };
   private int[][] fedge3 = { { 0,1 },{ 1,2 },{ 2,3 },{ 3,0 },{ 0,4 },{ 1,4 },{ 2,4 },{ 3,4 },{ 0,5 },{ 1,5 },{ 2,5 },{ 3,5 } };
   private int[][] fedge4 = { { 0,1 },{ 1,2 },{ 2,3 },{ 3,0 },{ 0,4 },{ 1,4 },{ 2,4 },{ 3,4 },{ 0,5 },{ 1,5 },{ 2,5 },{ 3,5 },{ 0,6 },{ 1,6 },{ 2,6 },{ 3,6 },{ 4,6 },{ 5,6 },{ 0,7 },{ 1,7 },{ 2,7 },{ 3,7 },{ 4,7 },{ 5,7 } };
   private double[][] fvertex3 = { { 1,0,0 },{ 0,0,1 },{ -1,0,0 },{ 0,0,-1 },{ 0,1,0 },{ 0,-1,0 } };
   private double[][] fvertex4 = { { 1,0,0,0 },{ 0,0,1,0 },{ -1,0,0,0 },{ 0,0,-1,0 },{ 0,0,0,1 },{ 0,0,0,-1 },{ 0,1,0,0 },{ 0,-1,0,0 } };
}
