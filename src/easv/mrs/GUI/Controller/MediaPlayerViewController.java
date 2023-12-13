package easv.mrs.GUI.Controller;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
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
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
    private VBox tblSongsInPlaylistVBOX;
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
    private Button btnCreatePlaylist, btnUpdatePlaylist, btnPlay, btnShuffle, btnForward, btnBackward, btnRepeat;
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
    private int currentIndex = 0;
    private boolean previousPress = false;
    private Playlist currentPlaylist, currentPlaylistPlaying; //The current playing selected and playing from
    private Song currentSong, currentSongPlaying; //The current Song selected and playing
    private SongModel songModel;
    private PlaylistModel playlistModel;
    private SongPlaylistModel songPlaylistModel;
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

    public MediaPlayerViewController()  {
        instance = this;
        displayErrorModel = new DisplayErrorModel();
        try {
            songModel = new SongModel();
            playlistModel = new PlaylistModel();
            songPlaylistModel = new SongPlaylistModel();
            currentPlaylist = null;
        }
        catch (Exception e) {
            displayErrorModel.displayError(e);
            e.printStackTrace();
        }
    }

    public static MediaPlayerViewController getInstance()  {
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
        tblSongsInPlaylist.setPlaceholder(new Label("No songs found"));
        tblSongs.setPlaceholder(new Label("No songs found"));
        tblSongsInPlaylistVBOX.setManaged(false);


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

    ContextMenu contextMenuSongsInPlaylist = new ContextMenu();
    ContextMenu contextMenuPlaylist = new ContextMenu();
    ContextMenu contextMenuSongs = new ContextMenu();
    MenuItem delete = new MenuItem("Delete");
    MenuItem createPlaylist = new MenuItem("Create Playlist");
    MenuItem updatePlaylist = new MenuItem("Update Playlist");
    MenuItem deleteAllSongs = new MenuItem("Delete All Songs");
    Menu playlistSubMenu = new Menu("Add to Playlist");

    private void contextSystemSubMenuAddSongs()   {
        playlistSubMenu.getItems().clear();
        PlaylistModel.getObservablePlaylists().forEach(playlist -> {
            MenuItem playlistItem = new MenuItem(playlist.getPlaylistName());
            playlistItem.setUserData(playlist); // Store the playlist as user data
            playlistSubMenu.getItems().add(playlistItem);
        });
    }

    private void contextSystem() {
        MenuItem createSong = new MenuItem("Create Song");
        MenuItem updateSong = new MenuItem("Update Song");


        contextMenuPlaylist.getItems().addAll(createPlaylist, updatePlaylist, delete, deleteAllSongs);
        contextMenuSongs.getItems().addAll(createSong, updateSong, delete);
        contextMenuSongsInPlaylist.getItems().addAll();

        tblSongs.setContextMenu(contextMenuSongs);
        tblSongsInPlaylist.setContextMenu(contextMenuSongsInPlaylist);
        tblPlaylist.setContextMenu(contextMenuPlaylist);

        tblSongs.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    currentSong = row.getItem();
                }
                else if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenuSongs.getItems().clear();
                    currentSong = row.getItem();
                    if (row.getIndex() >= tblSongs.getItems().size()) {
                        contextMenuSongs.getItems().addAll(createSong);
                    } else {
                        contextSystemSubMenuAddSongs(); //We update the playlist list
                        contextMenuSongs.getItems().addAll(createSong, updateSong, delete, new SeparatorMenuItem(), playlistSubMenu );
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
                    currentPlaylist = getPlaylistById(selectedPlaylist.getId());
                }
                songPlaylistModel.playlistSongs(currentPlaylist);

                if (songPlaylistModel.addSongToPlaylist(currentSong, currentPlaylist)) { //We first need to make sure it not already in the playlist
                    tblPlaylist.getSelectionModel().select(currentPlaylist);
                    songPlaylistModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());
                    refreshEverything();
                    contextMenuSongs.hide();
                }
                else {
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

        delete.setOnAction((event) -> {
            try {
                handleDelete();
                contextMenuSongs.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        deleteAllSongs.setOnAction((event) -> {
            try {
                songPlaylistModel.deleteAllSongsFromPlaylist(tblPlaylist.getSelectionModel().getSelectedItem());
                refreshEverything();
                contextMenuPlaylist.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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

   private CompletableFuture<Void> addSongsToSoundMap(Song s) {
        CompletableFuture<Void> future = new CompletableFuture<>(); //We use to make sure the Media player get 100% done before going next
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
    private void handleSongPlay() {
        Song selectedSong;
        if (currentMusic != null) {
            selectedSong = null;
            togglePlayPause();
        } else if (tblSongs.getSelectionModel().getSelectedItem() != null)
            selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        else if (tblSongsInPlaylist.getSelectionModel().getSelectedItem() != null) {
            selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
            currentSongList = songPlaylistModel.getObservablePlaylistsSong();
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
    private void playSongFromTableView() {
        tblSongs.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY)
                tblSongsInPlaylist.getSelectionModel().clearSelection();
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) { // Check for double-click
                currentSongList = songModel.getObservableSongs();
                currentIndex = currentSongList.indexOf(tblSongs.getSelectionModel().getSelectedItem());
                currentPlaylistPlaying = null;
                Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    sliderProgressSong.setValue(0);
                    isMusicPaused = false;
                    PlaySong(selectedSong);
                }
            }
        });
    }

    private void playSongFromTableViewPlaylist() {
        tblSongsInPlaylist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                tblSongs.getSelectionModel().clearSelection();
            }
            if (event.getClickCount() == 2) { // Check for double-click
                currentSongList = songPlaylistModel.getObservablePlaylistsSong();
                currentIndex = currentSongList.indexOf(tblSongsInPlaylist.getSelectionModel().getSelectedItem());
                currentPlaylistPlaying = currentPlaylist;
                Song selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();

                if (selectedSong != null) {
                    sliderProgressSong.setValue(0);
                    isMusicPaused = false;
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



    private void togglePlayPause() {
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
        setStyle(playingGraphic);
        }
        }
        });
         tableView.refresh();
    }



    private void handlePlayingSongColor()    {
        if (currentPlaylistPlaying == null && currentSongPlaying != null) {
           // changeRowColor(tblSongsInPlaylist, -1);
            changeRowColor(tblSongs, currentIndex);
        }

        else if (currentPlaylist == currentPlaylistPlaying){
           // changeRowColor(tblSongsInPlaylist, currentIndex);
            changeRowColor(tblSongs, -1);
        }
        else {
          //changeRowColor(tblSongsInPlaylist, -1);
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
        addSongsToSoundMap(song).thenRun(() -> {
            MediaPlayer newMusic = soundMap.get(song.getId());
            if (newMusic == null) {//Will this work
                soundMap.put(song.getId(), new MediaPlayer(new Media(new File("resources/Sounds/missingFileErrorSound.mp3").toURI().toString())));
                newMusic = soundMap.get(song.getId());
                sliderProgressSong.setValue(0);
            }
            handleNewSong(newMusic, song);
        });
    }

    private void handleSongSwitch(int newIndex) {
        if (shuffleMode == 1) { //If shuffle is enable shuffle
            shuffleMode();
            return;
        }
        if (repeatMode == 0 && currentSongList !=  songModel.getObservableSongs()) {//If repeat is disable do it
            if (repeatModeDisable())  {
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
            return;
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

    public void shuffleMode() {
        if (repeatMode == 0 && currentSongList !=  songModel.getObservableSongs()) { // Get a list of playlists with songs inside itself
            List<Playlist> nonEmptyPlaylists = PlaylistModel.getObservablePlaylists().stream()
                    .filter(playlist -> playlist.getSongCount() >= 1).toList();

            if (!nonEmptyPlaylists.isEmpty()) {
                Random random = new Random(); //Select a random playlist from playlists
                currentPlaylist = nonEmptyPlaylists.get(random.nextInt(nonEmptyPlaylists.size()));
                currentPlaylistPlaying = currentPlaylist;
                try {
                    songPlaylistModel.playlistSongs(currentPlaylist);
                    currentSongList = songPlaylistModel.getObservablePlaylistsSong();
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

    public void handleDelete(){
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
                refreshEverything();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
//******************************************HELPER*METHOD********************************************
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

    public int getRandomSong(){
        int min = 0;
        int max = currentSongList.size();
        int range = max - min;
        return (int)(Math.random() * range) + min;
    }

    private Song getSongById(int songId) { //This is not right layer!
        for (Song s : songModel.getObservableSongs()) {
            if (s.getId() == songId) {
                return s;
            }
        }
        return null;
    }

    private Playlist getPlaylistById(int plId) { //This is not right layer!
        for (Playlist pl : PlaylistModel.getObservablePlaylists()) {
            if (pl.getId() == plId) {
                return pl;
            }
        }
        return null;
    }

    public void seekCurrentMusic10Plus() {
        if (currentMusic != null) {
            currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()+10));
        }
    }

    public void seekCurrentMusic10Minus() {
        if (currentMusic != null) {
            currentMusic.seek(Duration.seconds(sliderProgressSong.getValue()-10));
        }
    }

//******************************************DRAG*DROP************************************************
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

        tblPlaylist.setOnDragOver(event -> {
            if (event.getGestureSource() != tblSongsInPlaylist && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
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

        tblPlaylist.setRowFactory(tv -> {
            TableRow<Playlist> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (row.getIndex() >= tblPlaylist.getItems().size()) {
                        tblSongsInPlaylist.getItems().clear();
                        tblSongsInPlaylistVBOX.setManaged(false);
                        tblSongsInPlaylistVBOX.setVisible(false);
                    } else {
                        try {
                            currentPlaylist = row.getItem();
                            tblSongsInPlaylistVBOX.setManaged(true);
                            tblSongsInPlaylistVBOX.setVisible(true);
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
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    int songId = Integer.parseInt(db.getString());
                    Song selectedSong = getSongById(songId);

                    // Access the data associated with the target row
                    currentPlaylist = row.getItem();

                    try {
                        refreshEverything();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    if (selectedSong != null) {
                        try {
                            if (songPlaylistModel.addSongToPlaylist(selectedSong, currentPlaylist)) { //We first need to make sure it not already in the playlist
                                tblPlaylist.getSelectionModel().select(currentPlaylist);
                                songPlaylistModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());
                                refreshEverything();
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
            return row;
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
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(mainIcon);
                            alert.showAndWait();
                        } else if (songPlaylistModel.addSongToPlaylist(selectedSong, currentPlaylist)) { //We first need to make sure it not already in the playlist
                            songPlaylistModel.playlistSongs(tblPlaylist.getSelectionModel().getSelectedItem());

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

        tblSongsInPlaylist.setRowFactory(tv -> new TableRow<Song>() {
            @Override
            public void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty) ;
                if (item == null) {
                    setStyle("");
                } else if (item.equals(currentSongPlaying) && currentPlaylistPlaying == currentPlaylist) {
                    setStyle(playingGraphic);

                } else {
                    setStyle("");
                }

                if (currentPlaylistPlaying == currentPlaylist)  {
                    try {
                        tblSongsInPlaylist.refresh();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                // Handle mouse events here
                setOnMouseClicked(event -> {
                    tblSongsInPlaylist.refresh();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        contextMenuSongsInPlaylist.getItems().clear();
                        currentSong = getItem();
                        if (getIndex() >= getTableView().getItems().size()) {
                            contextMenuSongsInPlaylist.getItems().clear();
                        } else {
                            contextMenuSongsInPlaylist.getItems().addAll(delete);
                        }
                    }
                });

                setOnDragDetected(event -> {
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

                setOnDragDropped(event -> {
                    if (!currentTableview.equals("tblSongs")) {
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

//*******************************************KEYBOARD**************************************************
    Set<KeyCode> pressedKeys = new HashSet<>();
    @FXML
    private void keyboardKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }


    @FXML
    private void keyboardKeyPressed(KeyEvent event) throws Exception {
        KeyCode keyCode = event.getCode(); //Get the button press value
        pressedKeys.add(event.getCode());

        if (keyCode == KeyCode.DELETE) {
            handleDelete();
        }
        if (event.getCode() == KeyCode.SPACE) {
            event.consume();
            tblSongs.getSelectionModel().clearSelection();
            tblPlaylist.getSelectionModel().clearSelection();
            tblSongsInPlaylist.getSelectionModel().clearSelection();
            btnPlay.requestFocus();
            togglePlayPause();
        }

        if (keyCode == KeyCode.ESCAPE) {
            tblSongs.getSelectionModel().clearSelection();
            tblPlaylist.getSelectionModel().clearSelection();
            tblSongsInPlaylist.getSelectionModel().clearSelection();
            anchorPane.requestFocus();
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.LEFT) {
                sliderProgressSong.requestFocus();
                seekCurrentMusic10Minus();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.RIGHT) {
                sliderProgressSong.requestFocus();
                seekCurrentMusic10Plus();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.H) {
                btnShuffleSong();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.T) {
                btnRepeatSong();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.B) {
                btnBackwardSong();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.F) {
                btnForwardSong();
            }
        }

        if (pressedKeys.contains(KeyCode.SHIFT) &&
                pressedKeys.containsAll(Arrays.asList(KeyCode.M, KeyCode.T))) {
            System.out.println("Secret combination pressed");
        }

        if (event.isControlDown()) {
            if (keyCode == KeyCode.P) {
                btnCreatePlaylistNow();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.O) {
                btnUpdatePlaylistNow();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.C) {
                btnNewWindowCreate();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.U) {
                btnNewWindowUpdate();
            }
        }
    }
//******************************************BUTTONS*SLIDERS************************************************
    public void createUpdatePlaylist(String buttonText) throws Exception {
        TextInputDialog dialog = new TextInputDialog("");
        if (currentPlaylist == null) {
            displayErrorModel.displayErrorC("You forgot choose a playlist");
            return;
        }

        if (buttonText.equals(btnCreatePlaylist.getText())) {
            dialog.setTitle("New Playlist");
            dialog.setHeaderText("What do you want to call your new Playlist");
        }
        if (buttonText.equals(btnUpdatePlaylist.getText())) {
            dialog.setTitle("Update Playlist " + currentPlaylist.getPlaylistName());
            dialog.setHeaderText("What would you like to rename the Playlist");
        }

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

    public void btnCreatePlaylistNow() throws Exception {
        createUpdatePlaylist(btnCreatePlaylist.getText());
    }

    public void btnUpdatePlaylistNow() throws Exception {
        createUpdatePlaylist(btnUpdatePlaylist.getText());
    }
    public void btnNewWindowCreate() throws IOException {
        MediaPlayerCUViewController.setTypeCU(1);
        newUCWindow("Song Creator");
    }

    public void btnNewWindowUpdate() throws IOException {
        MediaPlayerCUViewController.setTypeCU(2);
        if (currentSong == null)  {btnNewWindowCreate();return;} //If user want to update but forgot a song they can create a new one instead
        currentSong = tblSongs.getSelectionModel().getSelectedItem();
        newUCWindow("Song Updater");
    }
    public void btnDelete() {
        handleDelete();
    }

    public void btnShuffleSong() {
        if (btnShuffleIcon.getImage().equals(shuffleIcon))    {
            btnShuffleIcon.setImage(shuffleIconDisable);
            shuffleMode = 0; //Execute shuffle disable method
        }
        else if (btnShuffleIcon.getImage().equals(shuffleIconDisable))    {
            btnShuffleIcon.setImage(shuffleIcon);
            shuffleMode = 1; //Execute shuffle method
        }
    }

    public void btnBackwardSong() {
        previousPress = true;
        handleSongSwitch(currentIndex - 1 + currentSongList.size());
    }

    public void btnPlaySong() {
        handleSongPlay();
    }

    public void btnForwardSong() {
        previousPress = false;
        handleSongSwitch(currentIndex + 1);
    }

    public void btnRepeatSong() {
        if (btnRepeatIcon.getImage().equals(repeat1Icon))    {
            btnRepeatIcon.setImage(repeatDisableIcon);
            repeatMode = 0;     //Change repeat mode to disabled so system know
        }

        else if (btnRepeatIcon.getImage().equals(repeatDisableIcon))    {
            btnRepeatIcon.setImage(repeatIcon);
            repeatMode = 1;  //Change repeat mode to repeat same song so system know
        }
        else if (btnRepeatIcon.getImage().equals(repeatIcon))    {
            btnRepeatIcon.setImage(repeat1Icon);
            repeatMode = 2;    //Change repeat mode to repeat playlist so system know
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
//******************************************STYLING*SLIDERS************************************************
    private void setSliderVolumeStyle()  {
        double percentage = sliderProgressVolume.getValue() / (sliderProgressVolume.getMax() - sliderProgressVolume.getMin());
        String color = String.format(Locale.US, "-fx-background-color: linear-gradient(to right, #038878 0%%, #038878 %.2f%%, #92dc9b %.2f%%, #92dc9b 100%%);", percentage * 100, percentage * 100);
        sliderProgressVolume.lookup(".track").setStyle(color);
    }
    private void setSliderSongProgressStyle()  {
        double percentage = sliderProgressSong.getValue() / (sliderProgressSong.getMax() - sliderProgressSong.getMin());
        String color = String.format(Locale.US, "-fx-background-color: linear-gradient(to right, #04a650 0%%, #04a650 %.10f%%, #92dc9b %.10f%%, #92dc9b 100%%);", percentage * 100, percentage * 100);
        sliderProgressSong.lookup(".track").setStyle(color);
    }



}