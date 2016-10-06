package net.frog_parrot.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This is the servlet which a small device running the 
 * dungeon game can call to download more boards for the game.
 * 
 * @author Carol Hamer
 */
public class DungeonDownload extends HttpServlet {

  //------------------------------------------------------
  //   data 

  /**
   * The data in bytes that gives the various boards.
   * no more than 15 boards should be sent to the device  
   * in this version because the value that gives the 
   * number of remote boards in the transaction is 
   * stored in a byte.  If the value is greater than 
   * 15, there will be errors when transforming it to a byte.
   */
  static byte[][] myData = {
    { 0, 1, 122, 90, -62, 34, -43, 72, 
      -59, -29, 56, -55, 98, 126, -79, 61,
      -1, -1, -125, 1, -128, 17, -26, 29, -31, 57, -72, 1, -128, -51, 
      -100, 65, -124, 57, -2, 1, -126, 13, -113, 1, -97, 25, -127, 
      -99, -8, 1, -1, -1 },
    { 0, 2, 108, -24, 18, -26, 102, 30, -58, 46, -28, -88, 34, 
      -98, 97, -41,
      -1, -1, -96, 1, -126, 57, -9, 97, -127, 69, -119, 73, -127, 
      1, -109, 59, -126, 1, -26, 103, -127, 65, -103, 115, -127, 
      65, -25, 73, -128, 1, -1, -1 },
    { 0, 3, -114, 18, -34, 27, -39, -60, -76, -50, 118, 90, 82, 
      -88, 34, -74,
      -1, -1, -66, 1, -128, 121, -26, 125, -128, -123, -103, 29, 
      -112, 1, -109, 49, -112, 1, -116, -31, -128, 5, -122, 5, 
      -32, 13, -127, -51, -125, 1, -1, -1 },
  };

  //------------------------------------------------------
  //   implementation of servlet

  /**
   * send the doPut requests to doPost.
   */
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  /**
   * send the doGet requests to doPost.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  /**
   * send the data..
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      InputStream is = request.getInputStream();
      // read the number of boards currently on the device.
      int remoteBoards = is.read();
      System.out.println("DungeonDownload.doPost-->remoteBoards = " + remoteBoards);
      OutputStream os = response.getOutputStream();
      // use the number of remote boards to decide how many 
      // boards to send:
      if(myData.length > remoteBoards) {
	response.setContentLength(
	    myData[0].length*(myData.length - remoteBoards));
	for(int i = remoteBoards; i < myData.length; i++) {
	  os.write(myData[i]);
	}
      } else {
	response.setContentLength(0);
      }
      // send the message
      os.close();
      response.flushBuffer();
    } catch(EOFException eofe) {
      System.out.println("DungeonDownload.doPost-->caught " + eofe.getClass()
			 + ": " + eofe.getMessage());
      response.sendError(408, eofe.getMessage());
    } catch(IOException ioe) {
      System.out.println("DungeonDownload.doPost-->caught " + ioe.getClass()
			 + ": " + ioe.getMessage());
      ioe.printStackTrace(System.out);
      response.sendError(500, ioe.getClass() + ": " + ioe.getMessage());
    } catch(Exception e) {
      System.out.println("DungeonDownload.doPost-->caught " + e.getClass()
			 + ": " + e.getMessage());
      response.sendError(500, e.getClass() + ": " + e.getMessage());
    }
  }

}
