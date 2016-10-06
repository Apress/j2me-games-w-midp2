package net.frog_parrot.jump;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * This class draws the background grass.
 *
 * @author Carol Hamer
 */
public class Grass extends TiledLayer {

  //---------------------------------------------------------
  //    dimension fields 
  //  (constant after initialization)

  /**
   * The width of the square tiles that make up this layer..
   */
  static final int TILE_WIDTH = 20;

  /**
   * This is the order that the frames should be displayed 
   * for the animation.
   */
  static final int[] FRAME_SEQUENCE = { 2, 3, 2, 4 };

  /**
   * This gives the number of squares of grass to put along 
   * the bottom of the screen.
   */
  static int COLUMNS;

  /**
   * After how many tiles does the background repeat.
   */
  static final int CYCLE = 5;

  /**
   * the fixed Y coordinate of the strip of grass.
   */
  static int TOP_Y;

  //---------------------------------------------------------
  //    instance fields

  /**
   * Which tile we are currently on in the frame sequence.
   */
  private int mySequenceIndex = 0;

  /**
   * The index to use in the static tiles array to get the 
   * animated tile..
   */
  private int myAnimatedTileIndex;

  //---------------------------------------------------------
  //   gets / sets

  /**
   * Takes the width of the screen and sets my columns 
   * to the correct corresponding number
   */
  static int setColumns(int screenWidth) {
    COLUMNS = ((screenWidth / 20) + 1)*3;
    return(COLUMNS);
  }

  //---------------------------------------------------------
  //   initialization

  /**
   * constructor initializes the image and animation.
   */
  public Grass() throws Exception {
    super(setColumns(JumpCanvas.DISP_WIDTH), 1, 
	  Image.createImage("/images/grass.png"), 
	  TILE_WIDTH, TILE_WIDTH);
    TOP_Y = JumpManager.DISP_HEIGHT - TILE_WIDTH;
    setPosition(0, TOP_Y);
    myAnimatedTileIndex = createAnimatedTile(2);
    for(int i = 0; i < COLUMNS; i++) {
      if((i % CYCLE == 0) || (i % CYCLE == 2)) {
	setCell(i, 0, myAnimatedTileIndex);
      } else {
	setCell(i, 0, 1);
      }
    }
  }

  //---------------------------------------------------------
  //   graphics

  /**
   * sets the grass back to its initial position..
   */
  void reset() {
    setPosition(-(TILE_WIDTH*CYCLE), TOP_Y);
    mySequenceIndex = 0;
    setAnimatedTile(myAnimatedTileIndex, FRAME_SEQUENCE[mySequenceIndex]);
  }

  /**
   * alter the background image appropriately for this frame..
   * @param left whether or not the player is moving left
   */
  void advance(int tickCount) {
    if(tickCount % 2 == 0) { // slow the animation down a little
      mySequenceIndex++;
      mySequenceIndex %= 4;
      setAnimatedTile(myAnimatedTileIndex, FRAME_SEQUENCE[mySequenceIndex]);
    }
  }

}
