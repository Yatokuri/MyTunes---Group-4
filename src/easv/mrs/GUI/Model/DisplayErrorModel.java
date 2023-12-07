package easv.mrs.GUI.Model;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DisplayErrorModel {
    private static final Image programIcon = new Image ("/Icons/mainIcon.png");




    public void displayError(Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setIcon(alert);

        if (t instanceof SQLServerException) {
            alert.setTitle("Something went wrong");
            alert.setHeaderText("The database could not be reached. Please try again.");
            alert.showAndWait();
        } else {
            alert.setTitle("Something went wrong");
            alert.setHeaderText(t.getMessage());
            alert.showAndWait();
        }
    }

    private void setIcon(Dialog<?> dialog) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(programIcon);
    }


}
