package com.quizapp.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void correctPasswordVerifies() {
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash("secret123", salt);
        assertTrue(PasswordUtil.verify("secret123", salt, hash));
    }

    @Test
    void wrongPasswordFails() {
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash("secret123", salt);
        assertFalse(PasswordUtil.verify("Secret123", salt, hash));
    }

    @Test
    void samePasswordGetsDifferentHashesWithDifferentSalts() {
        String hash1 = PasswordUtil.hash("secret123", PasswordUtil.newSalt());
        String hash2 = PasswordUtil.hash("secret123", PasswordUtil.newSalt());
        assertNotEquals(hash1, hash2);
    }

    @Test
    void hashIsNotThePlainPassword() {
        String hash = PasswordUtil.hash("secret123", PasswordUtil.newSalt());
        assertFalse(hash.contains("secret123"));
    }
}
