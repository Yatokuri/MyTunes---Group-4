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
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MediaPlayerViewController implements Initializable {

    @FXML
    private TableView<Playlist> tblPlaylist;
    @FXML
    private TableView<Song> tblSongsInPlaylist, tblSongs;
    @FXML
    private TableColumn<Song, String> colTitlePlaylist, colArtistPlaylist, colName, colArtist, colSongTime, colPlaylistName;
    @FXML
    private TableColumn<Song, Integer> colYearPlaylist, colYear, colSongCount;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ImageView btnPlayIcon;
    @FXML
    private Button btnPlay, btnDelete, btnCreatePlaylist, btnUpdatePlaylist;
    @FXML
    private TextField txtName, txtArtist, txtSongSearch;
    @FXML
    private Label lblPlayingNow, lblSongDuration, lblCurrentSongProgress, lblVolume;
    @FXML
    private Slider sliderProgressSong, sliderProgressVolume;
    @FXML
    private MediaPlayer currentMusic = null;
    private final Map<Integer, MediaPlayer> soundMap = new HashMap<>(); //Every song has a unique id
    private boolean isUserChangingSlider = false;
    private boolean isMusicPaused = false;

    private Playlist currentPlaylist; //The current Playlist to be used
    private Song currentSong; //The current Song that gets used
    private SongModel songModel;
    private PlaylistModel playlistModel;
    private MediaPlayerCUViewController mediaPlayerCUViewController;

    public Song getCurrentSong() {return currentSong;}
    public void setCurrentSong(Song currentSong) {this.currentSong = currentSong;}

    private static MediaPlayerViewController instance;

    public MediaPlayerViewController()  {
        instance = this;

        try {
            songModel = new SongModel();
            playlistModel = new PlaylistModel();
            currentPlaylist = null;
        }
        catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    public static MediaPlayerViewController getInstance() {
        if (instance == null) {
            instance = new MediaPlayerViewController();
        }
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        mediaPlayerCUViewController = MediaPlayerCUViewController.getInstance();


        try {
            songModel = new SongModel();
            playlistModel = new PlaylistModel();
            currentPlaylist = null;
        }
        catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }


        // Initialize the tables with columns.
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
                txtArtist.setText("");
            }
        });

        tblSongsInPlaylist.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (tblSongsInPlaylist.getSelectionModel().getSelectedItem() == null && newValue == null)    {
                clearSearch();
            }
            else {
                txtName.setText(newValue.getTitle());
                txtArtist.setText(newValue.getArtist());;
            }
        });
        // table view listener (when user selects a song in the tableview)
        tblSongs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (tblSongs.getSelectionModel().getSelectedItem() == null && newValue == null)   {
                clearSearch();
            }
            else {
                currentSong = tblSongs.getSelectionModel().getSelectedItem();

                txtName.setText(newValue.getTitle());
                txtArtist.setText(newValue.getArtist());
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

        // Setup context search for the songs
        txtSongSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {

            if (txtSongSearch.getText().isEmpty())  {
                try {
                    tblPlaylist.getItems().clear();
                    tblPlaylist.setItems(playlistModel.updatePlaylistList());
                    tblPlaylist.refresh();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                songModel.searchSong(newValue);
                playlistModel.searchPlaylist(newValue);
            } catch (Exception e) {
                displayError(e);
                e.printStackTrace();
            }
        });

        // Add tableview functionality
        playSongFromTableViewPlaylist();
        playSongFromTableView();
        initializeDragAndDrop();
    }
    private void clearSearch(){
        txtName.setText("");
        txtArtist.setText("");
    }

    private void displayError(Throwable t)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(t.getMessage());
        alert.showAndWait();
    }

    public void refreshEverything() throws Exception {
        tblSongs.getItems().clear();
        tblSongs.setItems(songModel.updateSongList());

        if (currentPlaylist == null)   {
            currentPlaylist = playlistModel.getObservablePlaylists().getFirst();
        }

            playlistModel.playlistSongs(currentPlaylist);
        /*
        tblPlaylist.getItems().clear();
        tblPlaylist.setItems(playlistModel.updatePlaylistList());
         */

        tblSongsInPlaylist.setItems(playlistModel.getObservablePlaylistsSong());
        tblSongs.refresh();
        tblPlaylist.refresh();
        tblSongsInPlaylist.refresh();
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
           Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
           if (selectedSong != null) {
               MediaPlayer newSong = soundMap.get(selectedSong.getId());
            if (currentMusic != newSong && newSong != null) {
                handleNewSong(newSong, selectedSong);
                return;
                }
            }
        }
        if (currentMusic != null) {
            togglePlayPause();
        }
    }

    public void PlaySong(Song song) {
        MediaPlayer newMusic = soundMap.get(song.getId());
        handleNewSong(newMusic, song);
    }

    private boolean notLastSong, notFirstSong = true;

    public void nextSong() {
        TableView<Song> selectedTable = (tblPlaylist.getSelectionModel().getSelectedItem() == null) ? tblSongs : tblSongsInPlaylist;
        boolean isLastSong = selectedTable.getSelectionModel().isSelected(selectedTable.getItems().size() - 1);

        if (isLastSong && !notLastSong) {
            notLastSong = true;
            selectedTable.getSelectionModel().selectFirst();
        } else {
            notLastSong = false;
            selectedTable.getSelectionModel().selectNext();
        }

        PlaySong(selectedTable.getSelectionModel().getSelectedItem());
    }

    public void previousSong() {
        TableView<Song> selectedTable = (tblPlaylist.getSelectionModel().getSelectedItem() == null) ? tblSongs : tblSongsInPlaylist;
        boolean isFirstSong = selectedTable.getSelectionModel().isSelected(0);

        if (isFirstSong && !notFirstSong) {
            notFirstSong = true;
            selectedTable.getSelectionModel().selectLast();
        } else {
            notFirstSong = false;
            selectedTable.getSelectionModel().selectPrevious();
        }

        PlaySong(selectedTable.getSelectionModel().getSelectedItem());
    }





    private void togglePlayPause()   {
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


    private void handleNewSong(MediaPlayer newSong, Song selectedSong) {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        sliderProgressSong.setDisable(false);
        currentMusic = newSong;

        sliderProgressSong.setMax(newSong.getTotalDuration().toSeconds()); //Set our progress to the time so, we know maximum value
        lblPlayingNow.setText("Now playing: " + selectedSong.getTitle() + " - " + selectedSong.getArtist());
        currentMusic.seek(Duration.ZERO); //When you start a song again it should start from start
        currentMusic.play();
        btnPlayIcon.setImage(new Image("Icons/pause.png"));

        currentMusic.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            // Update the slider value as the song progresses
            if (!isUserChangingSlider) {
                sliderProgressSong.setValue(newValue.toSeconds());
            }
        });
        //Do these when song is finished
        currentMusic.setOnEndOfMedia(this::onEndOfSong);


    }

    public void onEndOfSong(){
            currentMusic = null;
            sliderProgressSong.setValue(0);
            lblPlayingNow.setText("No song playing");
            sliderProgressSong.setDisable(true);
            updateSongProgressTimer();
            btnPlayIcon.setImage(new Image("Icons/play.png"));

            nextSong();
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


                    playlistModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());

                    currentPlaylist = tblPlaylist.getSelectionModel().getSelectedItem();



                    refreshEverything();


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


    @FXML
    private void initializeDragAndDrop() {
        tblSongsInPlaylist.setOnDragDetected(event -> { //When user drag a song from playlist song list
            Song selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                Dragboard db = tblSongsInPlaylist.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(DataFormat.PLAIN_TEXT, Integer.toString(selectedSong.getId()));
                db.setContent(content);
                event.consume();
            }
        });

        tblSongs.setOnDragDetected(event -> { //When user drag a song from song list
            Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                Dragboard db = tblSongs.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(DataFormat.PLAIN_TEXT, Integer.toString(selectedSong.getId()));
                db.setContent(content);
                event.consume();
            }
        });

        tblSongsInPlaylist.setOnDragOver(event -> {
            if (event.getGestureSource() != tblSongsInPlaylist && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });


        anchorPane.setOnDragOver(event -> { // Allowing drop only if the source is tblSongsInPlaylist and has a string
            if (event.getGestureSource() == tblSongsInPlaylist && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
                event.consume();
        });



        tblSongsInPlaylist.setOnDragDropped(event -> { //When user drop a song from song list into playlist song
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                int songId = Integer.parseInt(db.getString());
                Song selectedSong = getSongById(songId);

                if (selectedSong != null) {
                    try {
                        if (playlistModel.addSongToPlaylist(selectedSong, currentPlaylist)) { //We first need to make sure it not already in the playlist
                            playlistModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());
                                tblPlaylist.refresh();
                                success = true;

                        }
                        else if (currentPlaylist == null) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Playlist System");
                            alert.setHeaderText("You need to choose a playlist");
                            alert.showAndWait();
                        }
                        else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Playlist System");
                            alert.setHeaderText("The song is already in the playlist");
                            alert.showAndWait();
                        }

                    } catch (Exception e) {
                        displayError(e);
                        e.printStackTrace();
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });


        anchorPane.setOnDragDropped(event -> { //When user drop a song from playlist song list into background
            Dragboard db = event.getDragboard();
            int songId = Integer.parseInt(db.getString());

            Song selectedSong = getSongById(songId);

            if (selectedSong != null) {
                currentPlaylist.setSongCount(currentPlaylist.getSongCount() - 1);
                currentPlaylist.setSongTotalTime(currentPlaylist.getSongTotalTime() - selectedSong.getSongLength());
                tblPlaylist.refresh();

                try {
                    playlistModel.deleteSongFromPlaylist(selectedSong, currentPlaylist);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            anchorPane.requestFocus();
            event.setDropCompleted(true);
            event.consume();
        });

        //Source Stackoverflow
   /**   tblSongsInPlaylist.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    int index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(Integer.toString(index));
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    if (row.isEmpty() || row.getIndex() != draggedIndex) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {

                    int draggedIndex = Integer.parseInt(db.getString());
                    int dropIndex = row.isEmpty() ? tblSongsInPlaylist.getItems().size() : row.getIndex();

                        if (dropIndex == tblSongsInPlaylist.getItems().size()) { //Make sure you can not drop a song to an empty spot
                            event.setDropCompleted(false);
                            event.consume();
                            return;
                        }

                        // Perform the reordering in your data model
                        moveSongInPlaylist(draggedIndex, dropIndex);
                        event.setDropCompleted(true);
                        tblSongsInPlaylist.getSelectionModel().select(dropIndex);
                        event.consume();


                }
            });

            return row;
        });

 **/
    }


    private Song getSongById(int songId) { //This is not right layer!
        for (Song s : songModel.getObservableSongs()) {
            if (s.getId() == songId) {
                return s;
            }
        }
        return null;
    }

    public void moveSongInPlaylist(int fromIndex, int toIndex) {
        // Get the selected song
        Song selectedSong = tblSongsInPlaylist.getItems().remove(fromIndex);

        System.out.println("Fra " + fromIndex + " Til " + toIndex + " Hvem? " + selectedSong);

        //Database skal opdatere eller bare gemme listen forfra hver gang?

        tblSongsInPlaylist.getItems().add(toIndex, selectedSong);
        tblSongsInPlaylist.refresh();
    }

//******************************BUTTONS*****************************
    public void testNewWindowCreate() throws IOException {
    MediaPlayerCUViewController.setTypeCU(1);
    testNewWindow("Song Creator");
    }

    public void testNewWindowUpdate() throws IOException {
        MediaPlayerCUViewController.setTypeCU(2);
        if (currentSong == null)  {
            testNewWindowCreate();
            return;
        }
        testNewWindow("Song Updater");
    }

    public void testNewWindow(String windowTitle) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MediaPlayerCU.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.getIcons().add(new Image("/Icons/mainIcon.png"));
        stage.setTitle(windowTitle);
        stage.setScene(new Scene(root));
        //stage.setMaximized(true);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the first window until second is close
        stage.show();
    }

    public void createUpdatePlaylist(ActionEvent event) throws Exception {
        Button clickedButton = (Button) event.getSource(); // Get the button that triggered the event
        String buttonText = clickedButton.getText(); // Get the text of the button
        TextInputDialog dialog = new TextInputDialog("");

        if (buttonText.equals(btnCreatePlaylist.getText())) {
            dialog.setTitle("New Playlist");
            dialog.setHeaderText("What do you want to call your new Playlist");
        }
        if (buttonText.equals(btnUpdatePlaylist.getText())) {
            dialog.setTitle("Update Playlist " + currentPlaylist);
            dialog.setHeaderText("What should the new name be for the Playlist");
        }

            // dialog.setGraphic(new ImageView(new Image("/Icons/mainIcon.png"))); //Maybe?

        // Set the icon for the dialog window
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/Icons/mainIcon.png"));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String inputValue = result.get(); // Get the actual value from Optional

            if (buttonText.equals(btnCreatePlaylist.getText())) {
                Playlist p = new Playlist(-1, inputValue, 0, 0);
                playlistModel.createNewPlaylist(p);
            }
            if (buttonText.equals(btnUpdatePlaylist.getText())) {
                currentPlaylist.setPlaylistName(inputValue);
                playlistModel.updatePlaylist(currentPlaylist);
            }
        }
        refreshEverything();
    }
    public void forwardSong(ActionEvent event) {
        nextSong();
    }

    public void backwardSong(ActionEvent event) {
        previousSong();
    }
    public void deleteBtn(ActionEvent actionEvent) {
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = tblPlaylist.getSelectionModel().getSelectedItem();
        Song selectedSongInPlaylist = tblSongsInPlaylist.getSelectionModel().getSelectedItem();

        if (selectedSong != null & selectedSongInPlaylist == null) {



            try {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Song");
                alert.setHeaderText("You want to delete " + currentSong.getTitle());
                alert.setContentText("Are you ok with this?");
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image("/Icons/mainIcon.png"));

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    songModel.deleteSong(selectedSong);
                    tblSongs.refresh();
                }

            }
            catch (Exception e) {
                displayError(e);
                e.printStackTrace();
            }
        }
        if (selectedPlaylist != null & selectedSongInPlaylist == null){
            try {
                playlistModel.deletePlaylist(selectedPlaylist);
                tblPlaylist.refresh();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (selectedSongInPlaylist != null){
            try {
                playlistModel.deleteSongFromPlaylist(selectedSongInPlaylist, selectedPlaylist);
                currentPlaylist.setSongCount(currentPlaylist.getSongCount() - 1);
                currentPlaylist.setSongTotalTime(currentPlaylist.getSongTotalTime() - selectedSong.getSongLength());
                refreshEverything();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


//**************************THIS*IS*ON*THE*RIGHT*LAYER*CONFIRMED***************************
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
}