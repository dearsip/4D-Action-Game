/*
 * IMenu.java
 */

/**
 * An interface for invoking menu commands via keyboard shortcuts.
 */

public interface IMenu {

   void doNew();
   void doOptions();
   void doExit();
   void doReload(int delta);
   int     doSelectShape(ISelectShape iss);
   boolean doSelectPaint(ISelectShape iss);

}

