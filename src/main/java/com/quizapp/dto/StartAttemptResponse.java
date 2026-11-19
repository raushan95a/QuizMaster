package com.quizapp.dto;

import java.util.List;
import java.util.Map;

/**
 * Sent when a student starts (or resumes) a quiz.
 * savedAnswers holds answers already saved for a resumed attempt (question id -> "A".."D").
 */
public record StartAttemptResponse(int attemptId, String quizTitle, int timeLimitSec, int secondsLeft,
                                   List<QuestionView> questions, Map<Integer, String> savedAnswers) {
}
