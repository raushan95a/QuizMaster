package com.quizapp.server.web;

import com.quizapp.dto.ErrorResponse;
import com.quizapp.exception.QuizAppException;
import com.quizapp.server.service.LiveService;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Real-time updates over WebSocket (/ws/live?quiz=ID).
 * Keeps the open connections per quiz; after every start / auto-save / submit, the new
 * live state is pushed to everyone watching that quiz.
 */
public class LiveHub {

    private static final Logger LOG = LoggerFactory.getLogger(LiveHub.class);

    private final LiveService liveService;
    /** quiz id -> open connections. Thread-safe map and sets: many threads use them at once. */
    private final Map<Integer, Set<WsContext>> watchers = new ConcurrentHashMap<>();
    /** One background thread sends all messages, so sends never overlap and requests never wait. */
    private final ExecutorService sender = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "live-sender");
        t.setDaemon(true);
        return t;
    });

    public LiveHub(LiveService liveService) {
        this.liveService = liveService;
    }

    /** WebSocket handlers, registered in ServerMain with app.ws("/ws/live", hub::configure). */
    public void configure(WsConfig ws) {
        ws.onConnect(ctx -> {
            int quizId = quizIdOf(ctx);
            if (quizId <= 0) {
                ctx.send(new ErrorResponse("Add ?quiz=<id> to the address"));
                ctx.closeSession();
                return;
            }
            ctx.enableAutomaticPings(25, TimeUnit.SECONDS); // keeps idle connections open
            watchers.computeIfAbsent(quizId, id -> ConcurrentHashMap.newKeySet()).add(ctx);
            sender.submit(() -> sendState(quizId, Set.of(ctx)));
        });
        ws.onClose(ctx -> forget(ctx));
        ws.onError(ctx -> forget(ctx));
    }

    /** Called after something changed for this quiz. Returns immediately; sending happens in the background. */
    public void publish(int quizId) {
        Set<WsContext> set = watchers.get(quizId);
        if (set == null || set.isEmpty()) {
            return; // nobody is watching
        }
        sender.submit(() -> sendState(quizId, set));
    }

    private void sendState(int quizId, Set<WsContext> targets) {
        Object message;
        try {
            message = liveService.buildState(quizId);
        } catch (QuizAppException e) {
            message = new ErrorResponse(e.getMessage());
        }
        for (WsContext ctx : targets) {
            try {
                if (ctx.session.isOpen()) {
                    ctx.send(message);
                } else {
                    forget(ctx);
                }
            } catch (Exception e) {
                LOG.debug("Live send failed, dropping connection", e);
                forget(ctx);
            }
        }
    }

    private void forget(WsContext ctx) {
        for (Set<WsContext> set : watchers.values()) {
            set.remove(ctx);
        }
    }

    private static int quizIdOf(WsContext ctx) {
        try {
            return Integer.parseInt(ctx.queryParam("quiz"));
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }
}
