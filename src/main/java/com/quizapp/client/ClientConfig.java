package com.quizapp.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Where the server is. Checked in this order:
 * 1. -Dserver.url=...            (command line)
 * 2. server.url in config.properties (next to where the app is started)
 * 3. http://localhost:7070       (local development)
 */
public final class ClientConfig {

    public static final String DEFAULT_SERVER_URL = "http://localhost:7070";
    private static final String serverUrl = loadServerUrl();

    private ClientConfig() {
    }

    public static String serverUrl() {
        return serverUrl;
    }

    private static String loadServerUrl() {
        String url = System.getProperty("server.url");
        if (url == null || url.isBlank()) {
            url = readFromFile(Path.of("config.properties"));
        }
        if (url == null || url.isBlank()) {
            url = DEFAULT_SERVER_URL;
        }
        url = url.trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String readFromFile(Path file) {
        if (!Files.exists(file)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file.toFile())) {
            props.load(in);
            return props.getProperty("server.url");
        } catch (IOException e) {
            return null;
        }
    }
}
