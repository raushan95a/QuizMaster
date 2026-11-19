package com.quizapp.client.ui;

import com.quizapp.client.Session;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Screen 3: admin home. Quiz management is added in step 2. */
public class AdminDashboard {

    public Parent build() {
        Label title = new Label(Session.getUser().getDashboardTitle());
        title.getStyleClass().add("screen-title");
        Label info = new Label("Quiz management is coming in the next step.");
        info.getStyleClass().add("muted");

        VBox center = new VBox(10, title, info);
        center.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane(center);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");
        return root;
    }
}
