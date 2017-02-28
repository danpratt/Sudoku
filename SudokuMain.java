package pratt.dan.sudoku;
// Imports
import java.awt.event.*;  // import for event handling

import javax.swing.*;    // import for graphics

import java.awt.BasicStroke;   // for setting stroke in graphics 2d
import java.awt.BorderLayout;  // import for BorderLayout functionality
import java.awt.Color;         // for coloring items
import java.awt.Dimension;     // for setting up dimensions
import java.awt.Font;         // for setting font
import java.awt.Graphics;    // for drawing
import java.awt.Graphics2D;  // to use graphics 2d to draw.
import java.awt.GridLayout;  // layout manager
import java.awt.Toolkit;    // for annoying beep sound.
import java.io.*;  // for file save / load functions.
import java.util.Observable;  // for implementing observer
import java.util.Observer;   // for implementing observers

import javax.swing.border.Border;  // import to create compound border
import javax.swing.filechooser.FileNameExtensionFilter;  // for filtering file extensions when saving / loading a game.
import javax.swing.BorderFactory;  // import to create borders


/**
 * Controller for playing a Sudoku game.  Allows user to start a new game, and interact with board model.
 * Level: Challenge
 * @author Dan Pratt
 * @version Assignment 7: Sudoku Serialization/Integration
 *
 */
public class SudokuMain extends JComponent implements ItemListener, ActionListener, KeyListener, Observer {
	
	// Fields to keep track of board values
	private NumericSupport displayNumbers;
	
	// Fields to keep track of current and old models
	private SudokuBoard gameModel;  // the board for the current or new game
	private SudokuBoard oldModel;   // placeholder for the board if the user starts a new game and then cancels.
	
	// Field that holds init details
	private SudokuMRU mruModel;  // placeholder for loading functions that will load last game being played / saved games list.
	
	// Field to keep track of view
	private SudokuView gameView;  // The game View object
	private CompletionView completionView;  //  The view that allows user to see completion of board
	
	// Field to keep track of files
	private File saveFile = null;  // last saved file;
	// MRU Files
	private File[] mruFileList;  // array for all files to be passed into
	private File mruFileOne = null;
	private File mruFileTwo = null;
	private File mruFileThree = null;
	private File mruFileFour = null;
	
	// Field to set trigger for when game is won
	private boolean gameWon = false;
	
	// Constant values
	private static final int WIDTH = 800;  // height of window
	private static final int HEIGHT  = 600;  // width of window
	private static final Color artColor = new Color(177, 221, 161); // Color of regular values
	private static final Color lightBackground = new Color(0, 151, 172);  // darker color for board fill
	private static final Color darkBackground = new Color(0, 121, 150);  // lighter color for board fill
	private static final Color selectedBackground = new Color(34, 85, 51);  // fill color when cell is selected
	private static final Color activeBackground = new Color(151, 234, 244); // background color when a toolbar item is being clicked
	
	// Constant for holding current version to display in about
	private static final String version = "Version 0.7";
	
	// Font used in numeric version
	private static final Font numeric 	 = new Font("Serif", Font.PLAIN, 18);  // regular inputs
	
	// Init file used for MRU
	private static final File MRU_FILE = new File("sudoku.mru");
	
	// Shared UI Components
	private JFrame game;  // the entire game view
	private JFrame newGame; // the window that opens with new game dialogue 
	private JPanel toolbar;  // the toolbar
	private JMenuBar gameMenu;  // the menu bard
	private JMenu fileMenu, optionsMenu, aboutMenu;  // the individual menus
	private JMenuItem newGameCmd, saveGameCmd, saveAsGameCmd, loadGameCmd, quitGameCmd, helpGameCmd, aboutGameCmd, resetGameCmd,
				loadMruOneCmd, loadMruTwoCmd, loadMruThreeCmd, loadMruFourCmd; // menu options in file menu
	private JCheckBoxMenuItem showNumeric;  // checkbox menu item that allows user to turn on and off numeric display
	private JPanel gameSetupToolbar;  // toolbar that is used when user is setting up a custom game.
	
	/**
	 * Sets up a new default Sudoku board.
	 */
	public SudokuMain() {
		if (MRU_FILE.exists()) {
			loadMRU();  // load the MRU file, because it exists.
			gameModel = mruModel.getLastGame();  // set the last open game to the active game
			
			// Make sure there is a valid game
			if (gameModel != null) {
			
				loadMruFiles();  // load the MRU files into their respective variables to be used by menu.
		
				// Create the GUI
				game = new JFrame("Sudoku!");  // create window to play game in
				game.setBackground(darkBackground);  // set window background
				game.setSize(WIDTH, HEIGHT);   // set size of window for all views to fit inside.
				game.setLocationRelativeTo(null);  // centers GUI screen
				game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // set default close operation so other windows get closed as well.
				
				// create an adapter so mru still gets saved if user hits OS close button
			    game.addWindowListener(new java.awt.event.WindowAdapter() {
			    	public void windowClosing( java.awt.event.WindowEvent e ) {  
			    		quitGame();  // quit the game gracefully
		        	} 
			    });   
			
				game.setFocusable(true);  // let keyboard work
				game.addKeyListener(this);  // listen for keyboard 
				
				// setup the game
				//setupDefaultBoard();  // fill up the board with defaults
				gameView = new SudokuView(gameModel);  // create the View and link it with the Model
				game.add(gameView, BorderLayout.CENTER);  // add the View to the game.
				
				// Add the view that shows how much of the board has been completed
				completionView = new CompletionView(gameModel);
				game.add(completionView, BorderLayout.SOUTH);
				
				// add observers
				//gameModel.addObserver(gameView);  // add observer to view
				gameModel.addObserver(completionView);  // add observer so completion status can update
				gameModel.addObserver(this);  // add observer so game win can be displayed
				
				// create toolbar
				createToolbarUI(gameModel.size + 1);
				game.add(toolbar, BorderLayout.NORTH);
				// create menu
				createMenuUI();
				
				// set the board to be visible
				game.setVisible(true); // display the board
				game.pack();  // set things to their preferred size.
				
				// setup a new default game if either of the if statements are untrue
			} else {
				setupDefaultGame();  // game board is invalid
			}
			
		} else {
			setupDefaultGame();  // no mru file exists
		}
		
	}
	
	/*
	 * Creates a new game using default settings
	 */
	private void setupDefaultGame() {
		game = new JFrame("Sudoku!");  // create window to play game in
		game.setBackground(darkBackground);  // set window background
		game.setSize(WIDTH, HEIGHT);   // set size of window for all views to fit inside.
		game.setLocationRelativeTo(null);
		game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // set default close operation so other windows get closed as well.
		game.setFocusable(true);
		game.addKeyListener(this);  // listen for keyboard 
		
		// Create the model
		gameModel = new SudokuBoard(3, 3);
		// setup the game
		setupDefaultBoard();  // fill up the board with defaults
		gameView = new SudokuView(gameModel);  // create the View and link it with the Model
		game.add(gameView, BorderLayout.CENTER);  // add the View to the game.
		
		// Add the view that shows how much of the board has been completed
		completionView = new CompletionView(gameModel);
		game.add(completionView, BorderLayout.SOUTH);
		
		// add observers
		//gameModel.addObserver(gameView);  // add observer to view
		gameModel.addObserver(completionView);  // add observer so completion status can update
		gameModel.addObserver(this);  // add observer so game win can be displayed
		
		// create toolbar
		createToolbarUI(gameModel.size + 1);
		game.add(toolbar, BorderLayout.NORTH);
		// create menu
		createMenuUI();
		
		// set the board to be visible
		game.setVisible(true); // display the board
		game.pack();  // set things to their preferred size.
		
		createMRU();  // create an MRU file, because it doesn't exist (at least not in the proper location).
	}
	
	/*
	 * Loads the MRU / last used game model.
	 */
	private void loadMRU() {
		try {
			   FileInputStream fileIn = new FileInputStream(MRU_FILE);
			   ObjectInputStream sudokuMRU = new ObjectInputStream(fileIn);
			   mruModel = (SudokuMRU) sudokuMRU.readObject();
			   sudokuMRU.close();
			   fileIn.close();
		} catch (IOException e) {
			// whoops
		} catch (ClassNotFoundException e) {
			// whoops
		}
	}
	
	/*
	 * If no MRU file exists, create a new one and store in game directory.
	 */
	private void createMRU() {
		mruModel = new SudokuMRU(gameModel);
		try {
			MRU_FILE.createNewFile();
		} catch (IOException e) {
			System.out.println("File creation failure");
		}
		
		writeMRU();  // write the file
	}
	
	/*
	 * Loads file array into main so that recent saves can show up in menu
	 */
	private void loadMruFiles() {
		// load MRU Files
		mruFileList = mruModel.getMruFiles();  // load up the file array
		
		// Load individual files up into respective fields
		mruFileOne = mruFileList[0];
		mruFileTwo = mruFileList[1];
		mruFileThree = mruFileList[2];
		mruFileFour = mruFileList[3];
	}
	
	
	/*
	 * Writes the existing mruModel to file.
	 */
	private void writeMRU() {
		   try {
			   FileOutputStream fileOut = new FileOutputStream(MRU_FILE);
			   ObjectOutputStream out = new ObjectOutputStream(fileOut);
			   
			   out.writeObject(mruModel);
			   out.close();
			   fileOut.close();
			   
		   } catch (IOException e) {
			 // Functionality is hidden from user. this kind of error will also be hidden.
		   }
	}
	
	/**
	 * Nothing happens when a key is typed.
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		// Do nothing
	}

	/**
	 * When a key is pressed the user can interact with the board or enter values.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
	    int keyPressed = e.getKeyCode();  // set the value of the key that was pressed to an integer
	    int selectedRow = gameView.getSelectedRow(), selectedCol = gameView.getSelectedColumn();
	    // switch on the given key
	    switch( keyPressed ) { 
	    	// if the up key was pressed
	        case KeyEvent.VK_UP:
	        	// check to make sure the desired new row position is within range
	        	if (selectedRow > 0) {
	        		// it is in range, so move into place
	        		int newRow = selectedRow - 1;  // set the new row to be one above the old value
	        		gameView.setSelected(newRow, selectedCol);  // set the selected row to the new value
	        	} else {
	        		// Out of range, play annoying beep.
	        		Toolkit.getDefaultToolkit().beep();
	        	}
	            break;
	            // if the down key was pressed
	        case KeyEvent.VK_DOWN:
	        	// check to make sure the desired new row position is within range
	        	if (selectedRow < gameModel.size - 1) {
	        		int newRow = selectedRow + 1;  // set the new row to be one below the old value
	        		gameView.setSelected(newRow, selectedCol);  // set the selected row to the new value
	        	} else {
	        		// Out of range, play annoying beep.
	        		Toolkit.getDefaultToolkit().beep();
	        	}
	            break;
	            // if the left key was pressed
	        case KeyEvent.VK_LEFT:
	        	// check to make sure the desired new row position is within range
	        	if (selectedCol > 0) {
	        		int newCol = selectedCol - 1;  // set the new column to be one left of the old value
	        		gameView.setSelected(selectedRow, newCol);  // set the selected column to the new value
	        	} else {
	        		// Out of range, play annoying beep.
	        		Toolkit.getDefaultToolkit().beep();
	        	}
	            break;
	            // if the right key was pressed
	        case KeyEvent.VK_RIGHT :
	        	// check to make sure the desired new row position is within range
	        	if (selectedCol < gameModel.size - 1) {
	        		int newCol = selectedCol + 1;  // set the new column to be one right of the old value
	        		gameView.setSelected(selectedRow, newCol);  // set the selected column to the new value
	        	} else {
	        		// Out of range, play annoying beep.
	        		Toolkit.getDefaultToolkit().beep();
	        	}
	        	break;
	        	// if user presses 0 key
	        case KeyEvent.VK_0 :
	        	// 0 is always in range, so only need to check if it is a given
	        	if (!gameModel.isGiven(selectedRow, selectedCol)) {
	        		gameModel.setValue(selectedRow, selectedCol, 0);  // set value to 0
	        		toolbar.getComponent(0).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	            break;
	            // if user presses 1 key
	        case KeyEvent.VK_1 :
	        	// 1 is always in range, so only need to check if it is a given
	        	if (!gameModel.isGiven(selectedRow, selectedCol)) {
	        		gameModel.setValue(selectedRow, selectedCol, 1);  // set value to 0
	        		toolbar.getComponent(1).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	            // if user presses 2 key
	        case KeyEvent.VK_2 :
	        	// 2 is always in range, so only need to check if it is a given
	        	if (!gameModel.isGiven(selectedRow, selectedCol)) {
	        		gameModel.setValue(selectedRow, selectedCol, 2);  // set value to 0
	        		toolbar.getComponent(2).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user presses 3 key
	        case KeyEvent.VK_3 :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 3) {
	        		gameModel.setValue(selectedRow, selectedCol, 3);  // set value to 0
	        		toolbar.getComponent(3).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user presses 4 key
	        case KeyEvent.VK_4 :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 4) {
	        		gameModel.setValue(selectedRow, selectedCol, 4);  // set value to 0
	        		toolbar.getComponent(4).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user presses 5 key
	        case KeyEvent.VK_5 :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 5) {
	        		gameModel.setValue(selectedRow, selectedCol, 5);  // set value to 0
	        		toolbar.getComponent(5).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user presses 6 key
	        case KeyEvent.VK_6 :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 6) {
	        		gameModel.setValue(selectedRow, selectedCol, 6);  // set value to 0
	        		toolbar.getComponent(6).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user presses 7 key
	        case KeyEvent.VK_7 :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 7) {
	        		gameModel.setValue(selectedRow, selectedCol, 7);  // set value to 0
	        		toolbar.getComponent(7).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user presses 8 key
	        case KeyEvent.VK_8 :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 8) {
	        		gameModel.setValue(selectedRow, selectedCol, 8);  // set value to 0
	        		toolbar.getComponent(8).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user presses 9 key
	        case KeyEvent.VK_9 :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 9) {
	        		gameModel.setValue(selectedRow, selectedCol, 9);  // set value to 0
	        		toolbar.getComponent(9).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user pressed I key it represents 10
	        case KeyEvent.VK_I :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 10) {
	        		gameModel.setValue(selectedRow, selectedCol, 10);  // set value to 0
	        		toolbar.getComponent(10).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user pressed O it represents 11
	        case KeyEvent.VK_O :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 11) {
	        		gameModel.setValue(selectedRow, selectedCol, 11);  // set value to 0
	        		toolbar.getComponent(11).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	        	// if user pressed O it represents 12
	        case KeyEvent.VK_P :
	        	// Check if it is a given and in range
	        	if (!gameModel.isGiven(selectedRow, selectedCol) && gameModel.size >= 12) {
	        		gameModel.setValue(selectedRow, selectedCol, 12);  // set value to 0
	        		toolbar.getComponent(12).setBackground(activeBackground);  // flash the background
	        	} else Toolkit.getDefaultToolkit().beep();  // otherwise play annoying beep
	        	break;
	     }
		
	}

	/**
	 * Nothing happens when key is released.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		// do nothing
		
	}
	
	/**
	 * Runs the program
	 * @param args No command line arguments are used.
	 */
	public static void main(String[] args) {
		new SudokuMain();
	}
	
	/*
	 * Creates the tool bar used for entering data into cells
	 */
	private void createToolbarUI(int size) {
		toolbar = new JPanel();  // create a JPanel for the tool-bar to go into
		toolbar.setBackground(lightBackground);  // set the toolbar's background color
		// create the tool-bar
		for (int toolButton = 0; toolButton < size; toolButton++) {
			toolbar.add(new ToolbarItem(toolButton));  // create a new ToolbarItem (click-able icon used for entry)
		}
		
	}

	/*
	 * Adds values to the default board modeled after a Sudoku board found on http://www.websudoku.com
	 */
	private void setupDefaultBoard() {
		// set the values
		gameModel.setValue(0, 2, 3);
		gameModel.setValue(0, 8, 9);
		gameModel.setValue(1, 1, 7);
		gameModel.setValue(1, 3, 3);
		gameModel.setValue(1, 4, 9);
		gameModel.setValue(1, 7, 2);
		gameModel.setValue(2, 1, 2);
		gameModel.setValue(2, 2, 1);
		gameModel.setValue(2, 3, 5);
		gameModel.setValue(2, 4, 7);
		gameModel.setValue(2, 7, 4);
		gameModel.setValue(2, 8, 8);
		gameModel.setValue(3, 4, 3);
		gameModel.setValue(3, 7, 7);
		gameModel.setValue(4, 0, 4);
		gameModel.setValue(4, 1, 6);
		gameModel.setValue(4, 4, 8);
		gameModel.setValue(4, 7, 3);
		gameModel.setValue(4, 8, 5);
		gameModel.setValue(5, 1, 3);
		gameModel.setValue(5, 4, 1);
		gameModel.setValue(6, 0, 7);
		gameModel.setValue(6, 1, 9);
		gameModel.setValue(6, 4, 6);
		gameModel.setValue(6, 5, 1);
		gameModel.setValue(6, 6, 2);
		gameModel.setValue(6, 7, 5);
		gameModel.setValue(7, 1, 5);
		gameModel.setValue(7, 4, 4);
		gameModel.setValue(7, 5, 9);
		gameModel.setValue(7, 7, 1);
		gameModel.setValue(8, 0, 6);
		gameModel.setValue(8, 6, 4);
		
		// fix values as givens so user can't edit them
		gameModel.fixGivens();	
	}

	/*
	 * Creates a menu that allows user to start new game, quit, get help, etc.
	 */
	private void createMenuUI() {
		// setup menu bar
		gameMenu = new JMenuBar();  // create the menu bar object
		gameMenu.setBackground(darkBackground);  // set menu background color
		game.setJMenuBar(gameMenu);
		
		// setup file menu
		fileMenu = new JMenu("File");  // create the file menu
		fileMenu.setBackground(darkBackground);
		fileMenu.setMnemonic(KeyEvent.VK_F);  // set alt + f to file menu
		gameMenu.add(fileMenu);     // add file to the menu
		
		// add menu items to file
		
		// New game menu option
		newGameCmd = new JMenuItem("New", KeyEvent.VK_N);  // setup new game and enable keybaord menu interaction
		newGameCmd.addActionListener(this);  // allow things to happen when user wants to start a new game
		fileMenu.add(newGameCmd);   // add new game option to file menu.
		// create a keyboard shortcut
		newGameCmd.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_N, ActionEvent.SHIFT_MASK));
		
		// Save game menu option
		saveGameCmd = new JMenuItem("Save", KeyEvent.VK_S); // allows user to save
		saveGameCmd.addActionListener(this);  // allow things to happen when user wants to save a new game
		fileMenu.add(saveGameCmd);
		// create a keyboard shortcut
		saveGameCmd.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		
		// Save game as menu option
		saveAsGameCmd = new JMenuItem("Save As", KeyEvent.VK_A);  // allows user to save with new filename
		saveAsGameCmd.addActionListener(this);  // allow things to happen when user wants to save a game as a new filename
		fileMenu.add(saveAsGameCmd);  // add to the file menu
		// Set the keyboard shortcut
		saveAsGameCmd.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		
		// Load game menu option
		loadGameCmd = new JMenuItem("Load Game", KeyEvent.VK_L);  // allows user to load an old game
		loadGameCmd.addActionListener(this); // allow user to load games using this command
		fileMenu.add(loadGameCmd); // add it to the file menu
		// Set keyboard shortcut
		loadGameCmd.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		
		// Quit game menu option
		quitGameCmd = new JMenuItem("Quit", KeyEvent.VK_Q); // Allows user to quit
		quitGameCmd.addActionListener(this);  // adds listener so game can quit when option is selected
		// set keyboard shortcut
		quitGameCmd.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_Q, ActionEvent.SHIFT_MASK));
		fileMenu.add(quitGameCmd);  // add the quit option to the menu
		
		// Add MRU list only if there are any items to add
		if (mruFileOne != null) {
			
			fileMenu.add(new JSeparator()); // Add a spacer between the main file functions and MRU if there are any MRU files to show
			
			// Most recent file
			loadMruOneCmd = new JMenuItem(mruFileOne.toString(), KeyEvent.VK_1);  // shortcut to load recent game 1 is 1
			loadMruOneCmd.addActionListener(this);  // allow user to click on this
			fileMenu.add(loadMruOneCmd);  // only add if there is an actual file.
			
			// Next most recent
			if (mruFileTwo != null) {
			loadMruTwoCmd = new JMenuItem(mruFileTwo.toString(), KeyEvent.VK_2);  // shortcut to load recent game 2 is 2
			loadMruTwoCmd.addActionListener(this);  // allow user to click on this
			fileMenu.add(loadMruTwoCmd);  // only add if there is an actual file.
			}
			
			// Third most recent
			if (mruFileThree != null) {
			loadMruThreeCmd = new JMenuItem(mruFileThree.toString(), KeyEvent.VK_3);  // shortcut to load recent game 3 is 3
			loadMruThreeCmd.addActionListener(this);  // allow user to click on this
			fileMenu.add(loadMruThreeCmd);  // only add if there is an actual file.
			}
			
			// Fourth most recent (last on list)
			if (mruFileFour != null) {
			loadMruFourCmd = new JMenuItem(mruFileFour.toString(), KeyEvent.VK_4);  // shortcut to load recent game 4 is 4
			loadMruFourCmd.addActionListener(this);  // allow user to click on this
			fileMenu.add(loadMruFourCmd);  // only add if there is an actual file.
			}
		}
		
		// Setup Options Menu
		optionsMenu = new JMenu("Options");  // create the game menu
		optionsMenu.setMnemonic(KeyEvent.VK_T);  // set keyboard shortcut
		optionsMenu.setBackground(darkBackground); // set menu background color
		optionsMenu.addItemListener(this);  // add listener in case actions need to be performed.
		gameMenu.add(optionsMenu);  // add to the game menu
		
		// Add menu items to Options
		
		// Allow user to switch between symbols and numerical display
		showNumeric = new JCheckBoxMenuItem("Display Numbers");
		// Allow menu key navigation
		showNumeric.setMnemonic(KeyEvent.VK_D);
		// setup keyboard shortcut
		showNumeric.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_D, ActionEvent.SHIFT_MASK));  
		// finish implementation
		displayNumbers = gameView;  // connects with view
		showNumeric.setSelected(displayNumbers.showsNumeric());  // sets selected value to being controlled by view
		showNumeric.addItemListener(this);  // add item listener to this check-box.
		optionsMenu.add(showNumeric);  // add menu item to options menu
		
		// Allow user to clear everything from the board except givens
		resetGameCmd = new JMenuItem("Reset Game", KeyEvent.VK_R);  // create the menu item.
		resetGameCmd.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_R, ActionEvent.SHIFT_MASK));   // allow shortcut access
		resetGameCmd.addActionListener(this); // allows game to be reset when clicked on
		optionsMenu.add(new JSeparator());  // create a separator between Display Numbers and Reset Game
		optionsMenu.add(resetGameCmd);  // add to the option menu
		
		// Setup About Menu
		aboutMenu = new JMenu("About");
		aboutMenu.setBackground(darkBackground); // set menu background color
		aboutMenu.setMnemonic(KeyEvent.VK_A);  // allow alt + a access to menu
		gameMenu.add(aboutMenu);  // add to the menu bar
		
		// Add menu items to About
		
		// Setup help menu
		helpGameCmd = new JMenuItem("Help", KeyEvent.VK_H);  // create help menu item
		// Create keyboard shortcut
		helpGameCmd.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_H, ActionEvent.SHIFT_MASK));
		helpGameCmd.addActionListener(this);  // setup an action listener
		aboutMenu.add(helpGameCmd);  // add to the about menu
		aboutMenu.add(new JSeparator());  // create a separator between help and about
		
		// Setup about menu
		aboutGameCmd = new JMenuItem("About", KeyEvent.VK_B);  // create the about menu item with 
		// Create keyboard shortcut
		aboutGameCmd.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_B, ActionEvent.SHIFT_MASK));
		aboutGameCmd.addActionListener(this);  // setup an action listener
		aboutMenu.add(aboutGameCmd);  // add it to the menu
	}
	
	/**
	 * Controls behavior of regular menu items when clicked
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();  // get the source of the item that has been clicked
		
		// If user wants to start a new game
		if (source == newGameCmd) {
			showNewGameWindow();
		}
		
		// If user wants to quit
		if (source == quitGameCmd) {
			// Show pop-up that will allow user to save, really quit, or cancel.
			Object[] options = {"Yes",
			                    "Save", 
			                    "Cancel" };
			int n = JOptionPane.showOptionDialog(game,
			    "Do you really want to quit without saving your game first?",
			    "Quit Game",
			    JOptionPane.YES_NO_CANCEL_OPTION, 
			    JOptionPane.QUESTION_MESSAGE, 
			    null,
			    options,
			    options[2]);
			
			if ( n == JOptionPane.YES_OPTION ) {
				quitGame();
			} else if ( n == JOptionPane.NO_OPTION ) {
				saveGame(); // default save game option, tries to save to previously used file
			} else {
				// User canceled, just close the box and do nothing.
			}
		}
		
		// If user wants to save
		if (source == saveGameCmd) {
			saveGame();  // default save game option, tries to save to previously used file
		}
		
		// If user wants to save a game as a new filename
		if (source == saveAsGameCmd) {
			saveGameAs();  // lets user select save game file.
		}
		
		// If the user wants to load a game
		if (source == loadGameCmd) {
			JFileChooser loadPicker = new JFileChooser();  // create the file chooser
			loadPicker.setDialogTitle("Load Game");  // set the title to Load Game so user knows that this is what they are doing
			FileNameExtensionFilter sdkFilter = new FileNameExtensionFilter("Sudoku save files (*.sdk)", "sdk");  // create filter for sudoku save game filetypes
			// set the filter
			loadPicker.setFileFilter(sdkFilter);
			loadPicker.addChoosableFileFilter(sdkFilter);
			
			// get response from user
			int returnValue = loadPicker.showOpenDialog(this);  // get the value to see if user hit save or cancel
			
			// If user hit load, load the game using given file
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File loadFile = loadPicker.getSelectedFile();  // get the filename the user typed
				loadGame(loadFile);  // load the game with selected file.
			}
		}
		
		// Loads for MRU list
		
		// Load first game in list
		if (source == loadMruOneCmd) {
			// check if the file exists, if it doesn't, remove from MRU list
			if(mruFileOne.exists()) {
				loadGame(mruFileOne);  // load the game
			} else {
				mruModel.removeFileFromSaveList(mruFileOne);  // game doesn't exist, so remove it from the mru file
				writeMRU();  // update the mru save file.
				fileMenu.remove(loadMruOneCmd);  // remove option from file menu
				gameDoesNotExist(); // let user know the game no longer exists
			}
		}
		
		// Load second game in list
			if (source == loadMruTwoCmd) {
				// check if the file exists, if it doesn't, remove from MRU list
				if(mruFileTwo.exists()) {
					loadGame(mruFileTwo);  // load the game
				} else {
					mruModel.removeFileFromSaveList(mruFileTwo);  // game doesn't exist, so remove it from the mru file
					writeMRU();  // update the mru save file.
					fileMenu.remove(loadMruTwoCmd);  // remove option from file menu
					gameDoesNotExist(); // let user know the game no longer exists
				}
			}
			
			// Load third game in list
			if (source == loadMruThreeCmd) {
				// check if the file exists, if it doesn't, remove from MRU list
				if(mruFileThree.exists()) {
					loadGame(mruFileThree);  // load the game
				} else {
					mruModel.removeFileFromSaveList(mruFileThree);  // game doesn't exist, so remove it from the mru file
					writeMRU();  // update the mru save file.
					fileMenu.remove(loadMruThreeCmd);  // remove option from file menu
					gameDoesNotExist(); // let user know the game no longer exists
				}
			}
			
			// Load fourth (last) game in list
			if (source == loadMruFourCmd) {
				// check if the file exists, if it doesn't, remove from MRU list
				if(mruFileFour.exists()) {
					loadGame(mruFileFour);  // load the game
				} else {
					mruModel.removeFileFromSaveList(mruFileFour);  // game doesn't exist, so remove it from the mru file
					writeMRU();  // update the mru save file.
					fileMenu.remove(loadMruFourCmd);  // remove option from file menu
					gameDoesNotExist(); // let user know the game no longer exists
				}
			}
		
		// If user wants to reset the game
		if (source == resetGameCmd) {
			// Show popup that will allow user to change their minds.
			Object[] options = {"Okay",
			                    "Cancel", };
			int n = JOptionPane.showOptionDialog(game,
			    "Warning: This will remove all user entered values.\n"
			    + "\nAre you sure you want to continue?",
			    "Reset Game",
			    JOptionPane.OK_CANCEL_OPTION, 
			    JOptionPane.QUESTION_MESSAGE, 
			    null,
			    options,
			    options[1]);
			
			// As long as the user hits okay, reset the board.
			if (n == JOptionPane.OK_OPTION) {
				for ( int row = 0; row < gameModel.size; row++ ) {
					for (int col = 0; col < gameModel.size; col ++ ) {
						if (gameModel.isGiven(row, col) == false) {
							gameModel.setValue(row, col, 0);  // setting values to 0 will clear it.
						}
					}
				}
			} else {
				// User canceled, so do nothing.
			}
		}
		
		// If user wants to get help
		if (source == helpGameCmd) {
			String helpMessage = "Sudoku is a game that requires you to fill up a board with non-repeating values.\n"
					+ "Each row, column, and region should contain unique, non-repeating values. \n\n"
					+ "For keyboard entry 0 will clear a cell, 1-9 represent values 1-9, and I represents 10,\n"
					+ "O represents 11, and P represtents 12.\n\n"
					+ "Have fun!";
			JOptionPane.showMessageDialog(game, helpMessage, 
					"Sudoku Help", 
					JOptionPane.PLAIN_MESSAGE);
		}
		
		// If user wants to get view the about message
		if (source == aboutGameCmd) {
			String aboutMessage = "Sudoku is a game developed by Dan Pratt\n\n"
					+ version;
			JOptionPane.showMessageDialog(game, aboutMessage, 
					"About Sudoku", 
					JOptionPane.PLAIN_MESSAGE);
		}

		
	}
	
	/*
	 * Quits the game
	 */
	private void quitGame() {
		// update the MRU before quitting so current game will load next time
		mruModel.setLastGame(gameModel);  // update the MRU with current game
		writeMRU();  // write the file to disk.
		
		// Now actually quit.
		game.dispose();  // quit the game
		// if the new game box is up, close that too so the JRE will exit.
		if (newGame != null) {
			newGame.dispose();  // close this window if open.
		}
	}
	
	/*
	 * Saves the game (default option
	 */
	private void saveGame() {
		// If user has previously saved, or loaded a game.
		if (saveFile != null && gameSetupToolbar == null) {
			gameModel.writeToStream(gameModel, saveFile);  // save the game
			// no save game exits, so create a new one
		} else if (gameSetupToolbar != null) {
			displaySetupModeSaveError();  // show error message
		} else {
			saveGameAs();   // call method to allow user to choose filename / location to save
		}
	}
	
	/*
	 * gives user a chance to pick and name a file then saves file with specified name
	 */
	private void saveGameAs() {
		
		// check to make sure user is not in game setup mode.
		if ( gameSetupToolbar == null ) {
		
			JFileChooser savePicker = new JFileChooser();  // create the file chooser
			savePicker.setDialogTitle("Save Game");  // set the title to Save Game so user knows that this is what they are doing
			FileNameExtensionFilter sdkFilter = new FileNameExtensionFilter("Sudoku save files (*.sdk)", "sdk");  // create filter for sudoku save game filetypes
			// set the file extension filter to show save files
			savePicker.setFileFilter(sdkFilter);  
			savePicker.addChoosableFileFilter(sdkFilter);
			
			// get response from user
			int returnValue = savePicker.showSaveDialog(this);  // get the value to see if user hit save or cancel
			
			// If user hit save, save the game using given file
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				saveFile = savePicker.getSelectedFile();  // get the filename the user typed
				String fileName = saveFile.getAbsolutePath();
				if (fileName.endsWith(".sdk")) {
					gameModel.writeToStream(gameModel, saveFile);  // save the game
				} else {
					saveFile = new File(fileName + ".sdk");
					gameModel.writeToStream(gameModel, saveFile);  // save the game
				}
				
				mruModel.addFileToSaveList(saveFile);  // update the MRU list object
				writeMRU();  // update MRU file with saved game info
				game.remove(gameMenu);  // remove the old menu
				loadMruFiles();  // reload the MruFiles
				createMenuUI();  // re-create the menu with newly saved file added to the list.
				game.pack();    // refresh display.
			} 
		} else {
			displaySetupModeSaveError();  // show error message
		}
	}
	
	/*
	 * Displays setup mode save error
	 */
	private void displaySetupModeSaveError() {
		String noGameMsg = "Game cannot be saved in setup mode.";
		JOptionPane.showMessageDialog(game, noGameMsg, 
				"Save Error", 
				JOptionPane.WARNING_MESSAGE);
	}
	
	/*
	 * Loads a game using given filename.
	 */
	private void loadGame(File loadFile) {
		SudokuBoard temp = gameModel.readFromStream(loadFile);  // set to a temp placeholder to make sure we got a non null game
		
		if (mruModel.doesFileExist(loadFile)) {
			mruModel.removeFileFromSaveList(loadFile); // remove from current position in list
			mruModel.addFileToSaveList(loadFile);  // add it back to the top
			writeMRU();  // update MRU file with saved game info
			game.remove(gameMenu);  // remove the old menu
			loadMruFiles();  // reload the MruFiles
			createMenuUI();  // re-create the menu with newly saved file added to the list.
		}
		
		// As long as it isn't null, load the game
		if (temp != null) {
			saveFile = loadFile;  // If user does not use save-as, make sure they are saving over the newly loaded file.
			// Use method for reverting to old game to load the new game
			oldModel = temp;  // set the oldModel field value to the temp value we just read in to allow for revert to work.
			revertToOldGame();  // using revertToOldGame() will cause the newly loaded game to appear as the current game.
			
		}
	}

	/**
	 * Controls behavior when check-box items are checked or unchecked
	 */
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();  // get the source of the item whose state has changed
		
		// For showNumeric (check-box that controls whether or not numbers are displayed or symbols are displayed.
		if (source == showNumeric) {
			displayNumbers.setNumeric(showNumeric.isSelected());  // update the view
		}
	}
	
	/*
	 * Creates a new game that allows user to customize the board or revert back to the old game.
	 */
	private void userDefinedGameSetup(int rows, int columns) {
		// save old game state
		oldModel = null;  // clear old oldModel data
		oldModel = gameModel;  // backup the model.
		game.remove(gameView);  // remove the old board
		game.remove(completionView); 
		gameModel = new SudokuBoard(rows, columns);  // setup a new 3 x 3 model
		gameView  = new SudokuView(gameModel);  // create a view for the model
		game.add(gameView, BorderLayout.CENTER);  // add the view to the center of the game
		game.remove(toolbar);  // remove the old tool-bar
		createToolbarUI(rows * columns + 1);   // create a new one with 10 buttons
		game.add(toolbar, BorderLayout.NORTH);    // add the tool-bar to the north
		createMenuUI();  // re-create the menu (to display numbers properly)
		createSetupUI(); // add the temp buttons that allows the user to setup the board, or cancel and go back to the old board.
		game.add(gameSetupToolbar, BorderLayout.SOUTH);  // add the setup tool-bar
		game.validate();  // re-validate the screen
		// Add the view that shows how much of the board has been completed
		completionView = new CompletionView(gameModel);  // create a view to display row, column, and region status of gameModel
		
		// add observers
		gameModel.addObserver(completionView);  // add observer so completion status can update
		gameModel.addObserver(this);  // add observer so game win can be displayed
		game.pack();  // set to preferred sizes
		newGame.dispose();  //  close the setup window
	}
	
	/*
	 * If user cancels the game setup mode, the old game will be setup again here.
	 */
	private void revertToOldGame() {
		game.remove(gameView);  // remove the old board
		game.remove(completionView);
		gameModel = oldModel;   // go back to old model.
		oldModel = null;  // get rid of the stuff that used to be in oldModel because we no longer need it.
		gameView  = new SudokuView(gameModel);  // create a view for the model
		game.add(gameView, BorderLayout.CENTER);  // add the view to the center of the game
		game.remove(toolbar);  // remove the old tool-bar
		createToolbarUI(gameModel.rows * gameModel.columns + 1);   // create a new one with 10 buttons
		game.add(toolbar, BorderLayout.NORTH);    // add the tool-bar to the north
		createMenuUI();  // re-create the menu (to display numbers properly)
		// Add the view that shows how much of the board has been completed
		completionView = new CompletionView(gameModel);
		game.add(completionView, BorderLayout.SOUTH);
		
		// add observers
		gameModel.addObserver(completionView);  // add observer so completion status can update
		gameModel.addObserver(this);  // add observer so game win can be displayed
		game.validate();  // re-validate the screen
		game.pack();  // set to preferred sizes
	}
	
	/*
	 * Display the UI for setting up a new game
	 */
	private void createSetupUI() {
		gameSetupToolbar = new JPanel();  // create the toolbar
		gameSetupToolbar.setBackground(darkBackground);
		// Create the buttons
		JButton fixBoardBtn = new JButton("Play");  // button that will start the game
		fixBoardBtn.addActionListener(new ActionListener() {

			/**
			 * Fixes the givens user has entered into the board and starts the new game.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				gameModel.fixGivens();  // fix the entered values and start the game
				game.remove(gameSetupToolbar);  // remove the setup tool-bar
				gameSetupToolbar = null;  // set reference for setup toolbar to null.
				game.add(completionView, BorderLayout.SOUTH);  // replace the setup bar with the play bar.
				saveFile = null;  // Set save file to null so game does not overwrite old save that was loaded.
				game.repaint();  // repaint the game frame so tool-bar disappears.
				game.pack();  // set everything to preferred sizes.
			}
		});
		
		// Create the buttons
		JButton cancelBtn = new JButton("Cancel");  // button that will cancel setup and have board revert back to the old board
		cancelBtn.addActionListener(new ActionListener() {

			/**
			 * Cancels the setup and goes back to old game
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				game.remove(gameSetupToolbar);  // remove the setup bar
				game.repaint();  // update display so bar is gone
				revertToOldGame();  // revert back to the old game
			}
		});
		
		// Add buttons to tool-bar
		gameSetupToolbar.add(fixBoardBtn);
		gameSetupToolbar.add(cancelBtn);
		
		
	}
	
	/*
	 * Creates pop-up that allows a user to start a new game
	 */
	private void showNewGameWindow() {
		newGame = new JFrame("New Game");  // Create the new game pop-up window
		newGame.setSize(550, 145);  // set window size
		newGame.setLocationRelativeTo(game);  // make it pop up in the middle of wherever the game window is open
		newGame.setLayout(new GridLayout(4, 1));  // set to a grid layout
		newGame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // don't close other windows if this one gets closed
		
		// Create the labels
		JLabel setupLbl = new JLabel("New Game Mode", SwingConstants.CENTER);
		
		// Create the panels for button groups to go into
		JPanel customGame = new JPanel();  // create a panel for selecting the game size radio buttons
		JPanel buttons = new JPanel();
			
		
		// Create the group of selectable buttons
		ButtonGroup setSize = new ButtonGroup();  // create the group
		
		// Default (3 x 3) board
		final JRadioButton defaultGame = new JRadioButton("Quick Start", true);
		setSize.add(defaultGame);
		customGame.add(defaultGame);
		
		// 2 x 2 layout
        final JRadioButton twoByTwo = new JRadioButton("2 x 2", false);
        setSize.add(twoByTwo);
        customGame.add(twoByTwo);
        
        // 2 x 3 layout
        final JRadioButton twoByThree = new JRadioButton("2 x 3", false);
        setSize.add(twoByThree);
        customGame.add(twoByThree);
        
        // 3 x 2 layout
        final JRadioButton threeByTwo = new JRadioButton("3 x 2", false);
        setSize.add(threeByTwo);
        customGame.add(threeByTwo);
        
        // 3 x 3 layout
        final JRadioButton threeByThree = new JRadioButton("3 x 3", false);
        setSize.add(threeByThree);
        customGame.add(threeByThree);
        
        // 3 x 4 layout
        final JRadioButton threeByFour = new JRadioButton("3 x 4", false);
        setSize.add(threeByFour);
        customGame.add(threeByFour);
        
        // 4 x 3 layout
        final JRadioButton fourByThree = new JRadioButton("4 x 3", false);
        setSize.add(fourByThree);
        customGame.add(fourByThree);
        
    	
		// Create buttons
		
		// start button
		JButton startBtn = new JButton("Start");  // Creates the new board
		startBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ( defaultGame.isSelected() ) {
					game.remove(gameView);  // remove the old board
					gameModel = new SudokuBoard(3, 3);  // setup a new 3 x 3 model
					gameView  = new SudokuView(gameModel);  // create a vew for the model
					setupDefaultBoard();
					game.remove(toolbar);  // remove the old toolbard
					if (gameSetupToolbar != null) game.remove(gameSetupToolbar); // remove the setup toolbar if user was setting up a new game when they selected quick setup.
					// if (completionView != null) game.remove(completionView); // if an old view exists, get rid of it.
					game.add(gameView, BorderLayout.CENTER);  // add the view to the center of the game
					completionView = new CompletionView(gameModel);  // create a new completion view based on the model.
					game.add(completionView, BorderLayout.SOUTH);  // add the new completion view to the game.
					createToolbarUI(10);   // create a new one with 10 buttons
					game.add(toolbar, BorderLayout.NORTH);    // add the toolbar to the north
					createMenuUI();  // re-create the menu (to display numbers properly)
					// add observers
					gameModel.addObserver(completionView);  // add observer so completion status can update
					game.validate();  // re-validate the screen
					game.pack();  // set to preferred sizes
					newGame.dispose();  //  close the setup window
					// setup for a 2 x 2 board
				} else if ( twoByTwo.isSelected() ) {
					userDefinedGameSetup(2, 2);
					// setup for a 2 x 3 board
				} else if ( twoByThree.isSelected() ) {
					userDefinedGameSetup(2, 3);
					// setup for a 3 x 2 board
				} else if ( threeByTwo.isSelected() ) {
					userDefinedGameSetup(3, 2);
					// setup for a 3 x 3 board
				} else if ( threeByThree.isSelected() ) {
					userDefinedGameSetup(3, 3);
					// setup for a 3 x 4 board
				} else if ( threeByFour.isSelected() ) {
					userDefinedGameSetup(3, 4);
					// setup for a 4 x 3 board
				} else if ( fourByThree.isSelected() ) {
					userDefinedGameSetup(4, 3);
				}
			}
		});
		
		// cancel button
		JButton cancelBtn = new JButton("Cancel");  // Cancels operation
		cancelBtn.addActionListener(new ActionListener() {

			/**
			 * Exits the game
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				newGame.dispose();  // close game setup window
			}
			
		});
		
		// add the buttons to JFrame
		buttons.add(startBtn); 
		buttons.add(cancelBtn);
        
        // Add everything to the window
        newGame.add(setupLbl, BorderLayout.NORTH);
        newGame.add(customGame, BorderLayout.CENTER);
        newGame.add(buttons, BorderLayout.SOUTH);
        newGame.setVisible(true);
	}
	
	/**
	 * Checks to see if game has been won
	 */
	@Override
	public void update(Observable sudoKuBoard, Object arg) {
		if ( gameWon != true ) {
		
			for (int i = 0; i < gameModel.size; i++) {
				if (gameModel.getRegionState(i) != SudokuBoard.State.COMPLETE ) {
					break;  // leave false if game has not been won
					} else if ( i == gameModel.size - 1 && gameModel.getRegionState(i) == SudokuBoard.State.COMPLETE) {
					gameWon = true; // set game win to true
					}
				}
			
			// gameModel.deleteObserver(this);  // delete the observer
			if (gameWon == true ) winGame();  // if the game has been won, show win dialouge box.
		}
	}

	/*
	 * Displays a congratulatory message to user
	 */
	private void winGame() {
		gameModel.fixGivens();
		String winMessage = "Congratulations!  You won!\n"
				+ "\nTry starting a new game!";
		JOptionPane.showMessageDialog(game, winMessage, 
				"Winner!", 
				JOptionPane.PLAIN_MESSAGE);
		gameWon = false;  // sets flag back to false so user can play a new game if they want.
	}
	
	/*
	 * Lets user know a game doesn't exist
	 */
	private void gameDoesNotExist() {
		String noGameMsg = "Sorry, that save game no longer exists";
		JOptionPane.showMessageDialog(game, noGameMsg, 
				"Load Error", 
				JOptionPane.WARNING_MESSAGE);
	}
	
	

	/**
	 * Class used in drawing the characters into the tool bar used for selecting values
	 * Level: Challenge
	 * @author Dan Pratt
	 * @version Assignment 5: Sudoku Input Handling
	 *
	 */
	class ToolbarItem extends JPanel {
		private int value;  // value held inside object
		private final Dimension BUTTON_SIZE = new Dimension(44, 44);  // Preferred size for each button
		public Color backGroundColor = darkBackground;  // variable color to handle mouse-overs
		
		/**
		 * Creates a new ToolbarItem object.  Item is a click-able "button" that will allow a user to enter information into a SudokuBoard Model
		 * @param value the integer value that the "button" should represent
		 */
		public ToolbarItem(int value) {
			this.value = value;  // sets the value
			setPreferredSize(BUTTON_SIZE);  // set the preferred size to constant dimension
			
		}

		/**
		 * Paints the contents of the "buttons"
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);  // clear anything that used to be here.
			Border buttonBorder = BorderFactory.createCompoundBorder(
					BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());  // create a compound border to create a cool effect
			setBorder(buttonBorder);  // set the border to frame buttons
			setBackground(backGroundColor);  // set default background color
			// setup mouse actions
			addMouseListener( new MouseAdapter() {
				// When the mouse hovers over area
				public void mouseEntered(MouseEvent e) {
					backGroundColor = selectedBackground;  // set to the selected background color
				}
				// When the mouse leaves the area
				public void mouseExited(MouseEvent e) {
					backGroundColor = darkBackground;  // set back to the default (dark) background
				}
				// when mouse is pressed change to the active background color
				public void mousePressed(MouseEvent e) {
					backGroundColor = activeBackground;
				}
				// when mouse click is released change back to normal color
				public void mouseReleased(MouseEvent e) {
					backGroundColor = darkBackground;
				}
				// When the mouse has been clicked
				public void mouseClicked(MouseEvent e) {
					int row = gameView.getSelectedRow(), col = gameView.getSelectedColumn();  // figure out what cell the view is on
					// make sure that the currently selected cell isn't a given
					if (!gameModel.isGiven(row, col)) {
						gameModel.setValue(row, col, value);  // set the value into the model
						// if it is a given, give user a warning sound
					} else {
						Toolkit.getDefaultToolkit().beep();  // uh-oh, that can't happen!  Play a beep
					}
				}
			});
			
			// Create numerical representations if option is enabled
			if (displayNumbers.showsNumeric()) {
				if (value != 0) {
					g.setFont(numeric);  // set the font
					g.setColor(artColor);  // set the color
					g.drawString(String.valueOf(value), this.getWidth() / 2 - this.getWidth() / 10, this.getHeight() / 2 + this.getHeight() / 8);  // draw the value as a number
				}
			}
			
			// If the numerical representation option is not enabled, draw graphical representations (Mayan numbers)
			else {
				Graphics2D g2D = (Graphics2D)g.create();  // create Graphics2D object to draw with
				g2D.setColor(artColor);  // color for all values
				g2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));  // set stroke
				switch (value) {
				// Blank space
				case 0:
					break;  // don't draw anything that has a value of 0.
				// Mayan one symbol
				case 1:
					/* Everything below here draws the individual Mayan symbols to represent the numbers */
					
					/*
					 * Draws the Mayan symbol that represents one
					 */
					g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 12, this.getWidth() / 6, this.getHeight() / 6);  // draw the dot
					break;
				// Mayan two symbol
				case 2:
					g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
					g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
					break;
				// Mayan three symbol
				case 3:
					g2D.fillOval(this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
					g2D.fillOval(this.getWidth() / 2 - getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
					g2D.fillOval(this.getWidth() / 2 + this.getWidth() / 6, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the third dot
					break;
				// Mayan four symbol
				case 4:
					g2D.fillOval(this.getWidth() / 8, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
					g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
					g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the third dot
					g2D.fillOval(this.getWidth() / 2 + this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 14, this.getWidth() / 6, this.getHeight() / 6);  // draw the fourth dot
					break;
				// Mayan five symbol
				case 5:
					g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
					break;
				// Mayan six symbol
				case 6:
					g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() /2); // draw the line
					g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the dot
					break;
				// Mayan seven symbol
				case 7:
					g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() /2);  // draw the line
					g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
					g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
					break;
				// Mayan eight symbol
				case 8:
					g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
					g2D.fillOval(this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
					g2D.fillOval(this.getWidth() / 2 - getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
					g2D.fillOval(this.getWidth() / 2 + this.getWidth() / 6, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the third dot
					break;
				// Mayan nine symbol
				case 9:
					g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
					g2D.fillOval(this.getWidth() / 8, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
					g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
					g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the third dot
					g2D.fillOval(this.getWidth() / 2 + this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the fourth dot
					break;
				// Mayan ten symbol
				case 10:
					g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
					g2D.drawLine(getWidth() / 6, getHeight() / 2 + getHeight() / 6, getWidth() - getWidth() / 6, getHeight() / 2 + getHeight() / 6);  // draw the bottom line
					break;
				// Mayan eleven symbol
				case 11:
					g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
					g2D.drawLine(getWidth() / 6, getHeight() / 2 + getHeight() / 6, getWidth() - getWidth() / 6, getHeight() / 2 + getHeight() / 6);  // draw the bottom line
					g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 12, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the dot
					break;
				// Mayan twelve symbol
				case 12:
					g2D.drawLine(getWidth() / 6, getHeight() / 2, getWidth() - getWidth() / 6, getHeight() / 2);  // draw the line
					g2D.drawLine(getWidth() / 6, getHeight() / 2 + getHeight() / 6, getWidth() - getWidth() / 6, getHeight() / 2 + getHeight() / 6);  // draw the bottom line
					g2D.fillOval(this.getWidth() / 2 - this.getWidth() / 5, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the first dot
					g2D.fillOval(this.getWidth() / 2, this.getHeight() / 2 - this.getHeight() / 4, this.getWidth() / 6, this.getHeight() / 6);  // draw the second dot
					break;
				// no more cases, if a number is above 12, it is outside the scope of existing characters that are available inside this class.
				}
					
			}
			
		}
		
	}
}