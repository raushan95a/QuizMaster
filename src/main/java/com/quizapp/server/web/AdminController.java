package com.quizapp.server.web;

import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.server.service.QuizService;
import io.javalin.http.Context;

/** HTTP endpoints under /api/admin (admins only — checked by a before-handler in ServerMain). */
public class AdminController {

    private final QuizService quizService;

    public AdminController(QuizService quizService) {
        this.quizService = quizService;
    }

    public void listQuizzes(Context ctx) throws Exception {
        ctx.json(quizService.listQuizzes());
    }

    public void getQuiz(Context ctx) throws Exception {
        ctx.json(quizService.getQuiz(RequestUtil.intParam(ctx, "id")));
    }

    public void createQuiz(Context ctx) throws Exception {
        Quiz input = RequestUtil.body(ctx, Quiz.class);
        ctx.status(201).json(quizService.createQuiz(input, Auth.currentUser(ctx).getId()));
    }

    public void updateQuiz(Context ctx) throws Exception {
        ctx.json(quizService.updateQuiz(RequestUtil.intParam(ctx, "id"), RequestUtil.body(ctx, Quiz.class)));
    }

    public void deleteQuiz(Context ctx) throws Exception {
        quizService.deleteQuiz(RequestUtil.intParam(ctx, "id"));
        ctx.status(204);
    }

    public void publish(Context ctx) throws Exception {
        ctx.json(quizService.setPublished(RequestUtil.intParam(ctx, "id"), true));
    }

    public void unpublish(Context ctx) throws Exception {
        ctx.json(quizService.setPublished(RequestUtil.intParam(ctx, "id"), false));
    }

    public void addQuestion(Context ctx) throws Exception {
        Question input = RequestUtil.body(ctx, Question.class);
        ctx.status(201).json(quizService.addQuestion(RequestUtil.intParam(ctx, "id"), input));
    }

    public void updateQuestion(Context ctx) throws Exception {
        Question input = RequestUtil.body(ctx, Question.class);
        ctx.json(quizService.updateQuestion(RequestUtil.intParam(ctx, "qid"), input));
    }

    public void deleteQuestion(Context ctx) throws Exception {
        quizService.deleteQuestion(RequestUtil.intParam(ctx, "qid"));
        ctx.status(204);
    }
}
