import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
*  @author Artem Polkovnikov
*  @version 16.03.2021
*/

/** A set of function for displaying messages to the user*/
public class DisplayMessage
{
   /** 
   *  Shows an alert message to the user and waits
   *
   *  @param _at alert type
   *  @param _ht header text of the alert
   *  @param _ct content text of the alert
   */
   public static void showAlert(Stage _owner, AlertType _at, String _ht, String _ct)
   {
      Alert alert = new Alert(_at);
      alert.setHeaderText(_ht);
      alert.setContentText(_ct);
      alert.initOwner(_owner);
      
      alert.showAndWait();
   }
   
   /**
   * Shows a confirmation popup and returns true if OK button was pressed
   *
   *  @param _owner the stage on which the alert pop up is shown
   *  @param _ht header text of the alert
   */
   public static boolean showConfirmation(Stage _owner, String _ht)
   {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setHeaderText(_ht);
      alert.setContentText("Press OK to continue");
      alert.initOwner(_owner);
      
      Optional <ButtonType> result = alert.showAndWait();
      if(result.isPresent() && result.get() == ButtonType.OK)
      {
         return true;
      }
      return false;
   }
}