package net.frog_parrot.util;

import java.io.*;

/**
 * This class is a set of simple utility functions that 
 * can be used to convert standard data types to bytes 
 * and back again.  It is used especially for data storage, 
 * but also for sending and receiving data.
 * 
 * @author Carol Hamer
 */
public class DataConverter {

  //--------------------------------------------------------
  //  utilities to encode small, compactly-stored small ints.

  /**
   * Encodes a coordinate pair into a byte.
   * @param coordPair a pair of integers to be compacted into
   * a single byte for storage.
   * WARNING: each of the two values MUST BE 
   * between 0 and 15 (inclusive).  This method does not 
   * verify the length of the array (which must be 2!) 
   * nor does it verify that the ints are of the right size.
   */
  public static byte encodeCoords(int[] coordPair) {
    // get the byte value of the first coordinate:
    byte retVal = (new Integer(coordPair[0])).byteValue();
    // move the first coordinate's value up to the top 
    // half of the storage byte:
    retVal = (new Integer(retVal << 4)).byteValue();
    // store the second coordinate in the lower half
    // of the byte:
    retVal += (new Integer(coordPair[1])).byteValue();
    return(retVal);
  }

  /**
   * Encodes eight ints into a byte.
   * This could be easily modified to encode eight booleans.
   * @param eight an array of at least eight ints.
   * WARNING: all values must be 0 or 1!  This method does 
   * not verify that the values are in the correct range 
   * nor does it verify that the array is long enough.
   * @param offset the index in the array eight to start
   * reading data from.  (should usually be 0)
   */
  public static byte encode8(int[] eight, int offset) {
    // get the byte value of the first int:
    byte retVal = (new Integer(eight[offset])).byteValue();
    // progressively move the data up one bit in the 
    // storage byte and then record the next int in
    // the lowest spot in the storage byte:
    for(int i = offset + 1; i < 8 + offset; i++) {
      retVal = (new Integer(retVal << 1)).byteValue();
      retVal += (new Integer(eight[i])).byteValue();
    }
    return(retVal);
  }

  //--------------------------------------------------------
  //  utilities to decode small, compactly-stored small ints.

  /**
   * Turns a byte into a pair of coordinates.
   */
  public static int[] decodeCoords(byte coordByte) {
    int[] retArray = new int[2];
    // we perform a bitwise and with the value 15 
    // in order to just get the bits of the lower
    // half of the byte:
    retArray[1] = coordByte & 15;
    // To get the bits of the upper half of the 
    // byte, we perform a shift to move them down:
    retArray[0] = coordByte >> 4;
    // bytes in Java are generally assumed to be 
    // signed, but in this coding algorithm we 
    // would like to treat them as unsigned: 
    if(retArray[0] < 0) {
      retArray[0] += 16;
    }
    return(retArray);
  }

  /**
   * Turns a byte into eight ints.
   */
  public static int[] decode8(byte data) {
    int[] retArray = new int[8];
    // The flag allows us to look at each bit individually
    // to determine if it is 1 or 0.  The number 128 
    // corresponds to the highest bit of a byte, so we 
    // start with that one.
    int flag = 128;
    // We use a loop that checks 
    // the data bit by bit by performing a bitwise 
    // and (&) between the data byte and a flag:
    for(int i = 0; i < 8; i++) {
      if((flag & data) != 0) {
	retArray[i] = 1;
      } else {
	retArray[i] = 0;
      }
      // move the flag down one bit so that we can 
      // check the next bit of data on the next pass
      // through the loop:
      flag = flag >> 1;
    }
    return(retArray);
  }


  //--------------------------------------------------------
  //  standard integer interpretation

  /**
   * Uses an input stream to convert an array of bytes to an int.
   */
  public static int parseInt(byte[] data) throws IOException {
    DataInputStream stream 
      = new DataInputStream(new ByteArrayInputStream(data));
    int retVal = stream.readInt();
    stream.close();
    return(retVal);
  }

  /**
   * Uses an output stream to convert an int to four bytes.
   */
  public static byte[] intToFourBytes(int i) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(i);
    baos.close();
    dos.close();
    byte[] retArray = baos.toByteArray();
    return(retArray);
  }

  //--------------------------------------------------------
  //  integer interpretation illustrated

  /**
   * Java appears to treat a byte as being signed when
   * returning it as an int--this function converts from
   * the signed value to the corresponding unsigned value.
   * This method is used by nostreamParseInt.
   */
  public static int unsign(int signed) {
    int retVal = signed;
    if(retVal < 0) {
      retVal += 256;
    }
    return(retVal);
  }

  /**
   * Takes an array of bytes and returns an int.
   * This version will return the same value as the 
   * method parseInt above.  This version is included 
   * in order to illustrate how Java encodes int values
   * in terms of bytes.
   * @param data an array of 1, 2, or 4 bytes.
   */
  public static int nostreamParseInt(byte[] data) {
    // byte 0 is the high byte which is assumed 
    // to be signed.  As we add the lower bytes 
    // one by one, we unsign them because because 
    // a single byte alone is interpreted as signed, 
    // but in an int only the top byte should be signed.
    // (note that the high byte is the first one in the array)
    int retVal = data[0];
    for(int i = 1; i < data.length; i++) {
      retVal = retVal << 8;
      retVal += unsign(data[i]);
    }
    return(retVal);
  }

  /**
   * Takes an arbitrary int and returns
   * an array of four bytes.
   * This version will return the same byte array 
   * as the method intToFourBytes above.  This version 
   * is included in order to illustrate how Java encodes 
   * int values in terms of bytes.
   */
  public static byte[] nostreamIntToFourBytes(int i) {
    byte[] fourBytes = new byte[4];
    // when you take the byte value of an int, it
    // only gives you the lowest byte.  So we 
    // get all four bytes by taking the lowest 
    // byte four times and moving the whole int 
    // down by one byte between each one.
    // (note that the high byte is the first one in the array)
    fourBytes[3] = (new Integer(i)).byteValue();
    i = i >> 8;
    fourBytes[2] = (new Integer(i)).byteValue();
    i = i >> 8;
    fourBytes[1] = (new Integer(i)).byteValue();
    i = i >> 8;
    fourBytes[0] = (new Integer(i)).byteValue();
    return(fourBytes);
  }


  /**
   * Takes an int between -32768 and 32767 and returns
   * an array of two bytes.  This does not verify that 
   * the argument is of the right size.  If the absolute
   * value of i is too high, it will not be encoded 
   * correctly.
   */
  public static byte[] nostreamIntToTwoBytes(int i) {
    byte[] twoBytes = new byte[2];
    // when you take the byte value of an int, it
    // only gives you the lowest byte.  So we 
    // get the lower two bytes by taking the lowest 
    // byte twice and moving the whole int 
    // down by one byte between each one.
    twoBytes[1] = (new Integer(i)).byteValue();
    i = i >> 8;
    twoBytes[0] = (new Integer(i)).byteValue();
    return(twoBytes);
  }

}
