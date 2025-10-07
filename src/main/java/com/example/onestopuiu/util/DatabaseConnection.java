package com.example.onestopuiu.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/onestopuiu";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[Database] MySQL JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("[Database] Failed to register MySQL JDBC driver: " + e.getMessage());
            System.err.println("[Database] Application will use fallback authentication");
        }
    }

    public static Connection getConnection() {
        try {
            System.out.println("\n[Database] Attempting to connect to database...");
            System.out.println("[Database] URL: " + URL);
            System.out.println("[Database] User: " + USER);
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[Database] Connection established successfully!");
            return conn;
        } catch (SQLException e) {
            System.err.println("\n[Database] Failed to connect to database!");
            System.err.println("[Database] Error message: " + e.getMessage());
            System.err.println("[Database] SQL State: " + e.getSQLState());
            System.err.println("[Database] Error Code: " + e.getErrorCode());
            System.err.println("[Database] Application will continue with fallback authentication");
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[Database] Connection closed successfully");
                }
            } catch (SQLException e) {
                System.err.println("[Database] Error closing connection: " + e.getMessage());
            }
        }
    }
} 