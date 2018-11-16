/*
 * ISelectShape.java
 */

import java.awt.Color;
import java.util.Vector;

/**
 * An interface to connect DialogSelectShape to GeomModel.
 */

public interface ISelectShape {

   // these are vectors of NamedObject
   Vector getAvailableColors();
   Vector getAvailableShapes();

   Color      getSelectedColor();
   Geom.Shape getSelectedShape();
   void setSelectedColor(Color      color);
   void setSelectedShape(Geom.Shape shape);

   // special color objects that we recognize by object identity
   final Color RANDOM_COLOR = new Color(0);
   final Color REMOVE_COLOR = new Color(0);

   // ISelectPaint
   Color getPaintColor();
   void  setPaintColor(Color color);
   int  getPaintMode();
   void setPaintMode(int mode);

}

