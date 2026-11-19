package com.quizapp.client.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/**
 * Runs slow work (network calls) on a background thread so the window never freezes,
 * then hands the result back on the JavaFX Application Thread.
 */
public final class Async {

    private Async() {
    }

    /** Generic method: works for any result type T. */
    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call(); // runs on the background thread
            }
        };
        // these two callbacks run on the JavaFX Application Thread
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> onError.accept(task.getException()));

        Thread thread = new Thread(task, "api-call");
        thread.setDaemon(true); // does not keep the app alive after the window closes
        thread.start();
    }
}
