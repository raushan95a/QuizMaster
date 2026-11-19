package com.quizapp.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.quizapp.client.ApiClient;
import com.quizapp.client.util.Async;
import com.quizapp.client.util.TimeFormat;
import com.quizapp.dto.ResultDto;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Screen 10: the student's past results. Double-click a row to review it. */
public class HistoryView {

    private final TableView<ResultDto> table = new TableView<>();

    public Parent build() {
        Button backButton = new Button("< Back to quizzes");
        backButton.setOnAction(e -> SceneManager.showDashboard());
        Label title = new Label("My results");
        title.getStyleClass().add("screen-title");
        HBox header = new HBox(16, backButton, title);
        header.setAlignment(Pos.CENTER_LEFT);

        table.getColumns().add(column("Quiz", r -> r.quizTitle()));
        table.getColumns().add(column("Score", r -> r.score() + " / " + r.total()));
        table.getColumns().add(column("Percent", r -> r.percentage() + " %"));
        table.getColumns().add(column("Time", r -> TimeFormat.minutesSeconds(r.timeTakenSec())));
        table.getColumns().add(column("Date", r -> r.submittedAt() == null ? "" : r.submittedAt().substring(0, 10)));
        table.getColumns().add(column("Note", r -> r.autoSubmitted() ? "Time ran out" : ""));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("You have not finished any quiz yet."));
        table.setRowFactory(tv -> {
            TableRow<ResultDto> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    ResultDto r = row.getItem();
                    SceneManager.show(new ReviewView(r.attemptId(), r.quizTitle(),
                            () -> SceneManager.show(new HistoryView().build(), "My Results")).build(), "Review");
                }
            });
            return row;
        });
        VBox.setVgrow(table, Priority.ALWAYS);

        Label hint = new Label("Double-click a result to see the answers.");
        hint.getStyleClass().add("muted");

        VBox content = new VBox(16, header, table, hint);
        content.setPadding(new Insets(20));
        BorderPane root = new BorderPane(content);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");

        Async.run(
                () -> ApiClient.get("/api/me/attempts", new TypeReference<List<ResultDto>>() { }),
                rows -> table.getItems().setAll(rows),
                error -> table.setPlaceholder(new Label(error.getMessage())));
        return root;
    }

    /** Small helper to make a text column from a function. */
    private static TableColumn<ResultDto, String> column(String name,
                                                         java.util.function.Function<ResultDto, String> value) {
        TableColumn<ResultDto, String> col = new TableColumn<>(name);
        col.setCellValueFactory(c -> new SimpleStringProperty(value.apply(c.getValue())));
        return col;
    }
}
