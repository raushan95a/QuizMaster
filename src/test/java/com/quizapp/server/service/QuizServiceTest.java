package com.quizapp.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quizapp.exception.NotFoundException;
import com.quizapp.exception.ValidationException;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.server.DatabaseManager;
import com.quizapp.server.SessionStore;
import com.quizapp.server.dao.QuestionDAO;
import com.quizapp.server.dao.QuizDAO;
import com.quizapp.server.dao.UserDAO;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuizServiceTest {

    @TempDir
    Path tempDir;

    private QuizService quizzes;
    private int adminId;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager db = new DatabaseManager(tempDir.resolve("test.db").toString());
        db.createTables();
        UserDAO users = new UserDAO(db);
        new AuthService(users, new SessionStore()).ensureAdminExists("admin", "admin123");
        adminId = users.findByUsername("admin").orElseThrow().getId();
        quizzes = new QuizService(new QuizDAO(db), new QuestionDAO(db));
    }

    private Quiz newQuiz() throws Exception {
        return quizzes.createQuiz(new Quiz("Unit 1", "Java", "Basics", 10), adminId);
    }

    private Question sampleQuestion() {
        return new Question("2 + 2 = ?", "3", "4", "5", "6", "b");
    }

    @Test
    void createdQuizStartsAsUnpublishedDraft() throws Exception {
        Quiz quiz = newQuiz();
        assertTrue(quiz.getId() > 0);
        assertFalse(quiz.isPublished());
        assertEquals(1, quizzes.listQuizzes().size());
    }

    @Test
    void invalidQuizIsRejected() {
        assertThrows(ValidationException.class,
                () -> quizzes.createQuiz(new Quiz("", "Java", null, 10), adminId));
        assertThrows(ValidationException.class,
                () -> quizzes.createQuiz(new Quiz("T", "Java", null, 0), adminId));
        assertThrows(ValidationException.class,
                () -> quizzes.createQuiz(new Quiz("T", "Java", null, 500), adminId));
    }

    @Test
    void questionIsSavedWithUppercaseAnswer() throws Exception {
        Quiz quiz = newQuiz();
        quizzes.addQuestion(quiz.getId(), sampleQuestion());

        Quiz loaded = quizzes.getQuiz(quiz.getId());
        assertEquals(1, loaded.getQuestions().size());
        assertEquals(1, loaded.getQuestionCount());
        assertEquals("B", loaded.getQuestions().get(0).getCorrectOption());
    }

    @Test
    void questionNeedsAllFieldsAndValidAnswer() throws Exception {
        int id = newQuiz().getId();
        assertThrows(ValidationException.class,
                () -> quizzes.addQuestion(id, new Question("Q", "a", "", "c", "d", "A")));
        assertThrows(ValidationException.class,
                () -> quizzes.addQuestion(id, new Question("Q", "a", "b", "c", "d", "E")));
    }

    @Test
    void cannotPublishEmptyQuiz() throws Exception {
        int id = newQuiz().getId();
        assertThrows(ValidationException.class, () -> quizzes.setPublished(id, true));

        quizzes.addQuestion(id, sampleQuestion());
        assertTrue(quizzes.setPublished(id, true).isPublished());
        assertFalse(quizzes.setPublished(id, false).isPublished());
    }

    @Test
    void cannotDeleteLastQuestionOfPublishedQuiz() throws Exception {
        int id = newQuiz().getId();
        Question q = quizzes.addQuestion(id, sampleQuestion());
        quizzes.setPublished(id, true);

        assertThrows(ValidationException.class, () -> quizzes.deleteQuestion(q.getId()));
        quizzes.setPublished(id, false);
        quizzes.deleteQuestion(q.getId());
        assertTrue(quizzes.getQuiz(id).getQuestions().isEmpty());
    }

    @Test
    void deletingQuizRemovesItsQuestions() throws Exception {
        int id = newQuiz().getId();
        Question q = quizzes.addQuestion(id, sampleQuestion());
        quizzes.deleteQuiz(id);

        assertThrows(NotFoundException.class, () -> quizzes.getQuiz(id));
        assertThrows(NotFoundException.class, () -> quizzes.updateQuestion(q.getId(), sampleQuestion()));
    }

    @Test
    void sampleQuizIsAddedOnlyOnce() throws Exception {
        quizzes.addSampleQuizIfEmpty(adminId);
        quizzes.addSampleQuizIfEmpty(adminId);
        assertEquals(1, quizzes.listQuizzes().size());
        assertTrue(quizzes.listQuizzes().get(0).isPublished());
        assertEquals(5, quizzes.listQuizzes().get(0).getQuestionCount());
    }
}
