import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;

public class Server{
	int count = 0;
	int port;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	int lastWinner;
	
	Server(Consumer<Serializable> call, int portVal){
		lastWinner = 0;
		port = portVal;
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	Server(int portVal) {
        port = portVal;
    }
	
	public class TheServer extends Thread
	{
		
		public void run() {
		
			try(ServerSocket mysocket = new ServerSocket(port);)
			{
				
			callback.accept("WordGuess Server....");
		    System.out.println("Server is waiting for a client!");

		    // Creation of client thread loop
		     while(true) 
		     {
		    	count++;
		
				ClientThread c = new ClientThread(mysocket.accept(), count);
				callback.accept("Client has connected to server: " + "Client #" + count);
				clients.add(c);

				c.start();					
			 }
		    }//end of try
			
				catch(Exception e) {
					callback.accept("Server socket did not launch");
				}
			}//end run
		}// end TheServer
	
		class ClientThread extends Thread
		{
			// Client Connection Info
			Socket connection;
			int clientNumber;

			ObjectInputStream in;
			ObjectOutputStream out;
			
			
			// Client Game Info
			char currentGuess;
			String currentWord;
			ArrayList<Character> uniqueLettersLeft;
			int currentWordLength;
			int currentCategory;
			
			boolean playerWasCorrect;
			boolean wishToReplay;
					
			ArrayList<String> cat1;
			ArrayList<String> cat2;
			ArrayList<String> cat3;
			
			String correctGuessMessage;
			String correctWordMessage;
			String gameWonMessage;
			
			String incorrectGuessMessage;
			String incorrectWordMessage;
			String gameLostMessage;
			
			// Package to communicate via
			WordGuessInfo b;
			
			ClientThread(Socket s, int val)
			{				
				this.connection = s;
				this.clientNumber = val;	
			}
			
			public void run(){
				
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);	
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}
				
		
				System.out.println("clientNumber: " + clientNumber);

				// Creating initial package that will be used to communicate with Client
				b = new WordGuessInfo();
				
				// GAME LOOP
				 while(true) {
					    try { 
					    	startGame(b);
					    	
					    	if(!wishToReplay) {
					    		break;
					    	}
					    }// end try
					    
					    catch(Exception e) {	
					    	callback.accept("Client: " + clientNumber + " has closed down.");
					    	--count;
					    	clients.remove(this);
					    	break;
					    }			    
					}// end of while
				}//end of run

			public void startGame(WordGuessInfo b) throws Exception  {
				// Clear initial package for use in new game
				brandNew(b);
				
				// Send Client For Category Choice
				requestCategoryChoice(b);
			}
			
			// Function to reset server variables
			public void resetVar() {
				currentGuess = '0';
				currentWord = "";
				uniqueLettersLeft = new ArrayList<Character>();
				currentWordLength=0;
				currentCategory =0;
				
				playerWasCorrect = false;
				wishToReplay = false;
			}
			
			// First Stage of Server Client Communication
			// Requesting Category Choice from Client
			public void requestCategoryChoice(WordGuessInfo b) throws Exception 
			{
				// Choose client to send package to
				ClientThread t = clients.get( clients.indexOf(this));
				
				// Assemble package
				b.hasServerCategoryRequest = true;
				System.out.println("Sending message (requestCategoryChoice) to client " + (clientNumber));

				// Send Client For Category Choice
				t.out.writeObject(b);
				t.out.reset(); 

				// Receive Client Category Choice
				b = (WordGuessInfo) in.readObject();
				
				// Package should be assembled correctly
				if(b.hasClientCategoryResponse) {
					findWordToSend(b); //  Picking word from that category.	
					b.hasClientCategoryResponse = false;
				}
				
				// Package was not assembled correctly
				else {
					System.out.print("Took Else in requestCategoryChoice");
				}

				// Send Client Number of Letters in Word from Category
				sendLengthMysteryWord(b, currentWord);
			}
			
			public void sendLengthMysteryWord(WordGuessInfo b, String mysteryWord) throws Exception{
				// Choose client to send package to
				ClientThread t = clients.get( clients.indexOf(this));
				
				// Assemble package
				playerWasCorrect = false;
				b.hasServerLengthOfMysteryWord = true;
				b.length = currentWordLength;
				
				// Send Client Number of Letters in Word from Category
				System.out.println("Sending message (sendLengthMysteryWord) to client " + clientNumber);
				t.out.writeObject(b);
				t.out.reset();  

				// Guessing Game Loop
				guessGameLoop();
			}
			
			// Function to return proper string
			public String serverMessage(int val) {
				if( val == 1) {
					return ("Client " + clientNumber + " has guessed " + currentGuess + " for word " + currentWord);
				}
				
				else if (val == 2) {
					return ("Client " + clientNumber + " has guessed word " + currentWord + " correctly!");
				}
				else if (val == 3) {
					return ("Client " + clientNumber + " failed to guess the word " + currentWord + ".");
				}
				
				else if (val == 4) {
					return ("Client " + clientNumber + " has won the game!");
				}
				else if (val == 5) {
					return ("Client " + clientNumber + " has lost the game!");
				}
				
				else if (val == 6) {
					return ("Client " + clientNumber + " has chosen to replay the game!");
				}
				else if (val == 7) {
					return ("Client " + clientNumber + " has chosen to exit the game.");
				}
				else {
					return "DID NOT PROVIDE PROPER VAL";
				}	
			}
			
			public void guessGameLoop()  throws Exception{
				// Game loop: Receive Client Guess, Evaluate, Send Response, Repeat
				while (true) {
					// Array to hold indices of where guess is in word, if present
					ArrayList<Integer> result = new ArrayList<Integer>();
					
					// Receive Client Guess	
					b = (WordGuessInfo) in.readObject();
					
					// Check if Package Assembled Correctly
					if( !b.hasClientLetterGuess) {
						System.out.println("Package did not have client guess");
					}
					
					// Package was assembled correctly with guess
					else {
						// Evaluate guess and return indices if needed
						result = evaluateGuess(b);
						b.hasClientLetterGuess = false;
						
						// Send to server log
						callback.accept(serverMessage(1));
					}
					
					// Client's Guess was in Word ----------------------------------
					if(playerWasCorrect) {
						b.locationInWord = result;
						
						// Check if Correct Guess Finished Word
						if( guessFinishedWordW()) {
							// Send to server log
							callback.accept(serverMessage(2));
							
							b.letterWasGuessedCorrectly = true;
							incrementCorrectGuessCounter(b);
							
							// Check if Correct Word Finished Game in a Win
							if( gameHasEndedW(b)) {
								// Variable for laster winner server GUI
								lastWinner = clientNumber;
								
								// Send Client Package indicating he won, and ask for replay
								callback.accept(serverMessage(4));
								sendClientWinningPackage(b);
								break;
							}
							
							// Correct Word did NOT finish GAME, so lock category of word and re ask for category
							else {
								// Assembling package
								b.numberOfGuessesLeft = 6;
								b.categoryRoundHasEnded = true;
								
								// Send Client
								sendMessageToClient(b, createMessage("wordWin"));						
								requestCategoryChoice(b); 
							}			
						} //end finishedWord
						
						// Correct Guess did NOT finish WORD
						else {
							// Continue game, locking category guessed correctly in
							sendMessageToClient(b, createMessage("guessWin"));
							sendLengthMysteryWord(b, currentWord);	
						}
						
					}// end ifPlayerWasCorrect
					
					// Client's Guess wasn't in Word ----------------------------------
					else {
						// Decrement player's number of guesses
						b.numberOfGuessesLeft -= 1;
						
						// Incorrect Guess finished Word
						if( guessFinishedWordL(b)) {
							// Update server log
							callback.accept(serverMessage(3));
							
							// Update game variables
							b.letterWasGuessedCorrectly = false;
							incrementIncorrectGuessCounter(b);
		
							// Incorrect Word finished Game in Loss
							if ( gameHasEndedL(b)) {
								// Send Client Package indicating he lost, and ask for replay
								callback.accept(serverMessage(5));
								sendClientLosingPackage(b);
								break; 
								}
							
							// Incorrect word but did not lose game yet
							else {
								// Reset player's number of guesses
								b.numberOfGuessesLeft = 6;
								b.categoryRoundHasEnded = true;
								
								// Send for client 
								sendMessageToClient(b, createMessage("wordLoss"));						
								requestCategoryChoice(b); 
							}
						}
						
						// Incorrect Guess did NOT finish word (still have guesses left)
						else {
							sendMessageToClient(b, createMessage("guessLoss"));
							sendLengthMysteryWord(b, currentWord);
						}		
					}
								
				}//end while
							
			} // end guessGameLoop()
			
			// Function to send client winning package
			public void sendClientWinningPackage(WordGuessInfo b) throws Exception{
				// Assembling package
				b.gameIsOver = false;
				sendMessageToClient(b, createMessage("gameWin"));
				
				// Choose correct client to send to
				ClientThread t = clients.get( clients.indexOf(this));
				
				// Assembling package
				b.gameIsOver = true;
				b.playerWon = true;
				
				// Send Client Number of Letters in Word from Category
				System.out.println("Sending message (sendClientWinningPackage) to client " + clientNumber);
				t.out.writeObject(b);
				t.out.reset();  

				// Guessing Game Loop
				b = (WordGuessInfo) in.readObject();
				
				// Check if player wish to replay
				if(b.playerWishesToPlayAgain) {
					callback.accept(serverMessage(6));
					resetVar();
					startGame(b);
				}
				
				// Player wanted to exit
				else {
					callback.accept(serverMessage(7));
				}
				
			}
			
			// Function to send client losing package
			public void sendClientLosingPackage(WordGuessInfo b) throws Exception{
				// Game variables
				b.gameIsOver = false;
				sendMessageToClient(b, createMessage("gameLoss"));
				ClientThread t = clients.get( clients.indexOf(this));
				
				// Assembling package
				b.gameIsOver = true;
				b.playerWon = false;
				
				// Send Client Number of Letters in Word from Category
				System.out.println("Sending message (sendClientLosingPackage) to client " + clientNumber);
				t.out.writeObject(b);
				t.out.reset();  
		
				// Guessing Game Loop
				b = (WordGuessInfo) in.readObject();
				
				// Check if player wish to replay
				if(b.playerWishesToPlayAgain) {
					callback.accept(serverMessage(6));
					resetVar();
					startGame(b);
				}
				
				// Player wanted to exit
				else {
					callback.accept(serverMessage(7));
				}
			}
			
			// Function to return custom strings
			public String createMessage(String val) {
				String result = "";
				
				if( val.equals("guessWin")) {
					result += "Your guess was correct!";
				}
				else if ( val.equals("wordWin")) {
					result += "Your guess completed the word! \nCategory has been completed!";
				}
				
				else if ( val.equals("gameWin")) {
					result += "You've won the game! \nClick on the Menu to Replay or Exit";
				}
				
				else if ( val.equals("guessLoss") && b.numberOfGuessesLeft == 1) {
					result += "Your guess was incorrect. \nYou have " + b.numberOfGuessesLeft + " Guess Left";
				}
				
				else if ( val.equals("guessLoss")) {
					result += "Your guess was incorrect. \nYou have " + b.numberOfGuessesLeft + " Guesses Left";
				}
				
				else if ( val.equals("wordLoss")) {
					result += "You've run out of guesses for this word!";
				}
				
				else if ( val.equals("gameLoss")) {
					result += "You've lost the game. \nClick on the Menu to Replay or Exit";
				}
				
				else if ( ( val.equals("0"))  || ( val.equals("2")) || ( val.equals("3")) ||
						( val.equals("4")) || ( val.equals("5")) )  {
					result += "You have " + val + " guesses left!";
				}
				
				else if ( val.equals("1")) {
					result += "You have " + val + " guess left!";
				}
							
				return result;
			}
			
			// Function to send package 
			public void sendMessageToClient(WordGuessInfo b, String messageToSend) throws Exception{
				// Find correct client to send to
				ClientThread t = clients.get( clients.indexOf(this));
				
				// Set correct server variables
				b.hasMessageToDisplay = true;
				b.Message = messageToSend;
				
				// Send for Client
				System.out.println("Sending message (sendMessageToClient) to client " + (clientNumber));	
				t.out.writeObject(b);		 
				t.out.reset();
				b.hasMessageToDisplay = false;
						
			}
			
			// If Guess is right, return indices of appearances, otherwise return empty ArrayList
			public ArrayList<Integer> evaluateGuess(WordGuessInfo c) {
				ArrayList<Integer> result = new ArrayList<Integer>();
				currentGuess = c.guess;
				
				// Find all instances of guess in word
				for (int i=0; i<currentWord.length(); i++) {
					if(currentWord.charAt(i) == currentGuess) {
						result.add(i);
						playerWasCorrect = true;
						c.letterWasGuessedCorrectly = true;
					}
				}		
				
				return result;			
			}
			
			// Function to check if the word is finished being guessed
			public boolean guessFinishedWordW() {
				// Remove guess from unique letters list
				uniqueLettersLeft.remove(uniqueLettersLeft.indexOf(b.guess));
				
				// Guess was last unique letter left to guess
				if(uniqueLettersLeft.size() <= 0) {
					// Set variables correctly
					b.wordHasBeenCompleted = true;
					return true;
				}
				
				else {
					return false;
				}	
			}
			
			// Function to see if word is finished 
			public boolean guessFinishedWordL( WordGuessInfo c) {
				// Guess was last unique letter left to guess
				if(c.numberOfGuessesLeft <= 0) {
					return true;
				}
				
				else {
					return false;
				}	
			}
			
			// Function to see if game has ended in a win
			public boolean gameHasEndedW(WordGuessInfo c) {
				// Check Winning Conditions
				if( (c.catOneRightGuessCounter >= 1) && ( c.catTwoRightGuessCounter >= 1) &&
						( c.catThreeRightGuessCounter >= 1) ) {
						
						return true;
					}
			
				return false;
			}
			
			// Function to see if game has ended in a loss
			public boolean gameHasEndedL(WordGuessInfo c) {
				// Check Losing Conditions
				if( (c.catOneWrongGuessCounter >= 3) || ( c.catTwoWrongGuessCounter >= 3) || 
						( c.catThreeWrongGuessCounter >= 3) ) {
						c.gameIsOver = true;
						c.playerWon = false;
						return true;
					}
			
				return false;
			}

			// Function to reset package
			public void brandNew(WordGuessInfo b) {
				b.categoryOneSwitch = true;
				b.categoryTwoSwitch = true;
				b.categoryThreeSwitch = true;
				
				b.clientWantsCat = 0;
				
				b.hasServerCategoryRequest = false;
				b.hasClientCategoryResponse = false;
				b.hasServerLengthOfMysteryWord = false;
				b.hasClientLetterGuess = false;
				b.hasServerVerdict = false;
				b.hasClientReplayDecision = false;
				
				b.gameIsOver = false; 
				b.categoryRoundHasEnded = false;     
				b.letterWasGuessedCorrectly = false;
				b.wordHasBeenCompleted = false;
				
				b.length = 0;
				b.locationInWord = new ArrayList<Integer>();
				b.numberOfGuessesLeft = 6;
				
				
				b.catOneRightGuessCounter = 0;
				b.catTwoRightGuessCounter = 0;
				b.catThreeRightGuessCounter = 0;
				
				b.catOneWrongGuessCounter = 0;
				b.catTwoWrongGuessCounter = 0;
				b.catThreeWrongGuessCounter = 0;
				
				
				// CLIENT THREAD STUFF
				
				// Also create new category word lists for this new game
				// Category1 = Animals
				cat1 = new ArrayList<String>( Arrays.asList(
						"elephant","tiger", "zebra", "giraffe", "dolphin", "ostrich", "Hyena"));
				// Category2 = Foods
				cat2 = new ArrayList<String>( Arrays.asList(
						"pizza","hotdog", "spaghetti", "banana", "apple", "burger", "sandwich"));
				// Category3 = Colors
				cat3 = new ArrayList<String>( Arrays.asList(
						"magenta","cyan", "purple", "yellow", "orange", "turquoise", "burgundy"));
				
				// Server Variables
				playerWasCorrect = false;
				wishToReplay = false;
				uniqueLettersLeft = new ArrayList<Character>();
							
			}//end brandNew()
			
			// Function to find the word to send 
			public void findWordToSend(WordGuessInfo c) {		
				String result;
				int sizeCatList;
				
				// Pick a random word from the category
				Random index = new Random();
				
				// Category 1
				if(c.clientWantsCat == 1) {
					
					currentCategory = 1;
					// Getting a random index in the range of the category list, grabbing that word
					sizeCatList = cat1.size();
					
					int wordIndex = index.nextInt(sizeCatList-1);
					
					result = cat1.get(wordIndex);
					
					cat1.remove(wordIndex);
					
				}
				
				// Category 2
				else if(c.clientWantsCat == 2) {
					currentCategory = 2;
					// Getting a random index in the range of the category list, grabbing that word
					sizeCatList = cat2.size();
					int wordIndex = index.nextInt(sizeCatList-1);
					
					result = cat2.get(wordIndex);
					cat2.remove(wordIndex);
				}
				
				// Category 3
				else if(c.clientWantsCat == 3) {
					currentCategory = 3;
					// Getting a random index in the range of the category list, grabbing that word
					sizeCatList = cat3.size();
					int wordIndex = index.nextInt(sizeCatList-1);
					
					result = cat3.get(wordIndex);
					cat3.remove(wordIndex);
				}
				
				// Somehow a category was not chosen
				else {
					sizeCatList = -999; // error value
					result = "error!!";
					System.out.println("Should not have gotten here, *findWordsToSend*");
				}
				
				// Reset just in case
				uniqueLettersLeft = new ArrayList<Character>();
						
				// Fill unique letter arrayList for game
				for(int i=0; i < result.length();i++) {
					if ( !uniqueLettersLeft.contains( result.charAt(i)) ) {
						uniqueLettersLeft.add(result.charAt(i));
					}
				}
				
				// Setting variables to word that was taken from category list
				currentWord = result;
				currentWordLength = result.length();	
			}
			
			// Function to increment wrong guess counters
			public void incrementIncorrectGuessCounter(WordGuessInfo d) {
				if(currentCategory == 1) {
					d.catOneWrongGuessCounter++;
				}
				else if(currentCategory == 2) {
					d.catTwoWrongGuessCounter++;
				}
				else if(currentCategory == 3) {
					d.catThreeWrongGuessCounter++;
				}
				// Error check
				else {
					System.out.println("Shouldn't get here, *incrementIncorrectGuessCounter*");
				}
				
			}//end incrementIncorrect()
			
			// Function to increment correct guess counter
			public void incrementCorrectGuessCounter(WordGuessInfo d) {
				if(currentCategory == 1) {
					d.catOneRightGuessCounter++;
					d.categoryOneSwitch = false;
				}
				else if(currentCategory == 2) {
					d.catTwoRightGuessCounter++;
					d.categoryTwoSwitch = false;
				}
				else if(currentCategory == 3) {
					d.catThreeRightGuessCounter++;
					d.categoryThreeSwitch = false;
				}
				
				else {
					System.out.println("Shouldn't get here, *incrementCorrectGuessCounter*");
				}
				
			}//end incrementCorrect()
			
		}//end class clientThread
		
}//end class Server 


	
	

	
