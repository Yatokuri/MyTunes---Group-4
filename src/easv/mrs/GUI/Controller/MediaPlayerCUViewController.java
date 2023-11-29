package easv.mrs.GUI.Controller;

import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.SongModel;
import easv.mrs.GUI.Model.ValidateModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaPlayerCUViewController implements Initializable {
    @FXML
    private TextField lblTime;
    @FXML
    private ComboBox<String> comCategory;
    @FXML
    private TextField txtInputName, txtInputArtist, txtInputYear, txtInputFilepath;
    @FXML
    private Button btnCancel, btnSave, btnChoose;

    private MediaPlayerViewController mediaPlayerViewController;
    private long currentSongLength;
    private final SongModel songModel;
    private final ValidateModel validateModel = new ValidateModel();
    private final BooleanProperty isNameValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isArtistValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isFilepathValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isYearValid = new SimpleBooleanProperty(true);



    private static int typeCU = 0;
    private static Song currentSelectedSong = null;
    private static MediaPlayerCUViewController instance;




    public int getTypeCU() {
        return typeCU;
    }
    public static void setTypeCU(int typeCU) {MediaPlayerCUViewController.typeCU = typeCU;}


    public MediaPlayerCUViewController() {
        try {
            songModel = new SongModel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MediaPlayerCUViewController getInstance() {
        if (instance == null) {
            instance = new MediaPlayerCUViewController();
        }
        return instance;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mediaPlayerViewController = MediaPlayerViewController.getInstance();
        currentSelectedSong = mediaPlayerViewController.getCurrentSong();

        if (currentSelectedSong == null)  {
            typeCU = 1; //If user forgot to choose a song to update they can create instead

            Stage parent = (Stage) txtInputYear.getScene().getWindow();
            parent.setTitle("Song Creator" + " NOT WORKING");
        }

        addValidationListener(txtInputName, isNameValid);
        addValidationListener(txtInputArtist, isArtistValid);
        addValidationListener(txtInputFilepath, isFilepathValid);
        addValidationListener(txtInputYear, isYearValid);

        //Should be loaded for database?
        comCategory.getItems().addAll("Pop", "Rock", "Disco");

        // Add a listener to the filepath input to make sure its valid and update time automatic
        txtInputFilepath.textProperty().addListener((observable, oldValue, newValue) -> {
            if (validateModel.isValidMediaPath(newValue)) {
                updateTimeText();
            } else {
                lblTime.setText("00:00:00"); //Also mean not valid file
            }
        });
        startupSetup();
    }

    private void addValidationListener(TextField textField, BooleanProperty validationProperty) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = validateModel.validateInput(textField, newValue);
            validationProperty.set(isValid);
            setBorderStyle(textField, isValid);
        });
    }



    public void startupSetup() {
        if (typeCU == 1) //1 mean create song
            btnSave.setText("Create");
        if (typeCU == 2 & currentSelectedSong != null) { //2 mean update song
            btnSave.setText("Update");
            txtInputName.setText(currentSelectedSong.getTitle());
            txtInputYear.setText(String.valueOf(currentSelectedSong.getYear()));
            txtInputArtist.setText(currentSelectedSong.getArtist());
            txtInputFilepath.setText(currentSelectedSong.getSongPath());
            updateTimeText();
        }
    }



    public void save() {
        // Validate all inputs before saving
        boolean isNameValid = validateModel.validateInput(txtInputName, txtInputName.getText());
        boolean isArtistValid = validateModel.validateInput(txtInputArtist, txtInputArtist.getText());
        boolean isFilepathValid = validateModel.validateInput(txtInputFilepath, txtInputFilepath.getText());
        boolean isYearValid = validateModel.validateInput(txtInputYear, txtInputYear.getText());

        if (isNameValid && isArtistValid && isFilepathValid && isYearValid) {
            if (this.getTypeCU() == 1) {
                createNewSong();
            }
            if (this.getTypeCU() == 2) {
                updateSong();
            }
        }
    }

    private void createNewSong() {
        String title = txtInputName.getText();
        String artist = txtInputArtist.getText();
        String songPath = txtInputFilepath.getText();
        double songTime = currentSongLength;
        int year = Integer.parseInt(txtInputYear.getText());

        Song newSong = new Song(-1, year, title, artist, songPath, songTime);

        try {
            songModel.createNewSong(newSong);
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSong() {
        if (currentSelectedSong != null) {
            currentSelectedSong.setTitle(txtInputName.getText());
            currentSelectedSong.setArtist(txtInputArtist.getText());
            currentSelectedSong.setYear(Integer.parseInt(txtInputYear.getText()));
            currentSelectedSong.setSongPath(txtInputFilepath.getText());

            try {
                songModel.updateSong(currentSelectedSong);
                closeWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeWindow() throws Exception {
        Stage parent = (Stage) txtInputYear.getScene().getWindow();
        mediaPlayerViewController.refreshEverything();
        parent.close();
    }

    public void btnChoose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"));//Have a place where all valid format is stored
        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            txtInputFilepath.setText(selectedFile.getAbsolutePath());  // Get the selected file path and save it
            updateTimeText();
        }
    }

    private void updateTimeText() { //We pass the info to ValidateModel class
        MediaPlayer newSong = new MediaPlayer(new Media(new File(txtInputFilepath.getText()).toURI().toString()));
        validateModel.updateTimeText(newSong, formattedTime   -> { //This is because we need to wait because setOnReady is an asynchronous operation,
            String[] parts = formattedTime.split("-"); //We need to split the return because we got time in HH:MM:SS and just seconds
            lblTime.setText(parts[0]);
            currentSongLength = Long.parseLong(parts[1]);
        });
    }


    private void setBorderStyle(TextField textField, boolean isValid) {
        if (isValid) {
            textField.setStyle("-fx-border-color: green; -fx-border-width: 1px; -fx-effect: null;"); // Valid style
        } else {
            textField.setStyle("-fx-border-color: #010c1e; -fx-border-width: 10px; -fx-effect: innershadow( three-pass-box, rgb(245,0,0), 3, 5, 0, 0);"); // Invalid style
        }
    }


}
