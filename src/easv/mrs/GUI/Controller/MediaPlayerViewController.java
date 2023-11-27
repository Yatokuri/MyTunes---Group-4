package easv.mrs.GUI.Controller;

import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.SongModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MediaPlayerViewController implements Initializable {


    public TextField txtSongSearch;
    public ListView<Song> lstSongs;
    public TableView tblMovies;
    public Button btnDelete;
    public Slider sliderProgressSong;
    public Button btnPlay;

    private MediaPlayer currentMusic = null;
    private final Map<Integer, MediaPlayer> soundMap = new HashMap<>(); //Every song have there unique id

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
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));

        // add data from observable list
        lstSongs.setItems(songModel.getObservableMovies());
        tblSongs.setItems(songModel.getObservableMovies());

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
        int year = Integer.parseInt(txtYear.getText());

        // create movie object to pass to method
        Song newSong = new Song(-1, year, title, artist);

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
                // Update movie in DAL layer (through the layers)
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
                songModel.deleteMovie(selectedSong);
            }
            catch (Exception e) {
                displayError(e);
                e.printStackTrace();
            }
        }
    }

    public void onSlideProgressPressed(KeyEvent keyEvent) {
    }

    public void playSong(ActionEvent actionEvent) {


        soundMap.put(1, new MediaPlayer(new Media(new File("data/MGP – fællessang (LIVE) _ MGP 2020.mp3").toURI().toString())));
        MediaPlayer currentMusic = soundMap.get(1);
        currentMusic.play();
    }

    public void onSlideProgressReleased(KeyEvent keyEvent) {
    }
}
