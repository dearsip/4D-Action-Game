/*
 * PropertyStore.java
 */

import java.lang.reflect.*;
import java.util.Properties;

/**
 * A store, i.e., a place where objects can be stored persistently,
 * implemented on top of a Properties object.<p>
 *
 * The logic for primitive types is a bit convoluted.
 * The end result is that array components and structure fields
 * can be (some) primitive types but not primitive wrapper types.
 * The latter will store correctly, but won't load.
 */

public class PropertyStore implements IStore {

// --- fields ---

   private Properties p;

// --- construction ---

   public PropertyStore(Properties p) {
      this.p = p;
   }

// --- helpers ---

   /**
    * Parse a string to produce a boolean value.
    * This is different from the function Boolean.parseBoolean in that
    *     (a) it exists
    * and (b) it uses strict parsing, unlike the related functions in Boolean.
    */
   private boolean parseBoolean(String s) throws NumberFormatException {
           if (s.equals("true" )) return true;
      else if (s.equals("false")) return false;
      else throw new NumberFormatException(); // not really a number, but close enough
   }

   /**
    * Get a property, converting the not-found condition into an exception.
    * In most cases, we could just return the null
    * and let the parse function convert it into a NumberFormatException,
    * but I think the error message is clearer this way.
    */
   private String getProperty(String key) throws ValidationException {
      String value = p.getProperty(key);
      if (value == null) throw App.getException("PropertyStore.e1",new Object[] { key });
      return value;
   }

   private String getNullableProperty(String key) {
      return p.getProperty(key);
   }

   private String arrayKey(String key, int i) {
      return key + "[" + i + "]";
   }

   private String fieldKey(String key, java.lang.reflect.Field field) {
      return key + "." + field.getName();
   }

   /**
    * An analogue of Class.isPrimitive for primitive wrapper types.
    */
   private boolean isPrimitiveWrapper(Class c) {

      return (    c == Boolean.class
               || c == Integer.class
               || c == Long.class
               || c == Double.class

               || c == Byte.class
               || c == Short.class
               || c == Float.class
               || c == Character.class );
   }

// --- get functions ---

   public boolean getBoolean(String key) throws ValidationException {
      String value = getProperty(key);
      try {
         return parseBoolean(value); // there is no Boolean.parseBoolean, see above
      } catch (NumberFormatException e) {
         throw App.getException("PropertyStore.e2",new Object[] { key, value });
      }
   }

   public int getInteger(String key) throws ValidationException {
      String value = getProperty(key);
      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException e) {
         throw App.getException("PropertyStore.e3",new Object[] { key, value });
      }
   }

   public long getLong(String key) throws ValidationException {
      String value = getProperty(key);
      try {
         return Long.parseLong(value);
      } catch (NumberFormatException e) {
         throw App.getException("PropertyStore.e4",new Object[] { key, value });
      }
   }

   public double getDouble(String key) throws ValidationException {
      String value = getProperty(key);
      try {
         return Double.parseDouble(value);
      } catch (NumberFormatException e) {
         throw App.getException("PropertyStore.e5",new Object[] { key, value });
      }
   }

   public String getString(String key) throws ValidationException {
      return getProperty(key);
   }

   public Integer getNullableInteger(String key) throws ValidationException {
      String value = getNullableProperty(key);
      if (value == null) return null;
      try {
         return new Integer(Integer.parseInt(value));
      } catch (NumberFormatException e) {
         throw App.getException("PropertyStore.e11",new Object[] { key, value });
      }
   }

   /**
    * Get data from the store into an array.
    * The array and any substructures and subarrays must exist and be correctly sized.
    */
   private void getArray(String key, Object array) throws ValidationException {

      Class componentType = array.getClass().getComponentType();
      boolean isPrimitive = componentType.isPrimitive();
      int len = Array.getLength(array);

      for (int i=0; i<len; i++) {
         String arrayKey = arrayKey(key,i);

         if (isPrimitive) Array.set(array,i,getPrimitive(arrayKey,componentType));
         else getObject(arrayKey,Array.get(array,i));
      }

      // the Array functions can throw a couple of exceptions,
      // but only if there's programmer error, so don't catch them.
   }

   /**
    * Get data from the store into a structure.
    * The structure and any substructures and subarrays must exist and be correctly sized.
    */
   private void getStruct(String key, Object o) throws ValidationException {

      java.lang.reflect.Field[] field = o.getClass().getFields();

      for (int i=0; i<field.length; i++) {

         int mods = field[i].getModifiers();
         if (    Modifier.isStatic(mods)
              || Modifier.isFinal (mods) ) continue; // ignore globals and constants

         Class fieldType = field[i].getType();
         String fieldKey = fieldKey(key,field[i]);

         try {

            if (fieldType.isPrimitive()) field[i].set(o,getPrimitive(fieldKey,fieldType));
            else getObject(fieldKey,field[i].get(o));

         } catch (IllegalAccessException e) {
            throw App.getException("PropertyStore.e9",new Object[] { key, e.getMessage() });
         }
      }

      if (o instanceof IValidate) ((IValidate) o).validate();
   }

   /**
    * Get a (wrapped) object of primitive type from the store.
    */
   private Object getPrimitive(String key, Class c) throws ValidationException {

           if (c == Boolean.TYPE) return new Boolean(getBoolean(key));
      else if (c == Integer.TYPE) return new Integer(getInteger(key));
      else if (c == Long.TYPE   ) return new Long   (getLong   (key));
      else if (c == Double.TYPE ) return new Double (getDouble (key));
      else throw App.getException("PropertyStore.e6",new Object[] { key, c.getName() });
   }

   /**
    * Get data from the store into an object.
    * The object and any substructures and subarrays must exist and be correctly sized.<p>
    *
    * Because the primitive wrapper types aren't mutable, i.e., don't act as pointers,
    * we can't handle primitive types here.  Use getPrimitive instead.
    */
   public void getObject(String key, Object o) throws ValidationException {
      Class c = o.getClass();

           if (c.isPrimitive()) throw App.getException("PropertyStore.e7",new Object[] { key, c.getName() });
      else if (c.isArray()    ) getArray (key,o);
      else                      getStruct(key,o);
   }

// --- put functions ---

   public void putBoolean(String key, boolean b) {
      p.setProperty(key,String.valueOf(b));
   }

   public void putInteger(String key, int i) {
      p.setProperty(key,String.valueOf(i));
   }

   public void putLong(String key, long l) {
      p.setProperty(key,String.valueOf(l));
   }

   public void putDouble(String key, double d) {
      p.setProperty(key,String.valueOf(d));
   }

   public void putString(String key, String s) {
      p.setProperty(key,s);
   }

   /**
    * Put an array into the store.
    */
   private void putArray(String key, Object array) throws ValidationException {

      int len = Array.getLength(array);

      for (int i=0; i<len; i++) {
         putObject(arrayKey(key,i),Array.get(array,i));
      }

      // the Array functions can throw a couple of exceptions,
      // but only if there's programmer error, so don't catch them.
   }

   /**
    * Put a structure into the store.
    */
   private void putStruct(String key, Object o) throws ValidationException {

      java.lang.reflect.Field[] field = o.getClass().getFields();

      for (int i=0; i<field.length; i++) {

         int mods = field[i].getModifiers();
         if (    Modifier.isStatic(mods)
              || Modifier.isFinal (mods) ) continue; // ignore globals and constants

         try {

            putObject(fieldKey(key,field[i]),field[i].get(o));

         } catch (IllegalAccessException e) {
            throw App.getException("PropertyStore.e10",new Object[] { key, e.getMessage() });
         }
      }
   }

   /**
    * Put an object into the store.  The object can be a primitive wrapper type.
    */
   public void putObject(String key, Object o) throws ValidationException {
      Class c = o.getClass();

           if (c == Boolean.class) putBoolean(key,((Boolean) o).booleanValue());
      else if (c == Integer.class) putInteger(key,((Integer) o).intValue());
      else if (c == Long.class   ) putLong   (key,((Long)    o).longValue());
      else if (c == Double.class ) putDouble (key,((Double)  o).doubleValue());

      else if (isPrimitiveWrapper(c)) throw App.getException("PropertyStore.e8",new Object[] { key, c.getName() });
      else if (c.isArray()    ) putArray (key,o);
      else                      putStruct(key,o);
   }

}

