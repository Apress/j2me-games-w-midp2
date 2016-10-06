package net.frog_parrot.jump;

import javax.microedition.media.*;

/**
 * This is the class that plays a little tune while you 
 * play the game.
 *
 * @author Carol Hamer
 */
public class MusicMaker extends Thread {

  //---------------------------------------------------------
  //   fields

  /**
   * Whether or not the main thread would like this thread 
   * to stop.
   */
  public static final int NOTE_LENGTH = 250;

  /**
   * Whether or not the main thread would like this thread 
   * to pause.
   */
  private boolean myShouldPause;

  /**
   * If the whole game is paused, we pause the music too..
   */
  private boolean myGamePause;

  /**
   * Whether or not the main thread would like this thread 
   * to stop.
   */
  private static boolean myShouldStop;

  /**
   * The tune played by the game, stored as an array 
   * of notes and durations.
   *
   * NOTE: 69 is A. To get other notes, just add or subtract 
   * their difference from A on the keyboard including the 
   * black keys in the calculation.  See the scales below 
   * for an idea.
   * 
   */
  private byte[][] myTune = { { 69, 1 }, { 69, 1 }, { 69, 1 }, { 71, 1 }, 
		     { 73, 2 }, { 71, 2 }, { 69, 1 }, { 73, 1 },
		     { 71, 1 }, { 71, 1 }, { 69, 4 }, 
		     { 69, 1 }, { 69, 1 }, { 69, 1 }, { 71, 1 }, 
		     { 73, 2 }, { 71, 2 }, { 69, 1 }, { 73, 1 },
		     { 71, 1 }, { 71, 1 }, { 69, 4 }, 
		     { 71, 1 }, { 71, 1 }, { 71, 1 }, { 71, 1 }, 
		     { 66, 2 }, { 66, 2 }, { 71, 1 }, { 69, 1 },
		     { 68, 1 }, { 66, 1 }, { 64, 4 }, 
		     { 69, 1 }, { 69, 1 }, { 69, 1 }, { 71, 1 }, 
		     { 73, 2 }, { 71, 2 }, { 69, 1 }, { 73, 1 },
		     { 71, 1 }, { 71, 1 }, { 69, 4 }
  };

  /**
   * An example "tune" that is just a scale..
   * not used.
   */
  private byte[][] myScale = { { 69, 1 }, { 71, 1 }, { 73, 1 }, { 74, 1 }, 
		     { 76, 1 }, { 78, 1 }, { 80, 1 }, { 81, 1 } };

  /**
   * An example "tune" that is just a scale..
   * not used.
   */
  private byte[][] myScale2 = { { 57, 1 }, { 59, 1 }, { 61, 1 }, { 62, 1 }, 
		     { 64, 1 }, { 66, 1 }, { 68, 1 }, { 69, 1 } };

  //----------------------------------------------------------
  //   actions

  /**
   * call this when the game pauses.
   */
  void pauseGame() {
    myGamePause = true;
  }

  /**
   * call this when the game resumes.
   */
  synchronized void resumeGame() {
    myGamePause = false;
    this.notify();
  }

  /**
   * toggle the music. 
   * (pause it if it's going, start it again if it's paused).
   */
  synchronized void toggle() {
    myShouldPause = !myShouldPause;
    this.notify();
  }

  /**
   * stops the music.
   */
  synchronized void requestStop() {
    myShouldStop = true;
    this.notify();
  }

  /**
   * start the music..
   */
  public void run() {
    myShouldStop = false;
    myShouldPause = true;
    myGamePause = false;
    int counter = 0;
    while(true) {
      if(myShouldStop) {
	break;
      }
      synchronized(this) {
	while((myShouldPause) || (myGamePause)) {
	  try {
	    wait();
	  } catch(Exception e) {}
	}
      } 
      try {
	Manager.playTone(myTune[counter][0], 
			 myTune[counter][1]*NOTE_LENGTH, 50);
      } catch(Exception e) {
	// the music isn't necessary, so we ignore exceptions.
      }
      synchronized(this) {
	try {
	  wait(myTune[counter][1]*NOTE_LENGTH);
	} catch(Exception e) {}
      }
      counter++;
      if(counter >= myTune.length) {
	counter = 0;
      }
    }
  }

}
