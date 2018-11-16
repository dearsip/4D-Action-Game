/*
 * MainTransform.java
 */

/**
 * A helper class for RenderRelative and OptionsFisheye.
 */

public class MainTransform implements PointTransform {

   private double[] offset;

   public MainTransform(double[] offset) {
      this.offset = offset;
   }

   public void transform(double[] p) {
      Vec.addScaled(p,offset,p,OptionsFisheye.of.scale0);
   }

}

