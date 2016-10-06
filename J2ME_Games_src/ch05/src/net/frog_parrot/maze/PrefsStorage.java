package net.frog_parrot.maze;

import javax.microedition.rms.*;

/**
 * This class helps to store and retrieve the data about 
 * the maze size preferences.
 *
 * This is a utility class that does not contain instance data, 
 * so to simplify acess all of the methods are static.
 * 
 * @author Carol Hamer
 */
public class PrefsStorage {

  //---------------------------------------------------------
  //   static fields

  /**
   * The name of the datastore.
   */
  public static final String STORE = "SizePrefs";

  //---------------------------------------------------------
  //   business methods

  /**
   * This gets the preferred square size from the stored data.
   */
  static int getSquareSize() {
    // if data retrieval fails, the default value is 5
    int retVal = 5;
    RecordStore store = null;
    try {
      // if the record store does not yet exist, we 
      // send "false" so it won't bother to create it.
      store = RecordStore.openRecordStore(STORE, false);
      if((store != null) && (store.getNumRecords() > 0)) {
	// the first record has id number 1
	// (In fact this program stores only one record)
	byte[] rec = store.getRecord(1);
	retVal = rec[0];
      }
    } catch(Exception e) {
      // data storage is not critical for this game and we're 
      // not creating a log, so if data retrieval fails, we 
      // just skip it and move on.
    } finally {
      try {
	store.closeRecordStore();
      } catch(Exception e) {
	// if the record store is open this shouldn't throw.
      }
    }
    return(retVal);
  }

  /**
   * This saves the preferred square size.
   */
  static void setSquareSize(int size) {
    RecordStore store = null;
    try {
      // since we are storing the int as a single byte, 
      // it is very important that it's value be less than 
      // 128.  In fact in real life the value would never 
      // get anywhere near this high, but I'm adding this 
      // little size check as a last line of defense against 
      // errors:
      if(size > 127) {
	size = 127;
      }
      // if the record store does not yet exist, the second 
      // arg "true" tells it to create.
      store = RecordStore.openRecordStore(STORE, true);
      byte[] record = new byte[1];
      record[0] = (new Integer(size)).byteValue();
      int numRecords = store.getNumRecords();
      if(numRecords > 0) {
	store.setRecord(1, record, 0, 1);
      } else {
	store.addRecord(record, 0, 1);
      }
    } catch(Exception e) {
      // data storage is not critical for this game and we're 
      // not creating a log, so if data storage fails, we 
      // just skip it and move on.
    } finally {
      try {
	store.closeRecordStore();
      } catch(Exception e) {
	// if the record store is open this shouldn't throw.
      }
    }
  }

}
