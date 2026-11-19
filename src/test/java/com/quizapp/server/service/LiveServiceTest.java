package com.quizapp.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quizapp.dto.LiveState;
import com.quizapp.dto.RegisterRequest;
import com.quizapp.dto.StartAttemptResponse;
import com.quizapp.exception.NotFoundException;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LiveServiceTest {

    @TempDir
    Path tempDir;

    private final AttemptServiceTest.TestClock clock = new AttemptServiceTest.TestClock();
    private final List<Integer> notifications = new ArrayList<>();
    private AttemptService attempts;
    private LiveService live;
    private QuizService quizzes;
    private User ravi;
    private User anu;
    private int quizId;
    private int questionId;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager db = new DatabaseManager(tempDir.resolve("test.db").toString());
        db.createTables();
        AuthService auth = new AuthService(new UserDAO(db), new SessionStore());
        auth.ensureAdminExists("admin", "admin123");
        ravi = auth.userForToken(auth.register(new RegisterRequest("Ravi", "ravi", "secret123")).token());
        anu = auth.userForToken(auth.register(new RegisterRequest("Anu", "anu", "secret123")).token());

        QuizDAO quizDAO = new QuizDAO(db);
        QuestionDAO questionDAO = new QuestionDAO(db);
        AttemptDAO attemptDAO = new AttemptDAO(db);
        quizzes = new QuizService(quizDAO, questionDAO);
        attempts = new AttemptService(quizDAO, questionDAO, attemptDAO, clock);
        attempts.setChangeListener(notifications::add);
        live = new LiveService(quizDAO, attemptDAO, clock);

        quizId = quizzes.createQuiz(new Quiz("Live quiz", "Java", "", 5), 1).getId();
        questionId = quizzes.addQuestion(quizId, new Question("Q", "a", "b", "c", "d", "A")).getId();
        quizzes.setPublished(quizId, true);
    }

    @Test
    void showsWritersWithProgressThenLeaderboard() throws Exception {
        StartAttemptResponse r = attempts.start(ravi, quizId);
        attempts.saveProgress(ravi, r.attemptId(), Map.of(questionId, "A"));
        attempts.start(anu, quizId);

        LiveState state = live.buildState(quizId);
        assertEquals(2, state.writing().size());
        assertEquals(1, state.writing().get(0).answered());
        assertTrue(state.leaderboard().isEmpty());

        attempts.submit(ravi, r.attemptId(), Map.of(), false);
        state = live.buildState(quizId);
        assertEquals(1, state.writing().size());
        assertEquals("Ravi", state.leaderboard().get(0).name());
        assertEquals(1, state.leaderboard().get(0).score());
        assertEquals(1, state.submittedCount());
    }

    @Test
    void leaderboardKeepsOnlyBestAttemptPerStudent() throws Exception {
        int first = attempts.start(ravi, quizId).attemptId();
        attempts.submit(ravi, first, Map.of(questionId, "B"), false); // 0 points
        int second = attempts.start(ravi, quizId).attemptId();
        attempts.submit(ravi, second, Map.of(questionId, "A"), false); // 1 point

        LiveState state = live.buildState(quizId);
        assertEquals(1, state.leaderboard().size());
        assertEquals(1, state.leaderboard().get(0).score());
        assertEquals(2, state.submittedCount());
        assertEquals(2, live.results(quizId).size());
    }

    @Test
    void writersWhoseTimeRanOutAreNotShown() throws Exception {
        attempts.start(ravi, quizId);
        clock.advance(Duration.ofMinutes(10));
        assertTrue(live.buildState(quizId).writing().isEmpty());
    }

    @Test
    void everyChangeNotifiesListener() throws Exception {
        StartAttemptResponse r = attempts.start(ravi, quizId);
        attempts.saveProgress(ravi, r.attemptId(), Map.of(questionId, "A"));
        attempts.submit(ravi, r.attemptId(), Map.of(), false);
        attempts.submit(ravi, r.attemptId(), Map.of(), false); // repeat: no new notification
        assertEquals(List.of(quizId, quizId, quizId), notifications);
    }

    @Test
    void unpublishedQuizIsNotLive() throws Exception {
        quizzes.setPublished(quizId, false);
        assertThrows(NotFoundException.class, () -> live.buildState(quizId));
    }
}
