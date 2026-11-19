package com.quizapp.client.ui;

import com.quizapp.client.ApiClient;
import com.quizapp.client.awt.HelpWindow;
import com.quizapp.client.thread.AnswerQueue;
import com.quizapp.client.thread.AutoSaveThread;
import com.quizapp.client.thread.QuizTimer;
import com.quizapp.client.util.AlertUtil;
import com.quizapp.client.util.Async;
import com.quizapp.client.util.TimeFormat;
import com.quizapp.dto.AnswersRequest;
import com.quizapp.dto.QuestionView;
import com.quizapp.dto.ResultDto;
import com.quizapp.dto.StartAttemptResponse;
import com.quizapp.model.Question;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Screen 7: taking the quiz — one question at a time, with a timer and background auto-save. */
public class QuizView implements QuizTimer.Listener, AutoSaveThread.StatusListener {

    private final StartAttemptResponse data;
    private final List<QuestionView> questions;
    private final Map<Integer, String> answers = new HashMap<>(); // question id -> "A".."D"
    private int index = 0;

    // threads
    private final AnswerQueue answerQueue = new AnswerQueue();
    private QuizTimer timer;
    private AutoSaveThread autoSave;
    private final AtomicBoolean submitted = new AtomicBoolean(false);

    // controls
    private final Label timerLabel = new Label();
    private final Label saveLabel = new Label();
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label numberLabel = new Label();
    private final Label questionLabel = new Label();
    private final ToggleGroup optionGroup = new ToggleGroup();
    private final RadioButton[] optionButtons = new RadioButton[4];
    private final Button prevButton = new Button("< Previous");
    private final Button nextButton = new Button("Next >");
    private final Button submitButton = new Button("Submit");
    private final FlowPane navPane = new FlowPane(6, 6);
    private boolean showingQuestion; // true while the screen itself changes the selection

    public QuizView(StartAttemptResponse data) {
        this.data = data;
        this.questions = data.questions();
    }

    public Parent build() {
        // Answers already saved (resumed attempt) + anything saved on this PC that did not reach the server
        answers.putAll(data.savedAnswers());
        Map<Integer, String> local = AutoSaveThread.readBackup(data.attemptId());
        for (Map.Entry<Integer, String> e : local.entrySet()) {
            if (!e.getValue().equals(answers.get(e.getKey()))) {
                answers.put(e.getKey(), e.getValue());
                answerQueue.put(e.getKey(), e.getValue()); // re-send to the server
            }
        }

        Parent root = buildLayout();
        showQuestion(0);

        timer = new QuizTimer(data.secondsLeft(), this);
        autoSave = new AutoSaveThread(data.attemptId(), answerQueue, answers, this);
        timer.start();
        autoSave.start();
        if (data.secondsLeft() <= 0) {
            submit(true);
        }
        return root;
    }

    private Parent buildLayout() {
        Label title = new Label(data.quizTitle());
        title.getStyleClass().add("screen-title");
        timerLabel.getStyleClass().add("timer");
        saveLabel.getStyleClass().add("muted");
        submitButton.getStyleClass().add("primary-button");
        submitButton.setOnAction(e -> submit(false));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(16, title, spacer, saveLabel, timerLabel, submitButton);
        top.setAlignment(Pos.CENTER_LEFT);

        progressBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        HBox progressRow = new HBox(12, progressBar, numberLabel);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        questionLabel.getStyleClass().add("question-text");
        questionLabel.setWrapText(true);
        VBox optionsBox = new VBox(12);
        for (int i = 0; i < 4; i++) {
            RadioButton rb = new RadioButton();
            rb.setToggleGroup(optionGroup);
            rb.setUserData(String.valueOf(Question.OPTIONS[i]));
            rb.getStyleClass().add("option");
            rb.setWrapText(true);
            rb.setMaxWidth(Double.MAX_VALUE);
            optionButtons[i] = rb;
            optionsBox.getChildren().add(rb);
        }
        // Listener: the student picked an option
        optionGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (!showingQuestion && newToggle != null) {
                answerChosen((String) newToggle.getUserData());
            }
        });
        VBox questionCard = new VBox(18, questionLabel, optionsBox);
        questionCard.getStyleClass().add("card");
        VBox.setVgrow(questionCard, Priority.ALWAYS);

        prevButton.setOnAction(e -> showQuestion(index - 1));
        nextButton.setOnAction(e -> showQuestion(index + 1));
        navPane.setAlignment(Pos.CENTER);
        HBox.setHgrow(navPane, Priority.ALWAYS);
        HBox bottom = new HBox(12, prevButton, navPane, nextButton);
        bottom.setAlignment(Pos.CENTER);

        Label hint = new Label("Keys: 1-4 choose an answer,  ← / → move between questions,  F1 help");
        hint.getStyleClass().add("muted");

        VBox content = new VBox(14, top, progressRow, questionCard, bottom, hint);
        content.setPadding(new Insets(20));

        BorderPane root = new BorderPane(content);
        root.getStyleClass().add("page");
        // KeyEvent: keyboard shortcuts for the whole screen
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        return root;
    }

    private void handleKey(KeyEvent event) {
        KeyCode code = event.getCode();
        int option = -1;
        if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
            option = 0;
        } else if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
            option = 1;
        } else if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
            option = 2;
        } else if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4) {
            option = 3;
        }
        if (option >= 0 && !submitted.get()) {
            optionButtons[option].setSelected(true);
            event.consume();
        } else if (code == KeyCode.RIGHT) {
            showQuestion(index + 1);
            event.consume();
        } else if (code == KeyCode.LEFT) {
            showQuestion(index - 1);
            event.consume();
        } else if (code == KeyCode.F1) {
            HelpWindow.showWindow(); // AWT help window
            event.consume();
        }
    }

    private void showQuestion(int newIndex) {
        if (newIndex < 0 || newIndex >= questions.size()) {
            return;
        }
        index = newIndex;
        QuestionView q = questions.get(index);

        showingQuestion = true;
        questionLabel.setText((index + 1) + ".  " + q.text());
        String chosen = answers.get(q.id());
        optionGroup.selectToggle(null);
        for (int i = 0; i < 4; i++) {
            char letter = Question.OPTIONS[i];
            optionButtons[i].setText(letter + ")  " + q.option(letter));
            if (String.valueOf(letter).equals(chosen)) {
                optionButtons[i].setSelected(true);
            }
        }
        showingQuestion = false;

        numberLabel.setText("Question " + (index + 1) + " of " + questions.size());
        prevButton.setDisable(index == 0);
        nextButton.setDisable(index == questions.size() - 1);
        refreshNavigation();
    }

    private void answerChosen(String letter) {
        int questionId = questions.get(index).id();
        answers.put(questionId, letter);
        answerQueue.put(questionId, letter); // producer: hand it to the AutoSaveThread
        saveLabel.setText("Saving...");
        refreshNavigation();
    }

    /** Numbered buttons: current question highlighted, answered ones coloured. */
    private void refreshNavigation() {
        navPane.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            int target = i;
            Button b = new Button(String.valueOf(i + 1));
            b.getStyleClass().add("nav-button");
            if (answers.containsKey(questions.get(i).id())) {
                b.getStyleClass().add("answered");
            }
            if (i == index) {
                b.getStyleClass().add("current");
            }
            b.setFocusTraversable(false);
            b.setOnAction(e -> showQuestion(target));
            navPane.getChildren().add(b);
        }
        progressBar.setProgress(questions.isEmpty() ? 0 : (double) answers.size() / questions.size());
    }

    // ---------- timer + auto-save callbacks (already on the JavaFX thread) ----------

    @Override
    public void onTick(int secondsLeft) {
        timerLabel.setText("⏱ " + TimeFormat.minutesSeconds(secondsLeft));
        if (secondsLeft <= 60 && !timerLabel.getStyleClass().contains("timer-warning")) {
            timerLabel.getStyleClass().add("timer-warning");
        }
    }

    @Override
    public void onTimeUp() {
        submit(true);
    }

    @Override
    public void onSaved(boolean onServer) {
        saveLabel.setText(onServer ? "✔ Saved" : "⚠ Offline - saved on this PC");
    }

    // ---------- submit ----------

    /**
     * Submits exactly once, whether the student clicks Submit or the timer reaches zero.
     * synchronized + AtomicBoolean: even if both happen at the same moment, only one submit goes out.
     */
    private synchronized void submit(boolean automatic) {
        if (submitted.get()) {
            return;
        }
        if (!automatic) {
            long unanswered = questions.stream().filter(q -> !answers.containsKey(q.id())).count();
            String msg = unanswered == 0 ? "Submit your answers now?"
                    : "You have " + unanswered + " unanswered question(s). Submit anyway?";
            if (!AlertUtil.confirm(msg)) {
                return;
            }
        }
        if (!submitted.compareAndSet(false, true)) {
            return; // the timer submitted while the confirm dialog was open
        }

        timer.stop();
        autoSave.shutdown();
        setControlsDisabled(true);
        submitButton.setText("Submitting...");
        if (automatic) {
            timerLabel.setText("⏱ Time is over");
        }

        Map<Integer, String> finalAnswers = new HashMap<>(answers);
        Async.run(
                () -> ApiClient.post("/api/attempts/" + data.attemptId() + "/submit",
                        new AnswersRequest(finalAnswers, automatic), ResultDto.class),
                result -> {
                    AutoSaveThread.deleteBackup(data.attemptId());
                    SceneManager.show(new ResultView(result).build(), "Result");
                },
                error -> {
                    // keep the answers; let the student try again (the server accepts a late retry safely)
                    submitted.set(false);
                    submitButton.setDisable(false);
                    submitButton.setText("Retry submit");
                    AlertUtil.error("Could not submit. Check your connection and press \"Retry submit\".", error);
                });
    }

    private void setControlsDisabled(boolean disabled) {
        submitButton.setDisable(disabled);
        prevButton.setDisable(disabled);
        nextButton.setDisable(disabled);
        navPane.setDisable(disabled);
        for (RadioButton rb : optionButtons) {
            rb.setDisable(disabled);
        }
    }
}
