/*
 * IStorable.java
 */

/**
 * An interface for objects that know how to load and store themselves.
 */

public interface IStorable {

   void load(IStore store) throws ValidationException;
   void save(IStore store) throws ValidationException;

}

