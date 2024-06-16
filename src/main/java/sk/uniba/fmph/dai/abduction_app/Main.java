package sk.uniba.fmph.dai.abduction_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private final int WIDTH = 1280;
    private final int HEIGHT = 720;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view.fxml"));
        ApplicationController controller = new ApplicationController();
        loader.setController(controller);

        //Application.setUserAgentStylesheet("src/main/resources/Atlanta FX 2.0.1/dracula.css");

        Scene scene = new Scene(loader.load(), WIDTH, HEIGHT);
        //scene.getStylesheets().add("/bootstrap3.css");
        //scene.getStylesheets().add(Main.class.getResource("bootstrap3.css").toExternalForm());
        stage.setMinHeight(257);
        stage.setMaxHeight(HEIGHT+50);
        stage.setMaxWidth(WIDTH);
        stage.setTitle("AbductionApp");
        stage.getIcons().add(new Image("file:src/main/resources/abduction_icon.png"));
        stage.setScene(scene);
        stage.show();

        controller.stage = stage;

        controller.init();
    }
}
