/*
 * ShootModel.java
 */

import java.awt.Color;
import java.util.LinkedList;

public class ShootModel extends ActionModel {

   private int count;
   private Enemy[] enemies;
   private Bullet[] bullets;
   private int[] reg4;
   private boolean dead;

   public ShootModel(int dim, Geom.Shape[] shapes, Struct.DrawInfo drawInfo, Struct.ViewInfo viewInfo, Struct.FootInfo footInfo, Enemy[] enemies) throws Exception {
      super(dim, TrainModel.join(shapes,getShapes(enemies)), drawInfo, viewInfo, footInfo, null);
      this.enemies = enemies;
      for (int i=0; i<enemies.length; i++) {
         enemies[i].setModel(this);
      }
      bullets = new Bullet[16];
      count = enemies.length;
      reg4 = new int[dim];
      dead = false;
   }

   public static LinkedList getShapes(Enemy[] enemies) {
      LinkedList list = new LinkedList();
      for (int i=0; i<enemies.length; i++) {
         list.add(enemies[i].getShape());
      }
      return list;
   }

   public double[] getOrigin(double[] dest) {
      Vec.unitVector(dest,1);
      Vec.addScaled(dest,engine.getOrigin(),dest,0.5);
      return dest;
   }

   // --- implementation of IKeysNew ---

   public boolean canAddShapes() { return true; }

   public void addShapes(int quantity, boolean alignMode, double[] origin, double[] viewAxis) {
      Vec.unitVector(reg3,1);
      Vec.addScaled(reg3,origin,reg3,0.5);
      addBullet(reg3,engine.getAxisArray(),0.3);
   }

   // --- implementation of IModel ---

   public int getSaveType() {
      return IModel.SAVE_SHOOT;
   }

   public boolean atFinish(double[] origin, int[] reg1, int[] reg2) {
      return (count == 0);
   }

   public boolean dead() { return dead; }

   public void animate() {
      engine.fall();
      for (int i = 0; i < enemies.length; i++) {
         if (enemies[i] != null) enemies[i].move();
      }
      for (int i = 0; i < bullets.length; i++) {
         if (bullets[i] != null && bulletMove(bullets[i])) deleteBullet(i);
      }
   }

   // --- bullet ---

   public void addBullet(double[] center, double[] dir, double speed) {
      double[][] axis = new double[dim][dim];
      Vec.normalize(dir,dir);

      Vec.copy(axis[dim-1],dir);

      if (dim == 4) {
         axis[0][0]=-dir[1]; axis[0][1]=dir[0]; axis[0][2]=-dir[3]; axis[0][3]=dir[2];
         axis[1][0]=-dir[2]; axis[1][1]=dir[3]; axis[1][2]=dir[0]; axis[1][3]=-dir[1];
         axis[2][0]=-dir[3]; axis[2][1]=-dir[2]; axis[2][2]=dir[1]; axis[2][3]=dir[0];
      } else {
         double[] reg = new double[dim-1];
         for (int i=0; i<dim-1; i++) {
            reg[i] = dir[(2+i)%dim];
         }
         if (dir[1] < 1) {
            axis[1][1] = Math.sqrt(1-dir[1]*dir[1]);
            Vec.scale(reg,reg,-dir[1]/axis[1][1]);
            for (int i=0; i<dim-1; i++) {
               axis[1][(2+i)%dim] = reg[i];
            }
         } else Vec.unitVector(axis[1],0);
         for (int i=0; i<dim; i++) {
            int i1 = (i+1)%dim;
            int i2 = (i+2)%dim;
            axis[0][i] = axis[1][i1]*axis[2][i2] - axis[1][i2]*axis[2][i1];
         }
      }
      addBullet(center,axis,speed);
   }

   public void addBullet(double[] center, double[][] axis, double speed) {
      for (int i=0; i<bullets.length; i++) {
         if (bullets[i] == null) {bullets[i] = new Bullet(center,axis,speed,reg3);
         addShape(bullets[i].shape);
         return;
         }
      }
      Bullet[] bulletsNew = new Bullet[bullets.length+16];
      System.arraycopy(bullets,0,bulletsNew,0,bullets.length);
      bulletsNew[bullets.length] = new Bullet(center,axis,speed,reg3);
      addShape(bulletsNew[bullets.length].shape);
      bullets = bulletsNew;
   }

   private void addShape(Geom.Shape shape) {
      if (countSlots() == 0) reallocate(shapes.length+16);
      shapes[findSlot(0)] = shape;
   }

   private void deleteBullet(int ib) {
      int i = indexOf(bullets[ib].shape);
      shapes[i] = null;
      clipUnits[i].setBoundaries(null);
      clearSeparators(i);
      bullets[ib] = null;
   }

   public boolean bulletMove(Bullet bullet) {
      if (bullet.leave-- == 0) return true;
      Geom.Shape shape = bullet.shape;
      Vec.scale(reg3,shape.axis[dim-1],bullet.speed);
      Vec.add(reg3,shape.aligncenter,reg3);
      if (!canMove(shape.aligncenter,reg3,reg4,reg2)) {
         Geom.Shape hitShape = getHitShape();
         for (int i=0; i<enemies.length; i++) {
            if (enemies[i] != null && hitShape == enemies[i].getShape()) {
               if (enemies[i].hit()) killEnemy(i);
               break;
            }
         }
         return true;
      }
      Vec.sub(reg2,reg3,getOrigin(reg1));
      if (Vec.norm(reg2) < 0.25) {
         dead = true;
      }
      Vec.sub(reg3,reg3,shape.aligncenter);
      shape.translate(reg3);
      return false;
   }

   public static class Bullet {

      public Geom.Shape shape;
      public double speed;
      public int leave;

      public Bullet(double[] center, double[][] axis, double speed, double[] reg3) {
         int dim = center.length;
         try {
            if (dim == 3) {
               double[][] d = { { 0, 0.1 }, { 0, 0.1 }, { 0, 0.2 } };
               shape = GeomUtil.rect(d);
            } else {
               double[][] d = { { 0, 0.1 }, { 0, 0.1 }, { 0, 0.1 } , { 0, 0.2 } };
               shape = GeomUtil.rect(d);
            }
         } catch (Exception e) {}
         shape.setNoUserMove();
         shape.setShapeColor(Color.white);
         shape.place(center,axis);
         Vec.copy(reg3,axis[dim-1]);
         Vec.scale(reg3,reg3,0.5);
         shape.translate(reg3);

         this.speed = speed;
         leave = 100;
      }

   }

   private void killEnemy(int ie) {
      int i = indexOf(enemies[ie].getShape());
      shapes[i] = null;
      clipUnits[i].setBoundaries(null);
      clearSeparators(i);
      enemies[ie] = null;
      count--;
   }

   private void printShape() {
      for (int i=0; i<shapes.length; i++) {
         if (shapes[i] != null) System.out.print("1");
         else System.out.print("0");
         System.out.print(",");
      }
      System.out.print("\n");
   }

   private void printBullet() {
      System.out.print("b");
      for (int i=0; i<bullets.length; i++) {
         if (bullets[i] != null) System.out.print("1");
         else System.out.print("0");
         System.out.print(",");
      }
      System.out.print("\n");
   }
}
