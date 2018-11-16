/*
 * PostScriptPrinter.java
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * An object that prints lines to a print stream in PostScript format.
 */

public class PostScriptPrinter {

// --- fields ---

   private PrintStream out;
   private DecimalFormat decimalFormat;

   private int edge;
   private int gap;
   private double resolution; // pixels per stereo unit
   private OptionsImage oi;

   private boolean first;
   private Color color;

// --- construction ---

   /**
    * @param edge The edge length of a single frame, in pixels.
    * @param gap  The gap between frames, in pixels.
    * @param screenWidth The screen width, in stereo units.
    */
   public PostScriptPrinter(PrintStream out, int edge, int gap, double screenWidth, OptionsImage oi) {
      this.out = out;

      decimalFormat = new DecimalFormat("0.####");
      // if we were printing a 4" image at 600 dpi,
      // there would be 2400 dots.
      // four digits of precision on the coordinates [-1,1]
      // lets us specify 20000, which is plenty

      this.edge = edge;
      this.gap  = gap;
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      resolution = screenSize.width / screenWidth;
      this.oi = oi;

      first = true;
      color = null;
   }

// --- helpers ---

   private String format(boolean b) {
      return b ? "true" : "false";
   }

   private String format(double d) {
      return decimalFormat.format(d);
   }

   /**
    * Convert a number of pixels into a distance in scaled coordinates.
    */
   private double scale(int pixels) {
      // the scaled coordinates fall in the range [-1,1],
      // so an edge is two units, and the number of pixels per unit is edge/2
      return pixels / (edge / (double) 2);
   }

   /**
    * Convert 0-255 into 0-1 in a way that produces round numbers.
    */
   private double convert(int c) {
      return (c == 255) ? 1 : (c / (double) 256);
   }

// --- methods ---

   public void printHeader(int n) {

      int width = n*edge + (n-1)*gap;

   // EPSF header

      final int POINTS_PER_INCH = 72; // points are standard PostScript units

      // so, here's what we have
      //
      // edge              pixels
      // gap               pixels
      //
      // POINTS_PER_INCH   points per inch
      // resolution        pixels per stereo unit
      // oi.oneInch        stereo units per inch

      double pointsPerPixel = POINTS_PER_INCH / (resolution * oi.oneInch);
      double edgePoints  = edge  * pointsPerPixel;
      double widthPoints = width * pointsPerPixel;

      out.println("%!PS-Adobe-3.0 EPSF-3.0");
      out.println("%%BoundingBox: 0 0 " + format(widthPoints) + " " + format(edgePoints));
      out.println("%%Creator: Maze (" + App.getString("URL1") + ")");
      out.println("%%CreationDate: " + new Date());
      out.println("%%EndComments");
      out.println();

   // options

      // put these in the file so that they're easy to change after the fact.
      // of course, if you change oi.oneInch, you have to change the EPSF header too,
      // but at least we can minimize the work required

      out.println("/ib " + format(oi.background == OptionsImage.BACKGROUND_WHITE) + " def");
      out.println("/ic " + format(oi.invertColors) + " def");
      out.println("/cc " + oi.convertColors + " def");
      out.println("/lw " + format(oi.lineWidth) + " def");
      out.println("/oi " + format(oi.oneInch) + " def");
      out.println();

   // parameters

      // bring these up front so that the setup code is invariant

      // we want to scale so that edgePoints/2 is one unit,
      // but we want to factor out oi.oneInch so that it can be changed after the fact
      double edgeFactored = edge * (POINTS_PER_INCH / resolution);
      out.println("/sc " + format(edgeFactored/2) + " def");

      out.println("/wd " + format(scale(width))   + " def");
      out.println("/px " + scale(1) + " def"); // don't format, want mega-precision
      out.println();

   // setup

      out.println("sc oi div dup scale");
      out.println("ib not {0 0 wd 2 rectfill} if");
      out.println("1 1 translate");
      out.println("lw px mul setlinewidth");
      out.println();

   // definitions

      // figure out the foreground color once
      out.println("/fg ib {{0 setgray}} {{1 setgray}} ifelse bind def");

      // the rest depends on whether we're in black-and-white mode

      // if we're doing black-and-white, we will set the color now,
      // and the color-setting function will just drop its arguments.
      // note hardcoded value of OptionsImage.CONVERT_B_AND_W

      out.println("cc 2 eq");
      out.println("{");
      out.println("fg /c {pop pop pop} bind def");
      out.print  ("}");

      // otherwise we just put all the options together,
      // doing as much of the conditional stuff as possible up front

      out.println("{");
      out.println("/c1 ic {{3 {1 exch sub 3 1 roll} repeat}} {{}} ifelse bind def");
      out.println("/c2 cc 1 eq {{add add 3 div setgray}} {{setrgbcolor}} ifelse bind def");
      out.println("/c {3 copy add add 3 eq {fg pop pop pop} {c1 c2} ifelse} bind def");
      out.println("}");
      out.println("ifelse");

      out.println("/m { moveto } bind def");
      out.println("/l { lineto stroke } bind def");
      out.println();
   }

   public void print(LineBuffer buf) {

      if (first) {
         first = false;
      } else {
         out.println(format(2+scale(gap)) + " 0 translate");
      }

      out.println("gsave");
      out.println("-1 -1 2 2 rectclip");

      color = null; // force respecification (and blank line)

      for (int i=0; i<buf.size(); i++) {
         Line line = buf.get(i);

         if (color == null || ! line.color.equals(color)) {
            color = line.color;

            out.println();
            out.print  (format(convert(color.getRed()  )) + " ");
            out.print  (format(convert(color.getGreen())) + " ");
            out.println(format(convert(color.getBlue() )) + " c");
         }

         // we could join consecutive line segments,
         // but we would not be joining everything that shares the same point, so don't bother

         out.print  (format(line.p1[0]) + " " + format(line.p1[1]) + " m ");
         out.println(format(line.p2[0]) + " " + format(line.p2[1]) + " l");
      }

      out.println();
      out.println("grestore");
   }

}

