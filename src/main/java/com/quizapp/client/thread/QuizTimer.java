package com.quizapp.client.thread;

import javafx.application.Platform;

/**
 * Countdown timer for a quiz, running on its own thread (created from a Runnable).
 * Every second it tells the screen how much time is left; at zero it tells the screen to submit.
 * The screen is only updated through Platform.runLater, because JavaFX controls may only be
 * touched on the JavaFX Application Thread.
 */
public class QuizTimer implements Runnable {

    /** What the quiz screen wants to hear from the timer. */
    public interface Listener {
        void onTick(int secondsLeft);

        void onTimeUp();
    }

    private final Listener listener;
    private final long endNanos;           // when the time runs out (System.nanoTime based)
    private volatile boolean running = true; // volatile: written by one thread, read by another
    private Thread thread;

    public QuizTimer(int seconds, Listener listener) {
        this.listener = listener;
        this.endNanos = System.nanoTime() + seconds * 1_000_000_000L;
    }

    public void start() {
        thread = new Thread(this, "quiz-timer");
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }

    @Override
    public void run() {
        int left = secondsLeft();
        while (running && left > 0) {
            int shown = left;
            Platform.runLater(() -> listener.onTick(shown));
            try {
                Thread.sleep(1000); // thread goes to TIMED_WAITING for one second
            } catch (InterruptedException e) {
                return; // stop() was called
            }
            left = secondsLeft(); // computed from the clock, so the timer never drifts
        }
        if (running) {
            Platform.runLater(() -> {
                listener.onTick(0);
                listener.onTimeUp();
            });
        }
    }

    /** Stops the timer (after submit or when leaving the screen). */
    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private int secondsLeft() {
        long nanos = endNanos - System.nanoTime();
        return nanos <= 0 ? 0 : (int) Math.ceil(nanos / 1_000_000_000.0);
    }
}
