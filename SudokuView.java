package pratt.dan.sudoku;
// Observer imports
import java.util.Observable;  // required to setup observable implementation
import java.util.Observer;  // for implementation

// awt imports
import java.awt.*; // import for fonts, color, etc.
import java.awt.event.*; // import for handling events (i.e. mouse and keyboard)

// swing imports
import javax.swing.border.Border;  // import to create compound border
import javax.swing.BorderFactory;  // import to create borders
import javax.swing.JPanel; // import for JPanel.


/**
 * This class controls the graphics for the Sudoku game.
 * Level: Challenge
 * @author Dan Pratt
 * @version Assignment 4: Sudoku Graphics
 *
 */
public class SudokuView extends JPanel implements NumericSupport, SelectedCell,
		Observer {
	/* Constants */
	
	// Class constant for preferred sizes
	private static final int CELL_SIZE = 50; // cells are squares, so only one int is needed to represent both sides.
	private static final Dimension CELL_DIMENSION = new Dimension(CELL_SIZE, CELL_SIZE);  // Dimension object used for setting the preferred size of each cell.
	
	// Class constants for board colors
	private static final Color lightBackground = new Color(0, 151, 172);  // darker color for board fill
	private static final Color darkBackground = new Color(0, 121, 150);  // lighter color for board fill
	private static final Color selectedBackground = new Color(34, 85, 51);  // fill color when cell is selected
	private static final Color givenColor = new Color(95, 216, 250);  // Color values of givens take on  
	private static final Color artColor = new Color(177, 221, 161); // Color of regular values
	
	// Font used in numeric version
	private static final Font numeric 	 = new Font("Serif", Font.PLAIN, 18);  // regular inputs
	private static final Font numericBold = new Font("Serif", Font.BOLD, 18);  // bold given values
	
	/* Fields */
	
	// Selector location
	private int selectedRow, selectedCol; // placeholder for selected row and column.
	
	// Class board field
	private SudokuBoard sudokuBoard;  // Model that this view class represents
	
	// Flag variables
	private boolean isNumeric;  // is true if isNumeric box is checked, will cause numeric values to be displayed.
	
	/**
	 * Creates a new SudokuView object that provides a graphical view of a SudokuBoard model object.
	 * @param board the SudokuBoard (model) object that will be represented graphically in SudokuView.
	 */
	public SudokuView(SudokuBase board) {
		
		// Set data to the board
		sudokuBoard = (SudokuBoard)board;  // attach model
		setLayout(new GridLayout(board.size, board.size));  // set to a grid layout that matches the board dimensions.
		
		// create the board
		for (int row = 0; row < sudokuBoard.size; row++) {
			for (int col = 0; col < sudokuBoard.size; col++) {
				add(new Cell(row, col));  // add Cell object (object that graphically represents individual values contained in given row, column).
			}
		}
		
	}


	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable sudokuBoard, Object arg) {
		repaint();  // repaint when model changes
	}

	/* (non-Javadoc)
	 * @see SelectedCell#setSelected(int, int)
	 */
	@Override
	public void setSelected(int row, int col) {
		// set selected Row / Column
		selectedRow = row;  // set row
		selectedCol = col;  // set column
	}

	/* (non-Javadoc)
	 * @see SelectedCell#getSelectedRow()
	 */
	@Override
	public int getSelectedRow() {
		return selectedRow;  // return currently selected row
	}

	/* (non-Javadoc)
	 * @see SelectedCell#getSelectedColumn()
	 */
	@Override
	public int getSelectedColumn() {
		return selectedCol;  // return currently selected column
	}

	/* (non-Javadoc)
	 * @see NumericSupport#setNumeric(boolean)
	 */
	@Override
	public void setNumeric(boolean flag) {
		// If Numeric option is clicked or un-clicked, update flag variable.
		isNumeric = flag;
	}

	/* (non-Javadoc)
	 * @see NumericSupport#showsNumeric()
	 */
	@Override
	public boolean showsNumeric() {
		// Lets other classes find out if the view's isNumeric flag is true or false.
		return isNumeric;  // returns current state of isNumeric
	}
	
	/**
	 * Class responsible for drawing individual cells that are contained inside of SudokuView object.
	 * Level: Challenge
	 * @author Dan Pratt
	 * @version Assignment 4: Sudoku Graphics
	 *
	 */
	class Cell extends JPanel {
		private int row, col, region;  // values for row, column position.
		// private boolean isDark;
		private Border cellBorder; // for the border, a variable since it will have a slightly different behavior when selected.
		
		/**
		 * Constructs a Cell object.  This object is responsible for displaying the values contained in the given row, column of a SudokuBoard model object.
		 * @param row Given row inside the SudokuBoard model object whoes value is to be represented.
		 * @param col Given column inside the SudokuBoard model object whose value is to be represented.
		 */
		public Cell( int row, int col ) {
			this.row = row;
			this.col = col;
			this.region = (row / sudokuBoard.rows * sudokuBoard.rows) + (col / sudokuBoard.columns);
			setPreferredSize(CELL_DIMENSION);
		}
		
		/**
		 * Draws the cell based on location on board (for background color), if the cell is currently selected, and the contents of the SudokuBoard model at the Cell's row, column.
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);  // clear whatever was there before
			// create the cell borders.
			cellBorder = BorderFactory.createCompoundBorder(
					BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());
			setBorder(cellBorder);  // set the border around the cell
			// if the row needs to be selected, set to the selected background color
			// add the mouse listener to see if this one has been clicked on
			addMouseListener( new MouseAdapter() {
				 public void mousePressed(MouseEvent e) {
					 setSelected(row, col);  // if there is a click, set the selected cell to the one that was clicked.	
				 }
			 });

			// If the row is selected, color it as such
			if (row == selectedRow && col == selectedCol) {
				setBackground(selectedBackground);  // set a row that is selected with the appropriate background color
				
				// special case for 4 x 3 board.
				} else if ( sudokuBoard.rows == 4 && sudokuBoard.columns == 3 ){
					if ( region % 2 == 0 ) {
						if (region != 0 && region / 4 == 1 ) {
							setBackground(darkBackground);
						} else setBackground(lightBackground);
					} else if ( region % 2 == 1 ) {
						if (region != 0 && region / 4 == 1 ) {
							setBackground(lightBackground);
						} else setBackground(darkBackground);
					}
				// if the board has an odd number of regions per row
				} else if (sudokuBoard.rows % 2 == 1) {
					// set the even numbered regions with the light colored background.
					if ( region  % 2 == 0 ) {
						setBackground(lightBackground);  // set the appropriate background color
						// set the odd numbered regions with the light colored background.
					} else if (region % 2 == 1) {
						setBackground(darkBackground);  // set the appropriate background color
					}
					// if there are an even number of regions per row
				} else {
					// set the pattern so it swaps every region row
					if ( Math.ceil((double)region / (double)sudokuBoard.rows) % 2 == 0) {
						setBackground(lightBackground);  // set the appropriate background color
					} else {
						setBackground(darkBackground); // set the appropriate background color
					}
				}
			
			// now we have to display the values inside each cell.  If the isNumeric value is checked (set to true) show numbers inside the cells.
			if (isNumeric) {
				
				// if the value is a fixed (given) value
				if (sudokuBoard.isGiven(row, col)) {
					setFont(numericBold);  // set to the bold version of the font
					setForeground(givenColor);  // set the color of the font to the fixed (given) color.
					g.drawString(String.valueOf(sudokuBoard.getValue(row, col)), this.getWidth() / 2 - this.getWidth() / 10, this.getHeight() / 2 + this.getHeight() / 8);  // draw the number inside the cell.
				} else {
					// if the value is not fixed (given) value and not a 0, draw it inside the cell
					if ( sudokuBoard.getValue(row, col) != 0 ) { 
						setFont(numeric);  // set to the standard font
						setForeground(artColor); // set to the standard color
						g.drawString(String.valueOf(sudokuBoard.getValue(row, col)), this.getWidth() / 2 - this.getWidth() / 10, this.getHeight() / 2 + this.getHeight() / 8);  // draw the number inside the cell
					}
					
				}
			
			// if the numeric box is not checked, display the corresponding Mayan character for that number.
			} else {
				Graphics2D g2D = (Graphics2D)g.create();  // create Graphics2D object to draw with
				g2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));  // set stroke
				// switch up to 12 numbers (the max we have characters created for)
				switch ( sudokuBoard.getValue(row, col) ) {
				case 0:
					break;  // don't draw anything that has a value of 0.
				case 1:
					// Draw the character that will represent one
					if ( sudokuBoard.isGiven(row, col) ) {
						drawOne(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawOne(g2D, false); // if the character is not a given, draw it with the normal color
					}
					break;
				case 2:
					// Draw the character that will represent two
					if ( sudokuBoard.isGiven(row, col) ) {
						drawTwo(g2D, true);
					} else {
						drawTwo(g2D, false);
					}
					break;
				case 3:
					// Draw the character that will represent three
					if ( sudokuBoard.isGiven(row, col) ) {
						drawThree(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawThree(g2D, false);  // if the character is not a given, draw it with the normal color
					}
					break;
				case 4:
					// Draw the character that will represent four
					if ( sudokuBoard.isGiven(row, col) ) {
						drawFour(g2D, true);   // if the character is a given, draw it with the given color
					} else {
						drawFour(g2D, false);  // if the character is not a given, draw it with the normal color
					}
					break;
				case 5:
					// Draw the character that will represent five
					if ( sudokuBoard.isGiven(row, col) ) {
						drawFive(g2D, true);   // if the character is a given, draw it with the given color
					} else {
						drawFive(g2D, false);   // if the character is not a given, draw it with the normal color
					}
					break;
				case 6:
					// Draw the character that will represent two
					if ( sudokuBoard.isGiven(row, col) ) {
						drawSix(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawSix(g2D, false);   // if the character is not a given, draw it with the normal color
					}
					break;
				case 7:
					// Draw the character that will represent seven
					if ( sudokuBoard.isGiven(row, col) ) {
						drawSeven(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawSeven(g2D, false);  // if the character is not a given, draw it with the normal color
					}
					break;
				case 8:
					// Draw the character that will represent eight
					if ( sudokuBoard.isGiven(row, col) ) {
						drawEight(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawEight(g2D, false);  // if the character is not a given, draw it with the normal color
					}
					break;
				case 9:
					// Draw the character that will represent nine
					if ( sudokuBoard.isGiven(row, col) ) {
						drawNine(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawNine(g2D, false);  // if the character is not a given, draw it with the normal color
					}
					break;
				case 10:
					// Draw the character that will represent ten
					if ( sudokuBoard.isGiven(row, col) ) {
						drawTen(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawTen(g2D, false);  // if the character is not a given, draw it with the normal color
					}
					break;
				case 11:
					// Draw the character that will represent eleven
					if ( sudokuBoard.isGiven(row, col) ) {
						drawEleven(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawEleven(g2D, false);  // if the character is not a given, draw it with the normal color
					}
					break;
				case 12:
					// Draw the character that will represent twelve
					if ( sudokuBoard.isGiven(row, col) ) {
						drawTwelve(g2D, true);  // if the character is a given, draw it with the given color
					} else {
						drawTwelve(g2D, false);  // if the character is not a given, draw it with the normal color
					}
					break;
				// no more cases, if a number is above 12, it is outside the scope of existing characters that are available inside this class.
				}
					
			}
		}

		/* Everything below here draws the individual Mayan symbols to represent the numbers */
		
		/*
		 * Draws the Mayan symbol that represents one
		 */
		private void drawOne(Graphics g2D, boolean colorGiven) {
			// set color based on if it is given or not
			if (colorGiven) {
				g2D.setColor(givenColor);  // set to the given color if it is a given
			} else {
				g2D.setColor(artColor);   // if it is not a given, set to normal color
			}
			g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 12, this.getWidth() / 6, this.getHeight() / 6);  // draw the dot
		}
		
		/*
		 * Draws the symbol that represents two
		 */
		private void drawTwo(Graphics g2D, boolean colorGiven) {
			// set color based on if it is given or not
			if (colorGiven) {
				g2D.setColor(givenColor);  // set to the given color if it is a given
			} else {
				g2D.setColor(artColor);  // if it is not a given, set to normal color
			}
			
			g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
			g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot

		}

	
	/*
	 * Draws the Mayan symbol that represents three
	 */
	private void drawThree(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}
		
		g2D.fillOval(this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
		g2D.fillOval(this.getWidth() / 2 - getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
		g2D.fillOval(this.getWidth() / 2 + this.getWidth() / 6, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the third dot
	}

	
	/*
	 * Draws the Mayan symbol that represents four
	 */
	private void drawFour(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}
		
		g2D.fillOval(this.getWidth() / 8, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
		g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
		g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the third dot
		g2D.fillOval(this.getWidth() / 2 + this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the fourth dot
		}
	
	/*
	 * Draws the Mayan symbol that represents five
	 */
	private void drawFive(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}
		
		g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
		}
	
	/*
	 * Draws the Mayan symbol that represents six
	 */
	private void drawSix(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}
		
		g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() /2); // draw the line
		g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the dot
		}
	
	/*
	 * Draws the Mayan symbol that represents seven
	 */
	private void drawSeven(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}

		g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() /2);  // draw the line
		g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
		g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
		}
	
	/*
	 * Draws the Mayan symbol that represents eight
	 */
	private void drawEight(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}

		g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
		g2D.fillOval(this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
		g2D.fillOval(this.getWidth() / 2 - getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
		g2D.fillOval(this.getWidth() / 2 + this.getWidth() / 6, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the third dot
		}
	
	/*
	 * Draws the Mayan symbol that represents nine
	 */
	private void drawNine(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}

		g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
		g2D.fillOval(this.getWidth() / 8, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
		g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
		g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the third dot
		g2D.fillOval(this.getWidth() / 2 + this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the fourth dot
		}
	
	/*
	 * Draws the Mayan symbol that represents ten
	 */
	private void drawTen(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}

		g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
		g2D.drawLine(getWidth() / 6, getHeight() / 2 + getHeight() / 6, getWidth() - getWidth() / 6, getHeight() / 2 + getHeight() / 6);  // draw the bottom line
		}
	
	/*
	 * Draws the Mayan symbol that represents eleven
	 */
	private void drawEleven(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}

		g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
		g2D.drawLine(getWidth() / 6, getHeight() / 2 + getHeight() / 6, getWidth() - getWidth() / 6, getHeight() / 2 + getHeight() / 6);  // draw the bottom line
		g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the dot
		}
	
	/*
	 * Draws the Mayan symbol that represents twelve
	 */
	private void drawTwelve(Graphics g2D, boolean colorGiven) {
		// set color based on if it is given or not
		if (colorGiven) {
			g2D.setColor(givenColor);  // set to the given color if it is a given
		} else {
			g2D.setColor(artColor);  // if it is not a given, set to normal color
		}

		g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
		g2D.drawLine(getWidth() / 6, getHeight() / 2 + getHeight() / 6, getWidth() - getWidth() / 6, getHeight() / 2 + getHeight() / 6);  // draw the bottom line
		g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
		g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
		}

	
	}

}