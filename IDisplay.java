/*
 * IDisplay.java
 */

/**
 * An interface for controlling a set of displays on screen
 * without really knowing anything about them.
 */

public interface IDisplay {

   void setMode3D      (LineBuffer buf);
   void setMode4DMono  (LineBuffer buf);
   void setMode4DStereo(LineBuffer buf1, LineBuffer buf2);

   void nextFrame();

}

