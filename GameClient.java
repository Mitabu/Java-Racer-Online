// JavaFX
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.text.*;
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

/**
*  @author Artem Polkovnikov
*  @version 16.03.2021
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
      // Window
   public static final double WINDOW_WIDTH = TRACK_WIDTH;
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
   
   // Multiplayer
   private int playerNumber;
   private ArrayList<Opponent> opponents = new ArrayList<Opponent>();
   private ArrayList<Player> opponentPlayers = new ArrayList<Player>();
   
   private double mainStartX = 0;
   private double mainStartY = 0;
   private double mainStartDegree = 0;
   
   // Animation Timer
   private long lastUpdate = 0;
   
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
      
      root.add(track, 0, 0);
      
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
               if (now - lastUpdate >= 3_000_000) // Force update every 6.9 MS
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
         }
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
   
   public void sendCoordinatesToServer(double x, double y, double degree)
   {
      CoordinateSet cs = new CoordinateSet(playerNumber, x, y, degree);
      // Request
      try
      {
         oos.writeObject("UPDATE_COORDINATES");
         oos.writeObject(cs);
      }
      catch(IOException ioe)
      {
         DisplayMessage.showAlert(stage, AlertType.ERROR, "Error sending coordinates to the server", ioe + "");
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
         while(true)
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
            }
            catch(ClassNotFoundException cnfe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: Error receiving command", cnfe + "");
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: Error receiving command", ioe + "");
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
                        System.out.println("sX: " + mainStartX + "   sY: " + mainStartY);
                     }
                     catch(ClassNotFoundException cnfe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "INIT_PLAYER CNFE", cnfe + "");
                     }
                     catch(IOException ioe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "INIT_PLAYER IOE", ioe + "");
                     }
                     break;
                  case "INIT_OPPONENT":
                     try
                     {
                        Opponent op = (Opponent)ois.readObject();
                        opponents.add(op);
                        //DisplayMessage.showAlert(stage, AlertType.INFORMATION, "INIT_OPPONENT: Opponent Info received", op + "");
                        System.out.println("Player" + playerNumber + " INIT_OPPONENT: " + op);
                     }
                     catch(ClassNotFoundException cnfe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "INIT_OPPONENT CNFE", cnfe + "");
                     }
                     catch(IOException ioe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "INIT_OPPONENT IOE", ioe + "");
                     }
                     break; // INIT_OPPONENT
                  case "START_GAME":
                     System.out.println("\nSTART_GAME RECEIVED\n");
                     
                     // Create the main player
                     // PlayerNumber, NameOfCarFile, StartPosX, StartPosY, StartRotation(degrees)      
                     mainPlayer = new Player(gameClient, stage, playerNumber, carFileArray[carArrayIndex], mainStartX, mainStartY, mainStartDegree, TRACK_WIDTH, TRACK_HEIGHT);
                     //mainPlayer = new Player(this.stage, playerNumber, carFileArray[carArrayIndex], 0, 0, mainStartDegree, TRACK_WIDTH, TRACK_HEIGHT);
                     System.out.println("sX: " + mainStartX + "   sY: " + mainStartY);

                     createGameScene();
                     // Create opponents as players
                     for(Opponent op:opponents)
                     {
                        Player plOp = new Player(gameClient, stage, op.getClientNumber(), op.getCarFileName(), op.getStartX(), op.getStartY(), op.getStartDegree(), TRACK_WIDTH, TRACK_HEIGHT);
                        //Player plOp = new Player(stage, op.getClientNumber(), op.getCarFileName(), 0, 0, op.getStartDegree(), TRACK_WIDTH, TRACK_HEIGHT);
                        opponentPlayers.add(plOp);
                        System.out.println(op);
                     }
                     
                     for(Player p:opponentPlayers)
                     {
                        track.getChildren().add(p);
                        //System.out.println(p);
                        System.out.println(p.getCoordinates());
                     }
                     track.getChildren().add(mainPlayer);
                     System.out.println(mainPlayer.getCoordinates());
                     
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
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "UPDATE_COORDINATES IOE", ioe + "");
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
               } // switch(command)
            } // if instanceof String
         } // while(true)
      } // run()
   } // Client
}