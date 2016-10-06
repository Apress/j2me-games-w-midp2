package net.frog_parrot.dungeon;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import javax.microedition.rms.*;

import net.frog_parrot.util.DataConverter;

/**
 * This class contains the data for a game currently in progress.
 * used to store a game and to resume a stored game.
 *
 * @author Carol Hamer
 */
public class GameInfo {

  //--------------------------------------------------------
  //  fields

  /**
   * The name of the datastore.
   */
  public static final String STORE = "GameData";

  /**
   * This is set to true if an attempt is made to 
   * read a game when no game has been saved.
   */
  boolean myNoDataSaved;

  /**
   * The number that indicates which board the player 
   * is currently on.
   */
  int myBoardNum;

  /**
   * The amount of time that has passed.
   */
  int myTime;

  /**
   * The coordinates of where the player is on the board.
   * coordinate values must be between 0 and 15.
   */
  int[] myPlayerSquare;

  /**
   * The coordinates of where the keys are currently found.
   * MUST BE four sets of two integer coordinates.  
   * coordinate values must be between 0 and 15.
   */
  int[][] myKeyCoords;

  /**
   * The list of which doors are currently open.
   * 0 = open
   * 1 = closed
   * WARNING: this array MUST have length 8.
   */
  int[] myDoorsOpen;

  /**
   * The number of the key that is currently being held 
   * by the player.  if no key is held, then the value is -1.
   */
  int myHeldKey;

  //--------------------------------------------------------
  //  data gets/sets

  /**
   * @return true if no saved game records were found.
   */
  boolean getIsEmpty() {
    return(myNoDataSaved);
  }

  /**
   * @return The number that indicates which board the player 
   * is currently on.
   */
  int getBoardNum() {
    return(myBoardNum);
  }

  /**
   * @return The number of the key that is currently being held 
   * by the player.  if no key is held, then the value is -1.
   */
  int getHeldKey() {
    return(myHeldKey);
  }

  /**
   * @return The amount of time that has passed.
   */
  int getTime() {
    return(myTime);
  }

  /**
   * @return The coordinates of where the player is on the board.
   * coordinate values must be between 0 and 15.
   */
  int[] getPlayerSquare() {
    return(myPlayerSquare);
  }

  /**
   * @return The coordinates of where the keys are currently found.
   * MUST BE four sets of two integer coordinates.  
   * coordinate values must be between 0 and 15.
   */
  int[][] getKeyCoords() {
    return(myKeyCoords);
  }

  /**
   * @return The list of which doors are currently open.
   * 0 = open
   * 1 = closed
   * WARNING: this array MUST have length 8.
   */
  int[] getDoorsOpen() {
    return(myDoorsOpen);
  }

  //--------------------------------------------------------
  //  constructors

  /**
   * This constructor records the game info of a game currently 
   * in progress.
   */
  GameInfo(int boardNum, int time, int[] playerSquare, int[][] keyCoords, 
	   int[] doorsOpen, int heldKey) throws Exception {
    myBoardNum = boardNum;
    myTime = time;
    myPlayerSquare = playerSquare;
    myKeyCoords = keyCoords; 
    myDoorsOpen = doorsOpen;
    myHeldKey = heldKey;
    encodeInfo();
  }

  /**
   * This constructor reads the game configuration from memory.
   * This is used to reconstruct a saved game.
   */
  GameInfo() {
    RecordStore store = null;
    try {
      // if the record store does not yet exist, don't 
      // create it
      store = RecordStore.openRecordStore(STORE, false);
      if((store != null) && (store.getNumRecords() > 0)) {
	// the first record has id number 1
	// it should also be the only record since this 
	// particular game stores only one game.
	byte[] data = store.getRecord(1);
	myBoardNum = data[0];
	myPlayerSquare = DataConverter.decodeCoords(data[1]);
	myKeyCoords = new int[4][];
	myKeyCoords[0] = DataConverter.decodeCoords(data[2]);
	myKeyCoords[1] = DataConverter.decodeCoords(data[3]);
	myKeyCoords[2] = DataConverter.decodeCoords(data[4]);
	myKeyCoords[3] = DataConverter.decodeCoords(data[5]);
	myDoorsOpen = DataConverter.decode8(data[6]);
	myHeldKey = data[7];
	byte[] fourBytes = new byte[4];
	System.arraycopy(data, 8, fourBytes, 0, 4);
	myTime = DataConverter.parseInt(fourBytes);
      } else {
	myNoDataSaved = true;
      }
    } catch(Exception e) {
      // this throws when the record store doesn't exist.
      // for that or any error, we assume no data is saved:
      myNoDataSaved = true;
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
  //  encoding method

  /**
   * Turn the data into a byte array and save it.
   */
  void encodeInfo() throws Exception {
    RecordStore store = null;
    try {
      byte[] data = new byte[12];
      data[0] = (new Integer(myBoardNum)).byteValue();
      data[1] = DataConverter.encodeCoords(myPlayerSquare);
      data[2] = DataConverter.encodeCoords(myKeyCoords[0]);
      data[3] = DataConverter.encodeCoords(myKeyCoords[1]);
      data[4] = DataConverter.encodeCoords(myKeyCoords[2]);
      data[5] = DataConverter.encodeCoords(myKeyCoords[3]);
      data[6] = DataConverter.encode8(myDoorsOpen, 0);
      data[7] = (new Integer(myHeldKey)).byteValue();
      byte[] timeBytes = DataConverter.intToFourBytes(myTime);
      System.arraycopy(timeBytes, 0, data, 8, 4);
      // if the record store does not yet exist, the second 
      // arg "true" tells it to create.
      store = RecordStore.openRecordStore(STORE, true);
      int numRecords = store.getNumRecords();
      if(numRecords > 0) {
	store.setRecord(1, data, 0, data.length);
      } else {
	store.addRecord(data, 0, data.length);
      }
    } catch(Exception e) {
      throw(e);
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

}
