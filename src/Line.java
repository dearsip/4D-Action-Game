/*
 * Line.java
 */

import java.awt.Color;

/**
 * An object that represents a colored line in some number of dimensions.
 */

public class Line {

// --- fields ---

   public double[] p1;
   public double[] p2;
   public Color color;

// --- construction ---

   public Line() {
   }

   public Line(double[] p1, double[] p2, Color color) {
      this.p1 = p1;
      this.p2 = p2;
      this.color = color;
   }

}

