package net.frog_parrot.checkers;

import javax.microedition.lcdui.*;

/**
 * This class is the display of the game.
 * 
 * @author Carol Hamer
 */
public class CheckersCanvas extends Canvas {

  //---------------------------------------------------------
  //   static fields

  /**
   * color constant
   */
  public static final int BLACK = 0;

  /**
   * color constant
   */
  public static final int WHITE = 0xffffff;

  /**
   * color constant.
   * (not quite bright red)
   */
  public static final int RED = 0xf96868;

  /**
   * color constant
   */
  public static final int GREY = 0xc6c6c6;

  /**
   * color constant
   */
  public static final int LT_GREY = 0xe5e3e3;

  /**
   * how many rows and columns the display is divided into.
   */
  public static final int GRID_WIDTH = 8;

  //---------------------------------------------------------
  //   instance fields

  /**
   * The black crown to draw on the red pieces..
   */
  private Image myBlackCrown;

  /**
   * The red crown to draw on the black pieces..
   */
  private Image myWhiteCrown;

  /**
   * a handle to the display.
   */
  private Display myDisplay;

  /**
   * a handle to the object that stores the game logic
   * and game data.
   */
  private CheckersGame myGame;

  /**
   * checkers dimension: the width of the squares of the checkerboard.
   */
  private int mySquareSize;

  /**
   * checkers dimension: the minimum width possible for the 
   * checkerboard squares.
   */
  private int myMinSquareSize = 15;

  /**
   * whether or not we're waiting for another player to join 
   * the game.
   */
  private boolean myIsWaiting;

  //-----------------------------------------------------
  //    gets / sets

  /**
   * @return a handle to the class that holds the logic of the 
   * checkers game.
   */
  CheckersGame getGame() {
    return(myGame);
  }

  /**
   * Display a screen to inform the player that we're 
   * waiting for another player.
   */
  void setWaitScreen(boolean wait) {
    myIsWaiting = wait;
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Constructor performs size calculations.
   * @throws Exception if the display size is too 
   *         small to make a checkers.
   */
  CheckersCanvas(Display d) throws Exception {
    myDisplay = d;
    myGame = new CheckersGame();
    // a few calculations to make the right checkerboard 
    // for the current display.
    int width = getWidth();
    int height = getHeight();
    // get the smaller dimension fo the two possible 
    // screen dimensions in order to determine how 
    // big to make the checkerboard.
    int screenSquareWidth = height;
    if(width < height) {
      screenSquareWidth = width;
    }
    mySquareSize = screenSquareWidth / GRID_WIDTH;
    // if the display is too small to make a reasonable checkerboard, 
    // then we throw an Exception
    if(mySquareSize < myMinSquareSize) {
      throw(new Exception("Display too small"));
    }
    // initialize the crown images:
    myBlackCrown = Image.createImage("/images/blackCrown.png");
    myWhiteCrown = Image.createImage("/images/whiteCrown.png");
  }

  /**
   * This is called as soon as the application begins.
   */
  void start() {
    myDisplay.setCurrent(this);
    // prepare the game data for the first move:
    myGame.start();
  }

  //-------------------------------------------------------
  //  graphics methods

  /**
   * Repaint the checkerboard..
   */
  protected void paint(Graphics g) {
    int width = getWidth();
    int height = getHeight();
    g.setColor(WHITE);
    // clear the board (including the region around
    // the board, which can get menu stuff and other 
    // garbage painted onto it...)
    g.fillRect(0, 0, width, height);
    // If we need to wait for another player to join the 
    // game before we can start, this displays the appropriate
    // message:
    if(myIsWaiting) {
      // perform some calculations to place the text correctly:
      Font font = g.getFont();
      int fontHeight = font.getHeight();
      int fontWidth = font.stringWidth("waiting for another player");
      g.setColor(WHITE);
      g.fillRect((width - fontWidth)/2, (height - fontHeight)/2,
		       fontWidth + 2, fontHeight);
      // write in black
      g.setColor(BLACK);
      g.setFont(font);
      g.drawString("waiting for another player", (width - fontWidth)/2, 
		   (height - fontHeight)/2,
			 g.TOP|g.LEFT);
      return;
    }
    // now draw the checkerboard:
    // first the dark squares:
    byte offset = 0;
    for(byte i = 0; i < 4; i++) {
      for(byte j = 0; j < 8; j++) {
	// the offset is used to handle the fact that in every 
	// other row the dark squares are shifted one place 
	// to the right.
	if(j % 2 != 0) {
	  offset = 1;
	} else {
	  offset = 0;
	}
	// now if this is a selected square, we draw it lighter:
	if(myGame.isSelected(i, j)) {
	  g.setColor(LT_GREY);
	  g.fillRect((2*i + offset)*mySquareSize, j*mySquareSize, 
	  	     mySquareSize, mySquareSize);
	} else {
	  // if it's not selected, we draw it dark grey:
	  g.setColor(GREY);
	  g.fillRect((2*i + offset)*mySquareSize, j*mySquareSize, 
		     mySquareSize, mySquareSize);
	}
	// now put the pieces in their places:
	g.setColor(RED);
	int piece = myGame.getPiece(i, j);
	int circleOffset = 2;
	int circleSize = mySquareSize - 2*circleOffset;
	if(piece < 0) {
	  // color the piece in black
	  g.setColor(BLACK);
	  g.fillRoundRect((2*i + offset)*mySquareSize + circleOffset, 
			  j*mySquareSize + circleOffset, 
		   circleSize, circleSize, circleSize, circleSize);
	  // if the player is a king, draw a crown on:
	  if(piece < -1) {
	    g.drawImage(myWhiteCrown, 
		      (2*i + offset)*mySquareSize + mySquareSize/2, 
		      j*mySquareSize + 1 + mySquareSize/2, 
		      Graphics.VCENTER|Graphics.HCENTER);
	  }
	} else if(piece > 0) {
	  // color the piece in red
	  g.fillRoundRect((2*i + offset)*mySquareSize + circleOffset, 
			  j*mySquareSize + circleOffset, 
		   circleSize, circleSize, circleSize, circleSize);
	  // if the player is a king, draw a crown on:
	  if(piece > 1) {
	    g.drawImage(myBlackCrown, 
		      (2*i + offset)*mySquareSize + mySquareSize/2, 
		      j*mySquareSize + 1 + mySquareSize/2, 
		      Graphics.VCENTER|Graphics.HCENTER);
	  }
	}
      }
    }
    // now the blank squares:
    // actually, this part is probably not necessary...
    g.setColor(WHITE);
    for(int i = 0; i < 4; i++) {
      for(int j = 0; j < 8; j++) {
	if(j % 2 == 0) {
	  offset = 1;
	} else {
	  offset = 0;
	}
	g.fillRect((2*i + offset)*mySquareSize, j*mySquareSize, 
		   mySquareSize, mySquareSize);
      }
    }
    // if the player has reached the end of the game, 
    // we display the end message.
    if(myGame.getGameOver()) {
      // perform some calculations to place the text correctly:
      Font font = g.getFont();
      int fontHeight = font.getHeight();
      int fontWidth = font.stringWidth("Game Over");
      g.setColor(WHITE);
      g.fillRect((width - fontWidth)/2, (height - fontHeight)/2,
		       fontWidth + 2, fontHeight);
      // write in black
      g.setColor(BLACK);
      g.setFont(font);
      g.drawString("Game Over", (width - fontWidth)/2, 
		   (height - fontHeight)/2,
			 g.TOP|g.LEFT);
    }
  }

  //-------------------------------------------------------
  //  handle keystrokes

  /**
   * Move the player.
   */
  public void keyPressed(int keyCode) {  
    if(myGame.isMyTurn()) {
      int action = getGameAction(keyCode);   
      switch (action) {
      case LEFT:
	myGame.leftPressed();
	break;
      case RIGHT:
	myGame.rightPressed();
	break;
      case UP:
	myGame.upPressed();
	break;
      case DOWN:
	myGame.deselect();
	break;
      }
      repaint();
      serviceRepaints();
    }
  }

}
