package com.quizapp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.quizapp.dto.LiveState;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * WebSocket connection to /ws/live?quiz=ID using java.net.http.
 * Messages arrive on a background thread; they are handed to the screen with Platform.runLater.
 * If the connection drops, it reconnects every 3 seconds until close() is called.
 */
public class LiveClient implements WebSocket.Listener {

    /** What the Live Results screen wants to hear. */
    public interface Handler {
        void onState(LiveState state);

        void onConnection(boolean connected, String message);
    }

    private final String serverUrl;
    private final int quizId;
    private final Handler handler;
    private final Executor uiThread; // where handler calls run (the JavaFX thread in the app)
    private final StringBuilder buffer = new StringBuilder(); // a message may arrive in several parts
    private final ScheduledExecutorService retry = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "live-reconnect");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean closed;
    private volatile WebSocket socket;

    public LiveClient(int quizId, Handler handler) {
        this(ClientConfig.serverUrl(), quizId, handler, Platform::runLater);
    }

    /** Full constructor (also used by tests, which run handlers directly instead of on the JavaFX thread). */
    public LiveClient(String serverUrl, int quizId, Handler handler, Executor uiThread) {
        this.serverUrl = serverUrl;
        this.quizId = quizId;
        this.handler = handler;
        this.uiThread = uiThread;
    }

    public void connect() {
        if (closed) {
            return;
        }
        String url = serverUrl.replaceFirst("^http", "ws") + "/ws/live?quiz=" + quizId;
        HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(url), this)
                .whenComplete((ws, error) -> {
                    if (error != null) {
                        notifyConnection(false, "Cannot reach the server - retrying...");
                        reconnectLater();
                    } else {
                        socket = ws;
                    }
                });
    }

    public void close() {
        closed = true;
        retry.shutdownNow();
        WebSocket ws = socket;
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
        }
    }

    // ---------- WebSocket.Listener (called on a background thread) ----------

    @Override
    public void onOpen(WebSocket webSocket) {
        notifyConnection(true, "Live");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        buffer.append(data);
        if (last) {
            String text = buffer.toString();
            buffer.setLength(0);
            handleMessage(text);
        }
        webSocket.request(1); // ask for the next message
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        notifyConnection(false, "Disconnected - reconnecting...");
        reconnectLater();
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        notifyConnection(false, "Connection problem - reconnecting...");
        reconnectLater();
    }

    // ---------- helpers ----------

    private void handleMessage(String text) {
        try {
            JsonNode node = ApiClient.json().readTree(text);
            if (node.has("error")) {
                notifyConnection(false, node.get("error").asText());
                return;
            }
            LiveState state = ApiClient.json().treeToValue(node, LiveState.class);
            uiThread.execute(() -> handler.onState(state)); // hand over to the JavaFX thread
        } catch (Exception e) {
            // ignore a broken message; the next one will update the screen
        }
    }

    private void notifyConnection(boolean connected, String message) {
        if (!closed) {
            uiThread.execute(() -> handler.onConnection(connected, message));
        }
    }

    private void reconnectLater() {
        if (!closed) {
            try {
                retry.schedule(this::connect, 3, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // executor already shut down
            }
        }
    }
}
