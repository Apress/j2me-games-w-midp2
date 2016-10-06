package net.frog_parrot.hello;

import javax.microedition.lcdui.*;

/**
 * This class represents the region of the screen that has been allotted 
 * to the game.
 * 
 * @author Carol Hamer
 */
public class HelloCanvas extends Canvas {

  //---------------------------------------------------------
  //   fields

  /**
   * whether or not the screen should currently display the 
   * "hello world" message.
   */
  boolean mySayHello = true;

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * toggle the hello message.
   */
  void toggleHello() {
    mySayHello = !mySayHello;
    repaint();
  }

  //-------------------------------------------------------
  //  graphics methods

  /**
   * clear the screen and display the hello world message if appropriate.
   */
  public void paint(Graphics g) {
    // get the dimensions of the screen:
    int width = getWidth();
    int height = getHeight();
    // clear the screen (paint it white):
    g.setColor(0xffffff);
    // The first two args give the coordinates of the top 
    // left corner of the rectangle.  (0,0) corresponds 
    // to the top left corner of the screen.
    g.fillRect(0, 0, width, height);
    // display the hello world message if appropriate:.
    if(mySayHello) {
      Font font = g.getFont();
      int fontHeight = font.getHeight();
      int fontWidth = font.stringWidth("Hello World!");
      // set the text color to red:
      g.setColor(255, 0, 0);
      g.setFont(font);
      // write the string in the center of the screen
      g.drawString("Hello World!", (width - fontWidth)/2, 
		   (height - fontHeight)/2,
		   g.TOP|g.LEFT);
    }
  }

}


