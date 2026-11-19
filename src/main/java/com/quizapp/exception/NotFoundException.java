package com.quizapp.exception;

/** The requested quiz / question / attempt does not exist. */
public class NotFoundException extends QuizAppException {

    public NotFoundException(String what) {
        super(what + " not found");
    }

    @Override
    public int getHttpStatus() {
        return 404;
    }
}
