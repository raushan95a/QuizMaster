package com.quizapp.dto;

import java.util.List;

/**
 * Everything the live page shows for one quiz. Contains only names, counts and scores —
 * never questions or answers, because the live page is public.
 */
public record LiveState(int quizId, String quizTitle, int totalQuestions, int submittedCount,
                        List<Writing> writing, List<LeaderboardEntry> leaderboard, String updatedAt) {

    /** A student who is writing right now. */
    public record Writing(String name, int answered) {
    }

    /** One row of the leaderboard (best attempt per student). */
    public record LeaderboardEntry(int rank, String name, int score, int total, int timeTakenSec) {
    }
}
