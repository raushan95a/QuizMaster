package com.quizapp.exception;

/** The chosen username is already taken. */
public class DuplicateUserException extends QuizAppException {

    public DuplicateUserException(String username) {
        super("Username '" + username + "' is already taken");
    }

    @Override
    public int getHttpStatus() {
        return 409;
    }
}
