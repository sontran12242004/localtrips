package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    // Support both Railway (DATABASE_URL) and standard env vars or local PostgreSQL
    private static final String DEFAULT_URL =
        "jdbc:postgresql://localhost:5432/LocalTripDB";

    private static String getJdbcUrl() {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl != null && !dbUrl.trim().isEmpty()) {
            dbUrl = dbUrl.trim();
            // Convert postgres:// or postgresql:// to jdbc:postgresql://
            if (dbUrl.startsWith("postgres://")) {
                dbUrl = "jdbc:postgresql://" + dbUrl.substring("postgres://".length());
            } else if (dbUrl.startsWith("postgresql://")) {
                dbUrl = "jdbc:postgresql://" + dbUrl.substring("postgresql://".length());
            } else if (!dbUrl.startsWith("jdbc:postgresql://")) {
                dbUrl = "jdbc:postgresql://" + dbUrl;
            }
            return dbUrl;
        }

        String customUrl = System.getenv("DB_URL");
        if (customUrl != null && !customUrl.trim().isEmpty()) {
            return customUrl.trim();
        }

        return DEFAULT_URL;
    }

    private static String getUser() {
        String user = System.getenv("DB_USER");
        if (user == null || user.trim().isEmpty()) {
            user = System.getenv("PGUSER");
        }
        if (user == null || user.trim().isEmpty()) {
            user = System.getenv("POSTGRES_USER");
        }
        return (user != null && !user.trim().isEmpty()) ? user.trim() : "postgres";
    }

    private static String getPassword() {
        String pass = System.getenv("DB_PASSWORD");
        if (pass == null) {
            pass = System.getenv("PGPASSWORD");
        }
        if (pass == null) {
            pass = System.getenv("POSTGRES_PASSWORD");
        }
        return pass != null ? pass : "postgres";
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        }

        String jdbcUrl = getJdbcUrl();
        // If DATABASE_URL includes user:pass embedded in standard URI format, DriverManager will handle or fallback
        return DriverManager.getConnection(jdbcUrl, getUser(), getPassword());
    }

    public static void close(AutoCloseable... resources) {
        if (resources == null) {
            return;
        }

        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}