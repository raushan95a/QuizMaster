package com.quizapp.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quizapp.dto.AuthResponse;
import com.quizapp.dto.LoginRequest;
import com.quizapp.dto.RegisterRequest;
import com.quizapp.exception.DuplicateUserException;
import com.quizapp.exception.InvalidLoginException;
import com.quizapp.exception.UnauthorizedException;
import com.quizapp.exception.ValidationException;
import com.quizapp.model.Admin;
import com.quizapp.model.Role;
import com.quizapp.model.Student;
import com.quizapp.model.User;
import com.quizapp.server.DatabaseManager;
import com.quizapp.server.SessionStore;
import com.quizapp.server.dao.UserDAO;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuthServiceTest {

    @TempDir
    Path tempDir;

    private AuthService auth;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager db = new DatabaseManager(tempDir.resolve("test.db").toString());
        db.createTables();
        auth = new AuthService(new UserDAO(db), new SessionStore());
    }

    @Test
    void registerCreatesStudentAndLogsIn() throws Exception {
        AuthResponse response = auth.register(new RegisterRequest("Ravi Kumar", "ravi", "secret123"));

        assertNotNull(response.token());
        assertEquals(Role.STUDENT, response.user().role());
        User user = auth.userForToken(response.token());
        assertTrue(user instanceof Student);
        assertEquals("Ravi Kumar", user.getFullName());
    }

    @Test
    void duplicateUsernameIsRejectedIgnoringCase() throws Exception {
        auth.register(new RegisterRequest("Ravi", "ravi", "secret123"));
        assertThrows(DuplicateUserException.class,
                () -> auth.register(new RegisterRequest("Other", "RAVI", "secret123")));
    }

    @Test
    void invalidInputIsRejected() {
        assertThrows(ValidationException.class, () -> auth.register(new RegisterRequest("", "ravi", "secret123")));
        assertThrows(ValidationException.class, () -> auth.register(new RegisterRequest("Ravi", "r", "secret123")));
        assertThrows(ValidationException.class, () -> auth.register(new RegisterRequest("Ravi", "ravi", "123")));
    }

    @Test
    void loginWorksWithCorrectPasswordOnly() throws Exception {
        auth.register(new RegisterRequest("Ravi", "ravi", "secret123"));

        assertNotNull(auth.login(new LoginRequest("ravi", "secret123")).token());
        assertThrows(InvalidLoginException.class, () -> auth.login(new LoginRequest("ravi", "wrong-pass")));
        assertThrows(InvalidLoginException.class, () -> auth.login(new LoginRequest("nobody", "secret123")));
    }

    @Test
    void logoutInvalidatesToken() throws Exception {
        String token = auth.register(new RegisterRequest("Ravi", "ravi", "secret123")).token();
        auth.logout(token);
        assertThrows(UnauthorizedException.class, () -> auth.userForToken(token));
    }

    @Test
    void adminIsCreatedOnlyOnce() throws Exception {
        assertTrue(auth.ensureAdminExists("admin", "admin123"));
        assertFalse(auth.ensureAdminExists("admin2", "admin123"));

        User admin = auth.userForToken(auth.login(new LoginRequest("admin", "admin123")).token());
        assertTrue(admin instanceof Admin);
    }

    @Test
    void missingAdminSettingsFailClearly() {
        assertThrows(ValidationException.class, () -> auth.ensureAdminExists(null, null));
    }
}
