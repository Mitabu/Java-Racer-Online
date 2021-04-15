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

/**
*  @author Artem Polkovnikov
*  @version 14.04.2021
*/

/** Creates title screen scene*/
public class TitleScreen
{
   // Attributes
         
         // ROOT
   /** Root pane of the scene*/
   private static VBox root;
         
         // GUI
   /** Game exit button*/
   private static Button btnExit = new Button("Exit");
   /** Game start button*/
   private static Button btnStart = null;
   /** Configuration save button*/
   private static Button btnSaveConfig = new Button("Save  Config");
   /** Configuration reset button*/
   private static Button btnResetConfig = new Button("Reset Config");
   /** Label that stores a 'waiting for other' players message*/
   private static Label lblWaitingForPlayers = new Label("Connected to the server. The game will start as soon as all players connect.");
         
         // Log In
   /** Server IP text field*/
   private static TextField tfServerIp = null;
   /** Standard server IP from config*/
   private static String presetTextServerIP = null;
   /** Game password text field*/
   private static TextField tfServerPassword = null;
   /** Player's nickname text field*/
   private static TextField tfClientName = null;
   /** Standard player's nickname from config*/
   private static String presetTextNickname = null;
   
         // Car Color Select
   /** Pane for storing car color select controls*/
   private static FlowPane fpColorSelect;
   /** Previous car color button*/
   private static Button btnPrev = new Button("Prev. color");
   /** Selected car color text field*/
   private static TextField tfColorSelect = null;
   /** Standard car color from config*/
   private static String selectedColor = null;
   /** Next car color button*/
   private static Button btnNext = new Button("Next color");
   
         // Title Screen Scene
   /** Title screen scene*/
   private static Scene titleScreen;
   /** Width of the scene in pixels*/
   private static int screenWidth;
   /** Height of the scene in pixels*/
   private static int screenHeight;
      
      // EventHandler
   /** EventHandelr for buttons*/
   private static EventHandler<ActionEvent> ae;
   
   /**
   * Sets up and returns the title screen scene
   *
   * @param _ae an event handler object for the options scene buttons
   * @param _screenWidth the width of the application window in pixels
   * @param _screenHeight the height of the application window in pixels
   * @param _tfServerIp text field for the server IP
   * @param _tfServerPassword text field for the game password
   * @param _tfClientName text field for the player's nickname
   * @param _btnStart button for starting the game
   * @param _tfColorSelect text field for displaying selected car color
   * @param _selectedColor car color from the config
   * @param _serverIP server IP from the config
   * @param _nickname player's nickname from the config
   *
   * @return title screen scene
   */
   public static Scene getScene(EventHandler<ActionEvent> _ae, int _screenWidth, int _screenHeight, TextField _tfServerIp, TextField _tfServerPassword, TextField _tfClientName, Button _btnStart, TextField _tfColorSelect, String _selectedColor, String _serverIP, String _nickname)
   {
      // Set scene parameters
      ae = _ae;
      screenWidth = _screenWidth;
      screenHeight = _screenHeight;
      
      // Get elements from the main class
      tfServerIp = _tfServerIp;
      tfServerPassword = _tfServerPassword;
      tfClientName = _tfClientName;
      tfColorSelect = _tfColorSelect;
      selectedColor = _selectedColor;
      btnStart = _btnStart;
      presetTextServerIP = _serverIP;
      presetTextNickname = _nickname;
      
      createTitleScreen();
      
      return titleScreen;
   }
   
   /** Makes the 'waiting for players' message visible on the scene*/
   public static void showConnectionMessage()
   {
      lblWaitingForPlayers.setVisible(true);
   }
   
   /** Sets up the title screen scene and its elements*/
   private static void createTitleScreen()
   {
      // Game Logo
      FlowPane fpLabel = new FlowPane();
         fpLabel.setAlignment(Pos.CENTER);
         Label lbl = new Label("Java Racer Online");
         lbl.setStyle("-fx-font-size: 40px; -fx-font-weight: bold");
         fpLabel.getChildren().add(lbl);
      
      // Start game button
      FlowPane fpStart = new FlowPane();
         fpStart.setAlignment(Pos.CENTER);
         fpStart.getChildren().add(btnStart);
      
      // Game configuration buttons
      FlowPane fpConfig = new FlowPane();
         Label lblSeparator = new Label("  ||  ");
         fpConfig.setAlignment(Pos.CENTER);
         fpConfig.getChildren().addAll(btnSaveConfig, lblSeparator, btnResetConfig);
      
      // Server IP selection
      FlowPane fpServerIp = new FlowPane(5,5);
         fpServerIp.setAlignment(Pos.CENTER);
         Label lblServerIp = new Label("Server IP: ");
         tfServerIp.setPromptText("Server IP (1 to 20 char)");
         tfServerIp.setText(presetTextServerIP);
         fpServerIp.getChildren().add(tfServerIp);
      
      // Server log in (password)
      FlowPane fpServerPassword = new FlowPane(5,5);
         fpServerPassword.setAlignment(Pos.CENTER);
         Label lblServerPassword = new Label("Password: ");
         tfServerPassword.setPromptText("Password (1 to 20 char)");
         fpServerPassword.getChildren().addAll(tfServerPassword);
      
      // Nickname
      FlowPane fpClientName = new FlowPane(5,5);
         fpClientName.setAlignment(Pos.CENTER);
         Label lblClientName = new Label("Nickname: ");
         tfClientName.setPromptText("Nickname (1 to 10 char)");
         if(presetTextNickname.trim().length() > 0)
         {
            tfClientName.setText(presetTextNickname);
         }
         fpClientName.getChildren().add(tfClientName);
      
      // Color Select
      VBox vbColorSelect = new VBox(3);
         FlowPane fpColorSelectLabel = new FlowPane();
         Label lblColorSelect = new Label("Car Color");
         fpColorSelectLabel.setAlignment(Pos.CENTER);
         fpColorSelectLabel.getChildren().add(lblColorSelect);
         fpColorSelect = new FlowPane(5, 5);
         fpColorSelect.setAlignment(Pos.CENTER);
         tfColorSelect.setDisable(true);
         tfColorSelect.setText(selectedColor);
         fpColorSelect.getChildren().addAll(btnPrev, tfColorSelect, btnNext);
      vbColorSelect.getChildren().addAll(fpColorSelectLabel, fpColorSelect);
      
      // Exit button
      FlowPane fpExit = new FlowPane();
         fpExit.setAlignment(Pos.CENTER);
         fpExit.getChildren().add(btnExit);
      
      // Set on action
      btnStart.setOnAction(ae);
      btnSaveConfig.setOnAction(ae);
      btnResetConfig.setOnAction(ae);
      btnPrev.setOnAction(ae);
      btnNext.setOnAction(ae);
      btnExit.setOnAction(ae);
      
      // Make 'waiting for players' message invisible
      lblWaitingForPlayers.setVisible(false);
      
      // Root pane
      root = new VBox(20);
      root.setAlignment(Pos.CENTER);
      root.getChildren().addAll(fpLabel, lblWaitingForPlayers, /*fpTest,*/ fpStart, fpConfig, fpServerIp, fpServerPassword, fpClientName, /*fpOptions,*/ vbColorSelect, fpExit);
      
      // Scene
      titleScreen = new Scene(root, screenWidth, screenHeight);
   }
   
   /** 
   * Disables all of the interactive elements on the scene while game client is connecting/connected to the game server
   *
   * @param _switch disable state of interactive elements
   */
   public static void disableInterface(boolean _switch)
   {
      btnSaveConfig.setDisable(_switch);
      btnResetConfig.setDisable(_switch);
      tfServerIp.setDisable(_switch);
      tfServerPassword.setDisable(_switch);
      tfClientName.setDisable(_switch);
      btnPrev.setDisable(_switch);
      btnNext.setDisable(_switch);
      btnExit.setDisable(_switch);
   }
}