package net.frog_parrot.dungeon;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * This class handles the graphics objects.
 * 
 * @author Carol Hamer
 */
public class DungeonManager extends LayerManager {

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
   * The height of this object's visible region. 
   */
  static int DISP_HEIGHT;

  /**
   * the (right or left)  distance the player 
   * goes in a single keystroke.
   */
  static final int MOVE_LENGTH = 8;

  /**
   * The width of the square tiles that this game is divided into.
   * This is the width of the stone walls as well as the princess and 
   * the ghost.
   */
  static final int SQUARE_WIDTH = 24;

  /**
   * The jump index that indicates that no jump is 
   * currently in progress..
   */
  static final int NO_JUMP = -6;

  /**
   * The maximum speed for the player's fall..
   */
  static final int MAX_FREE_FALL = 3;

  //---------------------------------------------------------
  //   game object fields

  /**
   * the handle back to the canvas.
   */
  private DungeonCanvas myCanvas;

  /**
   * the background dungeon.
   */
  private TiledLayer myBackground;

  /**
   * the player.
   */
  private Sprite myPrincess;

  /**
   * the goal.
   */
  private Sprite myCrown;

  /**
   * the doors.
   */
  private DoorKey[] myDoors;

  /**
   * the keys.
   */
  private DoorKey[] myKeys;

  /**
   * the key currently held by the player.
   */
  private DoorKey myHeldKey;

  /**
   * The leftmost x-coordinate that should be visible on the 
   * screen in terms of this objects internal coordinates.
   */
  private int myViewWindowX;

  /**
   * The top y-coordinate that should be visible on the 
   * screen in terms of this objects internal coordinates.
   */
  private int myViewWindowY;

  /**
   * Where the princess is in the jump sequence.
   */
  private int myIsJumping = NO_JUMP;

  /**
   * Whether or not the screen needs to be repainted.
   */
  private boolean myModifiedSinceLastPaint = true;

  /**
   * Which board we're playing on.
   */
  private int myCurrentBoardNum = 0;

  //-----------------------------------------------------
  //    gets/sets

  /**
   * Tell the layer manager that it needs to repaint.
   */
  public void setNeedsRepaint() {
    myModifiedSinceLastPaint = true;
  }

  //-----------------------------------------------------
  //    initialization
  //    set up or save game data.

  /**
   * Constructor merely sets the data.
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
   * @param canvas the DungeonCanvas that this LayerManager 
   * should appear on.
   */
  public DungeonManager(int x, int y, int width, int height, 
			DungeonCanvas canvas) throws Exception {
    myCanvas = canvas;
    CANVAS_X = x;
    CANVAS_Y = y;
    DISP_WIDTH = width;
    DISP_HEIGHT = height;
    // create a decoder object that creates the dungeon and 
    // its associated Sprites from data.  
    BoardDecoder decoder = new BoardDecoder(myCurrentBoardNum);
    // get the background TiledLayer
    myBackground = decoder.getLayer();
    // get the coordinates of the square that the princess 
    // starts on.
    int[] playerCoords = decoder.getPlayerSquare();
    // create the player sprite
    myPrincess = new Sprite(Image.createImage("/images/princess.png"), 
			    SQUARE_WIDTH, SQUARE_WIDTH);
    myPrincess.setFrame(1);
    // we define the reference pixel to be in the middle 
    // of the princess image so that when the princess turns 
    // from right to left (and vice versa) she does not 
    // appear to move to a different location.
    myPrincess.defineReferencePixel(SQUARE_WIDTH/2, 0);
    // the dungeon is a 16x16 grid, so the array playerCoords
    // gives the player's location in terms of the grid, and 
    // then we multiply those coordinates by the SQUARE_WIDTH
    // to get the precise pixel where the player should be 
    // placed (in terms of the LayerManager's coordinate system)
    myPrincess.setPosition(SQUARE_WIDTH * playerCoords[0], 
			   SQUARE_WIDTH * playerCoords[1]);
    // we append all of the Layers (TiledLayer and Sprite) 
    // so that this LayerManager will paint them when 
    // flushGraphics is called.
    append(myPrincess);
    // get the coordinates of the square where the crown 
    // should be placed.
    int[] goalCoords = decoder.getGoalSquare();
    myCrown = new Sprite(Image.createImage("/images/crown.png"));
    myCrown.setPosition((SQUARE_WIDTH * goalCoords[0]) + (SQUARE_WIDTH/4), 
			(SQUARE_WIDTH * goalCoords[1]) + (SQUARE_WIDTH/2));
    append(myCrown);
    // The decoder creates the door and key sprites and places 
    // them in the correct locations in terms of the LayerManager's
    // coordinate system.
    myDoors = decoder.createDoors();
    myKeys = decoder.createKeys();
    for(int i = 0; i < myDoors.length; i++) {
      append(myDoors[i]);
    }
    for(int i = 0; i < myKeys.length; i++) {
      append(myKeys[i]);
    }
    // append the background last so it will be painted first.
    append(myBackground);
    // this sets the view screen so that the player is 
    // in the center.
    myViewWindowX = SQUARE_WIDTH * playerCoords[0] 
      - ((DISP_WIDTH - SQUARE_WIDTH)/2);
    myViewWindowY = SQUARE_WIDTH * playerCoords[1] 
      - ((DISP_HEIGHT - SQUARE_WIDTH)/2);
    // a number of objects are created in order to set up the game,
    // but they should be eliminated to free up memory:
    decoder = null;
    System.gc();
  }

  /**
   * sets all variables back to their initial positions.
   */
  void reset() throws Exception {
    // first get rid of the old board:
    for(int i = 0; i < myDoors.length; i++) {
      remove(myDoors[i]);
    }
    myHeldKey = null;
    for(int i = 0; i < myKeys.length; i++) {
      remove(myKeys[i]);
    }
    remove(myBackground);
    // now create the new board:
    myCurrentBoardNum++;
    // in this version we go back to the beginning if 
    // all boards have been completed.
    if(myCurrentBoardNum == BoardDecoder.getNumBoards()) {
      myCurrentBoardNum = 0;
    }
    // we create a new decoder object to read and interpret 
    // all of the data for the current board.
    BoardDecoder decoder = new BoardDecoder(myCurrentBoardNum);
    // get the background TiledLayer
    myBackground = decoder.getLayer();
    // get the coordinates of the square that the princess 
    // starts on.
    int[] playerCoords = decoder.getPlayerSquare();
    // the dungeon is a 16x16 grid, so the array playerCoords
    // gives the player's location in terms of the grid, and 
    // then we multiply those coordinates by the SQUARE_WIDTH
    // to get the precise pixel where the player should be 
    // placed (in terms of the LayerManager's coordinate system)
    myPrincess.setPosition(SQUARE_WIDTH * playerCoords[0], 
			   SQUARE_WIDTH * playerCoords[1]);
    myPrincess.setFrame(1);
    // get the coordinates of the square where the crown 
    // should be placed.
    int[] goalCoords = decoder.getGoalSquare();
    myCrown.setPosition((SQUARE_WIDTH * goalCoords[0]) + (SQUARE_WIDTH/4), 
			(SQUARE_WIDTH * goalCoords[1]) + (SQUARE_WIDTH/2));
    // The decoder creates the door and key sprites and places 
    // them in the correct locations in terms of the LayerManager's
    // coordinate system.
    myDoors = decoder.createDoors();
    myKeys = decoder.createKeys();
    for(int i = 0; i < myDoors.length; i++) {
      append(myDoors[i]);
    }
    for(int i = 0; i < myKeys.length; i++) {
      append(myKeys[i]);
    }
    // append the background last so it will be painted first.
    append(myBackground);
    // this sets the view screen so that the player is 
    // in the center.
    myViewWindowX = SQUARE_WIDTH * playerCoords[0] 
      - ((DISP_WIDTH - SQUARE_WIDTH)/2);
    myViewWindowY = SQUARE_WIDTH * playerCoords[1] 
      - ((DISP_HEIGHT - SQUARE_WIDTH)/2);
    // a number of objects are created in order to set up the game,
    // but they should be eliminated to free up memory:
    decoder = null;
    System.gc();
  }

  /**
   * sets all variables back to the position in the saved game.
   * @return the time on the clock of the saved game.
   */
  int revertToSaved() throws Exception {
    int retVal = 0;
    // first get rid of the old board:
    for(int i = 0; i < myDoors.length; i++) {
      remove(myDoors[i]);
    }
    myHeldKey = null;
    for(int i = 0; i < myKeys.length; i++) {
      remove(myKeys[i]);
    }
    remove(myBackground);
    // now get the info of the saved game
    // only one game is saved at a time, and the GameInfo object 
    // will read the saved game's data from memory.
    GameInfo info = new GameInfo();
    if(info.getIsEmpty()) {
      // if no game has been saved, we start from the beginning.
      myCurrentBoardNum = 0;
      reset();
    } else {
      // get the time on the clock of the saved game.
      retVal = info.getTime();
      // get the number of the board the saved game was on.
      myCurrentBoardNum = info.getBoardNum();
      // create the BoradDecoder that gives the data for the 
      // desired board.
      BoardDecoder decoder = new BoardDecoder(myCurrentBoardNum);
      // get the background TiledLayer
      myBackground = decoder.getLayer();
      // get the coordinates of the square that the princess 
      // was on in the saved game.
      int[] playerCoords = info.getPlayerSquare();
      myPrincess.setPosition(SQUARE_WIDTH * playerCoords[0], 
			     SQUARE_WIDTH * playerCoords[1]);
      myPrincess.setFrame(1);
      // get the coordinates of the square where the crown 
      // should be placed (this is given by the BoardDecoder 
      // and not from the data of the saved game because the 
      // crown does not move during the game.
      int[] goalCoords = decoder.getGoalSquare();
      myCrown.setPosition((SQUARE_WIDTH * goalCoords[0]) + (SQUARE_WIDTH/4), 
			  (SQUARE_WIDTH * goalCoords[1]) + (SQUARE_WIDTH/2));
      // The decoder creates the door and key sprites and places 
      // them in the correct locations in terms of the LayerManager's
      // coordinate system.
      myDoors = decoder.createDoors();
      myKeys = decoder.createKeys();
      // get an array of ints that lists whether each door is 
      // open or closed in the saved game
      int[] openDoors = info.getDoorsOpen();
      for(int i = 0; i < myDoors.length; i++) {
	append(myDoors[i]);
	if(openDoors[i] == 0) {
	  // if the door was open, make it invisible
	  myDoors[i].setVisible(false);
	}
      }
      // the keys can be moved by the player, so we get their 
      // coordinates from the GameInfo saved data.
      int[][] keyCoords = info.getKeyCoords();
      for(int i = 0; i < myKeys.length; i++) {
	append(myKeys[i]);
	myKeys[i].setPosition(SQUARE_WIDTH * keyCoords[i][0], 
			     SQUARE_WIDTH * keyCoords[i][1]);
      }
      // if the player was holding a key in the saved game, 
      // we have the player hold that key and set it to invisible.
      int heldKey = info.getHeldKey();
      if(heldKey != -1) {
	myHeldKey = myKeys[heldKey];
	myHeldKey.setVisible(false);
      }
      // append the background last so it will be painted first.
      append(myBackground);
      // this sets the view screen so that the player is 
      // in the center.
      myViewWindowX = SQUARE_WIDTH * playerCoords[0] 
	- ((DISP_WIDTH - SQUARE_WIDTH)/2);
      myViewWindowY = SQUARE_WIDTH * playerCoords[1] 
	- ((DISP_HEIGHT - SQUARE_WIDTH)/2);
      // a number of objects are created in order to set up the game,
      // but they should be eliminated to free up memory:
      decoder = null;
      System.gc();
    }
    return(retVal);
  }

  /**
   * save the current game in progress.
   */
  void saveGame(int gameTicks) throws Exception {
    int[] playerSquare = new int[2];
    // the coordinates of the player are given in terms of 
    // the 16 x 16 dungeon grid. We divide the player's 
    // pixel coordinates to ge the right grid square.
    // If the player was not precisely alligned with a
    // grid square when the game was saved, the difference 
    // will be shaved off.
    playerSquare[0] = myPrincess.getX()/SQUARE_WIDTH;
    playerSquare[1] = myPrincess.getY()/SQUARE_WIDTH;  
    // save the coordinates of the current locations of
    // the keys, and if a key is currently held by the 
    // player, we save the info of which one it was.  
    int[][] keyCoords = new int[4][];
    int heldKey = -1;
    for(int i = 0; i < myKeys.length; i++) {
      keyCoords[i] = new int[2];
      keyCoords[i][0] = myKeys[i].getX()/SQUARE_WIDTH;
      keyCoords[i][1] = myKeys[i].getY()/SQUARE_WIDTH;
      if((myHeldKey != null) && (myKeys[i] == myHeldKey)) {
	heldKey = i;
      }
    }
    // save the information of which doors were open.
    int[] doorsOpen = new int[8];
    for(int i = 0; i < myDoors.length; i++) {
      if(myDoors[i].isVisible()) {
	doorsOpen[i] = 1;
      }
    }
    // take all of the information we've gathered and 
    // create a GameInfo object that will save the info 
    // in the device's memory.
    GameInfo info = new GameInfo(myCurrentBoardNum, gameTicks, 
				 playerSquare, keyCoords, 
				 doorsOpen, heldKey);
  }

  //-------------------------------------------------------
  //  graphics methods

  /**
   * paint the game graphic on the screen.
   */
  public void paint(Graphics g) throws Exception {
    // only repaint if something has changed:
    if(myModifiedSinceLastPaint) {
      g.setColor(DungeonCanvas.WHITE);
      // paint the background white to cover old game objects
      // that have changed position since last paint.
      // here coordinates are given 
      // with respect to the graphics (canvas) origin:
      g.fillRect(0, 0, DISP_WIDTH, DISP_HEIGHT);
      // here coordinates are given 
      // with respect to the LayerManager origin:
      setViewWindow(myViewWindowX, myViewWindowY, DISP_WIDTH, DISP_HEIGHT);
      // call the paint funstion of the superclass LayerManager
      // to paint all of the Layers
      paint(g, CANVAS_X, CANVAS_Y);
      // don't paint again until something changes:
      myModifiedSinceLastPaint = false;
    }
  }

  //-------------------------------------------------------
  //  game movements

  /**
   * respond to keystrokes by deciding where to move 
   * and then moving the pieces and the view window correspondingly.
   */
  void requestMove(int horizontal, int vertical) {
    if(horizontal != 0) {
      // see how far the princess can move in the desired 
      // horizontal direction (if not blocked by a wall 
      // or closed door)
      horizontal = requestHorizontal(horizontal);
    }
    // vertical < 0 indicates that the user has 
    // pressed the UP button and would like to jump.
    // therefore, if we're not currently jumping, 
    // we begin the jump.
    if((myIsJumping == NO_JUMP) && (vertical < 0)) {
      myIsJumping++;
    } else if(myIsJumping == NO_JUMP) {
      // if we're not jumping at all, we need to check 
      // if the princess should be falling:  
      // we (temporarily) move the princess down and see if that 
      // causes a collision with the floor:
      myPrincess.move(0, MOVE_LENGTH);
      // if the princess can move down without colliding 
      // with the floor, then we set the princess to 
      // be falling.  The variable myIsJumping starts 
      // negative while the princess is jumping up and 
      // is zero or positive when the princess is coming 
      // back down.  We therefore set myIsJumping to 
      // zero to indicate that the princess should start 
      // falling.
      if(! checkCollision()) {
	myIsJumping = 0;
      } 
      // we move the princess Sprite back to the correct 
      // position she was at before we (temporarily) moved 
      // her down to see if she would fall.
      myPrincess.move(0, -MOVE_LENGTH);
    }
    // if the princess is currently jumping or falling, 
    // we calculate the vertical distance she should move 
    // (taking into account the horizontal distance that 
    // she is also moving).
    if(myIsJumping != NO_JUMP) {
      vertical = jumpOrFall(horizontal);
    }
    // now that we've calculated how far the princess 
    // should move, we move her. (this is a call to 
    // another internal method of this method 
    // suite, it is not a built-in LayerManager method):
    move(horizontal, vertical);
  }

  /**
   * Internal to requestMove.  Calculates what the 
   * real horizontal distance moved should be 
   * after taking obstacles into account.
   * @return the horizontal distance that the 
   * player can move.
   */
  private int requestHorizontal(int horizontal) {
    // we (temporarily) move her to the right or left
    // and see if she hits a wall or a door:
    myPrincess.move(horizontal * MOVE_LENGTH, 0);
    if(checkCollision()) {
      // if she hits something, then she's not allowed 
      // to go in that direction, so we set the horizontal
      // move distance to zero and then move the princess 
      // back to where she was.
      myPrincess.move(-horizontal * MOVE_LENGTH, 0);
      horizontal = 0;
    } else {
      // if she doesn't hit anything then the move request 
      // succeeds, but we still move her back to the 
      // earlier position because this was just the checking 
      // phase.
      myPrincess.move(-horizontal * MOVE_LENGTH, 0);
      horizontal *= MOVE_LENGTH;
    }
    return(horizontal);
  }

  /**
   * Internal to requestMove.  Calculates the vertical 
   * change in the player's position if jumping or 
   * falling.
   * this method should only be called if the player is 
   * currently jumping or falling.
   * @return the vertical distance that the player should 
   * move this turn. (negative moves up, positive moves down)
   */
  private int jumpOrFall(int horizontal) {
    // by default we do not move vertically
    int vertical = 0;
    // The speed of rise or descent is computed using 
    // the int myIsJumping.  Since we are in a jump or 
    // fall, we advance the jump by one (which simulates 
    // the downward pull of gravity by slowing the rise 
    // or accellerating the fall) unless the player is 
    // already falling at maximum speed.  (a maximum 
    // free fall speed is necessary because otherwise 
    // it is possible for the player to fall right through 
    // the bottom of the maze...)
    if(myIsJumping <= MAX_FREE_FALL) {
      myIsJumping++;
    }
    if(myIsJumping < 0) {
      // if myIsJumping is negative, that means that 
      // the princess is rising.  We calculate the 
      // number of pixels to go up by raising 2 to 
      // the power myIsJumping (absolute value).  
      // note that we make the result negative because 
      // the up and down coordinates in Java are the 
      // reverse of the vertical coordinates we learned 
      // in math class: as you go up, the coordinate 
      // values go down, and as you go down the screen, 
      // the coordinate numbers go up.
      vertical = -(2<<(-myIsJumping));
    } else {
      // if myIsJumping is positive, the princess is falling.  
      // we calculate the distance to fall by raising two 
      // to the power of the absolute value of myIsJumping.
      vertical = (2<<(myIsJumping));
    }
    // now we temporarily move the princess the desired 
    // vertical distance (with the corresponding horizontal
    // distance also thrown in), and see if she hits anything:
    myPrincess.move(horizontal, vertical);
    if(checkCollision()) {
      // here we're in the case where she did hit something.
      // we move her back into position and then see what 
      // to do about it.
      myPrincess.move(-horizontal, -vertical);
      if(vertical > 0) {
	// in this case the player is falling.
	// so we need to determine precisely how 
	// far she can fall before she hit the bottom
	vertical = 0;
	// we temporarily move her the desired horizontal
	// distance while calculating the corresponding 
	// vertical distance.
	myPrincess.move(horizontal, 0);
	while(! checkCollision()) {
	  vertical++;
	  myPrincess.move(0, 1);
	}
	// now that we've calculated how far she can fall, 
	// we move her back to her earlier position
	myPrincess.move(-horizontal, -vertical);
	// we subtract 1 pixel from the distance calculated
	// because once she has actually collided with the 
	// floor, she's gone one pixel too far...
	vertical--;
	// now that she's hit the floor, she's not jumping 
	// anymore.
	myIsJumping = NO_JUMP;
      } else {
	// in this case we're going up, so she 
	// must have hit her head.
	// This next if is checking for a special 
	// case where there's room to jump up exactly 
	// one square.  In that case we increase the 
	// value of myIsJumping in order to make the 
	// princess not rise as high.  The details 
	// of the calculation in this case were found 
	// through trial and error:
	if(myIsJumping == NO_JUMP + 2) {
	  myIsJumping++;
	  vertical = -(2<<(-myIsJumping));
	  // now we see if the special shortened jump
	  // still makes her hit her head:
	  // (as usual, temporarily move her to test
	  // for collisions)
	  myPrincess.move(horizontal, vertical);
	  if(checkCollision()) {
	    // if she still hits her head even 
	    // with this special shortened jump, 
	    // then she was not meant to jump...
	    myPrincess.move(-horizontal, -vertical);
	    vertical = 0;
	    myIsJumping = NO_JUMP;
	  } else {
	    // now that we've chhecked for collisions,
	    // we move the player back to her earlier 
	    // position:
	    myPrincess.move(-horizontal, -vertical);
	  }
	} else {
	  // if she hit her head, then she should not 
	  // jump up.  
	  vertical = 0;
	  myIsJumping = NO_JUMP;
	}
      }
    } else {
      // since she didn't hit anything when we moved 
      // her, then all we have to do is move her back.
      myPrincess.move(-horizontal, -vertical);
    }
    return(vertical);
  }

  /**
   * Internal to requestMove.  Once the moves have been 
   * determined, actually perform the move.
   */
  private void move(int horizontal, int vertical) {
    // repaint only if we actually change something:
    if((horizontal != 0) || (vertical != 0)) {
      myModifiedSinceLastPaint = true;
    }
    // if the princess is moving left or right, we set 
    // her image to be facing the right direction:
    if(horizontal > 0) {
      myPrincess.setTransform(Sprite.TRANS_NONE);
    } else if(horizontal < 0) {
      myPrincess.setTransform(Sprite.TRANS_MIRROR);
    }
    // if she's jumping or falling, we set the image to 
    // the frame where the skirt is inflated:
    if(vertical != 0) {
      myPrincess.setFrame(0);
      // if she's just running, we alternate between the 
      // two frames:
    } else if(horizontal != 0) {
      if(myPrincess.getFrame() == 1) {
	myPrincess.setFrame(0);
      } else {
	myPrincess.setFrame(1);
      }
    }
    // move the position of the view window so that 
    // the player stays in the center:
    myViewWindowX += horizontal;
    myViewWindowY += vertical;
    // after all that work, we finally move the 
    // princess for real!!!
    myPrincess.move(horizontal, vertical);
  }

  //-------------------------------------------------------
  //  sprite interactions

  /**
   * Drops the currently held key and picks up another.
   */
  void putDownPickUp() {
    // we do not want to allow the player to put 
    // down the key in the air, so we verify that 
    // we're not jumping or falling first:
    if((myIsJumping == NO_JUMP) && 
       (myPrincess.getY() % SQUARE_WIDTH == 0)) {
      // since we're picking something up or putting 
      // something down, the display changes and needs 
      // to be repainted:
      setNeedsRepaint();
      // if the thing we're picking up is the crown, 
      // we're done, the player has won:
      if(myPrincess.collidesWith(myCrown, true)) {
	myCanvas.setGameOver();
	return;
      }
      // keep track of the key we're putting down in 
      // order to place it correctly:
      DoorKey oldHeld = myHeldKey;
      myHeldKey = null;
      // if the princess is on top of another key, 
      // that one becomes the held key and is hence 
      // made invisible:
      for(int i = 0; i < myKeys.length; i++) {
	// we check myHeldKey for null because we don't 
	// want to accidentally pick up two keys.
	if((myPrincess.collidesWith(myKeys[i], true)) && 
	   (myHeldKey == null)) {
	  myHeldKey = myKeys[i];
	  myHeldKey.setVisible(false);
	}
      }
      if(oldHeld != null) {
	// place the key we're putting down in the Princess's
	// current position and make it visible:
	oldHeld.setPosition(myPrincess.getX(), myPrincess.getY());
	oldHeld.setVisible(true);
      }
    }
  }

  /**
   * Checks of the player hits a stone wall or a door.
   */
  boolean checkCollision() {
    boolean retVal = false;
    // the "true" arg meand to check for a pixel-level 
    // collision (so merely an overlap in image 
    // squares does not register as a collision)
    if(myPrincess.collidesWith(myBackground, true)) {
      retVal = true;
    } else {
      // Note: it is not necessary to synchronize
      // this block because the thread that calls this 
      // method is the same as the one that puts down the 
      // keys, so there's no danger of the key being put down 
      // between the moment we check for the key and 
      // the moment we open the door:
      for(int i = 0; i < myDoors.length; i++) {
	// if she's holding the right key, then open the door
	// otherwise bounce off
	if(myPrincess.collidesWith(myDoors[i], true)) {
	  if((myHeldKey != null) && 
	     (myDoors[i].getColor() == myHeldKey.getColor())) {
	    setNeedsRepaint();
	    myDoors[i].setVisible(false);
	  } else {
	    // if she's not holding the right key, then 
	    // she has collided with the door just the same 
	    // as if she had collided with a wall:
	    retVal = true;
	  }
	}
      }
    }
    return(retVal);
  }

}
