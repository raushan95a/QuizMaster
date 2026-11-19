package com.quizapp.client;

import com.quizapp.client.ui.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/** The JavaFX application (client program). Run with: mvnw javafx:run */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.init(stage);
        SceneManager.showLogin();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
