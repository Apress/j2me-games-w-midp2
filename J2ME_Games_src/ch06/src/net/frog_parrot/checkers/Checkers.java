package net.frog_parrot.checkers;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the main class of the checkers game.
 *
 * @author Carol Hamer
 */
public class Checkers extends MIDlet implements CommandListener {

  //-----------------------------------------------------
  //    game object fields

  /**
   * The canvas that the checkerboard is drawn on.
   */
  private CheckersCanvas myCanvas;

  /**
   * The class that makes the http connection.
   */
  private Communicator myCommunicator;

  //-----------------------------------------------------
  //    command fields

  /**
   * The button to exit the game.
   */
  private Command myExitCommand = new Command("Exit", Command.EXIT, 99);

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Initialize the canvas and the commands.
   */
  public Checkers() {
    try { 
      //create the canvas and set up the commands:
      myCanvas = new CheckersCanvas(Display.getDisplay(this));
      myCanvas.addCommand(myExitCommand);
      myCanvas.setCommandListener(this);
      CheckersGame game = myCanvas.getGame();
      myCommunicator = new Communicator(this, myCanvas, game);
      game.setCommunicator(myCommunicator);
    } catch(Exception e) {
      // if there's an error during creation, display it as an alert.
      errorMsg(e);
    }
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
    // tell the canvas to set up the game data and paint the 
    // checkerboard.
    if(myCanvas != null) {
      myCanvas.start();
    }
    // tell the communicator to start its thread and make a
    // connection.
    if(myCommunicator != null) {
      myCommunicator.start();
    }
  }
  
  /**
   * Throw out the garbage.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
    // tell the communicator to send the end game 
    // message to the other player and then disconnect:
    if(myCommunicator != null) {
      myCommunicator.endGame();
    }
    // throw the larger game objects in the garbage:
    myCommunicator = null;
    myCanvas = null;
    System.gc();
  }

  /**
   * Pause the game.
   * This method merely ends the game because this 
   * version of the Checkers game does not support 
   * re-entering a game that is in play.  A possible 
   * improvement to the game would be to allow 
   * a player to diconeect and leave a game and then 
   * later return to it, using some sort of session
   * token to find the correct game in progress on 
   * the server side.
   */
  public void pauseApp() {
    try {
      destroyApp(false);
      notifyDestroyed();
    } catch (MIDletStateChangeException ex) {
    }
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to a command issued on the Canvas.
   */
  public void commandAction(Command c, Displayable s) {
    if((c == myExitCommand) || (c == Alert.DISMISS_COMMAND)) {
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
    e.printStackTrace();
    if(e.getMessage() == null) {
      errorMsg(e.getClass().getName());
    } else {
      errorMsg(e.getMessage());
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
