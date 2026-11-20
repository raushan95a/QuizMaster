package com.quizapp.server.dao;

import com.quizapp.exception.QuizAppException;
import com.quizapp.model.Attempt;
import com.quizapp.server.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** JDBC access to the attempts and attempt_answers tables. */
public class AttemptDAO implements Repository<Attempt> {

    private static final String SELECT =
            "SELECT a.*, q.title AS quiz_title, u.full_name AS student_name, "
                    + "(SELECT COUNT(*) FROM attempt_answers aa "
                    + " WHERE aa.attempt_id = a.id AND aa.chosen_option IS NOT NULL) AS answered_count "
                    + "FROM attempts a JOIN quizzes q ON q.id = a.quiz_id JOIN users u ON u.id = a.user_id ";

    private final DatabaseManager db;

    public AttemptDAO(DatabaseManager db) {
        this.db = db;
    }

    /** Starts a new attempt (status IN_PROGRESS). */
    @Override
    public int add(Attempt a) throws QuizAppException {
        String sql = "INSERT INTO attempts (user_id, quiz_id, status, started_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getUserId());
            ps.setInt(2, a.getQuizId());
            ps.setString(3, Attempt.IN_PROGRESS);
            ps.setString(4, a.getStartedAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                a.setId(keys.getInt(1));
                return a.getId();
            }
        } catch (SQLException e) {
            throw new QuizAppException("Database error while starting attempt", e);
        }
    }

    @Override
    public Optional<Attempt> findById(int id) throws QuizAppException {
        List<Attempt> list = query(SELECT + "WHERE a.id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Attempt> findAll() throws QuizAppException {
        return query(SELECT + "ORDER BY a.id DESC");
    }

    /** The student's unfinished attempt at this quiz, if any. */
    public Optional<Attempt> findInProgress(int userId, int quizId) throws QuizAppException {
        List<Attempt> list = query(SELECT + "WHERE a.user_id = ? AND a.quiz_id = ? AND a.status = 'IN_PROGRESS' "
                + "ORDER BY a.id DESC", userId, quizId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** A student's submitted attempts, newest first. */
    public List<Attempt> findSubmittedByUser(int userId) throws QuizAppException {
        return query(SELECT + "WHERE a.user_id = ? AND a.status = 'SUBMITTED' "
                + "ORDER BY a.submitted_at DESC, a.id DESC", userId);
    }

    /** Submitted attempts of a quiz, best first (highest score, then fastest). */
    public List<Attempt> findSubmittedByQuiz(int quizId) throws QuizAppException {
        return query(SELECT + "WHERE a.quiz_id = ? AND a.status = 'SUBMITTED' "
                + "ORDER BY a.score DESC, a.time_taken_sec ASC, a.id ASC", quizId);
    }

    /** Attempts of a quiz that are still being written. */
    public List<Attempt> findInProgressByQuiz(int quizId) throws QuizAppException {
        return query(SELECT + "WHERE a.quiz_id = ? AND a.status = 'IN_PROGRESS' ORDER BY a.id", quizId);
    }

    /** Attempts are only changed through submit(); a plain update is not allowed. */
    @Override
    public void update(Attempt a) throws QuizAppException {
        throw new UnsupportedOperationException("Use submit() to finish an attempt");
    }

    @Override
    public void delete(int id) throws QuizAppException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM attempts WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new QuizAppException("Database error while deleting attempt", e);
        }
    }

    /** Saved answers of an attempt: question id -> option letter. */
    public Map<Integer, String> findAnswers(int attemptId) throws QuizAppException {
        Map<Integer, String> answers = new HashMap<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT question_id, chosen_option FROM attempt_answers WHERE attempt_id = ?")) {
            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getString("chosen_option") != null) {
                        answers.put(rs.getInt("question_id"), rs.getString("chosen_option"));
                    }
                }
            }
            return answers;
        } catch (SQLException e) {
            throw new QuizAppException("Database error while loading answers", e);
        }
    }

    /** Auto-save: inserts or replaces answers while the student is writing. */
    public void saveAnswers(int attemptId, Map<Integer, String> answers) throws QuizAppException {
        try (Connection conn = db.getConnection()) {
            upsertAnswers(conn, attemptId, answers);
        } catch (SQLException e) {
            throw new QuizAppException("Database error while saving answers", e);
        }
    }

    /**
     * Saves the final answers and the graded result in ONE transaction.
     * The "WHERE status = 'IN_PROGRESS'" guard makes sure an attempt is graded only once,
     * even if the student's submit and an automatic submit arrive at the same moment.
     *
     * @return true if this call submitted it, false if it was already submitted
     */
    public boolean submit(Attempt graded, Map<Integer, String> answers) throws QuizAppException {
        String sql = "UPDATE attempts SET status = 'SUBMITTED', submitted_at = ?, score = ?, total = ?, "
                + "correct_count = ?, wrong_count = ?, unanswered_count = ?, time_taken_sec = ?, auto_submitted = ? "
                + "WHERE id = ? AND status = 'IN_PROGRESS'";
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false); // start transaction
            try { // nested try: roll back if anything inside fails
                upsertAnswers(conn, graded.getId(), answers);
                int updated;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, graded.getSubmittedAt());
                    ps.setInt(2, graded.getScore());
                    ps.setInt(3, graded.getTotal());
                    ps.setInt(4, graded.getCorrectCount());
                    ps.setInt(5, graded.getWrongCount());
                    ps.setInt(6, graded.getUnansweredCount());
                    ps.setInt(7, graded.getTimeTakenSec());
                    ps.setInt(8, graded.isAutoSubmitted() ? 1 : 0);
                    ps.setInt(9, graded.getId());
                    updated = ps.executeUpdate();
                }
                if (updated == 0) {
                    conn.rollback(); // someone else submitted it first
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new QuizAppException("Database error while submitting attempt", e);
        }
    }

    private void upsertAnswers(Connection conn, int attemptId, Map<Integer, String> answers) throws SQLException {
        String sql = "INSERT INTO attempt_answers (attempt_id, question_id, chosen_option) VALUES (?, ?, ?) "
                + "ON CONFLICT(attempt_id, question_id) DO UPDATE SET chosen_option = excluded.chosen_option";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<Integer, String> entry : answers.entrySet()) {
                ps.setInt(1, attemptId);
                ps.setInt(2, entry.getKey());
                if (entry.getValue() == null) {
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setString(3, entry.getValue());
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Attempt> query(String sql, int... params) throws QuizAppException {
        List<Attempt> attempts = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Attempt a = new Attempt();
                    a.setId(rs.getInt("id"));
                    a.setUserId(rs.getInt("user_id"));
                    a.setQuizId(rs.getInt("quiz_id"));
                    a.setQuizTitle(rs.getString("quiz_title"));
                    a.setStatus(rs.getString("status"));
                    a.setStartedAt(rs.getString("started_at"));
                    a.setSubmittedAt(rs.getString("submitted_at"));
                    a.setScore(rs.getInt("score"));
                    a.setTotal(rs.getInt("total"));
                    a.setCorrectCount(rs.getInt("correct_count"));
                    a.setWrongCount(rs.getInt("wrong_count"));
                    a.setUnansweredCount(rs.getInt("unanswered_count"));
                    a.setTimeTakenSec(rs.getInt("time_taken_sec"));
                    a.setAutoSubmitted(rs.getInt("auto_submitted") == 1);
                    a.setStudentName(rs.getString("student_name"));
                    a.setAnsweredCount(rs.getInt("answered_count"));
                    attempts.add(a);
                }
            }
            return attempts;
        } catch (SQLException e) {
            throw new QuizAppException("Database error while loading attempts", e);
        }
    }
}
