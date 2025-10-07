package com.example.onestopuiu.controller;

import com.example.onestopuiu.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.net.URL;

public class SimpleSectionController {
    @FXML
    private Label welcomeLabel;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        if (welcomeLabel != null && user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        }
        System.out.println("[SimpleSection] Initialized with user: " + (user != null ? user.getUsername() : "null"));
    }

    @FXML
    protected void handleCanteenOrdering() {
        showAlert("Canteen Feature", "Food ordering feature will be available soon!");
    }

    @FXML
    protected void handleMyOrders() {
        showAlert("My Orders", "Order history feature will be available soon!");
    }

    @FXML
    protected void handleBecomeSellerRequest() {
        showAlert("Seller Application", "Seller application feature will be available soon!");
    }

    @FXML
    protected void handleLogout() {
        try {
            System.out.println("[SimpleSection] Logging out...");
            URL fxmlUrl = getClass().getResource("/com/example/onestopuiu/login.fxml");
            if (fxmlUrl == null) {
                fxmlUrl = getClass().getResource("login.fxml");
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
            
            System.out.println("[SimpleSection] Successfully logged out");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not return to login: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}