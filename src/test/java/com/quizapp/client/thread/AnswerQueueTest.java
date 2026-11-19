package com.quizapp.client.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AnswerQueueTest {

    @Test
    void consumerWaitsUntilProducerPutsAnswer() throws Exception {
        AnswerQueue queue = new AnswerQueue();
        AtomicReference<Map<Integer, String>> received = new AtomicReference<>();

        Thread consumer = new Thread(() -> {
            try {
                received.set(queue.take());
            } catch (InterruptedException ignored) {
                // test ends
            }
        });
        consumer.start();
        Thread.sleep(200);
        assertEquals(Thread.State.WAITING, consumer.getState()); // blocked in wait()

        queue.put(7, "B");
        consumer.join(2000);
        assertEquals(Map.of(7, "B"), received.get());
    }

    @Test
    void latestAnswerForAQuestionWins() throws Exception {
        AnswerQueue queue = new AnswerQueue();
        queue.put(1, "A");
        queue.put(1, "C");
        queue.put(2, "D");
        assertEquals(Map.of(1, "C", 2, "D"), queue.take());
    }

    @Test
    void backupFileRoundTrip() throws Exception {
        int attemptId = -12345; // test-only id
        try {
            AutoSaveThread.writeBackup(attemptId, Map.of(3, "A", 9, "D"));
            assertTrue(Files.exists(AutoSaveThread.backupFile(attemptId)));
            assertEquals(Map.of(3, "A", 9, "D"), AutoSaveThread.readBackup(attemptId));
        } finally {
            AutoSaveThread.deleteBackup(attemptId);
        }
        assertTrue(AutoSaveThread.readBackup(attemptId).isEmpty());
    }
}
