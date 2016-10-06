package net.frog_parrot.jump;

import java.util.Random;

/**
 * This class contains the loop that keeps the game running.
 *
 * @author Carol Hamer
 */
public class TumbleweedThread extends Thread {

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
  private Tumbleweed[] myTumbleweeds;

  /**
   * Random number generator to randomly decide when to appear.
   */
  private Random myRandom = new Random();

  //----------------------------------------------------------
  //   initialization

  /**
   * standard constructor, sets data.
   */
  TumbleweedThread(JumpCanvas canvas) throws Exception {
    myTumbleweeds = canvas.getTumbleweeds();
  }

  //----------------------------------------------------------
  //   actions

  /**
   * pause the thread.
   */
  void pauseGame() {
    myShouldPause = true;
  }

  /**
   * restart the thread after a pause.
   */
  synchronized void resumeGame() {
    myShouldPause = false;
    notify();
  }

  /**
   * stops the thread.
   */
  synchronized void requestStop() {
    myShouldStop = true;
    notify();
  }

  /**
   * start the thread..
   */
  public void run() {
    myShouldStop = false;
    myShouldPause = false;
    while(true) {
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
      // wait a random length of time:
      int waitTime = (1 + getRandomInt(10)) * 100;
      synchronized(this) {
	try {
	  wait(waitTime);
	} catch(Exception e) {}
      }
      if(!myShouldPause) {
	// randomly select which one to set in motion and 
	// tell it to go.  If the chosen tumbleweed is 
	// currently visible, it will not be affected
	int whichWeed = getRandomInt(myTumbleweeds.length);
	myTumbleweeds[whichWeed].go();
      }
    }
  }

  //----------------------------------------------------------
  //   randomization utilities

  /**
   * Gets a random int between 
   * zero and the param upper (exclusive).
   */
  public int getRandomInt(int upper) {
    int retVal = myRandom.nextInt() % upper;
    if(retVal < 0) {
      retVal += upper;
    }
    return(retVal);
  }

}
