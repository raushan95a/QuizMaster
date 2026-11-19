package com.quizapp.server;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers who is logged in: token -> user id.
 * Kept in memory, so restarting the server logs everyone out.
 * ConcurrentHashMap is used because many request threads read and write it at the same time
 * (Hashtable would also be thread-safe, but locks the whole map for every call).
 */
public class SessionStore {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, Integer> sessions = new ConcurrentHashMap<>();

    /** Creates a new random token for this user. */
    public String create(int userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, userId);
        return token;
    }

    public Optional<Integer> getUserId(String token) {
        return Optional.ofNullable(token == null ? null : sessions.get(token));
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
