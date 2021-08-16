import java.util.ArrayList;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class WordGuessClient extends Application {
	private Button clientConnectButton, buttonCat1, buttonCat2, buttonCat3,  exitButton, replayButton, rulesButton;
	
	private EventHandler<ActionEvent> goToGame;
	private EventHandler<ActionEvent> goToRules;
	private EventHandler<ActionEvent> goToRules2;
	
	private TextFlow wordHolderFlow;
	private Text wordGuessMessage, clientPort, clientIP, wordHolder, exitText;
	private TextField portText, ipText;
	
	private HBox ipBox, portBox, catButtonHBox, importantButtonsHBox,  middle, mainBox;
	private GridPane lettersGridPane;
	private VBox ultimateVBox, introVbox, wrapper;
	private Client clientConnection;
	private ListView<String> clientListItems;
	
	private ImageView hangmanImage;
	private Image hang0Guess, hang1Guess, hang2Guess, hang3Guess, hang4Guess, hang5Guess, blankImage, win, introImg, rulesImg, buttonsImg, clientImage,
	exitImg;

	private Image a, be, c, d, e, f, g, h, i, j, k, l, m,
		  n, o, p, q, r, s, t, u, v, w, x, y, z;

	private ImageView A, B, C, D, E, F, G, H, I, J, K, L, M,
			N, O, P, Q, R, S, T, U, V, W, X, Y, Z;

	private BackgroundImage rulesBack, buttonsBack, introBack, clientBackgroundImage, exitBack;
	private Background rulesBackground, buttonsBackground, introBackground, clientBackground, exitBackground;

	private Image[] letters = { a, be, c, d, e, f, g, h, i, j, k, l, m,
						n, o, p, q, r, s, t, u, v, w, x, y, z};

	private ImageView[] lettersViews = {A, B, C, D, E, F, G, H, I, J, K, L, M,
								N, O, P, Q, R, S, T, U, V, W, X, Y, Z};

	private Scene[] mainGame;
	private String[] picNames = {"a.png", "b.png", "c.png", "d.png", "e.png", "f.png", "g.png", "h.png", "i.png", "j.png", "k.png", "l.png", "m.png",
						  "n.png", "o.png", "p.png", "q.png", "r.png", "s.png", "t.png", "u.png", "v.png", "w.png", "x.png", "y.png", "z.png"  };
	private Character[] alphabet = {'a','b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
							'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v','w', 'x', 'y', 'z' };

	WordGuessInfo b;
	private int lengthOfWord;
	private ArrayList<Integer> placesInWord;
	private boolean hasUnderscoreUpdate, beginning, roundJustEnded, gameHasEnded, playerWon, playWantsPlayAgain, clickedOption;
	private String wordHolderString;
	
	private PauseTransition beginningPause = new PauseTransition(Duration.seconds(2));
	private PauseTransition pauseCheck = new PauseTransition(Duration.seconds(2));
	

	// Function to create buttons
	public void styleButtons(Button[] cat){
		buttonsImg = new Image("btn1.png");
		buttonsBack = new BackgroundImage(buttonsImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
				      new BackgroundSize(1.0, 1.0, true, true, false, false));
		buttonsBackground = new Background(buttonsBack);
		for (Button button: cat){
			button.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-font-family: Herculanum ");
			button.setBackground(buttonsBackground);
			button.setDisable(true);
			button.setMinSize(150, 100);
		}
	}

	// Function to update underscores
	public void underscoreUpdate() {		
		
		if((!hasUnderscoreUpdate) || (roundJustEnded)) {
			System.out.println("1");
			if (beginning) {
				System.out.println("2");
				wordHolderString = "";
				for(int i=0; i<b.length; i++) {
			    	wordHolderString += "_ ";
			    }
				beginning = false;
			}

		    wordHolder.setText(wordHolderString);
		    return;
		}
		
		
		// Replace underscores with letters based off guesses
		else {
			char[] holder = wordHolderString.toCharArray();
			for(int i: placesInWord) {
				holder[i*2] = b.guess;
				
			}
			wordHolderString = String.valueOf(holder);
		    wordHolder.setText(wordHolderString);
		    return;
		}	    
	}
	
	// Function to create background
	public void createBackground() {
		clientImage = new Image("server.jpg");
        clientBackgroundImage = new BackgroundImage(clientImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                                                                    new BackgroundSize(1.0, 1.0, true, true, false, false)); 
        clientBackground = new Background(clientBackgroundImage);
	}

	// Function for creation of letters
	public void createImageViewsLetters(){
		for (int i = 0; i < 26; i++){
			letters[i] = new Image(picNames[i]);
			lettersViews[i] = new ImageView(letters[i]);
			lettersViews[i].setPreserveRatio(true);
			lettersViews[i].setFitWidth(60);
			int finalI = i;
			lettersViews[i].setOnMouseClicked(e -> {
				lettersViews[finalI].setDisable(true);
				lettersViews[finalI].setVisible(false);
				b.hasClientLetterGuess = true;
				b.guess = alphabet[finalI] ;
				System.out.println("Guess is " + b.guess );
				clientConnection.send(b);
			});
		}
	}

	// Grid Pane
	public GridPane createGridPane(){
		GridPane grid = new GridPane();
		for (int row = 0; row< 3; row++){
			for (int col = 0; col < 8; col++){
				int i = 8*row +col;
				grid.add(lettersViews[i], col, row);
			}
		}
		grid.add(lettersViews[24], 3,3);
		grid.add(lettersViews[25], 4,3);
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(20);
		return grid;
	}

	// Reset images of letters
	public void resetLetters(boolean visible, boolean disable, ImageView[] imageViews){
		for (ImageView imageView: imageViews ){
			imageView.setVisible(visible);
			imageView.setDisable(disable);
		}
	}
	
	// Function to update hangman image
	public void updateHangman() {
		int guesses = b.numberOfGuessesLeft;
		
		switch(guesses) {
			case 6: 
				hangmanImage.setImage(blankImage);
				break;
			case 5: 
				hangmanImage.setImage(hang5Guess);
				break;
			case 4: 
				hangmanImage.setImage(hang4Guess);
				break;
			case 3: 
				hangmanImage.setImage(hang3Guess);
				break;
			case 2: 
				hangmanImage.setImage(hang2Guess);
				break;
			case 1: 
				hangmanImage.setImage(hang1Guess);
				break;
		}	
	}
	
	// Initialize hangman image
	public void createHangmanImages() {
		hang0Guess = new Image("rip.png");
		hang1Guess = new Image("5.png");
		hang2Guess = new Image("4.png");
		hang3Guess = new Image("3.png");
		hang4Guess = new Image("2.png");
		hang5Guess = new Image("1.png");
		blankImage = new Image("0.png");
		win = new Image("victory.png");
	}

	// Create intro scene
	public Scene createIntroClientScene() {
		clientConnectButton = new Button();
		clientConnectButton.setText("Connect");
		clientConnectButton.setPrefSize(150, 50);
		clientConnectButton.setStyle("-fx-font-size: 15; -fx-font-family: Herculanum");

		clientConnectButton.setOnAction(goToRules);

		wordGuessMessage = new Text("Welcome to Hangman!");
		wordGuessMessage.setStyle("-fx-font-size: 35;-fx-font-weight: bold; -fx-font-family: Herculanum");

		clientPort = new Text("Enter Your Port Number: ");
		clientPort.setStyle("-fx-font-family: Herculanum");
		portText = new TextField("5555");
		portText.setStyle("-fx-font-family: Herculanum");

		clientIP = new Text("Enter Your IP Number: ");
		clientIP.setStyle("-fx-font-family: Herculanum");
		ipText = new TextField("127.0.0.1");
		ipText.setStyle("-fx-font-family: Herculanum");

		ipBox = new HBox(15, clientIP, ipText);

		portBox = new HBox(clientPort, portText);

		ipBox.setAlignment(Pos.CENTER);
		portBox.setAlignment(Pos.CENTER);

		introVbox = new VBox(wordGuessMessage, ipBox, portBox, clientConnectButton);


		introImg = new Image("introClientBackground.jpg");

		introBack = new BackgroundImage(introImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
				new BackgroundSize(1.0, 1.0, true, true, false, false));
		introBackground = new Background(introBack);
		introVbox.setBackground(introBackground);

		introVbox.setAlignment(Pos.CENTER);
		introVbox.setSpacing(30);
		introVbox.setPadding(new Insets(90,0,0,0));

		return new Scene(introVbox, 600,600);
	}
	
	// Method to create main client game scene
	public Scene createGameScene() {
		// Create Images to be used
		hangmanImage = new ImageView();
		hangmanImage.setPreserveRatio(true);
		hangmanImage.setFitWidth(170);

		createHangmanImages();
		createImageViewsLetters();

		// Create Client Connection 
		clientConnection = new Client(data->{
			Platform.runLater(
					()->{
						// Receive package from Client to GUI
						b = (WordGuessInfo) data;
						
						// Process Package
						// Check if Game is Over
						if(b.gameIsOver) {
							gameHasEnded = true;
							// Player Won
							if( b.wordHasBeenCompleted) {
								playerWon = true;
								hangmanImage.setImage(win);
								b.wordHasBeenCompleted = false;
								placesInWord = b.locationInWord;
								underscoreUpdate();
								
								
							}
							
							// Player Lost
							else {
								wordHolderString = "";
								wordHolder.setText(wordHolderString);
								
							}
							
							//primaryStage.setScene(mainGame[1]);
							
			
							
							System.out.println("Received message (GAMEOVERPACKAGE) from Server");
							b.gameIsOver = false;

							// Turn on Replay/Exit Buttons
							//freshStart.setDisable(false);
							
							// Lock Buttons
							resetLetters(false, true, lettersViews);
						}
						
						// Server sent a message, display it, wait for next package
						else if(b.hasMessageToDisplay) {
							System.out.println("Received message (hasMessageToDisplay) from Server");
							clientListItems.getItems().add( b.Message.toString());
							
						}
						
						// Server sent a category request, unlock buttons and send a category
						else if(b.hasServerCategoryRequest) { 
							beginning = true;
							
							if(b.categoryRoundHasEnded && !b.letterWasGuessedCorrectly) {
								hangmanImage.setImage(hang0Guess);
							}
								
							resetLetters(false, true, lettersViews);
							if( b.wordHasBeenCompleted) {
								b.wordHasBeenCompleted = false;
								placesInWord = b.locationInWord;
								underscoreUpdate();
								hangmanImage.setImage(win);

							}
							else {
								wordHolderString = "";
								wordHolder.setText(wordHolderString);
							}
							
							// Check states of category, lock accordingly
							beginningPause.play();
							beginningPause.setOnFinished(e ->
							{
								buttonCat3.setVisible(true);
								buttonCat2.setVisible(true);
								buttonCat1.setVisible(true);
								buttonSwitch(true);
								if(!b.categoryOneSwitch) {
									buttonCat1.setDisable(true);
								}
								if(!b.categoryTwoSwitch) {
									buttonCat2.setDisable(true);
								}
								if(!b.categoryThreeSwitch) {
									buttonCat3.setDisable(true);
								}
							});
							
							System.out.println("Received message (requestCategoryChoice) from Server");
						}
						
						//  Server has sent a length of a mystery word, unlock letters and send a guess
						else if(b.hasServerLengthOfMysteryWord) {
							System.out.println("Received message (sendLengthMysteryWord) from Server");
							hasUnderscoreUpdate = false;
							roundJustEnded = false;
							// Check if the category round had just ended
							if(b.categoryRoundHasEnded) {
								System.out.println("Category Round JUST ENDED!");
								b.categoryRoundHasEnded = false;
								roundJustEnded = true;
							}

							// Update Client Values
							lengthOfWord = b.length;
							
							if(b.letterWasGuessedCorrectly) {
								b.letterWasGuessedCorrectly = false;
								
								// UPDATE GUI HERE WITH NEW LOCATIONS OF CORRECT LOCATIONS OF GUESS
								placesInWord = b.locationInWord;
								
								 // LOCATIONINWORD ARRAYLIST HAS INDICES OF WHERE THE LETTER APPEARED IN WORD
								hasUnderscoreUpdate = true;
							}

							// Update Hangman
							updateHangman();
							
							// CREATE OR UPDATE UNDERSCORES BASED OFF GUESS
							underscoreUpdate();
							
							// Update package booleans
							b.hasServerLengthOfMysteryWord = false;
							b.hasClientLetterGuess = true;					
						}			
					});
		});
		
		clientListItems.setPrefSize(300, 600);
		
		// Attempting to connect and start from port/ip given
		Integer port = Integer.parseInt(portText.getText());
		clientConnection.setPort(port);
		clientConnection.setIP(ipText.getText());
		clientConnection.start();
		clientListItems.setStyle("-fx-font-family: Herculanum");


		// Category Buttons
		buttonCat1 = new Button();
		buttonCat2 = new Button();
		buttonCat3 = new Button();
		exitButton = new Button();
		replayButton = new Button();

		Button[] categories = {buttonCat1, buttonCat2, buttonCat3,exitButton, replayButton};
		styleButtons(categories);

		// Animals
		buttonCat1.setText("Animals");
		buttonCat1.setOnAction(e->{
			    buttonSwitch(false);
			    resetLetters(true, false, lettersViews);
			    buttonCat2.setVisible(false);
			    buttonCat3.setVisible(false);
				b.hasServerCategoryRequest = false;
				b.hasClientCategoryResponse = true;
				b.clientWantsCat = 1;
				clientConnection.send(b);
            });
		
		// Food
		buttonCat2.setText("Food");
		buttonCat2.setOnAction(e->{
		    buttonSwitch(false);
			resetLetters(true, false, lettersViews);
			buttonCat1.setVisible(false);
			buttonCat3.setVisible(false);
			b.hasServerCategoryRequest = false;
			b.hasClientCategoryResponse = true;
			b.clientWantsCat = 2;
			clientConnection.send(b);
        });
		
		// Color
		buttonCat3.setText("Colors");
		buttonCat3.setOnAction(e->{
		    buttonSwitch(false);
			resetLetters(true, false,lettersViews);
			buttonCat2.setVisible(false);
			buttonCat1.setVisible(false);
			b.hasServerCategoryRequest = false;
			b.hasClientCategoryResponse = true;
			b.clientWantsCat = 3;
			clientConnection.send(b);
        });

		catButtonHBox = new HBox(30, buttonCat1, buttonCat2, buttonCat3);
		catButtonHBox.setPadding(new Insets(20, 0, 10,0));
		catButtonHBox.setAlignment(Pos.CENTER);
						
		wordHolder = new Text("");
		wordHolder.setStyle("-fx-font-size: 50; -fx-font-family: Herculanum");
		
		wordHolderFlow = new TextFlow(wordHolder);
		wordHolderFlow.setStyle("-fx-border-color: black;-fx-background-color: white; -fx-font-family: Herculanum");
		wordHolderFlow.setTextAlignment(TextAlignment.CENTER);
		lettersGridPane = createGridPane();

		
		MenuItem rulesMenuItem = new MenuItem("Rules");
		rulesMenuItem.setOnAction(goToRules2);
		
		MenuItem exitMenuItem = new MenuItem("Exit");
		exitMenuItem.setOnAction(e->{
			Platform.exit();
			System.exit(0);
        });
		
		Menu options = new Menu ("Options");
		options.getItems().addAll(rulesMenuItem, exitMenuItem);
		
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(options);

		
		
		
		VBox vbox = new VBox( 50, catButtonHBox, wordHolder);
		HBox box = new HBox(vbox, hangmanImage);
		box.setAlignment(Pos.TOP_CENTER);
		vbox.setAlignment(Pos.CENTER);

	    ultimateVBox = new VBox(30, box, lettersGridPane);
	    ultimateVBox.setAlignment(Pos.CENTER);

	    clientListItems.setTranslateY(15);
	    mainBox = new HBox(clientListItems, ultimateVBox);
	    mainBox.setAlignment(Pos.CENTER);
	    mainBox.setSpacing(40);
	    wrapper = new VBox(menuBar, mainBox);

	    createBackground();
	    wrapper.setBackground(clientBackground);

	    if(gameHasEnded) {
	    	return new Scene(wrapper, 500, 660);
	    }
		return new Scene(wrapper, 1100, 660);
	}

	// Method to create rules scene to explain game
	public Scene createRulesScene(){
		rulesImg = new Image("rulesBackground.jpg");
		rulesBack = new BackgroundImage(rulesImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
				new BackgroundSize(1.0, 1.0, true, true, false, false));
		rulesBackground = new Background(rulesBack);

		Text rulesText = new Text("\n" +
				"\nHOW TO PLAY: \n" +
				"    - Pick a category and a word will be chosen from that category\n" +
				"    - Pick any letter you think belongs to the word!\n" +
				"    - Continue picking letters until you either solve the word \n" +
				"      or incorrectly pick 6 letters.\n" +
				"\nTO WIN: \n" +
				"    - Guess one word from each category correctly!\n" +
				"\nTO LOSE: \n" +
				"    - Guess three words from one category incorrectly.\n" +
				"\nHINT:\n" +
				"    - Try starting with vowels to make guessing the word easier!");

		rulesText.setStyle(" -fx-font-family: Herculanum; -fx-alignment: center; -fx-font-size: 16");

		Button goToGameButton = new Button("PLAY");
		goToGameButton.setOnAction(goToGame);
		goToGameButton.setStyle("-fx-font-size: 25; -fx-font-family: Herculanum");

		rulesButton = new Button("PLAY");

		rulesButton.setStyle("-fx-font-family: Herculanum");
		rulesButton.setOnAction(goToGame);
		
		VBox wrap = new VBox(20, rulesText, goToGameButton);
		wrap.setAlignment(Pos.CENTER);
		wrap.setPadding(new Insets(90,0,20,0));
	
		wrap.setBackground(rulesBackground);
		Scene rulesScene = new Scene(wrap, 1000, 700);
		return rulesScene;
	}
	
	// Method to send client to ending Scene
	public Scene createExitReplayScene(){
		exitImg = new Image("server.jpg");
		exitBack = new BackgroundImage(exitImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
				new BackgroundSize(1.0, 1.0, true, true, false, false));
		exitBackground = new Background(exitBack);
		
		// Different messages depending on game ending
		if(playerWon) {
			playerWon = false; 
			exitText = new Text("       You've Won the Game! \nWould you like to Replay or Exit?");
		}
		else {
			exitText = new Text("       You've Lost the Game... \nWould you like to Replay or Exit?");
		}

		exitText.setStyle(" -fx-font-family: Herculanum; -fx-alignment: center; -fx-font-size: 16");
		exitText.setTranslateX(50);
		Button exitButton = new Button("Exit");
		Button replayButton = new Button("Replay");
		
		exitButton.setOnAction(e->{
			Platform.exit();
            System.exit(0);
        });
		exitButton.setPrefSize(150, 75);
		exitButton.setStyle("-fx-font-size: 30; -fx-background-color: #f53333; ");
		
		replayButton.setOnAction(e->{
			playWantsPlayAgain = true;
            b.playerWishesToPlayAgain = true;
            clientConnection.send(b);
        });
		replayButton.setPrefSize(150, 75);
		replayButton.setStyle("-fx-font-size: 30;-fx-background-color: #2feb05; ");
		
		HBox replayBox = new HBox(20, replayButton, exitButton);

		
		VBox mainExitBox = new VBox(30, exitText, replayBox);
		mainExitBox.setLayoutX(40);
		mainExitBox.setLayoutY(100);
		
		Pane exitPane = new Pane();
		exitPane.setBackground(exitBackground);
		exitPane.getChildren().addAll(mainExitBox);
		return new Scene(exitPane, 400,400);
	}
	
	
	// Method to turn category buttons on and off
	public void buttonSwitch(boolean val) {
		//on
		if (val) {
			buttonCat1.setDisable(false);
			buttonCat2.setDisable(false);
			buttonCat3.setDisable(false);
			
		}
		
		else {
			buttonCat1.setDisable(true);
			buttonCat2.setDisable(true);
			buttonCat3.setDisable(true);
		}	
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Word Guess Game");
		
		// Game State Variables
		placesInWord = new ArrayList<Integer>();
		clientListItems = new ListView<String>();
		
		gameHasEnded = false;
		playerWon = false;
		playWantsPlayAgain = false;
		clickedOption = false;
		
		// Array of Scenes
		mainGame = new Scene[4];
		
		// Event Handlers
		// Goes to Rules Scene
		goToRules = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent action) {
				mainGame[0] = createRulesScene();
				primaryStage.setScene(mainGame[0]);
			}
		};
		
		// Goes to Rules Scene from Game
		goToRules2 = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent action) {
				clickedOption = true;
				primaryStage.setScene(mainGame[0]);
			}
		};
		
		// Goes To Game Scene
		goToGame = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent action) {
				// Check if client came from game or not
				if(!clickedOption) {
					mainGame[1] = createGameScene();
				}	
				else {
					clickedOption = false;
				}
				primaryStage.setScene(mainGame[1]);
				pauseCheck.play();
				
			}			
		}; 
		
		// EventHandler for Exiting
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
		
		// Pause to check for conditions
		pauseCheck.setOnFinished(e -> 
		{
			if (gameHasEnded) {
				gameHasEnded = false;
				primaryStage.setScene(createExitReplayScene());
				
			}
			
			if(playWantsPlayAgain) {
				playWantsPlayAgain = false;
				clientListItems.getItems().clear();
				primaryStage.setScene(mainGame[0]);	
				
				
			}		
			pauseCheck.play();
			
		});

		primaryStage.setScene(createIntroClientScene());
		primaryStage.show();
	}

}
