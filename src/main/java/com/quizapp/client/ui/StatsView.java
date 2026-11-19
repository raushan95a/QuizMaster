package com.quizapp.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.quizapp.client.ApiClient;
import com.quizapp.client.util.Async;
import com.quizapp.client.util.Pair;
import com.quizapp.client.util.ScoreStats;
import com.quizapp.dto.ResultDto;
import com.quizapp.model.Quiz;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Admin screen with charts: how many students scored in each range, and pass vs fail. */
public class StatsView {

    private final Quiz initialQuiz;
    private final ComboBox<Quiz> quizBox = new ComboBox<>();
    private final Label summary = new Label("Choose a quiz");
    private final CategoryAxis rangeAxis = new CategoryAxis();
    private final NumberAxis countAxis = new NumberAxis();
    private final BarChart<String, Number> barChart = new BarChart<>(rangeAxis, countAxis);
    private final PieChart pieChart = new PieChart();

    public StatsView(Quiz quiz) {
        this.initialQuiz = quiz;
    }

    public Parent build() {
        Button backButton = new Button("< Back to quizzes");
        backButton.setOnAction(e -> SceneManager.showDashboard());
        Label title = new Label("Charts");
        title.getStyleClass().add("screen-title");
        quizBox.setPromptText("Choose a quiz");
        // Listener on the value: runs whether the user picks a quiz or the code selects one
        quizBox.valueProperty().addListener((obs, oldQuiz, newQuiz) -> load(newQuiz));
        HBox header = new HBox(16, backButton, title, quizBox);
        header.setAlignment(Pos.CENTER_LEFT);

        summary.getStyleClass().add("section-title");

        rangeAxis.setLabel("Score range");
        countAxis.setLabel("Students");
        countAxis.setTickUnit(1);
        countAxis.setMinorTickVisible(false);
        barChart.setTitle("Score distribution");
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        pieChart.setTitle("Pass (50 % or more) vs fail");
        pieChart.setLabelsVisible(true);

        HBox charts = new HBox(16, wrap(barChart), wrap(pieChart));
        VBox.setVgrow(charts, Priority.ALWAYS);

        VBox content = new VBox(14, header, summary, charts);
        content.setPadding(new Insets(20));
        BorderPane root = new BorderPane(content);
        root.setTop(MainMenu.create());
        root.getStyleClass().add("page");

        Async.run(
                () -> ApiClient.get("/api/admin/quizzes", new TypeReference<List<Quiz>>() { }),
                quizzes -> {
                    quizBox.getItems().setAll(quizzes);
                    Quiz first = quizzes.stream()
                            .filter(q -> initialQuiz != null && q.getId() == initialQuiz.getId())
                            .findFirst()
                            .orElse(quizzes.isEmpty() ? null : quizzes.get(0));
                    if (first != null) {
                        quizBox.setValue(first); // triggers load()
                    }
                },
                error -> summary.setText(error.getMessage()));
        return root;
    }

    private void load(Quiz quiz) {
        if (quiz == null) {
            return;
        }
        summary.setText("Loading...");
        Async.run(
                () -> ApiClient.get("/api/admin/quizzes/" + quiz.getId() + "/results",
                        new TypeReference<List<ResultDto>>() { }),
                results -> show(ScoreStats.of(results)),
                error -> summary.setText(error.getMessage()));
    }

    private void show(ScoreStats stats) {
        if (stats.getCount() == 0) {
            summary.setText("No results yet for this quiz.");
        } else {
            summary.setText(stats.getCount() + " attempt(s)   •   average " + stats.getAverage()
                    + " %   •   highest " + stats.getHighest() + " %   •   lowest " + stats.getLowest() + " %");
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int most = 0;
        for (Pair<String, Integer> range : stats.getRanges()) {
            series.getData().add(new XYChart.Data<>(range.getFirst(), range.getSecond()));
            most = Math.max(most, range.getSecond());
        }
        // Whole numbers only on the axis (you can't have 2.5 students)
        countAxis.setAutoRanging(false);
        countAxis.setLowerBound(0);
        countAxis.setUpperBound(most + 1);
        countAxis.setTickUnit(Math.max(1, (most + 1) / 10));
        barChart.getData().setAll(List.of(series));

        pieChart.getData().setAll(
                new PieChart.Data("Pass (" + stats.getPassed() + ")", stats.getPassed()),
                new PieChart.Data("Fail (" + stats.getFailed() + ")", stats.getFailed()));
    }

    private static VBox wrap(javafx.scene.Node chart) {
        VBox box = new VBox(chart);
        box.getStyleClass().add("card");
        VBox.setVgrow(chart, Priority.ALWAYS);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }
}
