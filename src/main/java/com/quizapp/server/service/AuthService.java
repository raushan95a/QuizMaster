package com.quizapp.server.service;

import com.quizapp.dto.AuthResponse;
import com.quizapp.dto.LoginRequest;
import com.quizapp.dto.RegisterRequest;
import com.quizapp.dto.UserDto;
import com.quizapp.exception.DuplicateUserException;
import com.quizapp.exception.InvalidLoginException;
import com.quizapp.exception.QuizAppException;
import com.quizapp.exception.UnauthorizedException;
import com.quizapp.exception.ValidationException;
import com.quizapp.model.Admin;
import com.quizapp.model.Role;
import com.quizapp.model.Student;
import com.quizapp.model.User;
import com.quizapp.server.PasswordUtil;
import com.quizapp.server.SessionStore;
import com.quizapp.server.dao.UserDAO;

/** Registration, login, logout and "who is this token?". */
public class AuthService {

    public static final int MIN_PASSWORD_LENGTH = 6;

    private final UserDAO userDAO;
    private final SessionStore sessions;

    public AuthService(UserDAO userDAO, SessionStore sessions) {
        this.userDAO = userDAO;
        this.sessions = sessions;
    }

    /** Creates a new student account and logs it in. */
    public AuthResponse register(RegisterRequest request) throws QuizAppException {
        if (request == null) {
            throw new ValidationException("Request body is missing");
        }
        String fullName = trim(request.fullName());
        String username = trim(request.username());
        String password = request.password() == null ? "" : request.password();

        if (fullName.isEmpty() || fullName.length() > 80) {
            throw new ValidationException("Full name must be 1 to 80 characters");
        }
        if (!username.matches("[A-Za-z0-9_.]{3,30}")) {
            throw new ValidationException("Username must be 3 to 30 letters, digits, '_' or '.'");
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > 100) {
            throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (userDAO.findByUsername(username).isPresent()) {
            throw new DuplicateUserException(username);
        }

        String salt = PasswordUtil.newSalt();
        User student = new Student(0, username, fullName, PasswordUtil.hash(password, salt), salt);
        userDAO.add(student);
        return new AuthResponse(sessions.create(student.getId()), UserDto.from(student));
    }

    public AuthResponse login(LoginRequest request) throws QuizAppException {
        if (request == null || request.username() == null || request.password() == null) {
            throw new InvalidLoginException();
        }
        User user = userDAO.findByUsername(request.username().trim())
                .orElseThrow(InvalidLoginException::new);
        if (!PasswordUtil.verify(request.password(), user.getSalt(), user.getPasswordHash())) {
            throw new InvalidLoginException();
        }
        return new AuthResponse(sessions.create(user.getId()), UserDto.from(user));
    }

    public void logout(String token) {
        sessions.remove(token);
    }

    /** Finds the logged-in user for a token, or throws UnauthorizedException. */
    public User userForToken(String token) throws QuizAppException {
        int userId = sessions.getUserId(token).orElseThrow(UnauthorizedException::new);
        return userDAO.findById(userId).orElseThrow(UnauthorizedException::new);
    }

    /**
     * Makes sure at least one admin exists (called at server start).
     * @return true if a new admin was created
     */
    public boolean ensureAdminExists(String username, String password) throws QuizAppException {
        if (userDAO.countByRole(Role.ADMIN) > 0) {
            return false;
        }
        if (username == null || password == null) {
            throw new ValidationException(
                    "No admin account exists. Set ADMIN_USERNAME and ADMIN_PASSWORD environment variables.");
        }
        String salt = PasswordUtil.newSalt();
        userDAO.add(new Admin(0, username, "Administrator", PasswordUtil.hash(password, salt), salt));
        return true;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
