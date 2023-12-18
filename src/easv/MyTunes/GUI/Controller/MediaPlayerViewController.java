/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.GUI.Controller;

import easv.MyTunes.BE.Playlist;
import easv.MyTunes.BE.Song;
import easv.MyTunes.GUI.Model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MediaPlayerViewController implements Initializable {
    @FXML
    private HBox vboxTblBtn, mediaViewBox;
    @FXML
    private VBox tblSongsInPlaylistVBOX;
    @FXML
    private TableView<Playlist> tblPlaylist;
    @FXML
    private MediaView mediaView;
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
    private Button btnCreatePlaylist, btnUpdatePlaylist, btnPlay, btnRepeat, btnShuffle, btnVideo, btnSpeed;
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
    private int repeatMode = 0; //Default repeat mode
    private int shuffleMode = 0; //Default shuffle
    private int currentIndex = 0;
    private boolean previousPress = false;
    public List<Double> speeds = new ArrayList<>();
    private int currentSpeedIndex = 0;
    private Double currentSpeed = 1.00;
    private Playlist currentPlaylist, currentPlaylistPlaying; //The current playing selected and playing from
    private Song currentSong, currentSongPlaying; //The current Song selected and playing
    private SongModel songModel;
    private PlaylistModel playlistModel;
    private PlaylistSongModel playlistSongModel;
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
    private static final String playingGraphic = "-fx-background-color: rgb(42,194,42); -fx-border-color: #1aa115; -fx-background-radius: 15px; -fx-border-radius: 15px 15px 15px 15px;";


    public Song getCurrentSong() {
        return currentSong;
    }

    public MediaPlayerViewController() {
        instance = this;
        displayErrorModel = new DisplayErrorModel();
        try {
            songModel = new SongModel();
            playlistModel = new PlaylistModel();
            playlistSongModel = new PlaylistSongModel();
            currentPlaylist = null;
        } catch (Exception e) {
            displayErrorModel.displayError(e);
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
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mediaPlayerCUViewController = MediaPlayerCUViewController.getInstance();

        btnRepeatIcon.setImage(repeatDisableIcon); //We set picture here so the button know what is chosen
        btnShuffleIcon.setImage(shuffleIconDisable); // -||-
        sliderProgressSong.setPickOnBounds(false); // So you only can use slider when actually touch it
        sliderProgressVolume.setPickOnBounds(false); // -||-
        tblSongsInPlaylist.setPlaceholder(new Label("No songs found"));
        tblSongs.setPlaceholder(new Label("No songs found"));
        tblPlaylist.setPlaceholder(new Label("No playlist found"));
        tblSongsInPlaylistVBOX.setManaged(false); // Hide songs in playlist while no playlist is selected

        // Initializes the Observable list into a Filtered list for use in the search function
        FilteredList<Song> filteredSongs = new FilteredList<>(FXCollections.observableList(SongModel.getObservableSongs()));
        tblSongs.setItems(filteredSongs);

        // Adds a FilterList to the tblSongs that will automatically filter based on search input through the use
        // of a FilteredList made from our observable list of songs
        txtSongSearch.textProperty().addListener((observable, oldValue, newValue) ->
                tblSongs.setItems(songModel.filterList(SongModel.getObservableSongs(), newValue.toLowerCase()))
        );

        // Initialize the tables with columns.
        initializeTableColumns();

        // Add data from observable list
        tblSongs.setItems(SongModel.getObservableSongs());
        tblPlaylist.setItems(PlaylistModel.getObservablePlaylists());

        // Set default volume to 10% (↓) and updates song progress
        sliderProgressVolume.setValue(0.1F);
        setVolume();
        updateProgressStyle();

        speeds.addAll(Arrays.asList(0.25, 0.50 ,0.75, 1.00, 1.25, 1.5, 1.75, 2.0, 3.0, 4.0, 5.0)); // We add the speeds to the list
        btnSpeed.setText(currentSpeed + "x");

        // Add tableview functionality
        playSongFromTableViewPlaylist();
        playSongFromTableView();
        clearSelectionForPlaylistSelect();
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

    ContextMenu contextMenuSongsInPlaylist = new ContextMenu();
    ContextMenu contextMenuPlaylist = new ContextMenu();
    ContextMenu contextMenuSongs = new ContextMenu();
    MenuItem deletePlaylist = new MenuItem("Delete");
    MenuItem deleteSong = new MenuItem("Delete");
    MenuItem deleteSongInPlaylist = new MenuItem("Delete");
    MenuItem createPlaylist = new MenuItem("Create Playlist");
    MenuItem updatePlaylist = new MenuItem("Update Playlist");
    MenuItem deleteAllSongs = new MenuItem("Delete All Songs");
    Menu playlistSubMenu = new Menu("Add to Playlist");

    // Initializes the submenu for the contextmenu that allows you to add songs to playlist and have it update dynamically
    private void contextSystemSubMenuAddSongs() {
        playlistSubMenu.getItems().clear();
        PlaylistModel.getObservablePlaylists().forEach(playlist -> {
            MenuItem playlistItem = new MenuItem(playlist.getPlaylistName());
            playlistItem.setUserData(playlist); // Store the playlist as user data
            playlistSubMenu.getItems().add(playlistItem);
        });
    }

    // Setup for the context menu system in the program with it dynamically removing options unless you have a song or playlist selected
    private void contextSystem() {
        MenuItem createSong = new MenuItem("Create Song");
        MenuItem updateSong = new MenuItem("Update Song");


        contextMenuPlaylist.getItems().addAll(createPlaylist, updatePlaylist, deletePlaylist, deleteAllSongs);
        contextMenuSongs.getItems().addAll(createSong, updateSong, deleteSong);
        contextMenuSongsInPlaylist.getItems().addAll(deleteSongInPlaylist);

        tblSongs.setContextMenu(contextMenuSongs);
        tblSongsInPlaylist.setContextMenu(contextMenuSongsInPlaylist);
        tblPlaylist.setContextMenu(contextMenuPlaylist);

        tblSongs.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    currentSong = row.getItem();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenuSongs.getItems().clear();
                    currentSong = row.getItem();
                    if (row.getIndex() >= tblSongs.getItems().size()) {
                        contextMenuSongs.getItems().addAll(createSong);
                    } else {
                        contextSystemSubMenuAddSongs(); //We update the playlist list
                        contextMenuSongs.getItems().addAll(createSong, updateSong, deleteSong, playlistSubMenu);
                    }
                }
            });
            return row;
        });

        createSong.setOnAction((event) -> {
            try {
                btnNewWindowCreate();
                contextMenuSongs.hide();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        updateSong.setOnAction((event) -> {
            try {
                btnNewWindowUpdate();
                contextMenuSongs.hide();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        createPlaylist.setOnAction((event) -> {
            try {
                btnCreatePlaylistNow();
                contextMenuPlaylist.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        playlistSubMenu.setOnAction(event -> {
            try {

                if (event.getSource() instanceof MenuItem) {
                    MenuItem selectedItem = (MenuItem) event.getTarget();
                    Playlist selectedPlaylist = (Playlist) selectedItem.getUserData();
                    currentPlaylist = playlistModel.getPlaylistById(selectedPlaylist.getId());
                }
                playlistSongModel.playlistSongs(currentPlaylist);

                if (playlistSongModel.addSongToPlaylist(currentSong, currentPlaylist)) { //We first need to make sure it not already in the playlist
                    tblPlaylist.getSelectionModel().select(currentPlaylist);
                    playlistSongModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());
                    refreshPlaylists();
                    contextMenuSongs.hide();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Playlist System");
                    alert.setHeaderText("The song is already in the playlist");
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(mainIcon);
                    alert.showAndWait();
                    contextMenuSongs.hide();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        updatePlaylist.setOnAction((event) -> {
            try {
                btnUpdatePlaylistNow();
                contextMenuPlaylist.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteSong.setOnAction((event) -> {
            try {
                handleDelete();
                contextMenuSongs.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteSongInPlaylist.setOnAction((event) -> {
            try {
                handleDelete();
                contextMenuSongs.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deletePlaylist.setOnAction((event) -> {
            try {
                handleDelete();
                contextMenuSongs.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        deleteAllSongs.setOnAction((event) -> {
            try {
                playlistSongModel.deleteAllSongsFromPlaylist(tblPlaylist.getSelectionModel().getSelectedItem());
                refreshPlaylists();
                contextMenuPlaylist.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


//*******************************************PROGRESS*SYSTEM**************************************************

    private void updateProgressStyle() { // We wait 1 second, after we listen to change in slider and take care of it.
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

    public void setVolume() { // Sets the volume for the media player to the standard 10%
        double progress = sliderProgressVolume.getValue();
        int percentage = (int) (progress * 100);
        lblVolume.setText(String.format("%d%%", percentage));

        if (currentMusic != null) {
            currentMusic.setVolume((sliderProgressVolume.getValue()));
        }
    }

    private void updateSongProgressTimer() { // Update the slider for songs with time
        if (currentMusic != null) {
            double progressValue = sliderProgressSong.getValue();
            long currentSeconds = (long) progressValue;
            lblCurrentSongProgress.setText(String.format("%02d:%02d:%02d", currentSeconds / 3600, (currentSeconds % 3600) / 60, currentSeconds % 60)); //Format HH:MM:SS
            Duration totalDuration = currentMusic.getTotalDuration();
            long totalSeconds = (long) totalDuration.toSeconds();
            lblSongDuration.setText(String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60)); //Format HH:MM:SS
        } else {
            lblCurrentSongProgress.setText("00:00:00");
            lblSongDuration.setText("00:00:00");
        }
    }
//********************************MEDIA*PLAYER*FUNCTION**************************************************

    private CompletableFuture<Void> addSongsToSoundMap(Song s) { // Will try to add the selected song to the sound map when trying to play it
        CompletableFuture<Void> future = new CompletableFuture<>(); // We use to make sure the Media player is 100% done before going next
        if (soundMap.get(s.getId()) == null) { // Check if the song is not already in the soundMap
            Path filePath = Paths.get(s.getSongPath());
            try {
                CompletableFuture.runAsync(() -> {
                    if (Files.exists(filePath)) {
                        MediaPlayer mp = new MediaPlayer(new Media(new File(String.valueOf(filePath)).toURI().toString()));
                        mp.setOnReady(() -> {
                            mp.getTotalDuration();
                            soundMap.put(s.getId(), mp);
                            future.complete(null); // Signal completion
                        });
                    } else { // File does not exist, use the error sound
                        MediaPlayer mp = new MediaPlayer(new Media(new File("resources/Sounds/missingFileErrorSound.mp3").toURI().toString()));
                        mp.setOnReady(() -> {
                            mp.getTotalDuration();
                            soundMap.put(s.getId(), mp);
                            future.complete(null); // Signal completion
                        });
                    }
                }).exceptionally(ex -> null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        } else { //This is when song is there already
            future.complete(null); // Signal completion
        }
        return future;
    }

    private void handleSongPlay() { //Tries to figure out in which table view the song you want to play is located
        Song selectedSong;
        if (currentMusic != null) {
            selectedSong = null;
            togglePlayPause();
        } else if (tblSongs.getSelectionModel().getSelectedItem() != null)
            selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        else if (tblSongsInPlaylist.getSelectionModel().getSelectedItem() != null) {
            selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
            currentSongList = playlistSongModel.getObservablePlaylistsSong();
        } else {
            selectedSong = null;
        }
        if (selectedSong != null) {
            addSongsToSoundMap(selectedSong).thenRun(() -> {

                MediaPlayer newSong = soundMap.get(selectedSong.getId());
                if (currentMusic != newSong && newSong != null) {
                    handleNewSong(newSong, selectedSong);
                }
            });
        }
    }

    private void playSongFromTableView() { //Plays a song from the song list table view
        tblSongs.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY)
                tblSongsInPlaylist.getSelectionModel().clearSelection(); // Clears selection from song in the playlist to stop delete from interacting weirdly
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) { // Check for double-click
                currentSongList = SongModel.getObservableSongs();
                currentIndex = currentSongList.indexOf(tblSongs.getSelectionModel().getSelectedItem());
                currentPlaylistPlaying = null;
                Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
                // Checks if a valid song was selected and if it was, tries to play it
                if (selectedSong != null) {
                    sliderProgressSong.setValue(0);
                    isMusicPaused = false;
                    PlaySong(selectedSong);
                }
            }
        });
    }

    private void playSongFromTableViewPlaylist() { // Plays a song from the songInPlaylist Table view
        tblSongsInPlaylist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                tblSongs.getSelectionModel().clearSelection(); // Clears the selection from the normal table view for songs to stop delete from deleting the wrong song
            }
            if (event.getClickCount() == 2) { // Check for double-click
                currentSongList = playlistSongModel.getObservablePlaylistsSong();
                currentIndex = currentSongList.indexOf(tblSongsInPlaylist.getSelectionModel().getSelectedItem());
                currentPlaylistPlaying = currentPlaylist;
                Song selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
                // Checks if a song exists and if it does, it plays the song
                if (selectedSong != null) {
                    sliderProgressSong.setValue(0);
                    isMusicPaused = false;
                    PlaySong(selectedSong);
                }
            }
        });
    }

    private void clearSelectionForPlaylistSelect() { //Clears the selection from both song tableview and song in playlist table view when opening a new playlist
        tblPlaylist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                tblSongs.getSelectionModel().clearSelection();
                tblSongsInPlaylist.getSelectionModel().clearSelection();
            }
        });
    }

    public void togglePlayPause() { // This controls the play/Pause functionality of the program when listening ot songs
        if (currentMusic != null) {
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
    }

    public static <T> void changeRowColor(TableView<T> tableView, int rowNumber) { // This method changes the color of the row where the playing song is located
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                // Reset the style for all rows
                setStyle("");

                // Set the style for the row where the playing song is located
                if (!empty && getIndex() == rowNumber) {
                    setStyle(playingGraphic);
                }
            }
        });
        tableView.refresh();
    }

    private void handlePlayingSongColor() { // Handles colouring the current playing song in the song tableview
        if (currentPlaylistPlaying == null && currentSongPlaying != null) {
            changeRowColor(tblSongs, currentIndex);
        } else if (currentPlaylist == currentPlaylistPlaying) {
            changeRowColor(tblSongs, -1);
        }
    }

    private void handleNewSong(MediaPlayer newSong, Song selectedSong) {
        if (currentMusic != null) {
            currentMusic.stop();
        }

        currentSongPlaying = selectedSong;
        sliderProgressSong.setDisable(false);
        currentMusic = newSong;

        sliderProgressSong.setMax(newSong.getTotalDuration().toSeconds()); //Set our progress to the time so, we know maximum value
        lblPlayingNow.setText("Now playing: " + selectedSong.getTitle() + " - " + selectedSong.getArtist());
        currentMusic.seek(Duration.ZERO); //When you start a song again it should start from start
        currentMusic.setVolume((sliderProgressVolume.getValue())); //We set the volume
        handlePlayingSongColor();
        tblSongsInPlaylist.refresh(); //So the song in song playlist get its color
        tblPlaylist.refresh(); //So the playlist in playlist get its color
        currentMusic.setRate(currentSpeed);

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
        addSongsToSoundMap(song).thenRun(() -> {
            MediaPlayer newMusic = soundMap.get(song.getId());
            if (newMusic == null) {// Checks if the selected music will work or if it should throw this error instead
                soundMap.put(song.getId(), new MediaPlayer(new Media(new File("resources/Sounds/missingFileErrorSound.mp3").toURI().toString())));
                newMusic = soundMap.get(song.getId());
                sliderProgressSong.setValue(0);
            }
            handleNewSong(newMusic, song);
        });
    }

    private void handleSongSwitch(int newIndex) {
        if (shuffleMode == 1) { //If shuffle is enable, the playlist will be played in a random order
            shuffleMode();
            return;
        }
        if (repeatMode == 0 && currentSongList != SongModel.getObservableSongs()) {//If repeat is disable do it
            if (repeatModeDisable()) {
                return;
            }
        }
        if (!currentSongList.isEmpty()) {
            currentIndex = newIndex % currentSongList.size();
            Song switchedSong = currentSongList.get(currentIndex);
            PlaySong(switchedSong);
        }
    }

    public void onEndOfSong() {
        if (repeatMode == 2) {//Repeat 1
            handleNewSong(currentMusic, currentSongPlaying);
            return;
        }
        if (shuffleMode == 1) { // If enabled, the shuffle mode will play a random song from the selected table view, not including the playlist table view
            shuffleMode();
            return;
        }
        currentMusic = null;
        sliderProgressSong.setValue(0);
        lblPlayingNow.setText("No song playing");
        sliderProgressSong.setDisable(true);
        updateSongProgressTimer();
        btnPlayIcon.setImage(playIcon);
        handleSongSwitch(currentIndex + 1); //Moves the user to the next song in the table view index
    }

    //********************************REPEAT*SHUFFLE*FUNCTION**************************************************
    public boolean repeatModeDisable() { // Method to run when repeat is not enabled
        if (currentPlaylistPlaying != null) {
            Playlist nextPlaylistToGoTo = null;
            // When user is on the first song and go backwards we need to take the playlist before
            if (previousPress && currentIndex == 0) {
                Optional<Playlist> optionalNextPlaylist = PlaylistModel.getObservablePlaylists().stream()
                        .filter(p -> p.getId() < currentPlaylistPlaying.getId())
                        .max(Comparator.comparing(Playlist::getId));

                nextPlaylistToGoTo = optionalNextPlaylist.orElse(PlaylistModel.getObservablePlaylists().getLast());
                PlaylistModel.getObservablePlaylists().stream().close();
                // When user is on the last song and go forward we need to take the next playlist
            } else if (currentSongList.indexOf(currentSongPlaying) + 1 == currentSongList.size() && !previousPress) {
                Optional<Playlist> optionalNextPlaylist = PlaylistModel.getObservablePlaylists().stream()
                        .filter(p -> p.getId() > currentPlaylistPlaying.getId())
                        .min(Comparator.comparing(Playlist::getId));
                nextPlaylistToGoTo = optionalNextPlaylist.orElse(PlaylistModel.getObservablePlaylists().getFirst());
                PlaylistModel.getObservablePlaylists().stream().close();
            }

            if (nextPlaylistToGoTo != null) {
                try { //In these lines we set some variable depend on the info from new playlist
                    playlistSongModel.playlistSongs(nextPlaylistToGoTo);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                currentPlaylistPlaying = nextPlaylistToGoTo;
                currentPlaylist = nextPlaylistToGoTo;
                currentSongList = playlistSongModel.getObservablePlaylistsSong();
                currentIndex = 0;

                if (!currentSongList.isEmpty()) { // We take the next playlist to go have songs inside
                // If there is songs we play the right one if no song we start this method again to check a new playlist
                    if (previousPress) {
                        currentIndex = currentSongList.size() - 1;
                        PlaySong(currentSongList.getLast());
                        return true;
                    }
                    PlaySong(currentSongList.getFirst());
                    return true;
                }
                return repeatModeDisable();
            }
            return false;
        }
        return false;
    }

    public void shuffleMode() {
        if (repeatMode == 0 && currentSongList != SongModel.getObservableSongs()) { // Get a list of playlists with songs inside itself
            List<Playlist> nonEmptyPlaylists = PlaylistModel.getObservablePlaylists().stream()
                    .filter(playlist -> playlist.getSongCount() >= 1).toList();

            if (!nonEmptyPlaylists.isEmpty()) {
                Random random = new Random(); //Select a random playlist from playlists
                currentPlaylist = nonEmptyPlaylists.get(random.nextInt(nonEmptyPlaylists.size()));
                currentPlaylistPlaying = currentPlaylist;
                try {
                    playlistSongModel.playlistSongs(currentPlaylist);
                    currentSongList = playlistSongModel.getObservablePlaylistsSong();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        currentIndex = getRandomSong(); //Select random song from the current song list and play it
        currentIndex = (currentIndex) % currentSongList.size();
        Song randomSong = currentSongList.get(currentIndex);
        PlaySong(randomSong);
    }

//*****************************************CREATE*UPDATE*DELETE********************************************

    public void updateSongPathSoundMap(Song currentSelectedSong) { //We remove old path and add new one
        soundMap.remove(currentSelectedSong.getId());
        soundMap.put(currentSelectedSong.getId(), new MediaPlayer(new Media(new File(currentSelectedSong.getSongPath()).toURI().toString()))); //We add new song to the hashmap
    }

    public void addSongToSoundMap(Song newCreatedSong) { //We add the song to our hashmap, so it can be played
        soundMap.put(newCreatedSong.getId(), new MediaPlayer(new Media(new File(newCreatedSong.getSongPath()).toURI().toString())));
    }

    public void handleDelete() { // Handles the delete functionality of the table views
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = tblPlaylist.getSelectionModel().getSelectedItem();
        Song selectedSongInPlaylist = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Are you ok with this?");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);
        // This sets up the schematic for deleting playlists & songs

        if (selectedSong != null & selectedSongInPlaylist == null) {
            alert.setTitle("Song");
            alert.setHeaderText("You want to delete " + currentSong.getTitle());
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);
            Optional<ButtonType> result = alert.showAndWait();
            // Sets up relevant information for knowing which song you are trying to delete
            if (result.isPresent() && result.get() == okButton) {
                try {
                    for (Playlist p : PlaylistModel.getObservablePlaylists()) { // This will check through each playlist and delete the song from there since the songId is a key in the DB
                        playlistSongModel.deleteSongFromPlaylist(selectedSong, p);
                    }
                    songModel.deleteSong(selectedSong); /// removes song from database
                    refreshPlaylists(); // Refreshes the playlists so the correct time and count is shown
                    refreshSongList(); // Refreshes the song list so the deleted song is no longer there.
                } catch (Exception e) {
                    displayErrorModel.displayError(e);
                    e.printStackTrace();
                }
            }
            return;
        }
        if (selectedPlaylist != null & selectedSongInPlaylist == null) {
            alert.setTitle("Playlist");
            alert.setHeaderText("You want to delete " + currentPlaylist.getPlaylistName());
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);
            Optional<ButtonType> result = alert.showAndWait();
            // Sets up relevant information for knowing which playlist you are trying to delete
            if (result.isPresent() && result.get() == okButton) {
                try {
                    playlistModel.deletePlaylist(selectedPlaylist); // deletes the playlist from the database
                    tblPlaylist.refresh(); // refreshes playlist table view to no longer show the deleted one.
                } catch (Exception e) {
                    displayErrorModel.displayError(e);
                    e.printStackTrace();
                }
            }
            return;
        }
        if (selectedSongInPlaylist != null) { // Deletes a song from a playlist, will not show a warning since nothing permanent is done here, you can always re add the song
            try {
                currentPlaylist.setSongCount(currentPlaylist.getSongCount() - 1);
                currentPlaylist.setSongTotalTime(currentPlaylist.getSongTotalTime() - selectedSongInPlaylist.getSongLength());
                playlistSongModel.deleteSongFromPlaylist(selectedSongInPlaylist, selectedPlaylist);
                refreshPlaylists();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void newUCWindow(String windowTitle) throws IOException { // Creates the second window that will allow you to update and create new songs
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

    //******************************************HELPER*METHOD********************************************
    public void refreshPlaylists() throws Exception { // Refreshes the playlist and songs in playlist table views.
        if (currentPlaylist == null) {
            currentPlaylist = PlaylistModel.getObservablePlaylists().getFirst();
        }
        playlistSongModel.playlistSongs(currentPlaylist);
        tblSongsInPlaylist.setItems(playlistSongModel.getObservablePlaylistsSong());
        tblPlaylist.refresh();
        tblSongsInPlaylist.refresh();
    }

    public void refreshSongList() throws Exception { // Refreshes the song list by clearing all items and reinserting them
        tblSongs.getItems().clear();
        tblSongs.setItems(songModel.updateSongList());
        tblSongs.refresh();
    }

    public int getRandomSong() { // Fetches a random song from the currently selected song list, which is either the song list or song in playlist
        int min = 0;
        int max = currentSongList.size();
        int range = max - min;
        return (int) (Math.random() * range) + min;
    }

    public void seekCurrentMusic10Plus() { // Goes 10 seconds forwards in the currently playing song
        if (currentMusic != null) {
            currentMusic.seek(Duration.seconds(sliderProgressSong.getValue() + 10));
        }
    }

    public void seekCurrentMusic10Minus() { // Goes 10 seconds backwards in the currently playing song
        if (currentMusic != null) {
            currentMusic.seek(Duration.seconds(sliderProgressSong.getValue() - 10));
        }
    }

    //******************************************DRAG*DROP************************************************
    @FXML
    private void initializeDragAndDrop() { // Sets up the drag & drop functionality for the entire program
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

        tblPlaylist.setOnDragOver(event -> { // Allowing drop on playlist only if the source not is tblSongsInPlaylist and has a string
            if (event.getGestureSource() != tblSongsInPlaylist && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        tblSongsInPlaylist.setOnDragOver(event -> { // Allowing drop on playlist song list only if the source not is tblSongsInPlaylist and has a string
            if (event.getGestureSource() != tblSongsInPlaylist && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        anchorPane.setOnDragOver(event -> { // Allowing drop on anchor pane only if the source is tblSongsInPlaylist and has a string
            if (Objects.equals(currentTableview, "tblSongsInPlaylist")) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

         tblPlaylist.setRowFactory(tv -> new TableRow<>() {
             @Override
             public void updateItem(Playlist playlist, boolean empty) { // Adds the currently playing functionality to the tblPlaylist
                 super.updateItem(playlist, empty);
                 if (playlist != null) {
                     if (playlist.equals(currentPlaylistPlaying) && currentPlaylistPlaying == currentPlaylist) {
                         setStyle(playingGraphic);
                     } else {
                         setStyle("");
                     }
                 } else {
                     setStyle("");
                 }

                 setOnMouseClicked(event -> { // Adds dynamic functionality ofr the tblSongsInPlaylist, so it gets hidden when no playlist is selected
                     if (event.getButton() == MouseButton.PRIMARY) {
                         if (getIndex() >= tblPlaylist.getItems().size()) {
                             tblSongsInPlaylist.getItems().clear();
                             tblSongsInPlaylistVBOX.setManaged(false);
                             tblSongsInPlaylistVBOX.setVisible(false);

                         } else {
                             try { // Makes the tblSongsInPlaylist visible when a playlist is selected
                                 currentPlaylist = getItem();
                                 tblSongsInPlaylistVBOX.setManaged(true);
                                 tblSongsInPlaylistVBOX.setVisible(true);
                                 refreshPlaylists();
                             } catch (Exception e) {
                                 throw new RuntimeException(e);
                             }
                         }
                     }
                     if (event.getButton() == MouseButton.SECONDARY) { // Adds the context menu functionality to the tblPlaylist
                         contextMenuPlaylist.getItems().clear();
                         currentPlaylist = getItem();
                         if (getIndex() >= tblPlaylist.getItems().size()) {
                             contextMenuPlaylist.getItems().addAll(createPlaylist);
                         } else {
                             contextMenuPlaylist.getItems().addAll(createPlaylist, updatePlaylist, deletePlaylist, deleteAllSongs);
                         }
                     }
                 });
                 setOnDragDropped(event -> { // Allows you to drag a song from tblSong onto the tblPlaylist to add it to the playlist
                     Dragboard db = event.getDragboard();
                     boolean success = false;

                     if (db.hasString()) {
                         int songId = Integer.parseInt(db.getString());
                         Song selectedSong = songModel.getSongById(songId);

                         // Access the data associated with the target row
                         currentPlaylist = getItem();

                         try {
                             refreshPlaylists();
                         } catch (Exception e) {
                             throw new RuntimeException(e);
                         }

                         if (selectedSong != null) {
                             try {
                                 if (playlistSongModel.addSongToPlaylist(selectedSong, currentPlaylist)) { //We first need to make sure it not already in the playlist
                                     tblPlaylist.getSelectionModel().select(currentPlaylist);
                                     playlistSongModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());
                                     refreshPlaylists();
                                     success = true;

                                 } else {
                                     Alert alert = new Alert(Alert.AlertType.ERROR);
                                     alert.setTitle("Playlist System");
                                     alert.setHeaderText("The song is already in the playlist");
                                     Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                                     stage.getIcons().add(mainIcon);
                                     alert.showAndWait();
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
             }
         });

        tblSongsInPlaylist.setOnDragDropped(event -> { //When user drop a song from song list into playlist song
            Dragboard db = event.getDragboard();
            boolean success = false;


            if (db.hasString() && Objects.equals(currentTableview, "tblSongs")) {
                int songId = Integer.parseInt(db.getString());
                Song selectedSong = songModel.getSongById(songId);

                if (selectedSong != null) {
                    try { // We check is everything is what it should be display error if all its fine we add the song
                        if (currentPlaylist == null) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Playlist System");
                            alert.setHeaderText("You need to choose a playlist");
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(mainIcon);
                            alert.showAndWait();
                        } else if (playlistSongModel.addSongToPlaylist(selectedSong, currentPlaylist)) { //We first need to make sure it not already in the playlist
                            playlistSongModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());

                            tblPlaylist.refresh();
                            success = true;
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Playlist System");
                            alert.setHeaderText("The song is already in the playlist");
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(mainIcon);
                            alert.showAndWait();
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

        anchorPane.setOnDragDropped(event -> { // When user drop a song from playlist song list into background
            if (draggedSong != null) {
                currentPlaylist.setSongCount(currentPlaylist.getSongCount() - 1);
                currentPlaylist.setSongTotalTime(currentPlaylist.getSongTotalTime() - draggedSong.getSongLength());
                tblPlaylist.refresh(); // We remove in the playlist and update playlist song count and total time
                try {
                    playlistSongModel.deleteSongFromPlaylist(draggedSong, currentPlaylist);
                    draggedSong = null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            anchorPane.requestFocus();
            event.setDropCompleted(true);
            event.consume();
        });

        tblSongsInPlaylist.setRowFactory(tv -> new TableRow<>() {
            @Override
            public void updateItem(Song song, boolean empty) { // Enables the current song playing colour function in the songs in playlist
                super.updateItem(song, empty);
                if (song != null) {
                    if (song.equals(currentSongPlaying) && currentPlaylistPlaying == currentPlaylist) {
                        setStyle(playingGraphic);
                    } else {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }


                // Handle mouse events here
                setOnMouseClicked(event -> { // Enables the dynamic context menu functionality
                    tblSongsInPlaylist.refresh();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        contextMenuSongsInPlaylist.getItems().clear();
                        currentSong = getItem();
                        if (getIndex() >= getTableView().getItems().size()) {
                            contextMenuSongsInPlaylist.getItems().clear();
                        } else {
                            contextMenuSongsInPlaylist.getItems().addAll(deleteSongInPlaylist);
                        }
                    }
                });

                setOnDragDetected(event -> { // Starts the drag function in the tblSongsInPlaylist
                    if (event.getButton() == MouseButton.PRIMARY && !isEmpty()) {
                        draggedSong = getTableView().getSelectionModel().getSelectedItem();
                        currentTableview = "tblSongsInPlaylist";
                        int index = getIndex();
                        Dragboard db = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString(Integer.toString(index));
                        db.setContent(cc);
                        event.consume();
                    }
                });

                setOnDragOver(event -> {
                    Dragboard db = event.getDragboard();
                    if (db.hasString()) {
                        int draggedIndex = Integer.parseInt(db.getString());
                        if (isEmpty() || getIndex() != draggedIndex) {
                            event.acceptTransferModes(TransferMode.MOVE);
                            event.consume();
                        }
                    }
                });

                setOnDragDropped(event -> {                     // Ensures you cant put songs from tblSongInPlaylist back into the tblSong,
                    if (!currentTableview.equals("tblSongs")) { // but only drop them on empty space to delete them or move them around the playlist itself
                        Dragboard db = event.getDragboard();
                        if (db.hasString()) {
                            int draggedIndex = Integer.parseInt(db.getString());
                            int dropIndex = isEmpty() ? getTableView().getItems().size() : getIndex();

                            if (dropIndex == getTableView().getItems().size()) { //Make sure you can not drop a song to an empty spot
                                event.setDropCompleted(false);
                                event.consume();
                                return;
                            }
                            if (dropIndex != draggedIndex) { // Perform the reordering in your data model
                                moveSongInPlaylist(draggedIndex, dropIndex);
                                event.setDropCompleted(true);
                                getTableView().getSelectionModel().select(dropIndex);
                                event.consume();
                            }
                        }
                    }
                });
            }
        });
        tblSongsInPlaylist.refresh();
    }

    public void moveSongInPlaylist(int fromIndex, int toIndex) { // Method for moving songs around the playlist to change the play order
        Song selectedSong = tblSongsInPlaylist.getItems().get(fromIndex);  // Get the selected song
        Song oldSong = playlistSongModel.getObservablePlaylistsSong().get(toIndex);  // Get song there was before

        if (toIndex == tblSongsInPlaylist.getItems().size() - 1) {
            tblSongsInPlaylist.getItems().add(tblSongsInPlaylist.getItems().size(), selectedSong);
            selectedSong = tblSongsInPlaylist.getItems().remove(fromIndex);
        } else if (toIndex != tblSongsInPlaylist.getItems().size() - 1) {
            selectedSong = tblSongsInPlaylist.getItems().remove(fromIndex);
            tblSongsInPlaylist.getItems().add(toIndex, selectedSong);
        }
        try {
            playlistSongModel.updateSongInPlaylist(selectedSong, oldSong, currentPlaylist);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //*******************************************KEYBOARD**************************************************
    Set<KeyCode> pressedKeys = new HashSet<>();

    @FXML
    private void keyboardKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }

    @FXML
    private void keyboardKeyPressed(KeyEvent event) throws Exception { // Controls keyboard Functionality for the window

        KeyCode keyCode = event.getCode(); //Get the button press value
        pressedKeys.add(event.getCode());
        if (event.getCode() == KeyCode.SPACE) { // Tries to pause the currently playing song or start the playing song if it was paused
            event.consume();
            tblSongs.getSelectionModel().clearSelection();
            tblPlaylist.getSelectionModel().clearSelection();
            tblSongsInPlaylist.getSelectionModel().clearSelection();
            btnPlay.requestFocus();
            togglePlayPause();
        }

        if (keyCode == KeyCode.ESCAPE) { // Clears all selections when escape is pressed
            tblSongs.getSelectionModel().clearSelection();
            tblPlaylist.getSelectionModel().clearSelection();
            tblSongsInPlaylist.getSelectionModel().clearSelection();
            anchorPane.requestFocus();
        }

        if (event.isControlDown()) { // Checks if control key is held down
            if (keyCode == KeyCode.LEFT) { // Tries to move 10 seconds backwards in the currently playing song
                sliderProgressSong.requestFocus();
                seekCurrentMusic10Minus();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.RIGHT) { // Tries to move 10 seconds forwards in the currently playing song
                sliderProgressSong.requestFocus();
                seekCurrentMusic10Plus();
            }
        }

        if (!isSecreteModeActive) { //Disable key under media
            if (keyCode == KeyCode.DELETE) { // Tries to delete the selected song, playlist or song in playlist. Will still show relevant warning screen
                handleDelete();
            }

            if (event.isControlDown()) {
                if (keyCode == KeyCode.H) { // Enable/Disable shuffle song function
                    btnShuffleSong();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.T) { // Enable/Disable repeat song function
                    btnRepeatSong();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.B) { // Go to previously played song
                    btnBackwardSong();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.F) { // Go to next song to be played
                    btnForwardSong();
                }
            }

            if (event.isControlDown()) {
                if (keyCode == KeyCode.P) { // Opens the create new playlist dialog box
                    btnCreatePlaylistNow();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.O) { // Tries to update the currently selected playlist
                    btnUpdatePlaylistNow();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.C) { // Opens the create song window
                    btnNewWindowCreate();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.U) { // Tries to open the update song window with information from the currently selected song
                    btnNewWindowUpdate();
                }
            }
            // A secret attempt at a semi working movie Player - Changes the layout of the GUI to show the media player
            // When shift + M + T is clicked at the same time - Only works up to 1 gb size files
            if (pressedKeys.contains(KeyCode.SHIFT) && pressedKeys.containsAll(Arrays.asList(KeyCode.M, KeyCode.T))) {
                lastSongName = currentSongPlaying;
                lastSong = currentMusic;
                lastSongSeek = sliderProgressSong.getValue();
                System.out.println("Secret combination pressed");
                repeatMode = 2;
                isSecreteModeActive = true;
                btnRepeatIcon.setImage(repeatDisableIcon);
                btnShuffleIcon.setImage(shuffleIconDisable);
                btnVideo.setVisible(true);
                btnRepeat.setDisable(true);
                btnShuffle.setDisable(true);
                txtSongSearch.setVisible(false);
                vboxTblBtn.setVisible(false);
                anchorPane.setStyle("-fx-background-color: #6f787e;");
                mediaView.setMediaPlayer(mp);
                isMusicPaused = false;
                handleNewSong(mp, currentMovie);
                mediaView.fitWidthProperty().bind(mediaViewBox.widthProperty());
                mediaView.fitHeightProperty().bind(mediaViewBox.heightProperty());
            }
        }
        // Closes the movie player and attempts to reset everything to how it was before it opened which includes trying to find how far along you were
        // in your song if one was playing
        else if (pressedKeys.contains(KeyCode.SHIFT) && pressedKeys.containsAll(Arrays.asList(KeyCode.M, KeyCode.T))) {
            isSecreteModeActive = false;
            System.out.println("Secret system closed");
            repeatMode = 0;
            btnRepeatIcon.setImage(repeatDisableIcon);
            btnShuffleIcon.setImage(repeatDisableIcon);
            btnVideo.setVisible(false);
            btnRepeat.setDisable(false);
            btnShuffle.setDisable(false);
            txtSongSearch.setVisible(true);
            vboxTblBtn.setVisible(true);
            anchorPane.setStyle("");
            mediaView.setMediaPlayer(null);
            mediaView.fitWidthProperty().bind(mediaViewBox.widthProperty());
            mediaView.fitHeightProperty().bind(mediaViewBox.heightProperty());
            if (lastSong != null)   {
                handleNewSong(lastSong, lastSongName);
                Platform.runLater(() -> sliderProgressSong.setValue(lastSongSeek));
            }
            else {
                currentMusic.seek(Duration.millis(1000000000)); //So its 100% is done
            }
        }
    }

    public void btnNewVideo() { // Semi working movie player - Can sometimes take two attempted uploads for the movie to load
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi");
        fileChooser.getExtensionFilters().add(extFilter);
        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            MediaPlayer previousMediaPlayer = soundMap.remove(currentMovie.getId());
            currentMovie = new Song(-50, 0, selectedFile.getName(), "Unknown", selectedFile.getAbsolutePath(), 0.0, "Movie");
            addSongsToSoundMap(currentMovie).thenRun(() -> {
                MediaPlayer newMovie = soundMap.get(currentMovie.getId());
                sliderProgressSong.setValue(0);
                mediaView.setMediaPlayer(newMovie);
                handleNewSong(newMovie, currentMovie);
                if (previousMediaPlayer != null) {
                    previousMediaPlayer.dispose(); //Clear up resources
                }
            });

        }

    }

    MediaPlayer mp = new MediaPlayer(new Media(new File("resources/Videos/SECRET.mp4").toURI().toString()));
    Song currentMovie = new Song(-50, 2013, "Choose a", "Movie", "resources/Videos/SECRET.mp4", 0.0, "Movie");
    boolean isSecreteModeActive = false;
    MediaPlayer lastSong;
    Song lastSongName;
    double lastSongSeek;
    //******************************************BUTTONS*SLIDERS************************************************
    public void createUpdatePlaylist(String buttonText) throws Exception { // Method for updating or creating playlists
        TextInputDialog dialog = new TextInputDialog("");
        if (currentPlaylist == null && buttonText.equals(btnUpdatePlaylist.getText())) { // Checks if a valid playlist was selected when trying to update
            displayErrorModel.displayErrorC("You forgot choose a playlist");
            return;
        }

        if (buttonText.equals(btnCreatePlaylist.getText())) { // If you clicked create it will open a dialog box and ask you for a name
            dialog.setTitle("New Playlist");
            dialog.setHeaderText("What do you want to call your new Playlist");
        }
        if (buttonText.equals(btnUpdatePlaylist.getText())) { // If you clicked update it will open a dialog box and ask you what the selected playlist should be called instead
            dialog.setTitle("Update Playlist " + currentPlaylist.getPlaylistName());
            dialog.setHeaderText("What would you like to rename the Playlist");
        }

        // Set the icon for the dialog window
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String inputValue = result.get().strip(); // Get the actual value from Optional and ensure that all white space is removed in front and behind the text input

            if (buttonText.equals(btnCreatePlaylist.getText()) && !inputValue.isEmpty()) {
                Playlist p = new Playlist(-1, inputValue, 0, 0);
                playlistModel.createNewPlaylist(p);
            }
            if (buttonText.equals(btnUpdatePlaylist.getText()) && !inputValue.isEmpty()) {
                currentPlaylist.setPlaylistName(inputValue);
                playlistModel.updatePlaylist(currentPlaylist);
            }
            if (inputValue.isEmpty()) {
                displayErrorModel.displayErrorC("You need to input a valid name for your playlist");
            }
        }
        refreshPlaylists();
    }

    public void btnCreatePlaylistNow() throws Exception { // Functionality for context menu
        createUpdatePlaylist(btnCreatePlaylist.getText());
    }

    public void btnUpdatePlaylistNow() throws Exception { // Functionality for context menu
        createUpdatePlaylist(btnUpdatePlaylist.getText());
    }

    public void btnNewWindowCreate() throws IOException { // Opens the CU FXML window to create a new song
        MediaPlayerCUViewController.setTypeCU(1);
        newUCWindow("Song Creator");
    }

    public void btnNewWindowUpdate() throws IOException { // Opens the CU FXML window to update a new song, attempts to get all relevant information to input into new window
        MediaPlayerCUViewController.setTypeCU(2);
        if (currentSong == null) {
            btnNewWindowCreate();
            return;
        } //If user want to update but forgot a song they can create a new one instead
        currentSong = tblSongs.getSelectionModel().getSelectedItem();
        newUCWindow("Song Updater");
    }

    public void btnDelete() { // Calls the delete function to try and delete the selected song, playlist or song in playlist with relevant warnings where applicable
        handleDelete();
    }

    public void btnShuffleSong() { // Changes the shuffle mode and shuffle icon when clicked
        if (btnShuffleIcon.getImage().equals(shuffleIcon)) {
            btnShuffleIcon.setImage(shuffleIconDisable);
            shuffleMode = 0; //Execute shuffle disable method
        } else if (btnShuffleIcon.getImage().equals(shuffleIconDisable)) {
            btnShuffleIcon.setImage(shuffleIcon);
            shuffleMode = 1; //Execute shuffle method
        }
    }

    public void btnBackwardSong() { // Moves backwards a song in the currently selected table view
        previousPress = true;
        handleSongSwitch(currentIndex - 1 + currentSongList.size());
    }

    public void btnPlaySong() {
        handleSongPlay();
    }

    public void btnForwardSong() { // Moves forward a song in the currently selected table view
        previousPress = false;
        handleSongSwitch(currentIndex + 1);
    }

    public void btnRepeatSong() { // Enables or disables the repeat mode and sets the icon to the relevant one
        if (btnRepeatIcon.getImage().equals(repeat1Icon)) {
            btnRepeatIcon.setImage(repeatDisableIcon);
            repeatMode = 0;     //Change repeat mode to disabled so system know
        } else if (btnRepeatIcon.getImage().equals(repeatDisableIcon)) {
            btnRepeatIcon.setImage(repeatIcon);
            repeatMode = 1;  //Change repeat mode to repeat same song so system know
        } else if (btnRepeatIcon.getImage().equals(repeatIcon)) {
            btnRepeatIcon.setImage(repeat1Icon);
            repeatMode = 2;    //Change repeat mode to repeat playlist so system know
        }
    }

    public void btnSpeedSong() { // Here we change the songs speed
        if (currentSpeedIndex == speeds.size())  {
            currentSpeedIndex = -1;
        }
        currentSpeedIndex = (currentSpeedIndex + 1) % speeds.size();
        currentSpeed = speeds.get(currentSpeedIndex);
        btnSpeed.setText(currentSpeed + "x");

        // Set the media player's playback speed to the new speed
        if (currentMusic != null) {
            currentMusic.setRate(currentSpeed);
        }
    }


    public void onSlideProgressPressed() { // Tries to move the song progress to the selected duration
        if (currentMusic != null) {
            isUserChangingSlider = true; // This prevents the system from trying to update itself
            currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()));
            currentMusic.pause();
        }
    }

    public void onSlideProgressReleased() { // Tries to start the song when the user release the slider at a location
        if (currentMusic != null) {
            if (!isMusicPaused) {
                currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()));
                currentMusic.play();
                isUserChangingSlider = false; // This prevents the system from trying to update itself
                anchorPane.requestFocus();
            }
        }
    }

    //******************************************STYLING*SLIDERS************************************************
    private void setSliderVolumeStyle() { // Sets the CSS for the volume slider
        double percentage = sliderProgressVolume.getValue() / (sliderProgressVolume.getMax() - sliderProgressVolume.getMin());
        String color = String.format(Locale.US, "-fx-background-color: linear-gradient(to right, #038878 0%%, #038878 %.2f%%, " +
                "#92dc9b %.2f%%, #92dc9b 100%%);", percentage * 100, percentage * 100);
        sliderProgressVolume.lookup(".track").setStyle(color);
    }

    private void setSliderSongProgressStyle() { // Sets the css for the song progress slider
        double percentage = sliderProgressSong.getValue() / (sliderProgressSong.getMax() - sliderProgressSong.getMin());
        String color = String.format(Locale.US, "-fx-background-color: linear-gradient(to right, #04a650 0%%, #04a650 %.10f%%, " +
                "#92dc9b %.10f%%, #92dc9b 100%%);", percentage * 100, percentage * 100);
        sliderProgressSong.lookup(".track").setStyle(color);
    }
}