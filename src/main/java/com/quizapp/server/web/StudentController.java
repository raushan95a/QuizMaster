package com.quizapp.server.web;

import com.quizapp.dto.AnswersRequest;
import com.quizapp.server.service.AttemptService;
import io.javalin.http.Context;
import java.util.Map;

/** HTTP endpoints for taking quizzes (any logged-in user). */
public class StudentController {

    private final AttemptService attempts;

    public StudentController(AttemptService attempts) {
        this.attempts = attempts;
    }

    public void availableQuizzes(Context ctx) throws Exception {
        ctx.json(attempts.availableQuizzes());
    }

    public void start(Context ctx) throws Exception {
        ctx.json(attempts.start(Auth.currentUser(ctx), RequestUtil.intParam(ctx, "id")));
    }

    public void progress(Context ctx) throws Exception {
        AnswersRequest body = RequestUtil.body(ctx, AnswersRequest.class);
        int saved = attempts.saveProgress(Auth.currentUser(ctx), RequestUtil.intParam(ctx, "id"), body.answers());
        ctx.json(Map.of("saved", saved));
    }

    public void submit(Context ctx) throws Exception {
        AnswersRequest body = RequestUtil.body(ctx, AnswersRequest.class);
        ctx.json(attempts.submit(Auth.currentUser(ctx), RequestUtil.intParam(ctx, "id"),
                body.answers(), body.autoSubmitted()));
    }

    public void review(Context ctx) throws Exception {
        ctx.json(attempts.review(Auth.currentUser(ctx), RequestUtil.intParam(ctx, "id")));
    }

    public void history(Context ctx) throws Exception {
        ctx.json(attempts.history(Auth.currentUser(ctx)));
    }
}
