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
*  @version 31.03.2021
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
   private Button btnStart = new Button("Start");
            // Options
   private Scene optionsScene;
   private Options optionsObject;
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
   private ArrayList<Player> opponentPlayers = new ArrayList<Player>();
   
   private double mainStartX = 0;
   private double mainStartY = 0;
   private double mainStartDegree = 0;
   
      // Chat
         // General
   private VBox rootChat = new VBox(5);
   private VBox rootPublicChat = new VBox(5);
   private VBox rootPrivateChat = new VBox(5);

         // Public Chat
   private static final double BUTTON_SLEEP = 2000;
   private TextArea taChatBox = new TextArea();
   private TextField tfChatEnter = new TextField();
   private Button btnGLHF = null;
   private Button btnGG = null;
   
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
      
      // Image initialization
      initImages();
      
      // Track stack pane
      track.getChildren().add(imgViewTrack);
      
      // SET SCENES
      titleScreenScene = TitleScreen.getScene(this, (int)WINDOW_WIDTH, (int)WINDOW_HEIGHT, tfServerIp, btnStart);
      optionsObject = new Options(this, (int)WINDOW_WIDTH, (int)WINDOW_HEIGHT, tfColorSelect, carNameArray[carArrayIndex]);
      optionsScene = optionsObject.getScene();
      
      stage.setScene(titleScreenScene);
      stage.setResizable(false);
      stage.setX(20);
      stage.setY(20);
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
            
            taChatBox.setPrefWidth(250);
            taChatBox.setPrefHeight(400);
            taChatBox.setEditable(false);
            taChatBox.setText("Public Chat Room");
            
         FlowPane fpChatPreset = new FlowPane(5,5);
            fpChatPreset.setAlignment(Pos.CENTER);
            btnGLHF = new Button("GLHF");
            btnGG = new Button("GG");
            fpChatPreset.getChildren().addAll(btnGLHF, btnGG);
            
         FlowPane fpChatEnter = new FlowPane(5,5);
            fpChatEnter.setAlignment(Pos.CENTER);
            tfChatEnter.setPromptText("Message...");
            tfChatEnter.setPrefColumnCount(15);
            Button btnChatEnter = new Button("Send");
            fpChatEnter.getChildren().addAll(tfChatEnter, btnChatEnter);
      
      rootPublicChat.getChildren().addAll(/*fpMinimize,*/fpPublicChat, taChatBox, fpChatPreset, fpChatEnter);

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
         for(Opponent op:opponents)
         {
            int opponentId = op.getClientNumber();
            Button btnOpponent = null;
            
            switch(opponentId)
            {
               case 1:
                  btnOpponent = btnP1Chat;
                  break;
               case 2:
                  btnOpponent = btnP2Chat;
                  break;
               case 3:
                  btnOpponent = btnP3Chat;
                  break;
               case 4:
                  btnOpponent = btnP4Chat;
                  break;
            }
            
            if(btnOpponent != null)
            {
               fpPlayerSelect.getChildren().add(btnOpponent);
            }
         } // for(Opponent)
         showPrivateChat(opponents.get(0).getClientNumber());
      
      // Logs
      StackPane spPrivateChat = new StackPane();
         spPrivateChat.setAlignment(Pos.CENTER);
            taP1Chat.setPrefWidth(CHAT_WIDTH);
            taP1Chat.setPrefHeight(200);
            taP1Chat.setEditable(false);
            taP1Chat.setText("Player1 Private Chat Room");
            
            taP2Chat.setPrefWidth(CHAT_WIDTH);
            taP2Chat.setPrefHeight(200);
            taP2Chat.setEditable(false);
            taP2Chat.setText("Player2 Private Chat Room");
         
            taP3Chat.setPrefWidth(CHAT_WIDTH);
            taP3Chat.setPrefHeight(200);
            taP3Chat.setEditable(false);
            taP3Chat.setText("Player3 Private Chat Room");
         
            taP4Chat.setPrefWidth(CHAT_WIDTH);
            taP4Chat.setPrefHeight(200);
            taP4Chat.setEditable(false);
            taP4Chat.setText("Player4 Private Chat Room");
      spPrivateChat.getChildren().addAll(taP1Chat, taP2Chat, taP3Chat, taP4Chat);
      
      // Send private
      FlowPane fpPrivateChatEnter = new FlowPane(5,5);
         fpPrivateChatEnter.setAlignment(Pos.CENTER);
         tfPrivateChatEnter.setPromptText("Message...");
         tfPrivateChatEnter.setPrefColumnCount(12);
         fpPrivateChatEnter.getChildren().addAll(tfPrivateChatEnter, btnPrivateChatEnter);
         
      
      rootPrivateChat.getChildren().addAll(fpPrivateChat, fpPlayerSelect, spPrivateChat, fpPrivateChatEnter);
      
      Label lblFill1 = new Label("");
      Label lblFill2 = new Label("");
      
      rootChat.getChildren().addAll(rootPublicChat, lblFill1, lblFill2, rootPrivateChat);
      
      // CHAT setOnAction()
      btnChatEnter.setOnAction(this);
      btnGLHF.setOnAction(this);
      btnGG.setOnAction(this);
      
      btnPrivateChatEnter.setOnAction(this);
      btnP1Chat.setOnAction(this);
      btnP2Chat.setOnAction(this);
      btnP3Chat.setOnAction(this);
      btnP4Chat.setOnAction(this);
      
      
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
                  case W: case UP:    gas = true; 
                     break;
                  case S: case DOWN:  brake = true; 
                     break;
                  case A: case LEFT:  turnLeft  = true; 
                     break;
                  case D: case RIGHT: turnRight  = true; 
                     break;
               }
            }
         });
   
      gameScene.setOnKeyReleased(
         new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
               switch (event.getCode()) {
                  case W: case UP:    gas = false; 
                     break;
                  case S: case DOWN:  brake = false; 
                     break;
                  case A: case LEFT:  turnLeft  = false; 
                     break;
                  case D: case RIGHT: turnRight  = false; 
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
         
         switch(command)
         {
            case "Start":
               if(tfServerIp.getText().length() > 1)
               {
                  Client c = new Client();
                  c.start();
                  btnStart.setDisable(true);
               }
               else
               {
                  DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting the game", "Server IP field is empty. Please, enter a Server IP.");
               }
               break;
            case "Options":
               stage.setScene(optionsScene);
               stage.show();
               break;
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
                  taChatBox.appendText("\nYou: " + text);
                  sendChatMessageToServer(text);
               }
               break;
            case "GLHF":
               taChatBox.appendText("\nYou: GLHF!");
               sendChatMessageToServer("GLHF!");
               btnGLHF.setDisable(true);
               ButtonSleep bsGLHF = new ButtonSleep(btnGLHF, BUTTON_SLEEP);
               bsGLHF.start();
               break;
            case "GG":
               taChatBox.appendText("\nYou: GG");
               sendChatMessageToServer("GG");
               btnGG.setDisable(true);
               ButtonSleep bsGG = new ButtonSleep(btnGG, BUTTON_SLEEP);
               bsGG.start();
               break;
            case "Player 1":
               privateChatClientNumber = 1;
               
               showPrivateChat(privateChatClientNumber);
               break;
            case "Player 2":
               privateChatClientNumber = 2;
               
               showPrivateChat(privateChatClientNumber);
               break;
            case "Player 3":
               privateChatClientNumber = 3;
               
               showPrivateChat(privateChatClientNumber);
               break;
            case "Player 4":
               privateChatClientNumber = 4;
               
               showPrivateChat(privateChatClientNumber);
               break;
            case "Send Private":
               String privateText = tfPrivateChatEnter.getText();
               tfPrivateChatEnter.clear();
                              
               if(privateText.length() > 0)
               {
                  switch(privateChatClientNumber)
                  {
                     case 1:
                        taP1Chat.appendText("\nYou: " + privateText);
                        break;
                     case 2:
                        taP2Chat.appendText("\nYou: " + privateText);
                        break;
                     case 3:
                        taP3Chat.appendText("\nYou: " + privateText);
                        break;
                     case 4:
                        taP4Chat.appendText("\nYou: " + privateText);
                        break;
                  }
                  
                  sendPrivateChatMessageToServer(privateChatClientNumber, privateText);
               }
               break;
         }
      }
   }
   
   class ButtonSleep extends Thread
   {
      // Attributes
      Button btn;
      double time;
      public ButtonSleep(Button _btn, double _time)
      {
         this.btn = _btn;
         this.time = _time;
      }
      
      public void run()
      {
         try
         {
            Thread.sleep((long)time);
         }
         catch(InterruptedException ie)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "ButtonSleep", ie + "");
         }
         
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  btn.setDisable(false);
               }
            }
         );
      }
   }
   
   public void showPrivateChat(int _chatId)
   {
      showPrivateChat(_chatId, "");
   }
   
   public void showPrivateChat(int _chatId, String _message)
   {
      switch(_chatId)
      {
         case 1:
            
            if(_message.length() > 0)
            {
               taP1Chat.appendText("\nPlayer" + _chatId + ": " + _message);
            }
            
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
         
            if(_message.length() > 0)
            {
               taP2Chat.appendText("\nPlayer" + _chatId + ": " + _message);
            }
         
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
            
            if(_message.length() > 0)
            {
               taP3Chat.appendText("\nPlayer" + _chatId + ": " + _message);
            }
         
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
         
            if(_message.length() > 0)
            {
               taP4Chat.appendText("\nPlayer" + _chatId + ": " + _message);
            }
         
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
            synchronized(oosLock)
            {
               oos.writeObject("UPDATE_COORDINATES");
               oos.writeObject(cs);  
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
      
      public Client()
      {
      }
      
      public void run()
      {
         try
         {
            // Open socket for server connection
            socket = new Socket(tfServerIp.getText(), SERVER_PORT);
            
            // Input/output with the server																
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
         }
         //catch(ClassNotFoundException cnfe)
         //{
         //   DisplayMessage.showAlert(stage, AlertType.ERROR, "Error connecting to the server", cnfe + "");
         //}
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
         while(!error)
         {
            Object input = null;
            
            // Wait for a command
            try
            {
               input = ois.readObject();
            }
            catch(ConnectException ce)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: Error receiving command", ce + "");
               btnStart.setDisable(false);
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
                        //DisplayMessage.showAlert(stage, AlertType.INFORMATION, "CONNECTED TO THE SERVER", "YAY  " + playerNumber);
                           // SEND car file name
                        oos.writeObject(carFileArray[carArrayIndex]);
                           // RECEIVE starting coordintaes
                        mainStartX = (double)ois.readObject();
                        mainStartY = (double)ois.readObject();
                        mainStartDegree = (double)ois.readObject();
                        //System.out.println("sX: " + mainStartX + "   sY: " + mainStartY);
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
                        opponents.add(op);
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
                     for(Opponent op:opponents)
                     {
                        Player plOp = new Player(gameClient, stage, op.getClientNumber(), op.getCarFileName(), op.getStartX(), op.getStartY(), op.getStartDegree(), TRACK_WIDTH, TRACK_HEIGHT);
                        //Player plOp = new Player(stage, op.getClientNumber(), op.getCarFileName(), 0, 0, op.getStartDegree(), TRACK_WIDTH, TRACK_HEIGHT);
                        opponentPlayers.add(plOp);
                        //System.out.println(op);
                     }
                     
                     for(Player p:opponentPlayers)
                     {
                        track.getChildren().add(p);
                        //System.out.println(p);
                        //System.out.println(p.getCoordinates());
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
                        
                        for(Player p:opponentPlayers)
                        {
                           if(p.getPlayerNumber() == cs.getClientNumber() && cs != null)
                           {
                              p.setCoordinates(cs.getX(), cs.getY(), cs.getDegree());
                              //System.out.println(String.format("UC: X:%f  Y:%f  DEG:%f", cs.getX(), cs.getY(), cs.getDegree()));
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
                     
                     String chatMessage = String.format("Player%d: %s", senderId, message);
                     
                     taChatBox.appendText("\n" + chatMessage);
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
               } // switch(command)
            } // if instanceof String
         } // while(true)
      } // run()
   } // Client
}