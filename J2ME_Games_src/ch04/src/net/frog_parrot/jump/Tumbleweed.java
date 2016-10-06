package net.frog_parrot.jump;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * This class represents the tumbleweeds that the player 
 * must jump over.
 *
 * @author Carol Hamer
 */
public class Tumbleweed extends Sprite {

  //---------------------------------------------------------
  //   dimension fields

  /**
   * The width of the tumbleweed's bounding square.
   */
  static final int WIDTH = 16;

  //---------------------------------------------------------
  //    instance fields

  /**
   * whether or not this tumbleweed has been jumped over.
   * This is used to calculate the score.
   */
  private boolean myJumpedOver;

  /**
   * whether or not this tumbleweed enters from the left.
   */
  private boolean myLeft;

  /**
   * the Y coordinate of the tumbleweed.
   */
  private int myY;

  /**
   * the leftmost visible pixel.
   */
  private int myCurrentLeftBound;

  /**
   * the rightmost visible pixel.
   */
  private int myCurrentRightBound;

  //---------------------------------------------------------
  //   initialization

  /**
   * constructor initializes the image and animation.
   * @param left whether or not this tumbleweed enters from the left.
   */
  public Tumbleweed(boolean left) throws Exception {
    super(Image.createImage("/images/tumbleweed.png"), 
	  WIDTH, WIDTH);
    myY = JumpManager.DISP_HEIGHT - WIDTH - 2;
    myLeft = left;
    if(!myLeft) {
      setTransform(TRANS_MIRROR);
    }
    myJumpedOver = false;
    setVisible(false);
  }

  //---------------------------------------------------------
  //   game actions

  /**
   * Set the tumbleweed in motion if it is not currently visible.
   */
  synchronized boolean go() {
    boolean retVal = false;
    if(!isVisible()) {
      retVal = true;
      //System.out.println("Tumbleweed.go-->not visible");
      myJumpedOver = false;
      setVisible(true);
      // set the tumbleweed's position to the point 
      // where it just barely appears on the screen 
      // to that it can start approaching the cowboy:
      if(myLeft) {
	setRefPixelPosition(myCurrentRightBound, myY);
	move(-1, 0);
      } else {
	setRefPixelPosition(myCurrentLeftBound, myY);
	move(1, 0);
      }
    } else {
      //System.out.println("Tumbleweed.go-->visible");
    }
    return(retVal);
  }

  //---------------------------------------------------------
  //   graphics

  /**
   * move the tumbleweed back to its initial (inactive) state.
   */
  void reset() {
    setVisible(false);
    myJumpedOver = false;
  }

  /**
   * alter the tumbleweed image appropriately for this frame..
   * @param left whether or not the player is moving left
   * @return how much the score should change by after this 
   *         advance.
   */
  synchronized int advance(Cowboy cowboy, int tickCount, boolean left,
	      int currentLeftBound, int currentRightBound) {
    int retVal = 0;
    myCurrentLeftBound = currentLeftBound;
    myCurrentRightBound = currentRightBound;
    // if the tumbleweed goes outside of the display 
    // region, set it to invisible since it is 
    // no longer in use.
    if((getRefPixelX() - WIDTH >= currentRightBound) && (!myLeft)) {
      setVisible(false);
    } 
    if((getRefPixelX() + WIDTH <= currentLeftBound) && myLeft) {
      setVisible(false);
    } 
    if(isVisible()) {
      // when the tumbleweed is active, we advance the 
      // rolling animation to the next frame and then 
      // move the tumbleweed in the right direction across 
      // the screen.
      if(tickCount % 2 == 0) { // slow the animation down a little
	nextFrame();
      }
      if(myLeft) {
	move(-3, 0);
	// if the cowboy just passed the tumbleweed 
	// (without colliding with it) we increase the 
	// cowboy's score and set myJumpedOver to true 
	// so that no further points will be awarded 
	// for this tumbleweed until it goes offscreen 
	// and then is later reactivated:
	if((! myJumpedOver) && 
	   (getRefPixelX() < cowboy.getRefPixelX())) {
	  myJumpedOver = true;
	  retVal = cowboy.increaseScoreThisJump();
	}
      } else {
	move(3, 0);
	if((! myJumpedOver) && 
	   (getRefPixelX() > cowboy.getRefPixelX() + Cowboy.WIDTH)) {
	  myJumpedOver = true;
	  retVal = cowboy.increaseScoreThisJump();
	}
      }
    }
    return(retVal);
  }

}
