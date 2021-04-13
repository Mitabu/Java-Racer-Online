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
*  @version 02.04.2021
*/

public class GameClient extends Application implements EventHandler<ActionEvent>
{
   // Attributes
   // Stage
   private Stage stage;
   
   // Window proportions
      // Track (16/9)
   public static final double TRACK_WIDTH = 1550;
   public static final double TRACK_HEIGHT = 872;
      // Chat
   public static final double CHAT_WIDTH = 250;
   public static final double CHAT_HEIGHT = 500;
      // Window
   public static final double WINDOW_WIDTH = TRACK_WIDTH + CHAT_WIDTH;
   public static final double WINDOW_HEIGHT = TRACK_HEIGHT;
   
   // Layout
      // SCENES
            // Title Screen
   private Scene titleScreenScene;
   private TextField tfServerIp = new TextField();
   private TextField tfServerPassword = new TextField();
   private TextField tfClientName = new TextField();
   private Button btnStart = new Button("Start");
            // Options
   //private Scene optionsScene;
   //private Options optionsObject;
   private TextField tfColorSelect = new TextField();
            // Game
   private Scene gameScene;
      // GAME
         // ROOT
   private GridPane root;
         // Racing track
   private StackPane track;
   private Image imgTrack;
   private ImageView imgViewTrack;
         // TEMP
   private Button btnBack = new Button("Back to Title");
   
   // Images
   private static final String TRACK_FILE_NAME = "track.png";
   private String carFileName = "car_blue.png";
   
   // Networking
   private static final int SERVER_PORT = 42069;
   private Socket socket = null;
   
   private ObjectOutputStream oos = null;
   private ObjectInputStream ois = null;
   
   private GameClient gameClient = null;
   
      // Synchronization
   private Object oosLock = new Object();
   
   // Multiplayer
   private int playerNumber;
   private ArrayList<Opponent> opponents = new ArrayList<Opponent>();
   private Object opponentsLock = new Object();
   
   private ArrayList<Player> opponentPlayers = new ArrayList<Player>();
   private Object opponentPlayersLock = new Object();
   
   private double mainStartX = 0;
   private double mainStartY = 0;
   private double mainStartDegree = 0;
      
      // CheckPoints
   private ArrayList<Line> checkPoints = new ArrayList<Line>();
   private ArrayList<Double> checkPointCoordinatesX = new ArrayList<Double>();
   private ArrayList<Double> checkPointCoordinatesY = new ArrayList<Double>();
   
   private int currentCheckPoint = 0;
   private int currentLap = 1;
   private int numOfLaps = 0;
   
   private Text tLaps = new Text("Lap: " + currentLap + "/" + numOfLaps);
   
      // Game Start
   private Text tCountThree = new Text("3");
   private Text tCountTwo = new Text("2");
   private Text tCountOne = new Text("1");
   private Text tStart = new Text("Start!");
   private Text tFinish = new Text("Finish!");
   private boolean gameStart = false;
   
      // Chat
         // General
   private VBox rootChat = new VBox(5);
   private VBox rootPublicChat = new VBox(5);
   private VBox rootPrivateChat = new VBox(5);

         // Public Chat
   private static final double BUTTON_SLEEP = 2000;
   private TextArea taPublicChat = new TextArea();
   private TextField tfChatEnter = new TextField();
   
   private Button btnGLHF = new Button("GLHF");
   private Button btnGG = new Button("GG");
   private Button btnWP = new Button("WP");
   
   private Button[] reactionButtons = {btnGLHF, btnGG, btnWP};
   
         // Private Chat
   private TextArea taP1Chat = new TextArea();
   private TextArea taP2Chat = new TextArea();
   private TextArea taP3Chat = new TextArea();
   private TextArea taP4Chat = new TextArea();
   
   private String[] playerNames = {"Player 1", "Player 2", "Player 3", "Player 4"};
   
   private Button btnP1Chat = new Button(playerNames[0]);
   private Button btnP2Chat = new Button(playerNames[1]);
   private Button btnP3Chat = new Button(playerNames[2]);
   private Button btnP4Chat = new Button(playerNames[3]);
   
   private String btnP1Text = null;
   private String btnP2Text = null;
   private String btnP3Text = null;
   private String btnP4Text = null;
   
   private int privateChatClientNumber = 1;
   
   private TextField tfPrivateChatEnter = new TextField();
   private Button btnPrivateChatEnter = new Button("Send Private");
   
   // Animation Timer
   private long lastUpdate = 0;
   
   // GAME
   private boolean error = false;
   
   /** Boolean that determines the input from the keyboard*/
   private boolean gas, brake, turnLeft, turnRight;
   
   /** Main player*/
   private Player mainPlayer = null;
   
   /** Array of car file names*/
   private String[] carFileArray = {"car_blue.png", "car_orange.png", "car_purple.png", "car_red.png"};
   private String[] carNameArray = {"Blue",         "Orange",         "Purple",         "Red"};
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
            System.exit(0);
         }
      });
      
      // TRACK
      track = new StackPane(); 
      
      // GameClient
      gameClient = this;
      
      // CountDown
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
      
      // Track stack pane
      track.getChildren().addAll(imgViewTrack, spCountDown);
      
      // SET SCENES
      titleScreenScene = TitleScreen.getScene(this, (int)WINDOW_WIDTH, (int)WINDOW_HEIGHT, tfServerIp, tfServerPassword, tfClientName, btnStart, tfColorSelect, carNameArray[carArrayIndex]);
      //optionsObject = new Options(this, (int)WINDOW_WIDTH, (int)WINDOW_HEIGHT, tfColorSelect, carNameArray[carArrayIndex]);
      //optionsScene = optionsObject.getScene();
      
      stage.setScene(titleScreenScene);
      stage.setResizable(false);
      stage.centerOnScreen();
      stage.show();
   }
   
   public void createGameScene()
   {
      // Layout
         // ROOT
      root = new GridPane();
            
      //track.getChildren().add(mainPlayer);
      
      // Root
      TextArea taLog = new TextArea();
      taLog.setPrefColumnCount(5);
      
      // CHAT
/////////// PUBLIC CHAT
         FlowPane fpPublicChat = new FlowPane(5,5);
            Text txtPublicChat = new Text("Public Chat");
            txtPublicChat.setStyle("-fx-font-size: 25px; -fx-font-weight: bold");
            fpPublicChat.setAlignment(Pos.CENTER);
            fpPublicChat.getChildren().add(txtPublicChat);
            
            taPublicChat.setPrefWidth(250);
            taPublicChat.setPrefHeight(400);
            taPublicChat.setEditable(false);
            taPublicChat.setWrapText(true);
            taPublicChat.setText("Public Chat Room\n");
            
         FlowPane fpChatPreset = new FlowPane(5,5);
            fpChatPreset.setAlignment(Pos.CENTER);
            fpChatPreset.getChildren().addAll(btnGLHF, btnGG, btnWP);
            
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

/////////// PRIVATE CHAT
      // Sign
      FlowPane fpPrivateChat = new FlowPane(5,5);
         Text txtPrivateChat = new Text("Private Chat");
         txtPrivateChat.setStyle("-fx-font-size: 25px; -fx-font-weight: bold");
         fpPrivateChat.setAlignment(Pos.CENTER);
      fpPrivateChat.getChildren().add(txtPrivateChat);
      
      // Buttons
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
      
      // Logs
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
      
      // Send private
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
      
      // CHAT setOnAction()
      btnChatEnter.setOnAction(this);
      btnGLHF.setOnAction(this);
      btnGG.setOnAction(this);
      btnWP.setOnAction(this);
      
      btnPrivateChatEnter.setOnAction(this);
      btnP1Chat.setOnAction(this);
      btnP2Chat.setOnAction(this);
      btnP3Chat.setOnAction(this);
      btnP4Chat.setOnAction(this);
      
      // Track UI
      FlowPane fpLaps = new FlowPane();
         fpLaps.setAlignment(Pos.TOP_LEFT);
         tLaps.setStyle("-fx-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 1;");
         fpLaps.getChildren().add(tLaps);
      track.getChildren().add(fpLaps);
      
      // Track Check Points
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
      
      root.add(track, 0, 0);
      root.add(rootChat, 1, 0);
      
      // setOnAction
      btnBack.setOnAction(this);
      
      // Scene
      gameScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
      
      // Key Listner
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
                  
                  // Pass user commands to the Player
                  //System.out.println("Turn: " + turn + " Velocity: " + velocity);
                  
                  //for(Player p:opponentPlayers)
                  //{
                  //   if(mainPlayer.getBody().getBoundsInParent().intersects(p.getBody().getBoundsInParent()))
                  //   {
                  //      System.out.println("Intersection with Player " + p.getPlayerNumber());
                  //      //velocity = -10;
                  //   }
                  //}
                  
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
      Object source = ae.getSource();
      
      if(source instanceof Button)
      {
         String command = ((Button)source).getText();
         
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
         
         switch(command)
         {
            case "Start":
               if(tfServerIp.getText().length() > 0 &&
                  tfServerIp.getText().length() <= 20 &&
                  tfServerPassword.getText().length() <= 20 &&
                  tfClientName.getText().length() > 0 &&
                  tfClientName.getText().length() <= 10)
               {
                  Client c = new Client();
                  c.start();
                  btnStart.setDisable(true);
               }
               else
               {
                  DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting the game", "One or multiple fields are missing information and/or have too many symbols");
               }
               break;
            //case "Options":
            //   stage.setScene(optionsScene);
            //   stage.show();
            //   break;
            case "Prev. color":
               if(carArrayIndex > 0)
               {
                  carArrayIndex--;
                  tfColorSelect.setText(carNameArray[carArrayIndex]);
               }
               break;
            case "Next color":
               if(carArrayIndex < carFileArray.length - 1)
               {
                  carArrayIndex++;
                  tfColorSelect.setText(carNameArray[carArrayIndex]);
               }
               break;
            case "Exit":
               boolean answer = DisplayMessage.showConfirmation(this.stage, "Are you sure you want to exit the application?");
               if(answer) System.exit(0);
               break;
            case "Back to Title":
               stage.setScene(titleScreenScene);
               stage.show();
               break;
            case "Send":
               String text = tfChatEnter.getText();
               tfChatEnter.clear();
                              
               if(text.length() > 0)
               {
                  taPublicChat.appendText("\nYou: " + text);
                  sendChatMessageToServer(text);
               }
               break;
            case "GLHF":
               taPublicChat.appendText("\nYou: GLHF!");
               sendChatMessageToServer("GLHF!");
               ButtonsSleep bsGLHF = new ButtonsSleep(btnGLHF, BUTTON_SLEEP);
               bsGLHF.start();
               break;
            case "GG":
               taPublicChat.appendText("\nYou: GG");
               sendChatMessageToServer("GG");
               ButtonsSleep bsGG = new ButtonsSleep(btnGG, BUTTON_SLEEP);
               bsGG.start();
               break;
            case "WP":
               taPublicChat.appendText("\nYou: Well Played!");
               sendChatMessageToServer("Well Played!");
               ButtonsSleep bsWP = new ButtonsSleep(btnWP, BUTTON_SLEEP);
               bsWP.start();
               break;
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
            case "Send Private":
               String privateText = tfPrivateChatEnter.getText();
               tfPrivateChatEnter.clear();
                              
               sendPrivateMessage(privateText);
               break;
            case "TEST":
               Line cp1 = new Line(0, 0, 0, 145);
               cp1.setStyle("-fx-stroke: red;");
               mainPlayer = new Player(gameClient, stage, playerNumber, carFileArray[carArrayIndex], 150, 550, 180, TRACK_WIDTH, TRACK_HEIGHT);
               Pane checkPane = new Pane();
               checkPane.getChildren().add(cp1);
               checkPane.setTranslateX(220);
               checkPane.setTranslateY(680);
               System.out.println("X: " + checkPane.getTranslateX() + "  Y: " + checkPane.getTranslateY());
               
               createGameScene();
               track.getChildren().addAll(checkPane, mainPlayer);

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
               break;
         }
      }
   }
   
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
   }
   
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
   }
   
   class ButtonsSleep extends Thread
   {
      // Attributes
      Button btn;
      double time;
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
   }
   
   public void showPrivateChat(int _chatId)
   {
      showPrivateChat(_chatId, "", false);
   }
   
   public void showPrivateChat(int _chatId, String _message)
   {
      showPrivateChat(_chatId, _message, false);
   }
   
   public void sendPrivateServerMessage(int _chatId, String _message)
   {
      showPrivateChat(_chatId, _message, true);
   }
   
   public void showPrivateChat(int _chatId, String _message, boolean _serverMessage)
   {
      Opponent publicChatOp = null;
      for(Opponent op:opponents)
      {
         if(op.getClientNumber() == _chatId)
         {
            publicChatOp = op;
         }
      }
      
      String chatMessage = String.format("\n%s#%d: %s", publicChatOp.getClientName(), publicChatOp.getClientNumber(), _message);
      
      switch(_chatId)
      {
         case 1:
            
            if(_message.length() > 0 && !_serverMessage)
            {
               taP1Chat.appendText(chatMessage);
            }
            else if(_serverMessage)
            {
               taP1Chat.appendText("\nServer Message: " + _message);
            }
            
            privateChatClientNumber = 1;
            
            taP1Chat.setVisible(true);
            taP2Chat.setVisible(false);
            taP3Chat.setVisible(false);
            taP4Chat.setVisible(false);
            
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
   }
   
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
   }
   
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
   }
   
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
   }
   
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
   }
   
   class Client extends Thread
   {
      // Synchronization
      private Object lock = new Object();
      
      public void run()
      {
         boolean loggedIn = false;
         try
         {
            // Open socket for server connection
            socket = new Socket(tfServerIp.getText(), SERVER_PORT);
            
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
         }
         catch(ConnectException ce)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error connecting to the server", ce + "");
            btnStart.setDisable(false);
         }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error connecting to the server", ioe + "");
            btnStart.setDisable(false);
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
                        //System.out.println("sX: " + mainStartX + "   sY: " + mainStartY);
                     
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
                  case "INIT_OPPONENT":
                     try
                     {
                        Opponent op = (Opponent)ois.readObject();
                        synchronized(opponentsLock)
                        {
                           opponents.add(op);
                        }
                        //DisplayMessage.showAlert(stage, AlertType.INFORMATION, "INIT_OPPONENT: Opponent Info received", op + "");
                        //System.out.println("Player" + playerNumber + " INIT_OPPONENT: " + op);
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
                  case "START_GAME":
                     //System.out.println("\nSTART_GAME RECEIVED\n");
                     
                     // Create the main player
                     // PlayerNumber, NameOfCarFile, StartPosX, StartPosY, StartRotation(degrees)      
                     mainPlayer = new Player(gameClient, stage, playerNumber, carFileArray[carArrayIndex], mainStartX, mainStartY, mainStartDegree, TRACK_WIDTH, TRACK_HEIGHT);
                     //mainPlayer = new Player(this.stage, playerNumber, carFileArray[carArrayIndex], 0, 0, mainStartDegree, TRACK_WIDTH, TRACK_HEIGHT);
                     //System.out.println("sX: " + mainStartX + "   sY: " + mainStartY);

                     createGameScene();
                     // Create opponents as players
                     synchronized(opponentsLock)
                     {
                        for(Opponent op:opponents)
                        {
                           Player plOp = new Player(gameClient, stage, op.getClientNumber(), op.getCarFileName(), op.getStartX(), op.getStartY(), op.getStartDegree(), TRACK_WIDTH, TRACK_HEIGHT);
                           //Player plOp = new Player(stage, op.getClientNumber(), op.getCarFileName(), 0, 0, op.getStartDegree(), TRACK_WIDTH, TRACK_HEIGHT);
                           synchronized(opponentPlayersLock)
                           {
                              opponentPlayers.add(plOp);
                           }
                           //System.out.println(op);
                        }
                     }
                     
                     synchronized(opponentPlayersLock)
                     {
                        for(Player p:opponentPlayers)
                        {
                           track.getChildren().add(p);
                           //System.out.println(p);
                           //System.out.println(p.getCoordinates());
                        }
                     }
                     track.getChildren().add(mainPlayer);
                     //System.out.println(mainPlayer.getCoordinates());
                     
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
                  case "UPDATE_OPPONENT":
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
                                 //System.out.println(String.format("UC: X:%f  Y:%f  DEG:%f", cs.getX(), cs.getY(), cs.getDegree()));
                              }
                           }
                        }
                     }
                     break; // UPDATE_OPPONENT
                  case "RECEIVE_MESSAGE":
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
                  case "RECEIVE_PRIVATE_MESSAGE":
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
                  case "DISCONNECT_OPPONENT":
                     //System.out.println("DISCONNECT_OPPONENT");
                     int opponentToDisconnect = -1;
                     synchronized(lock)
                     {
                        try
                        {
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
                           {
                              if(op.getClientNumber() == opponentToDisconnect)
                              {
                                 disconnectedOp = op;
                                 break;
                              }
                           }
                        }
                        
                        String serverMessage = disconnectedOp.getClientName() + "#" + disconnectedOp.getClientNumber() + " disconnected.";
                        taPublicChat.appendText("\nServer Message: " + serverMessage + "\n");
                        sendPrivateServerMessage(opponentToDisconnect, serverMessage);
                        
                        synchronized(opponentsLock)
                        {
                           for(Opponent op:opponents)
                           {
                              if(op.getClientNumber() == opponentToDisconnect)
                              {
                                 disconnectedOp = op;
                                 opponents.remove(op);
                                 //System.out.println("Removed " + op.getClientName() + "#" + op.getClientNumber());
                                 break;
                              }
                           }
                        }
                        
                        synchronized(opponentPlayersLock)
                        {
                           for(Player p:opponentPlayers)
                           {
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
                  case "UPDATE_CHECKPOINT":
                     if(currentCheckPoint - 1 >= 0) checkPoints.get(currentCheckPoint - 1).setVisible(false);
                     checkPoints.get(currentCheckPoint).setStyle("-fx-stroke: green;");
                     currentCheckPoint++;
                     checkPoints.get(currentCheckPoint).setStyle("-fx-stroke: red;");
                     checkPoints.get(currentCheckPoint).setVisible(true);
                     break; // UPDATE_CHECKPOINT
                  case "UPDATE_LAP":
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
                  case "STOP_GAME":
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
                     {
                        //System.out.println("I WON!");
                        tLaps.setText("Finish!");
                        tFinish.setText("Congratulations\nYou won! (*^ W ^*)");
                        tFinish.setVisible(true);
                        checkPoints.get(checkPoints.size() - 1).setStyle("-fx-stroke: green;");
                     }
                     else
                     {
                        //System.out.println("I LOST :(");
                        tLaps.setText("Finish!");
                        tFinish.setText("You lost ( ; _ ;)");
                        tFinish.setVisible(true);
                     }
                     break;
                     
               } // switch(command)
            } // if instanceof String
         } // while(true)
      } // run()
   } // Client
   
   public static void main(String[] args) {
        launch(args);
    }
}