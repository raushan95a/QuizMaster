package com.quizapp.client.ui;

import com.quizapp.client.ApiClient;
import com.quizapp.client.ClientConfig;
import com.quizapp.client.Session;
import com.quizapp.client.util.Async;
import com.quizapp.dto.AuthResponse;
import com.quizapp.dto.LoginRequest;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Screen 1: login. */
public class LoginView {

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label errorLabel = new Label();
    private final Button loginButton = new Button("Login");

    public Parent build() {
        Label title = new Label("QuizMaster");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Sign in to continue");
        subtitle.getStyleClass().add("subtitle");

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(event -> login()); // delegation event model: button -> listener

        // Enter in the username field moves to the password field (KeyEvent)
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        Hyperlink registerLink = new Hyperlink("New student? Create an account");
        registerLink.setOnAction(event -> SceneManager.show(new RegisterView().build(), "Register"));

        VBox card = new VBox(12, title, subtitle, usernameField, passwordField, loginButton, errorLabel, registerLink);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(360);

        Label server = new Label("Server: " + ClientConfig.serverUrl());
        server.getStyleClass().add("muted");

        BorderPane root = new BorderPane(card);
        root.setBottom(server);
        BorderPane.setAlignment(server, Pos.CENTER);
        root.getStyleClass().add("page");
        return root;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Enter your username and password");
            return;
        }

        setBusy(true);
        Async.run(
                () -> ApiClient.post("/api/auth/login", new LoginRequest(username, password), AuthResponse.class),
                response -> {
                    Session.start(response);
                    SceneManager.showDashboard();
                },
                error -> {
                    setBusy(false);
                    errorLabel.setText(error.getMessage());
                    passwordField.clear();
                });
    }

    private void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        loginButton.setText(busy ? "Signing in..." : "Login");
        if (busy) {
            errorLabel.setText("");
        }
    }
}
