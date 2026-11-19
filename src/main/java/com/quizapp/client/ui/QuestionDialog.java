package com.quizapp.client.ui;

import com.quizapp.model.Question;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/** Pop-up to add or edit one question: text, four options, and a radio button for the correct one. */
public final class QuestionDialog {

    private QuestionDialog() {
    }

    /**
     * @param existing question to edit, or null for a new one
     * @return the filled-in question, or empty if the user cancelled
     */
    public static Optional<Question> show(Question existing) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add question" : "Edit question");
        dialog.setHeaderText("Type the question and four options, then select the correct one.");
        dialog.getDialogPane().getStylesheets().add(SceneManager.stylesheet());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea questionArea = new TextArea();
        questionArea.setPromptText("Question");
        questionArea.setPrefRowCount(3);
        questionArea.setWrapText(true);

        ToggleGroup correctGroup = new ToggleGroup();
        RadioButton[] radios = new RadioButton[4];
        TextField[] fields = new TextField[4];

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(questionArea, 0, 0, 2, 1);
        grid.add(new Label("Correct?   Option"), 0, 1, 2, 1);
        for (int i = 0; i < 4; i++) {
            char letter = Question.OPTIONS[i];
            radios[i] = new RadioButton(letter + ")");
            radios[i].setToggleGroup(correctGroup);
            radios[i].setUserData(String.valueOf(letter));
            radios[i].setFocusTraversable(false); // Tab goes option -> option; radios are clicked
            fields[i] = new TextField();
            fields[i].setPromptText("Option " + letter);
            fields[i].setPrefWidth(380);
            GridPane.setHgrow(fields[i], Priority.ALWAYS);
            grid.add(radios[i], 0, i + 2);
            grid.add(fields[i], 1, i + 2);
        }
        Label error = new Label();
        error.getStyleClass().add("error-label");
        grid.add(error, 0, 6, 2, 1);
        FormUtil.tabMovesFocus(questionArea, fields[0]);

        if (existing != null) {
            questionArea.setText(existing.getText());
            for (int i = 0; i < 4; i++) {
                char letter = Question.OPTIONS[i];
                fields[i].setText(existing.optionText(letter));
                if (existing.getCorrectOption() != null && existing.getCorrectOption().charAt(0) == letter) {
                    radios[i].setSelected(true);
                }
            }
        }
        dialog.getDialogPane().setContent(grid);
        // Start typing in the question box (otherwise focus starts on the OK button)
        dialog.setOnShown(e -> Platform.runLater(questionArea::requestFocus));

        // Keep the dialog open if something is missing
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String problem = null;
            if (questionArea.getText().isBlank()) {
                problem = "Please type the question.";
            } else if (correctGroup.getSelectedToggle() == null) {
                problem = "Select the correct option.";
            } else {
                for (int i = 0; i < 4; i++) {
                    if (fields[i].getText().isBlank()) {
                        problem = "Option " + Question.OPTIONS[i] + " is empty.";
                        break;
                    }
                }
            }
            if (problem != null) {
                error.setText(problem);
                event.consume(); // stops the dialog from closing
            }
        });

        dialog.setResultConverter(button -> button == ButtonType.OK
                ? new Question(questionArea.getText().trim(),
                        fields[0].getText().trim(), fields[1].getText().trim(),
                        fields[2].getText().trim(), fields[3].getText().trim(),
                        (String) correctGroup.getSelectedToggle().getUserData())
                : null);

        return dialog.showAndWait();
    }
}
