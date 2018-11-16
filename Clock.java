/*
 * Clock.java
 */

import java.awt.EventQueue;

/**
 * A clock, i.e., an object that ticks at a given rate (except when it's idle).<p>
 *
 * javax.swing.Timer is supposed to do approximately the same thing, but ...
 * I didn't feel like I had complete control over the last tick, the coalesce behavior wasn't clear,
 * and, worst of all, when I tried it, it consistently ran slow.
 * Maybe it wasn't taking into account the time used by the action being performed?
 */

public class Clock {

// --- fields ---

   private IClock clockInterface;
   private int interval; // ms

   private Thread thread;

   // synchronized access
   private boolean eventStart; // main -> clock
   private boolean eventTick;
   private boolean shouldIdle;
   private boolean isIdle;     // clock -> main

// --- construction ---

   public Clock(IClock clockInterface) {
      this.clockInterface = clockInterface;
      // interval initialized via setFrameRate

      thread = new Thread(new Runnable() { public void run() { thread(); } });

      eventStart = false;
      eventTick = false;
      // shouldIdle is only valid when eventTick is true
      isIdle = false; // let thread set correct value

      thread.start();
      try {
         waitIdle();
      } catch (InterruptedException e) {
         // won't happen
      }
   }

// --- main thread ---

   public void setFrameRate(double frameRate) {
      interval = (int) Math.ceil(1000 / frameRate);
   }

   /**
    * Start the clock, and continue running until the clock interface reports idleness.
    */
   public void start() {
      signalStart();
   }

   private void tick() {
      boolean more;
      try {
         more = clockInterface.tick();
      } catch (Throwable t) {
         more = true;
         // since we don't know, keep generating.  generation will stop eventually
         // if you stop pressing on the keys, unless it's a train model.
         // this is a desperate measure.  the controller is not designed to handle
         // exceptions and has been left in an unknown state.
      }
      signalResult( /* shouldIdle = */ ! more );
   }

// --- synchronization ---

   private synchronized void signalStart() {
      if ( ! isIdle ) return; // already started

      eventStart = true;
      notify();
   }

   private synchronized void signalResult(boolean shouldIdle) {
      // no need to check thread state, we know it because tick was called

      this.shouldIdle = shouldIdle;

      eventTick = true;
      notify();
   }

   private synchronized void waitIdle() throws InterruptedException {
      while ( ! isIdle ) wait();
   }

   private synchronized void waitStart() throws InterruptedException {
      isIdle = true;
      notify(); // to release constructor wait

      while ( ! eventStart ) wait();
      eventStart = false;

      isIdle = false;
   }

   private synchronized boolean waitResult() throws InterruptedException {

      while ( ! eventTick ) wait();
      eventTick = false;

      return shouldIdle;
   }

// --- clock thread ---

   private void thread() {
      Runnable runTick = new Runnable() { public void run() { tick(); } };
      try {
         while (true) {

            waitStart();

            long base = System.currentTimeMillis();
            while (true) {

               EventQueue.invokeLater(runTick);
               if ( /* shouldIdle = */ waitResult() ) break;

               long now  = System.currentTimeMillis();
               long next = base + interval; // unsynchronized use of interval is OK

               if (now < next) { // we have time, sleep a bit

                  long t = next - now;

                  // I don't understand this at all, but sometimes the current time
                  // jumps back by 4-6 minutes for no reason.  does it drift forward
                  // and then get corrected?  does it jump forward and then back?
                  // no idea.  in any case, if we don't detect and stop it, the game
                  // will lock up for that many minutes.
                  //
                  if (t > 1000) {
                     t = interval; // standard interval is best guess for sleep time
                     next = now + interval;
                  }

                  Thread.sleep(t);
                  base = next; // same equation as below would work, but this is clearer

               } else { // no time left, tick again immediately
                  base = Math.max(next,now - 3*interval); // see note below
               }

               // on my system, the actual sleep duration is granular,
               // with each grain being approximately 55 ms.
               // so, if you ask to sleep for 100 ms, you usually sleep for 110.

               // the code above is designed to compensate for this.
               // as long as we are producing frames sufficiently quickly,
               // base will advance by the exact interval,
               // so oversleeping will lead to requesting shorter wait times.

               // if we are not producing frames quickly enough,
               // there's no sense accumulating a large sleep debt,
               // just go ahead and reset the base.
               // actually, it would be nice to be able to recover from a few slow frames,
               // so do let debt accumulate, but limit it to a fixed number of multiples
            }

         }
      } catch (InterruptedException e) {
         // won't happen
      }
   }

}

