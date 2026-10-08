package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    // Tren server (Render) doc tu bien moi truong; chay local thi dung gia tri mac dinh.
    private static final String URL = env("DB_URL",
        "jdbc:sqlserver://localhost:1433;databaseName=LocalTripDB;encrypt=false;trustServerCertificate=true");

    private static final String USER = env("DB_USER", "sa");
    private static final String PASSWORD = env("DB_PASSWORD", "12345");

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found.", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
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