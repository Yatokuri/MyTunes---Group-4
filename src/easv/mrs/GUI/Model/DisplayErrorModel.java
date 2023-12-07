package easv.mrs.GUI.Model;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import javafx.scene.control.Alert;

public class DisplayErrorModel {


    public void displayError(Throwable t) {
        if (t instanceof SQLServerException) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Something went wrong");
            alert.setHeaderText("The database could not be reach, try again");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Something went wrong");
            alert.setHeaderText(t.getMessage());
            alert.showAndWait();
        }
    }
}
