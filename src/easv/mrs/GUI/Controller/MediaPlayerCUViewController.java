package easv.mrs.GUI.Controller;

import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.SongModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class MediaPlayerCUViewController implements Initializable {
    @FXML
    private TextField lblTime;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ComboBox comCategory;
    @FXML
    private TextField txtInputName, txtInputArtist, txtInputYear, txtInputFilepath;

    @FXML
    private Button btnCancel, btnSave, btnChoose;
    private SongModel songModel; //SingleTon?
    private MediaPlayerViewController mediaPlayerViewController;

    private String filePath = "";
    private static int typeCU = 0;

    private static Song currentSong = null;
    private long currentSongLength;

    public int getTypeCU() {
        return typeCU;
    }

    public static void setTypeCU(int typeCU) {MediaPlayerCUViewController.typeCU = typeCU;}
    private static MediaPlayerCUViewController instance;

    public static MediaPlayerCUViewController getInstance() {
        if (instance == null) {
            instance = new MediaPlayerCUViewController();
        }
        return instance;
    }


    public MediaPlayerCUViewController() {
        try {
            songModel = new SongModel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mediaPlayerViewController = MediaPlayerViewController.getInstance();
        currentSong = mediaPlayerViewController.getCurrentSong();

        //Should be loaded for database
        comCategory.getItems().add("Pop");
        comCategory.getItems().add("Rock");
        comCategory.getItems().add("Disco");

        // Add a listener to the filepath input to make sure its valid and update time automatic
        txtInputFilepath.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidMediaPath(newValue)) {
                updateTimeText();
            } else {
                lblTime.setText("00:00:00"); //Also mean not valid file
            }
        });
        startupSetup();
    }

    public void startupSetup() {
        if (typeCU == 1) //1 means create song
            btnSave.setText("Create");
        if (typeCU == 2 & currentSong != null) { //2 means update song
            btnSave.setText("Update");
            txtInputName.setText(currentSong.getTitle());
            txtInputYear.setText(String.valueOf(currentSong.getYear()));
            txtInputArtist.setText(currentSong.getArtist());
            txtInputFilepath.setText(currentSong.getSongPath());
            updateTimeText();
        }
    }



    public void save(ActionEvent actionEvent) {
        // get data from UI
        //typeCU 1 = Create 2 = Update

        //Hvis dette er true er det en valid fil der er sat ind lblTime.getText().equals("00:00:00");
        //Tid skal gemmes i sekunder format

        if (this.getTypeCU() == 1) { //1 means new song
            String title = txtInputName.getText();
            String artist = txtInputArtist.getText();
            String songPath = txtInputFilepath.getText();
            double songTime = currentSongLength;
            int year = Integer.parseInt(txtInputYear.getText());

            // create movie object to pass to method
            Song newSong = new Song(-1, year, title, artist, songPath, songTime);

            try {
                songModel.createNewSong(newSong);
                cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.getTypeCU() == 2) { //2 means update song
            Song selectedSong = currentSong;

            if (selectedSong != null) {
                // update movie based on textfield inputs from user
                selectedSong.setTitle(txtInputName.getText());
                selectedSong.setArtist(txtInputArtist.getText());
                selectedSong.setYear(Integer.parseInt(txtInputYear.getText()));
                selectedSong.setSongPath(txtInputFilepath.getText());

                try {
                    // Update song in DAL layer (through the layers)
                    songModel.updateSong(selectedSong);

                    cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void cancel() throws Exception {
        Stage parent = (Stage) txtInputYear.getScene().getWindow();
        mediaPlayerViewController.refreshEverything();
        parent.close();
    }

    public void btnChoose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"));
        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            filePath = selectedFile.getAbsolutePath(); // Get the selected file path and save it
            txtInputFilepath.setText(filePath);
            updateTimeText();
        }
    }

    private void updateTimeText() { //Temp make it so a song to get duration
        MediaPlayer newSong = new MediaPlayer(new Media(new File(txtInputFilepath.getText()).toURI().toString()));
        newSong.setOnReady(() -> { //We need to wait to its read otherwise we got no value
            long totalSeconds = (long) newSong.getTotalDuration().toSeconds();

            currentSongLength = totalSeconds;

            lblTime.setText(String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60)); //Format HH:MM:SS
        });

    }

    private boolean isValidMediaPath(String path) {
        List<String> supportedExtensions = Arrays.asList("mp3", "wav"); //Have a place where all valid format is stored
        try {
            Path filePath = FileSystems.getDefault().getPath(path);
            String fileName = filePath.getFileName().toString();
            int lastIndexOf = fileName.lastIndexOf(".");
            String extension = (lastIndexOf != -1 && lastIndexOf != 0) ? fileName.substring(lastIndexOf + 1).toLowerCase() : "";

            return filePath.toFile().isFile() && supportedExtensions.contains(extension);
        } catch (Exception e) {
            return false;
        }
    }

}

