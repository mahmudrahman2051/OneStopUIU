package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.FoodItemDAO;
import com.example.onestopuiu.dao.FoodOrderDAO;
import com.example.onestopuiu.model.FoodItem;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController extends AdminBaseController implements Initializable {
    @FXML private Text pendingOrdersCount;
    @FXML private Text todayFoodSales;
    @FXML private Text lowStockCount;

    @FXML private TableView<FoodItem> foodItemsTable;
    @FXML private TableColumn<FoodItem, Integer> foodIdColumn;
    @FXML private TableColumn<FoodItem, String> foodNameColumn;
    @FXML private TableColumn<FoodItem, Double> foodPriceColumn;
    @FXML private TableColumn<FoodItem, String> foodDescriptionColumn;
    @FXML private TableColumn<FoodItem, String> foodCategoryColumn;
    @FXML private TableColumn<FoodItem, Integer> foodStockColumn;
    @FXML private TableColumn<FoodItem, Boolean> foodAvailableColumn;

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();
    private final FoodOrderDAO foodOrderDAO = new FoodOrderDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        loadData();
        
        // Set up a timer to refresh data every 30 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> loadData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setupTables() {
        // Food Items Table
        foodIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        foodNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        foodPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        foodDescriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        foodCategoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        foodStockColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStockQuantity()).asObject());
        foodAvailableColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isAvailable()));
        foodAvailableColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });

        // Custom cell factories for better display
        foodPriceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("৳%.2f", price));
                }
            }
        });

        // Use Platform.runLater to ensure the scene graph is constructed before binding
        Platform.runLater(() -> {
            if (foodItemsTable.getScene() != null) {
                foodItemsTable.getParent().layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                    foodItemsTable.setPrefHeight(newVal.getHeight() - 10);
                });
            }
        });
        
        // Add listeners to handle the table parent being set later
        foodItemsTable.parentProperty().addListener((observable, oldParent, newParent) -> {
            if (newParent != null) {
                newParent.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                    foodItemsTable.setPrefHeight(newVal.getHeight() - 10);
                });
            }
        });
    }

    private void loadData() {
        try {
            // Load items for tables
            List<FoodItem> foodItems = foodItemDAO.getAll();
            foodItemsTable.setItems(FXCollections.observableArrayList(foodItems));

            // Update dashboard stats
            List<FoodItem> lowStockFoodItems = foodItemDAO.getAll().stream()
                .filter(item -> item.getStockQuantity() <= 5)
                .collect(Collectors.toList());
            
            // Get pending orders count
            int pendingFoodOrders = foodOrderDAO.getPendingOrdersCount();

            // Update UI with stats
            lowStockCount.setText(String.valueOf(lowStockFoodItems.size()));
            pendingOrdersCount.setText(String.valueOf(pendingFoodOrders));

            // Calculate today's sales
            double todayFoodSalesAmount = foodOrderDAO.getTodaySales();
            
            // Format sales with Bengali Taka symbol (৳)
            todayFoodSales.setText(String.format("৳%.2f", todayFoodSalesAmount));
        } catch (Exception e) {
            showError("Data Loading Error", "Failed to load items: " + e.getMessage());
        }
    }

    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/login.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load login view: " + e.getMessage());
        }
    }

    @FXML
    protected void handleAddFoodItem() {
        loadView("food-items-manager.fxml");
    }

    @FXML
    protected void handleViewFoodOrders() {
        loadView("admin-canteen-orders.fxml");
    }

    @FXML
    protected void handleManageOrders() {
        loadView("admin-orders-view.fxml");
    }

    @FXML
    protected void handleGenerateFoodReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("=== Food Sales Report ===\n\n");
            
            // Today's Sales
            double todaySales = foodOrderDAO.getTodaySales();
            report.append("Today's Sales: ৳").append(String.format("%.2f", todaySales)).append("\n\n");
            
            // All-time Sales
            double allTimeSales = foodOrderDAO.getAllTimeSales();
            report.append("All-time Sales: ৳").append(String.format("%.2f", allTimeSales)).append("\n\n");
            
            // Get all food items
            List<FoodItem> foodItems = foodItemDAO.getAll();
            
            // Popular Items (based on orders)
            report.append("Top 5 Popular Items:\n");
            foodItems.stream()
                .sorted((a, b) -> {
                    try {
                        int ordersA = foodOrderDAO.getItemOrderCount(a.getId());
                        int ordersB = foodOrderDAO.getItemOrderCount(b.getId());
                        return ordersB - ordersA;
                    } catch (SQLException e) {
                        return 0;
                    }
                })
                .limit(5)
                .forEach(item -> {
                    try {
                        int orderCount = foodOrderDAO.getItemOrderCount(item.getId());
                        report.append("- ").append(item.getName())
                            .append(" (Orders: ").append(orderCount)
                            .append(")\n");
                    } catch (SQLException e) {
                        report.append("- ").append(item.getName()).append("\n");
                    }
                });
            
            // Low Stock Items
            report.append("\nLow Stock Items (≤ 5):\n");
            foodItems.stream()
                .filter(item -> item.getStockQuantity() <= 5)
                .forEach(item -> report.append("- ").append(item.getName())
                    .append(" (Stock: ").append(item.getStockQuantity())
                    .append(")\n"));
            
            // Show report in dialog
            showReportDialog("Food Sales Report", report.toString());
        } catch (SQLException e) {
            showError("Error", "Failed to generate food report: " + e.getMessage());
        }
    }

    @FXML
    protected void handleViewSellerRequests() {
        loadView("admin-seller-requests.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/" + fxmlFile));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();

            Object controller = fxmlLoader.getController();
            if (controller instanceof AdminBaseController) {
                ((AdminBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }

    @Override
    protected void onInitialize() {
        // This is now handled by the initialize method
    }

    // Method to refresh item tables when needed
    public void refreshItemTables() {
        try {
            // Get food items and update the table
            List<FoodItem> foodItems = foodItemDAO.getAll();
            foodItemsTable.setItems(FXCollections.observableArrayList(foodItems));
        } catch (Exception e) {
            showError("Data Loading Error", "Failed to refresh items: " + e.getMessage());
        }
    }

    private void showReportDialog(String title, String content) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        // Set the button types
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        // Create the content
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(50);
        
        dialog.getDialogPane().setContent(textArea);
        dialog.showAndWait();
    }
} 
