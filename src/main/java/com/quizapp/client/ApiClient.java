package com.quizapp.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.dto.ErrorResponse;
import com.quizapp.exception.QuizAppException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Talks to the server: sends JSON, reads JSON, turns server errors into QuizAppException.
 * Calls block, so screens run them on a background thread (see util.Async).
 */
public final class ApiClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper JSON = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ApiClient() {
    }

    /** The shared JSON mapper (also used by LiveClient). */
    static ObjectMapper json() {
        return JSON;
    }

    // ---- GET (overloaded: plain class, or generic type such as List<QuizSummary>) ----

    public static <T> T get(String path, Class<T> type) throws QuizAppException {
        return send(request(path).GET(), JSON.constructType(type));
    }

    public static <T> T get(String path, TypeReference<T> type) throws QuizAppException {
        return send(request(path).GET(), JSON.constructType(type));
    }

    // ---- POST / PUT / DELETE ----

    public static <T> T post(String path, Object body, Class<T> type) throws QuizAppException {
        return send(request(path).POST(jsonBody(body)), JSON.constructType(type));
    }

    /** POST when no response body is needed. */
    public static void post(String path, Object body) throws QuizAppException {
        send(request(path).POST(jsonBody(body)), null);
    }

    public static <T> T put(String path, Object body, Class<T> type) throws QuizAppException {
        return send(request(path).PUT(jsonBody(body)), JSON.constructType(type));
    }

    /** PUT when no response body is needed. */
    public static void put(String path, Object body) throws QuizAppException {
        send(request(path).PUT(jsonBody(body)), null);
    }

    public static void delete(String path) throws QuizAppException {
        send(request(path).DELETE(), null);
    }

    // ---- internals ----

    private static HttpRequest.Builder request(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(ClientConfig.serverUrl() + path))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        if (Session.getToken() != null) {
            builder.header("Authorization", "Bearer " + Session.getToken());
        }
        return builder;
    }

    private static HttpRequest.BodyPublisher jsonBody(Object body) throws QuizAppException {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        try {
            return HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body));
        } catch (IOException e) {
            throw new QuizAppException("Could not prepare the request", e);
        }
    }

    private static <T> T send(HttpRequest.Builder builder, JavaType responseType) throws QuizAppException {
        HttpResponse<String> response;
        try {
            response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // multiple catch blocks: network problem vs. thread interrupted
            throw new QuizAppException("Cannot reach the server at " + ClientConfig.serverUrl(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new QuizAppException("Request was cancelled", e);
        }

        int status = response.statusCode();
        String body = response.body();
        if (status >= 200 && status < 300) {
            if (responseType == null || body == null || body.isBlank()) {
                return null;
            }
            try {
                return JSON.readValue(body, responseType);
            } catch (IOException e) {
                throw new QuizAppException("Unexpected response from the server", e);
            }
        }
        throw new QuizAppException(errorMessage(status, body));
    }

    private static String errorMessage(int status, String body) {
        try {
            ErrorResponse error = JSON.readValue(body, ErrorResponse.class);
            if (error.error() != null && !error.error().isBlank()) {
                return error.error();
            }
        } catch (IOException | RuntimeException ignored) {
            // body was not our JSON error format
        }
        return "Server error (" + status + ")";
    }
}
