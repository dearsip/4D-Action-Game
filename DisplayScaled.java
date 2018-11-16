/**
 * DisplayScaled.java
 */

/**
 * An object that displays a two-dimensional retina in scaled form.
 */

public class DisplayScaled extends Display {

// --- fields ---

   private LineBuffer in;
   private LineBuffer out;

   private double scale;

// --- construction ---

   public DisplayScaled(LineBuffer in, LineBuffer out, double scale) {
      this.in = in;
      this.out = out;

      setScale(scale);
   }

// --- options ---

   public void setScale(double scale) {
      this.scale = scale;
   }

   public void setOptions(double scale, OptionsStereo os) {
      setScale(scale);
      // the other arguments don't matter here
   }

// --- processing ---

   private void convert(double[] dest, double[] src) {
      Vec.scale(dest,src,scale);
   }

   public void run() {
      out.clear();
      for (int i=0; i<in.size(); i++) {
         Line src = in.get(i);
         Line dest = out.getNext();

         convert(dest.p1,src.p1);
         convert(dest.p2,src.p2);
         dest.color = src.color;
      }
   }

}

