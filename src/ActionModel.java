/*
 * ActionModel.java
 */

import java.awt.Color;

/**
 * A model that lets the user walk around geometric shapes.
 */

public class ActionModel extends GeomModel {

  private double[] reg3;
  private Engine engine;
  private int[] finish;
  private Geom.Texture foot;

  public ActionModel(int dim, Geom.Shape[] shapes, Struct.DrawInfo drawInfo, Struct.ViewInfo viewInfo, Struct.FinishInfo finishInfo) throws Exception {
    super(dim, shapes, drawInfo, viewInfo);
    this.finish = finishInfo.finish;
    reg3 = new double[dim];

    foot = (dim==3) ? setFoot3() : setFoot4();
  }

  public void setEngine(Engine engine) {
    this.engine = engine;
  }

// --- implementation of IKeysNew ---

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

  public boolean canPaint() {
    return false;
  }

  public void paint(double[] origin, double[] viewAxis) {
  }

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

    Color c = Color.green;
    for (int i=0; i<foot.edge.length; i++) {
      Geom.Edge edge = foot.edge[i];

      Vec.add(reg1,origin,foot.vertex[edge.iv1]);
      Vec.add(reg2,origin,foot.vertex[edge.iv2]);

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

  private int[][] fedge3 = { { 0,1 },{ 1,2 },{ 2,3 },{ 3,0 } };
  private int[][] fedge4 = { { 0,1 },{ 1,2 },{ 2,3 },{ 3,0 },{ 0,4 },{ 1,4 },{ 2,4 },{ 3,4 },{ 0,5 },{ 1,5 },{ 2,5 },{ 3,5 } };
  private double[][] fvertex3 = { { 0.001,0,0 },{ 0,0,0.001 },{ -0.001,0,0 },{ 0,0,-0.001 } };
  private double[][] fvertex4 = { { 0.001,0,0,0 },{ 0,0,0.001,0 },{ -0.001,0,0,0 },{ 0,0,-0.001,0 },{ 0,0,0,0.001 },{ 0,0,0,-0.001 } };
}
