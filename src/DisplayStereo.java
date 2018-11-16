/**
 * DisplayStereo.java
 */

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * An object that uses a realistic model to display a three-dimensional retina in two dimensions.
 */

public class DisplayStereo extends Display {

// --- fields ---

   private LineBuffer in;
   private LineBuffer out;
   private double windowSide;

   private boolean active;
   private double scaleWindow; // real-space scales
   private double scaleRetina;
   private double[] center; // real-space vector from eye to window center
   private double[][] axis; // axes of retina cube
   private double[] reg1; // temporary register

   private double scaleCache; // cache only used to allow single-parameter changes
   private OptionsStereo osCache;
   private int edgeCache;

// --- construction ---

   public DisplayStereo(LineBuffer in, LineBuffer out, double windowSide, double scale, OptionsStereo os, int edge) {
      this.in = in;
      this.out = out;
      this.windowSide = windowSide;

      center = new double[3];
      axis = new double[3][3];
      reg1 = new double[3];

      osCache = new OptionsStereo();

      setOptions(scale,os,edge);
   }

// --- options ---

   public void setScale(double scale) {
      scaleCache = scale;
      configure(scaleCache,osCache,edgeCache);
   }

   public void setScreenWidth(double screenWidth) {
      osCache.screenWidth = screenWidth;
      configure(scaleCache,osCache,edgeCache);
   }

   public void setScreenDistance(double screenDistance) {
      osCache.screenDistance = screenDistance;
      configure(scaleCache,osCache,edgeCache);
   }

   public void setEyeSpacing(double eyeSpacing) {
      osCache.eyeSpacing = eyeSpacing;
      configure(scaleCache,osCache,edgeCache);
   }

   public void setTiltVertical(double tiltVertical) {
      osCache.tiltVertical = tiltVertical;
      configure(scaleCache,osCache,edgeCache);
   }

   public void setTiltHorizontal(double tiltHorizontal) {
      osCache.tiltHorizontal = tiltHorizontal;
      configure(scaleCache,osCache,edgeCache);
   }

   public void setOptions(double scale, OptionsStereo os) {
      scaleCache = scale;
      OptionsStereo.copy(osCache,os);
      configure(scaleCache,osCache,edgeCache);
   }

   public void setOptions(double scale, OptionsStereo os, int edge) {
      scaleCache = scale;
      OptionsStereo.copy(osCache,os);
      edgeCache = edge;
      configure(scaleCache,osCache,edgeCache);
   }

   public void setEdge(int edge) {
      edgeCache = edge;
      configure(scaleCache,osCache,edgeCache);
   }

   private void configure(double scale, OptionsStereo os, int edge) {
      active = (edge != 0); // don't start processing until we know edge length
      if ( ! active ) return;

   // scales

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      double pixels = os.screenWidth / screenSize.width;
      // now N * pixels is a number with real-world dimensions

      scaleWindow = ((double) edge)/2 * pixels;
      scaleRetina = scaleWindow * scale;

   // prevent incorrect projection

      // if a line crosses the projection plane,
      // it ought to go off to infinity (and come in from the other side),
      // but the projection code would do the reverse.
      // worse, if a line ends exactly on the projection plane,
      // the code would get a divide-by-zero error.
      // so, we want to make sure that no lines reach the projection plane.

      // since the retina is a three-dimensional cube,
      // the maximum distance from the center to any point is at the corners,
      // where it's root 3 (times scaleRetina).
      // that must be less than the screen distance.

      if ( ! (scaleRetina * Math.sqrt(3) < os.screenDistance) ) { active = false; return; }

   // center

      double eyeSide = windowSide * (os.cross ? -1 : 1);
      // if eyes are crossed, left eye looks at right window and vice versa

      double windowX = windowSide * ((double) edge)/2 * pixels; // assumes the two windows are adjacent
      double eyeX = eyeSide * os.eyeSpacing/2;

      center[0] = windowX - eyeX;
      center[1] = 0;
      center[2] = -os.screenDistance; // z is outward

   // axes

      Vec.unitMatrix(axis);

      // if you were really looking at a 3D object in front of you,
      // the object would be oriented toward your "center eye".
      // to make the same should be true for stereo viewing,
      // we have to rotate so that z is opposite to the center vector.

      Vec.rotatePoint(axis[2],axis[0],axis[2],axis[0],os.screenDistance,-windowX);

      // now we can tilt the axes as desired

      Vec.rotateAngle(axis[2],axis[0],axis[2],axis[0], os.tiltHorizontal);
      Vec.rotateAngle(axis[1],axis[2],axis[1],axis[2],-os.tiltVertical); // sign convention
   }

// --- processing ---

   /**
    * Convert a three-dimensional point with coordinates in the range [-1,1]
    * into a two-dimensional point with coordinates in the range [-1,1].
    * At least, that's the idea.
    * If the scale is large, the points can be out of bounds;
    * and if the scale is really large, they can cross the projection plane
    * (except that we prevent that with a validation, above).
    */
   private void convert(double[] dest, double[] src) {

      Vec.fromAxisCoordinates(reg1,src,axis);

      Vec.scale(reg1,reg1,scaleRetina);
      Vec.add(reg1,reg1,center);

      // now we have a vector from the eye to the point,
      // and we want to project it into the plane z = -os.screenDistance = center[2];

      Vec.projectDistance(dest,reg1,center[2]);

      Vec.sub(dest,dest,center); // OK that center has different dimension
      Vec.scale(dest,dest,1/scaleWindow);
   }

   public void run() {
      out.clear();
      if ( ! active ) return;

      for (int i=0; i<in.size(); i++) {
         Line src = in.get(i);
         Line dest = out.getNext();

         convert(dest.p1,src.p1);
         convert(dest.p2,src.p2);
         dest.color = src.color;
      }
   }

}

