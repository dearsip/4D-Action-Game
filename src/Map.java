/*
 * Map.java
 */

/**
 * An object that contains map data.
 */

public class Map {

// --- fields ---

   private DynamicArray.OfBoolean map;
   private int[] start;
   private int[] finish;

// --- accessors ---

   public boolean inBounds(int[] p) { return map.inBounds(p); }

   public boolean isOpen(int[] p) { return map.get(p); }
   public void   setOpen(int[] p, boolean b) { map.set(p,b); } // generator only

   public int[] getStart () { return start;  }
   public int[] getFinish() { return finish; }

   public void setStart (int[] start ) { this.start  = start;  } // generator only
   public void setFinish(int[] finish) { this.finish = finish; }

// --- construction ---

   public Map(int dimSpace, OptionsMap om, long seed) {

      int[] limits = DynamicArray.makeLimits(om.size);

      map = new DynamicArray.OfBoolean(dimSpace,limits);
      // elements start out false, which is correct

      // start and finish are produced by the generation algorithm

      new MapGenerator(this,limits,om,seed).generate();
   }

   private static final String KEY_MAP    = "map";
   private static final String KEY_START  = "start";
   private static final String KEY_FINISH = "finish";

   public void save(IStore store, OptionsMap om) throws ValidationException {
      boolean[][][][] cells = new boolean[om.size[0]][][][];
      for (int i = 0; i < om.size[0]; i++) {
         cells[i] = new boolean[om.size[1]][][];
         for (int j = 0; j < om.size[1]; j++) {
            cells[i][j] = new boolean[om.size[2]][];
            for (int k = 0; k < om.size[2]; k++) {
               cells[i][j][k] = new boolean[om.size[3]];
            }
         }
      }
      int[] p = new int[4];
      for (int i = 0; i < om.size[0]; i++) {
         p[0] = i + 1;
         for (int j = 0; j < om.size[1]; j++) {
            p[1] = j + 1;
            for (int k = 0; k < om.size[2]; k++) {
               p[2] = k + 1;
               for (int l = 0; l < om.size[2]; l++) {
                  p[3] = l + 1;
                  cells[i][j][k][l] = isOpen(p);
               }
            }
         }
      }
      store.putObject(KEY_MAP,cells);
      store.putObject(KEY_START,start);
      store.putObject(KEY_FINISH,finish);
   }
}

