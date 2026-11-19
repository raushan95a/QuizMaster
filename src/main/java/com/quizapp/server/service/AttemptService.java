package com.quizapp.server.service;

import com.quizapp.dto.QuestionView;
import com.quizapp.dto.ResultDto;
import com.quizapp.dto.ReviewItem;
import com.quizapp.dto.StartAttemptResponse;
import com.quizapp.exception.ForbiddenException;
import com.quizapp.exception.NotFoundException;
import com.quizapp.exception.QuizAppException;
import com.quizapp.exception.ValidationException;
import com.quizapp.model.Attempt;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.Role;
import com.quizapp.model.User;
import com.quizapp.server.dao.AttemptDAO;
import com.quizapp.server.dao.QuestionDAO;
import com.quizapp.server.dao.QuizDAO;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Taking a quiz: start, auto-save, submit + grading, review, history. */
public class AttemptService {

    /** Extra seconds accepted after the time limit (network delay). */
    public static final int GRACE_SECONDS = 30;

    private final QuizDAO quizDAO;
    private final QuestionDAO questionDAO;
    private final AttemptDAO attemptDAO;
    private final Clock clock;

    public AttemptService(QuizDAO quizDAO, QuestionDAO questionDAO, AttemptDAO attemptDAO, Clock clock) {
        this.quizDAO = quizDAO;
        this.questionDAO = questionDAO;
        this.attemptDAO = attemptDAO;
        this.clock = clock;
    }

    /** Published quizzes for the student dashboard (questions are not included). */
    public List<Quiz> availableQuizzes() throws QuizAppException {
        return quizDAO.findPublished();
    }

    /** Starts a quiz, or resumes the student's unfinished attempt if time is left. */
    public StartAttemptResponse start(User user, int quizId) throws QuizAppException {
        Quiz quiz = quizDAO.findById(quizId)
                .filter(Quiz::isPublished)
                .orElseThrow(() -> new NotFoundException("Quiz"));
        List<Question> questions = questionDAO.findByQuiz(quizId);
        if (questions.isEmpty()) {
            throw new ValidationException("This quiz has no questions yet");
        }

        Attempt attempt = attemptDAO.findInProgress(user.getId(), quizId).orElse(null);
        if (attempt != null && secondsLeft(attempt, quiz) <= 0) {
            finish(attempt, quiz, questions, Map.of(), true); // time ran out earlier: grade what was saved
            attempt = null;
        }
        if (attempt == null) {
            attempt = new Attempt(user.getId(), quizId, now().toString());
            attemptDAO.add(attempt);
        }

        // Shuffle with the attempt id as seed: different order per attempt, same order when resuming
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled, new Random(attempt.getId()));
        List<QuestionView> views = new ArrayList<>();
        for (Question q : shuffled) {
            views.add(QuestionView.from(q)); // no correct answers leave the server
        }

        return new StartAttemptResponse(attempt.getId(), quiz.getTitle(), quiz.getTimeLimitMin() * 60,
                (int) Math.max(0, secondsLeft(attempt, quiz)), views, attemptDAO.findAnswers(attempt.getId()));
    }

    /** Auto-save while writing. Returns how many answers were saved. */
    public int saveProgress(User user, int attemptId, Map<Integer, String> answers) throws QuizAppException {
        Attempt attempt = ownAttempt(user, attemptId);
        if (attempt.isSubmitted()) {
            throw new ValidationException("This quiz was already submitted");
        }
        Quiz quiz = quizDAO.findById(attempt.getQuizId()).orElseThrow(() -> new NotFoundException("Quiz"));
        if (secondsLeft(attempt, quiz) < -GRACE_SECONDS) {
            throw new ValidationException("Time is over");
        }
        Map<Integer, String> clean = cleanAnswers(answers, questionDAO.findByQuiz(quiz.getId()));
        attemptDAO.saveAnswers(attemptId, clean);
        return clean.size();
    }

    /** Submits and grades. Calling it again returns the same result (safe to retry). */
    public ResultDto submit(User user, int attemptId, Map<Integer, String> answers, boolean autoSubmitted)
            throws QuizAppException {
        Attempt attempt = ownAttempt(user, attemptId);
        if (attempt.isSubmitted()) {
            return ResultDto.from(attempt);
        }
        Quiz quiz = quizDAO.findById(attempt.getQuizId()).orElseThrow(() -> new NotFoundException("Quiz"));
        List<Question> questions = questionDAO.findByQuiz(quiz.getId());

        boolean late = secondsLeft(attempt, quiz) < -GRACE_SECONDS;
        // Answers sent after the deadline are ignored; only what was auto-saved in time counts
        Map<Integer, String> finalAnswers = late ? Map.of() : cleanAnswers(answers, questions);
        return finish(attempt, quiz, questions, finalAnswers, autoSubmitted || late);
    }

    /** Correct answers, only for a submitted attempt of this student (or any attempt for an admin). */
    public List<ReviewItem> review(User user, int attemptId) throws QuizAppException {
        Attempt attempt = ownAttempt(user, attemptId);
        if (!attempt.isSubmitted()) {
            throw new ValidationException("Answers are shown after the quiz is submitted");
        }
        Map<Integer, String> chosen = attemptDAO.findAnswers(attemptId);
        List<ReviewItem> items = new ArrayList<>();
        for (Question q : questionDAO.findByQuiz(attempt.getQuizId())) {
            items.add(new ReviewItem(q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                    chosen.get(q.getId()), q.getCorrectOption()));
        }
        return items;
    }

    public List<ResultDto> history(User user) throws QuizAppException {
        List<ResultDto> rows = new ArrayList<>();
        for (Attempt a : attemptDAO.findSubmittedByUser(user.getId())) {
            rows.add(ResultDto.from(a));
        }
        return rows;
    }

    // ---------- grading ----------

    private ResultDto finish(Attempt attempt, Quiz quiz, List<Question> questions,
                             Map<Integer, String> newAnswers, boolean autoSubmitted) throws QuizAppException {
        // final answers = what was auto-saved, overwritten by what was sent now
        Map<Integer, String> answers = new HashMap<>(attemptDAO.findAnswers(attempt.getId()));
        answers.putAll(newAnswers);

        int correct = 0;
        int wrong = 0;
        for (Question q : questions) {
            String chosen = answers.get(q.getId());
            if (chosen == null) {
                continue;               // unanswered
            } else if (q.isCorrect(chosen)) {
                correct++;
            } else {
                wrong++;
            }
        }

        Instant end = now();
        long taken = Duration.between(Instant.parse(attempt.getStartedAt()), end).getSeconds();
        attempt.setScore(correct);
        attempt.setTotal(questions.size());
        attempt.setCorrectCount(correct);
        attempt.setWrongCount(wrong);
        attempt.setUnansweredCount(questions.size() - correct - wrong);
        attempt.setTimeTakenSec((int) Math.min(Math.max(taken, 0), quiz.getTimeLimitMin() * 60L));
        attempt.setAutoSubmitted(autoSubmitted);
        attempt.setSubmittedAt(end.toString());

        attemptDAO.submit(attempt, newAnswers);
        // Reload: if another request submitted first, this returns that stored result instead
        return ResultDto.from(attemptDAO.findById(attempt.getId()).orElseThrow(() -> new NotFoundException("Attempt")));
    }

    // ---------- helpers ----------

    private Attempt ownAttempt(User user, int attemptId) throws QuizAppException {
        Attempt attempt = attemptDAO.findById(attemptId).orElseThrow(() -> new NotFoundException("Attempt"));
        if (attempt.getUserId() != user.getId() && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException();
        }
        return attempt;
    }

    private long secondsLeft(Attempt attempt, Quiz quiz) {
        long elapsed = Duration.between(Instant.parse(attempt.getStartedAt()), now()).getSeconds();
        return quiz.getTimeLimitMin() * 60L - elapsed;
    }

    /** Keeps only answers for questions of this quiz with a valid letter (A-D). */
    private Map<Integer, String> cleanAnswers(Map<Integer, String> answers, List<Question> questions) {
        Map<Integer, String> clean = new HashMap<>();
        if (answers == null) {
            return clean;
        }
        List<Integer> validIds = new ArrayList<>();
        for (Question q : questions) {
            validIds.add(q.getId());
        }
        Iterator<Map.Entry<Integer, String>> it = answers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, String> e = it.next();
            String value = e.getValue() == null ? null : e.getValue().trim().toUpperCase();
            if (e.getKey() != null && validIds.contains(e.getKey()) && (value == null || value.matches("[ABCD]"))) {
                clean.put(e.getKey(), value);
            }
        }
        return clean;
    }

    private Instant now() {
        return clock.instant();
    }
}
