package net.frog_parrot.maze;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the main class of the maze game.
 *
 * @author Carol Hamer
 */
public class Maze extends MIDlet implements CommandListener {

  //----------------------------------------------------------------
  //  game object fields

  /**
   * The canvas that the maze is drawn on.
   */
  private MazeCanvas myCanvas;

  /**
   * The screen that allows the user to alter the size parameters 
   * of the maze.
   */
  private SelectScreen mySelectScreen;

  //----------------------------------------------------------------
  //  command fields

  /**
   * The button to exit the game.
   */
  private Command myExitCommand = new Command("Exit", Command.EXIT, 99);

  /**
   * The command to create a new maze.  (This command may appear in a menu)
   */
  private Command myNewCommand = new Command("New Maze", Command.SCREEN, 1);

  /**
   * The command to dismiss an alert error message.  In MIDP 2.0
   * an Alert set to Alert.FOREVER automatically has a default 
   * dismiss command.  This program does not use it in order to 
   * allow backwards com
   */
  private Command myAlertDoneCommand = new Command("Done", Command.EXIT, 1);

  /**
   * The command to go to the screen that allows the user 
   * to alter the size parameters.  (This command may appear in a menu)
   */
  private Command myPrefsCommand 
    = new Command("Size Preferences", Command.SCREEN, 1);

  //----------------------------------------------------------------
  //  initialization

  /**
   * Initialize the canvas and the commands.
   */
  public Maze() {
    try { 
      myCanvas = new MazeCanvas(Display.getDisplay(this));
      myCanvas.addCommand(myExitCommand);
      myCanvas.addCommand(myNewCommand);
      myCanvas.addCommand(myPrefsCommand);
      myCanvas.setCommandListener(this);
    } catch(Exception e) {
      // if there's an error during creation, display it as an alert.
      Alert errorAlert = new Alert("error", 
				   e.getMessage(), null, AlertType.ERROR);
      errorAlert.setCommandListener(this);
      errorAlert.setTimeout(Alert.FOREVER);
      errorAlert.addCommand(myAlertDoneCommand);
      Display.getDisplay(this).setCurrent(errorAlert);
    }
  }

  //----------------------------------------------------------------
  //  implementation of MIDlet

  /**
   * Start the application.
   */
  public void startApp() throws MIDletStateChangeException {
    if(myCanvas != null) {
      myCanvas.start();
    }
  }
  
  /**
   * Clean up.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
    myCanvas = null;
    System.gc();
  }

  /**
   * Does nothing since this program occupies no shared resources 
   * and little memory.
   */
  public void pauseApp() {
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to a command issued on the Canvas.
   * (reset, exit, or change size prefs).
   */
  public void commandAction(Command c, Displayable s) {
    if(c == myNewCommand) {
      myCanvas.newMaze();
    } else if(c == myAlertDoneCommand) {
      try {
	destroyApp(false);
	notifyDestroyed();
      } catch (MIDletStateChangeException ex) {
      }
    } else if(c == myPrefsCommand) {
      if(mySelectScreen == null) {
	mySelectScreen = new SelectScreen(myCanvas);
      }
      Display.getDisplay(this).setCurrent(mySelectScreen);
    } else if(c == myExitCommand) {
      try {
	destroyApp(false);
	notifyDestroyed();
      } catch (MIDletStateChangeException ex) {
      }
    }
  }
  
}
