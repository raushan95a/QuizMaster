package com.quizapp.client.ui;

import com.quizapp.client.App;
import com.quizapp.client.Session;
import com.quizapp.model.Role;
import com.quizapp.model.User;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Holds the one window and switches the screen shown in it. */
public final class SceneManager {

    private static Stage stage;
    private static Scene scene;

    private SceneManager() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        scene = new Scene(new javafx.scene.layout.Pane(), 1000, 650);
        scene.getStylesheets().add(stylesheet());
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(550);
    }

    /** The CSS file; dialogs add it too, since they are separate windows. */
    public static String stylesheet() {
        return App.class.getResource("/client/styles.css").toExternalForm();
    }

    public static void show(Parent root, String title) {
        scene.setRoot(root);
        stage.setTitle("QuizMaster - " + title);
    }

    public static void showLogin() {
        show(new LoginView().build(), "Login");
    }

    /** Opens the dashboard that matches the logged-in user's role. */
    public static void showDashboard() {
        User user = Session.getUser();
        // getDashboardTitle() runs the Admin or Student version depending on the real object (dynamic method dispatch)
        String title = user.getDashboardTitle();
        if (user.getRole() == Role.ADMIN) {
            show(new AdminDashboard().build(), title);
        } else {
            show(new StudentDashboard().build(), title);
        }
    }
}
