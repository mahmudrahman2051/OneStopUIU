package com.example.onestopuiu.util;

import com.example.onestopuiu.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Demo data setup for presentation
 * This ensures the database has good demo data for presentation
 */
public class PresentationSetup {
    
    public static void main(String[] args) {
        try {
            System.out.println("🎯 Setting up OneStopUIU for PRESENTATION...");
            System.out.println("====================================================");
            
            // Ensure all demo accounts exist
            setupDemoAccounts();
            
            // Ensure food items have good stock
            setupFoodItems();
            
            // Show final status
            showSystemStatus();
            
            System.out.println("====================================================");
            System.out.println("✅ PRESENTATION SETUP COMPLETED SUCCESSFULLY!");
            System.out.println("🎯 Ready for demonstration tomorrow!");
            System.out.println("====================================================");
            
        } catch (Exception e) {
            System.err.println("❌ Error setting up presentation data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void setupDemoAccounts() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("👥 Setting up demo user accounts...");
            
            // Admin account
            insertUser(conn, "admin", "admin123", "admin@onestopuiu.com", "ADMIN");
            
            // Customer accounts
            insertUser(conn, "customer1", "pass123", "customer1@uiu.ac.bd", "CUSTOMER");
            insertUser(conn, "student1", "student123", "student1@uiu.ac.bd", "CUSTOMER");
            insertUser(conn, "Mahmud12", "pass123", "mahmud@uiu.ac.bd", "CUSTOMER");
            
            // Seller account
            insertUser(conn, "seller1", "seller123", "seller1@uiu.ac.bd", "SELLER");
            
            System.out.println("✅ Demo accounts ready for presentation");
        }
    }
    
    private static void insertUser(Connection conn, String username, String password, String email, String role) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            
            if (rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, email);
                    stmt.setString(4, role);
                    stmt.executeUpdate();
                    System.out.println("   📝 Created: " + username + " (" + role + ")");
                }
            } else {
                // Update role to ensure it's correct
                String updateSql = "UPDATE users SET role = ? WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, role);
                    stmt.setString(2, username);
                    stmt.executeUpdate();
                    System.out.println("   ✅ Verified: " + username + " (" + role + ")");
                }
            }
        }
    }
    
    private static void setupFoodItems() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("🍽️ Setting up food items for demo...");
            
            // Ensure all items have good stock for demo
            updateFoodStock(conn, "Coffee", 20);
            updateFoodStock(conn, "Sandwitch", 15);
            updateFoodStock(conn, "French Fries", 12);
            updateFoodStock(conn, "Beef Curry", 10);
            updateFoodStock(conn, "Chicken Fry", 8);
            updateFoodStock(conn, "Duck fry", 6);
            updateFoodStock(conn, "Mojo", 50);
            
            System.out.println("✅ Food items ready for demonstration");
        }
    }
    
    private static void updateFoodStock(Connection conn, String itemName, int stock) throws SQLException {
        String sql = "UPDATE food_items SET stock_quantity = ?, available = 1 WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stock);
            stmt.setString(2, itemName);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("   📦 " + itemName + ": " + stock + " units");
            }
        }
    }
    
    private static void showSystemStatus() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("📊 FINAL SYSTEM STATUS FOR PRESENTATION:");
            System.out.println("----------------------------------------");
            
            // Count users by role
            String userSql = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String role = rs.getString("role");
                    int count = rs.getInt("count");
                    String icon = role.equals("ADMIN") ? "👑" : role.equals("SELLER") ? "🏪" : "👤";
                    System.out.println("   " + icon + " " + role + ": " + count + " users");
                }
            }
            
            // Count food items
            String foodSql = "SELECT COUNT(*) as total, SUM(stock_quantity) as total_stock FROM food_items WHERE available = 1";
            try (PreparedStatement stmt = conn.prepareStatement(foodSql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("   🍽️ FOOD ITEMS: " + rs.getInt("total") + " available");
                    System.out.println("   📦 TOTAL STOCK: " + rs.getInt("total_stock") + " units");
                }
            }
            
            // Count orders
            String orderSql = "SELECT status, COUNT(*) as count FROM food_orders GROUP BY status";
            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    String icon = status.equals("completed") ? "✅" : status.equals("pending") ? "⏳" : "❌";
                    System.out.println("   " + icon + " " + status.toUpperCase() + " orders: " + count);
                }
            }
            
            System.out.println("----------------------------------------");
        }
    }
}