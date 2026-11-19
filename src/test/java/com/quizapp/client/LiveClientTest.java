package com.quizapp.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quizapp.dto.LiveState;
import com.quizapp.server.Config;
import com.quizapp.server.ServerMain;
import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** End-to-end: real server + the app's LiveClient over a real WebSocket. */
class LiveClientTest {

    @TempDir
    Path tempDir;

    private Javalin server;
    private String baseUrl;
    private LiveClient client;
    private final BlockingQueue<LiveState> states = new LinkedBlockingQueue<>();
    private final BlockingQueue<Boolean> connections = new LinkedBlockingQueue<>();

    @BeforeEach
    void startServer() throws Exception {
        Config config = new Config(true, 0, tempDir.resolve("live.db").toString(), "admin", "admin123");
        server = ServerMain.createApp(config).start(0); // port 0 = any free port
        baseUrl = "http://localhost:" + server.port();
    }

    @AfterEach
    void stop() {
        if (client != null) {
            client.close();
        }
        server.stop();
    }

    @Test
    void receivesStateOnConnectAndAfterStudentStarts() throws Exception {
        client = new LiveClient(baseUrl, 1, new LiveClient.Handler() {
            @Override
            public void onState(LiveState state) {
                states.add(state);
            }

            @Override
            public void onConnection(boolean connected, String message) {
                connections.add(connected);
            }
        }, Runnable::run);
        client.connect();

        assertEquals(Boolean.TRUE, connections.poll(5, TimeUnit.SECONDS));
        LiveState first = states.poll(5, TimeUnit.SECONDS);
        assertNotNull(first, "no state received on connect");
        assertEquals("Java Basics", first.quizTitle()); // sample quiz seeded in dev mode
        assertTrue(first.writing().isEmpty());

        // A student registers and starts the quiz -> a new state is pushed
        String token = post("/api/auth/register",
                "{\"fullName\":\"Ravi\",\"username\":\"ravi\",\"password\":\"secret123\"}", null)
                .replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        post("/api/quizzes/1/start", "", token);

        LiveState next = states.poll(5, TimeUnit.SECONDS);
        assertNotNull(next, "no update after the student started");
        assertEquals(1, next.writing().size());
        assertEquals("Ravi", next.writing().get(0).name());
    }

    private String post(String path, String body, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return HttpClient.newHttpClient().send(b.build(), HttpResponse.BodyHandlers.ofString()).body();
    }
}
