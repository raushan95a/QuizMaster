package com.quizapp.client.thread;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hand-over point between two threads (producer-consumer):
 *  - the quiz screen (producer) puts each answer the student picks,
 *  - the AutoSaveThread (consumer) takes them and saves them.
 * take() waits while nothing is pending; put() wakes it up with notify().
 */
public class AnswerQueue {

    private final Map<Integer, String> pending = new LinkedHashMap<>();

    /** Called by the producer (JavaFX thread) when an answer changes. */
    public synchronized void put(int questionId, String option) {
        pending.put(questionId, option);
        notify(); // wake up the waiting consumer
    }

    /** Called by the consumer. Blocks (wait) until at least one answer is pending, then returns all of them. */
    public synchronized Map<Integer, String> take() throws InterruptedException {
        while (pending.isEmpty()) {
            wait(); // releases the lock and sleeps until put() calls notify()
        }
        Map<Integer, String> batch = new HashMap<>(pending);
        pending.clear();
        return batch;
    }
}
