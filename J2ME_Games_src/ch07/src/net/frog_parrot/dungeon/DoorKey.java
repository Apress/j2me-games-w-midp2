package net.frog_parrot.dungeon;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * This class represents doors and keys.
 * 
 * @author Carol Hamer
 */
public class DoorKey extends Sprite {

  //---------------------------------------------------------
  //    fields

  /**
   * The image file shared by all doors and keys.
   */
  public static Image myImage;

  /**
   * A code int that indicates the door or key's color.
   */
  int myColor;

  //---------------------------------------------------------
  //    get/set data

  /**
   * @return the door or key's color.
   */
  public int getColor() {
    return(myColor);
  }

  //---------------------------------------------------------
  //    constructor and initializer

  static {
    try {
      myImage = Image.createImage("/images/keys.png");
    } catch(Exception e) {
      throw(new RuntimeException(
	   "DoorKey.<init>-->failed to load image, caught " 
	   + e.getClass() + ": " + e.getMessage()));
    }
  }

  /**
   * Standard constructor sets the image to the correct frame 
   * (according to whether this is a door or a key and what 
   * color it should be) and then puts it in the correct location.
   */
  public DoorKey(int color, boolean isKey, int[] gridCoordinates) {
    super(myImage, DungeonManager.SQUARE_WIDTH, DungeonManager.SQUARE_WIDTH);
    myColor = color;
    int imageIndex = color * 2;
    if(isKey) {
      imageIndex++;
    }
    setFrame(imageIndex);
    setPosition(gridCoordinates[0] * DungeonManager.SQUARE_WIDTH, 
		gridCoordinates[1] * DungeonManager.SQUARE_WIDTH);
  }

}
