package com.quizapp.client;

import com.quizapp.dto.AuthResponse;
import com.quizapp.model.User;

/** The logged-in user and their token (one user per running app). */
public final class Session {

    private static User currentUser;
    private static String token;

    private Session() {
    }

    public static void start(AuthResponse response) {
        currentUser = response.user().toUser(); // becomes an Admin or a Student object
        token = response.token();
    }

    public static void clear() {
        currentUser = null;
        token = null;
    }

    public static User getUser() {
        return currentUser;
    }

    public static String getToken() {
        return token;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }
}
