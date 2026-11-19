package com.quizapp.server.dao;

import com.quizapp.exception.QuizAppException;
import com.quizapp.model.Question;
import com.quizapp.server.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC access to the questions table. */
public class QuestionDAO implements Repository<Question> {

    private final DatabaseManager db;

    public QuestionDAO(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public int add(Question q) throws QuizAppException {
        String sql = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, "
                + "correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getQuizId());
            ps.setString(2, q.getText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setString(7, q.getCorrectOption());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                q.setId(keys.getInt(1));
                return q.getId();
            }
        } catch (SQLException e) {
            throw new QuizAppException("Database error while saving question", e);
        }
    }

    @Override
    public Optional<Question> findById(int id) throws QuizAppException {
        List<Question> list = query("SELECT * FROM questions WHERE id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Question> findAll() throws QuizAppException {
        return query("SELECT * FROM questions ORDER BY quiz_id, id");
    }

    /** Questions of one quiz, in the order they were added. */
    public List<Question> findByQuiz(int quizId) throws QuizAppException {
        return query("SELECT * FROM questions WHERE quiz_id = ? ORDER BY id", quizId);
    }

    @Override
    public void update(Question q) throws QuizAppException {
        String sql = "UPDATE questions SET question_text = ?, option_a = ?, option_b = ?, option_c = ?, "
                + "option_d = ?, correct_option = ? WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getText());
            ps.setString(2, q.getOptionA());
            ps.setString(3, q.getOptionB());
            ps.setString(4, q.getOptionC());
            ps.setString(5, q.getOptionD());
            ps.setString(6, q.getCorrectOption());
            ps.setInt(7, q.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new QuizAppException("Database error while updating question", e);
        }
    }

    @Override
    public void delete(int id) throws QuizAppException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM questions WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new QuizAppException("Database error while deleting question", e);
        }
    }

    private List<Question> query(String sql, int... params) throws QuizAppException {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(new Question(
                            rs.getInt("id"),
                            rs.getInt("quiz_id"),
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            rs.getString("correct_option")));
                }
            }
            return questions;
        } catch (SQLException e) {
            throw new QuizAppException("Database error while loading questions", e);
        }
    }
}
