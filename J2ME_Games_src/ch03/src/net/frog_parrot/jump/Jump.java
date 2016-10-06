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

  //---------------------------------------------------------
  //   game object fields

  /**
   * the the canvas that all of the game will be drawn on.
   */
  private JumpCanvas myCanvas;

  /**
   * the thread that advances the cowboy.
   */
  private GameThread myGameThread;

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Initialize the canvas and the commands.
   */
  public Jump() {
    try {
      myCanvas = new JumpCanvas(this);
      myCanvas.addCommand(myExitCommand);
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
    if(myCanvas != null) {
      if(myGameThread == null) {
	myGameThread = new GameThread(myCanvas);
	myCanvas.start();
	myGameThread.start();
      } else {
	myCanvas.removeCommand(myGoCommand);
	myCanvas.addCommand(myPauseCommand);
	myCanvas.flushKeys();
	myGameThread.resumeGame();
      }
    }
  }
  
  /**
   * stop and throw out the garbage.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
    if(myGameThread != null) {
      myGameThread.requestStop();
    }
    myGameThread = null;
    myCanvas = null;
    System.gc();
  }

  /**
   * request the thread to pause.
   */
  public void pauseApp() {
    if(myCanvas != null) {
      setGoCommand();
    }
    if(myGameThread != null) {
      myGameThread.pauseGame();
    }
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to a command issued on the Canvas.
   * (either reset or exit).
   */
  public void commandAction(Command c, Displayable s) {
    if(c == myGoCommand) {
      myCanvas.removeCommand(myGoCommand);
      myCanvas.addCommand(myPauseCommand);
      myCanvas.flushKeys();
      myGameThread.resumeGame();
    } else if(c == myPauseCommand) {
      myCanvas.removeCommand(myPauseCommand);
      myCanvas.addCommand(myGoCommand);
      myGameThread.pauseGame();
    } else if(c == myNewCommand) {
      myCanvas.removeCommand(myNewCommand);
      myCanvas.addCommand(myPauseCommand);
      myCanvas.reset();
      myGameThread.resumeGame();
    } else if((c == myExitCommand) || (c == Alert.DISMISS_COMMAND)) {
      try {
	destroyApp(false);
	notifyDestroyed();
      } catch (MIDletStateChangeException ex) {
      }
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
