package com.quizapp.exception;

/** The request has no valid login token. */
public class UnauthorizedException extends QuizAppException {

    public UnauthorizedException() {
        super("Please log in again");
    }

    @Override
    public int getHttpStatus() {
        return 401;
    }
}
