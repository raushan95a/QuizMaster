package com.quizapp.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.quizapp.client.ApiClient;
import com.quizapp.client.util.Async;
import com.quizapp.dto.ReviewItem;
import com.quizapp.model.Question;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Screen 9: every question with the student's answer and the correct answer. */
public class ReviewView {

    private final int attemptId;
    private final String quizTitle;
    private final Runnable onBack;
    private final VBox list = new VBox(12);

    public ReviewView(int attemptId, String quizTitle, Runnable onBack) {
        this.attemptId = attemptId;
        this.quizTitle = quizTitle;
        this.onBack = onBack;
    }

    public Parent build() {
        Button backButton = new Button("< Back");
        backButton.setOnAction(e -> onBack.run());
        Label title = new Label("Review: " + quizTitle);
        title.getStyleClass().add("screen-title");
        HBox header = new HBox(16, backButton, title);
        header.setAlignment(Pos.CENTER_LEFT);

        list.getChildren().add(new Label("Loading..."));
        list.setPadding(new Insets(4));
        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("plain-scroll");

        VBox content = new VBox(16, header, scroll);
        content.setPadding(new Insets(20));
        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);

        BorderPane root = new BorderPane(content);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");

        Async.run(
                () -> ApiClient.get("/api/attempts/" + attemptId + "/review", new TypeReference<List<ReviewItem>>() { }),
                this::showItems,
                error -> list.getChildren().setAll(new Label(error.getMessage())));
        return root;
    }

    private void showItems(List<ReviewItem> items) {
        list.getChildren().clear();
        int number = 1;
        for (ReviewItem item : items) {
            list.getChildren().add(card(number++, item));
        }
    }

    private VBox card(int number, ReviewItem item) {
        Label question = new Label(number + ".  " + item.text());
        question.getStyleClass().add("section-title");
        question.setWrapText(true);

        String verdict = item.chosen() == null ? "Not answered"
                : item.isRight() ? "✔ Correct" : "✘ Wrong";
        Label verdictLabel = new Label(verdict);
        verdictLabel.getStyleClass().add(item.chosen() == null ? "stat-neutral"
                : item.isRight() ? "stat-good" : "stat-bad");

        VBox card = new VBox(8, question, verdictLabel);
        for (char letter : Question.OPTIONS) {
            String l = String.valueOf(letter);
            Label option = new Label(letter + ")  " + item.option(letter));
            option.setWrapText(true);
            option.getStyleClass().add("review-option");
            if (l.equals(item.correct())) {
                option.setText(option.getText() + "   ✔ correct answer");
                option.getStyleClass().add("review-correct");
            } else if (l.equals(item.chosen())) {
                option.setText(option.getText() + "   ✘ your answer");
                option.getStyleClass().add("review-wrong");
            }
            card.getChildren().add(option);
        }
        card.getStyleClass().add("card");
        return card;
    }
}
