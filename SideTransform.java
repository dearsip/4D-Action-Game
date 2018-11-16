/*
 * SideTransform.java
 */

/**
 * A helper class for RenderRelative and OptionsFisheye.
 */

public class SideTransform implements PointTransform {

   private double[] offset;
   private int saxis;
   private int ssign;

   public SideTransform(double[] offset) {
      this.offset = offset;
   }

   public void configure(int saxis, int ssign) {
      this.saxis = saxis;
      this.ssign = ssign;
   }

   public void transform(double[] p) {
      double temp = offset[saxis] + OptionsFisheye.of.scale1*p[saxis]; // cf. addScaled
      double scale2 = OptionsFisheye.of.scale2a + ssign*OptionsFisheye.of.scale2b*p[saxis];
      Vec.addScaled(p,offset,p,scale2);
      p[saxis] = temp; // overwriting is easier than creating a new variant of addScaled
   }

}

