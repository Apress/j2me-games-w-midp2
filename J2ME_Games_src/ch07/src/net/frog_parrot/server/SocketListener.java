package net.frog_parrot.server;

import javax.net.ssl.*;
import java.security.KeyStore;

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
      //ss = new ServerSocket(myPortNum);
      ss = getSecureServerSocket();
      System.out.println("SocketListener.run-->listening on port " 
			 + myPortNum);
      while(! myShouldStop) {
        Socket client = ss.accept();
	System.out.println("SocketListener.run-->accepted client socket");
	client.setKeepAlive(true);
	if(myCurrentServerGame == null) {
	  myCurrentServerGame = new ServerGame(client);
	} else {
	  myCurrentServerGame.setSecondPlayer(client);
	  myCurrentServerGame.start();
	  // note that even though we're setting the 
	  // handle to null, it won't be garbage 
	  // collected because it's a live thread.
	  myCurrentServerGame = null;
	}
      }
    } catch(Exception ioe) {
      ioe.printStackTrace();
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

//--------------------------------------------------------------------
//   internal methods

  /**
   * get a secure server socket.
   */
  private static SSLServerSocket getSecureServerSocket() {
    SSLServerSocket retObj = null;
    try {
      // initialize the key manager
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      // read the keys into the keystore and initialize the context:
      KeyStore ks = KeyStore.getInstance("JCEKS");
      char[] passphrase = "changeit".toCharArray();
      ks.load(new FileInputStream("/home/carol/.keystore"), passphrase);
      //ks.load(new FileInputStream("/home/carol/j2me/book/garbageKeys"), passphrase);
      kmf.init(ks, passphrase);
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(kmf.getKeyManagers(), null, null);
      // get the factory and use it to create the socket:
      SSLServerSocketFactory ssf = context.getServerSocketFactory();
      retObj = (SSLServerSocket)(ssf.createServerSocket(myPortNum));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return(retObj);
  }

}
