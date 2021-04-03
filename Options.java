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

/** Creates and updates options scene*/
public class Options
{
   // Attributes
         // ROOT
   private static VBox root;
         // GUI
               // Car Color Select
   private static FlowPane fpColorSelect;
   private static Button btnPrev = new Button("Prev. color");
   private static TextField tfColorSelect = null;
   private static String selectedColor;
   private static Button btnNext = new Button("Next color");
               // Back
   private static FlowPane fpBack;
   private static Button btnBack = new Button("Back to Title");
         // Title Screen Scene
   private static Scene options;
   private static int screenWidth;
   private static int screenHeight;
      
      // EventHandler
   private static EventHandler<ActionEvent> ae;
   
   /**
   * Constructor
   *
   * @param _ae an event handler object for the options scene buttons
   * @param _screenWidth the width of the application window in pixels
   * @param _screenHeight the height of the application window in pixels
   * @param _tfColorSelect textfield for displaying car color selection
   * @param _selectedColor car color selected during options scene initialization
   */
   public Options (EventHandler<ActionEvent> _ae, int _screenWidth, int _screenHeight, TextField _tfColorSelect, String _selectedColor)
   {
      ae = _ae;
      screenWidth = _screenWidth;
      screenHeight = _screenHeight;
      tfColorSelect = _tfColorSelect;
      selectedColor = _selectedColor;
      
      createOptionsMenu();
   }
   
   /** Returns the options scene*/
   public static Scene getScene()
   {
      return options;
   }
   
   /** Sets up the options scene and its elements*/
   private static void createOptionsMenu()
   {
      // Car Color Select
      fpColorSelect = new FlowPane();
         fpColorSelect.setAlignment(Pos.CENTER);
         tfColorSelect.setDisable(true);
         tfColorSelect.setText(selectedColor);
         fpColorSelect.getChildren().addAll(btnPrev, tfColorSelect, btnNext);
      
      // Back
      fpBack = new FlowPane();
         fpBack.setAlignment(Pos.CENTER);
         fpBack.getChildren().add(btnBack);
      
      // Set on action
      btnPrev.setOnAction(ae);
      btnNext.setOnAction(ae);
      btnBack.setOnAction(ae);
      
      // Root
      root = new VBox(20);
      root.setAlignment(Pos.CENTER);
      root.getChildren().addAll(fpColorSelect, fpBack);
      
      // Scene
      options = new Scene(root, screenWidth, screenHeight);
   }
}