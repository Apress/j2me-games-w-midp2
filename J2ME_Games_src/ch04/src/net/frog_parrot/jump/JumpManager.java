package net.frog_parrot.jump;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * This handles the graphics objects.
 * 
 * @author Carol Hamer
 */
public class JumpManager extends javax.microedition.lcdui.game.LayerManager {

  //---------------------------------------------------------
  //   dimension fields
  //  (constant after initialization)

  /**
   * The x-coordinate of the place on the game canvas where 
   * the LayerManager window should appear, in terms of the 
   * coordiantes of the game canvas.
   */
  static int CANVAS_X;

  /**
   * The y-coordinate of the place on the game canvas where 
   * the LayerManager window should appear, in terms of the 
   * coordiantes of the game canvas.
   */
  static int CANVAS_Y;

  /**
   * The width of the display window.
   */
  static int DISP_WIDTH;

  /**
   * The height of this object's graphical region. This is 
   * the same as the height of the visible part because 
   * in this game the layer manager's visible part scrolls 
   * only left and right but not up and down.
   */
  static int DISP_HEIGHT;

  //---------------------------------------------------------
  //   game object fields

  /**
   * the player's object.
   */
  private Cowboy myCowboy;

  /**
   * the tumbleweeds that enter from the left.
   */
  private Tumbleweed[] myLeftTumbleweeds;

  /**
   * the tumbleweeds that enter from the right.
   */
  private Tumbleweed[] myRightTumbleweeds;

  /**
   * the object representing the grass in the background..
   */
  private Grass myGrass;

  /**
   * Whether or not the player is currently going left.
   */
  private boolean myLeft;

  /**
   * The leftmost x-coordinate that should be visible on the 
   * screen in terms of this objects internal coordinates.
   */
  private int myCurrentLeftX;

  //-----------------------------------------------------
  //    gets/sets

  /**
   * This tells the player to turn left or right.
   * @param left whether or not the turn is towards the left..
   */
  void setLeft(boolean left) {
    myLeft = left;
  }

  /**
   * @return a handle to the tumbleweed objects.
   */
  Tumbleweed[] getTumbleweeds() {
    Tumbleweed[] retArray = new Tumbleweed[myLeftTumbleweeds.length 
					   + myRightTumbleweeds.length];
    for(int i = 0; i < myLeftTumbleweeds.length; i++) {
      retArray[i] = myLeftTumbleweeds[i];
    }
    for(int i = 0; i < myRightTumbleweeds.length; i++) {
      retArray[i + myLeftTumbleweeds.length] = myRightTumbleweeds[i];
    }
    return(retArray);
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Constructor sets the data and constructs the graphical objects..
   * @param x The x-coordinate of the place on the game canvas where 
   * the LayerManager window should appear, in terms of the 
   * coordiantes of the game canvas.
   * @param y The y-coordinate of the place on the game canvas where 
   * the LayerManager window should appear, in terms of the 
   * coordiantes of the game canvas.
   * @param width the width of the region that is to be 
   * occupied by the LayoutManager.
   * @param height the height of the region that is to be 
   * occupied by the LayoutManager.
   */
  public JumpManager(int x, int y, int width, int height)
      throws Exception {
    CANVAS_X = x;
    CANVAS_Y = y;
    DISP_WIDTH = width;
    DISP_HEIGHT = height;
    myCurrentLeftX = Grass.CYCLE*Grass.TILE_WIDTH;
    setViewWindow(0, 0, DISP_WIDTH, DISP_HEIGHT);
    // create the player:
    if(myCowboy == null) {
      myCowboy = new Cowboy(myCurrentLeftX + DISP_WIDTH/2, 
			    DISP_HEIGHT - Cowboy.HEIGHT - 2);
      append(myCowboy);
    }
    // create the tumbleweeds to jump over:
    if(myLeftTumbleweeds == null) {
      myLeftTumbleweeds = new Tumbleweed[2];
      for(int i = 0; i < myLeftTumbleweeds.length; i++) {
	myLeftTumbleweeds[i] = new Tumbleweed(true);
	append(myLeftTumbleweeds[i]);
      }
    }
    if(myRightTumbleweeds == null) {
      myRightTumbleweeds = new Tumbleweed[2];
      for(int i = 0; i < myRightTumbleweeds.length; i++) {
	myRightTumbleweeds[i] = new Tumbleweed(false);
	append(myRightTumbleweeds[i]);
      }
    }
    // create the background object:
    if(myGrass == null) {
      myGrass = new Grass();
      append(myGrass);
    }
  }

  /**
   * sets all variables back to their initial positions.
   */
  void reset() {
    if(myGrass != null) {
      myGrass.reset();
    }
    if(myCowboy != null) {
      myCowboy.reset();
    }
    if(myLeftTumbleweeds != null) {
      for(int i = 0; i < myLeftTumbleweeds.length; i++) {
	myLeftTumbleweeds[i].reset();
      }
    }
    if(myRightTumbleweeds != null) {
      for(int i = 0; i < myRightTumbleweeds.length; i++) {
	myRightTumbleweeds[i].reset();
      }
    }
    myLeft = false;
    myCurrentLeftX = Grass.CYCLE*Grass.TILE_WIDTH;
  }

  //-------------------------------------------------------
  //  graphics methods

  /**
   * paint the game graphic on the screen.
   */
  public void paint(Graphics g) {
    setViewWindow(myCurrentLeftX, 0, DISP_WIDTH, DISP_HEIGHT);
    paint(g, CANVAS_X, CANVAS_Y);
  }

  /**
   * If the cowboy gets to the end of the graphical region, 
   * move all of the pieces so that the screen appears to wrap.
   */
  private void wrap() {
    if(myCurrentLeftX % (Grass.TILE_WIDTH*Grass.CYCLE) == 0) {
      if(myLeft) {
	myCowboy.move(Grass.TILE_WIDTH*Grass.CYCLE, 0);
	myCurrentLeftX += (Grass.TILE_WIDTH*Grass.CYCLE);
	for(int i = 0; i < myLeftTumbleweeds.length; i++) {
	  myLeftTumbleweeds[i].move(Grass.TILE_WIDTH*Grass.CYCLE, 0);
	}
	for(int i = 0; i < myRightTumbleweeds.length; i++) {
	  myRightTumbleweeds[i].move(Grass.TILE_WIDTH*Grass.CYCLE, 0);
	}
      } else {
	myCowboy.move(-(Grass.TILE_WIDTH*Grass.CYCLE), 0);
	myCurrentLeftX -= (Grass.TILE_WIDTH*Grass.CYCLE);
	for(int i = 0; i < myLeftTumbleweeds.length; i++) {
	  myLeftTumbleweeds[i].move(-Grass.TILE_WIDTH*Grass.CYCLE, 0);
	}
	for(int i = 0; i < myRightTumbleweeds.length; i++) {
	  myRightTumbleweeds[i].move(-Grass.TILE_WIDTH*Grass.CYCLE, 0);
	}
      }
    }
  }

  //-------------------------------------------------------
  //  game movements

  /**
   * Tell all of the moving components to advance.
   * @param gameTicks the remainaing number of times that 
   *        the main loop of the game will be executed 
   *        before the game ends.
   * @return the change in the score after the pieces 
   *         have advanced.
   */
  int advance(int gameTicks) {
    int retVal = 0;
    // first we move the view window 
    // (so we are showing a slightly different view of 
    // the manager's graphical area.)
    if(myLeft) {
      myCurrentLeftX--;
    } else {
      myCurrentLeftX++;
    }
    // now we tell the game objects to move accordingly.
    myGrass.advance(gameTicks);
    myCowboy.advance(gameTicks, myLeft);
    for(int i = 0; i < myLeftTumbleweeds.length; i++) {
      retVal += myLeftTumbleweeds[i].advance(myCowboy, gameTicks, 
		    myLeft, myCurrentLeftX, myCurrentLeftX + DISP_WIDTH);
      retVal -= myCowboy.checkCollision(myLeftTumbleweeds[i]);
    }
    for(int i = 0; i < myLeftTumbleweeds.length; i++) {
      retVal += myRightTumbleweeds[i].advance(myCowboy, gameTicks, 
           myLeft, myCurrentLeftX, myCurrentLeftX + DISP_WIDTH);
      retVal -= myCowboy.checkCollision(myRightTumbleweeds[i]);
    }
    // now we check if we have reached an edge of the viewable
    // area, and if so we move the view area and all of the 
    // game objects so that the game appears to wrap.
    wrap();
    return(retVal);
  }

  /**
   * Tell the cowboy to jump..
   */
  void jump() {
    myCowboy.jump();
  }

}
