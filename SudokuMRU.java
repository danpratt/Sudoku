package pratt.dan.sudoku;
import java.io.File;  // for using File objects

/**
 * This class keeps track of the MRU data so that users can load the last played game (without needing to save) and keeps track of up to the last 4 games played.
 * 
 * Level: Challenge
 * @author Dan Pratt
 * @version Assignment 7: Sudoku Serialization/Integration
 *
 */
public class SudokuMRU implements java.io.Serializable {

	// Declare fields that class will keep track of
	private SudokuBoard lastOpenGame;  // keeps track of the last game that was being played (does not load from save game file).
	private int numberOfSaves;  // will let GUI know how many save game files can be loaded
	private File[] fileList = new File[4];  // array of files (can be up to four) of the save games
	
	private static final int MAX_SAVE_FILES = 4;
	
	public SudokuMRU(SudokuBoard game) {
		lastOpenGame = game;  // set the game
	}
	
	/**
	 * Saves the board object when called to be loaded in the future.
	 * @param lastGame SudokuBoard object to be saved
	 */
	public void setLastGame(SudokuBoard lastGame) {
		lastOpenGame = lastGame;   // update the board object. 
	}
	
	/**
	 * Provides access to game model saved inside MRU file
	 * @return SudokuBoard model of last game played
	 */
	public SudokuBoard getLastGame() {
		return lastOpenGame;
	}
	
	/**
	 * Provides list of filenames saved in MRU list object
	 * @return File[] array of files stored in MRU list.
	 */
	public File[] getMruFiles() {
		return fileList;
	}
	
	/**
	 * Adds a file to the list of save files to show in MRU list.  Only allows for up to 4, and reorders list so most recent save is first.
	 * @param file File to add to list.
	 */
	public void addFileToSaveList(File file) {
		// Take care of the new file insertion
		// It doesn't matter if the list is empty and it shifts a null value up one.
		File nextFile = fileList[0];
		fileList[0] = file;
		if ( numberOfSaves < MAX_SAVE_FILES ) numberOfSaves++;  // as long as the max number of saved files has not been reached, increment the number of saves.
		for ( int i = 1; i < numberOfSaves ; i++	) {
			if ( i == MAX_SAVE_FILES ) break;  // limit the number of save files to four
			File tempFile = fileList[i];  // take the value out of the current location
			fileList[i] = nextFile;  // add the held value to the current location
			nextFile = tempFile;  // queue the temp file to be the next to be placed into the list.
		}
		
	}
	
	/**
	 * Checks the number of save files held inside SudokuMRU object
	 * @return int value representing the number of save files held in SudokuMRU object.
	 */
	public int getNumberOfSaves() {
		return numberOfSaves;
	}
	
	/**
	 * If a file no longer exists, it can be removed from the list of saved files.
	 * @param file File to be removed from MRU.
	 */
	public void removeFileFromSaveList(File file) {
		for (int i = 0; i < numberOfSaves; i++) {
			if (fileList[i].equals(file)) {
				fileList[i] = null;
				break;
			}
		}
		File[] tempFiles = new File[4];
		int tc = 0;
		for (int i = 0; i < numberOfSaves; i++) {
			if (fileList[i] != null) {
				tempFiles[tc] = fileList[i];
				tc++;
			}
		}
		numberOfSaves--;
		fileList = new File[4];
		for(int i = 0; i < numberOfSaves; i++) {
			fileList[i] = tempFiles[i];
		}
	}
	
	/**
	 * Checks to see if a file exists in the MRU list
	 * @param file File to be checked
	 * @return true if file exists, false if it does not
	 */
	public boolean doesFileExist(File file) {
		for (int i = 0; i < numberOfSaves; i++) {
			if (fileList[i] == file) return true;  // match found, so return true.
		}
		return false;  // wasn't found, so return false
	}
	
	
}