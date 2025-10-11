package com.example.onestopuiu.dao;

import com.example.onestopuiu.model.Payment;
import com.example.onestopuiu.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentDAO {

    public PaymentDAO() {
        createTable();
    }

    // Create payments table if it doesn't exist
    private void createTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS payments (
                payment_id INT PRIMARY KEY AUTO_INCREMENT,
                order_id INT NOT NULL,
                customer_id INT,
                amount DECIMAL(10,2) NOT NULL,
                payment_method VARCHAR(50) NOT NULL,
                payment_status VARCHAR(20) DEFAULT 'PENDING',
                transaction_id VARCHAR(255) UNIQUE,
                card_number VARCHAR(20),
                mobile_number VARCHAR(20),
                billing_address TEXT,
                payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_order_id (order_id),
                INDEX idx_transaction_id (transaction_id)
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("[PaymentDAO] Payments table created/verified successfully");
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error creating payments table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Create payment record
    public boolean createPayment(Payment payment) {
        String sql = "INSERT INTO payments (order_id, customer_id, amount, payment_method, payment_status, " +
                    "transaction_id, card_number, mobile_number, billing_address, payment_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Generate unique transaction ID
            payment.setTransactionId("TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            
            stmt.setInt(1, payment.getOrderId());
            stmt.setInt(2, payment.getCustomerId());
            stmt.setDouble(3, payment.getAmount());
            stmt.setString(4, payment.getPaymentMethod());
            stmt.setString(5, payment.getPaymentStatus());
            stmt.setString(6, payment.getTransactionId());
            stmt.setString(7, payment.getCardNumber());
            stmt.setString(8, payment.getMobileNumber());
            stmt.setString(9, payment.getBillingAddress());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    payment.setPaymentId(rs.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update payment status
    public boolean updatePaymentStatus(int paymentId, String status) {
        String sql = "UPDATE payments SET payment_status = ? WHERE payment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, paymentId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get payment by ID
    public Payment getPaymentById(int paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, paymentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPayment(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get payments by order ID
    public List<Payment> getPaymentsByOrderId(int orderId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE order_id = ? ORDER BY payment_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    // Get payments by customer ID
    public List<Payment> getPaymentsByCustomerId(int customerId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE customer_id = ? ORDER BY payment_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    // Get payment by transaction ID
    public Payment getPaymentByTransactionId(String transactionId) {
        String sql = "SELECT * FROM payments WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPayment(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Process refund
    public boolean processRefund(int paymentId, double refundAmount) {
        String sql = "UPDATE payments SET payment_status = 'REFUNDED', refund_amount = ? WHERE payment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, refundAmount);
            stmt.setInt(2, paymentId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Simulate payment processing
    public boolean processPayment(Payment payment) {
        // Simulate payment gateway processing
        try {
            // Simulate processing delay
            Thread.sleep(1000);
            
            // 95% success rate simulation
            boolean success = Math.random() > 0.05;
            
            if (success) {
                payment.setPaymentStatus("COMPLETED");
                return updatePaymentStatus(payment.getPaymentId(), "COMPLETED");
            } else {
                payment.setPaymentStatus("FAILED");
                return updatePaymentStatus(payment.getPaymentId(), "FAILED");
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
            payment.setPaymentStatus("FAILED");
            updatePaymentStatus(payment.getPaymentId(), "FAILED");
        }
        return false;
    }

    // Helper method to map ResultSet to Payment object
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setOrderId(rs.getInt("order_id"));
        payment.setCustomerId(rs.getInt("customer_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setPaymentStatus(rs.getString("payment_status"));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setCardNumber(rs.getString("card_number"));
        payment.setMobileNumber(rs.getString("mobile_number"));
        payment.setBillingAddress(rs.getString("billing_address"));
        payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        return payment;
    }

    // Create payments table if not exists
    public void createPaymentsTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS payments (
                payment_id INT AUTO_INCREMENT PRIMARY KEY,
                order_id INT NOT NULL,
                customer_id INT NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                payment_method VARCHAR(50) NOT NULL,
                payment_status VARCHAR(20) DEFAULT 'PENDING',
                transaction_id VARCHAR(50) UNIQUE,
                card_number VARCHAR(20),
                mobile_number VARCHAR(15),
                billing_address TEXT,
                refund_amount DECIMAL(10,2) DEFAULT 0,
                payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (order_id) REFERENCES food_orders(order_id),
                FOREIGN KEY (customer_id) REFERENCES users(user_id)
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("Payments table created/verified successfully.");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete payment record
    public boolean deletePayment(int paymentId) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paymentId);
            int rowsAffected = pstmt.executeUpdate();
            
            System.out.println("[PaymentDAO] Payment deleted: ID=" + paymentId + ", rows affected=" + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error deleting payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete all payments for a specific order
    public boolean deletePaymentsByOrderId(int orderId) {
        String sql = "DELETE FROM payments WHERE order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            int rowsAffected = pstmt.executeUpdate();
            
            System.out.println("[PaymentDAO] Payments deleted for order: ID=" + orderId + ", rows affected=" + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error deleting payments for order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete all payments for a specific customer
    public boolean deletePaymentsByCustomerId(int customerId) {
        String sql = "DELETE FROM payments WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            int rowsAffected = pstmt.executeUpdate();
            
            System.out.println("[PaymentDAO] Payments deleted for customer: ID=" + customerId + ", rows affected=" + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error deleting payments for customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}