package net.frog_parrot.jump;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the main class of the tumbleweed game.
 *
 * @author Carol Hamer
 */
public class Jump extends MIDlet implements CommandListener {

  //---------------------------------------------------------
  //   commands

  /**
   * the command to end the game.
   */
  private Command myExitCommand = new Command("Exit", Command.EXIT, 99);

  /**
   * the command to start moving when the game is paused.
   */
  private Command myGoCommand = new Command("Go", Command.SCREEN, 1);

  /**
   * the command to pause the game.
   */
  private Command myPauseCommand = new Command("Pause", Command.SCREEN, 1);

  /**
   * the command to start a new game.
   */
  private Command myNewCommand = new Command("Play Again", Command.SCREEN, 1);

  /**
   * The command to start/pause the music.  (This command may appear in a menu)
   */
  private Command myMusicCommand = new Command("Music", Command.SCREEN, 2);

  //---------------------------------------------------------
  //   game object fields

  /**
   * the the canvas that all of the game will be drawn on.
   */
  private JumpCanvas myCanvas;

  //---------------------------------------------------------
  //   thread fields

  /**
   * the thread that advances the cowboy.
   */
  private GameThread myGameThread;

  /**
   * The class that plays music if the user wants.
   */
  //private MusicMaker myMusicMaker;
  private ToneControlMusicMaker myMusicMaker;

  /**
   * The thread tha sets tumbleweeds in motion at random 
   * intervals.
   */
  private TumbleweedThread myTumbleweedThread;

  /**
   * if the user has paused the game.
   */
  private boolean myGamePause;

  /**
   * if the game is paused because it is hidden.
   */
  private boolean myHiddenPause;

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Initialize the canvas and the commands.
   */
  public Jump() {
    try {
      myCanvas = new JumpCanvas(this);
      myCanvas.addCommand(myExitCommand);
      myCanvas.addCommand(myMusicCommand);
      myCanvas.addCommand(myPauseCommand);
      myCanvas.setCommandListener(this);
    } catch(Exception e) {
      errorMsg(e);
    }
  }

  /**
   * Switch the command to the play again command.
   */
  void setNewCommand() {
    myCanvas.removeCommand(myPauseCommand);
    myCanvas.removeCommand(myGoCommand);
    myCanvas.addCommand(myNewCommand);
  }

  /**
   * Switch the command to the go command.
   */
  private void setGoCommand() {
    myCanvas.removeCommand(myPauseCommand);
    myCanvas.removeCommand(myNewCommand);
    myCanvas.addCommand(myGoCommand);
  }

  /**
   * Switch the command to the pause command.
   */
  private void setPauseCommand() {
    myCanvas.removeCommand(myNewCommand);
    myCanvas.removeCommand(myGoCommand);
    myCanvas.addCommand(myPauseCommand);
  }

  //----------------------------------------------------------------
  //  implementation of MIDlet
  // these methods may be called by the application management 
  // software at any time, so we always check fields for null 
  // before calling methods on them.

  /**
   * Start the application.
   */
  public void startApp() throws MIDletStateChangeException {
    try {
      if(myCanvas != null) {
	myCanvas.start();
	myCanvas.flushKeys();
	systemStartThreads();
      }
    } catch(Exception e) {
      errorMsg(e);
    }
  }
  
  /**
   * stop and throw out the garbage.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
    try {
      stopThreads();
      myCanvas = null;
      System.gc();
    } catch(Exception e) {
      errorMsg(e);
    }
  }

  /**
   * request the game to pause. This method is called 
   * by the application management software, not in 
   * response to a user pausing the game.
   */
  public void pauseApp() {
    try {
      if(myCanvas != null) {
	setGoCommand();
	systemPauseThreads();
      }
    } catch(Exception e) {
      errorMsg(e);
    }
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to a command issued on the Canvas.
   * (either reset or exit).
   */
  public void commandAction(Command c, Displayable s) {
    try {
      if(c == myGoCommand) {
	myCanvas.removeCommand(myGoCommand);
	myCanvas.addCommand(myPauseCommand);
	myCanvas.flushKeys();
	userStartThreads();
      } else if(c == myPauseCommand) {
	myCanvas.removeCommand(myPauseCommand);
	myCanvas.addCommand(myGoCommand);
	userPauseThreads();
      } else if(c == myNewCommand) {
	myCanvas.removeCommand(myNewCommand);
	myCanvas.addCommand(myPauseCommand);
	System.gc();
	myCanvas.reset();
	myCanvas.flushKeys();
	myHiddenPause = false;
	myGamePause = false;
	startThreads();
      } else if(c == myMusicCommand) {
	if(myMusicMaker != null) {
	  myMusicMaker.toggle();
	  myCanvas.repaint();
	  myCanvas.serviceRepaints();
	}
      } else if((c == myExitCommand) || (c == Alert.DISMISS_COMMAND)) {
	try {
	  destroyApp(false);
	  notifyDestroyed();
	} catch (MIDletStateChangeException ex) {
	}
      } 
    } catch(Exception e) {
      errorMsg(e);
    }
  }
  
  //-------------------------------------------------------
  //  thread methods

  /**
   * start up all of the game's threads.  
   * Creates them if necessary.
   * to be called when the user hits the go command.
   */
  private synchronized void userStartThreads() throws Exception {
    myGamePause = false;
    if(! myHiddenPause) {
      startThreads();
    }
  }
  
  /**
   * start up all of the game's threads.  
   * Creates them if necessary.
   * used by showNotify
   */
  synchronized void systemStartThreads() throws Exception {
    myHiddenPause = false;
    if(! myGamePause) {
      startThreads();
    }
  }
  
  /**
   * start up all of the game's threads.  
   * Creates them if necessary.
   * internal version.
   * note: if this were synchronized, whould it cause deadlock?
   */
  private void startThreads() throws Exception {
    if(myGameThread == null) {
      myGameThread = new GameThread(myCanvas);
      myGameThread.start();
    } else {
      myGameThread.resumeGame();
    }
    if(myTumbleweedThread == null) {
      myTumbleweedThread = new TumbleweedThread(myCanvas);
      myTumbleweedThread.start();
    } else {
      myTumbleweedThread.resumeGame();
    }
    if(myMusicMaker == null) {
      myMusicMaker = new ToneControlMusicMaker();
      //myMusicMaker = new MusicMaker();
      myMusicMaker.start();
    } else {
      myMusicMaker.resumeGame();
    }
  }
  
  /**
   * Pause all of the threads started by this game.
   * to be called when the user hits the pause command.
   */
  synchronized void userPauseThreads() {
    myGamePause = true;
    pauseThreads();
  }
  
  /**
   * Pause all of the threads started by this game.
   * used by hideNotify
   */
  void systemPauseThreads() {
    myHiddenPause = true;
    pauseThreads();
  }
  
  /**
   * start up all of the game's threads.  
   * Creates them if necessary.
   * internal version.
   * note: if this were synchronized, whould it cause deadlock?
   */
  private void pauseThreads() {
    if(myGameThread != null) {
      myGameThread.pauseGame();
    } 
    if(myTumbleweedThread != null) {
      myTumbleweedThread.pauseGame();
    } 
    if(myMusicMaker != null) {
      myMusicMaker.pauseGame();
    } 
  }
  
  /**
   * Stop all of the threads started by this game and 
   * delete them as they are no longer usable.
   */
  private synchronized void stopThreads() {
    if(myGameThread != null) {
      myGameThread.requestStop();
    }
    if(myTumbleweedThread != null) {
      myTumbleweedThread.requestStop();
    }
    if(myMusicMaker != null) {
      myMusicMaker.requestStop();
    }
    myGameThread = null;
    myTumbleweedThread = null;
    myMusicMaker = null;
  }
  
  //-------------------------------------------------------
  //  error methods
  
  /**
   * Converts an exception to a message and displays 
   * the message..
   */
  void errorMsg(Exception e) {
    if(e.getMessage() == null) {
      errorMsg(e.getClass().getName());
    } else {
      errorMsg(e.getClass().getName() + ":" + e.getMessage());
    }
  }

  /**
   * Displays an error message alert if something goes wrong.
   */
  void errorMsg(String msg) {
    Alert errorAlert = new Alert("error", 
				 msg, null, AlertType.ERROR);
    errorAlert.setCommandListener(this);
    errorAlert.setTimeout(Alert.FOREVER);
    Display.getDisplay(this).setCurrent(errorAlert);
  }

}
