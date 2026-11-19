package com.quizapp.server.service;

import com.quizapp.dto.LiveState;
import com.quizapp.dto.ResultDto;
import com.quizapp.exception.NotFoundException;
import com.quizapp.exception.QuizAppException;
import com.quizapp.model.Attempt;
import com.quizapp.model.Quiz;
import com.quizapp.server.dao.AttemptDAO;
import com.quizapp.server.dao.QuizDAO;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Builds the live view of a quiz (who is writing, leaderboard) and the admin results list. */
public class LiveService {

    private final QuizDAO quizDAO;
    private final AttemptDAO attemptDAO;
    private final Clock clock;

    public LiveService(QuizDAO quizDAO, AttemptDAO attemptDAO, Clock clock) {
        this.quizDAO = quizDAO;
        this.attemptDAO = attemptDAO;
        this.clock = clock;
    }

    /** Published quizzes the live page can show. */
    public List<Quiz> liveQuizzes() throws QuizAppException {
        return quizDAO.findPublished();
    }

    public LiveState buildState(int quizId) throws QuizAppException {
        Quiz quiz = quizDAO.findById(quizId)
                .filter(Quiz::isPublished)
                .orElseThrow(() -> new NotFoundException("Quiz"));

        // Students writing now (attempts whose time is not over yet)
        List<LiveState.Writing> writing = new ArrayList<>();
        Instant now = clock.instant();
        for (Attempt a : attemptDAO.findInProgressByQuiz(quizId)) {
            long elapsed = Duration.between(Instant.parse(a.getStartedAt()), now).getSeconds();
            if (elapsed <= quiz.getTimeLimitMin() * 60L + AttemptService.GRACE_SECONDS) {
                writing.add(new LiveState.Writing(a.getStudentName(), a.getAnsweredCount()));
            }
        }

        // Leaderboard: attempts come sorted best-first, so the first one per student is their best
        List<Attempt> submitted = attemptDAO.findSubmittedByQuiz(quizId);
        List<LiveState.LeaderboardEntry> leaderboard = new ArrayList<>();
        Set<Integer> seenStudents = new HashSet<>();
        for (Attempt a : submitted) {
            if (seenStudents.add(a.getUserId())) {
                leaderboard.add(new LiveState.LeaderboardEntry(leaderboard.size() + 1, a.getStudentName(),
                        a.getScore(), a.getTotal(), a.getTimeTakenSec()));
            }
        }

        return new LiveState(quiz.getId(), quiz.getTitle(), quiz.getQuestionCount(), submitted.size(),
                writing, leaderboard, now.toString());
    }

    /** Every submitted attempt of a quiz (admin results and CSV export). */
    public List<ResultDto> results(int quizId) throws QuizAppException {
        quizDAO.findById(quizId).orElseThrow(() -> new NotFoundException("Quiz"));
        List<ResultDto> rows = new ArrayList<>();
        for (Attempt a : attemptDAO.findSubmittedByQuiz(quizId)) {
            rows.add(ResultDto.from(a));
        }
        return rows;
    }
}
