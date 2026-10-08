package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    // Support both Railway (DATABASE_URL) and standard env vars or local PostgreSQL
    private static final String DEFAULT_URL =
        "jdbc:postgresql://localhost:5432/LocalTripDB";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        }

        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            rawUrl = System.getenv("DB_URL");
        }

        if (rawUrl != null && !rawUrl.trim().isEmpty()) {
            rawUrl = rawUrl.trim();
            try {
                // Remove jdbc: prefix temporarily if present so URI can parse it
                String uriStr = rawUrl;
                if (uriStr.startsWith("jdbc:")) {
                    uriStr = uriStr.substring("jdbc:".length());
                }
                if (uriStr.startsWith("postgres://") || uriStr.startsWith("postgresql://")) {
                    java.net.URI uri = new java.net.URI(uriStr);
                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath(); // /railway
                    String cleanJdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                    String user = getUser();
                    String password = getPassword();

                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":", 2);
                        user = userInfo[0];
                        if (userInfo.length > 1) {
                            password = userInfo[1];
                        }
                    }

                    return DriverManager.getConnection(cleanJdbcUrl, user, password);
                }
            } catch (Exception ignored) {
                // Fallback to direct connection if URI parse fails
            }

            return DriverManager.getConnection(rawUrl, getUser(), getPassword());
        }

        return DriverManager.getConnection(DEFAULT_URL, getUser(), getPassword());
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