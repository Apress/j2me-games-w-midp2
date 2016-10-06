package net.frog_parrot.maze;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the screen that allows the user to modify the 
 * width of the maze walls..
 *
 * @author Carol Hamer
 */
public class SelectScreen extends Form 
  implements ItemStateListener, CommandListener  {

  //----------------------------------------------------------------
  //  fields

  /**
   * The "Done" button to exit this screen and return to the maze.
   */
  private Command myExitCommand = new Command("Done", Command.EXIT, 1);

  /**
   * The gague that modifies the width of the maze walls.
   */
  private Gauge myWidthGauge;

  /**
   * The gague that displays the number of columns of the maze.
   */
  private Gauge myColumnsGauge;

  /**
   * A handle to the main game canvas.
   */
  private MazeCanvas myCanvas;

  //----------------------------------------------------------------
  //  initialization

  /**
   * Create the gagues and place them on the screen.
   */
  public SelectScreen(MazeCanvas canvas) {
    super("Size Preferences");
    addCommand(myExitCommand);
    setCommandListener(this);
    myCanvas = canvas;
    setItemStateListener(this);
    myWidthGauge = new Gauge("Column Width", true, 
			     myCanvas.getMaxColWidth(), 
			     myCanvas.getColWidth());
    myColumnsGauge = new Gauge("Number of Columns", false,  
			       myCanvas.getMaxNumCols(), 
			       myCanvas.getNumCols());
    // Warning: the setLayout method does not exist in 
    // MIDP 1.4.  If there is any chance that a target 
    // device will be using MIDP 1.4, comment out the 
    // following two lines:
    myWidthGauge.setLayout(Item.LAYOUT_CENTER);
    myColumnsGauge.setLayout(Item.LAYOUT_CENTER);
    append(myWidthGauge);
    append(myColumnsGauge);
  }

  //----------------------------------------------------------------
  //  implementation of ItemStateListener

  /**
   * Respond to the user changing the width.
   */
  public void itemStateChanged(Item item) {
    if(item == myWidthGauge) {
      int val = myWidthGauge.getValue();
      if(val < myCanvas.getMinColWidth()) {
	myWidthGauge.setValue(myCanvas.getMinColWidth());
      } else {
	int numCols = myCanvas.setColWidth(val);
	myColumnsGauge.setValue(numCols);
      }
    }
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to a command issued on this screen.
   * (either reset or exit).
   */
  public void commandAction(Command c, Displayable s) {
    if(c == myExitCommand) {
      PrefsStorage.setSquareSize(myWidthGauge.getValue());
      myCanvas.newMaze();
    }
  }
  
}
