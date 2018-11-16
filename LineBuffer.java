/*
 * LineBuffer.java
 */

import java.awt.Color;
import java.util.Vector;

/**
 * A line buffer that reuses line objects to avoid memory allocation.
 */

public class LineBuffer implements IDraw {

// --- fields ---

   private int dim;
   private Vector lines;
   private int size;

// --- construction ---

   public LineBuffer(int dim) {
      this.dim = dim;
      lines = new Vector();
      size = 0;
   }

// --- methods for writing to the buffer ---

   public void clear() {
      size = 0;
      // do not clear the vector, we reuse the elements
   }

   /**
    * Get a line object to write into, allocating a new one if necessary.
    */
   public Line getNext() {
      Line line;

      if (size < lines.size()) {
         line = (Line) lines.get(size);

      } else { // size == lines.size()
         line = new Line(new double[dim],new double[dim],null);
         lines.add(line);
      }

      size++;
      return line;
   }

   /**
    * Back up after a call to getNext turns out to be unnecessary.
    */
   public void unget() {
      size--;
   }

   /**
    * Add a line to the buffer.
    * Ownership of the arguments p1 and p2 is not transferred,
    * so it is OK to keep and modify them after calling add.
    */
   public void add(double[] p1, double[] p2, Color color) {
      Line line = getNext();

      System.arraycopy(p1,0,line.p1,0,dim);
      System.arraycopy(p2,0,line.p2,0,dim);
      line.color = color;
   }

   public void drawLine(double[] p1, double[] p2, Color color, double[] origin) {
      Line line = getNext();

      Vec.sub(line.p1,p1,origin);
      Vec.sub(line.p2,p2,origin);
      line.color = color;
   }

// --- methods for reading from the buffer ---

   public int size() {
      return size;
   }

   public Line get(int i) {
      return (Line) lines.get(i);
   }

}

