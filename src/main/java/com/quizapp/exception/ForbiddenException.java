package com.quizapp.exception;

/** Logged in, but not allowed to do this (e.g. a student calling an admin endpoint). */
public class ForbiddenException extends QuizAppException {

    public ForbiddenException() {
        super("You are not allowed to do this");
    }

    @Override
    public int getHttpStatus() {
        return 403;
    }
}
