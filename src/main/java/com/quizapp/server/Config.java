package com.quizapp.server;

/**
 * Server settings, read from environment variables so the same jar runs locally and online.
 */
public final class Config {

    public static final int DEFAULT_PORT = 7070;
    public static final String DEFAULT_DB_PATH = "data/quiz.db";

    private final boolean dev;
    private final int port;
    private final String dbPath;
    private final String adminUsername;
    private final String adminPassword;

    public Config(boolean dev, int port, String dbPath, String adminUsername, String adminPassword) {
        this.dev = dev;
        this.port = port;
        this.dbPath = dbPath;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    /** Reads APP_ENV, PORT, DB_PATH, ADMIN_USERNAME, ADMIN_PASSWORD. */
    public static Config fromEnvironment() {
        boolean dev = !"prod".equalsIgnoreCase(env("APP_ENV", "dev"));
        int port = Integer.parseInt(env("PORT", String.valueOf(DEFAULT_PORT)));
        String dbPath = env("DB_PATH", DEFAULT_DB_PATH);

        // In dev a demo admin is created automatically; in production it must come from env vars.
        String adminUser = env("ADMIN_USERNAME", dev ? "admin" : null);
        String adminPass = env("ADMIN_PASSWORD", dev ? "admin123" : null);
        return new Config(dev, port, dbPath, adminUser, adminPass);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    public boolean isDev() {
        return dev;
    }

    public int getPort() {
        return port;
    }

    public String getDbPath() {
        return dbPath;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}
