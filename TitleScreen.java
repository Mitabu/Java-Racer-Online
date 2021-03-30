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
   private static TextField tfServerIp = null;
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
   public static Scene getScene(EventHandler<ActionEvent> _ae, int _screenWidth, int _screenHeight, TextField _tfServerIp, Button _btnStart)
   {
      // Set scene parameters
      ae = _ae;
      screenWidth = _screenWidth;
      screenHeight = _screenHeight;
      
      // Get elements from the main class
      tfServerIp = _tfServerIp;
      btnStart = _btnStart;
      
      createTitleScreen();
      
      return titleScreen;
   }
   
   /** Sets up the title screen scene and its elements*/
   private static void createTitleScreen()
   {
      // LABEL
      FlowPane fpLabel = new FlowPane();
         fpLabel.setAlignment(Pos.CENTER);
         Label lbl = new Label("THIS GUI DESIGN IS A PROTOTYPE AND WILL BE CHANGED IN LATER DEVELOPMENT STAGES");
         fpLabel.getChildren().add(lbl);
      // Server selection
      FlowPane fpServerIp = new FlowPane(5,5);
         fpServerIp.setAlignment(Pos.CENTER);
         Label lblServerIp = new Label("Server IP: ");
         tfServerIp.setText("127.0.0.1");
         fpServerIp.getChildren().addAll(lblServerIp, tfServerIp);
      // Start
      FlowPane fpStart = new FlowPane();
         fpStart.setAlignment(Pos.CENTER);
         fpStart.getChildren().add(btnStart);
         
      // Options
      FlowPane fpOptions = new FlowPane();
         fpOptions.setAlignment(Pos.CENTER);
         fpOptions.getChildren().add(btnOptions);
      
      // Exit
      FlowPane fpExit = new FlowPane();
         fpExit.setAlignment(Pos.CENTER);
         fpExit.getChildren().add(btnExit);
      
      // Set on action
      btnStart.setOnAction(ae);
      btnOptions.setOnAction(ae);
      btnExit.setOnAction(ae);
      
      // Root
      root = new VBox(20);
      root.setAlignment(Pos.CENTER);
      root.getChildren().addAll(fpLabel, fpServerIp, fpStart, fpOptions, fpExit);
      
      // Scene
      titleScreen = new Scene(root, screenWidth, screenHeight);
   }
}