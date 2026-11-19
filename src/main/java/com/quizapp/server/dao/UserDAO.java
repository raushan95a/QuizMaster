package com.quizapp.server.dao;

import com.quizapp.exception.DuplicateUserException;
import com.quizapp.exception.QuizAppException;
import com.quizapp.model.Role;
import com.quizapp.model.User;
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

/** JDBC access to the users table. */
public class UserDAO implements Repository<User> {

    private final DatabaseManager db;

    public UserDAO(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public int add(User user) throws QuizAppException {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, role, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getRole().name());
            ps.setString(6, Instant.now().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                user.setId(id);
                return id;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new DuplicateUserException(user.getUsername());
            }
            throw new QuizAppException("Database error while saving user", e);
        }
    }

    @Override
    public Optional<User> findById(int id) throws QuizAppException {
        return findOne("SELECT * FROM users WHERE id = ?", String.valueOf(id));
    }

    public Optional<User> findByUsername(String username) throws QuizAppException {
        return findOne("SELECT * FROM users WHERE username = ?", username);
    }

    @Override
    public List<User> findAll() throws QuizAppException {
        List<User> users = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users ORDER BY full_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new QuizAppException("Database error while loading users", e);
        }
    }

    /** Updates name and password (username and role never change). */
    @Override
    public void update(User user) throws QuizAppException {
        String sql = "UPDATE users SET full_name = ?, password_hash = ?, salt = ? WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new QuizAppException("Database error while updating user", e);
        }
    }

    @Override
    public void delete(int id) throws QuizAppException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new QuizAppException("Database error while deleting user", e);
        }
    }

    public int countByRole(Role role) throws QuizAppException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role = ?")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new QuizAppException("Database error while counting users", e);
        }
    }

    private Optional<User> findOne(String sql, String param) throws QuizAppException {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new QuizAppException("Database error while loading user", e);
        }
    }

    /** Converts one row into an Admin or Student object. */
    private User mapRow(ResultSet rs) throws SQLException {
        return User.create(
                Role.valueOf(rs.getString("role")),
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("password_hash"),
                rs.getString("salt"));
    }
}
