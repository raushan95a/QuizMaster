package com.quizapp.client.ui;

import com.quizapp.client.ApiClient;
import com.quizapp.client.util.AlertUtil;
import com.quizapp.client.util.Async;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Screen 4: create or edit a quiz and its questions. */
public class QuizEditorView {

    private Quiz quiz; // null until the quiz has been saved once

    private final Label headerLabel = new Label();
    private final TextField titleField = new TextField();
    private final TextField subjectField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final Spinner<Integer> timeSpinner = new Spinner<>(1, 180, 10);
    private final Label formError = new Label();
    private final Button saveButton = new Button("Save quiz");

    private final ListView<Question> questionList = new ListView<>();
    private final Button addButton = new Button("+ Add");
    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete");
    private final VBox questionPanel = new VBox(10);

    /** @param quiz the quiz to edit, or null to create a new one */
    public QuizEditorView(Quiz quiz) {
        this.quiz = quiz;
    }

    public Parent build() {
        Button backButton = new Button("< Back to quizzes");
        backButton.setOnAction(e -> SceneManager.showDashboard());
        headerLabel.getStyleClass().add("screen-title");
        HBox header = new HBox(16, backButton, headerLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox body = new HBox(20, buildForm(), buildQuestionPanel());
        HBox.setHgrow(questionPanel, Priority.ALWAYS);

        VBox content = new VBox(16, header, body);
        content.setPadding(new Insets(20));
        VBox.setVgrow(body, Priority.ALWAYS);

        BorderPane root = new BorderPane(content);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");

        if (quiz == null) {
            showQuiz(null);
        } else {
            reloadQuiz(quiz.getId());
        }
        return root;
    }

    // ---------- left side: quiz details ----------

    private VBox buildForm() {
        titleField.setPromptText("e.g. Unit 2 - Exceptions");
        subjectField.setPromptText("e.g. Advanced Java");
        descriptionArea.setPromptText("Short description (optional)");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        timeSpinner.setEditable(true);
        timeSpinner.setPrefWidth(120);
        FormUtil.tabMovesFocus(descriptionArea, timeSpinner.getEditor());
        formError.getStyleClass().add("error-label");
        formError.setWrapText(true);

        saveButton.getStyleClass().add("primary-button");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(e -> saveQuiz());

        VBox form = new VBox(8,
                new Label("Title"), titleField,
                new Label("Subject"), subjectField,
                new Label("Description"), descriptionArea,
                new Label("Time limit (minutes)"), timeSpinner,
                saveButton, formError);
        form.getStyleClass().add("card");
        form.setPrefWidth(340);
        form.setMinWidth(300);
        return form;
    }

    private void saveQuiz() {
        Integer minutes = readMinutes();
        if (minutes == null) {
            formError.setText("Time limit must be a number from 1 to 180");
            return;
        }
        Quiz input = new Quiz(titleField.getText(), subjectField.getText(), descriptionArea.getText(), minutes);
        boolean isNew = quiz == null;
        String path = isNew ? "/api/admin/quizzes" : "/api/admin/quizzes/" + quiz.getId();

        saveButton.setDisable(true);
        formError.setText("");
        Async.run(
                () -> isNew ? ApiClient.post(path, input, Quiz.class) : ApiClient.put(path, input, Quiz.class),
                saved -> {
                    saveButton.setDisable(false);
                    showQuiz(saved);
                    if (isNew) {
                        AlertUtil.info("Quiz saved. Now add its questions on the right.");
                    }
                },
                error -> {
                    saveButton.setDisable(false);
                    formError.setText(error.getMessage());
                });
    }

    /** Reads the spinner text (the user may have typed a value without pressing Enter). */
    private Integer readMinutes() {
        try {
            int value = Integer.parseInt(timeSpinner.getEditor().getText().trim());
            return (value >= 1 && value <= 180) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---------- right side: questions ----------

    private VBox buildQuestionPanel() {
        Label title = new Label("Questions");
        title.getStyleClass().add("section-title");

        questionList.setPlaceholder(new Label("No questions yet. Click \"+ Add\"."));
        questionList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Question q, boolean empty) {
                super.updateItem(q, empty);
                if (empty || q == null) {
                    setText(null);
                } else {
                    char answer = q.getCorrectOption().charAt(0);
                    setText((getIndex() + 1) + ". " + q.getText()
                            + "\n     Answer: " + answer + ") " + q.optionText(answer));
                }
            }
        });
        questionList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> updateButtons());
        questionList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                editQuestion();
            }
        });
        VBox.setVgrow(questionList, Priority.ALWAYS);

        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> addQuestion());
        editButton.setOnAction(e -> editQuestion());
        deleteButton.setOnAction(e -> deleteQuestion());
        HBox buttons = new HBox(8, addButton, editButton, deleteButton);

        questionPanel.getChildren().setAll(title, questionList, buttons);
        questionPanel.getStyleClass().add("card");
        return questionPanel;
    }

    private void addQuestion() {
        QuestionDialog.show(null).ifPresent(q -> Async.run(
                () -> ApiClient.post("/api/admin/quizzes/" + quiz.getId() + "/questions", q, Question.class),
                saved -> reloadQuiz(quiz.getId()),
                error -> AlertUtil.error(error.getMessage())));
    }

    private void editQuestion() {
        Question selected = questionList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        QuestionDialog.show(selected).ifPresent(q -> Async.run(
                () -> ApiClient.put("/api/admin/questions/" + selected.getId(), q, Question.class),
                saved -> reloadQuiz(quiz.getId()),
                error -> AlertUtil.error(error.getMessage())));
    }

    private void deleteQuestion() {
        Question selected = questionList.getSelectionModel().getSelectedItem();
        if (selected == null || !AlertUtil.confirm("Delete this question?\n\n" + selected.getText())) {
            return;
        }
        Async.run(
                () -> {
                    ApiClient.delete("/api/admin/questions/" + selected.getId());
                    return null;
                },
                ignored -> reloadQuiz(quiz.getId()),
                error -> AlertUtil.error(error.getMessage()));
    }

    // ---------- helpers ----------

    private void reloadQuiz(int quizId) {
        Async.run(
                () -> ApiClient.get("/api/admin/quizzes/" + quizId, Quiz.class),
                this::showQuiz,
                error -> AlertUtil.error("Could not load the quiz.", error));
    }

    /** Fills the screen from a quiz (or empties it for a new quiz). */
    private void showQuiz(Quiz loaded) {
        this.quiz = loaded;
        if (loaded == null) {
            headerLabel.setText("New quiz");
            questionList.getItems().clear();
        } else {
            headerLabel.setText(loaded.getTitle() + (loaded.isPublished() ? "  (Published)" : "  (Draft)"));
            titleField.setText(loaded.getTitle());
            subjectField.setText(loaded.getSubject());
            descriptionArea.setText(loaded.getDescription());
            timeSpinner.getValueFactory().setValue(loaded.getTimeLimitMin());
            questionList.getItems().setAll(loaded.getQuestions());
        }
        questionPanel.setDisable(loaded == null); // questions can be added only after the quiz is saved
        updateButtons();
    }

    private void updateButtons() {
        boolean none = questionList.getSelectionModel().getSelectedItem() == null;
        editButton.setDisable(none);
        deleteButton.setDisable(none);
    }
}
