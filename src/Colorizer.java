/*
 * Colorizer.java
 */

import java.awt.Color;
import java.util.Random;

/**
 * An object that determines face colors using one of several methods.
 * The different methods are implemented in a single object
 * so that you can switch back and forth without rebuilding the (large) color array.
 */

public class Colorizer implements IColorize {

// --- fields ---

   private int dimSpace;
   private int dimMap;
   private int[] limits;

   private int colorMode;
   private DynamicArray.OfColor byCell;
   private Color[] byOrientation;
   private Color[] byDirection;
   private DynamicArray.OfColor byTrace;

   private OptionsColor ocCache; // cache only used to detect changes
   private long seedCache;

// --- construction ---

   public Colorizer(int dimSpace, int dimMap, int[] size, OptionsColor oc, long seed) {
      this.dimSpace = dimSpace;
      this.dimMap = dimMap;
      limits = DynamicArray.makeLimits(size);

      byCell = new DynamicArray.OfColor(dimSpace,limits);
      byOrientation = new Color[  dimSpace];
      byDirection   = new Color[2*dimSpace];
      byTrace = new DynamicArray.OfColor(dimMap,limits);

      ocCache = new OptionsColor();

      setOptions(oc,seed,true);
   }

   public void setTrace(int[] cell) {
      byTrace.set(cell,Color.gray);
   }

// --- options ---

   public void setColorMode(int colorMode) {
      this.colorMode = ocCache.colorMode = colorMode;
   }

   public void setOptions(OptionsColor oc, long seed) {
      setOptions(oc,seed,false);
   }

   private void setOptions(OptionsColor oc, long seed, boolean force) {

      // changing anything but the color mode requires full generation

      colorMode = ocCache.colorMode = oc.colorMode;
      if (    ! force
           && OptionsColor.equals(ocCache,oc)
           && seedCache == seed               ) return;

      OptionsColor.copy(ocCache,oc);
      seedCache = seed;

      generate(oc,seed);
   }

   private void generate(OptionsColor oc, long seed) {

      Color[] colors = oc.getColors();
      Random random = new Random(seed);

      generateByCell(oc.dimSameParallel,oc.dimSamePerpendicular,colors,random);

      generate(byOrientation,colors,random);
      generate(byDirection,  colors,random);

      generateByTrace();
   }

// --- by cell ---

   private void generateByCell(int dimSameParallel, int dimSamePerpendicular, Color[] colors, Random random) {

   // figure out the dimension numbers

      int dimNon = dimSpace - dimMap;

      // the same-color dimensions are not checked against any upper bound ... until now
      int dimMapSame = Math.min(dimSameParallel,     dimMap);
      int dimNonSame = Math.min(dimSamePerpendicular,dimNon);

      // define these for slightly easier reading
      int dimMapDiff = dimMap - dimMapSame;
      int dimNonDiff = dimNon - dimNonSame;

   // decide which dimensions are which

      // make lists of the map and nonmap dimensions,
      // then permute them to decide which ones will be the same-color dimensions

      int[] aMap = Permute.sequence(0,     dimMap);
      int[] aNon = Permute.sequence(dimMap,dimNon);

      Permute.permute(aMap,random);
      Permute.permute(aNon,random);

      int[] aSame = join( aMap,0,         dimMapSame, aNon,0,         dimNonSame );
      int[] aDiff = join( aMap,dimMapSame,dimMapDiff, aNon,dimNonSame,dimNonDiff );

   // now fill in the array

      for (DynamicArray.Iterator iDiff = new DynamicArray.Iterator(aDiff,new int[dimSpace],limits);
                                 iDiff.hasCurrent(); iDiff.increment()) {

         Color color = colors[random.nextInt(colors.length)];

         for (DynamicArray.Iterator iSame = new DynamicArray.Iterator(aSame,iDiff.current(),limits);
                                    iSame.hasCurrent(); iSame.increment()) {

            byCell.set(iSame.current(),color);
         }
      }
   }

   /**
    * Join parts of two arrays together (by concatenation).
    */
   private static int[] join(int[] a1, int base1, int n1, int[] a2, int base2, int n2) {
      int[] a = new int[n1+n2];

      int next = 0;
      for (int i=0; i<n1; i++) a[next++] = a1[base1+i];
      for (int i=0; i<n2; i++) a[next++] = a2[base2+i];

      return a;
   }

// --- by orientation and direction ---

   private static void generate(Color[] bySomething, Color[] colors, Random random) {

      int[] p = Permute.permute(bySomething.length,colors.length,random);

      for (int i=0; i<bySomething.length; i++) {
         bySomething[i] = colors[p[i]];
      }
   }

// --- by trace ---

   private void generateByTrace() {
      for (DynamicArray.Iterator iTrace = new DynamicArray.Iterator(Permute.sequence(0,dimMap),new int[dimSpace],limits);
                                 iTrace.hasCurrent(); iTrace.increment()) {
         byTrace.set(iTrace.current(),Color.white);
      }
   }
      

// --- implementation of IColorize ---

   public Color getColor(int[] p, int dir) {
      Color color;

      switch (colorMode) {

      case OptionsColor.COLOR_MODE_EXTERIOR:
         Dir.apply(dir,p,1);
         color = byCell.get(p);
         Dir.apply(dir,p,-1);
         break;

      case OptionsColor.COLOR_MODE_INTERIOR:
         color = byCell.get(p);
         break;

      case OptionsColor.COLOR_MODE_BY_ORIENTATION:
         color = byOrientation[Dir.getAxis(dir)];
         break;

      case OptionsColor.COLOR_MODE_BY_DIRECTION:
         color = byDirection[dir];
         break;

      case OptionsColor.COLOR_MODE_BY_TRACE:
         color = byTrace.get(p);
         break;

      default:
         throw new IllegalArgumentException();
      }

      return color;
   }

}
