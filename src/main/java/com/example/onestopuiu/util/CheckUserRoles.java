package com.example.onestopuiu.util;

import com.example.onestopuiu.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility to check and fix user roles in the database
 */
public class CheckUserRoles {
    
    public static void main(String[] args) {
        try {
            System.out.println("🔍 Checking user roles in database...");
            
            checkAllUsers();
            fixMahmud12Role();
            
            System.out.println("✅ User role check completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Error checking user roles: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkAllUsers() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, username, email, role FROM users ORDER BY id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                System.out.println("\n👥 All Users in Database:");
                System.out.println("ID | Username   | Email                    | Role");
                System.out.println("---|------------|--------------------------|----------");
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    
                    String roleIcon = role.equals("ADMIN") ? "👑" : 
                                    role.equals("SELLER") ? "🏪" : "👤";
                    
                    System.out.printf("%2d | %-10s | %-24s | %s %s%n", 
                        id, username, email, roleIcon, role);
                }
            }
        }
    }
    
    private static void fixMahmud12Role() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check current role of Mahmud12
            String checkSql = "SELECT role FROM users WHERE username = 'Mahmud12'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    String currentRole = rs.getString("role");
                    System.out.println("\n🔍 Current role of Mahmud12: " + currentRole);
                    
                    if (!currentRole.equals("CUSTOMER")) {
                        // Fix the role to CUSTOMER
                        String updateSql = "UPDATE users SET role = 'CUSTOMER' WHERE username = 'Mahmud12'";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            int rowsUpdated = updateStmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                System.out.println("✅ Fixed Mahmud12 role: " + currentRole + " → CUSTOMER");
                            }
                        }
                    } else {
                        System.out.println("✅ Mahmud12 role is already correct: CUSTOMER");
                    }
                } else {
                    System.out.println("⚠️ User Mahmud12 not found in database");
                }
            }
        }
    }
}