package net.frog_parrot.maze;

import javax.microedition.lcdui.*;

/**
 * This class is the display of the game.
 * 
 * @author Carol Hamer
 */
public class MazeCanvas extends javax.microedition.lcdui.Canvas {

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

  //---------------------------------------------------------
  //   instance fields

  /**
   * a handle to the display.
   */
  private Display myDisplay;

  /**
   * The data object that describes the maze configuration.
   */
  private Grid myGrid;

  /**
   * Whether or not the currently displayed maze has 
   * been completed.
   */
  private boolean myGameOver = false;

  /**
   * maze dimension: the width of the maze walls.
   */
  private int mySquareSize;

  /**
   * maze dimension: the maximum width possible for the maze walls.
   */
  private int myMaxSquareSize;

  /**
   * maze dimension: the minimum width possible for the maze walls.
   */
  private int myMinSquareSize;

  /**
   * top corner of the display: x-coordiate
   */
  private int myStartX = 0;

  /**
   * top corner of the display: y-coordinate
   */
  private int myStartY = 0;

  /**
   * how many rows the display is divided into.
   */
  private int myGridHeight;

  /**
   * how many columns the display is divided into.
   */
  private int myGridWidth;

  /**
   * the maximum number columns the display can be divided into.
   */
  private int myMaxGridWidth;

  /**
   * the minimum number columns the display can be divided into.
   */
  private int myMinGridWidth;

  /**
   * previous location of the player in the maze: x-coordiate
   * (in terms of the coordinates of the maze grid, NOT in terms 
   * of the coordinate system of the Canvas.)
   */
  private int myOldX = 1;

  /**
   * previous location of the player in the maze: y-coordinate
   * (in terms of the coordinates of the maze grid, NOT in terms 
   * of the coordinate system of the Canvas.)
   */
  private int myOldY = 1;

  /**
   * current location of the player in the maze: x-coordiate
   * (in terms of the coordinates of the maze grid, NOT in terms 
   * of the coordinate system of the Canvas.)
   */
  private int myPlayerX = 1;

  /**
   * current location of the player in the maze: y-coordinate
   * (in terms of the coordinates of the maze grid, NOT in terms 
   * of the coordinate system of the Canvas.)
   */
  private int myPlayerY = 1;

  //-----------------------------------------------------
  //    gets / sets

  /**
   * Changes the width of the maze walls and calculates how 
   * this change affects the number of rows and columns 
   * the maze can have.
   * @return the number of columns now that the the 
   *         width of the columns has been updated.
   */
  int setColWidth(int colWidth) {
    if(colWidth < 2) {
      mySquareSize = 2;
    } else {
      mySquareSize = colWidth;
    }
    myGridWidth = getWidth() / mySquareSize;
    if(myGridWidth % 2 == 0) {
      myGridWidth -= 1;
    }
    myGridHeight = getHeight() / mySquareSize;
    if(myGridHeight % 2 == 0) {
      myGridHeight -= 1;
    }
    myGrid = null;
    return(myGridWidth);
  }

  /**
   * @return the minimum width possible for the maze walls.
   */
  int getMinColWidth() {
    return(myMinSquareSize);
  }

  /**
   * @return the maximum width possible for the maze walls.
   */
  int getMaxColWidth() {
    return(myMaxSquareSize);
  }

  /**
   * @return the maximum number of columns the display can be divided into.
   */
  int getMaxNumCols() {
    return(myMaxGridWidth);
  }

  /**
   * @return the width of the maze walls.
   */
  int getColWidth() {
    return(mySquareSize);
  }

  /**
   * @return the number of maze columns the display is divided into.
   */
  int getNumCols() {
    return(myGridWidth);
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Constructor performs size calculations.
   * @throws Exception if the display size is too 
   *         small to make a maze.
   */
  public MazeCanvas(Display d) throws Exception {
    myDisplay = d;
    // a few calculations to make the right maze 
    // for the current display.
    int width = getWidth();
    int height = getHeight();
    // tests indicate that 5 is a good default square size, 
    // but the user can change it...
    mySquareSize = 5;
    myMinSquareSize = 3;
    myMaxGridWidth = width / myMinSquareSize;
    if(myMaxGridWidth % 2 == 0) {
      myMaxGridWidth -= 1;
    }
    myGridWidth = width / mySquareSize;
    if(myGridWidth % 2 == 0) {
      myGridWidth -= 1;
    }
    myGridHeight = height / mySquareSize;
    if(myGridHeight % 2 == 0) {
      myGridHeight -= 1;
    }
    myMinGridWidth = 15;
    myMaxSquareSize = width / myMinGridWidth;
    if(myMaxSquareSize > height / myMinGridWidth) {
      myMaxSquareSize = height / myMinGridWidth;
    }
    // if the display is too small to make a reasonable maze, 
    // then we throw an Exception
    if(myMaxSquareSize < mySquareSize) {
      throw(new Exception("Display too small"));
    }
  }

  /**
   * This is called as soon as the application begins.
   */
  void start() {
    myDisplay.setCurrent(this);
    repaint();
  }

  /**
   * discard the current maze and draw a new one.
   */
  void newMaze() {
    myGameOver = false;
    // throw away the current maze.
    myGrid = null;
    // set the player back to the beginning of the maze.
    myPlayerX = 1;
    myPlayerY = 1;
    myOldX = 1;
    myOldY = 1;
    myDisplay.setCurrent(this);
    // paint the new maze
    repaint();
  }

  //-------------------------------------------------------
  //  graphics methods

  /**
   * Create and display a maze if necessary, otherwise just 
   * move the player.  Since the motion in this game is 
   * very simple, it is not necessary to repaint the whole 
   * maze each time, just the player + erase the square 
   * that the player just left..
   */
  protected void paint(Graphics g) {
    // If there is no current maze, create one and draw it.
    if(myGrid == null) {
      int width = getWidth();
      int height = getHeight();
      // create the underlying data of the maze.
      myGrid = new Grid(myGridWidth, myGridHeight);
      // draw the maze:
      // loop through the grid data and color each square the 
      // right color
      for(int i = 0; i < myGridWidth; i++) {
	for(int j = 0; j < myGridHeight; j++) {
	  if(myGrid.mySquares[i][j] == 0) {
	    g.setColor(BLACK);
	  } else {
	    g.setColor(WHITE);
	  }
	  // fill the square with the appropriate color
	  g.fillRect(myStartX + (i*mySquareSize), 
		     myStartY + (j*mySquareSize), 
		     mySquareSize, mySquareSize);
	}
      }
      // fill the extra space outside of the maze
      g.setColor(BLACK);
      g.fillRect(myStartX + ((myGridWidth-1) * mySquareSize), 
		 myStartY, width, height);
      // erase the exit path: 
      g.setColor(WHITE);
      g.fillRect(myStartX + ((myGridWidth-1) * mySquareSize), 
		 myStartY + ((myGridHeight-2) * mySquareSize), width, height);
      // fill the extra space outside of the maze
      g.setColor(BLACK);
      g.fillRect(myStartX, 
		 myStartY + ((myGridHeight-1) * mySquareSize), width, height);
    }
    // draw the player (red): 
    g.setColor(255, 0, 0);
    g.fillRoundRect(myStartX + (mySquareSize)*myPlayerX, 
		    myStartY + (mySquareSize)*myPlayerY, 
		    mySquareSize, mySquareSize, 
		    mySquareSize, mySquareSize);
    // erase the previous location
    if((myOldX != myPlayerX) || (myOldY != myPlayerY)) {
      g.setColor(WHITE);
      g.fillRect(myStartX + (mySquareSize)*myOldX, 
		    myStartY + (mySquareSize)*myOldY, 
		    mySquareSize, mySquareSize);
    }
    // if the player has reached the end of the maze, 
    // we display the end message.
    if(myGameOver) {
      // perform some calculations to place the text correctly:
      int width = getWidth();
      int height = getHeight();
      Font font = g.getFont();
      int fontHeight = font.getHeight();
      int fontWidth = font.stringWidth("Maze Completed");
      g.setColor(WHITE);
      g.fillRect((width - fontWidth)/2, (height - fontHeight)/2,
		       fontWidth + 2, fontHeight);
      // write in red
      g.setColor(255, 0, 0);
      g.setFont(font);
      g.drawString("Maze Completed", (width - fontWidth)/2, 
		   (height - fontHeight)/2,
			 g.TOP|g.LEFT);
    }
  }

  /**
   * Move the player.
   */
  public void keyPressed(int keyCode) {  
    if(! myGameOver) {
      int action = getGameAction(keyCode);   
      switch (action) {
      case LEFT:
	if((myGrid.mySquares[myPlayerX-1][myPlayerY] == 1) && 
	   (myPlayerX != 1)) {
	  myOldX = myPlayerX;
	  myOldY = myPlayerY;
	  myPlayerX -= 2;
	  repaint();
	}
	break;
      case RIGHT:
	if(myGrid.mySquares[myPlayerX+1][myPlayerY] == 1) {
	  myOldX = myPlayerX;
	  myOldY = myPlayerY;
	  myPlayerX += 2;
	  repaint();
	} else if((myPlayerX == myGrid.mySquares.length - 2) && 
		  (myPlayerY == myGrid.mySquares[0].length - 2)) {
	  myOldX = myPlayerX;
	  myOldY = myPlayerY;
	  myPlayerX += 2;
	  myGameOver = true;
	  repaint();
	}
	break;
      case UP:
	if(myGrid.mySquares[myPlayerX][myPlayerY-1] == 1) {
	  myOldX = myPlayerX;
	  myOldY = myPlayerY;
	  myPlayerY -= 2;
	  repaint();
	}
	break;
      case DOWN:
	if(myGrid.mySquares[myPlayerX][myPlayerY+1] == 1) {
	  myOldX = myPlayerX;
	  myOldY = myPlayerY;
	  myPlayerY += 2;
	  repaint();
	}
	break;
      }
    }
  }

}
