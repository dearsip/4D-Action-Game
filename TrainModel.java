/*
 * TrainModel.java
 */

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A model that lets the user move around geometric shapes.
 */

public class TrainModel extends GeomModel {

   private Track track;
   private Train[] trains;
   private int velNumber;

   public TrainModel(int dim, Geom.Shape[] shapes, Struct.DrawInfo drawInfo, Struct.ViewInfo viewInfo,
                     Track track, Train[] trains) throws Exception {

      super(dim,join(shapes,getShapes(trains)),drawInfo,viewInfo);
      this.track = track;
      this.trains = trains;
      this.velNumber = 0; // initial state is stopped
   }

   public static void init(Track track, Train[] trains) throws Exception {
      for (int i=0; i<trains.length; i++) {
         trains[i].init(track);
      }
      // split this out, it has to happen before GeomModel constructor
   }

   public static Geom.Shape[] join(Geom.Shape[] shapes, LinkedList list) {
      Geom.Shape[] result = new Geom.Shape[shapes.length + list.size()];
      int n = 0;

      for (int i=0; i<shapes.length; i++) {
         result[n++] = shapes[i];
      }

      Iterator j = list.iterator();
      while (j.hasNext()) {
         result[n++] = (Geom.Shape) j.next();
      }

      return result;
   }

   public static LinkedList getShapes(Train[] trains) {
      LinkedList list = new LinkedList();
      for (int i=0; i<trains.length; i++) {
         trains[i].getShapes(list);
      }
      return list;
   }

   public boolean isAnimated() {
      return true;
   }

   public int getSaveType() {
      return IModel.SAVE_NONE;
   }

   public void animate() {
      double d = velNumber*track.getVelStep();
      if (d == 0) return;
      for (int i=0; i<trains.length; i++) {
         boolean ok = (d > 0) ? trains[i].moveForward(d) : trains[i].moveReverse(-d);
         // ignore the result; if we bonk at a dead end that's OK.
         // the trouble is, when there are multiple trains,
         // we don't want to stop them all if one hits a dead end.
      }
   }

// --- implementation of IKeysNew ---

   public void adjustSpeed(int dv) {
      if (dv == 0) velNumber = 0; // not really dv in this case
      else velNumber += dv;
   }

   public void toggleTrack() {
      track.toggleTrack();
   }

   // GeomModel handles toggleEdgeColor

   protected void clickNoShape(double[] origin, double[] viewAxis) {
      track.click(origin,viewAxis);
   }

   protected void clickNoUserMove(Geom.Shape shape, double[] origin, double[] viewAxis) {
      Track.TileTexture tt = Platform.getTileTexture(shape);
      if (tt != null) track.click(origin,viewAxis,tt);
   }

}

