package com.quizapp.server.web;

import com.quizapp.model.Quiz;
import com.quizapp.server.service.LiveService;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Public read-only endpoints for the live page, plus the admin results list. */
public class LiveController {

    private final LiveService liveService;

    public LiveController(LiveService liveService) {
        this.liveService = liveService;
    }

    /** GET /api/live — published quizzes (id + title only). */
    public void quizzes(Context ctx) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Quiz q : liveService.liveQuizzes()) {
            list.add(Map.of("id", q.getId(), "title", q.getTitle()));
        }
        ctx.json(list);
    }

    /** GET /api/live/{id} — same data the WebSocket sends. */
    public void state(Context ctx) throws Exception {
        ctx.json(liveService.buildState(RequestUtil.intParam(ctx, "id")));
    }

    /** GET /api/admin/quizzes/{id}/results — every submitted attempt (admin only). */
    public void results(Context ctx) throws Exception {
        ctx.json(liveService.results(RequestUtil.intParam(ctx, "id")));
    }
}
