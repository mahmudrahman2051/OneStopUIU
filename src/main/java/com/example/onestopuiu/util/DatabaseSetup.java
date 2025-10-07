package com.example.onestopuiu.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class to set up admin user and fix stock issues
 */
public class DatabaseSetup {
    
    public static void main(String[] args) {
        try {
            System.out.println("🔧 Setting up database...");
            
            // Create admin user
            createAdminUser();
            
            // Fix stock levels
            updateStockLevels();
            
            // Show current stock status
            showStockStatus();
            
            System.out.println("✅ Database setup completed successfully!");
            System.out.println("\n🎯 You can now:");
            System.out.println("   • Order Coffee (now has 10 units in stock)");
            System.out.println("   • Order French Fries (now has 5 units in stock)"); 
            System.out.println("   • Login as admin: username='admin', password='admin123'");
            
        } catch (Exception e) {
            System.err.println("❌ Error setting up database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createAdminUser() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if admin already exists
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                
                if (count == 0) {
                    // Create admin user
                    String insertSql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                        stmt.setString(1, "admin");
                        stmt.setString(2, "admin123");
                        stmt.setString(3, "admin@onestopuiu.com");
                        stmt.setString(4, "ADMIN");
                        stmt.executeUpdate();
                        System.out.println("👤 Created admin user: admin/admin123");
                    }
                } else {
                    System.out.println("👤 Admin user already exists");
                }
            }
        }
    }
    
    private static void updateStockLevels() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update Coffee stock (ID: 3)
            updateItemStock(conn, 3, "Coffee", 10);
            
            // Update French Fries stock (ID: 2) 
            updateItemStock(conn, 2, "French Fries", 5);
        }
    }
    
    private static void updateItemStock(Connection conn, int itemId, String itemName, int newStock) throws SQLException {
        String updateSql = "UPDATE food_items SET stock_quantity = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, itemId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("📦 Updated " + itemName + " stock to " + newStock + " units");
            }
        }
    }
    
    private static void showStockStatus() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, stock_quantity, available FROM food_items ORDER BY id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                System.out.println("\n📊 Current Stock Levels:");
                System.out.println("ID | Name           | Stock | Available");
                System.out.println("---|----------------|-------|----------");
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int stock = rs.getInt("stock_quantity");
                    boolean available = rs.getBoolean("available");
                    
                    String status = available ? "✅" : "❌";
                    String stockStatus = stock > 0 ? "✅" : "⚠️";
                    
                    System.out.printf("%2d | %-14s | %3d %s | %s%n", 
                        id, name, stock, stockStatus, status);
                }
            }
        }
    }
}