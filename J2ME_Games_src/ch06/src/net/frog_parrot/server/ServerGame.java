package net.frog_parrot.server;

import java.io.*;
import java.net.*;

/**
 * This class handles the communications between 
 * two players that are playing a game of Checkers 
 * against each other.
 *
 * @author Carol Hamer
 */
public class ServerGame extends Thread {

  //--------------------------------------------------------
  //  static fields

  /**
   * The int to signal that the game is to begin.
   */
  public static final byte START_GAME_FLAG = -4;

  /**
   * The int to signal that the game is to end.
   */
  public static final byte END_GAME_FLAG = -3;

  /**
   * The int to signal the end of a turn.
   */
  public static final byte END_TURN_FLAG = -2;

  //-------------------------------------------------------------
  //          instance fields

  /**
   * The socket that the server uses to communicate 
   * with the first player.
   */
  private Socket myPlayerSocket1;

  /**
   * The stream we write to when communicating with the
   * first player.
   */
  private OutputStream myOutput1;

  /**
   * The stream we read from when communicating with the
   * first player.
   */
  private InputStream myInput1;

  /**
   * The socket that the server uses to communicate 
   * with the second player.
   */
  private Socket myPlayerSocket2;

  /**
   * The stream we write to when communicating with the
   * second player.
   */
  private OutputStream myOutput2;

  /**
   * The stream we read from when communicating with the
   * second player.
   */
  private InputStream myInput2;

  /**
   * Messages are sent and received in sets of four bytes.
   */
  private byte[] myData = new byte[4];

  //-------------------------------------------------------------
  //          initialization

  /**
   * Constructor sets the first player and waits for 
   * the second.
   */
  ServerGame(Socket player1) {
    System.out.println("ServerGame.ServerGame");
    try {
      // start communications with the first player:
      myPlayerSocket1 = player1;
      myInput1 = player1.getInputStream();
      myOutput1 = player1.getOutputStream();
      // test the communications by sending an initial 
      // set of four bytes:
      myData[0] = START_GAME_FLAG;
      myOutput1.write(myData);
      System.out.println("ServerGame.ServerGame-->wrote to player 1");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Add a second player to the game.
   */
  void setSecondPlayer(Socket player2) {
    try {
      // start communications with the second player:
      myPlayerSocket2 = player2;
      myInput2 = player2.getInputStream();
      myOutput2 = player2.getOutputStream();
      // test the communications by sending an initial 
      // set of four bytes:
      myData[0] = START_GAME_FLAG;
      myOutput2.write(myData);
      System.out.println("ServerGame.setSecondPlayer-->" 
			 + "wrote back to player 2");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  //-------------------------------------------------------------
  //          business methods

  /**
   * play the game.
   */
  public void run() {
    try {
      // we write and tell the first player to go:
      myData[0] = START_GAME_FLAG;
      myOutput1.write(myData);
      // the main loop receives move information from 
      // one player and passes it along to the other player,
      // then does the same thing in reverse:
      while(true) {
	readFour(myInput1);
	if(myData[0] == END_GAME_FLAG) {
	  break;
	}
	while(myData[0] != END_TURN_FLAG) {
	  System.out.println("ServerGame.run-->read from player 1: " 
			     + myData[0] + ", "  + myData[1] + " to " 
			     +  myData[2] +", "  + myData[3]);
	  myOutput2.write(myData);
	  readFour(myInput1);
	}
	// since the turn is over, we write the end turn flag:
	myOutput2.write(myData);
	System.out.println("ServerGame.run-->player 1 done, wrote: " 
			   + myData[0]);
	// now it's the second player's turn:
	readFour(myInput2);
	if(myData[0] == END_GAME_FLAG) {
	  break;
	}
	while(myData[0] != END_TURN_FLAG) {
	  System.out.println("ServerGame.run-->read from player 2: " 
			     + myData[0] +", "  + myData[1] + " to " 
			     +  myData[2] +", "  + myData[3]);
	  myOutput1.write(myData);
	  readFour(myInput2);
	}
	// since the turn is over, we write the end turn flag:
	myOutput1.write(myData);
	System.out.println("ServerGame.run-->player 2 done, wrote: " 
			   + myData[0]);
      }
    } catch(Exception e) {
      // here we print the stack trace for information even 
      // though often the Exception just indicates that one 
      // player has left the game and is not an error...
      e.printStackTrace();
    } finally {
      // regardless of what knocked us out of the main 
      // game loop, we need to 
      // tell everyone that the game is over then close 
      // up all of the streams and sockets.
      myData[0] = END_GAME_FLAG;
      try {
	myOutput1.write(myData);
	System.out.println("ServerGame.run-->" 
			   + "sent end game to player 1, wrote: " + myData[0]);
      } catch(Exception ie) {
	// this will throw if player 1 has left the game, 
	// but it's not an error, so we don't bother with it.
      }
      try {
	myOutput2.write(myData);
	System.out.println("ServerGame.run-->" 
			   + "sent end game to player 2, wrote: " + myData[0]);
      } catch(Exception ie) {
	// this will throw if player 2 has left the game, 
	// but it's not an error, so we don't bother with it.
      }
      // even if we fail to write to one of 
      // the players, we want to close the sockets and streams.
      try {
	myOutput1.close();
	myOutput2.close();
	myInput1.close();
	myInput2.close();
	myPlayerSocket1.close();
	myPlayerSocket2.close();
      } catch(Exception ie) {
	ie.printStackTrace();
      }
    }
  }

  //-------------------------------------------------------------
  //          internal utilities

  /**
   * This method reads exactly four bytes off the stream 
   * and puts them in the array myData.  This method is 
   * used because I know that in this game the client 
   * always sends sets of four bytes, but the method 
   * read may return without reading all of them.
   * @throws Exception when the player corresponding 
   * to the InputStream disconnects.
   */
  private void readFour(InputStream istream) throws Exception {
    int total = 0;
    int numRead = 0;
    while(total < 4) {
      numRead = istream.read(myData, total, myData.length - total);
      if(numRead >= 0) {
	total += numRead;
	System.out.println("ServerGame.readFour-->read " + total + " bytes");
      } else {
	throw(new Exception("player ended game"));
      }
    }
  }


}
