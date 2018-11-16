/*
 * RenderRelative.java
 */

import java.awt.Color;

/**
 * An object that takes a set of lines oriented with respect to absolute coordinates
 * and converts them to relative coordinates, i.e., projects them onto a retina.
 */

public class RenderRelative {

// --- fields ---

   private LineBuffer in;
   private LineBuffer out;
   private int dim;

   private double retina;
   private double[][] clip;
   private double[] reg1; // temporary registers
   private double[] reg2;

// --- construction ---

   public RenderRelative(LineBuffer in, LineBuffer out, int dim, double retina) {
      this.in = in;
      this.out = out;
      this.dim = dim;

      clip = new double[2*(dim-1)][dim];
      reg1 = new double[dim];
      reg2 = new double[dim];

      setRetina(retina);
   }

// --- options ---

   public void setRetina(double retina) {
      this.retina = retina;

      int next = 0;
      for (int a=0; a<dim-1; a++) {

         clip[next][a] = 1;
         clip[next][dim-1] = retina;
         next++;

         clip[next][a] = -1;
         clip[next][dim-1] = retina;
         next++;

         // no need to zero other components,
         // they never become nonzero
      }
   }

// --- processing ---

   // the call to projectRetina could cause division by zero
   // if the line being projected started or ended on the parallel plane through the origin.
   // that's not a problem here.
   // the clipping planes restrict the lines to a forward cone,
   // so the origin is the only dangerous point,
   // and we know there are no lines through the origin
   // because we're not allowed to move onto the walls.

   private boolean convert(Line dest, Line src, double[][] axis) {

      Vec.toAxisCoordinates(reg1,src.p1,axis);
      Vec.toAxisCoordinates(reg2,src.p2,axis);

      for (int i=0; i<clip.length; i++) {
         if (Vec.clip(reg1,reg2,clip[i])) return false;
      }

      Vec.projectRetina(dest.p1,reg1,retina);
      Vec.projectRetina(dest.p2,reg2,retina);
      dest.color = src.color;

      return true;
   }

   public void run(double[][] axis) {
      out.clear();
      for (int i=0; i<in.size(); i++) {
         Line src = in.get(i);
         Line dest = out.getNext();

         if ( ! convert(dest,src,axis) ) out.unget();
      }
   }

   public void run(double[][] axis, boolean clear, PointTransform pt) {
      if (clear) out.clear();
      for (int i=0; i<in.size(); i++) {
         Line src = in.get(i);
         Line dest = out.getNext();

         if (convert(dest,src,axis)) {
            pt.transform(dest.p1);
            pt.transform(dest.p2);
         } else {
            out.unget();
         }
      }
   }

   public void runObject(double[][] obj, int mask, PointTransform pt) {
      // no need for clear here
      for (int i=0; i<obj.length; i+=2) {
         if ((mask & 1) == 1) {
            Line dest = out.getNext();

            Vec.copy(dest.p1,obj[i]);
            Vec.copy(dest.p2,obj[i+1]);
            dest.color = Color.white;

            pt.transform(dest.p1);
            pt.transform(dest.p2);
         }
         mask>>=1;
      }
   }

}

