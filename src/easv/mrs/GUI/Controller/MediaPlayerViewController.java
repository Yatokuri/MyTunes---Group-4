package easv.mrs.GUI.Controller;

import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.SongModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MediaPlayerViewController implements Initializable {


    public TextField txtSongSearch;
    public ListView<Song> lstSongs;
    public TableView tblMovies;
    public Button btnDelete;
    public Slider sliderProgressSong;
    public Button btnPlay;
    public Slider sliderProgressVolume;
    public Label lblPlayingNow;
    public AnchorPane anchorPane;


    private MediaPlayer currentMusic = null;
    private final Map<Integer, MediaPlayer> soundMap = new HashMap<>(); //Every song have there unique id
    private boolean isUserChangingSlider = false;
    private boolean isMusicPaused = false;
    @FXML
    private Button btnCreate, btnUpdate;

    @FXML
    private TableColumn<Song, String> colName;
    @FXML
    private TableColumn<Song, Integer> colYear;
    @FXML
    private TableColumn<Song, String> colArtist;

    @FXML
    private TableView<Song> tblSongs;

    @FXML
    private TextField txtName, txtArtist, txtYear;

    private SongModel songModel;

    public MediaPlayerViewController()  {

        try {
            songModel = new SongModel();
        }
        catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        // Initialize the person table with the two columns.
        colName.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));

        // add data from observable list
        lstSongs.setItems(songModel.getObservableSongs());
        tblSongs.setItems(songModel.getObservableSongs());

        // table view listener (when user selects a movie in the tableview)
        tblSongs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtName.setText(newValue.getTitle());
            txtArtist.setText(newValue.getArtist());
            txtYear.setText(Integer.toString(newValue.getYear()));
        });

        // list view listener (when user selects a movie in the listview)
        lstSongs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtName.setText(newValue.getTitle());
            txtArtist.setText(newValue.getArtist());
            txtYear.setText(Integer.toString(newValue.getYear()));
        });


        updateProgressStyle();

        for (Song s: songModel.getObservableSongs()) {
            if (s.getSongPath() != null) {
                soundMap.put(s.getId(), new MediaPlayer(new Media(new File(s.getSongPath()).toURI().toString())));
            }
        }

        // Setup context search
        txtSongSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
            try {
                songModel.searchSong(newValue);
            } catch (Exception e) {
                displayError(e);
                e.printStackTrace();
            }
        });
    }

    /**
     *
     * @param t
     */
    private void displayError(Throwable t)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(t.getMessage());
        alert.showAndWait();
    }

    /**
     *
     * @param actionEvent
     */
    public void createNewMovie(ActionEvent actionEvent) {
        // get data from UI
        String title = txtName.getText();
        String artist = txtArtist.getText();
       // String songPath = txtSongPath.getText();
        String songPath = "";
        int year = Integer.parseInt(txtYear.getText());

        // create movie object to pass to method
        Song newSong = new Song(-1, year, title, artist, songPath);

        try {
            songModel.createNewSong(newSong);
        }
        catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }


    /**
     *
     * @param actionEvent
     */
    public void updateSong(ActionEvent actionEvent) {
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();

        if (selectedSong != null)
        {
            // update movie based on textfield inputs from user
            selectedSong.setTitle(txtName.getText());
            selectedSong.setYear(Integer.parseInt(txtYear.getText()));

            try {
                // Update song in DAL layer (through the layers)
                songModel.updateSong(selectedSong);

                // ask controls to refresh their content
                lstSongs.refresh();
                tblSongs.refresh();
            }
            catch (Exception e) {
                displayError(e);
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param actionEvent
     */
    public void deleteSong(ActionEvent actionEvent) {
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();

        if (selectedSong != null)
        {
            try {
                // Delete movie in DAL layer (through the layers)
                songModel.deleteSong(selectedSong);
            }
            catch (Exception e) {
                displayError(e);
                e.printStackTrace();
            }
        }
    }




    public void playSong()    {
        if (tblSongs.getSelectionModel().getSelectedItem() != null)   {
            MediaPlayer newSong = soundMap.get(tblSongs.getSelectionModel().getSelectedItem().getId());
            if (currentMusic != newSong && newSong != null)   {
                if (currentMusic != null)   {
                    currentMusic.stop();
                }
                sliderProgressSong.setDisable(false);
                currentMusic = newSong;

                sliderProgressSong.setMax(newSong.getTotalDuration().toSeconds()); //Set our progress to the time so, we know maximum value
                lblPlayingNow.setText("Playing " + tblSongs.getSelectionModel().getSelectedItem().getTitle() );
                currentMusic.seek(Duration.ZERO); //When you start a song again it should start from start
                currentMusic.play();


                currentMusic.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                    // Update the slider value as the song progresses
                    if (!isUserChangingSlider) {
                        sliderProgressSong.setValue(newValue.toSeconds());
                    }
                });

                currentMusic.setOnEndOfMedia(() -> { //Do these when song is finish
                    currentMusic = null;
                    sliderProgressSong.setValue(0);
                    lblPlayingNow.setText("No song playing");
                    sliderProgressSong.setDisable(true);
                });
            }

            else if (currentMusic != null) {
                if (currentMusic.getStatus() == MediaPlayer.Status.PLAYING) { //If it was paused now play
                    currentMusic.pause();
                    isMusicPaused = true;
                    //currentMusic.seek(Duration.ZERO); //Mark these two out, and then you can now pause when you use the button instead of start again
                    //currentMusic.play();
                } else {
                    currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()));
                    currentMusic.play();
                    isMusicPaused = false;
                }
            }
        }
    }

    public void onSlideProgressPressed() {
        if (currentMusic != null) {
            isUserChangingSlider = true;
            currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()));
            currentMusic.pause();
        }
    }

    public void onSlideProgressReleased() {
        if (currentMusic != null) {
            if (!isMusicPaused) {
                currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()));
                currentMusic.play();
                isUserChangingSlider = false;
                anchorPane.requestFocus();
            }

        }
    }
    
    public void setVolume() {
        if (currentMusic != null)
            currentMusic.setVolume((sliderProgressVolume.getValue()));
    }

        private void setSliderVolumeStyle()  {
            double percentage = sliderProgressVolume.getValue() / (sliderProgressVolume.getMax() - sliderProgressVolume.getMin());
            String color = String.format(Locale.US, "-fx-background-color: linear-gradient(to right, blue 0%%, blue %.2f%%, white %.2f%%, white 100%%);", percentage * 100, percentage * 100);
            sliderProgressVolume.lookup(".track").setStyle(color);
        }

        private void setSliderSongProgressStyle()  {
            double percentage = sliderProgressSong.getValue() / (sliderProgressSong.getMax() - sliderProgressSong.getMin());
            String color = String.format(Locale.US, "-fx-background-color: linear-gradient(to right, green 0%%, green %.10f%%, red %.10f%%, red 100%%);", percentage * 100, percentage * 100);
            sliderProgressSong.lookup(".track").setStyle(color);
        }

    private void updateProgressStyle() { //This automatic change the progress bar

        Timeline updater = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            setSliderVolumeStyle();
            setSliderSongProgressStyle();
        }));
        //updater.setCycleCount(Timeline.INDEFINITE); //The way to run it so many times u want
        updater.play();

        sliderProgressVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
            setSliderVolumeStyle();
            setVolume();
        });

        sliderProgressSong.valueProperty().addListener((obs, oldVal, newVal) -> {
            setSliderSongProgressStyle();
        }); 


    }

}

