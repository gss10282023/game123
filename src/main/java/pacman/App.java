package pacman;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import pacman.model.engine.GameEngine;
import pacman.model.engine.GameEngineImpl;
import pacman.view.GameWindow;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String configPath = getParameters().getNamed().getOrDefault("config", "/config.json");
        GameEngine model;
        try {
            model = new GameEngineImpl(configPath);
        } catch (ConfigurationParseException e) {
            System.err.println(e.getMessage());
            Platform.exit();
            return;
        }
        GameWindow window = new GameWindow(model, 448, 576);

        primaryStage.setTitle("PacmanFX");
        primaryStage.setScene(window.getScene());
        primaryStage.show();

        window.run();
    }
}
