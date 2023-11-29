package easv.mrs.GUI.Model;


import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ValidateModel {

    //Method to check valid to CU is correct
    public boolean validateInput(TextField textField, String value) { //Valid input
        switch (textField.getId()) {
            case "txtInputName":
                return !value.isEmpty() && value.length() <= 150;
            case "txtInputArtist":
                return !value.isEmpty() && value.length() <= 100;
            case "txtInputFilepath":
                return isValidMediaPath(value);
            case "txtInputYear":
                try {
                    int year = Integer.parseInt(value);
                    return year >= 1 && year <= 2500;
                } catch (NumberFormatException e) {
                    return false; // Not a valid integer
                }
            default:
                return true;
        }
    }

    //Method to check if a file is valid
    public boolean isValidMediaPath(String path) {
        List<String> supportedExtensions = Arrays.asList("mp3", "wav"); //Have a place where all valid format is stored
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

}