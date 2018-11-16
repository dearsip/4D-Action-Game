/*
 * Mat.java
 */

import java.awt.Color;

/**
 * Standard floor mat scenery objects.
 */

public class Mat {

   // want to see [0,1] centered on 5x5 mat
   protected static final int[] FI = new int[] { -2, 0, 1, 3 };
   protected static final int[] FIa = new int[] { -2, -2,  3, 3, 0, 0, 1, 1 };
   protected static final int[] FIb = new int[] { -2,  3, -2, 3, 0, 1, 0, 1 };
   protected static final int F1 = -2;
   protected static final int F2 =  3;
   protected static final Color defaultColor = Color.blue;

   public interface SetColor {
      void setColor(Color color);
   }

   public static class Mat3 extends SceneryBase implements SetColor {
      private Color FC;
      public Mat3() { super(3); FC = defaultColor; }

      public void setColor(Color color) { FC = color; }

      protected void draw() {
         reg1[1] = 0;
         for (int i=0; i<FI.length; i++) {

            reg1[2] = FI[i];
            Vec.copy(reg2,reg1);
            reg1[0] = F1;
            reg2[0] = F2;
            currentDraw.drawLine(reg1,reg2,FC,origin);

            reg1[0] = FI[i];
            Vec.copy(reg2,reg1);
            reg1[2] = F1;
            reg2[2] = F2;
            currentDraw.drawLine(reg1,reg2,FC,origin);
         }
      }
   }

   public static class OldMat4 extends SceneryBase implements SetColor {
      private Color FC;
      public OldMat4() { super(4); FC = defaultColor; }

      public void setColor(Color color) { FC = color; }

      protected void draw() {
         reg1[1] = 0;
         for (int i=0; i<FI.length; i++) {
            for (int j=0; j<FI.length; j++) {

               reg1[2] = FI[i];
               reg1[3] = FI[j];
               Vec.copy(reg2,reg1);
               reg1[0] = F1;
               reg2[0] = F2;
               currentDraw.drawLine(reg1,reg2,FC,origin);

               reg1[0] = FI[i];
               reg1[3] = FI[j];
               Vec.copy(reg2,reg1);
               reg1[2] = F1;
               reg2[2] = F2;
               currentDraw.drawLine(reg1,reg2,FC,origin);

               reg1[0] = FI[i];
               reg1[2] = FI[j];
               Vec.copy(reg2,reg1);
               reg1[3] = F1;
               reg2[3] = F2;
               currentDraw.drawLine(reg1,reg2,FC,origin);
            }
         }
      }
   }

   public static class Mat4 extends SceneryBase implements SetColor {
      private Color FC;
      public Mat4() { super(4); FC = defaultColor; }

      public void setColor(Color color) { FC = color; }

      private void square(int a1, int a2) {

         reg1[a1] = 0;
         reg1[a2] = 0;
         reg2[a1] = 0;
         reg2[a2] = 1;
         currentDraw.drawLine(reg1,reg2,FC,origin);

         reg1[a1] = 1;
         reg1[a2] = 1;
         currentDraw.drawLine(reg1,reg2,FC,origin);

         reg2[a1] = 1;
         reg2[a2] = 0;
         currentDraw.drawLine(reg1,reg2,FC,origin);

         reg1[a1] = 0;
         reg1[a2] = 0;
         currentDraw.drawLine(reg1,reg2,FC,origin);
      }

      private void squares(int a1, int a2, int a3) {

         reg1[a3] = F1;
         reg2[a3] = F1;
         square(a1,a2);

         reg1[a3] = F2;
         reg2[a3] = F2;
         square(a1,a2);
      }

      protected void draw() {
         reg1[1] = 0;
         for (int i=0; i<FIa.length; i++) {

            reg1[2] = FIa[i];
            reg1[3] = FIb[i];
            Vec.copy(reg2,reg1);
            reg1[0] = F1;
            reg2[0] = F2;
            currentDraw.drawLine(reg1,reg2,FC,origin);

            reg1[0] = FIa[i];
            reg1[3] = FIb[i];
            Vec.copy(reg2,reg1);
            reg1[2] = F1;
            reg2[2] = F2;
            currentDraw.drawLine(reg1,reg2,FC,origin);

            reg1[0] = FIa[i];
            reg1[2] = FIb[i];
            Vec.copy(reg2,reg1);
            reg1[3] = F1;
            reg2[3] = F2;
            currentDraw.drawLine(reg1,reg2,FC,origin);
         }
         squares(0,2,3);
         squares(3,0,2);
         squares(2,3,0);
      }
   }

   public static class NoMat implements IScenery {
      public void draw(IDraw currentDraw, double[] origin) {}
   }

}

