package net.frog_parrot.jump;

import javax.microedition.media.*;
import javax.microedition.media.control.*;

/**
 * This is the class that plays a little tune while you 
 * play the game.  This version uses the Player and 
 * Control interfaces.
 *
 * @author Carol Hamer
 */
public class ToneControlMusicMaker implements PlayerListener {

  //---------------------------------------------------------
  //   fields

  /**
   * The player object that plays the tune.
   */
  private Player myPlayer;

  /**
   * Whether or not the player wants to pause the music.
   */
  private boolean myShouldPause;

  /**
   * Whether or not the system wants to pause the music.
   */
  private boolean myGamePause;

  /**
   * The tune played by the game, stored as an array 
   * of bytes in BNF notation.
   */
  private byte[] myTune = {
        // first set the version
        ToneControl.VERSION, 1,   
	// set the tempo
        ToneControl.TEMPO, 30, 
	// define the first line of the song
        ToneControl.BLOCK_START, 0,   
        69,8, 69,8, 69,8, 71,8,       
        73,16, 71,16, 69,8, 73,8,           
        71,8, 71,8, 69,32,            
        ToneControl.BLOCK_END, 0,     
	// define the other line of the song
        ToneControl.BLOCK_START, 1,   
        71,8, 71,8, 71,8, 71,8,       
        66,16, 66,16, 71,8, 69,8,           
        68,8, 66,8, 64,32,            
        ToneControl.BLOCK_END, 1,     
	// play the song
        ToneControl.PLAY_BLOCK, 0,    
        ToneControl.PLAY_BLOCK, 0,    
        ToneControl.PLAY_BLOCK, 1,    
        ToneControl.PLAY_BLOCK, 0,    
  };

  //----------------------------------------------------------
  //   actions

  /**
   * call this when the game pauses.
   * This method does not affect the field 
   * myShouldPause because this method is called only 
   * when the system pauses the music, not when the 
   * player pauses the music.
   */
  void pauseGame() {
    try {
      myGamePause = true;
      myPlayer.stop();
      // when the application pauses the game, resources 
      // are supposed to be released, so we close the 
      // player and throw it away.
      myPlayer.close();
      myPlayer = null;
    } catch(Exception e) {
      // the music isn't necessary, so we ignore exceptions.
    }
  }
  
  /**
   * call this when the game resumes.
   * This method does not affect the field 
   * myShouldPause because this method is called only 
   * when the system reusmes the music, not when the 
   * player pauses the music.
   */
  synchronized void resumeGame() {
    try {
      myGamePause = false;
      if(! myShouldPause) {
	// if the player is null, we create a new one.
	if(myPlayer == null) {
	  start();
	}
	// start the music.
	myPlayer.start();
      }
    } catch(Exception e) {
      // the music isn't necessary, so we ignore exceptions.
    }
  }

  /**
   * toggle the music. 
   * (pause it if it's going, start it again if it's paused).
   */
  synchronized void toggle() {
    try {
      myShouldPause = !myShouldPause;
      if(myShouldPause) {
	if(myPlayer != null) {
	  myPlayer.stop();
	}
      } else if(! myGamePause) {
	// if the player is null, we create a new one.
	if(myPlayer == null) {
	  start();
	}
	// start the music.
	myPlayer.start();
      }
    } catch(Exception e) {
      // the music isn't necessary, so we ignore exceptions.
    }
  }

  /**
   * stops the music.
   */
  synchronized void requestStop() {
    try {
      myPlayer.stop();
      // this is called when the game is over, to we close
      // up the player to release the resources.
      myPlayer.close();
    } catch(Exception e) {
      // the music isn't necessary, so we ignore exceptions.
    }
  }

  //----------------------------------------------------------
  //   initialization

  /**
   * start the music..
   * Here the method is "start" instead of "run" because 
   * it is not necessary to create a thread for the Player.
   * the Player runs on its own thread.
   */
  public void start() {
    ToneControl control = null; 
    try {
      myPlayer = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
      // do the preliminary set-up: 
      myPlayer.realize(); 
      // set a listener to listen for the end of the tune:
      myPlayer.addPlayerListener(this);
      // get the ToneControl object in order to set the tune data:
      control = (ToneControl)myPlayer.getControl("ToneControl"); 
      control.setSequence(myTune);
      // set the volume to the highest possible volume: 
      VolumeControl vc = (VolumeControl)myPlayer.getControl("VolumeControl");
      vc.setLevel(100);
    } catch(Exception e) {
      // the music isn't necessary, so we ignore exceptions.
    }
  }

  //----------------------------------------------------------
  //   implementation of PlayerListener

  /**
   * If we reach the end of the song, play it again...
   */
  public void playerUpdate(Player player, String event, Object eventData) {
    if(event.equals(PlayerListener.END_OF_MEDIA)) {
      if((! myShouldPause) && (! myGamePause)) {
	try {
	  myPlayer.start(); 
	} catch(Exception e) {
	  // the music isn't necessary, so we ignore exceptions.
	}
      }
    }
  }

}
