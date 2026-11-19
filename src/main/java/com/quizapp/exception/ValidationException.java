package com.quizapp.exception;

/** Input is missing or invalid (e.g. empty username, password too short). */
public class ValidationException extends QuizAppException {

    public ValidationException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatus() {
        return 400;
    }
}
