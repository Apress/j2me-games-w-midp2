package net.frog_parrot.dungeon;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * This class is the display of the game.
 * 
 * @author Carol Hamer
 */
public class DungeonCanvas extends GameCanvas {

  //---------------------------------------------------------
  //   dimension fields
  //  (constant after initialization)

  /**
   * the height of the black region below the play area.
   */
  static int TIMER_HEIGHT = 32;

  /**
   * the top corner x coordinate according to this 
   * object's coordinate system:.
   */
  static int CORNER_X = 0;

  /**
   * the top corner y coordinate according to this 
   * object's coordinate system:.
   */
  static int CORNER_Y = 0;

  /**
   * the width of the portion of the screen that this 
   * canvas can use.
   */
  static int DISP_WIDTH;

  /**
   * the height of the portion of the screen that this 
   * canvas can use.
   */
  static int DISP_HEIGHT;

  /**
   * the height of the font used for this game.
   */
  static int FONT_HEIGHT;

  /**
   * the font used for this game.
   */
  static Font FONT;

  /**
   * color constant
   */
  public static final int BLACK = 0;

  /**
   * color constant
   */
  public static final int WHITE = 0xffffff;

  //---------------------------------------------------------
  //   game object fields

  /**
   * a handle to the display.
   */
  Display myDisplay;

  /**
   * a handle to the MIDlet object (to keep track of buttons).
   */
  Dungeon myDungeon;

  /**
   * the LayerManager that handles the game graphics.
   */
  DungeonManager myManager;

  /**
   * whether or not the game has ended.
   */
  static boolean myGameOver;

  /**
   * The number of ticks on the clock the last time the 
   * time display was updated.
   * This is saved to determine if the time string needs 
   * to be recomputed.
   */
  int myOldGameTicks = 0;

  /**
   * the number of game ticks that have passed since the 
   * beginning of the game.
   */
  int myGameTicks = myOldGameTicks;

  /**
   * we save the time string to avoid recreating it 
   * unnecessarily.
   */
  static String myInitialString = "0:00";

  /**
   * we save the time string to avoid recreating it 
   * unnecessarily.
   */
  String myTimeString = myInitialString;

  //-----------------------------------------------------
  //    gets/sets

  /**
   * This is called when the game ends.
   */
  void setGameOver() {
    myGameOver = true;
    myDungeon.pauseApp();
  }

  /**
   * Find out if the game has ended.
   */
  static boolean getGameOver() {
    return(myGameOver);
  }

  /**
   * Tell the layer manager that it needs to repaint.
   */
  public void setNeedsRepaint() {
    myManager.setNeedsRepaint();
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Constructor sets the data, performs dimension calculations, 
   * and creates the graphical objects.
   */
  public DungeonCanvas(Dungeon midlet) throws Exception {
    super(false);
    myDisplay = Display.getDisplay(midlet);
    myDungeon = midlet;
    // calculate the dimensions
    DISP_WIDTH = getWidth();
    DISP_HEIGHT = getHeight();
    if((!myDisplay.isColor()) || (myDisplay.numColors() < 256)) {
      throw(new Exception("game requires full-color screen"));
    }
    if((DISP_WIDTH < 150) || (DISP_HEIGHT < 170)) {
      throw(new Exception("Screen too small"));
    }
    if((DISP_WIDTH > 250) || (DISP_HEIGHT > 250)) {
      throw(new Exception("Screen too large"));
    }
    // since the time is painted in white on black, 
    // it shows up better if the font is bold:
    FONT = Font.getFont(Font.FACE_SYSTEM, 
				 Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    // calculate the height of the black region that the 
    // timer is painted on:
    FONT_HEIGHT = FONT.getHeight();
    TIMER_HEIGHT = FONT_HEIGHT + 8;
    // create the LayerManager (where all of the interesting 
    // graphics go!) and give it the dimensions of the 
    // region it is supposed to paint:
    if(myManager == null) {
      myManager = new DungeonManager(CORNER_X, CORNER_Y, 
	   DISP_WIDTH, DISP_HEIGHT - TIMER_HEIGHT, this);
    } 
  }

  /**
   * This is called as soon as the application begins.
   */
  void start() {
    myGameOver = false;
    myDisplay.setCurrent(this);
    setNeedsRepaint();
  }

  /**
   * sets all variables back to their initial positions.
   */
  void reset() throws Exception {
    // most of the variables that need to be reset 
    // are held by the LayerManager:
    myManager.reset();
    myGameOver = false;
    setNeedsRepaint();
  }

  /**
   * sets all variables back to the positions 
   * from a previously saved game.
   */
  void revertToSaved() throws Exception {
    // most of the variables that need to be reset 
    // are held by the LayerManager, so we 
    // prompt the LayerManager to get the 
    // saved data:
    myGameTicks = myManager.revertToSaved();
    myGameOver = false;
    myOldGameTicks = myGameTicks;
    myTimeString = formatTime();
    setNeedsRepaint();
  }

  /**
   * save the current game in progress.
   */
  void saveGame() throws Exception {
    myManager.saveGame(myGameTicks);
  }

  /**
   * clears the key states.
   */
  void flushKeys() {
    getKeyStates();
  }

  /**
   * If the game is hidden by another app (or a menu)
   * ignore it since not much happens in this game 
   * when the user is not actively interacting with it.
   * (we could pause the timer, but it's not important 
   * enough to bother with when the user is just pulling
   * up a menu for a few seconds)
   */
  protected void hideNotify() {
  }

  /**
   * When it comes back into view, just make sure the
   * manager knows that it needs to repaint.
   */
  protected void showNotify() {
    setNeedsRepaint();
  }

  //-------------------------------------------------------
  //  graphics methods

  /**
   * paint the game graphics on the screen.
   */
  public void paint(Graphics g) {
    // color the bottom segment of the screen black
    g.setColor(BLACK);
    g.fillRect(CORNER_X, CORNER_Y + DISP_HEIGHT - TIMER_HEIGHT, 
	       DISP_WIDTH, TIMER_HEIGHT);
    // paint the LayerManager (which paints 
    // all of the interesting graphics):
    try {
      myManager.paint(g);
    } catch(Exception e) {
      myDungeon.errorMsg(e);
    }
    // draw the time 
    g.setColor(WHITE);
    g.setFont(FONT);
    g.drawString("Time: " + formatTime(), DISP_WIDTH/2, 
		   CORNER_Y + DISP_HEIGHT - 4, g.BOTTOM|g.HCENTER);
    // write "Dungeon Completed" when the user finishes a board:
    if(myGameOver) {
      myDungeon.setNewCommand();
      // clear the top region:
      g.setColor(WHITE);
      g.fillRect(CORNER_X, CORNER_Y, DISP_WIDTH, FONT_HEIGHT*2 + 1);
      int goWidth = FONT.stringWidth("Dungeon Completed");
      g.setColor(BLACK);
      g.setFont(FONT);
      g.drawString("Dungeon Completed", (DISP_WIDTH - goWidth)/2, 
      		   CORNER_Y + FONT_HEIGHT, g.TOP|g.LEFT);
    }
  }

  /**
   * a simple utility to make the number of ticks look like a time...
   */
  public String formatTime() {
    if((myGameTicks / 16) != myOldGameTicks) {
      myTimeString = "";
      myOldGameTicks = (myGameTicks / 16) + 1;
      int smallPart = myOldGameTicks % 60;
      int bigPart = myOldGameTicks / 60;
      myTimeString += bigPart + ":";
      if(smallPart / 10 < 1) {
	myTimeString += "0";
      }
      myTimeString += smallPart;
    }
    return(myTimeString);
  }

  //-------------------------------------------------------
  //  game movements

  /**
   * update the display.
   */
  void updateScreen() {
    myGameTicks++;
    // paint the display
    try {
      paint(getGraphics());
      flushGraphics(CORNER_X, CORNER_Y, DISP_WIDTH, DISP_HEIGHT);
    } catch(Exception e) {
      myDungeon.errorMsg(e);
    }
  }

  /**
   * Respond to keystrokes.
   */
  public void checkKeys() { 
    if(! myGameOver) {
      int vertical = 0;
      int horizontal = 0;
      // determine which moves the user would like to make:
      int keyState = getKeyStates();
      if((keyState & LEFT_PRESSED) != 0) {
	horizontal = -1;
      } 
      if((keyState & RIGHT_PRESSED) != 0) {
	horizontal = 1;
      }
      if((keyState & UP_PRESSED) != 0) {
	vertical = -1;
      } 
      if((keyState & DOWN_PRESSED) != 0) {
	// if the user presses the down key, 
	// we put down or pick up a key object
	// or pick up the crown:
	myManager.putDownPickUp();
      } 
      // tell the manager to move the player 
      // accordingly if possible:
      myManager.requestMove(horizontal, vertical);
    }
  }

}
