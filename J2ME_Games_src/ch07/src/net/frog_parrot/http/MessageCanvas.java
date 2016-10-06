package net.frog_parrot.http;

import javax.microedition.lcdui.*;

/**
 * This class merely posts a message to teh screen
 * (like an Alert).
 * 
 * @author Carol Hamer
 */
public class MessageCanvas extends Canvas {

  //---------------------------------------------------------
  //   static fields

  /**
   * color constant
   */
  public static final int BLACK = 0;

  /**
   * color constant
   */
  public static final int WHITE = 0xffffff;

  /**
   * color constant.
   * (not quite bright red)
   */
  public static final int RED = 0xf96868;

  /**
   * color constant
   */
  public static final int GREY = 0xc6c6c6;

  /**
   * color constant
   */
  public static final int LT_GREY = 0xe5e3e3;

  /**
   * how many rows and columns the display is divided into.
   */
  public static final int GRID_WIDTH = 8;

  //---------------------------------------------------------
  //   instance fields

  /**
   * the string to write on the screen.
   */
  private String myMessage = "No message yet";

  //-----------------------------------------------------
  //    gets / sets

  /**
   * set the current display message.
   */
  void setMessage(String message) {
    myMessage = message;
    repaint();
    serviceRepaints();
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * This is called as soon as the application begins.
   */
  void start() {
    //myDisplay.setCurrent(this);
    repaint();
    serviceRepaints();
  }

  //-------------------------------------------------------
  //  graphics methods

  /**
   * Repaint the message..
   */
  protected void paint(Graphics g) {
    int width = getWidth();
    int height = getHeight();
    g.setColor(WHITE);
    // clear the board (including the region around
    // the board, which can get menu stuff and other 
    // garbage painted onto it...)
    g.fillRect(0, 0, width, height);
    // perform some calculations to place the text correctly:
    Font font = g.getFont();
    int fontHeight = font.getHeight();
    int fontWidth = font.stringWidth(myMessage);
    g.setColor(WHITE);
    g.fillRect((width - fontWidth)/2, (height - fontHeight)/2,
	       fontWidth + 2, fontHeight);
    // write in black
    g.setColor(BLACK);
    g.setFont(font);
    g.drawString(myMessage, (width - fontWidth)/2, 
		 (height - fontHeight)/2,
		 g.TOP|g.LEFT);
  }

}
