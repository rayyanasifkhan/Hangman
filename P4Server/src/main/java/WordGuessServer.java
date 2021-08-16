import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WordGuessServer extends Application {
	Text introPortMsg, numberOfClientsHeader, numberOfClientsText, lastWinnerHeader, lastWinnerText;
	TextField portText;
	Button serverPower;
	VBox serverIntroBox;
	HBox numberClientsBox, lastWinnerBox;
	Pane startPane;
	ListView<String> listItems;
	Server serverConnection;
	EventHandler<ActionEvent> goToServer;
	Image serverImage;
	BackgroundImage serverBackgroundImage;
	Background serverBackground;

	public static void main(String[] args) {
		launch(args);
	}
	
	// Method to initialize background
	public void makeBackground() {
		serverImage = new Image("server.jpg");
        serverBackgroundImage = new BackgroundImage(serverImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                                                                    new BackgroundSize(1.0, 1.0, true, true, false, false)); 
        serverBackground = new Background(serverBackgroundImage);
	}

	// Creating the server scene
	public Scene createServerScene() {
		// Create Pane to place things on
		Pane serverPane = new Pane();
		serverPane.setPadding(new Insets(20));
		
		// Creating server connection
		int portVal = Integer.parseInt(portText.getText());
		serverConnection = new Server(data -> {
			Platform.runLater(()->{
				listItems.getItems().add(data.toString());
				numberOfClientsText.setText(Integer.toString(serverConnection.count-1));
				if(serverConnection.lastWinner != 0 )
					lastWinnerText.setText(Integer.toString(serverConnection.lastWinner));
			});

		}, portVal);
		
		// Text Server GUI Elements	
		// Number of Clients
		numberOfClientsHeader = new Text("Number of Clients:");
		numberOfClientsHeader.setFont(Font.font("Bradley Hand ITC", FontWeight.BOLD, 30));
		
		
		numberOfClientsText = new Text("0");
		numberOfClientsText.setFont(Font.font("Bradley Hand ITC", FontWeight.BOLD, 30));
		
		numberClientsBox = new HBox(10, numberOfClientsHeader, numberOfClientsText);
		numberClientsBox.setLayoutX(330);
		numberClientsBox.setLayoutY(20);
		
		// Last Winner
		lastWinnerHeader = new Text("Last Winner: Client Number");
		lastWinnerHeader.setFont(Font.font("Bradley Hand ITC", FontWeight.BOLD, 30));
		
		lastWinnerText = new Text("N/A");
		lastWinnerText.setFont(Font.font("Bradley Hand ITC", FontWeight.BOLD, 30));
		
		lastWinnerBox = new HBox(10, lastWinnerHeader, lastWinnerText);
		lastWinnerBox.setLayoutX(330);
		lastWinnerBox.setLayoutY(60);
			
		// Display state of game and game changes with listView
		listItems.setPrefSize(300, 660);
		listItems.setLayoutX(10);
		listItems.setLayoutY(20);
		
		// Place nodes on pane, return scene with pane
		serverPane.setBackground(serverBackground);	
		serverPane.getChildren().addAll(listItems, numberClientsBox, lastWinnerBox);
				
		return new Scene(serverPane, 800, 700);
	}
	
	// Creating initial scene
	public Scene createIntroServerScene(){
		// Intro Server GUI Elements
		introPortMsg = new Text("Enter Your Port Number Below: ");
		introPortMsg.setStyle("-fx-font-weight: bold;");
		introPortMsg.setFont(Font.font("Bradley Hand ITC", FontWeight.BOLD, 30));
		introPortMsg.setFill(Color.BLACK);
		portText = new TextField("5555");
		
		serverPower = new Button();
		serverPower.setText("POWER ON");
		serverPower.setPrefSize(200, 100);
		serverPower.setStyle("-fx-font-size: 30 ");
		
		// To go to next window
		serverPower.setOnAction(goToServer);
		
		serverIntroBox = new VBox(40, introPortMsg, portText, serverPower);
		serverIntroBox.setAlignment(Pos.CENTER);
		serverIntroBox.setLayoutX(110);
		serverIntroBox.setLayoutY(125);
		
		// Pane creation
		startPane = new Pane();
		startPane.getChildren().addAll(serverIntroBox);
		makeBackground();
		startPane.setBackground(serverBackground);
		
		return new Scene(startPane, 600,400);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Word Guess Game Server");
		
		listItems = new ListView<String>();
		
		// Event Handler for going to server
		goToServer = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent action) {
				primaryStage.setScene(createServerScene());	
			}			
		};
		
		// Initial Stage Set
		primaryStage.setScene(createIntroServerScene());
		primaryStage.show();

		// Exit handler
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
	}

}
