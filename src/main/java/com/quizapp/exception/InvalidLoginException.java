package com.quizapp.exception;

/** Wrong username or password. */
public class InvalidLoginException extends QuizAppException {

    public InvalidLoginException() {
        super("Invalid username or password");
    }

    @Override
    public int getHttpStatus() {
        return 401;
    }
}
