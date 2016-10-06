package net.frog_parrot.checkers;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

import net.frog_parrot.util.DataConverter;

/**
 * This class contacts a remote server in order to 
 * play a game of checkers against an opponent..
 *
 * @author Carol Hamer
 */
public class Communicator extends Thread {

  //--------------------------------------------------------
  //  static fields

  /**
   * This is the URL to contact.
   * IMPORTANT: before compiling, the following URL
   * must be changed to the correct URL of the 
   * machine running the server code.
   */
  public static final String SERVER_URL 
    = "socket://malbec:8007";

  /**
   * The int to signal that the game is to begin.
   */
  public static final byte START_GAME_FLAG = -4;

  /**
   * The byte to signal that the game is to end.
   */
  public static final byte END_GAME_FLAG = -3;

  /**
   * The byte to signal the end of a turn.
   */
  public static final byte END_TURN_FLAG = -2;

  //--------------------------------------------------------
  //  game instance fields

  /**
   * The MIDlet subclass, used to set the Display 
   * in the case where an error message needs to be sent..
   */
  private Checkers myCheckers;

  /**
   * The Canvas subclass, used to set the Display 
   * in the case where an error message needs to be sent..
   */
  private CheckersCanvas myCanvas;

  /**
   * The game logic class that we send the opponent's 
   * moves to..
   */
  private CheckersGame myGame;

  /**
   * Whether or not the MIDlet class has requested the 
   * game to end.
   */
  private boolean myShouldStop;

  //--------------------------------------------------------
  //  data exchange instance fields

  /**
   * The data from the local player that is to 
   * be sent to the opponent.
   */
  private byte[] myMove;

  /**
   * Whether or not the current turn is done and 
   * should be sent.
   */
  private boolean myTurnIsDone = true;

  //--------------------------------------------------------
  //  initialization

  /**
   * Constructor is used only when the program wants 
   * to spawn a data-fetching thread, not for merely 
   * reading local data with static methods.
   */
  Communicator(Checkers checkers, CheckersCanvas canvas, 
	       CheckersGame game) {
    myCheckers = checkers;
    myCanvas = canvas;
    myGame = game;
  }

  //--------------------------------------------------------
  //  methods called by CheckersGame to send move
  //    information to the opponent.

  /**
   * Stop the game entirely.  Notify the servlet that 
   * the user is exiting the game.
   */
  synchronized void endGame() {
    myShouldStop = true;
    if(myGame != null) {
      myGame.setGameOver();
    }
    notify();
  }

  /**
   * This is called when the player moves a piece.
   */
  synchronized void move(byte sourceX, byte sourceY, byte destinationX, 
		    byte destinationY) {
    myMove = new byte[4];
    myMove[0] = sourceX;
    myMove[1] = sourceY;
    myMove[2] = destinationX;
    myMove[3] = destinationY;
    myTurnIsDone = false;
    notify();
  }

  /**
   * This is called when the local player's turn is over.
   */
  synchronized void endTurn() {
    myTurnIsDone = true;
    notify();
  }

  //--------------------------------------------------------
  //  main communication method

  /**
   * Makes a connection to the server and sends and receives
   * information about moves.
   */
  public void run() {
    DataInputStream dis = null;
    DataOutputStream dos = null;
    SocketConnection conn = null;
    byte[] fourBytes = new byte[4];
    try {
      // tell the user that we're waiting for the other player to join:
      myCanvas.setWaitScreen(true);
      myCanvas.repaint();
      myCanvas.serviceRepaints();
      // now make the connection:
      conn = (SocketConnection)Connector.open(SERVER_URL);
      conn.setSocketOption(SocketConnection.KEEPALIVE, 1);
      dos = conn.openDataOutputStream();
      dis = conn.openDataInputStream();
      // we read four bytes to make sure the connection works...
      dis.readFully(fourBytes);
      if(fourBytes[0] != START_GAME_FLAG) {
	throw(new Exception("server-side error"));
      }
      // On this line it will block waiting for another 
      // player to join the game or make a move:
      dis.readFully(fourBytes);
      // if the server sends the start game flag again, 
      // that means that we start with the local player's turn.
      // Otherwise, we read the other player's first move from the 
      // stream:
      if(fourBytes[0] != START_GAME_FLAG) {
	// verify that the other player sent a move 
	// and not just a message ending the game...
	if(fourBytes[0] == END_GAME_FLAG) {
	  throw(new Exception("other player quit"));
	}
	// we move the opponent on the local screen.
	// then we read from the opponent again, 
	// in case there's a double-jump:
	while(fourBytes[0] != END_TURN_FLAG) {
	  myGame.moveOpponent(fourBytes);
	  dis.readFully(fourBytes);
	}
      }
      // now signal the local game that the opponent is done
      // so the board must be updated and the local player 
      // prompted to make a move:
      myGame.endOpponentTurn();
      myCanvas.setWaitScreen(false);
      myCanvas.repaint();
      myCanvas.serviceRepaints();
      // begin main game loop:
      while(! myShouldStop) {
	// now it's the local player's turn.
	// wait for the player to move a piece:
	synchronized(this) {
	  wait();
	}
	// after every wait, we check if the game 
	// ended while we were waiting...
	if(myShouldStop) {
	  break;
	}
	while(! myTurnIsDone) {
	  // send the current move:
	  if(myMove != null) {
	    dos.write(myMove, 0, myMove.length);
	    myMove = null;
	  }
	  // If the player can continue the move with a double 
	  // jump, we wait for the player to do it:
	  synchronized(this) {
	    // make sure the turn isn't done before we start waiting
	    // (the end turn notify might accidentally be called 
	    // before we start waiting...)
	    if(! myTurnIsDone) {
	      wait();
	    } 
	  }
	}
	// after every wait, we check if the game 
	// ended while we were waiting...
	if(myShouldStop) {
	  break;
	}
	// now we tell the other player the this player's 
	// turn is over:
	fourBytes[0] = END_TURN_FLAG;
	dos.write(fourBytes, 0, fourBytes.length);
	// now that we've sent the move, we wait for a response:
	dis.readFully(fourBytes);
	while((fourBytes[0] != END_TURN_FLAG) && 
	      (fourBytes[0] != END_GAME_FLAG) && (!myShouldStop)) {
	  // we move the opponent on the local screen.
	  // then we read from the opponent again, 
	  // in case there's a double-jump:
	  myGame.moveOpponent(fourBytes);
	  dis.readFully(fourBytes);
	}
	// if the other player has left the game, we tell the 
	// local user that the game is over.
	if((fourBytes[0] == END_GAME_FLAG) || (myShouldStop)) {
	  endGame();
	  break;
	}
	myGame.endOpponentTurn();
	myCanvas.repaint();
	myCanvas.serviceRepaints();
      } // end while loop
    } catch(Exception e) {
      // if there's an error, we display its messsage and 
      // end the game.
      myCheckers.errorMsg(e.getMessage());
    } finally {
      // now we send the information that we're leaving the game,
      // then close up and delete everything.
      try {
	if(dos != null) {
	  dos.write(END_GAME_FLAG);
	  dos.close();
	}
	if(dis != null) {
	  dis.close();
	}
	if(conn != null) {
	  conn.close();
	}
	dis = null;
	dos = null;
	conn = null;
      } catch(Exception e) {
	// if this throws, at least we made our best effort 
	// to close everything up....
      }
    }
    // one last paint job to display the "Game Over"
    myCanvas.repaint();
    myCanvas.serviceRepaints();
  }
    
}
