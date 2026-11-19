package com.quizapp.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates JDBC connections to the SQLite database file and creates the tables.
 */
public class DatabaseManager {

    private final String url;

    public DatabaseManager(String dbPath) throws IOException {
        Path parent = Path.of(dbPath).toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent); // SQLite creates the file, but not the folder
        }
        this.url = "jdbc:sqlite:" + dbPath;
    }

    /** Opens a new connection. Callers close it with try-with-resources. */
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");  // SQLite has foreign keys OFF by default
            st.execute("PRAGMA busy_timeout = 5000"); // wait up to 5 s if another write is running
        }
        return connection;
    }

    /** Runs schema.sql (CREATE TABLE IF NOT EXISTS ...) so the database is ready to use. */
    public void createTables() throws SQLException, IOException {
        String sql;
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IOException("schema.sql not found on the classpath");
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Remove "--" comment lines first (a comment may itself contain ';'), then run each statement
        sql = sql.replaceAll("(?m)^\\s*--.*$", "");

        try (Connection connection = getConnection(); Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode = WAL"); // better when reads and writes happen together
            for (String statement : sql.split(";")) {
                if (!statement.isBlank()) {
                    st.execute(statement.trim());
                }
            }
        }
    }
}
