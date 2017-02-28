package pratt.dan.sudoku;
import java.awt.Color;  // for setting color
import java.awt.Dimension;  // for setting dimension
import java.awt.FlowLayout;  // for flowlayout use
import java.awt.Graphics;  // for drawing
import java.awt.GridLayout;  // for grid layout use
import java.util.Observable;  // for implementing observer
import java.util.Observer;  // for implementing observer

import javax.swing.BorderFactory;  // for creating the border
import javax.swing.JPanel;  // for using JPanel (the rest of swing is not needed)
import javax.swing.border.Border;  // for creating the compound border


	/**
	 * View that shows graphically how user is doing.  Will show if user has completed sections, if they are incomplete or in error.
	 * Level: Challenge
	 * 
	 * @author Dan Pratt
	 * @version Assignment 7: Sudoku Serialization / Integration
	 * 
	 *
	 */
	class CompletionView extends JPanel implements Observer {
		private final Dimension preferredSize = new Dimension(44, 44);
		private final Border CompletionViewBorder = BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());  // create a compound border to create a cool effect
		
		private SudokuBoard data;  // holds data to update view
		private JPanel rowCompletion = new JPanel();  // For row completion status
		private JPanel columnCompletion = new JPanel();  // For column completion status
		private JPanel regionCompletion = new JPanel();  // for region completion status
		
		// background color for frame
		private static final Color darkBackground = new Color(0, 121, 150);
		
		// Colors for displaying completion
		private static final Color incompleteBackground = new Color(0, 151, 172);  // lighter color for when state is incomplete
		private static final Color errorBackground = new Color(0, 121, 150);  // darker color for when state is in error
		private static final Color completeBackground = new Color(34, 85, 51);  // fill color when state is complete.
		
		CompletionView(SudokuBoard data) {
			setLayout(new FlowLayout(FlowLayout.CENTER, 25, 10)); // each completion view will take one of these rows.
			setBackground(darkBackground);
			this.data = data;  // connect with the model
			
			// Setup row completion box
			rowCompletion.setLayout(new GridLayout(data.size, 1));
			rowCompletion.setPreferredSize(preferredSize);
			rowCompletion.setBorder(CompletionViewBorder);
			
			// Setup columns completion box
			columnCompletion.setLayout(new GridLayout(1, data.size));
			columnCompletion.setPreferredSize(preferredSize);
			columnCompletion.setBorder(CompletionViewBorder);
			
			// Setup region completion box
			regionCompletion.setLayout(new GridLayout(data.columns, data.rows));
			regionCompletion.setPreferredSize(preferredSize);
			regionCompletion.setBorder(CompletionViewBorder);
			
			// Connect views with data
			setupViews();
			
			// Add completion boxes to view
			add(rowCompletion);
			add(columnCompletion);
			add(regionCompletion);
		}
		
		/*
		 * Updates view with correct data for rows, columns, and regions.
		 */
		private void setupViews() {
			for ( int i = 0; i < data.size; i++ ) {
				rowCompletion.add(new CompletionCell(i, SudokuBase.Type.ROW));
				columnCompletion.add(new CompletionCell(i, SudokuBase.Type.COLUMN));
				regionCompletion.add(new CompletionCell(i, SudokuBase.Type.REGION));
			}
			
		}

		/**
		 * Repaints the view to update any changes
		 */
		@Override
		public void update(Observable sudokuBoard, Object arg) {
			repaint();
		}
		
	
	/**
	 * Takes care of individual cells in completion view
	 * Level: Challenge
	 * @author Dan Pratt
	 * @version Assignment 7: Sudoku Serialization / Integration
	 *
	 */
	class CompletionCell extends JPanel {
		private int area;  // area cell object is responsible for (i.e. the row, column or region number)
		private SudokuBoard.State state;  // holds the state of the cell (incomplete, error, or complete)
		private SudokuBoard.Type type;  // holds the type of data it represents (row, column, or region)
		
		/**
		 * Creates a new CompletionCell view object.  This object displays various colors based on the state of the region it is representing.
		 * @param area the row, column, or region identifier that cell is responsible for handling
		 * @param type ENUM (ROW, COLUMN, REGION) identifier that cell is responsible for handling
		 */
		CompletionCell(int area, SudokuBoard.Type type) {
			this.area = area;  // set the area that cell is responsible for
			this.type = type;  // set the type that cell is responsible for
		}
		
		/**
		 * Sets color based on state to display game status to user
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);  // clear whatever used to be there
			
			// get data based on what we are checking (ROW, COLUMN, or REGION)
			switch (type) {
			case ROW:
				state = data.getRowState(area);
				break;
			case COLUMN:
				state = data.getColumnState(area);
				break;
			case REGION:
				state = data.getRegionState(area);
				break;
			}
			
			// Now set he color based on the state we found
			if (state == SudokuBoard.State.INCOMPLETE) {
				setBackground(incompleteBackground);
			} else if (state == SudokuBoard.State.ERROR) {
				setBackground(errorBackground);;
			} else {
				setBackground(completeBackground);
			}
			
		}
	}
}