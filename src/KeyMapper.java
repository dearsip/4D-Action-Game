/*
 * KeyMapper.java
 */

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An object that receives key events and updates a key buffer accordingly.
 */

public class KeyMapper {

// --- fields ---

   private HashMap hashMap; // maps key codes into entries
   private int modifiersDown;
   private KeyBuffer buf;

// --- construction ---

   public KeyMapper(KeyBuffer buf, OptionsKeys ok, OptionsKeysConfig okc) {
      this.hashMap = new HashMap();
      this.buf = buf;

      setOptions(ok,okc); // covers rest of construction
   }

// --- methods ---

   private void add(Key key, int id) {
      if ( ! key.isDefined() ) return;

      Object hashKey = new Integer(key.code);
      Entry entry = (Entry) hashMap.get(hashKey);
      if (entry == null) {
         entry = new Entry();
         hashMap.put(hashKey,entry);
      }
      entry.add(key.modifiers,id);
   }

   public void setOptions(OptionsKeys ok, OptionsKeysConfig okc) {
      hashMap.clear();

      // add these first, so that although they're hard-coded,
      // they can at least be overwritten
      add(new Key(KeyEvent.VK_X,0),KeyBuffer.getKeyNew(0));
      add(new Key(KeyEvent.VK_C,0),KeyBuffer.getKeyNew(1));
      add(new Key(KeyEvent.VK_V,0),KeyBuffer.getKeyNew(2));
      add(new Key(KeyEvent.VK_B,0),KeyBuffer.getKeyNew(3));
      add(new Key(KeyEvent.VK_Q,2),KeyBuffer.getKeyNew(4));
      add(new Key(KeyEvent.VK_SPACE,0),KeyBuffer.getKeyNew(5));
      add(new Key(KeyEvent.VK_R,2),KeyBuffer.getKeyNew(6));
      add(new Key(KeyEvent.VK_F,2),KeyBuffer.getKeyNew(7));
      add(new Key(KeyEvent.VK_S,2),KeyBuffer.getKeyNew(8));
      add(new Key(KeyEvent.VK_T,2),KeyBuffer.getKeyNew(9));
      add(new Key(KeyEvent.VK_N,0),KeyBuffer.getKeyNew(10));
      add(new Key(KeyEvent.VK_M,0),KeyBuffer.getKeyNew(11));
      add(new Key(KeyEvent.VK_N,1),KeyBuffer.getKeyNew(12));
      add(new Key(KeyEvent.VK_K,2),KeyBuffer.getKeyNew(13));
      add(new Key(KeyEvent.VK_J,2),KeyBuffer.getKeyNew(14));
      add(new Key(KeyEvent.VK_N,2),KeyBuffer.getKeyNew(15));
      add(new Key(KeyEvent.VK_H,2),KeyBuffer.getKeyNew(16));
      add(new Key(KeyEvent.VK_P,2),KeyBuffer.getKeyNew(17));
      add(new Key(KeyEvent.VK_P,3),KeyBuffer.getKeyNew(18));

      for (int i=0; i<OptionsKeys.NKEY; i++) {
         add(ok .key[i],KeyBuffer.getKeyID(i));
      }
      for (int i=0; i<OptionsKeysConfig.NKEY; i++) {
         add(okc.key[i],KeyBuffer.getKeyConfigID(i));
      }
      Iterator i = hashMap.values().iterator();
      while (i.hasNext()) {
         ((Entry) i.next()).fill();
      }
      // all new entries start with down set to false
      modifiersDown = 0;
      buf.clearDown(); // in case we remapped a key that was down
   }

   /**
    * Programmatically release all keys that are currently down.
    */
   public void releaseAll() {
      Iterator i = hashMap.values().iterator();
      while (i.hasNext()) {
         ((Entry) i.next()).down = false;
      }
      modifiersDown = 0;
      buf.clearDown();
   }

   /**
    * Programmatically release a key.<p>
    *
    * This is a function that should only be used under special circumstances.
    */
   public void release(Key key) {
      if ( ! key.isDefined() ) return;

      Object hashKey = new Integer(key.code);
      Entry entry = (Entry) hashMap.get(hashKey);
      if (entry == null) return;

      entry.down = false;
      // modifiersDown can stay as it is

      int id = entry.id[modifiersDown];
      if (id != KeyBuffer.ID_NONE) buf.down[id] = false;
   }

   /**
    * Programmatically unrelease a key, so that it is down without having been pressed.<p>
    *
    * This is a function that should only be used under special circumstances.
    * It may be called at most once, immediately after releaseAll has been called.
    */
   public void unrelease(Key key) {
      if ( ! key.isDefined() ) return;

      Object hashKey = new Integer(key.code);
      Entry entry = (Entry) hashMap.get(hashKey);
      if (entry == null) return;

      entry.down = true;
      modifiersDown = key.modifiers;

      int id = entry.id[modifiersDown];
      if (id != KeyBuffer.ID_NONE) buf.down[id] = true;
   }

   /**
    * Starting from scratch, figure out which mapped keys are down.
    */
   private void recalculate() {
      buf.clearDown();
      Iterator i = hashMap.values().iterator();
      while (i.hasNext()) {
         Entry entry = (Entry) i.next();
         if (entry.down) {
            int id = entry.id[modifiersDown];
            if (id != KeyBuffer.ID_NONE) {
               buf.down[id] = true;
            }
         }
      }
   }

   public void keyChanged(int code, boolean down) {

      int modifier = Key.translateAllowedModifier(code);
      if (modifier != 0) { // modifier key

         if (down) modifiersDown |=  modifier;
         else      modifiersDown &= ~modifier;

         recalculate();
         // note that pressing a modifier key doesn't affect buf.pressed

      } else { // regular key

         Object hashKey = new Integer(code);
         Entry entry = (Entry) hashMap.get(hashKey);

         if (entry != null) {         // is it mapped?
            if (entry.down != down) { // is it not an auto-repeat?

               // the condition we are trying to maintain is that for every entry,
               //
               //    buf.down[entry.id[modifiersDown]] = entry.down
               //
               // (unless the id is ID_NONE, in which case buf.down isn't affected)
               // here modifiersDown isn't changing, so it's easy,
               // we just set both down flags to match the one we received

               entry.down = down;

               int id = entry.id[modifiersDown];
               if (id != KeyBuffer.ID_NONE) {
                  if (down) buf.pressed[id] = true;
                            buf.down   [id] = down;
               }
            }
         }
      }
   }

// --- hash table entry class ---

   private static class Entry {

      public int[] id;
      public boolean down;

      public Entry() {
         id = blank();
         // down is false
      }

      private int[] blank() {
         int[] temp = new int[16];
         for (int i=0; i<16; i++) temp[i] = KeyBuffer.ID_NONE;
         return temp;
      }

      public void add(int modifiers, int id) {
         this.id[modifiers] = id;
      }

      private void fill(int[] temp, int modifiers, int id) {
         for (int i=0; i<16; i++) {
            temp[i|modifiers] = id;
         }
         // it's true that the loop often writes onto some entries multiple times,
         // but it writes in the correct spots, which is the main thing
      }

      public void fill() {
         int[] temp = blank();
         for (int i=0; i<16; i++) {
            if (id[i] != KeyBuffer.ID_NONE) {
               fill(temp,i,id[i]);
            }
         }
         id = temp;

         // how to see that this is correct?
         // if i and j both have ids, and j dominates i,
         // then j has more bits set, is therefore larger,
         // and so gets filled in last, taking precedence

         // if i and j aren't comparable, you get an arbitrary ordering.
         // for example, ALT_MASK is the high bit (8),
         // so Shift+Alt X inherits preferentially from Alt X over Shift X.
      }
   }

}

