package net.frog_parrot.maze;

import java.util.Random;
import java.util.Vector;

/**
 * This class contains the data necessary to draw the maze.
 *
 * @author Carol Hamer
 */
public class Grid {

  /**
   * Random number generator to create a random maze.
   */
  private Random myRandom = new Random();

  /**
   * data for which squares are filled and which are blank.
   * 0 = black
   * 1 = white
   * values higher than 1 are used during the maze creation 
   * algorithm.
   * 2 = the square could possibly be appended to the maze this round.
   * 3 = the square's color is not yet decided, and the square is 
   * not close enough to be appended to the maze this round.
   */
  int[][] mySquares;

  //--------------------------------------------------------
  //  maze generation methods

  /**
   * Create a new maze.
   */
  public Grid(int width, int height) {
    mySquares = new int[width][height];
    // initialize all of the squares to white except a lattice 
    // framework of black squares.
    for(int i = 1; i < width - 1; i++) {
      for(int j = 1; j < height - 1; j++) {
	if((i % 2 == 1) || (j % 2 == 1)) {
	  mySquares[i][j] = 1;
	}
      }
    }
    // the entrance to the maze is at (0,1).
    mySquares[0][1] = 1;
    createMaze();
  }

  /**
   * This method randomly generates the maze.
   */
  private void createMaze() {
    // create an initial framework of black squares.
    for(int i = 1; i < mySquares.length - 1; i++) {
      for(int j = 1; j < mySquares[i].length - 1; j++) {
	if((i + j) % 2 == 1) {
	  mySquares[i][j] = 0;
	}
      }
    }
    // initialize the squares that can be either black or white 
    // depending on the maze.
    // first we set the value to 3 which means undecided.
    for(int i = 1; i < mySquares.length - 1; i+=2) {
      for(int j = 1; j < mySquares[i].length - 1; j+=2) {
	mySquares[i][j] = 3;
      }
    }
    // Then those squares that can be selected to be open 
    // (white) paths are given the value of 2.  
    // We randomly select the square where the tree of maze 
    // paths will begin.  The maze is generated starting from 
    // this initial square and branches out from here in all 
    // directions to fill the maze grid.  
    Vector possibleSquares = new Vector(mySquares.length 
					* mySquares[0].length);
    int[] startSquare = new int[2];
    startSquare[0] = getRandomInt(mySquares.length / 2)*2 + 1;
    startSquare[1] = getRandomInt(mySquares[0].length / 2)*2 + 1;
    mySquares[startSquare[0]][startSquare[1]] = 2;
    possibleSquares.addElement(startSquare);
    // Here we loop to select squares one by one to append to 
    // the maze pathway tree.
    while(possibleSquares.size() > 0) {
      // the next square to be joined on is selected randomly.
      int chosenIndex = getRandomInt(possibleSquares.size());
      int[] chosenSquare = (int[])possibleSquares.elementAt(chosenIndex);
      // we set the chosen square to white and then 
      // remove it from the list of possibleSquares (i.e. squares 
      // that can possibly be added to the maze), and we link 
      // the new square to the maze.
      mySquares[chosenSquare[0]][chosenSquare[1]] = 1;
      possibleSquares.removeElementAt(chosenIndex);
      link(chosenSquare, possibleSquares);
    }
    // now that the maze has been completely generated, we 
    // throw away the objects that were created during the 
    // maze creation algorithm and reclaim the memory.
    possibleSquares = null;
    System.gc();
  }

  /**
   * internal to createMaze.  Checks the four squares surrounding 
   * the chosen square.  Of those that are already connected to 
   * the maze, one is randomly selected to be joined to the 
   * current square (to attach the current square to the 
   * growing maze).  Those squares that were not previously in 
   * a position to be joined to the maze are added to the list 
   * of "possible" squares (that could be chosen to be attached 
   * to the maze in the next round).
   */
  private void link(int[] chosenSquare, Vector possibleSquares) {
    int linkCount = 0;
    int i = chosenSquare[0];
    int j = chosenSquare[1];
    int[] links = new int[8];
    if(i >= 3) {
      if(mySquares[i - 2][j] == 1) {
	links[2*linkCount] = i - 1;
	links[2*linkCount + 1] = j;
	linkCount++;
      } else if(mySquares[i - 2][j] == 3) {
	mySquares[i - 2][j] = 2;
	int[] newSquare = new int[2];
	newSquare[0] = i - 2;
	newSquare[1] = j;
	possibleSquares.addElement(newSquare);
      }
    }
    if(j + 3 <= mySquares[i].length) {
      if(mySquares[i][j + 2] == 3) {
	mySquares[i][j + 2] = 2;
	int[] newSquare = new int[2];
	newSquare[0] = i;
	newSquare[1] = j + 2;
	possibleSquares.addElement(newSquare);
      } else if(mySquares[i][j + 2] == 1) {
	links[2*linkCount] = i;
	links[2*linkCount + 1] = j + 1;
	linkCount++;
      }
    } 
    if(j >= 3) {
      if(mySquares[i][j - 2] == 3) {
	mySquares[i][j - 2] = 2;
	int[] newSquare = new int[2];
	newSquare[0] = i;
	newSquare[1] = j - 2;
	possibleSquares.addElement(newSquare);
      } else if(mySquares[i][j - 2] == 1) {
	links[2*linkCount] = i;
	links[2*linkCount + 1] = j - 1;
	linkCount++;
      }
    } 
    if(i + 3 <= mySquares.length) {
      if(mySquares[i + 2][j] == 3) {
	mySquares[i + 2][j] = 2;
	int[] newSquare = new int[2];
	newSquare[0] = i + 2;
	newSquare[1] = j;
	possibleSquares.addElement(newSquare);
      } else if(mySquares[i + 2][j] == 1) {
	links[2*linkCount] = i + 1;
	links[2*linkCount + 1] = j;
	linkCount++;
      }
    } 
    if(linkCount > 0) {
      int linkChoice = getRandomInt(linkCount);
      int linkX = links[2*linkChoice];
      int linkY = links[2*linkChoice + 1];
      mySquares[linkX][linkY] = 1;
      int[] removeSquare = new int[2];
      removeSquare[0] = linkX;
      removeSquare[1] = linkY;
      possibleSquares.removeElement(removeSquare);
    }
  }

  /**
   * a randomization utility. 
   * @param upper the upper bound for the random int.
   * @return a random non-negative int less than the bound upper.
   */
  public int getRandomInt(int upper) {
    int retVal = myRandom.nextInt() % upper;
    if(retVal < 0) {
      retVal += upper;
    }
    return(retVal);
  }

}
