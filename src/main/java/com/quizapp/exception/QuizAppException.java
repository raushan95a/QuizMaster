package com.quizapp.exception;

/**
 * Base class of all application exceptions (user-defined, checked).
 * Each subclass overrides getHttpStatus(), so the server can turn any of them into the right
 * HTTP response just by calling this method (dynamic method dispatch).
 */
public class QuizAppException extends Exception {

    public QuizAppException(String message) {
        super(message);
    }

    public QuizAppException(String message, Throwable cause) {
        super(message, cause);
    }

    /** HTTP status code the server sends for this error. */
    public int getHttpStatus() {
        return 500;
    }
}
