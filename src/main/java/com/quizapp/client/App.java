package com.quizapp.client;

import com.quizapp.client.ui.SceneManager;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

/** The JavaFX application (client program). Run with: mvnw javafx:run */
public class App extends Application {

    private static HostServices hostServices;

    @Override
    public void start(Stage stage) {
        hostServices = getHostServices();
        SceneManager.init(stage);
        SceneManager.showLogin();
        stage.show();
    }

    /** Opens a web address in the default browser. */
    public static void openInBrowser(String url) {
        if (hostServices != null) {
            hostServices.showDocument(url);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
