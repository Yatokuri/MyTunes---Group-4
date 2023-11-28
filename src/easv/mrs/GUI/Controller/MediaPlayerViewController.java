package easv.mrs.GUI.Controller;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.PlaylistModel;
import easv.mrs.GUI.Model.SongModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MediaPlayerViewController implements Initializable {
    @FXML
    public TableView<Playlist> tblPlaylist;
    @FXML
    public TableColumn<Playlist, String> colPlaylistName;
    @FXML
    public TableColumn<Playlist, Integer> colSongCount;

    @FXML
    public TableColumn<Playlist, String> colSongTime;

    @FXML
    private TableView<Song> tblSongsInPlaylist;
    @FXML
    private TableColumn<Song, String> colTitlePlaylist;
    @FXML
    private TableColumn<Song, String> colArtistPlaylist;
    @FXML
    private TableColumn<Song, Integer> colYearPlaylist;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ImageView btnPlayIcon;
    @FXML
    private Button btnCreate, btnUpdate, btnPlay, btnDelete;
    @FXML
    private TextField txtName, txtArtist, txtYear, txtSongSearch;
    @FXML
    private Label lblPlayingNow, lblSongDuration, lblCurrentSongProgress, lblVolume;
    @FXML
    private Slider sliderProgressSong, sliderProgressVolume;
    @FXML
    private TableColumn<Song, String> colName, colArtist;
    @FXML
    private TableColumn<Song, Integer> colYear;
    @FXML
    private TableView<Song> tblSongs;
    @FXML

    private MediaPlayer currentMusic = null;
    private final Map<Integer, MediaPlayer> soundMap = new HashMap<>(); //Every song has a unique id
    private boolean isUserChangingSlider = false;
    private boolean isMusicPaused = false;
    private SongModel songModel;
    private PlaylistModel playlistModel;

    public MediaPlayerViewController()  {
        try {
            songModel = new SongModel();
            playlistModel = new PlaylistModel();

        }
        catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        // Initialize the person tables with columns.
        colName.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));

        colPlaylistName.setCellValueFactory(new PropertyValueFactory<>("playlistName"));
        colSongCount.setCellValueFactory(new PropertyValueFactory<>("songCount"));
        colSongTime.setCellValueFactory(new PropertyValueFactory<>("SongLengthHHMMSS"));

        colTitlePlaylist.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtistPlaylist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colYearPlaylist.setCellValueFactory(new PropertyValueFactory<>("year"));

        // add data from observable list
        tblSongs.setItems(songModel.getObservableSongs());
        tblPlaylist.setItems(playlistModel.getObservablePlaylists());


        tblPlaylist.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (tblPlaylist.getSelectionModel().getSelectedItem() == null)   {
                clearSearch();
            }
            else {
                txtName.setText(newValue.getPlaylistName());
                txtYear.setText("");
                txtArtist.setText("");
            }
        });

        tblSongsInPlaylist.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (tblPlaylist.getSelectionModel().getSelectedItem() == null)   {
                clearSearch();
            }
            else {
                txtName.setText(newValue.getTitle());
                txtArtist.setText(newValue.getArtist());
                txtYear.setText(Integer.toString(newValue.getYear()));
            }
        });
        // table view listener (when user selects a song in the tableview)
        tblSongs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (tblSongs.getSelectionModel().getSelectedItem() == null)   {
                clearSearch();
            }
            else {
                txtName.setText(newValue.getTitle());
                txtArtist.setText(newValue.getArtist());
                txtYear.setText(Integer.toString(newValue.getYear()));
            }
        });
        // set default volume to 50 and update song progress
        sliderProgressVolume.setValue(0.5);
        setVolume();
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

        // Add tableview functionality
        playSongFromTableView();

    }
    private void clearSearch(){
        txtName.setText("");
        txtArtist.setText("");
        txtYear.setText("");
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
        double songTime = 0;
        int year = Integer.parseInt(txtYear.getText());

        // create movie object to pass to method
        Song newSong = new Song(-1, year, title, artist, songPath, songTime);

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


    private void playSongFromTableView() {
        tblSongs.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();


                if (selectedSong != null && currentMusic != soundMap.get(selectedSong.getId())) {
                    sliderProgressSong.setValue(0);
                    playSong();
                }
            }
        });
    }

    private void playSongFromTableViewPlaylist() {
        tblSongsInPlaylist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                Song selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();


                if (selectedSong != null && currentMusic != soundMap.get(selectedSong.getId())) {
                    sliderProgressSong.setValue(0);
                    playSong();
                }
            }
        });
    }

    public void playSong()    {
        if (tblSongs.getSelectionModel().getSelectedItem() != null) {
            MediaPlayer newSong = soundMap.get(tblSongs.getSelectionModel().getSelectedItem().getId());
            if (currentMusic != newSong && newSong != null) {
                if (currentMusic != null) {
                    currentMusic.stop();
                }
                sliderProgressSong.setDisable(false);
                currentMusic = newSong;

                sliderProgressSong.setMax(newSong.getTotalDuration().toSeconds()); //Set our progress to the time so, we know maximum value
                lblPlayingNow.setText("Now playing: " + tblSongs.getSelectionModel().getSelectedItem().getTitle() + " - " + tblSongs.getSelectionModel().getSelectedItem().getArtist());
                currentMusic.seek(Duration.ZERO); //When you start a song again it should start from start
                currentMusic.play();
                btnPlayIcon.setImage(new Image("Icons/pause.png"));


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
                    updateSongProgressTimer();
                    btnPlayIcon.setImage(new Image("Icons/play.png"));
                });
                return;
            }
        }
            if (currentMusic != null) {
                if (currentMusic.getStatus() == MediaPlayer.Status.PLAYING) { //If it was paused now play
                    currentMusic.pause();
                    isMusicPaused = true;
                    btnPlayIcon.setImage(new Image("Icons/play.png"));
                    //currentMusic.seek(Duration.ZERO); //Mark these two out, and then you can now pause when you use the button instead of start again
                    //currentMusic.play();
                } else {
                    currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()));
                    currentMusic.play();
                    isMusicPaused = false;
                    btnPlayIcon.setImage(new Image("Icons/pause.png"));
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
        if (currentMusic != null) {
            currentMusic.setVolume((sliderProgressVolume.getValue()));
            double progress = sliderProgressVolume.getValue();
            int percentage = (int) (progress * 100);
            lblVolume.setText(String.format("%d%%", percentage));
        }
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

        tblPlaylist.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    tblSongsInPlaylist.getItems().clear();

                    playlistModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());

                    tblSongsInPlaylist.setItems(playlistModel.getObservablePlaylistsSong());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                tblSongsInPlaylist.getItems().clear();
            }
        });

        sliderProgressVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
            setSliderVolumeStyle();
            setVolume();
        });

        sliderProgressSong.valueProperty().addListener((obs, oldVal, newVal) -> {
            setSliderSongProgressStyle();
            updateSongProgressTimer();
        });


    }

    private void updateSongProgressTimer() {
        if (currentMusic != null) {
            double progressValue = sliderProgressSong.getValue();
            long currentSeconds = (long) progressValue;
            lblCurrentSongProgress.setText(String.format("%02d:%02d:%02d", currentSeconds / 3600, (currentSeconds % 3600) / 60, currentSeconds % 60)); //Format HH:MM:SS
            Duration totalDuration = currentMusic.getTotalDuration();
            long totalSeconds = (long) totalDuration.toSeconds();
            lblSongDuration.setText(String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60)); //Format HH:MM:SS
        }
        else {
            lblCurrentSongProgress.setText("00:00:00");
            lblSongDuration.setText("00:00:00");
        }
    }

    public void testNewWindowCreate() throws IOException {
        testNewWindow("Song Creator");
        MediaPlayerCUViewController.setTypeCU(1);
    }

    public void testNewWindowUpdate() throws IOException {
        testNewWindow("Song Updater");
        MediaPlayerCUViewController.setTypeCU(2);
    }


    public void testNewWindow(String windowTitle) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MediaPlayerCU.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.getIcons().add(new Image("/Icons/mainIcon.png"));
        stage.setTitle(windowTitle + " NOT WORKING");
        stage.setScene(new Scene(root));
        //stage.setMaximized(true);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the first window until second is close
        stage.show();
    }
}