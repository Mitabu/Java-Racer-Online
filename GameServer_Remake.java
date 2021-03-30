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

public class GameServer_Remake extends Application implements EventHandler<ActionEvent>
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
   private int playerNum = 1;
   private int numOfLaps;
   
   private double[] startX = {130, 180, 130, 180};
   private double[] startY = {520, 450, 380, 310};
   private double startDegree = 180;
   
   // Networking
      // Port
   private static final int SERVER_PORT = 42069;
      // Server Socket
   private ServerSocket serverSocket = null;
   
   /** Array of client objects*/
   private Client[] clients = null;
   private ArrayList<ClientThread> clientThreads = new ArrayList<ClientThread>();
   
   // Synchronization
   private Object clientsLock = new Object();
   private Object playerInitLock = new Object();
   
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
         clients = new Client[numOfPlayers];
         
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
         
         ServerThread st = new ServerThread(serverSocket);
         st.start();
      }
      catch(IOException ioe)
      {
         DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting server", ioe + "");
      }
      
   }
   
   class ServerThread extends Thread
   {
      // Attributes
      ServerSocket sSocket = null;
      
      /* ServerThread constructor*/
      public ServerThread(ServerSocket _sSocket)
      {
         this.sSocket = _sSocket;
      }
      
      public void run()
      {
         // Show that server started waiting for clients
         taLog.appendText("\nWaiting for players to join...\n");
         
         // Wait for clients
         while(numOfPlayers > 0)
         {
            Socket cSocket = null;
            try
            {
               cSocket = this.sSocket.accept();
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "Error joining players", ioe + "");
            }
            
            ClientThread ct = new ClientThread(cSocket, playerNum);
            clientThreads.add(ct);
            taLog.appendText("\nPlayer" + playerNum + " joined\n");
            numOfPlayers--;
            playerNum++;
         }
         
         // Initialize Players
         for(ClientThread cThread:clientThreads)
         {
            cThread.initializePlayer();
         }
         taLog.appendText("\nPlayers initialized...");
         
         System.out.println("\nClient list after initialization of players\n");
         for(Client c:clients)
         {
            System.out.println(c);
         }
         System.out.println("\n");
         
         System.out.println("\nOpponent initialization\n");
         
         // Initialize Opponents
         for(ClientThread cThread:clientThreads)
         {
            cThread.initializeOpponents();
         }
         taLog.appendText("\nOpponents initialized...");
         System.out.println("\n");
         
         // Start client threads for listening
         for(ClientThread cThread:clientThreads)
         {
            cThread.start();
         }
         taLog.appendText("\nClient threads started...");
         
         // Start Game
         for(ClientThread cThread:clientThreads)
         {
            ClientOutput co = new ClientOutput(cThread.getOos(), "START_GAME");
            co.start();
         }
         taLog.appendText("\nSTART_GAME sent...");
         
      } // run()
   } // Server Thread
   
   class ClientThread extends Thread
   {
      // Attributes
         // General
      private Socket cSocket = null;
      private int clientNumber;
         // Client Server Communication
      private ObjectOutputStream oos = null;
      private ObjectInputStream ois = null;
      
      public ObjectOutputStream getOos() {return this.oos;}
      
      /** ClientThread constructor*/
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
      } // constructor
      
      public void run()
      {
         
      } // run()
      
      /** Initializes and record player data to the list on the server*/
      public void initializePlayer()
      {
         try
         {
            oos.writeObject("INIT_PLAYER");
            oos.writeObject(this.clientNumber);
            
            String carFileName = (String)ois.readObject();
            
            Client c = new Client(this.cSocket, this.oos, this.ois, this.clientNumber);
            
            c.setCarFileName(carFileName);
            c.setStartX(startX[clientNumber-1]);
            c.setStartY(startY[clientNumber-1]);
            c.setStartDegree(startDegree);
            
            oos.writeObject(c.getStartX());
            oos.writeObject(c.getStartY());
            oos.writeObject(c.getStartDegree());
            
            //System.out.println(c);
            clients[clientNumber-1] = c;
            //System.out.println("Within array: " + clients[clientNumber-1]);
         }
         catch(ClassNotFoundException cnfe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "initializePlayer() CNFE", cnfe + "");
         }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "initializePlayer() IOE", ioe + "");
         }
      }
      
      /** Sends the player the data about their opponents*/
      public void initializeOpponents()
      {
         try
         {            
            for(Client c:clients)
            {
               if(c.getClientNumber() != this.clientNumber) // if this is not the receiving client
               {
                  oos.writeObject("INIT_OPPONENT");
                  Opponent op = new Opponent(c.getClientNumber(),
                                             c.getCarFileName(),
                                             c.getStartX(),
                                             c.getStartY(),
                                             c.getStartDegree());
                  System.out.println("Client" + this.clientNumber + " INIT_OPPONENT: " + c);
                  oos.writeObject(op);
               }
            }
         }
         // catch(ClassNotFoundException cnfe)
         // {
         //    DisplayMessage.showAlert(stage, AlertType.ERROR, "initializePlayer() CNFE", cnfe + "");
         // }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "initializePlayer() IOE", ioe + "");
         }
      }
      
   } // ClientThread
   
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
            oos.flush();
         }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "ClientOutput: Error sending Object", ioe + "");
         }
      }
   }
}