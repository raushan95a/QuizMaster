package com.quizapp.client.ui;

import com.quizapp.client.Session;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Screen 6: student home. The quiz list is added in step 3. */
public class StudentDashboard {

    public Parent build() {
        Label title = new Label(Session.getUser().getDashboardTitle());
        title.getStyleClass().add("screen-title");
        Label info = new Label("Available quizzes will appear here soon.");
        info.getStyleClass().add("muted");

        VBox center = new VBox(10, title, info);
        center.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane(center);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");
        return root;
    }
}
