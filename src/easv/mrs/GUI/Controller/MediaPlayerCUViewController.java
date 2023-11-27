package easv.mrs.GUI.Controller;

import easv.mrs.GUI.Model.SongModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaPlayerCUViewController implements Initializable {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ComboBox comCategory;
    @FXML
    private TextField txtInputName, txtInputArtist, txtInputYear, txtInputFilepath;

    @FXML
    private Button btnCancel, btnSave, btnChoose;

    private SongModel songModel; //SingleTon?

    private static int typeCU = 0;

    public int getTypeCU() {
        return typeCU;
    }

    public static void setTypeCU(int typeCU) {
        MediaPlayerCUViewController.typeCU = typeCU;
    }

    public MediaPlayerCUViewController()  {
        try {
            songModel = new SongModel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Should be loaded for database
        comCategory.getItems().add("Pop");
        comCategory.getItems().add("Rock");
        comCategory.getItems().add("Disco");
    }


    public void saveSong(ActionEvent actionEvent) {

       //typeCU 1 = C 2 = U
    }

    public void cancel(ActionEvent actionEvent) {
        Stage parent = (Stage) txtInputYear.getScene().getWindow();
        parent.close();
    }

    public void btnChoose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3","*.wav"));
        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath(); // Get the selected file path and save it
            System.out.println(filePath);
        }
    }

    public void cancelAction(ActionEvent actionEvent) {
        System.out.println(actionEvent.getSource());
        anchorPane.requestFocus();
    }
}






