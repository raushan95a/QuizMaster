package com.quizapp.client.ui;

import com.quizapp.client.ApiClient;
import com.quizapp.client.ClientConfig;
import com.quizapp.client.Session;
import com.quizapp.client.awt.HelpWindow;
import com.quizapp.client.util.AlertUtil;
import com.quizapp.client.util.Async;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;

/** The menu bar shown on every screen after login. Menu items have small image icons. */
public final class MainMenu {

    private MainMenu() {
    }

    public static MenuBar create() {
        MenuItem logout = new MenuItem("Logout", MenuIcons.icon("L", Color.web("#2563eb")));
        logout.setOnAction(event -> logout());
        MenuItem exit = new MenuItem("Exit", MenuIcons.icon("X", Color.web("#dc2626")));
        exit.setOnAction(event -> Platform.exit());
        Menu file = new Menu("File", null, logout, new SeparatorMenuItem(), exit);

        MenuItem shortcuts = new MenuItem("Keyboard Shortcuts", MenuIcons.icon("?", Color.web("#16a34a")));
        shortcuts.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        shortcuts.setOnAction(event -> HelpWindow.showWindow()); // opens the AWT window
        MenuItem about = new MenuItem("About", MenuIcons.icon("i", Color.web("#6b7280")));
        about.setOnAction(event -> AlertUtil.info(
                "QuizMaster Lite\nOnline Quiz / Exam System\nAdvanced Java Programming project\n\n"
                        + "Server: " + ClientConfig.serverUrl()));
        Menu help = new Menu("Help", null, shortcuts, about);

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
