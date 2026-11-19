package com.quizapp.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.quizapp.client.ApiClient;
import com.quizapp.client.Session;
import com.quizapp.client.util.AlertUtil;
import com.quizapp.client.util.Async;
import com.quizapp.model.Quiz;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Screen 3: admin home — list of quizzes with a toolbar of actions. */
public class AdminDashboard {

    private final TableView<Quiz> table = new TableView<>();
    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete");
    private final Button publishButton = new Button("Publish");
    private final Label statusLabel = new Label();

    public Parent build() {
        Label title = new Label(Session.getUser().getDashboardTitle());
        title.getStyleClass().add("screen-title");

        Button newButton = new Button("+ New Quiz");
        newButton.getStyleClass().add("primary-button");
        newButton.setOnAction(e -> SceneManager.show(new QuizEditorView(null).build(), "New Quiz"));
        editButton.setOnAction(e -> editSelected());
        deleteButton.setOnAction(e -> deleteSelected());
        publishButton.setOnAction(e -> togglePublish());
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadQuizzes());
        Button liveButton = new Button("● Live Results");
        liveButton.setOnAction(e -> SceneManager.show(
                new LiveResultsView(table.getSelectionModel().getSelectedItem()).build(), "Live Results"));

        ToolBar toolBar = new ToolBar(newButton, new Separator(), editButton, deleteButton, publishButton,
                new Separator(), liveButton, refreshButton);

        buildTable();
        updateButtons();

        statusLabel.getStyleClass().add("muted");
        VBox content = new VBox(12, title, toolBar, table, statusLabel);
        content.setPadding(new Insets(20));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        BorderPane root = new BorderPane(content);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");

        loadQuizzes();
        return root;
    }

    private void buildTable() {
        TableColumn<Quiz, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        titleCol.setPrefWidth(280);

        TableColumn<Quiz, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubject()));
        subjectCol.setPrefWidth(180);

        TableColumn<Quiz, Number> questionsCol = new TableColumn<>("Questions");
        questionsCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuestionCount()));

        TableColumn<Quiz, Number> timeCol = new TableColumn<>("Time (min)");
        timeCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTimeLimitMin()));

        TableColumn<Quiz, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isPublished() ? "Published" : "Draft"));

        table.getColumns().add(titleCol);
        table.getColumns().add(subjectCol);
        table.getColumns().add(questionsCol);
        table.getColumns().add(timeCol);
        table.getColumns().add(statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No quizzes yet. Click \"+ New Quiz\" to create one."));

        // Listener: enable/disable buttons when the selected row changes
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldQuiz, newQuiz) -> updateButtons());

        // Double-click a row to edit it
        table.setRowFactory(tv -> {
            TableRow<Quiz> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editSelected();
                }
            });
            return row;
        });
    }

    private void loadQuizzes() {
        statusLabel.setText("Loading...");
        Async.run(
                () -> ApiClient.get("/api/admin/quizzes", new TypeReference<List<Quiz>>() { }),
                quizzes -> {
                    table.getItems().setAll(quizzes);
                    statusLabel.setText(quizzes.size() + " quiz(zes)");
                    updateButtons();
                },
                error -> statusLabel.setText(error.getMessage()));
    }

    private void updateButtons() {
        Quiz selected = table.getSelectionModel().getSelectedItem();
        boolean none = selected == null;
        editButton.setDisable(none);
        deleteButton.setDisable(none);
        publishButton.setDisable(none);
        publishButton.setText(!none && selected.isPublished() ? "Unpublish" : "Publish");
    }

    private void editSelected() {
        Quiz selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SceneManager.show(new QuizEditorView(selected).build(), "Edit Quiz");
        }
    }

    private void deleteSelected() {
        Quiz selected = table.getSelectionModel().getSelectedItem();
        if (selected == null || !AlertUtil.confirm("Delete \"" + selected.getTitle()
                + "\"?\nIts questions and all student results will be deleted too.")) {
            return;
        }
        Async.run(
                () -> {
                    ApiClient.delete("/api/admin/quizzes/" + selected.getId());
                    return null;
                },
                ignored -> loadQuizzes(),
                error -> AlertUtil.error("Could not delete the quiz.", error));
    }

    private void togglePublish() {
        Quiz selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        String action = selected.isPublished() ? "unpublish" : "publish";
        Async.run(
                () -> ApiClient.post("/api/admin/quizzes/" + selected.getId() + "/" + action, null, Quiz.class),
                updated -> loadQuizzes(),
                error -> AlertUtil.error(error.getMessage()));
    }
}
