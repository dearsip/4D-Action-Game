/*
 * StandEnemy.java
 */

import java.util.Random;

/**
 * Standard enemy objects.
 */

public class StandEnemy extends Enemy {

   protected boolean shoot;
   protected boolean move;
   protected Random random;
   protected int wshoot;
   protected int wmove;
   protected double[] walk;
   protected double[] reg1;
   protected double[] reg2;
   protected double[] reg3;

   public StandEnemy(Geom.Shape shape, boolean shoot, boolean move) {
      super(shape);
      this.shoot = shoot;
      this.move = move;
      random = new Random();
      wshoot = 30;
      wmove = 0;
      if (move) shape.setNoUserMove();
      int dim = shape.getDimension();
      walk = new double[dim];
      reg1 = new double[dim];
      reg2 = new double[dim];
      reg3 = new double[dim-1];
   }

   public void move() {
      if (shoot) {
         if (wshoot == 0) {
            Vec.sub(reg1,model.getOrigin(reg1),shape.aligncenter);
            model.addBullet(shape.aligncenter,reg1,0.2);
            wshoot = 30 + random.nextInt(20);
         }
         wshoot--;
      }
      if (move) {
         if (wmove == 0) {
            Vec.randomNormalized(reg3,random);
            Vec.scale(reg3,reg3,0.03);
            for (int i=0; i<reg3.length; i++) {
               walk[(i+2)%walk.length] = reg3[i];
            }
            wmove = 50 + random.nextInt(60);
         }
         shape.translate(walk);
         if (!model.isSeparated(shape,model.getOrigin(reg1))) {
            Vec.scale(reg1,walk,-1);
            shape.translate(reg1);
         }
         wmove--;
      }
   }

   public boolean hit() { return true; }

}
