package com.quizapp.client.ui;

import com.quizapp.client.ApiClient;
import com.quizapp.client.Session;
import com.quizapp.client.util.Async;
import com.quizapp.dto.AuthResponse;
import com.quizapp.dto.RegisterRequest;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Screen 2: student registration. */
public class RegisterView {

    private final TextField fullNameField = new TextField();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmField = new PasswordField();
    private final Label errorLabel = new Label();
    private final Button registerButton = new Button("Create account");

    public Parent build() {
        Label title = new Label("Create student account");
        title.getStyleClass().add("screen-title");

        fullNameField.setPromptText("Full name");
        usernameField.setPromptText("Username (letters, digits, _ or .)");
        passwordField.setPromptText("Password (at least 6 characters)");
        confirmField.setPromptText("Confirm password");
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        registerButton.getStyleClass().add("primary-button");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setDefaultButton(true);
        registerButton.setOnAction(event -> register());

        Hyperlink backLink = new Hyperlink("Already have an account? Log in");
        backLink.setOnAction(event -> SceneManager.showLogin());

        VBox card = new VBox(12, title, fullNameField, usernameField, passwordField, confirmField,
                registerButton, errorLabel, backLink);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);

        BorderPane root = new BorderPane(card);
        root.getStyleClass().add("page");
        return root;
    }

    private void register() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }
        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmField.getText())) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        registerButton.setDisable(true);
        errorLabel.setText("");
        Async.run(
                () -> ApiClient.post("/api/auth/register",
                        new RegisterRequest(fullName, username, password), AuthResponse.class),
                response -> {
                    Session.start(response);
                    SceneManager.showDashboard();
                },
                error -> {
                    registerButton.setDisable(false);
                    errorLabel.setText(error.getMessage());
                });
    }
}
