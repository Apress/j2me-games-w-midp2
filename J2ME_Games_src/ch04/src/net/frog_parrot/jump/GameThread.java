package net.frog_parrot.jump;

/**
 * This class contains the loop that keeps the game running.
 *
 * @author Carol Hamer
 */
public class GameThread extends Thread {

  //---------------------------------------------------------
  //   fields

  /**
   * Whether or not the main thread would like this thread 
   * to pause.
   */
  private boolean myShouldPause;

  /**
   * Whether or not the main thread would like this thread 
   * to stop.
   */
  private boolean myShouldStop;

  /**
   * A handle back to the graphical components.
   */
  private JumpCanvas myJumpCanvas;

  /**
   * The System.time of the last screen refresh, used 
   * to regulate refresh speed.
   */
  private long myLastRefreshTime;

  //----------------------------------------------------------
  //   initialization

  /**
   * standard constructor.
   */
  GameThread(JumpCanvas canvas) {
    myJumpCanvas = canvas;
  }

  //----------------------------------------------------------
  //   utilities

  /**
   * Get the amount of time to wait between screen refreshes.
   * Normally we wait only a single millisecond just to give 
   * the main thread a chance to update the keystroke info, 
   * but this method ensures that the game will not attempt 
   * to show too many frames per second.
   */
  private long getWaitTime() {
    long retVal = 1;
    long difference = System.currentTimeMillis() - myLastRefreshTime;
    if(difference < 75) {
      retVal = 75 - difference;
    }
    return(retVal);
  }

  //----------------------------------------------------------
  //   actions

  /**
   * pause the game.
   */
  void pauseGame() {
    myShouldPause = true;
  }

  /**
   * restart the game after a pause.
   */
  synchronized void resumeGame() {
    myShouldPause = false;
    notify();
  }

  /**
   * stops the game.
   */
  synchronized void requestStop() {
    myShouldStop = true;
    notify();
  }

  /**
   * start the game..
   */
  public void run() {
    // flush any keystrokes that occurred before the 
    // game started:
    myJumpCanvas.flushKeys();
    myShouldStop = false;
    myShouldPause = false;
    while(true) {
      myLastRefreshTime = System.currentTimeMillis();
      if(myShouldStop) {
	break;
      }
      synchronized(this) {
	while(myShouldPause) {
	  try {
	    wait();
	  } catch(Exception e) {}
	}
      }
      myJumpCanvas.checkKeys();
      myJumpCanvas.advance();
      // we do a very short pause to allow the other thread 
      // to update the information about which keys are pressed:
      synchronized(this) {
	try {
	  wait(getWaitTime());
	} catch(Exception e) {}
      }
    }
  }

}
