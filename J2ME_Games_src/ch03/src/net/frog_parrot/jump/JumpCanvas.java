package net.frog_parrot.jump;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * This class is the display of the game.
 * 
 * @author Carol Hamer
 */
public class JumpCanvas extends javax.microedition.lcdui.game.GameCanvas {

  //---------------------------------------------------------
  //   dimension fields
  //  (constant after initialization)

  /**
   * the height of the green region below the ground.
   */
  static final int GROUND_HEIGHT = 32;

  /**
   * a screen dimension.
   */
  static final int CORNER_X = 0;

  /**
   * a screen dimension.
   */
  static final int CORNER_Y = 0;

  /**
   * a screen dimension.
   */
  static int DISP_WIDTH;

  /**
   * a screen dimension.
   */
  static int DISP_HEIGHT;

  /**
   * a font dimension.
   */
  static int FONT_HEIGHT;

  /**
   * the default font.
   */
  static Font FONT;

  /**
   * a font dimension.
   */
  static int SCORE_WIDTH;

  /**
   * The width of the string that displays the time,
   * saved for placement of time display.
   */
  static int TIME_WIDTH;

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
  private Display myDisplay;

  /**
   * a handle to the MIDlet object (to keep track of buttons).
   */
  private Jump myJump;

  /**
   * the LayerManager that handles the game graphics.
   */
  private JumpManager myManager;

  /**
   * whether or not the game has ended.
   */
  private boolean myGameOver;

  /**
   * the player's score.
   */
  private int myScore = 0;

  /**
   * How many ticks we start with.
   */
  private int myInitialGameTicks = 950;

  /**
   * this is saved to determine if the time string needs 
   * to be recomputed.
   */
  private int myOldGameTicks = myInitialGameTicks;

  /**
   * the number of game ticks that have passed.
   */
  private int myGameTicks = myOldGameTicks;

  /**
   * we save the time string to avoid recreating it 
   * unnecessarily.
   */
  private static String myInitialString = "1:00";

  /**
   * we save the time string to avoid recreating it 
   * unnecessarily.
   */
  private String myTimeString = myInitialString;

  //-----------------------------------------------------
  //    gets/sets

  /**
   * This is called when the game ends.
   */
  void setGameOver() {
    myGameOver = true;
    myJump.pauseApp();
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Constructor sets the data, performs dimension calculations, 
   * and creates the graphical objects.
   */
  public JumpCanvas(Jump midlet) throws Exception {
    super(false);
    myDisplay = Display.getDisplay(midlet);
    myJump = midlet;
    // calculate the dimensions
    DISP_WIDTH = getWidth();
    DISP_HEIGHT = getHeight();
    Display disp = Display.getDisplay(myJump);
    if(disp.numColors() < 256) {
      throw(new Exception("game requires 256 shades"));
    }
    if((DISP_WIDTH < 150) || (DISP_HEIGHT < 170)) {
      throw(new Exception("Screen too small"));
    }
    if((DISP_WIDTH > 250) || (DISP_HEIGHT > 250)) {
      throw(new Exception("Screen too large"));
    }
    FONT = getGraphics().getFont();
    FONT_HEIGHT = FONT.getHeight();
    SCORE_WIDTH = FONT.stringWidth("Score: 000");
    TIME_WIDTH = FONT.stringWidth("Time: " + myInitialString);
    if(myManager == null) {
      myManager = new JumpManager(CORNER_X, CORNER_Y + FONT_HEIGHT*2, 
	   DISP_WIDTH, DISP_HEIGHT - FONT_HEIGHT*2 - GROUND_HEIGHT);
    } 
  }

  /**
   * This is called as soon as the application begins.
   */
  void start() {
    myGameOver = false;
    myDisplay.setCurrent(this);
    repaint();
  }

  /**
   * sets all variables back to their initial positions.
   */
  void reset() {
    myManager.reset();
    myScore = 0;
    myGameOver = false;
    myGameTicks = myInitialGameTicks;
    myOldGameTicks = myInitialGameTicks;
    repaint();
  }

  /**
   * clears the key states.
   */
  void flushKeys() {
    getKeyStates();
  }

  /**
   * This version of the game does not deal with what happens
   * when the game is hidden, so hopefully it won't be hidden...
   * see the version in the next chapter for how to implement 
   * hideNotify and showNotify.
   */
  protected void hideNotify() {
  }

  /**
   * This version of the game does not deal with what happens
   * when the game is hidden, so hopefully it won't be hidden...
   * see the version in the next chapter for how to implement 
   * hideNotify and showNotify.
   */
  protected void showNotify() {
  }

  //-------------------------------------------------------
  //  graphics methods

  /**
   * paint the game graphic on the screen.
   */
  public void paint(Graphics g) {
    // clear the screen:
    g.setColor(WHITE);
    g.fillRect(CORNER_X, CORNER_Y, DISP_WIDTH, DISP_HEIGHT);
    // color the grass green
    g.setColor(0, 255, 0);
    g.fillRect(CORNER_X, CORNER_Y + DISP_HEIGHT - GROUND_HEIGHT, 
	       DISP_WIDTH, DISP_HEIGHT);
    // paint the layer manager:
    try {
      myManager.paint(g);
    } catch(Exception e) {
      myJump.errorMsg(e);
    }
    // draw the time and score
    g.setColor(BLACK);
    g.setFont(FONT);
    g.drawString("Score: " + myScore, 
		 (DISP_WIDTH - SCORE_WIDTH)/2, 
		 DISP_HEIGHT + 5 - GROUND_HEIGHT, g.TOP|g.LEFT);
    g.drawString("Time: " + formatTime(), 
		 (DISP_WIDTH - TIME_WIDTH)/2, 
		 CORNER_Y + FONT_HEIGHT, g.TOP|g.LEFT);
    // write game over if the game is over
    if(myGameOver) {
      myJump.setNewCommand();
      // clear the top region:
      g.setColor(WHITE);
      g.fillRect(CORNER_X, CORNER_Y, DISP_WIDTH, FONT_HEIGHT*2 + 1);
      int goWidth = FONT.stringWidth("Game Over");
      g.setColor(BLACK);
      g.setFont(FONT);
      g.drawString("Game Over", (DISP_WIDTH - goWidth)/2, 
      		   CORNER_Y + FONT_HEIGHT, g.TOP|g.LEFT);
    }
  }
  
  /**
   * a simple utility to make the number of ticks look like a time...
   */
  public String formatTime() {
    if((myGameTicks / 16) + 1 != myOldGameTicks) {
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
   * Tell the layer manager to advance the layers and then 
   * update the display.
   */
  void advance() {
    myGameTicks--;
    myScore += myManager.advance(myGameTicks);
    if(myGameTicks == 0) {
      setGameOver();
    }
    // paint the display
    try {
      paint(getGraphics());
      flushGraphics();
    } catch(Exception e) {
      myJump.errorMsg(e);
    }
  }

  /**
   * Respond to keystrokes.
   */
  public void checkKeys() { 
    if(! myGameOver) {
      int keyState = getKeyStates();
      if((keyState & LEFT_PRESSED) != 0) {
	myManager.setLeft(true);
      } 
      if((keyState & RIGHT_PRESSED) != 0) {
	myManager.setLeft(false);
      }
      if((keyState & UP_PRESSED) != 0) {
	myManager.jump();
      } 
    }
  }

}
