package com.quizapp.client.awt;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * "Keyboard Shortcuts" help window, built with AWT to show the delegation event model:
 *  - event source (Button / window / keyboard) -> event object -> registered listener
 *  - ActionListener for the Close button
 *  - KeyListener for the Esc key
 *  - WindowAdapter (an adapter class) for the window's close box: we override only windowClosing,
 *    instead of implementing all seven WindowListener methods.
 */
public class HelpWindow extends Frame implements ActionListener, KeyListener {

    private static final String HELP_TEXT = String.join("\n",
            "TAKING A QUIZ",
            "  1, 2, 3, 4      choose option A, B, C or D",
            "  Left arrow      previous question",
            "  Right arrow     next question",
            "  F1              open this help",
            "",
            "  Answers are saved automatically while you write.",
            "  When the timer reaches 0:00 the quiz is submitted for you.",
            "",
            "LOGIN SCREEN",
            "  Enter           log in",
            "",
            "QUESTION EDITOR (admin)",
            "  Tab             move to the next field",
            "",
            "Press Esc or click Close to close this window.");

    private static HelpWindow openWindow; // only one help window at a time

    private final Button closeButton = new Button("Close");

    private HelpWindow() {
        super("QuizMaster - Keyboard Shortcuts");
        setLayout(new BorderLayout(8, 8));

        Label heading = new Label("Keyboard shortcuts", Label.CENTER);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        TextArea text = new TextArea(HELP_TEXT, 18, 52, TextArea.SCROLLBARS_VERTICAL_ONLY);
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        // Register listeners with the event sources
        closeButton.addActionListener(this);
        closeButton.addKeyListener(this);
        text.addKeyListener(this);
        addKeyListener(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });

        Panel bottom = new Panel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(closeButton);

        add(heading, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null); // centre on screen
    }

    /** Opens the window (or brings it to the front). Safe to call from the JavaFX thread. */
    public static void showWindow() {
        EventQueue.invokeLater(() -> { // AWT work runs on the AWT event thread
            if (openWindow == null) {
                openWindow = new HelpWindow();
            }
            openWindow.setVisible(true);
            openWindow.toFront();
            openWindow.closeButton.requestFocus();
        });
    }

    private void closeWindow() {
        setVisible(false);
        dispose();
        openWindow = null;
    }

    // ---- ActionListener ----

    @Override
    public void actionPerformed(ActionEvent e) {
        closeWindow(); // Close button clicked
    }

    // ---- KeyListener (all three methods must be written; only keyPressed is needed) ----

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            closeWindow();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // not needed
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // not needed
    }
}
