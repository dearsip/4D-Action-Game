/*
 * PanelLine.java
 */

import java.awt.image.BufferedImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel that displays a set of colored lines.<p>
 *
 * The panel remembers and redraws the lines it has been given,
 * but does not perform any kind of clipping.
 * The coordinates should be in the range [-1,1],
 * with (-1,-1) being the <i>lower</i> left.<p>
 *
 * The lines are drawn into the full extent of the panel, regardless of aspect ratio.
 * If you want a 1:1 ratio, you must arrange for the panel to be square.
 */

public class PanelLine extends JComponent {

// --- fields ---

   private LineBuffer buf;
   private boolean redraw;
   private BufferedImage image;

// --- construction ---

   public PanelLine() {
      setOpaque(true);
      setBackground(Color.black);

      buf = new LineBuffer(2);
      redraw = true;
      image = null; // this also causes redraw
   }

// --- methods ---

   private void adjust(double[] dest, double[] src) {
      dest[0] = (1+src[0])/2; // convert to [0,1]
      dest[1] = (1-src[1])/2;
   }

   public void setLines(LineBuffer in) {
      buf.clear();

      for (int i=0; i<in.size(); i++) {
         Line src = in.get(i);
         Line dest = buf.getNext();

         adjust(dest.p1,src.p1);
         adjust(dest.p2,src.p2);
         dest.color = src.color;
      }

      redraw = true;
      repaint();
   }

// --- painting ---

   // for reasons that are mysterious to me, as of Java 1.5
   // it's a <i>lot</i> faster to draw into a buffer
   // and call drawImage than to draw directly to the screen.
   // so, that's what we do.
   //
   // before, the background color was handled by the JPanel
   // UI delegate, but now there's no need for that.
   // so, the call to super.paintComponent has gone away and
   // the inheritance has changed from JPanel to JComponent.
   //
   // to add to the mystery, another difference between JPanel
   // and JComponent is that the latter isn't double-buffered.
   // so, I wasn't even drawing directly to the screen, before,
   // but it was still really slow.

   public void paintComponent(Graphics g) {

      if (    image == null
           || image.getWidth () != getWidth ()
           || image.getHeight() != getHeight() ) {

         image = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
         redraw = true;
      }

      if (redraw) { // lines have changed or image has changed

         draw(image.getGraphics());
         redraw = false;
      }

      g.drawImage(image,0,0,null);
   }

   private void draw(Graphics g) {

      // an opaque component is supposed to paint its entire region,
      // even if there are borders.  (try with EmptyBorder and see)
      // we could test isBorderOpaque, but it's not worth the added complexity.
      g.setColor(getBackground());
      g.fillRect(0,0,getWidth(),getHeight());

      Insets insets = getInsets(); // handle borders correctly
      int w = getWidth () - insets.left - insets.right;
      int h = getHeight() - insets.top  - insets.bottom;

      g.translate(insets.left,insets.top);

      // draw lines in two passes, so that intersections in 3D
      // (which are always with a colored line terminating in a white one)
      // show white regardless of order in which the lines were created

      for (int i=0; i<buf.size(); i++) {
         Line line = buf.get(i);
         if (line.color == Color.white) continue;

         g.setColor(line.color);
         g.drawLine((int) (line.p1[0]*w), (int) (line.p1[1]*h),
                    (int) (line.p2[0]*w), (int) (line.p2[1]*h));
      }

      for (int i=0; i<buf.size(); i++) {
         Line line = buf.get(i);
         if (line.color != Color.white) continue;

         g.setColor(line.color);
         g.drawLine((int) (line.p1[0]*w), (int) (line.p1[1]*h),
                    (int) (line.p2[0]*w), (int) (line.p2[1]*h));
      }

      g.translate(-insets.left,-insets.top); // this isn't necessary
      // now that we're drawing into a buffer, but it's easy and polite
   }

}

