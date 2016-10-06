package net.frog_parrot.checkers;

import java.util.Vector;

/**
 * This class takes care of the underlying logic and data of 
 * the checkers game being played.  That includes where 
 * all of the pieces are on the board and where it is okay 
 * for them to move to.  
 *
 * @author Carol Hamer
 */
public class CheckersGame {

  //-------------------------------------------------------
  //   static fields

  /**
   * The length of the checkerboard in the x-direction.
   */
  public static final byte X_LENGTH = 4;

  /**
   * The length of the checkerboard in the y-direction.
   */
  public static final byte Y_LENGTH = 8;

  //-------------------------------------------------------
  //   instance fields

  /**
   * a handle to the communications class that exchanges
   * data with the server.
   */
  private Communicator myCommunicator;

  /**
   * This array represents the black squares of the 
   * checkerboard.  The two dimensions of the array 
   * represent the two dimensions of the checkerboard.
   * The value represents what type of piece is on 
   * the square.
   * 0 = empty
   * 1 = local player's piece
   * 2 = local player's king
   * -1 = remote player's piece
   * -2 = remote player's king
   */
  private byte[][] myGrid;

  /**
   * If the user has currently selected a piece to move, 
   * this is its X grid coordinate. (-1 if none selected)
   */
  private byte mySelectedX = -1;

  /**
   * If the user has currently selected a piece to move, 
   * this is its Y grid coordinate.(-1 if none selected)
   */
  private byte mySelectedY = -1;

  /**
   * If the user has currently selected a possible 
   * destination square for a move, this is its X coordinate..
   * (-1 if none selected)
   */
  private byte myDestinationX = -1;

  /**
   * If the user has currently selected a possible 
   * destination square for a move, this is its Y coordinate..
   * (-1 if none selected)
   */
  private byte myDestinationY = -1;

  /**
   * This Vector contains the coordinates of all of the 
   * squares that the player could currently move to.
   */
  private Vector myPossibleMoves = new Vector(4);

  /**
   * Whether or not the currently displayed checkers has 
   * been completed.
   */
  private boolean myGameOver = false;

  /**
   * Whether or not it is currently this player's turn.
   */
  private boolean myTurn = false;

  /**
   * This is true if the player has just jumped and can 
   * jump again.
   */
  private boolean myIsJumping = false;

  //-------------------------------------------------------
  //   get/set data
  
  /**
   * get the piece on the given grid square.
   */
  byte getPiece(byte x, byte y) {
    return(myGrid[x][y]);
  }

  /**
   * This is callsed by CheckersCanvas to determine if 
   * the square is currently selected (as containing 
   * a piece to move or a destination square).
   */
  boolean isSelected(byte x, byte y) {
    boolean retVal = false;
    if((x == mySelectedX) && (y == mySelectedY)) {
      retVal = true;
    } else if((x == myDestinationX) && (y == myDestinationY)) {
      retVal = true;
    }
    return(retVal);
  }

  /**
   * This tells whether or not the keystrokes should currently
   * be taken into account.
   */
  boolean isMyTurn() {
    boolean retVal = false;
    if((!myGameOver) && ((myTurn) || (myIsJumping))) {
      retVal = true;
    }
    return(retVal);
  }

  /**
   * This tells whether or not the game has ended.
   */
  boolean getGameOver() {
    boolean retVal = false;
    if(myGameOver) {
      retVal = true;
    }
    return(retVal);
  }

  /**
   * tell the CheckersGame that the other player has ended the game.
   */
  void setGameOver() {
    myGameOver = true;
  }

  /**
   * set the communicator object.
   */
  void setCommunicator(Communicator comm) {
    myCommunicator = comm;
  }

  //-------------------------------------------------------
  //   initialization

  /**
   * Constructor puts the pieces in their initial positions:
   */
  CheckersGame() {
    myGrid = new byte[X_LENGTH][];
    for(byte i = 0; i < myGrid.length; i++) {
      myGrid[i] = new byte[Y_LENGTH];
      for(byte j = 0; j < myGrid[i].length; j++) {
	if(j < 3) {
	  // fill the top of the board with remote players
	  myGrid[i][j] = -1;
	} else if(j > 4) {
	  // fill the bottom of the board with local players
	  myGrid[i][j] = 1;
	}
      }
    }
  }

  /**
   * This is called just before the player makes the 
   * first move.
   */
  void start() {
    mySelectedX = 0;
    mySelectedY = 5;
    myTurn = true;
    getMoves(mySelectedX, mySelectedY, myPossibleMoves, false);
  }

  //-------------------------------------------------------
  //   move the opponent
  // to be called by Communicator

  /**
   * This is called when the opponent wants to move
   * its piece.
   * @param moveData an array of four bytes:
   * moveData[0] = opponent's initial X coordinate
   * moveData[1] = opponent's initial Y coordinate
   * moveData[2] = opponent's destination X coordinate
   * moveData[3] = opponent's destination Y coordinate
   */
  void moveOpponent(byte[] moveData) {
    // since both players appear on their own screens 
    // as the red side (bottom of the screen), we need 
    // to invert the opponent's move:
    moveData[0] = (new Integer(X_LENGTH - moveData[0] - 1)).byteValue();
    moveData[2] = (new Integer(X_LENGTH - moveData[2] - 1)).byteValue();
    moveData[1] = (new Integer(Y_LENGTH - moveData[1] - 1)).byteValue();
    moveData[3] = (new Integer(Y_LENGTH - moveData[3] - 1)).byteValue();
    myGrid[moveData[2]][moveData[3]] 
      = myGrid[moveData[0]][moveData[1]];
    myGrid[moveData[0]][moveData[1]] = 0;
    // deal with an opponent's jump:
    if((moveData[1] - moveData[3] > 1) || 
       (moveData[3] - moveData[1] > 1)) {
      int jumpedY = (moveData[1] + moveData[3])/2;
      int jumpedX = moveData[0];
      int parity = moveData[1] % 2;
      if((parity > 0) && (moveData[2] > moveData[0])) {
	jumpedX++;
      } else if((parity == 0) && (moveData[0] > moveData[2])) {
	jumpedX--;
      }
      myGrid[jumpedX][jumpedY] = 0;
    }
    // if the opponent reaches the far side, 
    // make him a king:
    if(moveData[3] == Y_LENGTH - 1) {
      myGrid[moveData[2]][moveData[3]] = -2;
    }
  }

  /**
   * This is called when the opponent's turn is over.
   * Note that the turn doesn't automatically end after 
   * the opponent moves because the opponent may make 
   * a double or triple jump.
   */
  void endOpponentTurn() {
    myTurn = true;
    // Now begin the local player's turn: 
    // First select the first local piece that can be 
    // moved. (rightPressed will select an appropriate 
    // piece or end the game if the local player has 
    // no possible moves to make)
    mySelectedX = 0;
    mySelectedY = 0;
    myDestinationX = -1;
    myDestinationY = -1;
    rightPressed();
    // the local player's thread has been waiting 
    // for the opponent's turn to end.  
    synchronized(this) {
      notify();
    }
  }

  //-------------------------------------------------------
  //   handle keystrokes
  // to be called by CheckersCanvas

  /**
   * if the left button is pressed, this method takes 
   * the correct course of action depending on the situation.
   */
  void leftPressed() {
    // in the first case the user has not yet selected a 
    // piece to move:
    if(myDestinationX == -1) {
      // find the next possible piece (to the left) 
      // that can move:
      selectPrevious();
      // if selectPrevious fails to fill myPossibleMoves, that 
      // means that the local player cannot move, so the game
      // is over:
      if(myPossibleMoves.size() == 0) {
	myCommunicator.endGame();
      }
    } else {
      // if the user has already selected a piece to move, 
      // we give the options of where the piece can move to:
      for(byte i = 0; i < myPossibleMoves.size(); i++) {
	byte[] coordinates = (byte[])myPossibleMoves.elementAt(i);
	if((coordinates[0] == myDestinationX) && 
	   (coordinates[1] == myDestinationY)) {
	  i++;
	  i = (new Integer(i % myPossibleMoves.size())).byteValue();
	  coordinates = (byte[])myPossibleMoves.elementAt(i);
	  myDestinationX = coordinates[0];
	  myDestinationY = coordinates[1];
	  break;
	}
      }
    }
  }

  /**
   * if the left button is pressed, this method takes 
   * the correct course of action depending on the situation.
   */
  void rightPressed() {
    // in the first case the user has not yet selected a 
    // piece to move:
    if(myDestinationX == -1) {
      // find the next possible piece that can 
      // move:
      selectNext();
      // if selectNext fails to fill myPossibleMoves, that 
      // means that the local player cannot move, so the game
      // is over:
      if(myPossibleMoves.size() == 0) {
	myCommunicator.endGame();
      }
    } else {
      // if the user has already selected a piece to move, 
      // we give the options of where the piece can move to:
      for(byte i = 0; i < myPossibleMoves.size(); i++) {
	byte[] coordinates = (byte[])myPossibleMoves.elementAt(i);
	if((coordinates[0] == myDestinationX) && 
	   (coordinates[1] == myDestinationY)) {
	  i++;
	  i = (new Integer(i % myPossibleMoves.size())).byteValue();
	  coordinates = (byte[])myPossibleMoves.elementAt(i);
	  myDestinationX = coordinates[0];
	  myDestinationY = coordinates[1];
	  break;
	}
      }
    }
  }

  /**
   * If no piece is selected, we select one.  If a piece 
   * is selected, we move it.
   */
  void upPressed() {
    // in the first case the user has not yet selected a 
    // piece to move:
    if(myDestinationX == -1) {
      fixSelection();
    } else {
      // if the source square and destination square 
      // have been chosen, we move the piece:
      move();
    }
  }

  /**
   * If the user decided not to move the selected piece 
   * (and instead wants to select again), this undoes 
   * the selection. This corresponds to pressing the 
   * DOWN key.
   */
  void deselect() {
    // if the player has just completed a jump and 
    // could possibly jump again but decides not to 
    // (i.e. deselects), then the turn ends:
    if(myIsJumping) {
      mySelectedX = -1;
      mySelectedY = -1;
      myDestinationX = -1;
      myDestinationY = -1;
      myIsJumping = false;
      myTurn = false;
      myCommunicator.endTurn();
    } else {
      // setting the destination coordinates to -1 
      // is the signal that the the choice of which 
      // piece to move can be modified:
      myDestinationX = -1;
      myDestinationY = -1;
    }
  }

  //-------------------------------------------------------
  //   internal square selection methods

  /**
   * When the player has decided that the currently selected
   * square contains the piece he really wants to move, this 
   * is called. This method switches to the mode where 
   * the player selects the destination square of the move.
   */
  private void fixSelection() {
    byte[] destination = (byte[])myPossibleMoves.elementAt(0);
    // setting the destination coordinates to valid 
    // coordinates is the signal that the user is done 
    // selecting the piece to move and now is choosing 
    // the destination square:
    myDestinationX = destination[0];
    myDestinationY = destination[1];
  }

  /**
   * This method starts from the currently selected square 
   * and finds the next square that contains a piece that 
   * the player can move.
   */
  private void selectNext() {
    // Test the squares one by one (starting from the 
    // currently selected square) until we find a square 
    // that contains one of the local player's pieces 
    // that can move:
    byte testX = mySelectedX;
    byte testY = mySelectedY;
    while(true) {
      testX++;
      if(testX >= X_LENGTH) {
	testX = 0;
	testY++;
	testY = (new Integer(testY % Y_LENGTH)).byteValue();
      }
      getMoves(testX, testY, myPossibleMoves, false);
      if((myPossibleMoves.size() != 0) || 
	   ((testX == mySelectedX) && (testY == mySelectedY))) {
	mySelectedX = testX;
	mySelectedY = testY;
	break;
      }
    }
  }

  /**
   * This method starts from the currently selected square 
   * and finds the next square (to the left) that contains 
   * a piece that the player can move.
   */
  private void selectPrevious() {
    // Test the squares one by one (starting from the 
    // currently selected square) until we find a square 
    // that contains one of the local player's pieces 
    // that can move:
    byte testX = mySelectedX;
    byte testY = mySelectedY;
    while(true) {
      testX--;
      if(testX < 0) {
	testX += X_LENGTH;
	testY--;
	if(testY < 0) {
	  testY += Y_LENGTH;
	}
      }
      getMoves(testX, testY, myPossibleMoves, false);
      if((myPossibleMoves.size() != 0) || 
	 ((testX == mySelectedX) && (testY == mySelectedY))) {
	mySelectedX = testX;
	mySelectedY = testY;
	break;
      }
    }
  }

  //-------------------------------------------------------
  //   internal utilities

  /**
   * Once the user has selected the move to make, this 
   * updates the data accordingly.
   */
  private void move() {
    // the piece that was on the source square is 
    // now on the destination square:
    myGrid[myDestinationX][myDestinationY] 
      = myGrid[mySelectedX][mySelectedY];
    // the source square is emptied:
    myGrid[mySelectedX][mySelectedY] = 0;
    if(myDestinationY == 0) {
      myGrid[myDestinationX][myDestinationY] = 2;
    }
    // tell the communicator to inform the other player 
    // of this move:
    myCommunicator.move(mySelectedX, mySelectedY, 
			myDestinationX, myDestinationY);
    // deal with the special rules for jumps::
    if((mySelectedY - myDestinationY > 1) || 
       (myDestinationY - mySelectedY > 1)) {
      int jumpedY = (mySelectedY + myDestinationY)/2;
      int jumpedX = mySelectedX;
      int parity = mySelectedY % 2;
      // the coordinates of the jumped square depend on 
      // what row we're in:
      if((parity > 0) && (myDestinationX > mySelectedX)) {
	jumpedX++;
      } else if((parity == 0) && (mySelectedX > myDestinationX)) {
	jumpedX--;
      }
      // remove the piece that was jumped over:
      myGrid[jumpedX][jumpedY] = 0;
      // now get ready to jump again if possible:
      mySelectedX = myDestinationX;
      mySelectedY = myDestinationY;
      myDestinationX = -1;
      myDestinationY = -1;
      // see if another jump is possible.
      // The "true" argument tells the program to return 
      // only jumps because the player can go again ONLY 
      // if there's a jump:
      getMoves(mySelectedX, mySelectedY, myPossibleMoves, true);
      // if there's another jump possible with the same piece, 
      // allow the player to continue jumping:
      if(myPossibleMoves.size() != 0) {
	myIsJumping = true;
	byte[] landing = (byte[])myPossibleMoves.elementAt(0);
	myDestinationX = landing[0];
	myDestinationY = landing[1];
      } else {
	myTurn = false;
	myCommunicator.endTurn();
      }
    } else {
      // since it's not a jump, we just end the turn 
      // by deselecting everything.
      mySelectedX = -1;
      mySelectedY = -1;
      myDestinationX = -1;
      myDestinationY = -1;
      myPossibleMoves.removeAllElements();
      myTurn = false;
      // tell the other player we're done:
      myCommunicator.endTurn();
    }
  }
  
  /**
   * Given a square on the grid, get the coordinates 
   * of one of the adjoining (diagonal) squares.
   * 0 = top left
   * 1 = top right
   * 2 = bottom left
   * 3 = bottom right.
   * @return the coordinates or null if the desired corner 
   * is off the board.
   */
  private byte[] getCornerCoordinates(byte x, byte y, byte corner) {
    byte[] retArray = null;
    if(corner < 2) {
      y--;
    } else {
      y++;
    }
    // Where the corner is on the grid depends on 
    // whether this is an odd row or an even row:
    if((corner % 2 == 0) && (y % 2 != 0)) {
      x--;
    } else if((corner % 2 != 0) && (y % 2 == 0)) {
      x++;
    }
    try {
      if(myGrid[x][y] > -15) {
	// we don't really care about the value, this
	// if statement is just there to get it to 
	// throw if the coordinates aren't on the board.
	retArray = new byte[2];
	retArray[0] = x;
	retArray[1] = y;
      }
    } catch(ArrayIndexOutOfBoundsException e) {
      // this throws if the coordinates do not correspond 
      // to a square on the board. It's not a problem, 
      // so we do nothing--we just return null instead 
      // of returning coordinates since no valid 
      // coordinates correspond to the desired corner.
    }
    return(retArray);
  }
  
  /**
   * Determines where the piece in the given 
   * grid location can move.  Clears the Vector
   * and fills it with the locations that 
   * the piece can move to.
   * @param jumpsOnly if we should return only moves that 
   *        are jumps.
   */
  private void getMoves(byte x, byte y, Vector toFill, boolean jumpsOnly) {
    toFill.removeAllElements();
    // if the square does not contain one of the local player's 
    // pieces, then there are no corresponding moves and we just
    // return an empty vector.
    if(myGrid[x][y] <= 0) {
      return;
    }
    // check each of the four corners to see if the 
    // piece can move there:
    for(byte i = 0; i < 4; i++) {
      byte[] coordinates = getCornerCoordinates(x, y, i);
      // if the coordinate array is null, then the corresponding 
      // corner is off the board and we don't deal with it.
      // The later two conditions in the following if statement
      // ensure that either the move is a forward move or the 
      // current piece is a king:
      if((coordinates != null) &&
	 ((myGrid[x][y] > 1) || (i < 2))) {
	// if the corner is empty (and we're not looking 
	// for just jumps), then this is a possible move
	// so we add it to the vector of moves:
	if((myGrid[coordinates[0]][coordinates[1]] == 0) && (! jumpsOnly)) {
	  toFill.addElement(coordinates);
	  // if the space is occupied by an opponent, see if we can jump it:
	} else if(myGrid[coordinates[0]][coordinates[1]] < 0) {
	  byte[] jumpLanding = getCornerCoordinates(coordinates[0], 
						 coordinates[1], i);
	  // if the space on the far side of the opponent's piece
	  // is on the board and is unoccupied, then a jump 
	  // is possible, so we add it to the vector of moves:
	  if((jumpLanding != null) && 
	     (myGrid[jumpLanding[0]][jumpLanding[1]] == 0)) {
	    toFill.addElement(jumpLanding);
	  }
	}
      }
    } // end for loop
  }
  
}
