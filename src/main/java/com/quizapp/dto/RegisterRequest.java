package com.quizapp.dto;

/** Body of POST /api/auth/register. */
public record RegisterRequest(String fullName, String username, String password) {
}
