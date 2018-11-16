/*
 * SquareLayout.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A layout manager for containers that hold a row of equal-sized square objects.
 */

public class SquareLayout implements LayoutManager {

// --- fields ---

   private Observer observer;
   private int gap;
   private int edgeCache;

   // other results from getLayoutSize
   private int nTotal;
   private int nVisible;
   private Insets insets;
   private int wFixed;
   private int hFixed;

// --- construction ---

   public SquareLayout() {
      this(null,0);
   }

   public SquareLayout(int gap) {
      this(null,gap);
   }

   public SquareLayout(Observer observer) {
      this(observer,0);
   }

   public SquareLayout(Observer observer, int gap) {
      this.observer = observer;
      this.gap = gap;
   }

// --- accessors ---

   public int getGap()  { return gap; }
   public int getEdge() { return edgeCache; }

// --- observer interface ---

   public static interface Observer {
      void update(int edge);
   }

   public void notifyObserver(int edge) {
      edgeCache = edge;
      if (observer != null) observer.update(edge);
   }

// --- implementation of LayoutManager ---

   public void addLayoutComponent(String name, Component comp) {
   }

   public void removeLayoutComponent(Component comp) {
   }

   public Dimension getLayoutSize(Container parent, boolean useMinimum) {

      int edge = 0;

      nTotal = parent.getComponentCount();
      nVisible = 0;

      for (int i=0; i<nTotal; i++) {
         Component c = parent.getComponent(i);
         if ( ! c.isVisible() ) continue;

         nVisible++;

         Dimension d = useMinimum ? c.getMinimumSize() : c.getPreferredSize();
         edge = Math.max(edge,d.width );
         edge = Math.max(edge,d.height);

         // find the smallest square that will enclose all the component sizes.
         // for the minimum size, that's the correct method;
         // for the preferred size, it's just a reasonable approach.
      }

      insets = parent.getInsets();
      wFixed = insets.left + insets.right;
      hFixed = insets.top + insets.bottom;

      if (nVisible > 0) wFixed += (nVisible-1) * gap;

      return new Dimension(wFixed + nVisible * edge, hFixed + edge);
   }

   public Dimension minimumLayoutSize(Container parent) {
      return getLayoutSize(parent,true);
   }

   public Dimension preferredLayoutSize(Container parent) {
      return getLayoutSize(parent,false);
   }

   public void layoutContainer(Container parent) {

      Dimension size = parent.getSize();
      Dimension min = minimumLayoutSize(parent); // computes other results too

      if (nVisible == 0) { notifyObserver(0); return; }

      if (    size.width  < min.width
           || size.height < min.height ) { // degenerate layout

         for (int i=0; i<nTotal; i++) {
            Component c = parent.getComponent(i);
            if ( ! c.isVisible() ) continue;

            c.setBounds(0,0,0,0);
         }

         notifyObserver(0);

      } else { // normal layout

         // figure out what constrains the square size,
         // and hence where we should place the components

         int edgeHorizontal = (size.width - wFixed) / nVisible;
         int edgeVertical = size.height - hFixed;

         int edge;
         int x = insets.left;
         int y = insets.top;

         if (edgeHorizontal < edgeVertical) { // center vertically

            edge = edgeHorizontal;
            y += (size.height - hFixed - edge) / 2;

         } else { // center horizontally

            edge = edgeVertical;
            x += (size.width - wFixed - nVisible * edge) / 2;
         }

         for (int i=0; i<nTotal; i++) {
            Component c = parent.getComponent(i);
            if ( ! c.isVisible() ) continue;

            c.setBounds(x,y,edge,edge);
            x += (edge + gap);
         }

         notifyObserver(edge);
      }
   }

}

