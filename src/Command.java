/*
 * Command.java
 */

import java.awt.Color;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * Utility class containing built-in commands.
 */

public class Command {

// --- helpers ---

   // numbers are all stored as Double on the stack

   private static int toInt(Object o) { return ((Double) o).intValue(); }
   private static double toDbl(Object o) { return ((Double) o).doubleValue(); }
   private static boolean toBool(Object o) { return ((Boolean) o).booleanValue(); }

   private static int[] toIntArray(double[] d) {
      int[] a = new int[d.length];
      for (int i=0; i<d.length; i++) a[i] = (int) d[i];
      return a;
   }

   private static boolean[] toBoolArray(double[] d) {
      boolean[] a = new boolean[d.length];
      for (int i=0; i<d.length; i++) a[i] = (d[i] != 0); // so basically 0 / 1
      return a;
   }

// --- context commands ---

   public static class Dup implements ICommand { // stack copy
      public void exec(Context c) throws Exception {
         c.stack.push(c.stack.peek());
      }
   }

   public static class Exch implements ICommand {
      public void exec(Context c) throws Exception {
         Object o2 = c.stack.pop();
         Object o1 = c.stack.pop();
         c.stack.push(o2);
         c.stack.push(o1);
      }
   }

   public static class Pop implements ICommand {
      public void exec(Context c) throws Exception {
         c.stack.pop();
      }
   }

   public static class Index implements ICommand {
      public void exec(Context c) throws Exception {
         int i = toInt(c.stack.pop());
         c.stack.push(c.stack.get(c.stack.size()-1-i));
      }
   }

   public static class Def implements ICommand {
      public void exec(Context c) throws Exception {
         String s = (String) c.stack.pop();
         Object o = c.stack.pop();
         c.dict.put(s,o);
         if (c.isTopLevel()) {
            c.topLevelDef.add(s);
         } else {
            c.topLevelDef.remove(s);
         }
         // if there are any shape defs in DefaultContext,
         // they'll correctly be counted as not top level
      }
   }

// --- math commands ---

   public static class Add implements ICommand {
      public void exec(Context c) throws Exception {
         double d2 = toDbl(c.stack.pop());
         double d1 = toDbl(c.stack.pop());
         c.stack.push(new Double(d1+d2));
      }
   }

   public static class Sub implements ICommand {
      public void exec(Context c) throws Exception {
         double d2 = toDbl(c.stack.pop());
         double d1 = toDbl(c.stack.pop());
         c.stack.push(new Double(d1-d2));
      }
   }

   public static class Mul implements ICommand {
      public void exec(Context c) throws Exception {
         double d2 = toDbl(c.stack.pop());
         double d1 = toDbl(c.stack.pop());
         c.stack.push(new Double(d1*d2));
      }
   }

   public static class Div implements ICommand {
      public void exec(Context c) throws Exception {
         double d2 = toDbl(c.stack.pop());
         double d1 = toDbl(c.stack.pop());
         c.stack.push(new Double(d1/d2));
      }
   }

   public static class Neg implements ICommand {
      public void exec(Context c) throws Exception {
         double d = toDbl(c.stack.pop());
         c.stack.push(new Double(-d));
      }
   }

// --- array commands ---

   private static Object arrayMark = new Object();

   public static class ArrayStart implements ICommand {
      public void exec(Context c) throws Exception {
         c.stack.push(arrayMark);
      }
   }

   public static class ArrayEnd implements ICommand {
      public void exec(Context c) throws Exception {
         int len = c.stack.search(arrayMark)-1;
         if (len < 0) throw new Exception("Array start not found.");
         Class cl = c.stack.peek().getClass();
         Object array;
         if (cl == Double.class) { // special handling, convert to double
            double[] d = new double[len];
            array = d;
            for (int i=len-1; i>=0; i--) d[i] = toDbl(c.stack.pop());
         } else {
            array = Array.newInstance(cl,len);
            for (int i=len-1; i>=0; i--) Array.set(array,i,c.stack.pop());
         }
         c.stack.pop(); // remove mark
         c.stack.push(array);
      }
   }

// --- include ---

   public static class Include implements ICommand {
      public void exec(Context c) throws Exception {
         String filename = (String) c.stack.pop();
         Language.include(c,filename);
      }
   }

// --- setup commands ---

   public static class ViewInfo implements ICommand {
      public void exec(Context c) throws Exception {
         Struct.ViewInfo vi = new Struct.ViewInfo();
         vi.axis = (double[][]) c.stack.pop();
         vi.origin = (double[]) c.stack.pop();
         c.stack.push(vi);
      }
   }

   public static class DrawInfo implements ICommand {
      public void exec(Context c) throws Exception {
         Struct.DrawInfo di = new Struct.DrawInfo();
         di.useEdgeColor = toBool(c.stack.pop());
         di.texture = toBoolArray((double[]) c.stack.pop());
         c.stack.push(di);
      }
   }

// --- shape definition ---

   public static class Null implements ICommand {
      public void exec(Context c) throws Exception {
         c.stack.push(null);
      }
   }

   public static class Edge implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Edge e = new Geom.Edge();
         e.iv2 = toInt(c.stack.pop());
         e.iv1 = toInt(c.stack.pop());
         c.stack.push(e);
      }
   }

   public static class ColorEdge implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Edge e = new Geom.Edge();
         e.color = (Color) c.stack.pop();
         e.iv2 = toInt(c.stack.pop());
         e.iv1 = toInt(c.stack.pop());
         c.stack.push(e);
      }
   }

   public static class Face implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Face f = new Geom.Face();

         f.normal = (double[]) c.stack.pop(); // can stay null if we can't be bothered

         // center stays null, calculated later

         f.ie = toIntArray((double[]) c.stack.pop());

         c.stack.push(f);
      }
   }

   public static class Shape implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Face[] face = (Geom.Face[]) c.stack.pop();
         Geom.Edge[] edge = (Geom.Edge[]) c.stack.pop();
         double[][] vertex = (double[][]) c.stack.pop();
         Geom.Shape s = new Geom.Shape(face,edge,vertex);
         c.stack.push(s);
      }
   }

   public static class Texture implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Texture t = new Geom.Texture();
         t.edge = (Geom.Edge[]) c.stack.pop();
         t.vertex = (double[][]) c.stack.pop();
         c.stack.push(t);
      }
   }

   public static class ShapeTexture implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Shape s = (Geom.Shape) c.stack.pop();
         Geom.Texture t = new Geom.Texture();
         t.edge = s.edge;
         t.vertex = s.vertex;
         c.stack.push(t);
      }
   }

   public static class Glue implements ICommand {
      public void exec(Context c) throws Exception {
         c.stack.push(new Geom.CompositeShape((Geom.ShapeInterface[]) c.stack.pop()));
      }
   }

   public static class Unglue implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.pop();
         shape.unglue(c.stack); // so, no effect on Geom.Shape
         // not sure this command is useful, but it's harmless
      }
   }

// --- shape commands ---

   private static void checkUserMove(Geom.MoveInterface shape) throws Exception {
      if (shape.noUserMove()) throw new Exception("Shape cannot be moved.");
   }

   public static class NoUserMove implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.peek();
         shape.setNoUserMove();
      }
   }

   public static class Idealize implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Shape shape = (Geom.Shape) c.stack.peek();
         shape.idealize();
      }
   }

   public static class Copy implements ICommand { // shape copy
      public void exec(Context c) throws Exception {
         c.stack.push(Language.tryCopy(c.stack.peek()));
      }
   }

   public static class Place implements ICommand {
      public void exec(Context c) throws Exception {
         double[][] a = (double[][]) c.stack.pop();
         double[] d = (double[]) c.stack.pop();
         Geom.Shape shape = (Geom.Shape) c.stack.peek();
         shape.place(d,a);
      }
   }

   public static class Translate implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         Geom.MoveInterface shape = (Geom.MoveInterface) c.stack.peek();
         checkUserMove(shape);
         shape.translate(d);
      }
   }

   public static class Scale implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         Geom.MoveInterface shape = (Geom.MoveInterface) c.stack.peek();
         checkUserMove(shape);
         shape.scale(d);
      }
   }

   public static class AlignCenter implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         Geom.Shape shape = (Geom.Shape) c.stack.peek();
         shape.setAlignCenter(d);
      }
   }

   public static class Rotate implements ICommand {
      private boolean setOrigin;
      public Rotate(boolean setOrigin) { this.setOrigin = setOrigin; }
      public void exec(Context c) throws Exception {
         double[] origin = setOrigin ? (double[]) c.stack.pop() : null;
         double theta = toDbl(c.stack.pop());
         int dir2 = toInt(c.stack.pop());
         int dir1 = toInt(c.stack.pop());
         Geom.MoveInterface shape = (Geom.MoveInterface) c.stack.peek();
         checkUserMove(shape);
         shape.rotate(dir1,dir2,theta,origin);
      }
   }

   public static class Glass implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.peek();
         shape.glass();
      }
   }

   public static class ShapeColor implements ICommand {
      public void exec(Context c) throws Exception {
         Color color = (Color) c.stack.pop();
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.peek();
         shape.setShapeColor(color);
      }
   }

   public static class FaceColor implements ICommand {
      public void exec(Context c) throws Exception {
         Color color = (Color) c.stack.pop();
         int j = toInt(c.stack.pop());
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.peek();
         shape.setFaceColor(j,color,/* xor = */ false);
      }
   }

   public static class EdgeColor implements ICommand {
      public void exec(Context c) throws Exception {
         Color color = (Color) c.stack.pop();
         int j = toInt(c.stack.pop());
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.peek();
         shape.setEdgeColor(j,color);
      }
   }

   public static class FaceTexture implements ICommand {
      public void exec(Context c) throws Exception {
         double[] value = (double[]) c.stack.pop();
         int mode = toInt(c.stack.pop());
         Geom.Texture texture = (Geom.Texture) c.stack.pop();
         int j = toInt(c.stack.pop());
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.peek();
         shape.setFaceTexture(j,texture,mode,value);
      }
   }

   public static class GeneralPolygon implements ICommand {
      public void exec(Context c) throws Exception {
         double[][] vertex = (double[][]) c.stack.pop();
         Geom.Shape shape = GeomUtil.genpoly(vertex);
         c.stack.push(shape);
      }
   }

   public static class EdgeToRadius implements ICommand { // convert "e n" to "r n"
      public void exec(Context c) throws Exception {
         Object o = c.stack.pop(); // might as well save object
         int n = toInt(o);
         double e = toDbl(c.stack.pop());
         double r = GeomUtil.edgeToRadius(e,n);
         c.stack.push(new Double(r));
         c.stack.push(o);
      }
   }

   public static class EdgeToHeight implements ICommand { // convert "r n a [min e]" to "r n a [min max]"
      public void exec(Context c) throws Exception {

         // it's unfortunate that you can't control how the height
         // is distributed between min and max, but it's not worth
         // getting into.  cone has a similar problem.

         // don't pop and re-push the arguments,
         // just peek at everything.
         // we can modify the array in place.

         double[] d = (double[]) c.stack.peek();
         int size = c.stack.size();
         int n    = toInt(c.stack.get(size-3));
         double r = toDbl(c.stack.get(size-4));

         d[1] = d[0] + GeomUtil.edgeToHeight(r,n,d[1]);
      }
   }

   public static class Polygon implements ICommand {
      private double offset;
      public Polygon(double offset) { this.offset = offset; }
      public void exec(Context c) throws Exception {
         int n = toInt(c.stack.pop());
         double r = toDbl(c.stack.pop());
         double[] d = (double[]) c.stack.pop();
         Geom.Shape shape = GeomUtil.polygon(d[0],d[1],r,n,offset);
         c.stack.push(shape);
      }
   }

   public static class Product implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Shape s2 = (Geom.Shape) c.stack.pop();
         Geom.Shape s1 = (Geom.Shape) c.stack.pop();
         c.stack.push(GeomUtil.product(s1,s2));
      }
   }

   public static class Rect implements ICommand {
      public void exec(Context c) throws Exception {
         double[][] d = (double[][]) c.stack.pop();
         c.stack.push(GeomUtil.rect(d));
      }
   }

   public static class Prism implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         int a = toInt(c.stack.pop());
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.pop();
         shape = shape.prism(a,d[0],d[1]);
         c.stack.push(shape);
      }
   }

   public static class Frustum implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         int a = toInt(c.stack.pop());
         double[] p = (double[]) c.stack.pop();
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.pop();
         shape = shape.frustum(p,a,d[0],d[1],d[2]);
         c.stack.push(shape);
      }
   }

   public static class Cone implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         int a = toInt(c.stack.pop());
         double[] p = (double[]) c.stack.pop();
         Geom.ShapeInterface shape = (Geom.ShapeInterface) c.stack.pop();
         shape = shape.cone(p,a,d[0],d[1]);
         c.stack.push(shape);
      }
   }

   public static class Antiprism implements ICommand {
      private double offset;
      public Antiprism(double offset) { this.offset = offset; }
      public void exec(Context c) throws Exception {
         double[] m = (double[]) c.stack.pop();
         int a = toInt(c.stack.pop());
         int n = toInt(c.stack.pop());
         double r = toDbl(c.stack.pop());
         double[] d = (double[]) c.stack.pop();
         Geom.Shape shape = GeomUtil.antiprism(d[0],d[1],r,n,offset,a,m[0],m[1]);
         c.stack.push(shape);
      }
   }

   public static class TrainPoly implements ICommand {
      public void exec(Context c) throws Exception {
         double bb = toDbl(c.stack.pop());
         double bf = toDbl(c.stack.pop());
         double len = toDbl(c.stack.pop());
         Geom.Shape shape = GeomUtil.train(len,bf,bb);
         c.stack.push(shape);
      }
   }

// --- texture commands ---

   public static class TextureColor implements ICommand {
      public void exec(Context c) throws Exception {
         Color color = (Color) c.stack.pop();
         Geom.Texture t = (Geom.Texture) c.stack.peek();
         t.setTextureColor(color);
      }
   }

   public static class Union implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Texture t1 = (Geom.Texture) c.stack.pop();
         Geom.Texture t2 = (Geom.Texture) c.stack.pop();
         c.stack.push(t1.union(t2));
      }
   }

   public static class Merge implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Texture t = (Geom.Texture) c.stack.peek();
         t.merge();
      }
   }

   public static class Normalize implements ICommand {
      public void exec(Context c) throws Exception {
         Geom.Texture t = (Geom.Texture) c.stack.peek();
         t.normalize();
      }
   }

   public static class Lift implements ICommand {
      public void exec(Context c) throws Exception {
         double mid = toDbl(c.stack.pop());
         int a = toInt(c.stack.pop());
         Geom.Texture t = (Geom.Texture) c.stack.pop();
         c.stack.push(GeomUtil.noSplit(t,a,mid));
      }
   }

   public static class Project implements ICommand {
      public void exec(Context c) throws Exception {
         int a = toInt(c.stack.pop());
         Geom.Texture t = (Geom.Texture) c.stack.pop();
         c.stack.push(GeomUtil.project(t,a));
      }
   }

// --- train commands ---

   public static class NewTrack implements ICommand {
      public void exec(Context c) throws Exception {
         double velStep = toDbl(c.stack.pop());
         boolean expand = toBool(c.stack.pop());
         Color colorSel = (Color) c.stack.pop();
         Color color = (Color) c.stack.pop();
         int arcn = toInt(c.stack.pop());
         double margin = toDbl(c.stack.pop());
         double width = toDbl(c.stack.pop());
         double carScale = toDbl(c.stack.pop());
         double carLen = toDbl(c.stack.pop());
         int dim = toInt(c.stack.pop());
         Track track = new Track(dim,carLen,carScale,width,margin,arcn,color,colorSel,expand,velStep);
         c.stack.push(track);
      }
   }

   public static class NewTrack2 implements ICommand {
      public void exec(Context c) throws Exception {
         double vradius = toDbl(c.stack.pop());
         double vtheta = toDbl(c.stack.pop());
         double vscale = toDbl(c.stack.pop());
         double velStep = toDbl(c.stack.pop());
         boolean expand = toBool(c.stack.pop());
         Color colorSel = (Color) c.stack.pop();
         Color color = (Color) c.stack.pop();
         int arcn = toInt(c.stack.pop());
         double margin = toDbl(c.stack.pop());
         double width = toDbl(c.stack.pop());
         double carScale = toDbl(c.stack.pop());
         double carLen = toDbl(c.stack.pop());
         int dim = toInt(c.stack.pop());
         Track track = new Track(dim,carLen,carScale,width,margin,arcn,color,colorSel,expand,velStep,
                                 vscale,vtheta,vradius);
         c.stack.push(track);
      }
   }

   public static class Set implements ICommand {
      private Method m;
      public Set(Class cl, String methodName) {
         this(cl,methodName,Double.TYPE);
      }
      public Set(Class cl, String methodName, Class argClass) {
         try {
            m = cl.getMethod(methodName,new Class[] { argClass });
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         }
      }
      public void exec(Context c) throws Exception {
         Object arg = c.stack.pop();
         Object o = c.stack.peek();
         m.invoke(o,new Object[] { arg });
      }
   }

   public static class AddTrack implements ICommand {
      public void exec(Context c) throws Exception {
         String s = ((String) c.stack.pop()).toLowerCase();
         int dir2 = toInt(c.stack.pop());
         int dir1 = toInt(c.stack.pop());
         int[] pos = toIntArray((double[]) c.stack.pop());
         Track track = (Track) c.stack.peek();
         track.build(pos,dir1,dir2,s);
      }
   }

   public static class AddPlatforms implements ICommand {
      public void exec(Context c) throws Exception {
         int ymax = toInt(c.stack.pop());
         int ymin = toInt(c.stack.pop());
         Track track = (Track) c.stack.pop();
         c.stack.addAll(Platform.createPlatforms(track,ymin,ymax));
         c.stack.push(track); // float up the stack
      }
   }

   public static class AddPlatform implements ICommand {
      private boolean setRounding;
      public AddPlatform(boolean setRounding) { this.setRounding = setRounding; }
      public void exec(Context c) throws Exception {
         int rounding = setRounding ? toInt(c.stack.pop()) : -1;
         int[] max = toIntArray((double[]) c.stack.pop());
         int[] min = toIntArray((double[]) c.stack.pop());
         Track track = (Track) c.stack.pop();
         c.stack.push(Platform.createPlatform(track,min,max,rounding));
         c.stack.push(track); // float up the stack
      }
   }

   public static class RoundShape implements ICommand {
      public void exec(Context c) throws Exception {

      // get parameters

         boolean corner = toBool(c.stack.pop());
         double margin = toDbl(c.stack.pop());
         int rounding = toInt(c.stack.pop());
         int[] max = toIntArray((double[]) c.stack.pop());
         int[] min = toIntArray((double[]) c.stack.pop());

      // compute others

         // note that the y coordinates of min and max will not be used!

         int dim = min.length;
         // could validate against max.length, but rplatform doesn't

         if (min[1] != max[1]) throw new Exception("Inconsistent platform level.");
         // only thing rplatform does that we want

         Arc arc = Arc.curve((dim == 4) ? 3 : 7,false,margin,1);
         // set arcn to 7 in 3D, not worth having more options

      // make the call

         c.stack.push(Platform.getRoundOutline(dim,min,max,rounding,arc,corner));
      }
   }

   public static class AddRamp implements ICommand {
      public void exec(Context c) throws Exception {
         int[] pos = toIntArray((double[]) c.stack.pop());
         Track track = (Track) c.stack.pop();
         c.stack.push(Platform.createRamp(track,pos));
         c.stack.push(track); // float up the stack
      }
   }

   public static class AddPylon implements ICommand {
      private boolean setBase;
      public AddPylon(boolean setBase) { this.setBase = setBase; }
      public void exec(Context c) throws Exception {
         int base = setBase ? toInt(c.stack.pop()) : -1;
         double[] dpos = (double[]) c.stack.pop();
         Track track = (Track) c.stack.pop();
         c.stack.push(Platform.createPylon(track,dpos,base));
         c.stack.push(track); // float up the stack
      }
   }

   public static class TrainCtor implements ICommand {
      public void exec(Context c) throws Exception {
         int trainMode = toInt(c.stack.pop());
         double d0 = toDbl(c.stack.pop());
         int toDir   = toInt(c.stack.pop());
         int fromDir = toInt(c.stack.pop());
         int[] pos = toIntArray((double[]) c.stack.pop());
         double gap = toDbl(c.stack.pop());
         Car[] cars = (Car[]) c.stack.pop();
         Train train = new Train(cars,gap,pos,fromDir,toDir,d0,trainMode);
         c.stack.push(train);
      }
   }

   public static class CarCtor implements ICommand {
      private boolean override;
      public CarCtor(boolean override) { this.override = override; }
      public void exec(Context c) throws Exception {
         double carLenOverride = override ? toDbl(c.stack.pop()) : 0;
         Geom.Shape shape = (Geom.Shape) c.stack.pop();
         Car car = new Car(shape,carLenOverride);
         c.stack.push(car);
      }
   }

// --- scenery ---

   // can use for anything with a no-arg constructor
   public static class Construct implements ICommand {
      private Class cl;
      public Construct(Class cl) { this.cl = cl; }
      public void exec(Context c) throws Exception {
         c.stack.push(cl.newInstance());
      }
   }

   public static class ConstructSetColor implements ICommand {
      private Class cl;
      public ConstructSetColor(Class cl) { this.cl = cl; }
      public void exec(Context c) throws Exception {
         Color color = (Color) c.stack.pop();
         Object o = cl.newInstance();
         ((Mat.SetColor) o).setColor(color);
         c.stack.push(o);
      }
   }

   public static class MeshRing3 implements ICommand {
      public void exec(Context c) throws Exception {
         double offset = toDbl(c.stack.pop());
         int n = toInt(c.stack.pop());
         c.stack.push(Scenery.ring3(n,offset));
      }
   }

   public static class MeshRing4 implements ICommand {
      public void exec(Context c) throws Exception {
         double offset = toDbl(c.stack.pop());
         int n = toInt(c.stack.pop());
         c.stack.push(Scenery.ring4(n,offset));
      }
   }

   public static class MeshSphere4 implements ICommand {
      public void exec(Context c) throws Exception {
         double offset = toDbl(c.stack.pop());
         int n = toInt(c.stack.pop());
         double[] lat = (double[]) c.stack.pop();
         c.stack.push(Scenery.sphere4(lat,n,offset));
      }
   }

   public static class MeshFrame4 implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         c.stack.push(Scenery.frame4(d));
      }
   }

   public static class MeshRingFrame4 implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         c.stack.push(Scenery.ringframe4(d));
      }
   }

   public static class MeshCube4 implements ICommand {
      public void exec(Context c) throws Exception {
         double[] d = (double[]) c.stack.pop();
         c.stack.push(Scenery.cube4(d));
      }
   }

   public static class GroundCube3 implements ICommand {
      public void exec(Context c) throws Exception {
         c.stack.push(Scenery.gcube3());
      }
   }

   public static class GroundCube4 implements ICommand {
      public void exec(Context c) throws Exception {
         c.stack.push(Scenery.gcube4());
      }
   }

   public static class HeightConst implements ICommand {
      public void exec(Context c) throws Exception {
         double height = toDbl(c.stack.pop());
         c.stack.push(new Scenery.HeightConstant(height));
      }
   }

   public static class HeightPower implements ICommand {
      public void exec(Context c) throws Exception {
         double height = toDbl(c.stack.pop());
         double n = toDbl(c.stack.pop());
         c.stack.push(new Scenery.HeightPower(n,height));
      }
   }

   public static class HeightMountain implements ICommand {
      public void exec(Context c) throws Exception {
         double height = toDbl(c.stack.pop());
         double n = toDbl(c.stack.pop());
         double min = toDbl(c.stack.pop());
         double[] pos = (double[]) c.stack.pop();
         c.stack.push(new Scenery.HeightMountain(pos,min,n,height));
      }
   }

   public static class HeightMaxN implements ICommand {
      public void exec(Context c) throws Exception {
         Scenery.HeightFunction[] f = (Scenery.HeightFunction[]) c.stack.pop();
         c.stack.push(new Scenery.HeightMaxN(f));
      }
   }

   public static class ColorConst implements ICommand {
      public void exec(Context c) throws Exception {
         Color color = (Color) c.stack.pop();
         c.stack.push(new Scenery.ColorConstant(color));
      }
   }

   public static class ColorDir implements ICommand {
      public void exec(Context c) throws Exception {
         Color[] color = (Color[]) c.stack.pop();
         c.stack.push(new Scenery.ColorDir(color));
      }
   }

   public static class ColorBlend implements ICommand {
      public void exec(Context c) throws Exception {
         Color[] color = (Color[]) c.stack.pop();
         c.stack.push(new Scenery.ColorBlend(color));
      }
   }

   public static class Compass implements ICommand {
      private int dim;
      public Compass(int dim) { this.dim = dim; }
      public void exec(Context c) throws Exception {
         double width = toDbl(c.stack.pop());
         double length = toDbl(c.stack.pop());
         double radius = toDbl(c.stack.pop());
         Scenery.ColorFunction f = (Scenery.ColorFunction) c.stack.pop();
         c.stack.push(new Scenery.Compass(dim,f,radius,length,width));
      }
   }

   public static class Grid implements ICommand {
      private int dim;
      public Grid(int dim) { this.dim = dim; }
      public void exec(Context c) throws Exception {
         double interval = toDbl(c.stack.pop());
         double base = toDbl(c.stack.pop());
         boolean round = toBool(c.stack.pop());
         double radius = toDbl(c.stack.pop());
         Color color = (Color) c.stack.pop();
         c.stack.push(new Scenery.Grid(dim,color,radius,round,base,interval));
      }
   }

   public static class Ground implements ICommand {
      public void exec(Context c) throws Exception {
         double radius = toDbl(c.stack.pop());
         Color color = (Color) c.stack.pop();
         Geom.Texture mesh = (Geom.Texture) c.stack.pop();
         c.stack.push(new Scenery.Ground(mesh,color,radius));
      }
   }

   public static class GroundTexture implements ICommand {
      public void exec(Context c) throws Exception {
         double[] value = (double[]) c.stack.pop();
         int mode = toInt(c.stack.pop());
         Color color = (Color) c.stack.pop();
         Geom.Texture mesh = (Geom.Texture) c.stack.pop();
         c.stack.push(new Scenery.GroundTexture(mesh,color,mode,value));
      }
   }

   public static class Monolith implements ICommand {
      public void exec(Context c) throws Exception {
         Color color = (Color) c.stack.pop();
         Geom.Texture mesh = (Geom.Texture) c.stack.pop();
         c.stack.push(new Scenery.Monolith(mesh,color));
      }
   }

   public static class Horizon implements ICommand {
      public void exec(Context c) throws Exception {
         Scenery.HeightFunction fClip = (Scenery.HeightFunction) c.stack.pop();
         Scenery.HeightFunction f = (Scenery.HeightFunction) c.stack.pop();
         Color color = (Color) c.stack.pop();
         Geom.Texture mesh = (Geom.Texture) c.stack.pop();
         c.stack.push(new Scenery.Horizon(mesh,color,f,fClip));
      }
   }

   public static class Sky implements ICommand {
      public void exec(Context c) throws Exception {
         Scenery.HeightFunction fClip = (Scenery.HeightFunction) c.stack.pop();
         double[] sunBlend = (double[]) c.stack.pop();
         Color sunColor = (Color) c.stack.pop();
         double[] height = (double[]) c.stack.pop();
         Scenery.ColorFunction f = (Scenery.ColorFunction) c.stack.pop();
         Geom.Texture mesh = (Geom.Texture) c.stack.pop();
         c.stack.push(new Scenery.Sky(mesh,f,height,sunColor,sunBlend,fClip));
      }
   }

   public static class Sun implements ICommand {
      public void exec(Context c) throws Exception {
         double height = toDbl(c.stack.pop());
         Color color = (Color) c.stack.pop();
         Geom.Texture mesh = (Geom.Texture) c.stack.pop();
         c.stack.push(new Scenery.Sun(mesh,color,height));
      }
   }

   public static class FinishInfo implements ICommand {
     public void exec(Context c) throws Exception {
       Struct.FinishInfo fi = new Struct.FinishInfo();
       double[] fn = (double[]) c.stack.pop();
       int[] fni = new int[fn.length];
       double[][] d = new double[fn.length][];
       for (int i=0;i<fn.length;i++) {
         fni[i] = (int)fn[i];
         d[i] = new double[2];
         d[i][0] = fn[i]+0.25;
         d[i][1] = fn[i]+0.75;
       }
       Geom.Shape s = GeomUtil.rect(d);
       s.setShapeColor(Color.yellow);
       s.glass();
       c.stack.push(s);
       fi.finish = fni;
       c.stack.push(fi);
     }
   }
}

