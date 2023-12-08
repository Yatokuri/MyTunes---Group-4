/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("/View/MediaPlayer.fxml"));
        primaryStage.getIcons().add(new Image("/Icons/mainIcon.png"));
        primaryStage.setTitle("MyTunes Beta");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
