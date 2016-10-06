package net.frog_parrot.http;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;

/**
 * This class contacts a servelt to test the HTTP connection.
 *
 * @author Carol Hamer
 */
public class MessageThread extends Thread {

  //--------------------------------------------------------
  //  fields

  /**
   * This is the URL to contact.
   */
   public static final String SERVER_URL 
  = "https://127.0.0.1:8443/games/TwoWayServlet";

  /**
   * Whether or not the main thread would like this thread 
   * to stop.
   */
  boolean myShouldStop;

  //--------------------------------------------------------
  //  instance fields
  //   these are used by the thread when downloading 
  //   boards to display a possible error message.

  /**
   * The canvas to write messages on..
   */
  MessageCanvas myCanvas;

  //--------------------------------------------------------
  //  initialization

  /**
   * Constructor sets the canvas for messages
   */
  MessageThread(MessageCanvas canvas) {
    myCanvas = canvas;
  }

  //--------------------------------------------------------
  //  data transfer methods

  /**
   * stops the test.
   */
  synchronized void requestStop() {
    myShouldStop = true;
    this.notify();
  }

  /**
   * Makes a HTTP connection to the server and reads the data.
   */
  public void run() {
    //System.out.println("MessageThread.run-->entered");
    // we sync on the class because we don't want multiple 
    // instances simultaneously attempting to download
    synchronized(this.getClass()) {
      ContentConnection connection = null;
      DataInputStream dis = null;
      DataOutputStream dos = null;
      byte count = 3;
      try {
	connection = (ContentConnection)Connector.open(SERVER_URL);
	((HttpsConnection)connection).setRequestMethod(HttpConnection.POST);
	//connection.setRequestMethod(HttpConnection.POST);
	//System.out.println("MessageThread.run-->posting");
	//System.out.println("MessageThread.run-->connection = " + connection);
	dos = connection.openDataOutputStream();
	//System.out.println("MessageThread.run-->dos = " + dos);
	dos.write(count);
	//System.out.println("MessageThread.run-->wrote = " + count);
	dos.flush();
	myCanvas.setMessage("sent: " + count);
	//System.out.println("MessageThread.run-->dos flushed");
        int rc = ((HttpConnection)connection).getResponseCode();
	dis = connection.openDataInputStream();
	//System.out.println("MessageThread.run-->dis = " + dis);
	int available = 1;
	//System.out.println("MessageThread.run-->available = " + available);
	int received = 0;
	if(available > 0) {
	  byte[] data = new byte[available];
	  dis.readFully(data);
	  received = data[0];
	  myCanvas.setMessage("received: " + data[0]);
	}
      } catch(Exception e) {
	e.printStackTrace();
	// if this fails, it is almost undoubtedly 
	// a communication problem (server down, etc.)
	// we need to give the right message to the user:
	myCanvas.setMessage(e.getMessage());
      } finally {
	try {
	  if(dis != null) {
	    dis.close();
	  }
	  if(dos != null) {
	    dos.close();
	  }
	  if(connection != null) {
	    connection.close();
	  }
	} catch(Exception e) {
	  // if this throws, at least we made our best effort 
	  // to close everything up....
	}
      }
    }
  }

}
