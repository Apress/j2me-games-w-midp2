package net.frog_parrot.dungeon;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

import net.frog_parrot.util.DataConverter;

/**
 * This class contacts a remote server in order to 
 * downolad data for new game boards and stores 
 * them locally..
 *
 * @author Carol Hamer
 */
public class BoardReader extends Thread {

  //--------------------------------------------------------
  //  fields

  /**
   * This is the name of the local datastore on the CLDC device.
   */
  public static final String LOCAL_DATASTORE = "BoardData";

  /**
   * This is the URL to contact.
   * IMPORTANT: if the client and server are not running on the 
   * same machine, before compiling, the following URL
   * must be changed to the correct URL of the 
   * machine running the DungeonDownload servlet.
   */
  public static final String SERVER_URL 
    = "http://127.0.0.1:8080/games/DungeonDownload";

  /**
   * This is the size of the byte array containing 
   * all the info for one board..
   */
  public static final int DATA_LENGTH = 48;

  //--------------------------------------------------------
  //  instance fields
  //   these are used by the thread when downloading 
  //   boards to display a possible error message.

  /**
   * The MIDlet subclass, used to set the Display 
   * in the case where an error message needs to be sent..
   */
  private Dungeon myDungeon;

  /**
   * The Canvas subclass, used to set the Display 
   * in the case where an error message needs to be sent..
   */
  private DungeonCanvas myCanvas;

  //--------------------------------------------------------
  //  initialization

  /**
   * Constructor is used only when the program wants 
   * to spawn a data-fetching thread, not for merely 
   * reading local data with static methods.
   */
  BoardReader(Dungeon dungeon, DungeonCanvas canvas) {
    myDungeon = dungeon;
    myCanvas = canvas;
  }

  //--------------------------------------------------------
  //  local data methods
  //   note that these methods are static and do 
  //   not run on a separate thread even though this 
  //   class is a subclass of Thread

  /**
   * @return the number of boards currently stored in the 
   * device memory. (this does not include the hard-coded board)
   */
  static int getNumBoards() {
    RecordStore store = null;
    int retVal = 0;
    try {
      // if the record store does not yet exist, don't 
      // create it
      store = RecordStore.openRecordStore(LOCAL_DATASTORE, false);
      if(store != null) {
	retVal = store.getNumRecords();
      }
    } catch(Exception e) {
    } finally {
      try {
	if(store != null) {
	  store.closeRecordStore();
	}
      } catch(Exception e) {
	// if the record store is open this shouldn't throw.
      }
    }
    return(retVal);
  }

  /**
   * @return the byte array that gives the board that 
   * has the number boardNum (if it is found). returns null
   * if there is no board in memory that has the given number.
   */
  static byte[] getBoardData(int boardNum) {
    RecordStore store = null;
    byte[] retArray = null;
    try {
      // if the record store does not yet exist, don't 
      // create it
      store = RecordStore.openRecordStore(LOCAL_DATASTORE, false);
      if((store != null) && (store.getNumRecords() >= boardNum)) {
	retArray = store.getRecord(boardNum);
      }
    } catch(Exception e) {
    } finally {
      try {
	if(store != null) {
	  store.closeRecordStore();
	}
      } catch(Exception e) {
	// if the record store is open this shouldn't throw.
      }
    }
    return(retArray);
  }

  /**
   * Saves the data of a board being downloaded from the internet
   */
  static void saveBoardData(byte[] data) throws Exception {
    RecordStore store = null;
    try {
      // if the record store does not yet exist, 
      // create it
      store = RecordStore.openRecordStore(LOCAL_DATASTORE, true);
      store.addRecord(data, 0, data.length);
    } finally {
      try {
	if(store != null) {
	  store.closeRecordStore();
	}
      } catch(Exception e) {
	// if the record store is open this shouldn't throw.
      }
    }
  }

  //--------------------------------------------------------
  //  download methods

  /**
   * Makes a HTTP connection to the server and gets data
   * for more boards.
   */
  public void run() {
    // we sync on the class because we don't want multiple 
    // instances simultaneously attempting to download
    synchronized(this.getClass()) {
      ContentConnection connection = null;
      DataInputStream dis = null;
      DataOutputStream dos = null;
      try {
	connection = (ContentConnection)Connector.open(SERVER_URL);
	// send the number of local boards to the server 
	// so the server will know which boards to send:
	int numBoards = getNumBoards();
	dos = connection.openDataOutputStream();
	// munBoards is an int but it is transferred as a 
	// byte.  It should therefore not be more than 15.
	dos.write(numBoards);
	// flush to send the message:
	dos.flush();
	// connection.getLength() returns the value of the 
	// content-length header, not the number of bytes 
	// available to read.  The server must set this header
	// if the client wants to use it.
	// Here numBoards is the number 
	// of boards that will be read from the downloaded data.
	numBoards = ((int)connection.getLength())/DATA_LENGTH;
	dis = connection.openDataInputStream();
	for(int i = 0; i < numBoards; i++) {
	  byte[] data = new byte[DATA_LENGTH];
	  dis.readFully(data);
	  saveBoardData(data);
	}
      } catch(Exception e) {
	// if this fails, it is almost undoubtedly 
	// a communication problem (server down, etc.)
	// we need to give the right message to the user:
	Alert alert = new Alert("download failed", 
		    "please try again later", null, AlertType.INFO);
	// We set the timeout to forever so this Alert will 
	// have a default dismiss command. When the user 
	// presses the Alert.DISMISS_COMMAND, the displayable
	// myCanvas will become current (see setCurrent() below):
	alert.setTimeout(Alert.FOREVER);
	myCanvas.setNeedsRepaint();
	// the second arg tells the Display to go to 
	// myCanvas when the user dismisses the alert
	Display.getDisplay(myDungeon).setCurrent(alert, myCanvas);
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
