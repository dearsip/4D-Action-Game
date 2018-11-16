/*
 * IStore.java
 */

/**
 * An interface for loading and storing objects.
 * PropertyStore is the only thing that implements it,
 * but I find it provides clarity in the code.
 */

public interface IStore {

// --- get functions ---

   boolean getBoolean(String key) throws ValidationException;
   int     getInteger(String key) throws ValidationException;
   long    getLong   (String key) throws ValidationException;
   double  getDouble (String key) throws ValidationException;
   String  getString (String key) throws ValidationException;

   Integer getNullableInteger(String key) throws ValidationException;

   /**
    * Get data from the store into an object.
    * The object and any substructures and subarrays must exist and be correctly sized.
    */
   void getObject(String key, Object o) throws ValidationException;

// --- put functions ---

   void putBoolean(String key, boolean b);
   void putInteger(String key, int i);
   void putLong   (String key, long l);
   void putDouble (String key, double d);
   void putString (String key, String s);

   /**
    * Put an object into the store.
    */
   void putObject(String key, Object o) throws ValidationException;

}

