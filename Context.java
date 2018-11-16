/*
 * Context.java
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

/**
 * The data context for geometry-definition commands.
 */

public class Context {

   public Stack stack;
   public HashMap dict; // String -> Object
   public LinkedList libDirs; // File
   public Stack dirStack; // File
   public HashSet included; // File in canonical form
   public LinkedList topLevelInclude; // String
   public HashSet topLevelDef; // String

   public Context() {
      stack = new Stack();
      dict = new HashMap();
      libDirs = new LinkedList();
      dirStack = new Stack();
      included = new HashSet();
      topLevelInclude = new LinkedList();
      topLevelDef = new HashSet();
   }

   public boolean isTopLevel() {
      return (dirStack.size() == 1);
   }

}

