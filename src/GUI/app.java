package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class app extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
        primaryStage.setTitle("Modem - SIM - Communication");
        primaryStage.setScene(new Scene(root, 800, 750));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
