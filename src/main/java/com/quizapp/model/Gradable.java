package com.quizapp.model;

/** Anything that has a score out of a total (used for attempts and results). */
public interface Gradable {

    int getScore();

    int getTotal();

    /** Default method: every Gradable gets a percentage for free. */
    default double getPercentage() {
        return getTotal() == 0 ? 0 : Math.round(getScore() * 1000.0 / getTotal()) / 10.0;
    }
}
