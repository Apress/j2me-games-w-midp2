package net.frog_parrot.dungeon;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the main class of the dungeon game.
 *
 * @author Carol Hamer
 */
public class Dungeon extends MIDlet implements CommandListener {

  //-----------------------------------------------------
  //    game object fields

  /**
   * The canvas that the dungeon is drawn on.
   */
  private DungeonCanvas myCanvas;

  /**
   * the thread that advances the game clock.
   */
  private GameThread myGameThread;

  //-----------------------------------------------------
  //    command fields

  /**
   * The button to exit the game.
   */
  private Command myExitCommand = new Command("Exit", Command.EXIT, 99);

  /**
   * The command to save the game in progress.  
   */
  private Command mySaveCommand = new Command("Save Game", Command.SCREEN, 2);

  /**
   * The command to restore a previously saved game.  
   */
  private Command myRestoreCommand 
    = new Command("Restore Game", Command.SCREEN, 2);

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
  private Command myNewCommand = new Command("Next Board", Command.SCREEN, 1);

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Initialize the canvas and the commands.
   */
  public Dungeon() {
    try { 
      // create the canvas and set up the commands:
      myCanvas = new DungeonCanvas(this);
      myCanvas.addCommand(myExitCommand);
      myCanvas.addCommand(mySaveCommand);
      myCanvas.addCommand(myRestoreCommand);
      myCanvas.addCommand(myPauseCommand);
      myCanvas.setCommandListener(this);
    } catch(Exception e) {
      // if there's an error during creation, display it as an alert.
      errorMsg(e);
    }
  }

  /**
   * Switch the command to the play again command.
   * (removing other commands that are no longer relevant)
   */
  void setNewCommand() {
    myCanvas.removeCommand(myPauseCommand);
    myCanvas.removeCommand(myGoCommand);
    myCanvas.addCommand(myNewCommand);
  }

  /**
   * Switch the command to the go command.
   * (removing other commands that are no longer relevant)
   */
  void setGoCommand() {
    myCanvas.removeCommand(myPauseCommand);
    myCanvas.removeCommand(myNewCommand);
    myCanvas.addCommand(myGoCommand);
  }

  /**
   * Switch the command to the pause command.
   * (removing other commands that are no longer relevant)
   */
  void setPauseCommand() {
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
    if(myCanvas != null) {
      if(myGameThread == null) {
	// create the thread and start the game:
	myGameThread = new GameThread(myCanvas);
	myCanvas.start();
	myGameThread.start();
      } else {
	// in case this gets called again after 
	// the application has been started once:
	myCanvas.removeCommand(myGoCommand);
	myCanvas.addCommand(myPauseCommand);
	myCanvas.flushKeys();
	myGameThread.resumeGame();
      }
    }
  }
  
  /**
   * Stop the threads and throw out the garbage.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
    myCanvas = null;
    if(myGameThread != null) {
      myGameThread.requestStop();
    }
    myGameThread = null;
    System.gc();
  }

  /**
   * Pause the game.
   */
  public void pauseApp() {
    if(myCanvas != null) {
      setGoCommand();
    }
    if(myGameThread != null) {
      myGameThread.pause();
    }
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to a command issued on the Canvas.
   * (reset, exit, or change size prefs).
   */
  public void commandAction(Command c, Displayable s) {
    try {
      if(c == myGoCommand) {
	myCanvas.setNeedsRepaint();
	myCanvas.removeCommand(myGoCommand);
	myCanvas.addCommand(myPauseCommand);
	myCanvas.flushKeys();
	myGameThread.resumeGame();
      } else if(c == myPauseCommand) {
	myCanvas.setNeedsRepaint();
	myCanvas.removeCommand(myPauseCommand);
	myCanvas.addCommand(myGoCommand);
	myGameThread.pause();
      } else if(c == myNewCommand) {
	myCanvas.setNeedsRepaint();
	// go to the next board and restart the game
	myCanvas.removeCommand(myNewCommand);
	myCanvas.addCommand(myPauseCommand);
	myCanvas.reset();
	myGameThread.resumeGame();
      } else if(c == Alert.DISMISS_COMMAND) {
	// if there was a serious enough error to 
	// cause an alert, then we end the game 
	// when the user is done reading the alert:
	// (Alert.DISMISS_COMMAND is the default 
	// command that is placed on an Alert 
	// whose timeout is FOREVER)
	destroyApp(false);
	notifyDestroyed();
      } else if(c == mySaveCommand) {
	myCanvas.setNeedsRepaint();
	myCanvas.saveGame();
      } else if(c == myRestoreCommand) {
	myCanvas.setNeedsRepaint();
	myCanvas.removeCommand(myNewCommand);
	myCanvas.removeCommand(myGoCommand);
	myCanvas.addCommand(myPauseCommand);
	myCanvas.revertToSaved();
      } else if(c == myExitCommand) {
	destroyApp(false);
	notifyDestroyed();
      }
    } catch(Exception e) {
      errorMsg(e);
    }
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
