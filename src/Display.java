/*
 * Display.java
 */

/*
 * A common superclass for display objects.
 */

public abstract class Display {

   public abstract void run();

   public abstract void setScale(double scale);
   public void setScreenWidth(double screenWidth) {};
   public void setScreenDistance(double screenDistance) {};
   public void setEyeSpacing(double eyeSpacing) {};
   public void setTiltVertical(double tiltVertical) {};
   public void setTiltHorizontal(double tiltHorizontal) {};
   public abstract void setOptions(double scale, OptionsStereo os);

   public void setEdge(int edge) {};

}

