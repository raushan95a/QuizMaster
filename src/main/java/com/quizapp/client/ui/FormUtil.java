package com.quizapp.client.ui;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** Small helpers for forms. */
final class FormUtil {

    private FormUtil() {
    }

    /**
     * In a TextArea, Tab normally types a tab character. This makes Tab jump to the next field instead,
     * like in every other input (KeyEvent handling with an event filter).
     */
    static void tabMovesFocus(TextArea area, Node next) {
        area.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB && !event.isShiftDown() && !event.isControlDown()) {
                event.consume();
                next.requestFocus();
            }
        });
    }
}
