package com.quizapp.client.ui;

import com.quizapp.client.ApiClient;
import com.quizapp.client.Session;
import com.quizapp.client.util.AlertUtil;
import com.quizapp.client.util.Async;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/** The menu bar shown on every screen after login. */
public final class MainMenu {

    private MainMenu() {
    }

    public static MenuBar create() {
        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(event -> logout());
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> Platform.exit());
        Menu file = new Menu("File", null, logout, new SeparatorMenuItem(), exit);

        MenuItem about = new MenuItem("About");
        about.setOnAction(event -> AlertUtil.info(
                "QuizMaster Lite\nOnline Quiz / Exam System\nAdvanced Java Programming project"));
        Menu help = new Menu("Help", null, about);

        return new MenuBar(file, help);
    }

    /** Tells the server to forget the token, then returns to the login screen. */
    public static void logout() {
        Async.run(
                () -> {
                    ApiClient.post("/api/auth/logout", null);
                    return null;
                },
                ignored -> finishLogout(),
                error -> finishLogout()); // log out locally even if the server cannot be reached
    }

    private static void finishLogout() {
        Session.clear();
        SceneManager.showLogin();
    }
}
