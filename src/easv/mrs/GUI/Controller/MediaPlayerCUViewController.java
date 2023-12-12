/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.GUI.Controller;

import easv.mrs.BE.Category;
import easv.mrs.BE.Song;
import easv.mrs.GUI.Model.CategoryModel;
import easv.mrs.GUI.Model.DisplayErrorModel;
import easv.mrs.GUI.Model.SongModel;
import easv.mrs.GUI.Model.ValidateModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MediaPlayerCUViewController implements Initializable {
    @FXML
    private ComboBox<Category> comCategory;
    @FXML
    private TextField txtInputName, txtInputArtist, txtInputYear, txtInputFilepath, txtInputTime;
    @FXML
    private Button btnSave;
    private MediaPlayerViewController mediaPlayerViewController;
    private long currentSongLength;
    private final SongModel songModel;
    private final DisplayErrorModel displayErrorModel;
    private final CategoryModel categoryModel;
    private final ValidateModel validateModel = new ValidateModel();
    private final BooleanProperty isNameValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isArtistValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isFilepathValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isYearValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isTimeValid = new SimpleBooleanProperty(true);
    private static final Image mainIcon = new Image ("Icons/mainIcon.png");
    private static int typeCU = 0;
    private static Song currentSelectedSong = null;
    private static MediaPlayerCUViewController instance;

    public static void setTypeCU(int typeCU) {MediaPlayerCUViewController.typeCU = typeCU;}

    public MediaPlayerCUViewController() {
        try {
            songModel = new SongModel();
            categoryModel = new CategoryModel();
            displayErrorModel = new DisplayErrorModel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MediaPlayerCUViewController getInstance() {
        if (instance == null) {
            instance = new MediaPlayerCUViewController();
        }
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            mediaPlayerViewController = MediaPlayerViewController.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        currentSelectedSong = mediaPlayerViewController.getCurrentSong();
        addValidationListener(txtInputName, isNameValid);
        addValidationListener(txtInputArtist, isArtistValid);
        addValidationListener(txtInputFilepath, isFilepathValid);
        addValidationListener(txtInputYear, isYearValid);
        addValidationListener(txtInputTime, isTimeValid);
        contextSystem();

        comCategory.getItems().addAll(CategoryModel.getObservableCategories().sorted());
        comCategory.getSelectionModel().select(0);
        comCategory.setOnMouseClicked(event -> { // Consume the event to prevent the ComboBox from opening
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
                comCategory.hide();
            }
        });

        // Add a listener to the filepath input to make sure its valid and update time automatic
        txtInputFilepath.textProperty().addListener((observable, oldValue, newValue) -> {
            txtInputTime.setText("00:00:00"); //Also mean not valid file
            if (validateModel.isValidMediaPath(newValue)) {
                updateTimeText();
            }
        });
        startupSetup();
    }

    public void startupSetup() {
        if (typeCU == 1) { //1 mean create song
            btnSave.setText("Create");
        }
        if (typeCU == 2 & currentSelectedSong != null) { //2 mean update song
            btnSave.setText("Update");
            txtInputName.setText(currentSelectedSong.getTitle());
            txtInputYear.setText(String.valueOf(currentSelectedSong.getYear()));
            txtInputArtist.setText(currentSelectedSong.getArtist());
            txtInputFilepath.setText(currentSelectedSong.getSongPath());
            //comCategory.getSelectionModel().select(currentSelectedSong.getSongCategory());
        }
    }

    private void addValidationListener(TextField textField, BooleanProperty validationProperty) { //Detect change and validate it
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = validateModel.validateInput(textField, newValue);
            validationProperty.set(isValid);
            setBorderStyle(textField, isValid);
        });
    }

    private void updateTimeText() { //We pass the info to ValidateModel class
        MediaPlayer newSong = new MediaPlayer(new Media(new File(txtInputFilepath.getText()).toURI().toString()));
        validateModel.updateTimeText(newSong, formattedTime   -> { //This is because we need to wait because setOnReady is an asynchronous operation,
            String[] parts = formattedTime.split("-"); //We need to split the return because we got time in HH:MM:SS and just seconds
            txtInputTime.setText(parts[0]);
            currentSongLength = Long.parseLong(parts[1]);
        });
    }
//*******************************************CONTEXT*MENU**************************************************
    private void contextSystem() { //Here we created the context menu to category
        ContextMenu contextMenu = new ContextMenu();
        MenuItem createCategory = new MenuItem("Create Category");
        MenuItem deleteCategory = new MenuItem("Delete Category");
        contextMenu.getItems().addAll(deleteCategory,createCategory);
        comCategory.setContextMenu(contextMenu);

        createCategory.setOnAction((event) -> {
            try {
                btnMoreCategory();
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteCategory.setOnAction((event) -> {
            try {
                categoryModel.deleteCategory(comCategory.getSelectionModel().getSelectedItem());
                comCategory.getItems().remove(comCategory.getSelectionModel().getSelectedItem());
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
//*******************************BUTTONS***********************************************
    public void btnChooseFile() { //Use to choose file - We pass the info to ValidateModel class
        txtInputFilepath.setText(validateModel.btnChoose());  //
        if(!txtInputFilepath.getText().isEmpty())
            updateTimeText();
        }

    public void btnMoreCategory() throws Exception { //Use to add a new category

        TextInputDialog dialog = new TextInputDialog("");

        dialog.setTitle("New Category");
        dialog.setHeaderText("What do you want to call your new category");

        // Set the icon for the dialog window
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String inputValue = result.get(); // Get the actual value from Optional
            if (inputValue.length() > 40)   {
                displayErrorModel.displayErrorC("Max 40 character");
            }
            else {
                Category newCategory = new Category(inputValue);
                categoryModel.createNewCategory(newCategory);
                comCategory.getItems().add(newCategory);
            }
        }
    }

    public void btnSave() { // Validate all inputs before saving
        boolean isNameValid = validateModel.validateInput(txtInputName, txtInputName.getText());
        boolean isArtistValid = validateModel.validateInput(txtInputArtist, txtInputArtist.getText());
        boolean isFilepathValid = validateModel.validateInput(txtInputFilepath, txtInputFilepath.getText());
        boolean isYearValid = validateModel.validateInput(txtInputYear, txtInputYear.getText());
        boolean isTimeValid = validateModel.validateInput(txtInputTime, txtInputTime.getText());

        if (isNameValid && isArtistValid && isFilepathValid && isYearValid && isTimeValid) {
            if (typeCU == 1) {
                createNewSong();
            }
            if (typeCU == 2) {
                updateSong();
            }
        }
    }

    private void createNewSong() { //Here the songs get created
        String title = txtInputName.getText();
        String artist = txtInputArtist.getText();
        String songPath = txtInputFilepath.getText();
        double songTime = currentSongLength;
        int year = Integer.parseInt(txtInputYear.getText());
        String category = String.valueOf(comCategory.getSelectionModel().getSelectedItem());

        Song song = new Song(-1, year, title, artist, songPath, songTime, category);

        try {
            Song newCreatedSong = songModel.createNewSong(song);
            mediaPlayerViewController.addSongToSoundMap(newCreatedSong);
            btnCloseWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSong() { //Here the songs get updated
        if (currentSelectedSong != null) {
            currentSelectedSong.setTitle(txtInputName.getText());
            currentSelectedSong.setArtist(txtInputArtist.getText());
            currentSelectedSong.setYear(Integer.parseInt(txtInputYear.getText()));
            currentSelectedSong.setSongPath(txtInputFilepath.getText());
            currentSelectedSong.setSongLength(currentSongLength);
            currentSelectedSong.setSongCategory(String.valueOf(comCategory.getSelectionModel().getSelectedItem()));
            try {
                songModel.updateSong(currentSelectedSong);
                mediaPlayerViewController.updateSongPathSoundMap(currentSelectedSong);
                btnCloseWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void btnCloseWindow() throws Exception { //Close the window
        Stage parent = (Stage) txtInputYear.getScene().getWindow();
        mediaPlayerViewController.refreshSongList();
        parent.close();
    }

//*******************************STYLING***********************************************
    private void setBorderStyle(TextField textField, boolean isValid) { //We get the styling from the CSS file
        if (isValid) {
            textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("Invalid"), false);  // Valid style
            textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("Valid"), true);  // Valid style
        } else {
            textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("Valid"), false);  // Valid style
            textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("Invalid"), true);  // Valid style
        }
    }
}