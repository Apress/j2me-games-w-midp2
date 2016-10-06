package net.frog_parrot.http;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is a test of http messages on the emulator....
 *
 * @author Carol Hamer
 */
public class Messenger extends MIDlet implements CommandListener {

  //-----------------------------------------------------
  //    game object fields

  /**
   * the thread that sends and receives messages.
   */
  private MessageThread myThread;

   /**
   * the object that writes messages on teh screen.
   */
  private MessageCanvas myCanvas;

 //-----------------------------------------------------
  //    command fields

  /**
   * The button to exit the game.
   */
  private Command myExitCommand = new Command("Exit", Command.EXIT, 99);

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Initialize the thread and the commands.
   */
  public Messenger() {
    try { 
      // create the canvas that displays the messages:
      myCanvas = new MessageCanvas();
      myCanvas.addCommand(myExitCommand);
      myCanvas.setCommandListener(this);
      Display.getDisplay(this).setCurrent(myCanvas);
      // start the thread that makes the connection
      myThread = new MessageThread(myCanvas);
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
    try { 
      if(myThread != null) {
	myThread.start();
      }
    } catch(Exception e) {
      // note that this method will give an exception 
      // if called more than once....
      errorMsg(e);
    }
  }
  
  /**
   * Stop the threads and throw out the garbage.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
    if(myThread != null) {
      myThread.requestStop();
    }
    myThread = null;
    System.gc();
  }

  /**
   * Pause the game.
   */
  public void pauseApp() {
    // this is just a test, it doesn't have to have everything...
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to a command issued on the Canvas.
   * (reset, exit, or change size prefs).
   */
  public void commandAction(Command c, Displayable s) {
    try {
      if(c == myExitCommand) {
	// if there was a serious enough error to 
	// cause an alert, then we end the game 
	// when the user is done reading the alert:
	// (Alert.DISMISS_COMMAND is the default 
	// command that is placed on an Alert 
	// whose timeout is FOREVER)
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
    e.printStackTrace();
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
    myCanvas.setMessage(msg);
  }

}
