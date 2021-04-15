// JavaFX
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.control.Button; 
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.shape.*;
import javafx.animation.AnimationTimer;// Animation timer
import javafx.scene.input.KeyEvent;// Key Listener
// Java
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
*  @author Artem Polkovnikov
*  @version 14.04.2021
*/

/** Game client that is responsible for all the game's client side functionality*/
public class GameClient extends Application implements EventHandler<ActionEvent>
{
   // Attributes
   
   // Stage
   /** Main stage*/
   private Stage stage;
   
   // Window proportions
      // Track (16/9)
   /** Racing track width*/
   public static final double TRACK_WIDTH = 1550;
   /** Racing track height*/
   public static final double TRACK_HEIGHT = 872;
      // Chat
   /** Chat sidebar width*/
   public static final double CHAT_WIDTH = 250;
   /** Chat sidebar height*/
   public static final double CHAT_HEIGHT = 500;
      // Window
   /** Main window width*/
   public static final double WINDOW_WIDTH = TRACK_WIDTH + CHAT_WIDTH;
   /** Main window height*/
   public static final double WINDOW_HEIGHT = TRACK_HEIGHT;
   
   
   // Layout
      // Scenes
            // Title Screen scene
   /** Title screen scene*/
   private Scene titleScreenScene;
   /** Server IP text field*/
   private TextField tfServerIp = new TextField();
   /** Game password text field*/
   private TextField tfServerPassword = new TextField();
   /** Player nickname text field*/
   private TextField tfClientName = new TextField();
   /** Game start button*/
   private Button btnStart = new Button("Start");
   /** Car color select text field*/
   private TextField tfColorSelect = new TextField();
            // Game scene
   /** Game scene*/
   private Scene gameScene;
   /** Root pane of the game scene*/
   private GridPane root;
   /** Racing track pane*/
   private StackPane track;
   /** Racing track image*/
   private Image imgTrack;
   /** Racing track imageView*/
   private ImageView imgViewTrack;
   
   // Images
   /** Racing track image file locaiton*/
   private static final String TRACK_FILE_NAME = "Assets/track.png";
   
   // Networking
      // General attributes
   /** Server port*/
   private int serverPort = 42069;
   /** Server IP*/
   private String serverIP = "127.0.0.1";
   /** Client socket*/
   private Socket socket = null;
   /** Server ObjectOutputStream*/
   private ObjectOutputStream oos = null;
   /** Server ObjectInputStream*/
   private ObjectInputStream ois = null;
   /** Game client object storage*/
   private GameClient gameClient = null;
      // Synchronization of streams
   /** Client streams synchronization object*/
   private Object oosLock = new Object();
   
   // Multiplayer
      // Main client's game attributes
   /** Main client's identification number*/
   private int playerNumber;
   /** Main client's starting X coordinate*/
   private double mainStartX = 0;
   /** Main client's starting Y coordinate*/
   private double mainStartY = 0;
   /** Main client's starting rotation*/
   private double mainStartDegree = 0;
      // Opponents' information
   /** ArrayList of Opponent objects that contain opponent details used for their display*/
   private ArrayList<Opponent> opponents = new ArrayList<Opponent>();
   /** Synchronization object for opponents ArrayList*/
   private Object opponentsLock = new Object();
   /** ArrayList of opponent Player objects*/
   private ArrayList<Player> opponentPlayers = new ArrayList<Player>();
   /** Synchronization object for opponentPlayers ArrayList*/
   private Object opponentPlayersLock = new Object();
      
      // CheckPoints
   /** ArrayList of check point lines that are used for check point display on the game scene*/
   private ArrayList<Line> checkPoints = new ArrayList<Line>();
   /** ArrayList of check points' X coordinates*/
   private ArrayList<Double> checkPointCoordinatesX = new ArrayList<Double>();
   /** ArrayList of check points' Y coordinates*/
   private ArrayList<Double> checkPointCoordinatesY = new ArrayList<Double>();
   /** Number of the current check point that corresponds to the index of check points in each of the check point ArrayLists*/
   private int currentCheckPoint = 0;
   /** Number of the current lap. Used for lap number display.*/
   private int currentLap = 1;
   /** Total number of laps in the race. Used for total lap number display.*/
   private int numOfLaps = 0;
   /** Text that contains lap progress information*/
   private Text tLaps = new Text("Lap: " + currentLap + "/" + numOfLaps);
      // Game Start animation
   /** Count down text (three)*/
   private Text tCountThree = new Text("3");
   /** Count down text (two)*/
   private Text tCountTwo = new Text("2");
   /** Count down text (one)*/
   private Text tCountOne = new Text("1");
   /** Count down text (start)*/
   private Text tStart = new Text("Start!");
   /** Game end text (finish)*/
   private Text tFinish = new Text("Finish!");
   /** Game start boolean used for enabling keyboard listeners*/
   private boolean gameStart = false;
      
      // Chat
         // General
   /** Chat sidebar root pane*/
   private VBox rootChat = new VBox(5);
   /** Public chat root pane*/
   private VBox rootPublicChat = new VBox(5);
   /** Private chat root pane*/
   private VBox rootPrivateChat = new VBox(5);
         // Public Chat
   /** Public chat text area*/
   private TextArea taPublicChat = new TextArea();
   /** Public chat user intup text field*/
   private TextField tfChatEnter = new TextField();
            // Chat reaction buttons
   /** Amount of time chat reactions buttons go in cooldown for (Milliseconds)*/
   private static final double BUTTON_SLEEP = 2000;
   /** Good luck have fun button*/
   private Button btnGLHF = new Button("GLHF");
   /** Good game button*/
   private Button btnGG = new Button("GG");
   /** Well played button*/
   private Button btnWP = new Button("WP");
   /** Chat reaction buttons array*/
   private Button[] reactionButtons = {btnGLHF, btnGG, btnWP};
         // Private Chat
            // Private chat text areas
   /** Player 1 private chat text area*/
   private TextArea taP1Chat = new TextArea();
   /** Player 2 private chat text area*/
   private TextArea taP2Chat = new TextArea();
   /** Player 3 private chat text area*/
   private TextArea taP3Chat = new TextArea();
   /** Player 4 private chat text area*/
   private TextArea taP4Chat = new TextArea();
   /** Preset player names array*/
   private String[] playerNames = {"Player 1", "Player 2", "Player 3", "Player 4"};
            // Private chat enter buttons
   /** Player 1 private chat enter button*/
   private Button btnP1Chat = new Button(playerNames[0]);
   /** Player 2 private chat enter button*/
   private Button btnP2Chat = new Button(playerNames[1]);
   /** Player 3 private chat enter button*/
   private Button btnP3Chat = new Button(playerNames[2]);
   /** Player 4 private chat enter button*/
   private Button btnP4Chat = new Button(playerNames[3]);
            //Text displayed on player private chat buttons
   /** Player 1 private chat button text*/
   private String btnP1Text = null;
   /** Player 2 private chat button text*/
   private String btnP2Text = null;
   /** Player 3 private chat button text*/
   private String btnP3Text = null;
   /** Player 4 private chat button text*/
   private String btnP4Text = null;
            // Private chat work
   /** Number of the private chat in focus*/
   private int privateChatClientNumber = 1;
            // Private chat enter
   /** Private chat enter text field*/
   private TextField tfPrivateChatEnter = new TextField();
   /** Private chat enter button*/
   private Button btnPrivateChatEnter = new Button("Send Private");
   
   // Animation Timer
   /** Time of the last frame update. Used to limit the game's frame rate.*/
   private long lastUpdate = 0;
   
   // General game attributes
   /** Game configuration file location*/
   private static final String CONFIG_FILE = "game-configuration.xml";
   /** Used to stop certain functions from execution in case of an error*/
   private boolean error = false;
   
   /** Boolean that determines the input from the keyboard*/
   private boolean gas, brake, turnLeft, turnRight;
   
   /** Main player*/
   private Player mainPlayer = null;
   
   /** Array of car file names*/
   private String[] carFileArray = {"Assets/car_blue.png", "Assets/car_orange.png", "Assets/car_purple.png", "Assets/car_red.png"};
   /** Array of car color names*/
   private String[] carNameArray = {"Blue",         "Orange",         "Purple",         "Red"};
   /** Index that is used to set car color. It corresponds to the index withing carFileArray and carNameArray*/
   private int carArrayIndex = 0;
   
   /** Sets up the stage and GUI*/
   public void start(Stage _stage)
   {
      stage = _stage;
      stage.setTitle("Artem Polkovnikov - Java Racer Online");
      stage.setOnCloseRequest(
      new EventHandler<WindowEvent>() {
         public void handle(WindowEvent evt)
         {
            try
            {
               if(oos != null)    oos.close();
               if(ois != null)    ois.close();
               if(socket != null) socket.close();
            }
            catch(IOException ioe)
            {
               System.out.println(ioe);
               System.exit(1);
            }
            System.exit(0);
         }
      });
      
      // Load in Game Configuration
      XMLSettings xmlWorker = new XMLSettings(CONFIG_FILE);
      File configFile = new File(CONFIG_FILE);
      if(!configFile.exists())
      {
         xmlWorker.writeXML();
      }
      xmlWorker.readXML();
      
      // Get server info
      serverPort = xmlWorker.serverPort;
      serverIP = xmlWorker.serverIP;
      
      // Get user presets
      String presetNickname = xmlWorker.nickname;
      String presetCarColor = xmlWorker.carColor;
      
      switch(presetCarColor)
      {
         case "blue":
            carArrayIndex = 0;
            break;
         case "orange":
            carArrayIndex = 1;
            break;
         case "purple":
            carArrayIndex = 2;
            break;
         case "red":
            carArrayIndex = 3;
            break;
         default:
            carArrayIndex = 0;
            break;
      }
      
      // Track pane
      track = new StackPane(); 
      
      // GameClient
      gameClient = this;
      
      // CountDown styling
      StackPane spCountDown = new StackPane();
      tCountOne.setStyle("-fx-fill: white; -fx-font-size: 200px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 1;");
      tCountTwo.setStyle("-fx-fill: white; -fx-font-size: 200px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 1;");
      tCountThree.setStyle("-fx-fill: white; -fx-font-size: 200px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 1;");
      tStart.setStyle("-fx-fill: white; -fx-font-size: 120px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 1;");
      tFinish.setStyle("-fx-fill: white; -fx-font-size: 120px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 1;");
      
      tCountOne.setVisible(false);
      tCountTwo.setVisible(false);
      tCountThree.setVisible(false);
      tStart.setVisible(false);
      tFinish.setVisible(false);
      
      spCountDown.setAlignment(Pos.CENTER);
      spCountDown.getChildren().addAll(tCountOne, tCountTwo, tCountThree, tStart, tFinish);
      
      // Image initialization
      initImages();
      
      // Track pane set up
      track.getChildren().addAll(imgViewTrack, spCountDown);
      
      // Scene set up
      titleScreenScene = TitleScreen.getScene(this, (int)WINDOW_WIDTH, (int)WINDOW_HEIGHT, tfServerIp, tfServerPassword, tfClientName, btnStart, tfColorSelect, carNameArray[carArrayIndex], serverIP, presetNickname);
      
      stage.setScene(titleScreenScene);
      stage.setResizable(false);
      stage.centerOnScreen();
      stage.show();
   }
   
   /** Sets up the game scene that is used for the race*/
   public void createGameScene()
   {
      // Layout
         // Root
      root = new GridPane();
      
      // Chat sidebar setup
         // Public chat
            // Sign
      FlowPane fpPublicChat = new FlowPane(5,5);
         Text txtPublicChat = new Text("Public Chat");
         txtPublicChat.setStyle("-fx-font-size: 25px; -fx-font-weight: bold");
         fpPublicChat.setAlignment(Pos.CENTER);
         fpPublicChat.getChildren().add(txtPublicChat);
         
            // Public chat text area
         taPublicChat.setPrefWidth(250);
         taPublicChat.setPrefHeight(400);
         taPublicChat.setEditable(false);
         taPublicChat.setWrapText(true);
         taPublicChat.setText("Public Chat Room\n");
            // Reaction buttons
      FlowPane fpChatPreset = new FlowPane(5,5);
         fpChatPreset.setAlignment(Pos.CENTER);
         fpChatPreset.getChildren().addAll(btnGLHF, btnGG, btnWP);
            // Public chat enter
      FlowPane fpChatEnter = new FlowPane(5,5);
         fpChatEnter.setAlignment(Pos.CENTER);
         tfChatEnter.setPromptText("Message...");
         tfChatEnter.setPrefColumnCount(15);
         tfChatEnter.setOnKeyPressed(new EventHandler<KeyEvent>()
         {
             @Override
             public void handle(KeyEvent keyEvent)
             {
                 if (keyEvent.getCode() == KeyCode.ENTER)
                 {
                     String text = tfChatEnter.getText();
                     tfChatEnter.clear();
                                    
                     if(text.length() > 0)
                     {
                        taPublicChat.appendText("\nYou: " + text);
                        sendChatMessageToServer(text);
                     }
                 }
             }
         });
         Button btnChatEnter = new Button("Send");
         fpChatEnter.getChildren().addAll(tfChatEnter, btnChatEnter);
   
      rootPublicChat.getChildren().addAll(/*fpMinimize,*/fpPublicChat, taPublicChat, fpChatPreset, fpChatEnter);
         // END Public Chat
         
         // Private Chat
            // Sign
      FlowPane fpPrivateChat = new FlowPane(5,5);
         Text txtPrivateChat = new Text("Private Chat");
         txtPrivateChat.setStyle("-fx-font-size: 25px; -fx-font-weight: bold");
         fpPrivateChat.setAlignment(Pos.CENTER);
      fpPrivateChat.getChildren().add(txtPrivateChat);
      
            // Opponent player buttons set up
      FlowPane fpPlayerSelect = new FlowPane(5,5);
         fpPlayerSelect.setAlignment(Pos.CENTER);
         synchronized(opponentsLock)
         {
            for(Opponent op:opponents)
            {
               int opponentId = op.getClientNumber();
               Button btnOpponent = null;
               String btnText = op.getClientName() + "#" + op.getClientNumber();
               
               switch(opponentId)
               {
                  case 1:
                     btnOpponent = btnP1Chat;
                     btnOpponent.setText(btnText);
                     btnP1Text = btnText;
                     break;
                  case 2:
                     btnOpponent = btnP2Chat;
                     btnOpponent.setText(btnText);
                     btnP2Text = btnText;
                     break;
                  case 3:
                     btnOpponent = btnP3Chat;
                     btnOpponent.setText(btnText);
                     btnP3Text = btnText;
                     break;
                  case 4:
                     btnOpponent = btnP4Chat;
                     btnOpponent.setText(btnText);
                     btnP4Text = btnText;
                     break;
               }
               
               if(btnOpponent != null)
               {
                  fpPlayerSelect.getChildren().add(btnOpponent);
               }
            } // for(Opponent)
            
            // Select a private chat to show
            if(opponents.size() > 0)
            {
               showPrivateChat(opponents.get(0).getClientNumber());
            }
         }
      
            // Private chat logs
      StackPane spPrivateChat = new StackPane();
         spPrivateChat.setAlignment(Pos.CENTER);
            taP1Chat.setPrefWidth(CHAT_WIDTH);
            taP1Chat.setPrefHeight(200);
            taP1Chat.setEditable(false);
            taP1Chat.setWrapText(true);
            taP1Chat.setText(btnP1Text + " Private Chat Room\n");
            
            taP2Chat.setPrefWidth(CHAT_WIDTH);
            taP2Chat.setPrefHeight(200);
            taP2Chat.setEditable(false);
            taP2Chat.setWrapText(true);
            taP2Chat.setText(btnP2Text + " Private Chat Room\n");
         
            taP3Chat.setPrefWidth(CHAT_WIDTH);
            taP3Chat.setPrefHeight(200);
            taP3Chat.setEditable(false);
            taP3Chat.setWrapText(true);
            taP3Chat.setText(btnP3Text + " Private Chat Room\n");
         
            taP4Chat.setPrefWidth(CHAT_WIDTH);
            taP4Chat.setPrefHeight(200);
            taP4Chat.setEditable(false);
            taP4Chat.setWrapText(true);
            taP4Chat.setText(btnP4Text + " Private Chat Room\n");
      spPrivateChat.getChildren().addAll(taP1Chat, taP2Chat, taP3Chat, taP4Chat);
      
            // Private chat enters
      FlowPane fpPrivateChatEnter = new FlowPane(5,5);
         fpPrivateChatEnter.setAlignment(Pos.CENTER);
         tfPrivateChatEnter.setPromptText("Message...");
         tfPrivateChatEnter.setPrefColumnCount(12);
         tfPrivateChatEnter.setOnKeyPressed(new EventHandler<KeyEvent>()
         {
             @Override
             public void handle(KeyEvent keyEvent)
             {
                 if (keyEvent.getCode() == KeyCode.ENTER)
                 {
                     String privateText = tfPrivateChatEnter.getText();
                     tfPrivateChatEnter.clear();
                              
                     sendPrivateMessage(privateText);
                 }
             }
         });
         fpPrivateChatEnter.getChildren().addAll(tfPrivateChatEnter, btnPrivateChatEnter);
         
      
      rootPrivateChat.getChildren().addAll(fpPrivateChat, fpPlayerSelect, spPrivateChat, fpPrivateChatEnter);
      
      Label lblFill1 = new Label("");
      Label lblFill2 = new Label("");
      
      rootChat.getChildren().addAll(rootPublicChat, lblFill1, rootPrivateChat);
      
      // Both chats setOnAction()
      btnChatEnter.setOnAction(this);
      btnGLHF.setOnAction(this);
      btnGG.setOnAction(this);
      btnWP.setOnAction(this);
      
      btnPrivateChatEnter.setOnAction(this);
      btnP1Chat.setOnAction(this);
      btnP2Chat.setOnAction(this);
      btnP3Chat.setOnAction(this);
      btnP4Chat.setOnAction(this);
      
      // Racing track UI
      FlowPane fpLaps = new FlowPane();
         fpLaps.setAlignment(Pos.TOP_LEFT);
         tLaps.setStyle("-fx-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 1;");
         fpLaps.getChildren().add(tLaps);
      track.getChildren().add(fpLaps);
      
      // Racing track check points set up
      if(checkPoints.size() > 0)
      {
         for(Line l:checkPoints)
         {
            Pane p = new Pane();
               p.getChildren().add(l);
               p.setTranslateX(checkPointCoordinatesX.get(checkPoints.indexOf(l)));
               p.setTranslateY(checkPointCoordinatesY.get(checkPoints.indexOf(l)));
               l.setVisible(false);
            track.getChildren().add(p);
         }
         
         checkPoints.get(currentCheckPoint).setStyle("-fx-stroke: red;");
         checkPoints.get(currentCheckPoint).setVisible(true);
      }
      
      // Track focus request
      track.setOnMouseClicked(new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent mouseEvent) {
              track.requestFocus();
          }
      });
      
      // Root set up
      root.add(track, 0, 0);
      root.add(rootChat, 1, 0);
      
      // Scene
      gameScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
      
      // Key Listners
      gameScene.setOnKeyPressed(
         new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
               switch (event.getCode()) {
                  case W:    if(gameStart) {gas = true;} 
                     break;
                  case S:    if(gameStart) {brake = true;}
                     break;
                  case A:    if(gameStart) {turnLeft  = true;} 
                     break;
                  case D:    if(gameStart) {turnRight  = true;} 
                     break;
               }
            }
         });
   
      gameScene.setOnKeyReleased(
         new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
               switch (event.getCode()) {
                  case W:      gas = false; 
                     break;
                  case S:      brake = false; 
                     break;
                  case A:      turnLeft  = false; 
                     break;
                  case D:      turnRight  = false; 
                     break;
               }
            }
         });
      
      // Animation timer
      AnimationTimer timer = 
         new AnimationTimer() {
            @Override
            public void handle(long now)
            {
               if (now - lastUpdate >= 16_900_000) // Force update every 6.9 MS
               {
                  // Get the turning value
                  int turn = 0;
                  
                  if(turnRight) { turn += 1; }
                  if(turnLeft) { turn -= 1; }
                  
                  // Get the acceleration value
                  double velocity = 0.0;
                  
                  if(gas) { velocity += 0.5; }
                  if(brake) { velocity -= 1.0; }
                  
                  mainPlayer.update(turn, velocity);
                  lastUpdate = now;
               }
            }
         };
      timer.start();
   }
   
   /** Button dispatcher*/
   public void handle(ActionEvent ae)
   {
      // Source button text
      Object source = ae.getSource();
      
      // Command switch
      if(source instanceof Button)
      {
         String command = ((Button)source).getText();
         
         // Private chat selector switch
         // Sets command to standard value based on the players' id
         String[] parts = command.split("#");
         if(parts.length > 1)
         {
            command = parts[parts.length - 1];
            
            switch(command)
            {
               case "1":
                  command = "Player 1";
                  break;
               case "2":
                  command = "Player 2";
                  break;
               case "3":
                  command = "Player 3";
                  break;
               case "4":
                  command = "Player 4";
                  break;
            }
         }
         
         // Button commands
         switch(command)
         {
            case "Start": // Start game
               if(tfServerIp.getText().length() > 0 &&
                  tfServerIp.getText().length() <= 20 &&
                  tfServerPassword.getText().length() <= 20 &&
                  tfClientName.getText().length() > 0 &&
                  tfClientName.getText().length() <= 10)
               {
                  Client c = new Client();
                  c.start();
                  btnStart.setDisable(true);
                  TitleScreen.disableInterface(true);
               }
               else
               {
                  DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting the game", "One or multiple fields are missing information and/or have too many symbols");
               }
               break;
            case "Save  Config": // Save game configuration to xml
               // Ask user for the confirmation
               boolean saveAns = DisplayMessage.showConfirmation(this.stage, "Are you sure you want to overwrite the game config?");
               if(saveAns)
               {
                  // Save config
                  XMLSettings xmlWorker = new XMLSettings(CONFIG_FILE, serverPort, tfServerIp.getText(), tfClientName.getText(), carNameArray[carArrayIndex].toLowerCase());
                  xmlWorker.writeXML();
                  
                  DisplayMessage.showAlert(stage, AlertType.INFORMATION, "Configuration saved to the XML file", xmlWorker.readXML());
               }
               break;
            case "Reset Config": // Reset game configuration back to the standard
               boolean resetAns = DisplayMessage.showConfirmation(this.stage, "Are you sure you want to reset game config?");
               if(resetAns)
               {
                  // Reset config
                  XMLSettings xmlWorker = new XMLSettings(CONFIG_FILE);
                  xmlWorker.writeXML();
                  
                  DisplayMessage.showAlert(stage, AlertType.INFORMATION, "Configuration saved to the XML file", xmlWorker.readXML());
               }
               break;
            case "Prev. color": // Select prev. car color
               if(carArrayIndex > 0)
               {
                  carArrayIndex--;
                  tfColorSelect.setText(carNameArray[carArrayIndex]);
               }
               break;
            case "Next color": // Select next car color
               if(carArrayIndex < carFileArray.length - 1)
               {
                  carArrayIndex++;
                  tfColorSelect.setText(carNameArray[carArrayIndex]);
               }
               break;
            case "Exit": // Exit the game
               boolean answer = DisplayMessage.showConfirmation(this.stage, "Are you sure you want to exit the application?");
               if(answer)
               {
                  try
                  {
                     if(oos != null)    oos.close();
                     if(ois != null)    ois.close();
                     if(socket != null) socket.close();
                  }
                  catch(IOException ioe)
                  {
                     System.out.println(ioe);
                     System.exit(1);
                  }
                  System.exit(0);
               }
               break;
            case "Send": // Send public chat message
               String text = tfChatEnter.getText();
               tfChatEnter.clear();
                              
               if(text.length() > 0)
               {
                  taPublicChat.appendText("\nYou: " + text);
                  sendChatMessageToServer(text);
               }
               break;
            case "GLHF": // GLHF Rection
               taPublicChat.appendText("\nYou: GLHF!");
               sendChatMessageToServer("GLHF!");
               ButtonsSleep bsGLHF = new ButtonsSleep(btnGLHF, BUTTON_SLEEP);
               bsGLHF.start();
               break;
            case "GG": // GG Reaction
               taPublicChat.appendText("\nYou: GG");
               sendChatMessageToServer("GG");
               ButtonsSleep bsGG = new ButtonsSleep(btnGG, BUTTON_SLEEP);
               bsGG.start();
               break;
            case "WP": // WP Reaction
               taPublicChat.appendText("\nYou: Well Played!");
               sendChatMessageToServer("Well Played!");
               ButtonsSleep bsWP = new ButtonsSleep(btnWP, BUTTON_SLEEP);
               bsWP.start();
               break;
            // Private chat selectors
            case "Player 1":
               showPrivateChat(1);
               break;
            case "Player 2":
               showPrivateChat(2);
               break;
            case "Player 3":
               showPrivateChat(3);
               break;
            case "Player 4":
               showPrivateChat(4);
               break;
            // END Private chat selectors
            case "Send Private": // Send private chat message
               String privateText = tfPrivateChatEnter.getText();
               tfPrivateChatEnter.clear();
                              
               sendPrivateMessage(privateText);
               break;
         }
      }
   } // END handler()
   
   /** Fetches all of the necessary images and sets their proportions*/
   public void initImages()
   {
      // Initialize images
      try
      {
         imgTrack = new Image(new FileInputStream(new File(TRACK_FILE_NAME)));
      }
      catch(FileNotFoundException fnfe)
      {
         System.out.println(fnfe);
      }
      
      // Track image view
      imgViewTrack = new ImageView(imgTrack);
      
      // Image fit
      imgViewTrack.setFitWidth(TRACK_WIDTH);
      imgViewTrack.setFitHeight(TRACK_HEIGHT);
   } // END initImages()
   
   /** Game start animation*/
   class WaitBeforeStart extends Thread
   {
      public void run()
      {
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  tCountOne.setVisible(false);
                  tCountTwo.setVisible(false);
                  tCountThree.setVisible(true);
               }
            }
         );
         try
         {
            Thread.sleep(1000);
         }
         catch(InterruptedException ie)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "WaitBeforeStart", ie + "");
         }
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  tCountOne.setVisible(false);
                  tCountTwo.setVisible(true);
                  tCountThree.setVisible(false);
               }
            }
         );
         try
         {
            Thread.sleep(1000);
         }
         catch(InterruptedException ie)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "WaitBeforeStart", ie + "");
         }
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  tCountOne.setVisible(true);
                  tCountTwo.setVisible(false);
                  tCountThree.setVisible(false);
               }
            }
         );
         try
         {
            Thread.sleep(1000);
         }
         catch(InterruptedException ie)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "WaitBeforeStart", ie + "");
         }
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  tCountOne.setVisible(false);
                  tCountTwo.setVisible(false);
                  tCountThree.setVisible(false);
                  tStart.setVisible(true);
                  gameStart = true;
               }
            }
         );
         try
         {
            Thread.sleep(500);
         }
         catch(InterruptedException ie)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "WaitBeforeStart", ie + "");
         }
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  tStart.setVisible(false);
               }
            }
         );
      }
   } // END WaitBeforeStart
   
   /** Disables a button for a set period of time*/
   class ButtonsSleep extends Thread
   {
      // Attributes
      /** Button to put to disable*/
      private Button btn;
      /** Amount of time the button will stay disabled for*/
      double time;
      
      /**
      * Constructor
      *
      * @param _btn button to disable
      * @param _time amount of time the button will stay disabled for in milliseconds
      */
      public ButtonsSleep(Button _btn, double _time)
      {
         this.btn = _btn;
         this.time = _time;
      }
      
      public void run()
      {
         // Disable all reaction buttons
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  for(Button btn:reactionButtons)
                  {
                     btn.setDisable(true);
                  }
               }
            }
         );
         
         // Wait
         try
         {
            Thread.sleep((long)time);
         }
         catch(InterruptedException ie)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "ButtonSleep", ie + "");
         }
         
         // Enable all reaction buttons
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  for(Button btn:reactionButtons)
                  {
                     btn.setDisable(false);
                  }
               }
            }
         );
      }
   } // END ButtonSleep
   
   /** 
   * Shows a private chat based on the opponent ID
   *
   * @param _chatId id of the opponent
   */
   public void showPrivateChat(int _chatId)
   {
      showPrivateChat(_chatId, "", false);
   }
   
   /** 
   * Shows a private chat based on the opponent ID and displays a private chat message in it
   *
   * @param _chatId id of the opponent
   * @param _message message to display in the private chat
   */
   public void showPrivateChat(int _chatId, String _message)
   {
      showPrivateChat(_chatId, _message, false);
   }
   
   /** 
   * Shows a private chat based on the opponent ID and displays a private chat message in it.
   * Has a boolean for sending server messages in private chats
   *
   * @param _chatId id of the opponent
   * @param _message message to display in the private chat
   * @param _serverMessage true = server message; false = not a server message;
   */
   public void showPrivateChat(int _chatId, String _message, boolean _serverMessage)
   {
      // Find the opponents in the list
      Opponent publicChatOp = null;
      for(Opponent op:opponents)
      {
         if(op.getClientNumber() == _chatId)
         {
            publicChatOp = op;
         }
      }
      
      // Format the chat message
      String chatMessage = String.format("\n%s#%d: %s", publicChatOp.getClientName(), publicChatOp.getClientNumber(), _message);
      
      // Show private chat and display a private message in it
      switch(_chatId)
      {
         case 1:
            
            if(_message.length() > 0 && !_serverMessage) // Player to player message
            {
               taP1Chat.appendText(chatMessage);
            }
            else if(_serverMessage) // Server to player message
            {
               taP1Chat.appendText("\nServer Message: " + _message);
            }
            
            // Set chat ID number
            privateChatClientNumber = 1;
            
            // Make chat log visible
            taP1Chat.setVisible(true);
            taP2Chat.setVisible(false);
            taP3Chat.setVisible(false);
            taP4Chat.setVisible(false);
            
            // Change private chat button styles to display the selected chat
            btnP1Chat.setStyle("-fx-underline: true; -fx-font-weight: bold");
            btnP2Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            btnP3Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            btnP4Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            break;
         case 2:
         
            if(_message.length() > 0 && !_serverMessage)
            {
               taP2Chat.appendText(chatMessage);
            }
            else if(_serverMessage)
            {
               taP2Chat.appendText("\nServer Message: " + _message);
            }

            privateChatClientNumber = 2;
            
            taP1Chat.setVisible(false);
            taP2Chat.setVisible(true);
            taP3Chat.setVisible(false);
            taP4Chat.setVisible(false);
            
            btnP1Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            btnP2Chat.setStyle("-fx-underline: true; -fx-font-weight: bold");
            btnP3Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            btnP4Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            break;
         case 3:
            
            if(_message.length() > 0 && !_serverMessage)
            {
               taP3Chat.appendText(chatMessage);
            }
            else if(_serverMessage)
            {
               taP3Chat.appendText("\nServer Message: " + _message);
            }
            
            privateChatClientNumber = 3;
            
            taP1Chat.setVisible(false);
            taP2Chat.setVisible(false);
            taP3Chat.setVisible(true);
            taP4Chat.setVisible(false);
            
            btnP1Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            btnP2Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            btnP3Chat.setStyle("-fx-underline: true; -fx-font-weight: bold");
            btnP4Chat.setStyle("-fx-underline: false; -fx-font-weight: normal");
            break;
         case 4:
         
            if(_message.length() > 0 && !_serverMessage)
            {
               taP4Chat.appendText(chatMessage);
            }
            else if(_serverMessage)
            {
               taP4Chat.appendText("\nServer Message: " + _message);
            }
            
            privateChatClientNumber = 4;
            
            taP1Chat.setVisible(false);
            taP2Chat.setVisible(false);
            taP3Chat.setVisible(false);
            taP4Chat.setVisible(true);
            
            btnP1Chat.setStyle("-fx-underline: false");
            btnP2Chat.setStyle("-fx-underline: false");
            btnP3Chat.setStyle("-fx-underline: false");
            btnP4Chat.setStyle("-fx-underline: true; -fx-font-weight: bold");
            break;
      }
   } // END showPrivateChat()
   
   /** 
   * Sends a private chat message based on the selected private chat id
   *
   * @param _privateText chat message
   */
   public void sendPrivateMessage(String _privateText)
   {
      if(_privateText.length() > 0)
      {
         switch(privateChatClientNumber)
         {
            case 1:
               taP1Chat.appendText("\nYou: " + _privateText);
               break;
            case 2:
               taP2Chat.appendText("\nYou: " + _privateText);
               break;
            case 3:
               taP3Chat.appendText("\nYou: " + _privateText);
               break;
            case 4:
               taP4Chat.appendText("\nYou: " + _privateText);
               break;
         }
         
         sendPrivateChatMessageToServer(privateChatClientNumber, _privateText);
      }
   } // END sendPrivateMessage()
   
   /**
   * Sends a private chat message that displays as a server message
   *
   * @param _chatId id of the sender (-1 for the server)
   * @param _message server message
   */
   public void sendPrivateServerMessage(int _chatId, String _message)
   {
      showPrivateChat(_chatId, _message, true);
   } // END sendPrivateServerMessage()
   
   /** 
   * Sends a public chat message to the server
   *
   * @param _message public chat message to send to the server
   */
   public void sendChatMessageToServer(String _message)
   {
      // Request
      try
      {
         synchronized(oosLock)
         {
            oos.writeObject("CHAT_MESSAGE");
            oos.writeObject(playerNumber);
            oos.writeObject(_message);  
         }
      }
      catch(IOException ioe)
      {
         DisplayMessage.showAlert(stage, AlertType.ERROR, "Error sending coordinates to the server", ioe + "");
      }
   } // END sendChatMessageToServer()
   
   /** 
   * Sends a private chat message to the server
   *
   * @param _recipientNumber recipient client ID
   * @param _message private chat message to send to the server
   */
   public void sendPrivateChatMessageToServer(int _recipientNumber, String _message)
   {
      // Request
      try
      {
         synchronized(oosLock)
         {
            oos.writeObject("PRIVATE_CHAT_MESSAGE");
            oos.writeObject(playerNumber);
            oos.writeObject(_recipientNumber);
            oos.writeObject(_message);  
         }
      }
      catch(IOException ioe)
      {
         DisplayMessage.showAlert(stage, AlertType.ERROR, "Error sending coordinates to the server", ioe + "");
      }
   } // END sendPrivateChatMessageToServer()
   
   /**
   * Sends client's car coordinates to the server
   *
   * @param x X coordinate of the car
   * @param y Y coordinate of the car
   * @param degree rotation of the car in degrees
   */
   public void sendCoordinatesToServer(double x, double y, double degree)
   {
      if(!error)
      {
         CoordinateSet cs = new CoordinateSet(playerNumber, x, y, degree);
         // Request
         try
         {
            if(oos != null)
            {
               synchronized(oosLock)
               {
                  oos.writeObject("UPDATE_COORDINATES");
                  oos.writeObject(cs);  
               }
            }
         }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error sending coordinates to the server", ioe + "");
            error = true;
         }
      }
   } // END sendCoordinatesToServer()
   
   /** A class responsible for the client server communication*/
   class Client extends Thread
   {
      // Synchronization
      /** Local client synchronization object*/
      private Object lock = new Object();
      
      public void run()
      {
         boolean loggedIn = false;
         try
         {
            // Open socket for server connection
            socket = new Socket(tfServerIp.getText(), serverPort);
            
            // Input/output with the server																
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
         
            oos.writeObject(tfServerPassword.getText());
            String login = (String)ois.readObject();
            
            if(login.equals("LOGGED_IN"))
            {
               loggedIn = true;
               TitleScreen.showConnectionMessage();
            }
            else 
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "Error connecting to the server", "Wrong Password");
               btnStart.setDisable(false);
               TitleScreen.disableInterface(false);
            }
         }
         catch(ClassNotFoundException cnfe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error connecting to the server", cnfe + "");
         }
         catch(UnknownHostException uhe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error connecting to the server", uhe + "");
            btnStart.setDisable(false);
            TitleScreen.disableInterface(false);
         }
         catch(ConnectException ce)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error connecting to the server", ce + "");
            btnStart.setDisable(false);
            TitleScreen.disableInterface(false);
         }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error connecting to the server", ioe + "");
            btnStart.setDisable(false);
            TitleScreen.disableInterface(false);
         }
         
         // Start listening to server inputs
         while(!error && loggedIn)
         {
            Object input = null;
            
            // Wait for a command
            try
            {
               input = ois.readObject();
            }
            catch(SocketException se)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "Server went down", "You were disconnected");
               btnStart.setDisable(false);
               for(Player p:opponentPlayers)
               {
                  p.hideCar();
               }
               taPublicChat.appendText("\n\nYou were disconnected from the server\n\n");
               error = true;
            }
            catch(ClassNotFoundException cnfe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: Error receiving command", cnfe + "");
               error = true;
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: Error receiving command", ioe + "");
               error = true;
            }
            
            if(input instanceof String)
            {
               String command = (String)input;
               
               switch(command)
               {
                  case "INIT_PLAYER":
                     try
                     {
                        // Player initialization
                           // RECEIVE player number
                        playerNumber = (Integer)ois.readObject();
                           // RECEIVE number of laps in the race
                        numOfLaps = (Integer)ois.readObject();
                        Platform.runLater
                        (
                           new Runnable()
                           {
                              public void run()
                              {
                                 tLaps.setText("Lap: " + currentLap + "/" + numOfLaps);
                              }
                           }
                        );
                           // SEND car file name
                        oos.writeObject(carFileArray[carArrayIndex]);
                           // SEND nickname
                        oos.writeObject(tfClientName.getText());
                           // RECEIVE starting coordintaes
                        mainStartX = (double)ois.readObject();
                        mainStartY = (double)ois.readObject();
                        mainStartDegree = (double)ois.readObject();
                     
                        // Initialize check points
                        CheckPoint[] cpArray = (CheckPoint[])ois.readObject();
                        
                        for(CheckPoint cp:cpArray)
                        {
                           double x = GameLogic.getLineX(cp);
                           double y = GameLogic.getLineY(cp);
                           checkPoints.add(new Line(0, 0, x, y));
                           
                           checkPointCoordinatesX.add(Math.min(cp.getX1(), cp.getX2()));
                           checkPointCoordinatesY.add(Math.min(cp.getY1(), cp.getY2()));
                        }
                     }
                     catch(ClassNotFoundException cnfe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "INIT_PLAYER CNFE", cnfe + "");
                        error = true;
                     }
                     catch(IOException ioe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "INIT_PLAYER IOE", ioe + "");
                        error = true;
                     }
                     break;
                  case "INIT_OPPONENT": // Initialize opponent player
                     try
                     {
                        Opponent op = (Opponent)ois.readObject();
                        synchronized(opponentsLock)
                        {
                           opponents.add(op);
                        }
                     }
                     catch(ClassNotFoundException cnfe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "INIT_OPPONENT CNFE", cnfe + "");
                        error = true;
                     }
                     catch(IOException ioe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "INIT_OPPONENT IOE", ioe + "");
                        error = true;
                     }
                     break; // INIT_OPPONENT
                  case "START_GAME": // Start game    
                     mainPlayer = new Player(gameClient, stage, playerNumber, carFileArray[carArrayIndex], mainStartX, mainStartY, mainStartDegree, TRACK_WIDTH, TRACK_HEIGHT);

                     createGameScene();
                     // Create opponents as players
                     synchronized(opponentsLock)
                     {
                        for(Opponent op:opponents)
                        {
                           Player plOp = new Player(gameClient, stage, op.getClientNumber(), op.getCarFileName(), op.getStartX(), op.getStartY(), op.getStartDegree(), TRACK_WIDTH, TRACK_HEIGHT);
                           synchronized(opponentPlayersLock)
                           {
                              opponentPlayers.add(plOp);
                           }
                        }
                     }
                     
                     synchronized(opponentPlayersLock)
                     {
                        for(Player p:opponentPlayers)
                        {
                           track.getChildren().add(p);
                        }
                     }
                     track.getChildren().add(mainPlayer);
                     
                     // Show the game scene
                     Platform.runLater
                     (
                        new Runnable()
                        {
                           public void run()
                           {
                              stage.setScene(gameScene);
                              track.requestFocus();
                              stage.show();
                           }
                        }
                     );
                     WaitBeforeStart wbs = new WaitBeforeStart();
                     wbs.start();
                     break; // START_GAME
                  case "UPDATE_OPPONENT": // Update opponent's car location
                     synchronized(lock)
                     {
                        CoordinateSet cs = null;
                        try
                        {
                           cs = (CoordinateSet)ois.readObject();
                        }
                        catch(ClassNotFoundException cnfe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "UPDATE_COORDINATES CNFE", cnfe + "");
                           error = true;
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "UPDATE_COORDINATES IOE", ioe + "");
                           error = true;
                        }
                        
                        synchronized(opponentPlayersLock)
                        {
                           for(Player p:opponentPlayers)
                           {
                              if(p.getPlayerNumber() == cs.getClientNumber() && cs != null)
                              {
                                 p.setCoordinates(cs.getX(), cs.getY(), cs.getDegree());
                              }
                           }
                        }
                     }
                     break; // UPDATE_OPPONENT
                  case "RECEIVE_MESSAGE": // Receive a public chat message
                     int senderId = 0;
                     String message = "";
                     synchronized(lock)
                     {   
                        try
                        {
                           senderId = (Integer)ois.readObject();
                           message = (String)ois.readObject();
                        }
                        catch(ClassNotFoundException cnfe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "RECEIVE_MESSAGE CNFE", cnfe + "");
                           error = true;
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "RECEIVE_MESSAGE IOE", ioe + "");
                           error = true;
                        }
                     }
                     
                     if(senderId == -1)
                     {
                        String chatMessage = String.format("\nServer Message: " + message + "\n");
                        taPublicChat.appendText("\n" + chatMessage);
                     }
                     else
                     {
                        Opponent publicChatOp = null;
                        for(Opponent op:opponents)
                        {
                           if(op.getClientNumber() == senderId)
                           {
                              publicChatOp = op;
                           }
                        }
                        
                        String chatMessage = String.format("%s#%d: %s", publicChatOp.getClientName(), publicChatOp.getClientNumber(), message);
                        
                        taPublicChat.appendText("\n" + chatMessage);
                     }
                     break; // RECEIVE_MESSAGE
                  case "RECEIVE_PRIVATE_MESSAGE": // Receive a private chat message
                     int privateSenderId = 0;
                     String privateMessage = "";
                     synchronized(lock)
                     {   
                        try
                        {
                           privateSenderId = (Integer)ois.readObject();
                           privateMessage = (String)ois.readObject();
                        }
                        catch(ClassNotFoundException cnfe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "RECEIVE_MESSAGE CNFE", cnfe + "");
                           error = true;
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "RECEIVE_MESSAGE IOE", ioe + "");
                           error = true;
                        }
                     }
                     
                     showPrivateChat(privateSenderId, privateMessage);
                     break; // RECEIVE_PRIVATE_MESSAGE
                  case "DISCONNECT_OPPONENT": // Remove an opponent from the game
                     int opponentToDisconnect = -1;
                     synchronized(lock)
                     {
                        try
                        {  // Get opponent's ID
                           opponentToDisconnect = (Integer)ois.readObject();
                        }
                        catch(ClassNotFoundException cnfe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "RECEIVE_MESSAGE CNFE", cnfe + "");
                           error = true;
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "RECEIVE_MESSAGE IOE", ioe + "");
                           error = true;
                        }
                     }
                     
                     if(opponentToDisconnect != -1)
                     {
                        Opponent disconnectedOp = null;
                        
                        synchronized(opponentsLock)
                        {
                           for(Opponent op:opponents)
                           {  // Get the Opponent object
                              if(op.getClientNumber() == opponentToDisconnect)
                              {
                                 disconnectedOp = op;
                                 break;
                              }
                           }
                        }
                        
                        // Display opponent disconnection message
                        String serverMessage = disconnectedOp.getClientName() + "#" + disconnectedOp.getClientNumber() + " disconnected.";
                        taPublicChat.appendText("\nServer Message: " + serverMessage + "\n");
                        sendPrivateServerMessage(opponentToDisconnect, serverMessage);
                        
                        synchronized(opponentsLock)
                        {
                           for(Opponent op:opponents)
                           {  // Remove Opponent object
                              if(op.getClientNumber() == opponentToDisconnect)
                              {
                                 disconnectedOp = op;
                                 opponents.remove(op);
                                 break;
                              }
                           }
                        }
                        
                        synchronized(opponentPlayersLock)
                        {
                           for(Player p:opponentPlayers)
                           {  // Hide the removed opponent's car and remove their Player object
                              if(p.getPlayerNumber() == opponentToDisconnect)
                              {
                                 p.hideCar();
                                 opponentPlayers.remove(p);
                                 break;
                              }
                           }
                        }
                     } // if opponent to disconnect exists
                     break; // DISCONNECT_OPPONENT
                  case "UPDATE_CHECKPOINT": // Update the active check point number
                     if(currentCheckPoint - 1 >= 0) checkPoints.get(currentCheckPoint - 1).setVisible(false);
                     checkPoints.get(currentCheckPoint).setStyle("-fx-stroke: green;");
                     currentCheckPoint++;
                     checkPoints.get(currentCheckPoint).setStyle("-fx-stroke: red;");
                     checkPoints.get(currentCheckPoint).setVisible(true);
                     break; // UPDATE_CHECKPOINT
                  case "UPDATE_LAP": // Update lap number
                     // Remove the line before the finish line
                     if(currentCheckPoint - 1 >= 0) checkPoints.get(currentCheckPoint - 1).setVisible(false);
                     
                     // Remove the finish line
                     checkPoints.get(checkPoints.size() - 1).setVisible(false);
                     
                     // Make all other checkpoints black again
                     for(Line l:checkPoints)
                     {
                        l.setStyle("-fx-stroke: black;");
                     }
                     
                     // Make the first check point green and visible
                     currentCheckPoint = 0;
                     checkPoints.get(currentCheckPoint).setStyle("-fx-stroke: red;");
                     checkPoints.get(currentCheckPoint).setVisible(true);
                     
                     // Increment lap counter
                     currentLap++;
                     tLaps.setText("Lap: " + currentLap + "/" + numOfLaps);
                     break;
                  case "STOP_GAME": // Stop game when someone wins
                     int winnerNumber = -1;
                     synchronized(lock)
                     {
                        try
                        {
                           winnerNumber = (Integer)ois.readObject();
                        }
                        catch(ClassNotFoundException cnfe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "STOP_GAME CNFE", cnfe + "");
                           error = true;
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "STOP_GAME IOE", ioe + "");
                           error = true;
                        }
                     }
                     if(winnerNumber == playerNumber)
                     {  // If client won
                        tLaps.setText("Finish!");
                        tFinish.setText("Congratulations\nYou won! (*^ W ^*)");
                        tFinish.setVisible(true);
                        checkPoints.get(checkPoints.size() - 1).setStyle("-fx-stroke: green;");
                     }
                     else
                     {  // If client lost
                        tLaps.setText("Finish!");
                        tFinish.setText("You lost ( ; _ ;)");
                        tFinish.setVisible(true);
                     }
                     break;
                     
               } // END switch(command)
            } // END if instanceof String
         } // END while(true)
      } // END run()
   } // END Client
   
   /**
   * Main method
   *
   * @param args game client run parameters
   */
   public static void main(String[] args) {
        launch(args);
    }
}