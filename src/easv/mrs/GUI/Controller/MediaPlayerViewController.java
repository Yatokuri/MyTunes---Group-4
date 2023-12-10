package easv.mrs.GUI.Controller;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.DisplayErrorModel;
import easv.mrs.GUI.Model.PlaylistModel;
import easv.mrs.GUI.Model.SongModel;
import easv.mrs.GUI.Model.SongPlaylistModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private TableColumn<Song, String> colTitleInPlaylist, colArtistInPlaylist, colName, colArtist, colPlaylistTime, colSongTime, colPlaylistName, colCategory;
    @FXML
    private TableColumn<Song, Integer> colYear, colSongCount;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ImageView btnPlayIcon, btnRepeatIcon, btnShuffleIcon;
    @FXML
    private Button btnCreatePlaylist, btnUpdatePlaylist;
    @FXML
    private TextField txtSongSearch;
    @FXML
    private Label lblPlayingNow, lblSongDuration, lblCurrentSongProgress, lblVolume;
    @FXML
    private Slider sliderProgressSong, sliderProgressVolume;
    @FXML
    private MediaPlayer currentMusic = null;
    private final Map<Integer, MediaPlayer> soundMap = new HashMap<>(); //Every song has a unique id
    private List<Song> currentSongList = new ArrayList<>();
    private boolean isUserChangingSlider = false;
    private boolean isMusicPaused = false;
    private float volume = 0.1F; //Default song volume
    private int repeatMode = 0; //Default repeat mode
    private int shuffleMode = 0; //Default shuffle
    private Playlist currentPlaylist, currentPlaylistPlaying; //The current playing selected and playing from
    private int currentIndex = 0;
    private Song currentSong, currentSongPlaying; //The current Song selected and playing
    private final SongModel songModel;
    private final PlaylistModel playlistModel;
    private final SongPlaylistModel songPlaylistModel;
    private final DisplayErrorModel displayErrorModel;
    private MediaPlayerCUViewController mediaPlayerCUViewController;
    private Song draggedSong;
    private String currentTableview;
    private static MediaPlayerViewController instance;

    private static final Image shuffleIcon = new Image("Icons/shuffle.png");
    private static final Image shuffleIconDisable = new Image("Icons/shuffle-disable.png");
    private static final Image repeatIcon = new Image("Icons/repeat.png");
    private static final Image repeat1Icon = new Image("Icons/repeat-once.png");
    private static final Image repeatDisableIcon = new Image("/Icons/repeat-disable.png");
    private static final Image playIcon = new Image("Icons/play.png");
    private static final Image pauseIcon = new Image("Icons/pause.png");
    private static final Image mainIcon = new Image("Icons/mainIcon.png");



    public Song getCurrentSong() {
        return currentSong;
    }

    public MediaPlayerViewController() throws Exception {
        instance = this;

        songModel = new SongModel();
        playlistModel = new PlaylistModel();
        songPlaylistModel = new SongPlaylistModel();
        displayErrorModel = new DisplayErrorModel();
        currentPlaylist = null;
    }

    public static MediaPlayerViewController getInstance() throws Exception {
        if (instance == null) {
            instance = new MediaPlayerViewController();
        }
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mediaPlayerCUViewController = MediaPlayerCUViewController.getInstance();

        btnRepeatIcon.setImage(repeatDisableIcon);
        btnShuffleIcon.setImage(shuffleIconDisable);

        sliderProgressSong.setPickOnBounds(false); //So you only can use slider when actually touch it
        sliderProgressVolume.setPickOnBounds(false); // -||-

        // Initializes the Observable list into a Filtered list for use in the search function
        FilteredList<Song> filteredSongs = new FilteredList<>(FXCollections.observableList(songModel.getObservableSongs()));
        tblSongs.setItems(filteredSongs);

        // Adds a FilterList to the tblSongs that will automatically filter based on search input through the use
        // of a FilteredList made from our observable list of songs
        txtSongSearch.textProperty().addListener((observable, oldValue, newValue) ->
                tblSongs.setItems(filterList(songModel.getObservableSongs(), newValue.toLowerCase()))
        );

        // Initialize the tables with columns.
        initializeTableColumns();

        // add data from observable list
        tblSongs.setItems(songModel.getObservableSongs());
        tblPlaylist.setItems(PlaylistModel.getObservablePlaylists());

        // set default volume to 10 and update song progress
        sliderProgressVolume.setValue(volume);
        setVolume();
        updateProgressStyle();

        for (Song s : songModel.getObservableSongs()) {
            if (s.getSongPath() != null) {
                try {
                    soundMap.put(s.getId(), new MediaPlayer(new Media(new File(s.getSongPath()).toURI().toString())));
                } catch (MediaException e) {
                    soundMap.put(s.getId(), new MediaPlayer(new Media(new File("resources/Sounds/missingFileErrorSound.mp3").toURI().toString())));
                }
            }
        }

        // Add tableview functionality
        playSongFromTableViewPlaylist();
        clearSelectionForPlaylistSelect();
        playSongFromTableView();
        contextSystem();
        initializeDragAndDrop();
    }

    private void initializeTableColumns() {
        // Initialize the tables with columns.
        colName.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("songCategory"));
        colSongTime.setCellValueFactory(new PropertyValueFactory<>("SongLengthHHMMSS"));

        colPlaylistName.setCellValueFactory(new PropertyValueFactory<>("playlistName"));
        colSongCount.setCellValueFactory(new PropertyValueFactory<>("songCount"));
        colPlaylistTime.setCellValueFactory(new PropertyValueFactory<>("SongLengthHHMMSS"));

        colTitleInPlaylist.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtistInPlaylist.setCellValueFactory(new PropertyValueFactory<>("artist"));
    }


//*******************************************CONTEXT*MENU**************************************************

    ContextMenu contextMenuSongs = new ContextMenu();
    MenuItem delete = new MenuItem("Delete");

    private void contextSystem() {
        ContextMenu contextMenu = new ContextMenu();
        ContextMenu contextMenuPlaylist = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: lightgreen; -fx-padding: 0.0em 0.333333em 0.0em 0.333333em; -fx-background-radius: 6 6 6 6, 5 5 5 5, 4 4 4 4;");
        contextMenuPlaylist.setStyle("-fx-background-color: aqua; -fx-padding: 0.0em 0.333333em 0.0em 0.333333em; -fx-background-radius: 6 6 6 6, 5 5 5 5, 4 4 4 4;");
        contextMenuSongs.setStyle("-fx-background-color: pink; -fx-padding: 0.0em 0.333333em 0.0em 0.333333em; -fx-background-radius: 6 6 6 6, 5 5 5 5, 4 4 4 4;");

        MenuItem createSong = new MenuItem("Create Song");
        MenuItem updateSong = new MenuItem("Update Song");
        MenuItem createPlaylist = new MenuItem("Create Playlist");
        MenuItem updatePlaylist = new MenuItem("Update Playlist");
        MenuItem deleteAllSongs = new MenuItem("Delete All Songs");
        contextMenuPlaylist.getItems().addAll(createPlaylist, updatePlaylist, delete, deleteAllSongs);
        contextMenu.getItems().addAll(createSong, updateSong, delete);
        contextMenuSongs.getItems().addAll();

        tblSongs.setContextMenu(contextMenu);
        tblSongsInPlaylist.setContextMenu(contextMenuSongs);
        tblPlaylist.setContextMenu(contextMenuPlaylist);

        tblSongs.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    currentSong = row.getItem();
                }
                else if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.getItems().clear();
                    currentSong = row.getItem();
                    if (row.getIndex() >= tblSongs.getItems().size()) {
                        contextMenu.getItems().addAll(createSong);
                    } else {
                        contextMenu.getItems().addAll(createSong, updateSong, delete);
                    }
                }
            });
            return row;
        });

        tblPlaylist.setRowFactory(tv -> {
            TableRow<Playlist> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (row.getIndex() >= tblPlaylist.getItems().size()) {
                        tblSongsInPlaylist.getItems().clear();
                    } else {
                        try {
                            currentPlaylist = row.getItem();
                            refreshEverything();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenuPlaylist.getItems().clear();
                    currentPlaylist = row.getItem();
                    if (row.getIndex() >= tblPlaylist.getItems().size()) {
                        contextMenuPlaylist.getItems().addAll(createPlaylist);
                    } else {
                        contextMenuPlaylist.getItems().addAll(createPlaylist, updatePlaylist, delete, deleteAllSongs);
                    }
                }
            });
            return row;
        });


        createSong.setOnAction((event) -> {
            try {
                newWindowCreate();
                contextMenu.hide();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        updateSong.setOnAction((event) -> {
            try {
                newWindowUpdate();
                contextMenu.hide();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        createPlaylist.setOnAction((event) -> {
            try {
                btnCreatePlaylist.fireEvent(event);
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        updatePlaylist.setOnAction((event) -> {
            try {
                btnUpdatePlaylist.fireEvent(event);
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        delete.setOnAction((event) -> {
            try {
                deleteMethod();
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        deleteAllSongs.setOnAction((event) -> {
            try {
                songPlaylistModel.deleteAllSongsFromPlaylist(tblPlaylist.getSelectionModel().getSelectedItem());
                refreshEverything();
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    //*******************************************SEARCH*FUNCTION**************************************************
    //Searches through the titles and artists of all the songs to be used in the filterList method underneath
    private boolean searchFindsSongs(Song song, String searchText) {
        return (song.getTitle().toLowerCase().contains(searchText.toLowerCase())) || (song.getArtist().toLowerCase().contains(searchText.toLowerCase()));
    }

    private ObservableList<Song> filterList(List<Song> song, String searchText) {
        List<Song> filteredList = new ArrayList<>();
        for (Song s : song) {
            if (searchFindsSongs(s, searchText)) {
                filteredList.add(s);
            }
        }
        return FXCollections.observableList(filteredList);
    }
//********************************MEDIA*PLAYER*FUNCTION**************************************************

    private void playSongFromTableView() {
        tblSongs.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY)
                tblSongsInPlaylist.getSelectionModel().clearSelection();
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) { // Check for double-click
                currentSongList = songModel.getObservableSongs();
                currentIndex = currentSongList.indexOf(tblSongs.getSelectionModel().getSelectedItem());
                currentPlaylistPlaying = null;
                Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
                if (selectedSong != null && currentMusic != soundMap.get(selectedSong.getId())) {
                    sliderProgressSong.setValue(0);
                    PlaySong(selectedSong);
                }
            }
        });
    }

    private void playSongFromTableViewPlaylist() {
        tblSongsInPlaylist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY)
                tblSongs.getSelectionModel().clearSelection();
            if (event.getClickCount() == 2) { // Check for double-click
                currentSongList = songPlaylistModel.getObservablePlaylistsSong();
                currentPlaylistPlaying = currentPlaylist;
                currentIndex = currentSongList.indexOf(tblSongsInPlaylist.getSelectionModel().getSelectedItem());
                Song selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    sliderProgressSong.setValue(0);
                    PlaySong(selectedSong);
                }
            }
        });
    }

    private void clearSelectionForPlaylistSelect() {
        tblPlaylist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                tblSongs.getSelectionModel().clearSelection();
                tblSongsInPlaylist.getSelectionModel().clearSelection();
            }
        });
    }

    public void playSong() {
        Song selectedSong = null;
        if (currentMusic != null) {
            togglePlayPause();
        } else if (tblSongs.getSelectionModel().getSelectedItem() != null)
            selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        else if (tblSongsInPlaylist.getSelectionModel().getSelectedItem() != null) {
            selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
            currentSongList = songPlaylistModel.getObservablePlaylistsSong();
        }
        if (selectedSong != null) {
            MediaPlayer newSong = soundMap.get(selectedSong.getId());
            if (currentMusic != newSong && newSong != null) {
                handleNewSong(newSong, selectedSong);
            }
        }

    }

    private void togglePlayPause() {
        if (currentMusic.getStatus() == MediaPlayer.Status.PLAYING) { //If it was playing we pause it
            currentMusic.pause();
            isMusicPaused = true;
            btnPlayIcon.setImage(playIcon);
        } else { // If it was instead paused, we start playing the song again
            currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()));
            currentMusic.play();
            isMusicPaused = false;
            btnPlayIcon.setImage(pauseIcon);
        }
    }


    // This method changes the color of the row where the playing song is located
    public static <T> void changeRowColor(TableView<T> tableView, int rowNumber) {
        tableView.setRowFactory(tv -> new TableRow<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                // Reset the style for all rows
                setStyle("");

                // Set the style for the row where the playing song is located
                if (!empty && getIndex() == rowNumber) {
                    setStyle("-fx-background-color: rgb(42,194,42); -fx-border-color: #1aa115; -fx-text-fill: #02522b");
                 }
            }
        });
        tableView.refresh();
    }



   private void handlePlayingSongColor()    {
      if (currentPlaylistPlaying == null && currentSongPlaying != null) {
           changeRowColor(tblSongs, currentIndex);
           changeRowColor(tblSongsInPlaylist, -1);
       }

       else if (currentPlaylist == currentPlaylistPlaying){
           changeRowColor(tblSongs, -1);
           changeRowColor(tblSongsInPlaylist, currentIndex);
       }
       else {
           changeRowColor(tblSongsInPlaylist, -1);
       }
   }


    private void handleNewSong(MediaPlayer newSong, Song selectedSong) {
        if (currentMusic != null) {
            currentMusic.stop();
        }

        currentSongPlaying = selectedSong;
        sliderProgressSong.setDisable(false);
        currentMusic = newSong;
        currentMusic.setVolume((sliderProgressVolume.getValue())); //We set the volume
        sliderProgressSong.setMax(newSong.getTotalDuration().toSeconds()); //Set our progress to the time so, we know maximum value
        lblPlayingNow.setText("Now playing: " + selectedSong.getTitle() + " - " + selectedSong.getArtist());
        currentMusic.seek(Duration.ZERO); //When you start a song again it should start from start
        handlePlayingSongColor();

        // Play or pause based on the isMusicPaused flag
        if (isMusicPaused) {
            currentMusic.pause();
            btnPlayIcon.setImage(playIcon);
        } else {
            currentMusic.play();
            btnPlayIcon.setImage(pauseIcon);
        }


        currentMusic.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            // Update the slider value as the song progresses
            if (!isUserChangingSlider) {
                sliderProgressSong.setValue(newValue.toSeconds());
            }
        });
        //Do these when song is finished
        currentMusic.setOnEndOfMedia(this::onEndOfSong);
    }

    public void PlaySong(Song song) {
        MediaPlayer newMusic = soundMap.get(song.getId());
        handleNewSong(newMusic, song);
    }

    private void handleSongSwitch(int newIndex) {
        if (shuffleMode == 1) { //If shuffle is enable shuffle
            shuffleMode();
            return;

        }
        if (repeatMode == 0 && currentSongList !=  songModel.getObservableSongs()) {//If repeat is disable do it
            if (repeatModeDisable())  {
                System.out.println("Repeat mode is disabled.");
                return;
            }
        }
        if (!currentSongList.isEmpty()) {
            currentIndex = newIndex % currentSongList.size();
            Song switchedSong = currentSongList.get(currentIndex);
            PlaySong(switchedSong);
        }
    }




    public void onEndOfSong(){
        if (repeatMode == 2)    {//Repeat 1
            handleNewSong(currentMusic, getSongById(currentSongPlaying.getId()));
            return;
        }
        if (shuffleMode == 1) {
            shuffleMode();
        }

            currentMusic = null;
            sliderProgressSong.setValue(0);
            lblPlayingNow.setText("No song playing");
            sliderProgressSong.setDisable(true);
            updateSongProgressTimer();
            btnPlayIcon.setImage(playIcon);


            handleSongSwitch(currentIndex + 1); //Next song exactly same if user press the next song button
        }

//********************************REPEAT*SHUFFLE*FUNCTION**************************************************
    public boolean repeatModeDisable(){
        if (currentPlaylistPlaying != null) {

            Playlist nextPlaylistToGoTo = null;
            if (previousPress && currentIndex == 0) {
                Optional<Playlist> optionalNextPlaylist = PlaylistModel.getObservablePlaylists().stream()
                        .filter(p -> p.getId() < currentPlaylistPlaying.getId())
                        //  .filter(p -> p.getSongCount() != 0) // Check if the playlist has songs
                        .max(Comparator.comparing(Playlist::getId));

                nextPlaylistToGoTo = optionalNextPlaylist.orElse(PlaylistModel.getObservablePlaylists().getLast());
            } else if (currentSongList.indexOf(currentSongPlaying) + 1 == currentSongList.size() && !previousPress) {
                Optional<Playlist> optionalNextPlaylist = PlaylistModel.getObservablePlaylists().stream()
                        .filter(p -> p.getId() > currentPlaylistPlaying.getId())
                        // .filter(p -> p.getSongCount() != 0) // Check if the playlist has songs
                        .min(Comparator.comparing(Playlist::getId));

                nextPlaylistToGoTo = optionalNextPlaylist.orElse(PlaylistModel.getObservablePlaylists().getFirst());
                PlaylistModel.getObservablePlaylists().stream().close();
            }

            if (nextPlaylistToGoTo != null) {
                try {
                    songPlaylistModel.playlistSongs(nextPlaylistToGoTo);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                currentPlaylistPlaying = nextPlaylistToGoTo;
                currentPlaylist = nextPlaylistToGoTo;
                currentSongList = songPlaylistModel.getObservablePlaylistsSong();
                currentIndex = 0;

                if (!currentSongList.isEmpty()) {
                    PlaySong(currentSongList.getFirst());
                    return true;
                }
                return repeatModeDisable();
            }
            return false;
        }
        return false;
    }

    public void shuffleMode(){

        //If repeat is disabled it should also jump throw playlist 


        currentIndex = getRandomSong();
        currentIndex = (currentIndex) % currentSongList.size();
        Song randomSong = currentSongList.get(currentIndex);
        PlaySong(randomSong);
    }

    public int getRandomSong(){
        int min = 0;
        int max = currentSongList.size();
        int range = max - min;
        return (int)(Math.random() * range) + min;
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
        double progress = sliderProgressVolume.getValue();
        int percentage = (int) (progress * 100);
        lblVolume.setText(String.format("%d%%", percentage));

        if (currentMusic != null) {
            currentMusic.setVolume((sliderProgressVolume.getValue()));
        }
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

    private Song getSongById(int songId) { //This is not right layer!
        for (Song s : songModel.getObservableSongs()) {
            if (s.getId() == songId) {
                return s;
            }
        }
        return null;
    }

    public void refreshEverything() throws Exception {
        tblSongs.getItems().clear();
        tblSongs.setItems(songModel.updateSongList());

        if (currentPlaylist == null)   {
            currentPlaylist = PlaylistModel.getObservablePlaylists().getFirst();
        }

        songPlaylistModel.playlistSongs(currentPlaylist);
        tblSongsInPlaylist.setItems(songPlaylistModel.getObservablePlaylistsSong());
        tblSongs.refresh();
        tblPlaylist.refresh();
        tblSongsInPlaylist.refresh();
        handlePlayingSongColor();
    }

    public void refreshSongList() throws Exception {
        tblSongs.getItems().clear();
        tblSongs.setItems(songModel.updateSongList());
        tblSongs.refresh();
    }

    public void updateSongPathSoundMap(Song currentSelectedSong) { //We remove old path and add new one
        soundMap.remove(currentSelectedSong.getId());
        soundMap.put(currentSelectedSong.getId(), new MediaPlayer(new Media(new File(currentSelectedSong.getSongPath()).toURI().toString()))); //We add new song to the hashmap
    }

    public void addSongToSoundMap(Song newCreatedSong) { //We add the song to our hashmap, so it can be played
        soundMap.put(newCreatedSong.getId(), new MediaPlayer(new Media(new File(newCreatedSong.getSongPath()).toURI().toString())));
    }

    public void deleteMethod(){

        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = tblPlaylist.getSelectionModel().getSelectedItem();
        Song selectedSongInPlaylist = tblSongsInPlaylist.getSelectionModel().getSelectedItem();

        if (selectedSong != null & selectedSongInPlaylist == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Song");
                alert.setHeaderText("You want to delete " + currentSong.getTitle());
                alert.setContentText("Are you ok with this?");
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(mainIcon);

                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(okButton, cancelButton);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == okButton) {
                    try {
                        for (Playlist p : PlaylistModel.getObservablePlaylists()) {
                            songPlaylistModel.deleteSongFromPlaylist(selectedSong, p);
                        }
                        songModel.deleteSong(selectedSong);
                        refreshEverything();
                    } catch (Exception e) {
                        displayErrorModel.displayError(e);
                        e.printStackTrace();
                    }
                }
            return;
        }
        if (selectedPlaylist != null & selectedSongInPlaylist == null){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Playlist");
                alert.setHeaderText("You want to delete " + currentPlaylist.getPlaylistName());
                alert.setContentText("Are you ok with this?");
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(mainIcon);

                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(okButton, cancelButton);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == okButton) {
                    try {
                        playlistModel.deletePlaylist(selectedPlaylist);
                        tblPlaylist.refresh();
                    } catch (Exception e) {
                        displayErrorModel.displayError(e);
                        e.printStackTrace();
                    }
                }
                return;
            }
        if (selectedSongInPlaylist != null){
            try {
                currentPlaylist.setSongCount(currentPlaylist.getSongCount() - 1);
                currentPlaylist.setSongTotalTime(currentPlaylist.getSongTotalTime() - selectedSongInPlaylist.getSongLength());
                songPlaylistModel.deleteSongFromPlaylist(selectedSongInPlaylist, selectedPlaylist);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void initializeDragAndDrop() {
        tblSongs.setOnDragDetected(event -> { //When user drag a song from song list
            Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
            currentTableview = "tblSongs";
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
            if (Objects.equals(currentTableview, "tblSongsInPlaylist")) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });


        tblSongsInPlaylist.setOnDragDropped(event -> { //When user drop a song from song list into playlist song
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && Objects.equals(currentTableview, "tblSongs")) {
                int songId = Integer.parseInt(db.getString());
                Song selectedSong = getSongById(songId);

                if (selectedSong != null) {
                    try {
                        if (currentPlaylist == null) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Playlist System");
                            alert.setHeaderText("You need to choose a playlist");
                            alert.showAndWait();
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(mainIcon);
                        } else if (songPlaylistModel.addSongToPlaylist(selectedSong, currentPlaylist)) { //We first need to make sure it not already in the playlist
                            songPlaylistModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());

                            tblPlaylist.refresh();
                            success = true;

                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Playlist System");
                            alert.setHeaderText("The song is already in the playlist");
                            alert.showAndWait();
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(mainIcon);
                        }
                    } catch (Exception e) {
                        displayErrorModel.displayError(e);
                        e.printStackTrace();
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        anchorPane.setOnDragDropped(event -> { //When user drop a song from playlist song list into background
            if (draggedSong != null) {
                currentPlaylist.setSongCount(currentPlaylist.getSongCount() - 1);
                currentPlaylist.setSongTotalTime(currentPlaylist.getSongTotalTime() - draggedSong.getSongLength());
                tblPlaylist.refresh();

                try {
                    songPlaylistModel.deleteSongFromPlaylist(draggedSong, currentPlaylist);
                    draggedSong = null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            anchorPane.requestFocus();
            event.setDropCompleted(true);
            event.consume();
        });

        //Source Stackoverflow

        tblSongsInPlaylist.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenuSongs.getItems().clear();
                    currentSong = row.getItem();
                    if (row.getIndex() >= tblSongsInPlaylist.getItems().size()) {
                        contextMenuSongs.getItems().clear();
                    } else {
                        contextMenuSongs.getItems().addAll(delete);
                    }
                }
            });

            row.setOnDragDetected(event -> {
                if (event.getButton() == MouseButton.PRIMARY && !row.isEmpty()) {
                    draggedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
                    currentTableview = "tblSongsInPlaylist";
                    if (!row.isEmpty()) {
                        int index = row.getIndex();
                        Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString(Integer.toString(index));
                        db.setContent(cc);
                        event.consume();
                    }
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
                if (currentTableview.equals("tblSongs")) {}

                else {
                    Dragboard db = event.getDragboard();
                    if (db.hasString()) {
                        int draggedIndex = Integer.parseInt(db.getString());
                        int dropIndex = row.isEmpty() ? tblSongsInPlaylist.getItems().size() : row.getIndex();

                        if (dropIndex == tblSongsInPlaylist.getItems().size()) { //Make sure you can not drop a song to an empty spot
                            event.setDropCompleted(false);
                            event.consume();
                            return;
                        }
                        if (dropIndex != draggedIndex) { // Perform the reordering in your data model
                            moveSongInPlaylist(draggedIndex, dropIndex);
                            event.setDropCompleted(true);
                            tblSongsInPlaylist.getSelectionModel().select(dropIndex);
                            event.consume();
                        }
                    }
                }
            });
            return row;
        });
    }

    public void moveSongInPlaylist(int fromIndex, int toIndex) {
        Song selectedSong = tblSongsInPlaylist.getItems().get(fromIndex);  // Get the selected song
        Song oldSong = songPlaylistModel.getObservablePlaylistsSong().get(toIndex);  // Get song there was before

        if (toIndex == tblSongsInPlaylist.getItems().size()-1)   {
            tblSongsInPlaylist.getItems().add(tblSongsInPlaylist.getItems().size(), selectedSong);
            selectedSong = tblSongsInPlaylist.getItems().remove(fromIndex);
        }
        else if (toIndex != tblSongsInPlaylist.getItems().size()-1)   {
            selectedSong = tblSongsInPlaylist.getItems().remove(fromIndex);
            tblSongsInPlaylist.getItems().add(toIndex, selectedSong);
        }
        try {
            songPlaylistModel.updateSongInPlaylist(selectedSong, oldSong, currentPlaylist);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        tblSongsInPlaylist.refresh();
    }
//******************************BUTTONS*****************************
    public void newWindowCreate() throws IOException {
        MediaPlayerCUViewController.setTypeCU(1);
        newUCWindow("Song Creator");
    }

    public void newWindowUpdate() throws IOException {
        MediaPlayerCUViewController.setTypeCU(2);
        if (currentSong == null)  {newWindowCreate();return;} //If user want to update but forgot a song they can create a new one instead
        newUCWindow("Song Updater");
    }

    public void newUCWindow(String windowTitle) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MediaPlayerCU.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.getIcons().add(mainIcon);
        stage.setTitle(windowTitle);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
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
            dialog.setTitle("Update Playlist " + currentPlaylist.getPlaylistName());
            dialog.setHeaderText("What would you like to rename the Playlist");
        }

            // dialog.setGraphic(new ImageView(new Image("/Icons/mainIcon.png"))); //Maybe?

        // Set the icon for the dialog window
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);

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
    public void forwardSong() {
        previousPress = false;
        handleSongSwitch(currentIndex + 1);
    }
    private boolean previousPress = false;

    public void backwardSong() {
        previousPress = true;
        handleSongSwitch(currentIndex - 1 + currentSongList.size());
    }
    public void deleteBtn() {
        deleteMethod();
    }
    public void btnShuffleSong() {
        if (btnShuffleIcon.getImage().equals(shuffleIcon))    {
            btnShuffleIcon.setImage(shuffleIconDisable);
            shuffleMode = 0;
            //Execute shuffle disable method
        }
        else if (btnShuffleIcon.getImage().equals(shuffleIconDisable))    {
            btnShuffleIcon.setImage(shuffleIcon);
            shuffleMode = 1;
            //Execute shuffle method
        }
    }

    public void btnRepeatSong() {
         if (btnRepeatIcon.getImage().equals(repeat1Icon))    {
            btnRepeatIcon.setImage(repeatDisableIcon);
            repeatMode = 0;
            //Change repeat mode to disabled so system know
        }

         else if (btnRepeatIcon.getImage().equals(repeatDisableIcon))    {
            btnRepeatIcon.setImage(repeatIcon);
            repeatMode = 1;
             //Change repeat mode to repeat same song so system know
        }
        else if (btnRepeatIcon.getImage().equals(repeatIcon))    {
            btnRepeatIcon.setImage(repeat1Icon);
            repeatMode = 2;
             //Change repeat mode to repeat playlist so system know
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