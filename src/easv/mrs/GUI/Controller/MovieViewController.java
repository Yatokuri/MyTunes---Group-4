package easv.mrs.GUI.Controller;

import easv.mrs.BE.Movie;
import easv.mrs.GUI.Model.MovieModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MovieViewController implements Initializable {


    public TextField txtMovieSearch;
    public ListView<Movie> lstMovies;

    @FXML
    private Button btnCreate, btnUpdate;

    @FXML
    private TableColumn<Movie, String> colTitle;
    @FXML
    private TableColumn<Movie, Integer> colYear;

    @FXML
    private TableView<Movie> tblMovies;

    @FXML
    private TextField txtTitle, txtYear;

    private MovieModel movieModel;

    public MovieViewController()  {

        try {
            movieModel = new MovieModel();
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
        lstMovies.setItems(movieModel.getObservableMovies());
        tblMovies.setItems(movieModel.getObservableMovies());

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
                movieModel.searchMovie(newValue);
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
        int year = Integer.parseInt(txtYear.getText());

        // create movie object to pass to method
        Movie newMovie = new Movie(-1, year, title);

        try {
            movieModel.createNewMovie(newMovie);
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
    public void updateMovie(ActionEvent actionEvent) {
        Movie selectedMovie = tblMovies.getSelectionModel().getSelectedItem();

        if (selectedMovie != null)
        {
            // update movie based on textfield inputs from user
            selectedMovie.setTitle(txtTitle.getText());
            selectedMovie.setYear(Integer.parseInt(txtYear.getText()));

            try {
                // Update movie in DAL layer (through the layers)
                movieModel.updateMovie(selectedMovie);

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
    public void deleteMovie(ActionEvent actionEvent) {
        Movie selectedMovie = tblMovies.getSelectionModel().getSelectedItem();

        if (selectedMovie != null)
        {
            try {
                // Delete movie in DAL layer (through the layers)
                movieModel.deleteMovie(selectedMovie);
            }
            catch (Exception e) {
                displayError(e);
                e.printStackTrace();
            }
        }
    }
}
