package net.frog_parrot.dungeon;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

import net.frog_parrot.util.DataConverter;

/**
 * This class contains the data for the map of the dungeon..
 *
 * @author Carol Hamer
 */
public class BoardDecoder {

  //--------------------------------------------------------
  //  fields

  /**
   * The coordinates of where the player starts on the map 
   * in terms of the array indices.
   */
  int[] myPlayerSquare;


  /**
   * The coordinates of the goal (crown).
   */
  int[] myGoalSquare;

  /**
   * The coordinates of the doors.
   * the there should be two in a row of each color, 
   * following the same sequence as the keys.
   */
  int[][] myDoors;

  /**
   * The coordinates of the Keys.
   * the there should be of each color, 
   * following the same sequence as the doors.
   */
  int[][] myKeys;

  /**
   * The coordinates of the stone walls of the maze, 
   * encoded bit by bit.
   */
  TiledLayer myLayer;

  /**
   * This is the array of bytes for just the first board.  
   * encodes where to place the various items 
   * in the dungeon and the placement of the walls.
   */
  static byte[] myFirstBoard = {
      0, 0, -108, -100, -24, 65, 21, 58, 53, -54, -116, -58, -56, 
      -84, 115, -118,
      -1, -1, -128, 1, -103, -15, -128, 25, -97, -127, -128, 79, -14, 
      1, -126, 121, -122, 1, -113, -49, -116, 1, -100, -3, -124, 5, 
      -25, -27, -128, 1, -1, -1,
  };

  /**
   * The number of boards that are currently stored (including
   * the first board which is hard-coded).
   */
  int myNumBoards;

  //--------------------------------------------------------
  //  initialization

  /**
   * Constructor fills data fields by interpreting 
   * the data bytes.
   */
  public BoardDecoder(int boardNum) throws Exception {
    // we start by selecting the two dimensional 
    // array corresponding to the desired board:
    byte[] data = null;
    if(boardNum < 1) {
      data = myFirstBoard;
    } else {
      data = BoardReader.getBoardData(boardNum);
    }
    // The first two bytes give the version number and 
    // the board number, but we ignore them because
    // they are assumed to be correct.
    // The third byte of the first array is the first one 
    // we read: it gives the player's starting coordinates:
    myPlayerSquare = DataConverter.decodeCoords(data[2]);
    // the next byte gives the coordinates of the crown:
    myGoalSquare = DataConverter.decodeCoords(data[3]);
    // the next four bytes give the coordinates of the keys:
    myKeys = new int[4][];
    for(int i = 0; i < myKeys.length; i++) {
      myKeys[i] = DataConverter.decodeCoords(data[i + 4]);
    }
    // the next eight bytes give the coordinates of the doors:
    myDoors = new int[8][];
    for(int i = 0; i < myDoors.length; i++) {
      myDoors[i] = DataConverter.decodeCoords(data[i + 8]);
    }
    // now we create the TiledLayer object that is the 
    // background dungeon map:
    myLayer = new TiledLayer(16, 16, 
	  Image.createImage("/images/stone.png"), 
	  DungeonManager.SQUARE_WIDTH, DungeonManager.SQUARE_WIDTH);
    // now we call an internal utility that reads the array 
    // of data that gives the positions of the blocks in the 
    // walls of this dungeon:
    decodeDungeon(data, myLayer, 16);
  }

  //--------------------------------------------------------
  //  get/set data

  /**
   * get the coordinates of where the player starts on the map 
   * in terms of the array indices.
   */
  public int[] getPlayerSquare() {
    return(myPlayerSquare);
  }

  /**
   * get the coordinates of the goal crown
   * in terms of the array indices.
   */
  public int[] getGoalSquare() {
    return(myGoalSquare);
  }

  /**
   * get the tiled layer that gives the map of the dungeon.
   */
  public TiledLayer getLayer() {
    return(myLayer);
  }

  /**
   * Creates the array of door sprites. (call this only once to avoid 
   * creating redundant sprites).
   */
  DoorKey[] createDoors() {
    DoorKey[] retArray = new DoorKey[8];
    for(int i = 0; i < 4; i++) {
      retArray[2*i] = new DoorKey(i, false, myDoors[2*i]);
      retArray[2*i + 1] = new DoorKey(i, false, myDoors[2*i + 1]);
    }
    return(retArray);
  }

  /**
   * Creates the array of key sprites. (call this only once to avoid 
   * creating redundant sprites.)
   */
  DoorKey[] createKeys() {
    DoorKey[] retArray = new DoorKey[4];
    for(int i = 0; i < 4; i++) {
      retArray[i] = new DoorKey(i, true, myKeys[i]);
    }
    return(retArray);
  }

  //--------------------------------------------------------
  //  decoding utilities

  /**
   * Takes a dungeon given as a byte array and uses it 
   * to set the tiles of a tiled layer.
   * 
   * The TiledLayer in this case is a 16 x 16 grid 
   * in which each square can be either blank 
   * (value of 0) or can be filled with a stone block 
   * (value of 1).  Therefore each square requires only 
   * one bit of information.  Each byte of data in 
   * the array called "data" records the frame indices 
   * of eight squares in the grid.
   */
  static void decodeDungeon(byte[] data, TiledLayer dungeon, 
       int offset) throws Exception {
    if(data.length + offset < 32) {
      throw(new Exception(
	       "BoardDecoder.decodeDungeon-->not enough data!!!"));
    }
    // a frame index of zero indicates a blank square 
    // (this is always true in a TiledLayer).
    // This TiledLayer has only one possible (non-blank)
    // frame, so a frame index of 1 indicates a stone block
    int frame = 0;
    // Each of the 32 bytes in the data array records 
    // the frame indices of eight block in the 16 x 16 
    // grid.  Two bytes give one row of the dungeon, 
    // so we have the array index go from zero to 16 
    // to set the frame indices fro each of the 16 rows.
    for(int i = 0; i < 16; i++) {
      // The flag allows us to look at each bit individually
      // to determine if it is 1 or 0.  The number 128 
      // corresponds to the highest bit of a byte, so we 
      // start with that one.
      int flag = 128;
      // Here we check two bytes at the same time 
      // (the two bytes together correspond to one row 
      // of the dungeon).  We use a loop that checks 
      // the bytes bit by bit by performing a bitwise 
      // and (&) between the data byte and a flag:
      for(int j = 0; j < 8; j++) {
	if((data[offset + 2*i] & flag) != 0) {
	  frame = 1;
	} else {
	  frame = 0;
	}
	dungeon.setCell(j, i, frame);
	if((data[offset + 2*i + 1] & flag) != 0) {
	  frame = 1;
	} else {
	  frame = 0;
	}
	dungeon.setCell(j + 8, i, frame);
	// move the flag down one bit so that we can 
	// check the next bit of data on the next pass
	// through the loop:
	flag = flag >> 1;
      }
    }
  }

}
