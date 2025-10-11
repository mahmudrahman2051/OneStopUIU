package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.SellerRequestDAO;
import com.example.onestopuiu.model.SellerRequest;
import com.example.onestopuiu.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;

public class SectionSelectorController extends CustomerBaseController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Button canteenButton;

    @FXML
    private Button myOrdersButton;
    
    @FXML
    private Button becomeSellerButton;

    @FXML
    private Label becomeSellerLabel;

    private User currentUser;

    private final SellerRequestDAO sellerRequestDAO = new SellerRequestDAO();

    @Override
    protected void onInitialize() {
        // Any initialization specific to SectionSelector can go here
        checkSellerRequestStatus();
    }

    public void initData(User user) {
        this.currentUser = user;
        
        // Check if user is null
        if (user == null) {
            System.err.println("[SectionSelector] Warning: User data is null, redirecting to login");
            loadSection("login.fxml");
            return;
        }
        
        // Set personalized welcome message
        String timeOfDay = getTimeOfDay();
        welcomeLabel.setText(timeOfDay + ", " + user.getUsername() + " 👋");
        
        // If user is already a seller or admin, hide the become seller button and label
        if (user.getRole().equals("SELLER") || user.getRole().equals("ADMIN")) {
            becomeSellerButton.setVisible(false);
            becomeSellerLabel.setVisible(false);
        } else {
            becomeSellerButton.setVisible(true);
            becomeSellerLabel.setVisible(true);
        }
        
        checkSellerRequestStatus();
    }
    
    private String getTimeOfDay() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) {
            return "Good Morning";
        } else if (hour < 17) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }
    
    private void checkSellerRequestStatus() {
        if (currentUser == null) return;
        
        try {
            Optional<SellerRequest> requestOpt = sellerRequestDAO.getByUserId(currentUser.getId());
            if (requestOpt.isPresent()) {
                SellerRequest request = requestOpt.get();
                switch (request.getStatus()) {
                    case "pending":
                        becomeSellerButton.setText("🔄 Pending Review");
                        becomeSellerButton.setDisable(true);
                        becomeSellerButton.getStyleClass().add("pending-button");
                        break;
                    case "approved":
                        becomeSellerButton.setText("🏪 Seller Dashboard");
                        becomeSellerButton.setDisable(false);
                        break;
                    case "rejected":
                        becomeSellerButton.setText("❌ Application Rejected");
                        becomeSellerButton.setDisable(true);
                        becomeSellerButton.getStyleClass().add("rejected-button");
                        break;
                }
            } else {
                // No request, show default state
                becomeSellerButton.setText("🏪 Apply Now");
                becomeSellerButton.setDisable(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleCanteenSection() {
        loadSection("canteen-view.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadSection("login.fxml");
    }

    @FXML
    protected void handleMyOrders() {
        loadSection("my-orders.fxml");
    }
    
    @FXML
    protected void handleHome() {
        // Refresh current page
        loadSection("section-selector.fxml");
    }
    
    @FXML
    protected void handleHelp() {
        showAlert(Alert.AlertType.INFORMATION, "Help & Support", 
                  "OneStopUIU Support:\n\n" +
                  "📞 Phone: +880-1234-567890\n" +
                  "📧 Email: support@onestopuiu.com\n" +
                  "🕒 Hours: 9 AM - 6 PM (Mon-Fri)\n\n" +
                  "For technical issues or account help,\nplease contact our support team.");
    }
    
    @FXML
    protected void handleSettings() {
        showAlert(Alert.AlertType.INFORMATION, "Settings", 
                  "Settings feature coming soon!\n\n" +
                  "You'll be able to:\n" +
                  "• Update profile information\n" +
                  "• Change password\n" +
                  "• Notification preferences\n" +
                  "• Payment methods\n\n" +
                  "Stay tuned for updates!");
    }
    
    @FXML
    protected void handleBecomeSeller() {
        if (currentUser == null) return;
        
        // If the user is already a SELLER, redirect to seller dashboard
        if (currentUser.getRole().equals("SELLER")) {
            loadSection("seller-dashboard.fxml");
            return;
        }
        
        try {
            Optional<SellerRequest> existingRequest = sellerRequestDAO.getByUserId(currentUser.getId());
            if (existingRequest.isPresent()) {
                String status = existingRequest.get().getStatus();
                if (status.equals("approved")) {
                    loadSection("seller-dashboard.fxml");
                    return;
                } else if (status.equals("pending")) {
                    showAlert(Alert.AlertType.INFORMATION, "Request Pending", "Your seller request is still pending approval.");
                    return;
                } else if (status.equals("rejected")) {
                    showAlert(Alert.AlertType.INFORMATION, "Request Rejected", "Your seller request was rejected.");
                    return;
                }
            }
            
            // Show dialog to confirm
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Become a Seller");
            dialog.setHeaderText("Apply to become a seller");
            dialog.setContentText("Please provide a reason why you want to become a seller:");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String reason = result.get();
                
                SellerRequest request = new SellerRequest();
                request.setUserId(currentUser.getId());
                request.setStatus("pending");
                request.setReason(reason);
                
                sellerRequestDAO.save(request);
                
                becomeSellerButton.setText("Seller Request Pending");
                becomeSellerButton.setDisable(true);
                
                showAlert(Alert.AlertType.INFORMATION, "Request Submitted", "Your seller request has been submitted and is pending approval.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to process seller request. Please try again.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadSection(String fxmlFile) {
        try {
            // Debug: Check current user before navigation
            if (currentUser == null) {
                System.err.println("[SectionSelector] Warning: currentUser is null when navigating to " + fxmlFile);
                showAlert(Alert.AlertType.ERROR, "Session Error", "User session has expired. Please login again.");
                loadSection("login.fxml");
                return;
            }
            
            String fxmlPath = "/com/example/onestopuiu/" + fxmlFile;
            URL resourceUrl = getClass().getResource(fxmlPath);

            if (resourceUrl == null) {
                throw new IOException("Cannot find FXML file: " + fxmlPath);
            }

            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());

            // Try to get stage from multiple sources
            Stage stage = null;
            try {
                if (canteenButton != null && canteenButton.getScene() != null) {
                    stage = (Stage) canteenButton.getScene().getWindow();
                } else if (welcomeLabel != null && welcomeLabel.getScene() != null) {
                    stage = (Stage) welcomeLabel.getScene().getWindow();
                } else if (becomeSellerButton != null && becomeSellerButton.getScene() != null) {
                    stage = (Stage) becomeSellerButton.getScene().getWindow();
                }
            } catch (Exception e) {
                System.err.println("[SectionSelector] Error getting stage: " + e.getMessage());
            }
            
            if (stage == null) {
                throw new IOException("Could not find application stage");
            }
            
            stage.setScene(scene);
            
            // Configure stage properties
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.centerOnScreen();

            // Pass user data to the next controller
            Object controller = fxmlLoader.getController();
            if (controller instanceof CustomerBaseController) {
                System.out.println("[SectionSelector] Passing user data to " + controller.getClass().getSimpleName() + 
                                 ": " + (currentUser != null ? currentUser.getUsername() : "null"));
                ((CustomerBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load view: " + e.getMessage());
        }
    }
}
