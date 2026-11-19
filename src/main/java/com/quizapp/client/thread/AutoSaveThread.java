package com.quizapp.client.thread;

import com.quizapp.client.ApiClient;
import com.quizapp.dto.AnswersRequest;
import com.quizapp.exception.QuizAppException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;

/**
 * Background thread (created by extending Thread) that saves answers while the student writes:
 *  1. waits for new answers from the AnswerQueue,
 *  2. writes all answers to a local backup file (byte stream: FileOutputStream + DataOutputStream),
 *  3. sends them to the server, which also powers the live progress view.
 * If the network is down, the answers are still safe in the backup file.
 */
public class AutoSaveThread extends Thread {

    /** Tells the quiz screen whether the last save reached the server. */
    public interface StatusListener {
        void onSaved(boolean onServer);
    }

    private static final Path FOLDER = Path.of("autosave");

    private final int attemptId;
    private final AnswerQueue queue;
    private final StatusListener listener;
    private final Map<Integer, String> allAnswers = new HashMap<>();
    private volatile boolean running = true;

    public AutoSaveThread(int attemptId, AnswerQueue queue, Map<Integer, String> initialAnswers,
                          StatusListener listener) {
        super("auto-save");
        this.attemptId = attemptId;
        this.queue = queue;
        this.listener = listener;
        this.allAnswers.putAll(initialAnswers);
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY); // saving is background work; the UI matters more
    }

    @Override
    public void run() {
        while (running) {
            Map<Integer, String> batch;
            try {
                batch = queue.take(); // WAITING until the student answers something
            } catch (InterruptedException e) {
                break; // shutdown() was called
            }
            allAnswers.putAll(batch);
            Map<Integer, String> snapshot = new HashMap<>(allAnswers);

            try {
                writeBackup(attemptId, snapshot);
            } catch (IOException e) {
                // the server copy below still works; nothing else to do
            }

            boolean onServer;
            try {
                ApiClient.put("/api/attempts/" + attemptId + "/progress", new AnswersRequest(snapshot, false));
                onServer = true;
            } catch (QuizAppException e) {
                onServer = false;
            }
            if (running) {
                boolean ok = onServer;
                Platform.runLater(() -> listener.onSaved(ok));
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    // ---------- local backup file ----------

    static Path backupFile(int attemptId) {
        return FOLDER.resolve("attempt-" + attemptId + ".dat");
    }

    /** Writes the answers as: count, then (question id, option) pairs. */
    static void writeBackup(int attemptId, Map<Integer, String> answers) throws IOException {
        Files.createDirectories(FOLDER);
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(backupFile(attemptId).toFile()))) {
            out.writeInt(answers.size());
            for (Map.Entry<Integer, String> e : answers.entrySet()) {
                out.writeInt(e.getKey());
                out.writeUTF(e.getValue() == null ? "" : e.getValue());
            }
        }
    }

    /** Reads answers saved on this computer for an attempt (empty if there is no backup). */
    public static Map<Integer, String> readBackup(int attemptId) {
        Map<Integer, String> answers = new HashMap<>();
        Path file = backupFile(attemptId);
        if (!Files.exists(file)) {
            return answers;
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(file.toFile()))) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                int questionId = in.readInt();
                String option = in.readUTF();
                if (!option.isEmpty()) {
                    answers.put(questionId, option);
                }
            }
        } catch (IOException e) {
            answers.clear(); // damaged file: ignore it
        }
        return answers;
    }

    public static void deleteBackup(int attemptId) {
        try {
            Files.deleteIfExists(backupFile(attemptId));
        } catch (IOException ignored) {
            // not important
        }
    }
}
