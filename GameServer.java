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
*  @version 02.04.2021
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
   private FlowPane fpServerPassword = new FlowPane(5,5);
   //private Label lblServerIp = new Label("Server IP: ");
   private TextField tfServerIp = new TextField();
   private TextField tfServerPassword = new TextField();
   private String serverIp = null;
   private String serverPassword = null;
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
   
   // Synchronization
   private Object clientThreadsLock = new Object();
   
   // Game
   private int numOfPlayers;
   private int playerNum = 1;
   private int numOfLaps;
   
   private double[] startX = {130, 180, 130, 180};
   private double[] startY = {520, 450, 380, 310};
   private double startDegree = 180;
   
   
      // Check points
   private CheckPoint[] checkPointArray = 
      {new CheckPoint(220, 820, 220, 678), 
       new CheckPoint(435, 700, 435, 555), 
       new CheckPoint(895, 685, 895, 552), 
       new CheckPoint(1230, 718, 1230, 865), 
       new CheckPoint(1320, 235, 1465, 235), 
       new CheckPoint(1307, 60, 1307, 218), 
       new CheckPoint(848, 140, 785, 20), 
       new CheckPoint(577, 315, 577, 455), 
       new CheckPoint(225, 245, 150, 140), 
       new CheckPoint(85, 620, 205, 620)};
   private ArrayList<CheckPoint> checkPoints = new ArrayList<CheckPoint>();
   
   private boolean gameRunning = true;
   
   // Networking
      // Port
   private static final int SERVER_PORT = 42069;
      // Server Socket
   private ServerSocket serverSocket = null;
   
   /** Array of client objects*/
   private Client[] clients = null;
   private ArrayList<ClientThread> clientThreads = new ArrayList<ClientThread>();
   
   /** Sets up the stage and GUI*/
   public void start(Stage _stage)
   {
      stage = _stage;
      stage.setTitle("Artem Polkovnikov - JRO Server");
      stage.setOnCloseRequest(
      new EventHandler<WindowEvent>() {
         public void handle(WindowEvent evt)
         {
            try
            {
               serverSocket.close();
            }
            catch(Exception e)
            {
               System.exit(1);
            }
            System.exit(0);
         }
      });
      
      // VARIABLES
      Collections.addAll(checkPoints, checkPointArray);
      
      // Top line
         Label lblServerIp = new Label("Server IP: ");
         tfServerIp.setPrefColumnCount(10);
         tfServerIp.setDisable(true);
         
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
      
      // Password
         Label lblServerPassvord = new Label("Password(Optional): ");
         tfServerPassword.setPrefColumnCount(10);
         tfServerPassword.setPromptText("1 to 20 char");
         fpServerPassword.getChildren().addAll(lblServerPassvord, tfServerPassword);
      root.getChildren().add(fpServerPassword);
      
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
         tfNumOfLaps.setPromptText("1 to 2,147,483,646");
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
         taLog.setWrapText(true);
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
            if(tfServerPassword.getText().length() <= 20)
            {
               tfServerPassword.setDisable(true);
               serverPassword = tfServerPassword.getText();
               startGame();
            }
            else
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting game", "Password is too long. Password be 1 to 20 chars long.");
            }
            break;
      }
   }
   
   public void startGame()
   {
      if(cbNumOfPlayers.getValue() == null ||
         tfNumOfLaps.getText().length() < 1)
      {
         DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting game", "One or multiple game settings are incorrect");
         tfServerPassword.setDisable(false);
      }
      else
      {
         numOfPlayers = cbNumOfPlayers.getValue();
         String tempLaps = tfNumOfLaps.getText();
         //taLog.appendText("Creating client array " + numOfPlayers + "\n");
         clients = new Client[numOfPlayers];
         
         boolean isANumber = false;
         int tempLapNum = 0;
         try
         {
            tempLapNum = Integer.parseInt(tempLaps);
            isANumber = true;
         }
         catch(Exception e)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting game", "The value in the \"Number of Laps\" field is not a number.");
            tfServerPassword.setDisable(false);
         }
         
         if(isANumber)
         {
            if(tempLapNum >= 2_147_483_647 || tempLapNum < 1)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting game", "The value in the \"Number of Laps\" field is either to high or too low.");
               tfServerPassword.setDisable(false);
            }
            else
            {
               numOfLaps = tempLapNum;
               btnStartGame.setDisable(true);
               startServer();
            }
         }
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
      catch(SocketException se)
      {
         DisplayMessage.showAlert(stage, AlertType.ERROR, "Error starting server", se + "");
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
         Platform.runLater
         (
            new Runnable()
            {
               public void run()
               {
                  // Show that server started waiting for clients
                  taLog.appendText("\nWaiting for players to join...\n");
               }
            }
         );
         
         // Wait for clients
         while(numOfPlayers > 0)
         {
            Socket cSocket = null;
            try
            {
               cSocket = this.sSocket.accept();
            
               ObjectInputStream input = new ObjectInputStream(cSocket.getInputStream());
               ObjectOutputStream output = new ObjectOutputStream(cSocket.getOutputStream());
               
               String pass = (String)input.readObject();
               
               if(pass.equals(serverPassword))
               {
                  output.writeObject("LOGGED_IN");
                  ClientThread ct = new ClientThread(cSocket, output, input, playerNum);
                  synchronized(clientThreadsLock)
                  {
                     clientThreads.add(ct);
                  }
                  numOfPlayers--;
                  playerNum++;
               }
               else
               {
                  output.writeObject("DENIED");
               }
            }
            catch(ClassNotFoundException cnfe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "Error joining players", cnfe + "");
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "Error joining players", ioe + "");
            }
         }
         
         // Initialize Players
         synchronized(clientThreadsLock)
         {
            for(ClientThread cThread:clientThreads)
            {
               cThread.initializePlayer();
               taLog.appendText("\n" + cThread.getClientName() + "#" + cThread.getClientNumber() + " joined\n");
            }
         }
         taLog.appendText("\nPlayers initialized...");
         
//          System.out.println("\nClient list after initialization of players\n");
//          for(Client c:clients)
//          {
//             System.out.println(c);
//          }
//          System.out.println("\n");
         
//         System.out.println("\nOpponent initialization\n");
         
         // Initialize Opponents
         synchronized(clientThreadsLock)
         {
            for(ClientThread cThread:clientThreads)
            {
               cThread.initializeOpponents();
            }
         }
         taLog.appendText("\nOpponents initialized...");
//         System.out.println("\n");
         
         // Start client threads for listening
         synchronized(clientThreadsLock)
         {
            for(ClientThread cThread:clientThreads)
            {
               cThread.start();
            }
         }
         taLog.appendText("\nClient threads started...");
         
         // Start Game
         synchronized(clientThreadsLock)
         {
            for(ClientThread cThread:clientThreads)
            {
               ClientOutput co = new ClientOutput(cThread.getOos(), "START_GAME");
               co.start();
            }
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
      private String clientName;
      private boolean error = false;
         // Client Server Communication
      private ObjectOutputStream oos = null;
      private ObjectInputStream ois = null;
      
      public ObjectOutputStream getOos() {return this.oos;}
      
      // Synchronization
      private Object oisLock = new Object();
      private Object oosLock = new Object();
      private Object lock = new Object();
      
      // GamePlay
      private int currentCheckPoint = 0;
      private int currentLap = 1;
      private double prevX = 0;
      private double prevY = 0;
      
      public int getClientNumber() {return this.clientNumber;}
      public String getClientName() {return this.clientName;}
      
      /** ClientThread constructor*/
      public ClientThread(Socket _cSocket, ObjectOutputStream _oos, ObjectInputStream _ois, int _clientNumber)
      {
         this.cSocket = _cSocket;
         this.clientNumber = _clientNumber;
         this.oos = _oos;
         this.ois = _ois;
      } // constructor
      
      public void run()
      {
         // Client communication loop
         while(!error)
         {
            Object input = null;
            
            // Wait for a command
            try
            {
               input = ois.readObject();
            }
            catch(SocketException se)
            {
               //DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: Error receiving command", se + "");
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
                  case "UPDATE_COORDINATES":
                     CoordinateSet cs = null;
                     synchronized(lock)
                     {
                        try
                        {
                           cs = (CoordinateSet)ois.readObject();
                        }
                        catch(ClassNotFoundException cnfe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: UPDATE_COORDINATES", cnfe + "");
                           error = true; 
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: UPDATE_COORDINATES", ioe + "");
                           error = true; 
                        }
                     }
                     if(cs != null)
                     {
                        // Send coordinates to opponents
                        synchronized(clientThreadsLock)
                        {
                           for(ClientThread ct:clientThreads)
                           {
                              if(ct.getClientNumber() != clientNumber)
                              {
                                 ct.updateOpponent(cs);
                              }
                           }
                        }
                        
                        // Check if passed a check point
                        if(gameRunning && GameLogic.isIntersect(new CheckPoint(prevX + 20, prevY + 35, cs.getX() + 20, cs.getY() + 35),  checkPoints.get(currentCheckPoint)))
                        {
                           prevX = cs.getX();
                           prevY = cs.getY();
                           synchronized(lock)
                           {
                              try
                              {
                                 if(currentCheckPoint == checkPoints.size() - 1 && gameRunning)
                                 {
                                    currentCheckPoint = 0;
                                    currentLap++;
                                    if(currentLap > numOfLaps && gameRunning)
                                    {
                                       gameRunning = false;
                                       taLog.appendText("\n\nGame Over\n" + clientName + "#" + clientNumber + " won the race!");
                                       for(ClientThread ct:clientThreads)
                                       {
                                          ct.stopGame(clientNumber);
                                          ct.receiveChatMessage(-1, "Game Over\n" + clientName + "#" + clientNumber + " won the race!");
                                       }
                                    }
                                    else
                                    {
                                       oos.writeObject("UPDATE_LAP");
                                    }
                                 }
                                 else
                                 {
                                    oos.writeObject("UPDATE_CHECKPOINT");
                                    currentCheckPoint++;
                                 }
                              }
                              catch(IOException ioe)
                              {
                                 DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: UPDATE_COORDINATES (check points)", ioe + "");
                                 error = true;
                              }
                              catch(Exception e)
                              {
                                 DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: UPDATE_COORDINATES (check points)", e + "");
                                 System.out.println("AM I HERE?");
                              }
                           } // synchronized(lock)
                        }
                     }
                     break; // UPDATE_COORDINATES
                  case "CHAT_MESSAGE":
                     int clientNumber = 0;
                     String message = null;
                     
                     synchronized(lock)
                     {
                        try
                        {
                           clientNumber = (Integer)ois.readObject();
                           message = (String)ois.readObject();
                        }
                        catch(ClassNotFoundException cnfe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: CHAT_MESSAGE", cnfe + "");
                           error = true;
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: CHAT_MESSAGE", ioe + "");
                           error = true;
                        }
                     }
                     
                     synchronized(clientThreadsLock)
                     {
                        for(ClientThread ct:clientThreads)
                        {
                           if(ct.getClientNumber() != clientNumber)
                           {
                              ct.receiveChatMessage(clientNumber, message);
                           }
                        }
                     }
                     break; // CHAT_MESSAGE
                  case "PRIVATE_CHAT_MESSAGE":
                     int senderNumber = 0;
                     int recepientNumber = 0;
                     String privateMessage = null;
                     
                     synchronized(lock)
                     {
                        try
                        {
                           senderNumber = (Integer)ois.readObject();
                           recepientNumber = (Integer)ois.readObject();
                           privateMessage = (String)ois.readObject();
                        }
                        catch(ClassNotFoundException cnfe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: PRIVATE_CHAT_MESSAGE", cnfe + "");
                           error = true;
                        }
                        catch(IOException ioe)
                        {
                           DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: PRIVATE_CHAT_MESSAGE", ioe + "");
                           error = true;
                        }
                     }
                     
                     synchronized(clientThreadsLock)
                     {
                        for(ClientThread ct:clientThreads)
                        {
                           if(ct.getClientNumber() == recepientNumber)
                           {
                              ct.receivePrivateChatMessage(senderNumber, privateMessage);
                           }
                        }
                     }
                     break; // PRIVATE_CHAT_MESSAGE
               }
            }
         } // while(!error)
         
         if(error)
         {
            synchronized(clientThreadsLock)
            {
               for(ClientThread ct:clientThreads)
               {
                  if(ct.getClientNumber() != this.getClientNumber())
                  {
                     ct.disconnectOpponent(this.getClientNumber());
                     //System.out.println("Sent Player" + ct.getClientNumber() + " opponent disconnection request.");
                  }
               }
            }
            
            synchronized(clientThreadsLock)
            {
               for(ClientThread ct:clientThreads)
               {
                  if(ct.getClientNumber() == clientNumber)
                  {
                     clientThreads.remove(ct);
                     //System.out.println("Removed client" + ct.getClientNumber());
                     break;
                  }
               }
            }
            
            try
            {
               this.oos.close();
               this.ois.close();
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "ClienThread: PRIVATE_CHAT_MESSAGE", ioe + "");
               error = true;
            }
            
            taLog.appendText("\n\n" + clientName + "#" + clientNumber + " disconnected.");
            
            // if all clients disconnected stop the server
            if(clientThreads.size() == 0)
            {
               //DisplayMessage.showAlert(stage, AlertType.INFORMATION, "All players left the game", "Stopping the server");
               System.exit(0);
            }
         } // if(error)   
      } // run()
      
      /** Initializes and record player data to the list on the server*/
      public void initializePlayer()
      {
         try
         {
            oos.writeObject("INIT_PLAYER");
            oos.writeObject(this.clientNumber);
            oos.writeObject(numOfLaps);
            
            String carFileName = (String)ois.readObject();
            this.clientName = (String)ois.readObject();
            
            
            Client c = new Client(this.cSocket, this.oos, this.ois, this.clientNumber, this.clientName);
            
            c.setCarFileName(carFileName);
            c.setStartX(startX[clientNumber-1]);
            prevX = c.getStartX();
            c.setStartY(startY[clientNumber-1]);
            prevY = c.getStartY();
            c.setStartDegree(startDegree);
            
            oos.writeObject(c.getStartX());
            oos.writeObject(c.getStartY());
            oos.writeObject(c.getStartDegree());
            
            //System.out.println(c);
            clients[clientNumber-1] = c;
            //System.out.println("Within array: " + clients[clientNumber-1]);
         
            oos.writeObject(checkPointArray);
         }
         catch(ClassNotFoundException cnfe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "initializePlayer() CNFE", cnfe + "");
         }
         catch(IOException ioe)
         {
            DisplayMessage.showAlert(stage, AlertType.ERROR, "initializePlayer() IOE", ioe + "");
         }
      } // initializePlayer()
      
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
                                             c.getClientName(),
                                             c.getCarFileName(),
                                             c.getStartX(),
                                             c.getStartY(),
                                             c.getStartDegree());
                  //System.out.println("Client" + this.clientNumber + " INIT_OPPONENT: " + c);
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
      } // initializeOpponents()
      
      public void updateOpponent(CoordinateSet _cs)
      {
         synchronized(lock)
         {
            try
            {
               oos.writeObject("UPDATE_OPPONENT");
               oos.writeObject(_cs);
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "updateOpponent() IOE", ioe + "");
            }
         }
      }
      
      public void receiveChatMessage(int _clientNumber, String _message)
      {
         synchronized(lock)
         {
            try
            {
               oos.writeObject("RECEIVE_MESSAGE");
               oos.writeObject(_clientNumber);
               oos.writeObject(_message);
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "receiveChatMessage() IOE", ioe + "");
            }
         }
      }
      
      public void receivePrivateChatMessage(int _senderNumber, String _message)
      {
         synchronized(lock)
         {
            try
            {
               oos.writeObject("RECEIVE_PRIVATE_MESSAGE");
               oos.writeObject(_senderNumber);
               oos.writeObject(_message);
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "receivePrivateChatMessage() IOE", ioe + "");
            }
         }
      }
      
      public void disconnectOpponent(int _opponentNumber)
      {
         synchronized(lock)
         {
            try
            {
               oos.writeObject("DISCONNECT_OPPONENT");
               oos.writeObject(_opponentNumber);
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "disconnectOpponent() IOE", ioe + "");
            }
         }
      }
      
      public void stopGame(int _winnerNumber)
      {
         synchronized(lock)
         {
            try
            {
               oos.writeObject("STOP_GAME");
               oos.writeObject(_winnerNumber);
            }
            catch(IOException ioe)
            {
               DisplayMessage.showAlert(stage, AlertType.ERROR, "disconnectOpponent() IOE", ioe + "");
            }
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