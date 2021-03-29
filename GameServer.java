// JavaFX
import javafx.application.Application;
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
*  @version 29.03.2021
*/

public class GameServer extends Application implements EventHandler<ActionEvent>
{
   // Attributes
   // Stage
   private Stage stage;
   
   // Window proportions
      // Track (16/9)
   private static final double WINDOW_WIDTH = 300;
   private static final double WINDOW_HEIGHT = 400;
   
   // Layout
      // Main
   private Scene mainScene = null;
   private VBox root = new VBox(5);
      // Server address
   private FlowPane fpTop = new FlowPane(5,5);
   private Label lblServerIp = new Label("Server IP: ");
   private TextField tfServerIp = new TextField();
   private String serverIp = null;
      // Game settings
   private VBox vbSettings = new VBox(5);
         // Number of Players
   private Label lblNumOfPlayers = new Label("Number of Players: ");
   private ComboBox<Integer> cbNumOfPlayers = new ComboBox<Integer>();
   private int[] playerNums = {2, 3, 4};
         // Number of Laps
   private Label lblNumOfLaps = new Label("Number of Laps: ");
   private TextField tfNumOfLaps = new TextField();
      // StartGame
   private FlowPane fpStartGame = new FlowPane(5,5);
   private Button btnStartGame = new Button("Start Game");
      // Log
   private TextArea taLog = new TextArea();
   
   // Game
   private int numOfPlayers;
   private int numOfLaps;
   
   // Networking
      // Port
   private static final int SERVER_PORT = 42069;
      // Server Socket
   private ServerSocket serverSocket = null;
   
      /** ArrayList of client sockets*/
   private ArrayList<Client> clients = new ArrayList<Client>();
   private Object clientsLock = new Object();
   
   /** Sets up the stage and GUI*/
   public void start(Stage _stage)
   {
      stage = _stage;
      stage.setTitle("Artem Polkovnikov - JRO Server");
      
      // Top line
         tfServerIp.setPrefColumnCount(10);
         tfServerIp.setEditable(false);
         
         serverIp = "IP NOT FOUND";
         try
         {
            serverIp = InetAddress.getLocalHost().getHostAddress();
         }
         catch(UnknownHostException uhs)
         {
            System.out.println("Unknown Host: " + uhs);
         }
         
         tfServerIp.setText(serverIp);
         fpTop.getChildren().addAll(lblServerIp, tfServerIp);
      root.getChildren().add(fpTop);
      
      // Setttings
         // Number of players
      FlowPane fpNumOfPlayers = new FlowPane(5,5);
         for(int i:playerNums)
         {
            cbNumOfPlayers.getItems().add(i);
         }
         
         fpNumOfPlayers.getChildren().addAll(lblNumOfPlayers, cbNumOfPlayers);
         // Number of laps
      FlowPane fpNumOfLaps = new FlowPane(5,5);
         fpNumOfLaps.getChildren().addAll(lblNumOfLaps, tfNumOfLaps);
      vbSettings.getChildren().addAll(fpNumOfPlayers, fpNumOfLaps);
      root.getChildren().add(vbSettings);
      
      // Start Game
         fpStartGame.setAlignment(Pos.CENTER);
         fpStartGame.getChildren().add(btnStartGame);
      root.getChildren().add(fpStartGame);
      
      // Log
         taLog.setPrefWidth(WINDOW_WIDTH);
         taLog.setPrefHeight(WINDOW_HEIGHT - 50);
         taLog.setEditable(false);
      root.getChildren().add(taLog);
      
      // setOnAction
      btnStartGame.setOnAction(this);
      
      mainScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
      stage.setScene(mainScene);
      stage.setResizable(false);
      stage.show();
   }
   
   /** Button dispatcher*/
   public void handle(ActionEvent ae)
   {
      String command = ((Button)ae.getSource()).getText();
      
      switch(command)
      {
         case "Start Game":
            startGame();
            break;
      }
   }
   
   public void startGame()
   {
      if(cbNumOfPlayers.getValue() == null ||
         tfNumOfLaps.getText().length() < 1)
      {
         DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting game", "One or multiple game settings are incorrect");
      }
      else
      {
         numOfPlayers = cbNumOfPlayers.getValue();
         String tempLaps = tfNumOfLaps.getText();
         
         try
         {
            numOfLaps = Integer.parseInt(tempLaps);
         }
         catch(Exception e)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting game", "The value in the \"Number of Laps\" field is not a number.");
         }
         
         startServer();
      }
   }
   
   private void startServer()
   {
      try
      {
         // Claim server socket
         serverSocket = new ServerSocket(SERVER_PORT);
         // Report starting
         taLog.appendText("Server socket claimed\n");
         
         // Wait for the players
         ServerWaitToJoin swtj = new ServerWaitToJoin();
         swtj.start();
      }
      catch(IOException ioe)
      {
         DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting server", ioe + "");
      }
      
   }
   
   class ServerWaitToJoin extends Thread
   {
      public void run()
      {
         taLog.appendText("\nServer Started\nWaiting for players to join\n");
         while(numOfPlayers > 0)
         {
            try
            {
               // Wait for client
               Socket cSocket = serverSocket.accept();
               // Create client
               ClientThread ct = new ClientThread(cSocket, numOfPlayers);
               // Start the client thread
               ct.start();
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "Error joining players", ioe + "");
            }
            
            // Subtract availiable player spaces
            numOfPlayers--;
         }
         // Start the game
         taLog.appendText("\nGame room is full\nStarting the game...\n");
         
         ServerThread st = new ServerThread();
         st.start();
      }
   }
   
   class ServerThread extends Thread
   {
      
   }
   
   class ClientThread extends Thread
   {
      // Attributes
         // General
      private Socket cSocket = null;
      private int clientNumber;
         // Client Server Communication
      private ObjectOutputStream oos = null;
      private ObjectInputStream ois = null;
      
      public ClientThread(Socket _cSocket, int _clientNumber)
      {
         this.cSocket = _cSocket;
         this.clientNumber = _clientNumber;
         
         try
         {
            this.oos = new ObjectOutputStream(this.cSocket.getOutputStream());
            this.ois = new ObjectInputStream(this.cSocket.getInputStream());
         }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "ClientThread: Error instantiating streams", ioe + "");
         }
         
         // Create new client
         Client c = new Client(this.cSocket, this.oos, this.ois, this.clientNumber);
         // Add client to the client list
         clients.add(clientNumber, c);
      }
      
      public void run()
      {
         taLog.appendText("\nClient" + clientNumber + " joined the game\n");
         
         while(true)
         {
            Object input = null;
            
            // Wait for a command
            try
            {
               input = ois.readObject();
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
                     // Send client num
                     Integer clientNum = this.clientNumber;
                     sendObject(this.oos, clientNum);
                     
                     String carFileName = null;
                     try
                     {
                        carFileName = (String)ois.readObject();
                     }
                     catch(ClassNotFoundException cnfe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: Error receiving command", cnfe + "");
                     }
                     catch(IOException ioe)
                     {
                        DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: Error receiving command", ioe + "");
                     }
                     
                     break;
                  default:
                     taLog.appendText("ClientThread: An unknown command received.\n");
                     break;
               }
            }
         }
      }
   }
   
   private void sendObject(ObjectOutputStream _oos, Object _object)
   {
      ClientOutput co = new ClientOutput(_oos, _object);
      co.start();
   }
   
   class ClientOutput extends Thread
   {
      // Attributes
      private ObjectOutputStream oos = null;
      private Object objectToSend = null;
      
      public ClientOutput(ObjectOutputStream _oos, Object _objectToSend)
      {
         this.oos = _oos;
         this.objectToSend = _objectToSend;
      }
      
      public void run()
      {
         try
         {
            oos.writeObject(objectToSend);
         }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "ClientOutput: Error sending Object", ioe + "");
         }
      }
   }
}