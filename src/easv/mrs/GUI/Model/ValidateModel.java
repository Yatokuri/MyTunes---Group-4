package easv.mrs.GUI.Model;

import easv.mrs.BE.Song;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ValidateModel {
    private SongModel songModel;
    private String setupUpdateOriginalName = "";
    private boolean setupUpdateOriginal = true;

    private static final String[]validFiles  = {"wav" , "mp3"};


    public ValidateModel()  {
        try {
            songModel = new SongModel();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Method to check valid to CU is correct
    public boolean validateInput(TextField textField, String value) { //Valid input

        switch (textField.getId()) {
            case "txtInputName":
                return !value.isEmpty() && value.length() <= 150;
            case "txtInputArtist":
                return !value.isEmpty() && value.length() <= 100;
            case "txtInputFilepath":
                if (setupUpdateOriginal) {
                    setupUpdateOriginalName = value;
                    setupUpdateOriginal = false;
                }
                if (setupUpdateOriginalName.equals(value))  { //When updating a song the filepath can be the same
                    return isValidMediaPath(value);
                }
                for  (Song s : songModel.getObservableSongs()) { //We don't want people to have the same song path twice
                    if (s.getSongPath().equals(value)) {
                        return false;
                    }
                }
                return isValidMediaPath(value);
            case "txtInputYear":
                try {
                    int year = Integer.parseInt(value.replaceFirst("0",""));
                    int currentYear = Year.now().getValue();
                    return year >= 1 && year <= 2025;
                } catch (NumberFormatException e) {
                    return false; // Not a valid integer
                }
            default:
                return true;
        }
    }

    //Method to choose valid files
    public String btnChoose() {   //   FileChooser fileChooser = new FileChooser();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Audio Files", validFiles2);
        fileChooser.getExtensionFilters().add(extFilter);

        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();  // Get the selected file path and save it
        }
        return "";
    }

    //Method to check if a file is valid
    public boolean isValidMediaPath(String path) {
        List<String> supportedExtensions = Arrays.asList(validFiles); //Have a place where all valid format is stored
        try {
            Path filePath = FileSystems.getDefault().getPath(path);
            String fileName = filePath.getFileName().toString();
            int lastIndexOf = fileName.lastIndexOf(".");
            String extension = (lastIndexOf != -1 && lastIndexOf != 0) ? fileName.substring(lastIndexOf + 1).toLowerCase() : "";
            return filePath.toFile().isFile() && supportedExtensions.contains(extension);
        } catch (Exception e) {
            return false;
        }
    }

    //Method to get a song time in HH:MM:SS format
    public void updateTimeText(MediaPlayer newSong, Consumer<String> onReadyCallback) {
        newSong.setOnReady(() -> {
            long totalSeconds = (long) newSong.getTotalDuration().toSeconds();
            String formattedTime = String.format("%02d:%02d:%02d " +  "-" + totalSeconds , totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
            // Execute the callback with the formatted time
            if (onReadyCallback != null) {
                onReadyCallback.accept(formattedTime); //We return the time in format HH:MM:SS and just in seconds
            }
        });
    }

    //Convert validFiles where mp3 be to *.mp3
    private static final String[] validFiles2 = generateValidFiles2();
    private static String[] generateValidFiles2() {
        String[] validFiles2 = new String[ValidateModel.validFiles.length];
        for (int i = 0; i < ValidateModel.validFiles.length; i++) {
            validFiles2[i] = "*." + ValidateModel.validFiles[i];
        }
        return validFiles2;
    }

}