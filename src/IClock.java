/*
 * IClock.java
 */

/**
 * An interface for things that can be driven by a clock.
 */

public interface IClock {

   /**
    * Perform one tick's worth of work.
    * @return True if more work is pending, false if idle.
    */
   boolean tick();

}

