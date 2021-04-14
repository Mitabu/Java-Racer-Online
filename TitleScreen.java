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
*  @version 16.03.2021
*/

/** Creates title screen scene*/
public class TitleScreen
{
   // Attributes
         // ROOT
   private static VBox root;
         // GUI
   private static Button btnExit = new Button("Exit");
   private static Button btnOptions = new Button("Options");
   private static Button btnStart = null;
   private static Button btnTest = new Button("TEST");
   private static Button btnSaveConfig = new Button("Save Current Configuration");
   private static Label lblWaitingForPlayers = new Label("Connected to the server. The game will start as soon as all players connect.");
         // Log In
   private static TextField tfServerIp = null;
   private static String presetTextServerIP;
   private static TextField tfServerPassword = null;
   private static TextField tfClientName = null;
   private static String presetTextNickname;
         // Car Color Select
   private static FlowPane fpColorSelect;
   private static Button btnPrev = new Button("Prev. color");
   private static TextField tfColorSelect = null;
   private static String selectedColor;
   private static Button btnNext = new Button("Next color");
         // Title Screen Scene
   private static Scene titleScreen;
   private static int screenWidth;
   private static int screenHeight;
      
      // EventHandler
   private static EventHandler<ActionEvent> ae;
   
   /**
   * Sets up and returns the title screen scene
   *
   * @param _ae an event handler object for the options scene buttons
   * @param _screenWidth the width of the application window in pixels
   * @param _screenHeight the height of the application window in pixels
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
   
   public static void showConnectionMessage()
   {
      lblWaitingForPlayers.setVisible(true);
   }
   
   /** Sets up the title screen scene and its elements*/
   private static void createTitleScreen()
   {
      // LABEL
      FlowPane fpLabel = new FlowPane();
         fpLabel.setAlignment(Pos.CENTER);
         Label lbl = new Label("Java Racer Online");
         lbl.setStyle("-fx-font-size: 40px; -fx-font-weight: bold");
         fpLabel.getChildren().add(lbl);
      // Server selection
      FlowPane fpServerIp = new FlowPane(5,5);
         fpServerIp.setAlignment(Pos.CENTER);
         Label lblServerIp = new Label("Server IP: ");
         tfServerIp.setPromptText("Server IP (1 to 20 char)");
         tfServerIp.setText(presetTextServerIP);
         fpServerIp.getChildren().addAll(/*lblServerIp,*/ tfServerIp);
      // Server log in
      FlowPane fpServerPassword = new FlowPane(5,5);
         fpServerPassword.setAlignment(Pos.CENTER);
         Label lblServerPassword = new Label("Password: ");
         tfServerPassword.setPromptText("Password (1 to 20 char)");
         fpServerPassword.getChildren().addAll(/*lblServerPassword,*/ tfServerPassword);
      // Nickname
      FlowPane fpClientName = new FlowPane(5,5);
         fpClientName.setAlignment(Pos.CENTER);
         Label lblClientName = new Label("Nickname: ");
         tfClientName.setPromptText("Nickname (1 to 10 char)");
         if(presetTextNickname.trim().length() > 0)
         {
            tfClientName.setText(presetTextNickname);
         }
         fpClientName.getChildren().addAll(/*lblClientName,*/ tfClientName);
      // TEST
      FlowPane fpTest = new FlowPane();
         fpTest.setAlignment(Pos.CENTER);
         fpTest.getChildren().add(btnTest);
      
      // Start
      FlowPane fpStart = new FlowPane();
         fpStart.setAlignment(Pos.CENTER);
         fpStart.getChildren().add(btnStart);
      // Save configuration
      FlowPane fpSaveConfig = new FlowPane();
         fpSaveConfig.setAlignment(Pos.CENTER);
         fpSaveConfig.getChildren().add(btnSaveConfig);
         
      // Options
      //FlowPane fpOptions = new FlowPane();
      //   fpOptions.setAlignment(Pos.CENTER);
      //   fpOptions.getChildren().add(btnOptions);
      
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
      
      // Exit
      FlowPane fpExit = new FlowPane();
         fpExit.setAlignment(Pos.CENTER);
         fpExit.getChildren().add(btnExit);
      
      // Set on action
      btnTest.setOnAction(ae);
      btnStart.setOnAction(ae);
      btnSaveConfig.setOnAction(ae);
      btnPrev.setOnAction(ae);
      btnNext.setOnAction(ae);
      btnOptions.setOnAction(ae);
      btnExit.setOnAction(ae);
      
      lblWaitingForPlayers.setVisible(false);
      
      // Root
      root = new VBox(20);
      root.setAlignment(Pos.CENTER);
      root.getChildren().addAll(fpLabel, lblWaitingForPlayers, /*fpTest,*/ fpStart, fpSaveConfig, fpServerIp, fpServerPassword, fpClientName, /*fpOptions,*/ vbColorSelect, fpExit);
      
      // Scene
      titleScreen = new Scene(root, screenWidth, screenHeight);
   }
}