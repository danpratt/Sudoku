package pratt.dan.sudoku;

/**
 * Gets State values of a SudokuBase object.  Can check rows, columns, or grid regions.
 * COMPLETE: 	All values in checked area are given, valid, and unique.
 * ERROR:	 	There are duplicate values in checked area.
 * INCOMPLETE:	Values are missing from checked area, but there are no errors (duplicates). 
 * 
 * Level: Challenge
 * @author Dan Pratt
 * @version: Assignment 3 - Sudoku Core
 *
 */
public class SudokuBoard extends SudokuBase implements java.io.Serializable {
	
	/**
	 * Creates a new SudokuBoard object using given rows and columns for size of each region.
	 * @param layoutRows number of rows in each region.
	 * @param layoutColumns number of columns in each region.
	 */
	public SudokuBoard(int layoutRows, int layoutColumns) {
		super(layoutRows, layoutColumns);  // calls superclass version of constructor.
		// Other constructor implementation may come in later versions.
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public State getRowState (int rowToCheck) {
		// Check that values are within range
		checkValidIndex(rowToCheck);
		// returns State value of given row.
		return checkState(getLineContents(rowToCheck, 0, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public State getColumnState(int colToCheck) {
		// Check that values are within range
		checkValidIndex(colToCheck);
		// returns State value of given column.
		return checkState(getLineContents(0, colToCheck, false));
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public State getRegionState(int regionToCheck) {
		// Check that values are within range
		checkValidIndex(regionToCheck);
		// Create integers for starting and ending row, column values.
		int row, col, endRow, endCol;
		// set starting points to use in end point calculations.  These will still be reset (to the same algorithm) in for loops.
		row = regionToCheck / this.rows * this.rows;
		col = regionToCheck % this.rows * this.columns; 
		// set ending points to use in for loops.
		endRow 	 = row + this.rows;
		endCol 	 = col + this.columns;
		// create array to hold values to be checked.
		int[] contents = new int[this.size];
		// Create array iterator for setting values
		int i = 0;
		for (row = regionToCheck / this.rows * this.rows; row < endRow; row++) {
			for (col = regionToCheck % this.rows * this.columns;col < endCol; col++) {
				contents[i] = this.getValue(row, col);  // set current array index to contents.
				i++;  // go to next spot in contents array.
			}
		}
		// return State of given region.
		return checkState(contents);
	}
	
	/*
	 * Helper method for checking states.
	 * @param contentsToCheck an array of values to check.
	 * @return The State value of checked area.  (COMPLETE, ERROR, or INCOMPLETE).
	 */
	private State checkState(int[] contentsToCheck) {
		// Create a boolean to see if there are null values.
		// If there are, and no errors are detected, bool will tell to report INCOMPLETE.
		Boolean nullValues = false;
		// Compare every element to each other.
		for (int i = 0; i < contentsToCheck.length - 1; i++) {
			for (int j = i + 1; j < contentsToCheck.length; j++) {
				// if either element is 0, set boolean to show that there are holes.
				if (contentsToCheck[i] > this.size || contentsToCheck[i] == 0
						|| contentsToCheck[j] > this.size || contentsToCheck[j] == 0) {
					nullValues = true;
				} else if (contentsToCheck[i] == contentsToCheck[j]) {
					return State.ERROR;  // returns an error as soon as a duplicate is found.
					}
				}
		}
		if (nullValues) return State.INCOMPLETE;  // if nullValues boolean was triggered because there was a hole, report INCOMPLETE.
		return State.COMPLETE;  // If it gets this far, there are no errors and it is not incomplete, so report complete.
	}
	
	/*
	 * Makes sure that a given index is within range (not more than this.size and not less than 0).
	 * This will throw an IllegalArgumentException if given index is outside of range.
	 * May be updated to handle the error in future version.
	 * @param indexToCheck the index value to make sure it is within range.
	 */
	private void checkValidIndex (int indexToCheck) {
		String msg = "Value out of range";
		if ( indexToCheck > this.size || indexToCheck < 0) throw new IllegalArgumentException(msg);
	}
	
	/*
	 * Helper method for checking rows or column states.  Creates an array that will have state checked on it and returns the array.
	 * Preconditions: Row and Column must be within range (From 0 up to but not including this.size).  These conditions are checked inside calling method.
	 * @param row Starting row position.  If you are checking a column, this must be set to 0.
	 * @param col Starting column position.  If you are checking a row, this must be set to 0.
	 * @param isRow If you are checking a row, boolean must be set to true (will cause col value to be iterated).  Set to false if checking a column (will cause row value to iterated).
	 */
	private int[] getLineContents(int row, int col, boolean isRow) {
		// Create the array to fill with values.
		int[] contents = new int[this.size];
		// Check all positions in a given row or column.
		for (int i = 0; i < this.size; i++) {
			// Load value of current row, col coordinate into array.
			contents[i] = this.getValue(row, col);
			if (isRow) {
				// If checking a row, iterate column by one (since we need to check every element of the given row).
				col++;
			} else {
				// If checking a column, iterate he row by one (since we need to check every element of the given column).
				row++;
			}
		}
		// give the completed array of values back to calling method.
		return contents;
	}

}
