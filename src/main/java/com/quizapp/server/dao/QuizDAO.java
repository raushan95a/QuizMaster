package com.quizapp.server.dao;

import com.quizapp.exception.QuizAppException;
import com.quizapp.model.Quiz;
import com.quizapp.server.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC access to the quizzes table. */
public class QuizDAO implements Repository<Quiz> {

    /** Quiz columns plus the number of questions in it. */
    private static final String SELECT_WITH_COUNT =
            "SELECT q.*, (SELECT COUNT(*) FROM questions qs WHERE qs.quiz_id = q.id) AS question_count "
                    + "FROM quizzes q ";

    private final DatabaseManager db;

    public QuizDAO(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public int add(Quiz quiz) throws QuizAppException {
        String sql = "INSERT INTO quizzes (title, subject, description, time_limit_min, is_published, "
                + "created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getSubject());
            ps.setString(3, quiz.getDescription());
            ps.setInt(4, quiz.getTimeLimitMin());
            ps.setInt(5, quiz.isPublished() ? 1 : 0);
            ps.setInt(6, quiz.getCreatedBy());
            ps.setString(7, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                quiz.setId(keys.getInt(1));
                return quiz.getId();
            }
        } catch (SQLException e) {
            throw new QuizAppException("Database error while saving quiz", e);
        }
    }

    @Override
    public Optional<Quiz> findById(int id) throws QuizAppException {
        List<Quiz> list = query(SELECT_WITH_COUNT + "WHERE q.id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** All quizzes, newest first. */
    @Override
    public List<Quiz> findAll() throws QuizAppException {
        return query(SELECT_WITH_COUNT + "ORDER BY q.id DESC");
    }

    /** Only published quizzes (what students can see). */
    public List<Quiz> findPublished() throws QuizAppException {
        return query(SELECT_WITH_COUNT + "WHERE q.is_published = 1 ORDER BY q.id DESC");
    }

    @Override
    public void update(Quiz quiz) throws QuizAppException {
        String sql = "UPDATE quizzes SET title = ?, subject = ?, description = ?, time_limit_min = ? WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getSubject());
            ps.setString(3, quiz.getDescription());
            ps.setInt(4, quiz.getTimeLimitMin());
            ps.setInt(5, quiz.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new QuizAppException("Database error while updating quiz", e);
        }
    }

    public void setPublished(int quizId, boolean published) throws QuizAppException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE quizzes SET is_published = ? WHERE id = ?")) {
            ps.setInt(1, published ? 1 : 0);
            ps.setInt(2, quizId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new QuizAppException("Database error while publishing quiz", e);
        }
    }

    /** Deletes the quiz; its questions and attempts are removed too (ON DELETE CASCADE). */
    @Override
    public void delete(int id) throws QuizAppException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM quizzes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new QuizAppException("Database error while deleting quiz", e);
        }
    }

    public int count() throws QuizAppException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM quizzes");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new QuizAppException("Database error while counting quizzes", e);
        }
    }

    /** Runs a SELECT with optional int parameters and maps every row to a Quiz. */
    private List<Quiz> query(String sql, int... params) throws QuizAppException {
        List<Quiz> quizzes = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Quiz quiz = new Quiz(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("subject"),
                            rs.getString("description"),
                            rs.getInt("time_limit_min"),
                            rs.getInt("is_published") == 1,
                            rs.getInt("created_by"),
                            rs.getString("created_at"));
                    quiz.setQuestionCount(rs.getInt("question_count"));
                    quizzes.add(quiz);
                }
            }
            return quizzes;
        } catch (SQLException e) {
            throw new QuizAppException("Database error while loading quizzes", e);
        }
    }
}
