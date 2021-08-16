import java.io.Serializable;
import java.util.ArrayList;

public class WordGuessInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	boolean hasMessageToDisplay;
	String Message;
	
	// Boolean to turn on and off category tiles
	boolean categoryOneSwitch;
	boolean categoryTwoSwitch;
	boolean categoryThreeSwitch;
	
	// Client choice of category
	int clientWantsCat;

	// Type of message being sent booleans
	boolean hasServerCategoryRequest;
	boolean hasClientCategoryResponse;
	boolean hasServerLengthOfMysteryWord;
	boolean hasClientLetterGuess;
	boolean hasServerVerdict;
	boolean hasClientReplayDecision;
	
	// Check after every hasServerVerdict
	boolean gameIsOver;
	boolean categoryRoundHasEnded;     // ends as in the last guess led to complete correct answer or 6th incorrect guess
	boolean letterWasGuessedCorrectly;
	boolean wordHasBeenCompleted;
	// Specific Game Info
	int length;          			   // 1. Server sends length of word to be guessed
	char guess;          			   // 2. Client sends guess
	boolean letterWasInWord;    	   // 3. Server sends respond with if the letter was in word
	ArrayList<Integer> locationInWord; // 3a. location of correct if *if client was right*
	int numberOfGuessesLeft;    	   // 3b. number of guesses left *if client was wrong*
									   //                 repeat 2->3b until either 
									   // (numberOfGuessLeft == 0) || ( wordIsGuessedCorrectly )
	
	
	
	// Count of correct guesses from each category
	int catOneRightGuessCounter;
	int catTwoRightGuessCounter;
	int catThreeRightGuessCounter;
	
	// Count of incorrect guesses from each category
	int catOneWrongGuessCounter;
	int catTwoWrongGuessCounter;
	int catThreeWrongGuessCounter;
	
	
	
	// Ending Booleans
	boolean playerWon;
	boolean playerWishesToPlayAgain;
	//boolean playerWishesToEndGame;            // may not need

}
