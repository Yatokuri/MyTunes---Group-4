package easv.mrs.GUI.Model;


import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Year;
import java.util.ArrayList;
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
            case "txtInputFilepath": //Sikre at ingen sange har samme file path validate skal lyse rød så brug SQL i DB

                return isValidMediaPath(value);
            case "txtInputYear":
                try {
                    int year = Integer.parseInt(value.replaceFirst("0",""));
                    int currentYear = Year.now().getValue();

                    System.out.println(currentYear);

                    return year >= 1 && year <= 2025;
                } catch (NumberFormatException e) {
                    return false; // Not a valid integer
                }
            default:
                return true;
        }
    }

    public String btnChoose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"));//Have a place where all valid format is stored
        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

       // fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", validFiles2));

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();  // Get the selected file path and save it
        }

        return "";
    }


    private static final String[]validFiles  = {"mp3" , "wav" , "mp4"};

    private static final String[]validFiles2  = {"*.mp3" , "*.wav" , "*.mp4"};

    private static String[] validFileTypes(int test) {

        if (test == 1)   {
            ArrayList<String> animals = new ArrayList<>();
            String[] myIntArray = new String[validFiles.length];


            for (String a : validFiles) {
          //      StringBuilder sb = new StringBuilder();
               //sb.insert(1,"*." + a);

                a = "\"*." + a + "\"";

                animals.add(a);

            }

                System.out.println(validFiles);
                System.out.println(animals);

                myIntArray = animals.toArray(new String[0]);

                return myIntArray;
        }

     //   return arrayList;

        if (test == 2)
            return validFiles;

        return validFiles;
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

}