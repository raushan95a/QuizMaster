package com.quizapp.dto;

/** Response of login / register: the session token and the logged-in user. */
public record AuthResponse(String token, UserDto user) {
}
