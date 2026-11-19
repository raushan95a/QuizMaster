package com.quizapp.dto;

/** Body of every error response: {"error": "message"}. */
public record ErrorResponse(String error) {
}
