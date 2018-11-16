/*
 * NamedObject.java
 */

import java.util.Map;

/**
 * A helper structure for ISelectShape.
 */

public class NamedObject implements Comparable {

   public String name;
   public Object object;

   public NamedObject(String name, Object object) {
      this.name = name;
      this.object = object;
   }

   public NamedObject(Map.Entry entry) {
      name = (String) entry.getKey();
      object = entry.getValue();
   }

   public String toString() {
      return name;
   }

   public int compareTo(Object o) {
      NamedObject that = (NamedObject) o;
      return name.compareTo(that.name);
   }

}

