import classes.mainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("scenes/loginUI.fxml"));
        primaryStage.setTitle("TimeLine");
        primaryStage.getIcons().add(new Image("assets\\logo.png"));  // Set window icon
        primaryStage.setScene(new Scene(root, 700, 400));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        mainController.saveSched();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
