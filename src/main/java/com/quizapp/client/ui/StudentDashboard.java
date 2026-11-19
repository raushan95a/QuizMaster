package com.quizapp.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.quizapp.client.ApiClient;
import com.quizapp.client.Session;
import com.quizapp.client.util.AlertUtil;
import com.quizapp.client.util.Async;
import com.quizapp.dto.StartAttemptResponse;
import com.quizapp.model.Quiz;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Screen 6: student home — published quizzes as cards. */
public class StudentDashboard {

    private final ListView<Quiz> quizList = new ListView<>();
    private final Button startButton = new Button("Start quiz");
    private final Label statusLabel = new Label();

    public Parent build() {
        Label title = new Label(Session.getUser().getDashboardTitle());
        title.getStyleClass().add("screen-title");
        Label subtitle = new Label("Pick a quiz and press Start. The timer begins as soon as you start.");
        subtitle.getStyleClass().add("muted");

        quizList.setPlaceholder(new Label("No quizzes are available right now."));
        quizList.getStyleClass().add("card-list");
        quizList.setCellFactory(list -> new QuizCard());
        quizList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> startButton.setDisable(n == null));
        quizList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                startSelected();
            }
        });
        VBox.setVgrow(quizList, Priority.ALWAYS);

        startButton.getStyleClass().add("primary-button");
        startButton.setDisable(true);
        startButton.setOnAction(e -> startSelected());
        Button historyButton = new Button("My results");
        historyButton.setOnAction(e -> SceneManager.show(new HistoryView().build(), "My Results"));
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadQuizzes());
        HBox buttons = new HBox(10, startButton, historyButton, refreshButton);

        statusLabel.getStyleClass().add("muted");
        VBox content = new VBox(12, title, subtitle, quizList, buttons, statusLabel);
        content.setPadding(new Insets(20));

        BorderPane root = new BorderPane(content);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");
        loadQuizzes();
        return root;
    }

    private void loadQuizzes() {
        statusLabel.setText("Loading...");
        Async.run(
                () -> ApiClient.get("/api/quizzes", new TypeReference<List<Quiz>>() { }),
                quizzes -> {
                    quizList.getItems().setAll(quizzes);
                    statusLabel.setText(quizzes.size() + " quiz(zes) available");
                },
                error -> statusLabel.setText(error.getMessage()));
    }

    private void startSelected() {
        Quiz quiz = quizList.getSelectionModel().getSelectedItem();
        if (quiz == null || !AlertUtil.confirm("Start \"" + quiz.getTitle() + "\"?\n\n"
                + quiz.getQuestionCount() + " questions, " + quiz.getTimeLimitMin() + " minutes.\n"
                + "The quiz is submitted automatically when the time is over.")) {
            return;
        }
        startButton.setDisable(true);
        Async.run(
                () -> ApiClient.post("/api/quizzes/" + quiz.getId() + "/start", null, StartAttemptResponse.class),
                start -> SceneManager.show(new QuizView(start).build(), start.quizTitle()),
                error -> {
                    startButton.setDisable(false);
                    AlertUtil.error(error.getMessage());
                });
    }

    /** How one quiz looks in the list: a small card with a shadow. */
    private static class QuizCard extends ListCell<Quiz> {
        @Override
        protected void updateItem(Quiz quiz, boolean empty) {
            super.updateItem(quiz, empty);
            if (empty || quiz == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            Label name = new Label(quiz.getTitle());
            name.getStyleClass().add("card-title");
            Label meta = new Label(quiz.getSubject() + "   •   " + quiz.getQuestionCount() + " questions   •   "
                    + quiz.getTimeLimitMin() + " min");
            meta.getStyleClass().add("muted");
            VBox box = new VBox(4, name, meta);
            if (quiz.getDescription() != null && !quiz.getDescription().isBlank()) {
                Label desc = new Label(quiz.getDescription());
                desc.setWrapText(true);
                box.getChildren().add(desc);
            }
            box.getStyleClass().add("quiz-card");
            box.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.12))); // JavaFX effect
            setGraphic(box);
            setText(null);
        }
    }
}
