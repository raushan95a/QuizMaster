package com.quizapp.dto;

/** Body of POST /api/auth/login. */
public record LoginRequest(String username, String password) {
}
