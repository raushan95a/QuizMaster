package com.quizapp.client.ui;

import com.quizapp.client.util.TimeFormat;
import com.quizapp.dto.ResultDto;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Screen 8: the score after submitting, shown with an animation. */
public class ResultView {

    private final ResultDto result;

    public ResultView(ResultDto result) {
        this.result = result;
    }

    public Parent build() {
        Label heading = new Label(result.quizTitle());
        heading.getStyleClass().add("screen-title");

        Label score = new Label(result.score() + " / " + result.total());
        score.getStyleClass().add("score-big");
        Label percent = new Label(result.percentage() + " %");
        percent.getStyleClass().add("score-percent");

        Label message = new Label(result.percentage() >= 80 ? "Excellent work!"
                : result.percentage() >= 50 ? "Good job - keep practising!" : "Keep practising - you can do it!");
        message.getStyleClass().add("section-title");

        HBox counts = new HBox(24,
                stat("✔ Correct", result.correct(), "stat-good"),
                stat("✘ Wrong", result.wrong(), "stat-bad"),
                stat("– Unanswered", result.unanswered(), "stat-neutral"));
        counts.setAlignment(Pos.CENTER);

        Label time = new Label("Time taken: " + TimeFormat.minutesSeconds(result.timeTakenSec()));
        time.getStyleClass().add("muted");

        Button reviewButton = new Button("Review answers");
        reviewButton.getStyleClass().add("primary-button");
        reviewButton.setOnAction(e -> SceneManager.show(
                new ReviewView(result.attemptId(), result.quizTitle(), SceneManager::showDashboard).build(),
                "Review"));
        Button backButton = new Button("Back to quizzes");
        backButton.setOnAction(e -> SceneManager.showDashboard());
        HBox buttons = new HBox(12, reviewButton, backButton);
        buttons.setAlignment(Pos.CENTER);

        VBox card = new VBox(14, heading, score, percent, message, counts, time);
        if (result.autoSubmitted()) {
            Label auto = new Label("Time ran out - your saved answers were submitted automatically.");
            auto.getStyleClass().add("error-label");
            card.getChildren().add(auto);
        }
        card.getChildren().add(buttons);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(560);
        card.setMaxHeight(Region.USE_PREF_SIZE); // don't stretch to the full window height

        BorderPane root = new BorderPane(card);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");

        // Fade + scale the score in (JavaFX transitions run together with ParallelTransition)
        FadeTransition fade = new FadeTransition(Duration.millis(700), score);
        fade.setFromValue(0);
        fade.setToValue(1);
        ScaleTransition grow = new ScaleTransition(Duration.millis(700), score);
        grow.setFromX(0.4);
        grow.setFromY(0.4);
        grow.setToX(1);
        grow.setToY(1);
        new ParallelTransition(fade, grow).play();
        return root;
    }

    private VBox stat(String name, int value, String styleClass) {
        Label number = new Label(String.valueOf(value));
        number.getStyleClass().addAll("stat-number", styleClass);
        Label label = new Label(name);
        label.getStyleClass().add("muted");
        VBox box = new VBox(2, number, label);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
