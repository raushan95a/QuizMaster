package com.quizapp.server.service;

import com.quizapp.exception.NotFoundException;
import com.quizapp.exception.QuizAppException;
import com.quizapp.exception.ValidationException;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.server.dao.QuestionDAO;
import com.quizapp.server.dao.QuizDAO;
import java.util.List;

/** Rules for creating and changing quizzes and questions (admin side). */
public class QuizService {

    private final QuizDAO quizDAO;
    private final QuestionDAO questionDAO;

    public QuizService(QuizDAO quizDAO, QuestionDAO questionDAO) {
        this.quizDAO = quizDAO;
        this.questionDAO = questionDAO;
    }

    // ---------- quizzes ----------

    public List<Quiz> listQuizzes() throws QuizAppException {
        return quizDAO.findAll();
    }

    /** One quiz together with all its questions. */
    public Quiz getQuiz(int quizId) throws QuizAppException {
        Quiz quiz = quizDAO.findById(quizId).orElseThrow(() -> new NotFoundException("Quiz"));
        quiz.setQuestions(questionDAO.findByQuiz(quizId));
        return quiz;
    }

    public Quiz createQuiz(Quiz input, int adminId) throws QuizAppException {
        Quiz quiz = validQuiz(input);
        quiz.setCreatedBy(adminId);
        quiz.setPublished(false); // new quizzes start as drafts
        quizDAO.add(quiz);
        return getQuiz(quiz.getId());
    }

    public Quiz updateQuiz(int quizId, Quiz input) throws QuizAppException {
        getQuiz(quizId); // throws NotFoundException if missing
        Quiz quiz = validQuiz(input);
        quiz.setId(quizId);
        quizDAO.update(quiz);
        return getQuiz(quizId);
    }

    public void deleteQuiz(int quizId) throws QuizAppException {
        getQuiz(quizId);
        quizDAO.delete(quizId);
    }

    /** Publishing needs at least one question; unpublishing always works. */
    public Quiz setPublished(int quizId, boolean publish) throws QuizAppException {
        Quiz quiz = getQuiz(quizId);
        if (publish && quiz.getQuestions().isEmpty()) {
            throw new ValidationException("Add at least one question before publishing");
        }
        quizDAO.setPublished(quizId, publish);
        return getQuiz(quizId);
    }

    // ---------- questions ----------

    public Question addQuestion(int quizId, Question input) throws QuizAppException {
        getQuiz(quizId);
        Question question = validQuestion(input);
        question.setQuizId(quizId);
        questionDAO.add(question);
        return question;
    }

    public Question updateQuestion(int questionId, Question input) throws QuizAppException {
        Question existing = questionDAO.findById(questionId).orElseThrow(() -> new NotFoundException("Question"));
        Question question = validQuestion(input);
        question.setId(questionId);
        question.setQuizId(existing.getQuizId());
        questionDAO.update(question);
        return question;
    }

    public void deleteQuestion(int questionId) throws QuizAppException {
        Question existing = questionDAO.findById(questionId).orElseThrow(() -> new NotFoundException("Question"));
        Quiz quiz = getQuiz(existing.getQuizId());
        if (quiz.isPublished() && quiz.getQuestions().size() == 1) {
            throw new ValidationException("A published quiz needs at least one question. Unpublish it first.");
        }
        questionDAO.delete(questionId);
    }

    /** Adds a ready-made sample quiz on a fresh dev database, so the demo works immediately. */
    public void addSampleQuizIfEmpty(int adminId) throws QuizAppException {
        if (quizDAO.count() > 0) {
            return;
        }
        Quiz quiz = createQuiz(new Quiz("Java Basics", "Advanced Java",
                "A short warm-up quiz on core Java concepts.", 5), adminId);
        int id = quiz.getId();
        addQuestion(id, new Question("Which keyword prevents a method from being overridden?",
                "static", "final", "private", "abstract", "B"));
        addQuestion(id, new Question("Which class is the parent of every class in Java?",
                "Object", "Class", "System", "Main", "A"));
        addQuestion(id, new Question("Which collection does NOT allow duplicate elements?",
                "ArrayList", "LinkedList", "HashSet", "Vector", "C"));
        addQuestion(id, new Question("Which method starts a new thread?",
                "run()", "begin()", "execute()", "start()", "D"));
        addQuestion(id, new Question("Which JDBC object is used to run a parameterised SQL query?",
                "PreparedStatement", "Connection", "ResultSet", "DriverManager", "A"));
        setPublished(id, true);
    }

    // ---------- validation ----------

    private Quiz validQuiz(Quiz in) throws ValidationException {
        if (in == null) {
            throw new ValidationException("Quiz details are missing");
        }
        String title = required(in.getTitle(), "Title", 100);
        String subject = required(in.getSubject(), "Subject", 50);
        String description = in.getDescription() == null ? "" : in.getDescription().trim();
        if (description.length() > 500) {
            throw new ValidationException("Description must be at most 500 characters");
        }
        if (in.getTimeLimitMin() < 1 || in.getTimeLimitMin() > 180) {
            throw new ValidationException("Time limit must be between 1 and 180 minutes");
        }
        return new Quiz(title, subject, description, in.getTimeLimitMin());
    }

    private Question validQuestion(Question in) throws ValidationException {
        if (in == null) {
            throw new ValidationException("Question details are missing");
        }
        String correct = in.getCorrectOption() == null ? "" : in.getCorrectOption().trim().toUpperCase();
        if (!correct.matches("[ABCD]")) {
            throw new ValidationException("Choose the correct option (A, B, C or D)");
        }
        return new Question(
                required(in.getText(), "Question text", 500),
                required(in.getOptionA(), "Option A", 200),
                required(in.getOptionB(), "Option B", 200),
                required(in.getOptionC(), "Option C", 200),
                required(in.getOptionD(), "Option D", 200),
                correct);
    }

    private static String required(String value, String field, int maxLength) throws ValidationException {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) {
            throw new ValidationException(field + " is required");
        }
        if (v.length() > maxLength) {
            throw new ValidationException(field + " must be at most " + maxLength + " characters");
        }
        return v;
    }
}
