package com.quizapp.client.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/** Consistent pop-up dialogs. */
public final class AlertUtil {

    private AlertUtil() {
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Error", message);
    }

    /** Overloaded: shows the exception's message. */
    public static void error(String message, Throwable cause) {
        String detail = cause == null || cause.getMessage() == null ? "" : "\n\n" + cause.getMessage();
        show(Alert.AlertType.ERROR, "Error", message + detail);
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "QuizMaster", message);
    }

    /** Yes/No question; returns true if the user clicked OK. */
    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.setTitle("Please confirm");
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
