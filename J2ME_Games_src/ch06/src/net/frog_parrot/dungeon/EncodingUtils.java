package net.frog_parrot.dungeon;

import  net.frog_parrot.util.DataConverter;

/**
 * This class contains the data for the map of the dungeon.
 * This is a utility class that allows a developer to write 
 * the data for a board in a simple format, then this class 
 * encodes the data in a format that the game can use.
 *
 * note that the data that this class encodes is hard-coded.
 * that is because this class is intended to be used only a 
 * few times to encode the data.  Once the board data has been 
 * encoded, it never needs to be encoded again.  The encoding 
 * methods used in this class could be generalized to be used 
 * to create a board editor which would allow a user to easily
 * create new boards, but that is an exercise for another day...
 *
 * @author Carol Hamer
 */
public class EncodingUtils {

  //--------------------------------------------------------
  //  fields

  /**
   * data for which squares are filled and which are blank.
   * 0 = empty
   * 1 = filled
   */
  int[][] mySquares = {
    { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
    { 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1 },
    { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 1 },
    { 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1 },
    { 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1 },
    { 1, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1 },
    { 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 1 },
    { 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
  };

  /**
   * The coordinates of where the player starts on the map 
   * in terms of the array indices.
   */
  int[] myPlayerSquare = { 7, 10 };

  /**
   * The coordinates of the goal (crown).
   */
  int[] myGoalSquare = { 5, 10 };

  //--------------------------------------------------------
  //  get/set data

  /**
   * Creates the array of door sprites. (call this only once to avoid 
   * creating redundant sprites).
   */
  int[][] getDoorCoords() {
    int[][] retArray = new int[8][];
    for(int i = 0; i < retArray.length; i++) { 
      retArray[i] = new int[2];
    }
    // red
    retArray[0][0] = 12;
    retArray[0][1] = 5;
    retArray[1][0] = 14;
    retArray[1][1] = 3;
    // green
    retArray[2][0] = 3;
    retArray[2][1] = 8;
    retArray[3][0] = 12;
    retArray[3][1] = 9;
    // blue
    retArray[4][0] = 6;
    retArray[4][1] = 2;
    retArray[5][0] = 7;
    retArray[5][1] = 14;
    // yellow
    retArray[6][0] = 11;
    retArray[6][1] = 1;
    retArray[7][0] = 3;
    retArray[7][1] = 13;
    return(retArray);
  }

  /**
   * Creates the array of key sprites. (call this only once to avoid 
   * creating redundant sprites.)
   */
  int[][] getKeyCoords() {
    int[][] retArray = new int[4][];
    for(int i = 0; i < retArray.length; i++) { 
      retArray[i] = new int[2];
    }
    // red
    retArray[0][0] = 12;
    retArray[0][1] = 2;
    // green
    retArray[1][0] = 2;
    retArray[1][1] = 2;
    // blue
    retArray[2][0] = 13;
    retArray[2][1] = 5;
    // yellow
    retArray[3][0] = 4;
    retArray[3][1] = 8;
    return(retArray);
  }

  //--------------------------------------------------------
  //  encoding / decoding utilities

  /**
   * Encodes the entire dungeon.
   */
  byte[][] encodeDungeon() {
    byte[][] retArray = new byte[2][];
    retArray[0] = new byte[16];
    // the first byte is the version number:
    retArray[0][0] = 0;
    // the second byte is the board number:
    retArray[0][1] = 0;
    // the player's start square:
    retArray[0][2] = DataConverter.encodeCoords(myPlayerSquare);
    // the goal (crown) square:
    retArray[0][3] = DataConverter.encodeCoords(myGoalSquare);
    //encode the keys:
    int[][] keyCoords = getKeyCoords();
    for(int i = 0; i < keyCoords.length; i++) {
      retArray[0][i + 4] = DataConverter.encodeCoords(keyCoords[i]);
    }
    //encode the doors:
    int[][] doorCoords = getDoorCoords();
    for(int i = 0; i < doorCoords.length; i++) {
      retArray[0][i + 8] = DataConverter.encodeCoords(doorCoords[i]);
    }
    //encode the maze:
    try {
      retArray[1] = encodeDungeon(mySquares);
    } catch(Exception e) {
      e.printStackTrace();
    }
    return(retArray);
  }

  /**
   * Takes a dungeon given in terms of an array of 1s and 0s
   * and turns it into an array of bytes.
   * WARNING: the array MUST BE 16 X 16.
   */
  static byte[] encodeDungeon(int[][] dungeonMap) throws Exception {
    if((dungeonMap.length != 16) || (dungeonMap[0].length != 16)) {
      throw(new Exception("EncodingUtils.encodeDungeon-->must be 16x16!!!"));
    }
    byte[] retArray = new byte[32];
    for(int i = 0; i < 16; i++) {
      retArray[2*i] = DataConverter.encode8(dungeonMap[i], 0);
      retArray[2*i + 1] = DataConverter.encode8(dungeonMap[i], 8);
    }
    return(retArray);
  }

  //--------------------------------------------------------
  //  main prints the bytes to standard out.
  // (note that this class is not intended to be run as a MIDlet)

  /**
   * Prints the byte version of the board to standard out.
   */
  public static void main(String[] args) {
    try {
      EncodingUtils map = new EncodingUtils();
      byte[][] data = map.encodeDungeon();
      System.out.println("EncodingUtils.main-->dungeon encoded");
      System.out.print("{\n   " + data[0][0]);
      for(int i = 1; i < data[0].length; i++) {
	System.out.print(", " + data[0][i]);
      }
      for(int i = 1; i < data[1].length; i++) {
	System.out.print(", " + data[1][i]);
      }
      System.out.println("\n};");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}
