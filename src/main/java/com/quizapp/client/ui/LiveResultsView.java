package com.quizapp.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.quizapp.client.App;
import com.quizapp.client.ApiClient;
import com.quizapp.client.LiveClient;
import com.quizapp.client.util.AlertUtil;
import com.quizapp.client.util.Async;
import com.quizapp.client.util.NetworkUtil;
import com.quizapp.client.util.ResultExporter;
import com.quizapp.client.util.TimeFormat;
import com.quizapp.dto.LiveState;
import com.quizapp.dto.ResultDto;
import com.quizapp.model.Quiz;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/** Screen 5: admin watches a quiz in real time (WebSocket) and exports results to CSV. */
public class LiveResultsView implements LiveClient.Handler {

    private final Quiz initialQuiz;
    private LiveClient liveClient;
    private LiveState lastState;

    private final ComboBox<Quiz> quizBox = new ComboBox<>();
    private final Label statusLabel = new Label("Connecting...");
    private final Label writingCount = new Label("0");
    private final Label submittedCount = new Label("0");
    private final TableView<LiveState.Writing> writingTable = new TableView<>();
    private final TableView<LiveState.LeaderboardEntry> leaderboardTable = new TableView<>();
    /** Activity log. Vector is the old thread-safe List (every method is synchronized). */
    private final Vector<String> activity = new Vector<>();
    private final ListView<String> activityList = new ListView<>();
    private final TextField linkField = new TextField();

    /** @param quiz quiz to show first (may be null) */
    public LiveResultsView(Quiz quiz) {
        this.initialQuiz = quiz;
    }

    public Parent build() {
        Button backButton = new Button("< Back to quizzes");
        backButton.setOnAction(e -> SceneManager.showDashboard());
        Label title = new Label("Live results");
        title.getStyleClass().add("screen-title");
        quizBox.setPromptText("Choose a quiz");
        quizBox.setOnAction(e -> watch(quizBox.getValue()));
        statusLabel.getStyleClass().add("muted");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(16, backButton, title, quizBox, spacer, statusLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(16, statBox(writingCount, "writing now"), statBox(submittedCount, "submitted"));

        writingTable.getColumns().add(column("Name", LiveState.Writing::name));
        writingTable.getColumns().add(column("Answered", w -> w.answered() + " / "
                + (lastState == null ? "?" : lastState.totalQuestions())));
        writingTable.setPlaceholder(new Label("Nobody is writing"));
        writingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        leaderboardTable.getColumns().add(column("#", e -> String.valueOf(e.rank())));
        leaderboardTable.getColumns().add(column("Name", LiveState.LeaderboardEntry::name));
        leaderboardTable.getColumns().add(column("Score", e -> e.score() + " / " + e.total()));
        leaderboardTable.getColumns().add(column("Time", e -> TimeFormat.minutesSeconds(e.timeTakenSec())));
        leaderboardTable.setPlaceholder(new Label("No submissions yet"));
        leaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        activityList.setPlaceholder(new Label("Activity will appear here"));

        HBox panels = new HBox(16,
                panel("Leaderboard", leaderboardTable, 2),
                panel("Writing now", writingTable, 1),
                panel("Activity", activityList, 1));
        VBox.setVgrow(panels, Priority.ALWAYS);

        linkField.setEditable(false);
        HBox.setHgrow(linkField, Priority.ALWAYS);
        Button copyButton = new Button("Copy link");
        copyButton.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(linkField.getText());
            Clipboard.getSystemClipboard().setContent(content);
            statusLabel.setText("Link copied");
        });
        Button openButton = new Button("Open in browser");
        openButton.setOnAction(e -> App.openInBrowser(linkField.getText()));
        Button exportButton = new Button("Export CSV");
        exportButton.getStyleClass().add("primary-button");
        exportButton.setOnAction(e -> exportCsv());
        Label linkLabel = new Label("Live page (open on any phone on the same Wi-Fi):");
        HBox linkRow = new HBox(8, linkField, copyButton, openButton, exportButton);
        linkRow.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(14, header, stats, panels, linkLabel, linkRow);
        content.setPadding(new Insets(20));
        BorderPane root = new BorderPane(content);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");

        // Close the WebSocket when this screen is replaced by another one
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                stopWatching();
            }
        });
        loadQuizzes();
        return root;
    }

    private void loadQuizzes() {
        Async.run(
                () -> ApiClient.get("/api/admin/quizzes", new TypeReference<List<Quiz>>() { }),
                quizzes -> {
                    quizzes.removeIf(q -> !q.isPublished());
                    quizBox.getItems().setAll(quizzes);
                    Quiz first = quizzes.stream()
                            .filter(q -> initialQuiz != null && q.getId() == initialQuiz.getId())
                            .findFirst()
                            .orElse(quizzes.isEmpty() ? null : quizzes.get(0));
                    if (first == null) {
                        statusLabel.setText("Publish a quiz to watch it live");
                    } else {
                        quizBox.setValue(first); // triggers watch()
                    }
                },
                error -> statusLabel.setText(error.getMessage()));
    }

    private void watch(Quiz quiz) {
        stopWatching();
        lastState = null;
        activity.clear();
        activityList.getItems().clear();
        writingTable.getItems().clear();
        leaderboardTable.getItems().clear();
        if (quiz == null) {
            return;
        }
        linkField.setText(NetworkUtil.livePageUrl(quiz.getId()));
        liveClient = new LiveClient(quiz.getId(), this);
        liveClient.connect();
    }

    private void stopWatching() {
        if (liveClient != null) {
            liveClient.close();
            liveClient = null;
        }
    }

    // ---------- LiveClient.Handler (already on the JavaFX thread) ----------

    @Override
    public void onState(LiveState state) {
        logChanges(lastState, state);
        lastState = state;
        writingCount.setText(String.valueOf(state.writing().size()));
        submittedCount.setText(String.valueOf(state.submittedCount()));
        writingTable.getItems().setAll(state.writing());
        leaderboardTable.getItems().setAll(state.leaderboard());
    }

    @Override
    public void onConnection(boolean connected, String message) {
        statusLabel.setText((connected ? "● " : "○ ") + message);
        statusLabel.getStyleClass().removeAll("stat-good", "stat-bad");
        statusLabel.getStyleClass().add(connected ? "stat-good" : "stat-bad");
    }

    /** Compares two states and writes "started" / "submitted" lines to the activity log. */
    private void logChanges(LiveState before, LiveState now) {
        if (before == null) {
            return; // first state: nothing to compare with
        }
        Set<String> wasWriting = new HashSet<>();
        before.writing().forEach(w -> wasWriting.add(w.name()));
        Set<String> oldRows = new HashSet<>();
        before.leaderboard().forEach(e -> oldRows.add(e.name() + "|" + e.score() + "|" + e.timeTakenSec()));

        for (LiveState.Writing w : now.writing()) {
            if (!wasWriting.contains(w.name())) {
                addActivity(w.name() + " started");
            }
        }
        if (now.submittedCount() > before.submittedCount()) {
            for (LiveState.LeaderboardEntry e : now.leaderboard()) {
                if (!oldRows.contains(e.name() + "|" + e.score() + "|" + e.timeTakenSec())) {
                    addActivity(e.name() + " submitted: " + e.score() + "/" + e.total());
                }
            }
        }
    }

    private void addActivity(String text) {
        String line = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "  " + text;
        activity.add(0, line);             // newest first
        if (activity.size() > 100) {
            activity.remove(activity.size() - 1);
        }
        activityList.getItems().setAll(activity);
    }

    // ---------- CSV export ----------

    private void exportCsv() {
        Quiz quiz = quizBox.getValue();
        if (quiz == null) {
            AlertUtil.error("Choose a quiz first.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save results");
        chooser.setInitialFileName(quiz.getTitle().replaceAll("[^A-Za-z0-9 _-]", "") + " results.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file", "*.csv"));
        File file = chooser.showSaveDialog(linkField.getScene().getWindow());
        if (file == null) {
            return; // cancelled
        }
        Async.run(
                () -> {
                    List<ResultDto> rows = ApiClient.get("/api/admin/quizzes/" + quiz.getId() + "/results",
                            new TypeReference<List<ResultDto>>() { });
                    return ResultExporter.export(quiz.getTitle(), rows, file); // file writing off the UI thread
                },
                count -> AlertUtil.info("Saved " + count + " result(s) to:\n" + file.getAbsolutePath()),
                error -> AlertUtil.error("Could not export the results.", error));
    }

    // ---------- small builders ----------

    private static <T> TableColumn<T, String> column(String name, Function<T, String> value) {
        TableColumn<T, String> col = new TableColumn<>(name);
        col.setCellValueFactory(c -> new SimpleStringProperty(value.apply(c.getValue())));
        return col;
    }

    private static VBox statBox(Label number, String text) {
        number.getStyleClass().add("stat-number");
        Label label = new Label(text);
        label.getStyleClass().add("muted");
        VBox box = new VBox(2, number, label);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(12, 20, 12, 20));
        box.setMinWidth(150);
        return box;
    }

    private static VBox panel(String title, javafx.scene.Node body, int grow) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        VBox box = new VBox(8, label, body);
        VBox.setVgrow(body, Priority.ALWAYS);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setPrefWidth(grow * 300);
        return box;
    }
}
