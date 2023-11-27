package easv.mrs.GUI.Controller;

import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.SongModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MediaPlayerViewController implements Initializable {


    public TextField txtMovieSearch;
    public ListView<Song> lstMovies;

    @FXML
    private Button btnCreate, btnUpdate;

    @FXML
    private TableColumn<Song, String> colTitle;
    @FXML
    private TableColumn<Song, Integer> colYear;

    @FXML
    private TableView<Song> tblMovies;

    @FXML
    private TextField txtTitle, txtYear;

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
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));

        // add data from observable list
        lstMovies.setItems(songModel.getObservableMovies());
        tblMovies.setItems(songModel.getObservableMovies());

        // table view listener (when user selects a movie in the tableview)
        tblMovies.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtTitle.setText(newValue.getTitle());
            txtYear.setText(Integer.toString(newValue.getYear()));
        });

        // list view listener (when user selects a movie in the listview)
        lstMovies.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtTitle.setText(newValue.getTitle());
            txtYear.setText(Integer.toString(newValue.getYear()));
        });

        // Setup context search
        txtMovieSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
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
        String title = txtTitle.getText();
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
        Song selectedSong = tblMovies.getSelectionModel().getSelectedItem();

        if (selectedSong != null)
        {
            // update movie based on textfield inputs from user
            selectedSong.setTitle(txtTitle.getText());
            selectedSong.setYear(Integer.parseInt(txtYear.getText()));

            try {
                // Update movie in DAL layer (through the layers)
                songModel.updateSong(selectedSong);

                // ask controls to refresh their content
                lstMovies.refresh();
                tblMovies.refresh();
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
        Song selectedSong = tblMovies.getSelectionModel().getSelectedItem();

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
}
