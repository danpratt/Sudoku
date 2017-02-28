package pratt.dan.sudoku;
// Class imports
import java.io.*;  // for file operations

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Base class of Sudoku.java game.  Creates the board and allows interaction with game board.  Can set and retrieve values, as well as mask unchangeable starting values.
 * Level: Challenge
 * @author Dan Jinguji, JavaDoc comments added by Dan Pratt
 * @version Assignment 3: Sudoku Core
 * 
 *
 */
public abstract class SudokuBase extends java.util.Observable implements java.io.Serializable {
   
   // Class Public fields
	/** Holds number of rows in board instance, cannot be changed once object is instantiated */
   public final int rows;
   /** Holds number of columns in board instance, cannot be changed once object is instantiated  */
   public final int columns;
   /** Holds size (number of values in a row, column, or region - i.e. row * column) in board instance, cannot be changed once object is instantiated */
   public final int size;
   
   // Private field
   private final int[] grid;  // The board.  Holds all values on board inside this array.
   
   // Private constants for setting givens
   private static final int GIVEN_MASK = 0x00000100;  // bit mask
   private static final int GIVEN_UNMASK = ~ GIVEN_MASK; // unmask bit operation
   		
   /** Possible states a row, column or region can be in.*/
   public enum State {
	   /** All values are filled in and unique from 1 - size. */
	   COMPLETE, 
	   /** There are holes in checked area, but no duplicates. */
	   INCOMPLETE, 
	   /** There are duplicates in checked area. */
	   ERROR};
	   
	   /** Type of area for completion checking */
	public enum Type {
		/** For checking rows */
		ROW, 
		/** For checking columns */
		COLUMN, 
		/** For checking regions */
		REGION
		};
   
	   /**
	    * Creates Sudoku game board object according to given params.  Creates an array to hold board values. Note: no initial values are set.
	    * @param layoutRows number of rows in each region
	    * @param layoutColumns number of columns in each region
	    */
   public SudokuBase(int layoutRows, int layoutColumns) {
      rows = layoutRows;
      columns = layoutColumns;
      size = columns * rows;
      grid = new int[size*size];
   }
   
   /*
  	* Gets the corresponding index of a given row, column value.
    * @param row Row of value trying to find the index of.
    * @param col Column of value trying to find the index of.
    * @return The index in grid[] that the given row and column corresponds to.
    */
   private int getIndex(int row, int col) {
	  // make sure that the row and column are within range
      if(row < 0 || row >= size || col < 0 || col >= size) {
         String msg = "Error in location";
         throw new IllegalArgumentException(msg);
      }
      // return index of given row, column.
      return row * size + col;
   }
   
   /**
    * Gets the value held in given row, column of an instantiated SudokuBase object.
    * Precondition: Must be a valid (on game board) (row, column) coordinate.
    * @param row The row where the desired value is in. (Row cannot be less than 0 or greater or equal to this.size).
    * @param col The column where the desired value is in. (Column cannot be less than 0 or greater or equal to this.size).
    * @return The value held inside a given (row, column)
    */
   public int getValue(int row, int col) {
      return grid[getIndex(row, col)] & GIVEN_UNMASK;
   }
   
   /**
    * Sets the given value into a given (row, column) coordinate in SudokuBase.
    * Preconditions: Must be a valid (on game board) (row, column) coordinate.  Cannot override the value of a given.
    * @param row The row where the given value should be placed. (Row cannot be less than 0 or greater or equal to this.size).
    * @param col The column where the given value should be placed. (Column cannot be less than 0 or greater or equal to this.size).
    * @param value The desired value to put inside given (row, column) coordinate.  (Value must be from 1 up to and including this.size).
    */
   public void setValue(int row, int col, int value) {
	   // Make sure given value is within range.
      if(value < 0 || value > size) {
         String msg = "Value out of range: " + value;
         throw new IllegalArgumentException(msg);
      }
      // Make sure not trying to overwrite a given value.
      if(isGiven(row, col)) {
         String msg = "Cannot set given location: " + row + ", " + col;
         throw new IllegalStateException(msg);
      }
      // set value (exception thrown if row or column is outside of range)
      grid[getIndex(row, col)] = value;
      setChanged();
      notifyObservers();
   }
   
   /**
    * Checks to see if given (row, column) coordinate is a given or not.
    * Precondition: Must be a valid (on game board) (row, column) coordinate.
    * @param row The row coordinate to check. (Row cannot be less than 0 or greater or equal to this.size).
    * @param col The column coordinate to check. (Column cannot be less than 0 or greater or equal to this.size).
    * @return true if value is a given, false if it is not.
    */
   public boolean isGiven(int row, int col) {
	  // will return true if the value at index is equal to the GIVEN_MASK when a bitwise and operation is performed.
      return (grid[getIndex(row, col)] & GIVEN_MASK) == GIVEN_MASK;
   }
   
   /**
    * Sets all non-zero values inside SudokuBase object to givens.  Should be called after initial values are set into board.
    * Note: Should not call after game has begin as this will lock these values, preventing them to be set to anything else.
    */
   public void fixGivens() {
      for(int i = 0; i < grid.length; i++)
    	 // If the current value does not equal zero, perform bitwise or compare to GIVEN_MASK to lock these values.
         if(grid[i] != 0) 
            grid[i] |= GIVEN_MASK;
      		setChanged();
      		notifyObservers();
   }
   
   /**
    * Returns State value of a given row.
    * Precondition: Row must be within range (0 up to but not including this.size).
    * @param n Row to check State of.
    * @return State of row (COMPLETE, ERROR, or INCOMPLETE).
    */
   public abstract State getRowState(int n);
   /**
    * Returns State value of a given column.
    * Precondition: Column must be within Column (0 up to but not including this.size).
    * @param n Column to check State of.
    * @return State of column (COMPLETE, ERROR, or INCOMPLETE).
    */
   public abstract State getColumnState(int n);
   /**
    * Returns State value of a given region.
    * Precondition: Region must be within range (0 up to but not including this.size).
    * @param n Region to check State of.
    * @return State of region (COMPLETE, ERROR, or INCOMPLETE).
    */
   public abstract State getRegionState(int n);
   
   /**
    * toString method that represents SudokuBase by showing values held within the grid.
    */
   @Override
   public String toString() {
      String board = "";
      for(int i = 0; i < size; i ++) {
         for(int j = 0; j < size; j ++)
            board += charFor(i, j) + " ";
         board += "\n";
      }
      return board;
   }

   /*
    * Private helper method for toString.  Displays ? for invalid values, spaces for 0's, and the actual held value for all valid values.
    */
   private String charFor(int i, int j) {
      int v = getValue(i, j);
      if(v < 0) {
         return "?";
      } else if(v == 0) {
         return " ";
      } else if(v < 36) {
         return Character.toString(Character.forDigit(v, 36)).toUpperCase();
      } else {
         return "?";
      }
   }

   /*
    * Attempts to load a game model using given filename
    */
   protected SudokuBoard readFromStream(File file) {
	   // open the stream and write data to file
	   try {
		   FileInputStream fileIn = new FileInputStream(file);
		   ObjectInputStream gameModel = new ObjectInputStream(fileIn);
		   SudokuBoard loadedGame = (SudokuBoard) gameModel.readObject();
		   gameModel.close();
		   fileIn.close();
		   return loadedGame;
		   
	   } catch (ClassNotFoundException e) {
		 //custom title, error icon
		   JOptionPane.showMessageDialog(new JFrame(),
		       "File could not be loaded.",
		       "Load Error",
		       JOptionPane.ERROR_MESSAGE);
	   } catch (FileNotFoundException e) {
		   JOptionPane.showMessageDialog(new JFrame(),
			       "File could not be loaded.",
			       "Load Error",
			       JOptionPane.ERROR_MESSAGE);
	} catch (IOException e) {
		JOptionPane.showMessageDialog(new JFrame(),
			       "File could not be loaded.",
			       "Load Error",
			       JOptionPane.ERROR_MESSAGE);
	}
	// Something went wrong, so returning null.
	return null;

   }
   
   /*
    * Attempts to save a game model using given filename
    */
   protected void writeToStream(SudokuBoard gameModel, File filename) {
	   // open the stream and write data to file
	   try {
		   FileOutputStream fileOut = new FileOutputStream(filename);
		   ObjectOutputStream out = new ObjectOutputStream(fileOut);
		   out.writeObject(gameModel);
		   out.close();
		   fileOut.close();
		   
	   } catch (IOException e) {
		 //custom title, error icon
		   JOptionPane.showMessageDialog(new JFrame(),
		       "File could not be saved.",
		       "Save Error",
		       JOptionPane.ERROR_MESSAGE);
	   }
   }
   /*
    * Method to be implemented in later version; will read the values from grid.
    */
   protected int getRawValue(int row, int col) {
      return grid[getIndex(row, col)];
   }
   /*
    * Method to be implemented in later version; will write values to grid.
    */
   protected void setRawValue(int row, int col, int value) {
      grid[getIndex(row, col)] = value;
   }
}
