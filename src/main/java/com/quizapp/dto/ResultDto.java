package com.quizapp.dto;

import com.quizapp.model.Attempt;
import com.quizapp.model.Gradable;

/** The result of one submitted attempt (also used for history rows). */
public record ResultDto(int attemptId, int quizId, String quizTitle, int score, int total, double percentage,
                        int correct, int wrong, int unanswered, int timeTakenSec, boolean autoSubmitted,
                        String submittedAt) implements Gradable {

    public static ResultDto from(Attempt a) {
        return new ResultDto(a.getId(), a.getQuizId(), a.getQuizTitle(), a.getScore(), a.getTotal(),
                a.getPercentage(), a.getCorrectCount(), a.getWrongCount(), a.getUnansweredCount(),
                a.getTimeTakenSec(), a.isAutoSubmitted(), a.getSubmittedAt());
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public int getTotal() {
        return total;
    }
}
