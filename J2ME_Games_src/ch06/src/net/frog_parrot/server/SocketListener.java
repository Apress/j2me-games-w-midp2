package net.frog_parrot.server;

import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * This class is a very simple example of a server that 
 * can communicate with the checkers game.
 * 
 * @author Carol Hamer
 */

public class SocketListener {

//-------------------------------------------------------------
//       static fields

  /**
   * The port number to listen on.
   */
  static int myPortNum = 8007;

//-------------------------------------------------------------
//          instance fields

  /**
   * Variable to tell the web server to stop.
   */
  private boolean myShouldStop = false;

  /**
   * If another player is currently waiting, 
   * this is a handle to the other player's 
   * game.
   */
  private ServerGame myCurrentServerGame;

//--------------------------------------------------------------------
//   business methods

  /**
   * Start listening.
   */
  public void listen() {
    try {
      ServerSocket ss;
      ss = new ServerSocket(myPortNum);
      System.out.println("SocketListener.run-->listening on port " 
			 + myPortNum);
      while(! myShouldStop) {
        Socket client = ss.accept();
	System.out.println("SocketListener.run-->accepted client socket");
	client.setKeepAlive(true);
	// The following block of code does not 
	// need to be synchronized because all 
	// calls to this method take place on 
	// the same thread.
	if(myCurrentServerGame == null) {
	  myCurrentServerGame = new ServerGame(client);
	} else {
	  myCurrentServerGame.setSecondPlayer(client);
	  myCurrentServerGame.start();
	  // note that even though we're setting the 
	  // handle to null, it won't be garbage 
	  // collected because it's a live thread.
	  // When the game terminates, the thread's run()
	  // method will return and then the class will 
	  // be garbage clooected.
	  myCurrentServerGame = null;
	}
      }
    } catch(Exception ioe) {
      System.out.println("SocketListener.run-->caught Exception: "
          + ioe.getMessage());
    }
  }

//--------------------------------------------------------------------
//   main

  /**
   * main starts the server.
   */
  public static void main(String[] args) {
    try {
      SocketListener sl = new SocketListener();
      sl.listen();
    } catch(Exception e) {
      e.printStackTrace();
      System.out.println("SocketListener.main-->"
          + "caught " + e.getClass() + ": " + e.getMessage());
    }
  }

}
