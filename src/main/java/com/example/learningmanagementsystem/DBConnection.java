package com.example.learningmanagementsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/lms_database";
    private static final String USER = "postgres";
    private static final String PASSWORD = "mokone23"; // Replace with your PostgreSQL password

    // Method to establish a connection
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Method to test the connection
    public static boolean testConnection() {
        try (Connection conn = connect()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection test successful at " + java.time.LocalDateTime.now());
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    // Static class for logging activities
    public static class ActivityLogger {
        public static void logAction(int userId, String role, String action) {
            try (Connection conn = connect()) {
                String sql = "INSERT INTO audit_log (user_id, action) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setString(2, action + " by " + role + " at " + java.time.LocalDateTime.now());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Failed to log action: " + e.getMessage());
            }
        }
    }

    // Main method to run a test
    public static void main(String[] args) {
        boolean success = testConnection();
        if (success) {
            System.out.println("Connection test passed.");
        } else {
            System.out.println("Connection test failed.");
        }
    }
}
