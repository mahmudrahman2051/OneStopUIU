package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.FoodOrderDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SellerDashboardController extends SellerBaseController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Text pendingOrdersCount;
    @FXML private Text todaySales;
    
    private final FoodOrderDAO foodOrderDAO = new FoodOrderDAO();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up a timer to refresh data every 30 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> loadData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    @Override
    protected void onInitialize() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (Seller)");
            loadData();
        }
    }
    
    private void loadData() {
        if (currentUser == null) return;
        
        try {
            // Get pending orders count - only food orders now
            int pendingFoodOrders = foodOrderDAO.getPendingOrdersCount();
            pendingOrdersCount.setText(String.valueOf(pendingFoodOrders));

            // Calculate today's sales - only food sales now
            double todayFoodSalesAmount = foodOrderDAO.getTodaySales();
            
            // Format sales with Bengali Taka symbol (৳)
            todaySales.setText(String.format("৳%.2f", todayFoodSalesAmount));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Data Loading Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleCustomerView() {
        loadView("section-selector.fxml");
    }
    
    @FXML
    protected void handleEShoppingProductsManager() {
        // Navigate to canteen view where sellers can see and manage food items
        try {
            loadView("canteen-view.fxml");
        } catch (Exception e) {
            showError("Navigation Error", "Unable to load product management view: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleEShoppingOrders() {
        // Navigate to admin canteen orders view for order management
        try {
            loadView("admin-canteen-orders.fxml");
        } catch (Exception e) {
            showError("Navigation Error", "Unable to load order management view: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleEShoppingProfile() {
        // Navigate to user profile management (can use login view for profile editing)
        try {
            loadView("login.fxml");
        } catch (Exception e) {
            showError("Navigation Error", "Unable to load profile view: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleEShoppingInventory() {
        // Navigate to food items manager for inventory management
        try {
            loadView("food-items-manager.fxml");
        } catch (Exception e) {
            showError("Navigation Error", "Unable to load inventory management view: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }
} 
