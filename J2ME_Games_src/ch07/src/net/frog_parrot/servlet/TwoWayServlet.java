package net.frog_parrot.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This is a test servlet to see how well we 
 * can perform two-way communications....
 * 
 * @author Carol Hamer
 */
public class TwoWayServlet extends HttpServlet {

  //------------------------------------------------------
  //   data 

  /**
   * The int to send
   */
  private int myData = 0;


  //------------------------------------------------------
  //   implementation of servlet

  /**
   * send the doPut requests to doPost.
   */
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    //myData = 3;
    doPost(request, response);
  }

  /**
   * send the doGet requests to doPost.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    myData = 4;
    doPost(request, response);
  }

  /**
   * send the data..
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    OutputStream os = null;
    InputStream is = null;
    int sendInt = 0;
    byte[] oneByte = new byte[1];
    try {
      is = request.getInputStream();
      int count = -1;
      while(count < 1) {
	count = is.read(oneByte);
      }
      os = response.getOutputStream();
      //sendInt += oneByte[0] + 2;
      os.write(oneByte);
      // send it on its merry way...
      response.flushBuffer();
    } catch(EOFException eofe) {
      System.out.println("TwoWayServlet.doPost-->caught " + eofe.getClass()
			 + ": " + eofe.getMessage());
      response.sendError(408, eofe.getMessage());
    } catch(IOException ioe) {
      System.out.println("TwoWayServlet.doPost-->caught " + ioe.getClass()
			 + ": " + ioe.getMessage());
      ioe.printStackTrace(System.out);
      response.sendError(500, ioe.getClass() + ": " + ioe.getMessage());
    } catch(Exception e) {
      System.out.println("TwoWayServlet.doPost-->caught " + e.getClass()
			 + ": " + e.getMessage());
      response.sendError(500, e.getClass() + ": " + e.getMessage());
    } finally {
      if(is != null) {
	is.close();
      }
      if(os != null) {
	os.close();
      }
    }
  }
  /*
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    //if(myData == 0) {
    //  myData = -7;
    //}
    OutputStream os = null;
    InputStream is = null;
    int sendInt = 0;
    try {
      is = request.getInputStream();
      os = response.getOutputStream();
      //int count = 0;
      sendInt += is.read() + 2;
      while(true) {
	//int available = is.available();
	//os.write(myData);
	os.write(sendInt);
	// send it on its merry way...
	response.flushBuffer();
	synchronized(this) {
	  wait(500);
	}
	sendInt++;
      }
    } catch(EOFException eofe) {
      System.out.println("TwoWayServlet.doPost-->caught " + eofe.getClass()
			 + ": " + eofe.getMessage());
      response.sendError(408, eofe.getMessage());
    } catch(IOException ioe) {
      System.out.println("TwoWayServlet.doPost-->caught " + ioe.getClass()
			 + ": " + ioe.getMessage());
      ioe.printStackTrace(System.out);
      response.sendError(500, ioe.getClass() + ": " + ioe.getMessage());
    } catch(Exception e) {
      System.out.println("TwoWayServlet.doPost-->caught " + e.getClass()
			 + ": " + e.getMessage());
      response.sendError(500, e.getClass() + ": " + e.getMessage());
    } finally {
      if(is != null) {
	is.close();
      }
      if(os != null) {
	os.close();
      }
    }
  }
*/

}
