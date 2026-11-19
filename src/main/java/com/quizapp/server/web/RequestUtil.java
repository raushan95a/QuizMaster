package com.quizapp.server.web;

import com.quizapp.exception.ValidationException;
import io.javalin.http.Context;

/** Small helpers for reading requests. */
final class RequestUtil {

    private RequestUtil() {
    }

    /** Reads the JSON body; broken or missing JSON becomes a 400 error instead of a 500. */
    static <T> T body(Context ctx, Class<T> type) throws ValidationException {
        try {
            return ctx.bodyAsClass(type);
        } catch (Exception e) { // Javalin can throw Jackson's checked JsonParseException here

            throw new ValidationException("Invalid request body");
        }
    }
}
