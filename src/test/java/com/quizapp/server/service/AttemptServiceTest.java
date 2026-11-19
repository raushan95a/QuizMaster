package com.quizapp.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quizapp.dto.QuestionView;
import com.quizapp.dto.RegisterRequest;
import com.quizapp.dto.ResultDto;
import com.quizapp.dto.ReviewItem;
import com.quizapp.dto.StartAttemptResponse;
import com.quizapp.exception.ForbiddenException;
import com.quizapp.exception.NotFoundException;
import com.quizapp.exception.ValidationException;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.User;
import com.quizapp.server.DatabaseManager;
import com.quizapp.server.SessionStore;
import com.quizapp.server.dao.AttemptDAO;
import com.quizapp.server.dao.QuestionDAO;
import com.quizapp.server.dao.QuizDAO;
import com.quizapp.server.dao.UserDAO;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttemptServiceTest {

    /** A clock the test can move forward, so time limits can be tested instantly. */
    static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T10:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    @TempDir
    Path tempDir;

    private final TestClock clock = new TestClock();
    private AttemptService attempts;
    private QuizService quizzes;
    private User ravi;
    private User anu;
    private int quizId;
    private Map<Integer, String> correctAnswers; // question id -> correct letter

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager db = new DatabaseManager(tempDir.resolve("test.db").toString());
        db.createTables();
        UserDAO users = new UserDAO(db);
        AuthService auth = new AuthService(users, new SessionStore());
        auth.ensureAdminExists("admin", "admin123");
        ravi = auth.userForToken(auth.register(new RegisterRequest("Ravi", "ravi", "secret123")).token());
        anu = auth.userForToken(auth.register(new RegisterRequest("Anu", "anu", "secret123")).token());

        QuizDAO quizDAO = new QuizDAO(db);
        QuestionDAO questionDAO = new QuestionDAO(db);
        quizzes = new QuizService(quizDAO, questionDAO);
        attempts = new AttemptService(quizDAO, questionDAO, new AttemptDAO(db), clock);

        Quiz quiz = quizzes.createQuiz(new Quiz("Unit 1", "Java", "", 2), 1); // 2 minutes
        quizId = quiz.getId();
        correctAnswers = new HashMap<>();
        String[] answers = {"A", "B", "C", "D"};
        for (String a : answers) {
            Question q = quizzes.addQuestion(quizId, new Question("Q" + a, "a", "b", "c", "d", a));
            correctAnswers.put(q.getId(), a);
        }
        quizzes.setPublished(quizId, true);
    }

    @Test
    void startGivesAllQuestionsWithTimer() throws Exception {
        StartAttemptResponse start = attempts.start(ravi, quizId);
        assertEquals(4, start.questions().size());
        assertEquals(120, start.timeLimitSec());
        assertEquals(120, start.secondsLeft());
        assertTrue(start.savedAnswers().isEmpty());
    }

    @Test
    void unpublishedQuizCannotBeStarted() throws Exception {
        quizzes.setPublished(quizId, false);
        assertThrows(NotFoundException.class, () -> attempts.start(ravi, quizId));
    }

    @Test
    void gradingCountsCorrectWrongAndUnanswered() throws Exception {
        StartAttemptResponse start = attempts.start(ravi, quizId);
        List<QuestionView> qs = start.questions();
        Map<Integer, String> answers = new HashMap<>();
        answers.put(qs.get(0).id(), correctAnswers.get(qs.get(0).id()));   // right
        answers.put(qs.get(1).id(), correctAnswers.get(qs.get(1).id()));   // right
        answers.put(qs.get(2).id(), wrongFor(qs.get(2).id()));             // wrong
        // question 4 unanswered
        clock.advance(Duration.ofSeconds(45));

        ResultDto result = attempts.submit(ravi, start.attemptId(), answers, false);
        assertEquals(2, result.score());
        assertEquals(4, result.total());
        assertEquals(1, result.wrong());
        assertEquals(1, result.unanswered());
        assertEquals(50.0, result.percentage());
        assertEquals(45, result.timeTakenSec());
        assertFalse(result.autoSubmitted());
    }

    @Test
    void submittingTwiceReturnsSameResult() throws Exception {
        int id = attempts.start(ravi, quizId).attemptId();
        ResultDto first = attempts.submit(ravi, id, correctAnswers, false);
        ResultDto second = attempts.submit(ravi, id, Map.of(), true); // e.g. timer fired at the same moment
        assertEquals(4, first.score());
        assertEquals(first.score(), second.score());
        assertFalse(second.autoSubmitted());
    }

    @Test
    void resumeKeepsAttemptOrderAndSavedAnswers() throws Exception {
        StartAttemptResponse first = attempts.start(ravi, quizId);
        int qid = first.questions().get(0).id();
        attempts.saveProgress(ravi, first.attemptId(), Map.of(qid, "c"));
        clock.advance(Duration.ofSeconds(30));

        StartAttemptResponse again = attempts.start(ravi, quizId);
        assertEquals(first.attemptId(), again.attemptId());
        assertEquals(first.questions(), again.questions());
        assertEquals("C", again.savedAnswers().get(qid));
        assertEquals(90, again.secondsLeft());
    }

    @Test
    void lateSubmitIgnoresNewAnswersButKeepsSavedOnes() throws Exception {
        StartAttemptResponse start = attempts.start(ravi, quizId);
        int savedQ = start.questions().get(0).id();
        attempts.saveProgress(ravi, start.attemptId(), Map.of(savedQ, correctAnswers.get(savedQ)));

        clock.advance(Duration.ofSeconds(120 + AttemptService.GRACE_SECONDS + 1));
        assertThrows(ValidationException.class,
                () -> attempts.saveProgress(ravi, start.attemptId(), correctAnswers));

        ResultDto result = attempts.submit(ravi, start.attemptId(), correctAnswers, false);
        assertEquals(1, result.score()); // only the answer saved in time
        assertTrue(result.autoSubmitted());
        assertEquals(120, result.timeTakenSec());
    }

    @Test
    void expiredUnfinishedAttemptIsGradedWhenStartingAgain() throws Exception {
        int oldId = attempts.start(ravi, quizId).attemptId();
        clock.advance(Duration.ofMinutes(10));

        int newId = attempts.start(ravi, quizId).attemptId();
        assertNotEquals(oldId, newId);
        assertEquals(1, attempts.history(ravi).size());
        assertTrue(attempts.history(ravi).get(0).autoSubmitted());
    }

    @Test
    void studentsCannotTouchOtherStudentsAttempts() throws Exception {
        int id = attempts.start(ravi, quizId).attemptId();
        assertThrows(ForbiddenException.class, () -> attempts.saveProgress(anu, id, Map.of()));
        assertThrows(ForbiddenException.class, () -> attempts.submit(anu, id, Map.of(), false));
        attempts.submit(ravi, id, Map.of(), false);
        assertThrows(ForbiddenException.class, () -> attempts.review(anu, id));
    }

    @Test
    void reviewOnlyAfterSubmitAndShowsCorrectAnswers() throws Exception {
        StartAttemptResponse start = attempts.start(ravi, quizId);
        assertThrows(ValidationException.class, () -> attempts.review(ravi, start.attemptId()));

        int qid = start.questions().get(0).id();
        attempts.submit(ravi, start.attemptId(), Map.of(qid, wrongFor(qid)), false);
        List<ReviewItem> review = attempts.review(ravi, start.attemptId());
        assertEquals(4, review.size());
        assertEquals(1, review.stream().filter(r -> r.chosen() != null).count());
        assertTrue(review.stream().noneMatch(ReviewItem::isRight));
    }

    @Test
    void invalidAnswersAreIgnored() throws Exception {
        StartAttemptResponse start = attempts.start(ravi, quizId);
        int qid = start.questions().get(0).id();
        Map<Integer, String> answers = new HashMap<>();
        answers.put(qid, "Z");      // invalid letter
        answers.put(99999, "A");    // not a question of this quiz
        assertEquals(0, attempts.saveProgress(ravi, start.attemptId(), answers));
    }

    private String wrongFor(int questionId) {
        return "A".equals(correctAnswers.get(questionId)) ? "B" : "A";
    }
}
