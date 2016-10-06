package net.frog_parrot.jump;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * This class represents the player.
 *
 * @author Carol Hamer
 */
public class Cowboy extends Sprite {

  //---------------------------------------------------------
  //    dimension fields

  /**
   * The width of the cowboy's bounding rectangle.
   */
  static final int WIDTH = 32;

  /**
   * The height of the cowboy's bounding rectangle.
   */
  static final int HEIGHT = 48;

  /**
   * This is the order that the frames should be displayed 
   * for the animation.
   */
  static final int[] FRAME_SEQUENCE = { 3, 2, 1, 2 };

  //---------------------------------------------------------
  //    instance fields

  /**
   * the X coordinate of the cowboy where the cowboy starts 
   * the game.
   */
  private int myInitialX;

  /**
   * the Y coordinate of the cowboy when not jumping.
   */
  private int myInitialY;

  /**
   * The jump index that indicates that no jump is 
   * currently in progress..
   */
  private int myNoJumpInt = -6;

  /**
   * Where the cowboy is in the jump sequence.
   */
  private int myIsJumping = myNoJumpInt;

  /**
   * If the cowboy is currently jumping, this keeps track 
   * of how many points have been scored so far during 
   * the jump.  This helps the calculation of bonus points since
   * the points being scored depend on how many tumbleweeds 
   * are jumped in a single jump.
   */
  private int myScoreThisJump = 0;

  //---------------------------------------------------------
  //   initialization

  /**
   * constructor initializes the image and animation.
   */
  public Cowboy(int initialX, int initialY) throws Exception {
    super(Image.createImage("/images/cowboy.png"), 
	  WIDTH, HEIGHT);
    myInitialX = initialX;
    myInitialY = initialY;
    // we define the reference pixel to be in the middle 
    // of the cowboy image so that when the cowboy turns 
    // from right to left (and vice versa) he does not 
    // appear to move to a different location.
    defineReferencePixel(WIDTH/2, 0);
    setRefPixelPosition(myInitialX, myInitialY);
    setFrameSequence(FRAME_SEQUENCE);
  }

  //---------------------------------------------------------
  //   game methods

  /**
   * If the cowboy has landed on a tumbleweed, we decrease 
   * the score.
   */
  int checkCollision(Tumbleweed tumbleweed) {
    int retVal = 0;
    if(collidesWith(tumbleweed, true)) {
      retVal = 1;
      // once the cowboy has collided with the tumbleweed, 
      // that tumbleweed is done for now, so we call reset 
      // which makes it invisible and ready to be reused.
      tumbleweed.reset();
    }
    return(retVal);
  }

  /**
   * set the cowboy back to its initial position.
   */
  void reset() {
    myIsJumping = myNoJumpInt;
    setRefPixelPosition(myInitialX, myInitialY);
    setFrameSequence(FRAME_SEQUENCE);
    myScoreThisJump = 0;
    // at first the cowboy faces right:
    setTransform(TRANS_NONE);
  }

  //---------------------------------------------------------
  //   graphics

  /**
   * alter the cowboy image appropriately for this frame..
   */
  void advance(int tickCount, boolean left) {
    if(left) {
      // use the mirror image of the cowboy graphic when 
      // the cowboy is going towards the left.
      setTransform(TRANS_MIRROR);
      move(-1, 0);
    } else {
      // use the (normal, untransformed) image of the cowboy 
      // graphic when the cowboy is going towards the right.
      setTransform(TRANS_NONE);
      move(1, 0);
    }
    // this section advances the animation: 
    // every third time through the loop, the cowboy 
    // image is changed to the next image in the walking 
    // animation sequence:
    if(tickCount % 3 == 0) { // slow the animation down a little
      if(myIsJumping == myNoJumpInt) {
	// if he's not jumping, set the image to the next 
	// frame in the walking animation:
	nextFrame();
      } else {
	// if he's jumping, advance the jump:
	// the jump continues for several passes through 
	// the main game loop, and myIsJumping keeps track 
	// of where we are in the jump:
	myIsJumping++;
	if(myIsJumping < 0) {
	  // myIsJumping starts negative, and while it's 
	  // still negative, the cowboy is going up.  
	  // here we use a shift to make the cowboy go up a 
	  // lot in the beginning of the jump, and ascend 
	  // more and more slowly as he reaches his highest 
	  // position:
	  setRefPixelPosition(getRefPixelX(), 
			      getRefPixelY() - (2<<(-myIsJumping)));
	} else {
	  // once myIsJumping is negative, the cowboy starts 
	  // going back down until he reaches the end of the 
	  // jump sequence:
	  if(myIsJumping != -myNoJumpInt - 1) {
	    setRefPixelPosition(getRefPixelX(), 
				getRefPixelY() + (2<<myIsJumping));
	  } else {
	    // once the jump is done, we reset the cowboy to 
	    // his non-jumping position:
	    myIsJumping = myNoJumpInt;
	    setRefPixelPosition(getRefPixelX(), myInitialY);
	    // we set the image back to being the walking 
	    // animation sequence rather than the jumping image:
	    setFrameSequence(FRAME_SEQUENCE);
	    // myScoreThisJump keeps track of how many points 
	    // were scored during the current jump (to keep 
	    // track of the bonus points earned for jumping 
	    // multiple tumbleweeds).  Once the current jump is done, 
	    // we set it back to zero.  
	    myScoreThisJump = 0;
	  }
	}
      }
    }
  }

  /**
   * makes the cowboy jump.
   */
  void jump() {
    if(myIsJumping == myNoJumpInt) {
      myIsJumping++;
      // switch the cowboy to use the jumping image 
      // rather than the walking animation images:
      setFrameSequence(null);
      setFrame(0);
    }
  }

  /**
   * This is called whenever the cowboy clears a tumbleweed
   * so that more points are scored when more tumbleweeds 
   * are cleared in a single jump.
   */
  int increaseScoreThisJump() {
    if(myScoreThisJump == 0) {
      myScoreThisJump++;
    } else {
      myScoreThisJump *= 2;
    }
    return(myScoreThisJump);
  }

}
