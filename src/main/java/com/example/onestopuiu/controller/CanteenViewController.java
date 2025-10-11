package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.FoodItemDAO;
import com.example.onestopuiu.dao.FoodOrderDAO;
import com.example.onestopuiu.model.FoodItem;
import com.example.onestopuiu.model.FoodOrder;
import com.example.onestopuiu.model.FoodOrderItem;
import com.example.onestopuiu.model.User;
import com.example.onestopuiu.util.CartManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import com.example.onestopuiu.util.ImageCache;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CanteenViewController extends CustomerBaseController {
    @FXML private ComboBox<String> filterComboBox;
    @FXML private GridPane menuItemsGrid;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartItemColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, Double> itemPriceColumn;
    @FXML private TableColumn<CartItem, Double> totalColumn;
    @FXML private TableColumn<CartItem, Void> removeColumn;
    @FXML private Label totalAmountLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private StackPane mainContainer;

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();
    private final FoodOrderDAO foodOrderDAO = new FoodOrderDAO();
    private final CartManager cartManager = CartManager.getInstance();
    private ObservableList<CartItem> cartItems;
    private double totalAmount = 0.0;

    @FXML
    public void initialize() {
        // Initialize filter combo box with categories
        filterComboBox.setItems(FXCollections.observableArrayList("All", "breakfast", "Snacks", "lunch"));
        filterComboBox.setValue("All");
        
        // Initialize cart items (empty initially)
        cartItems = FXCollections.observableArrayList();
        
        // Setup tables and listeners
        setupCartTable();
        setupFilterListener();
        
        // Don't load menu here - wait for user data
    }

    @Override
    protected void onInitialize() {
        // This is called after initData sets the currentUser
        if (currentUser == null) {
            showError("Error", "No user data available");
            return;
        }

        // Load cart items from CartManager
        loadCartFromManager();
        
        // Update total amount
        updateTotalAmount();
        
        // Load menu items (this will run in background)
        loadMenu();
    }

    private void loadCartFromManager() {
        // Clear existing cart items
        cartItems.clear();
        
        // Load cart items from CartManager
        ObservableList<Object> savedCartItems = cartManager.getCanteenCartItems();
        if (savedCartItems != null && !savedCartItems.isEmpty()) {
            for (Object item : savedCartItems) {
                if (item instanceof CartItem) {
                    cartItems.add((CartItem) item);
                }
            }
        }
        
        // Get saved total amount
        totalAmount = cartManager.getCanteenTotalAmount();
    }

    @Override
    public void initData(User user) {
        super.initData(user);
        // The rest is handled in onInitialize() which is called by super.initData()
    }


    private void displayFoodItems(List<FoodItem> items) {
        // Clear existing items
        menuItemsGrid.getChildren().clear();
        
        if (items == null || items.isEmpty()) {
            return;
        }

        int columnCount = 3; // Number of items per row
        int row = 0;
        int col = 0;

        // Create cards for each item
        for (FoodItem item : items) {
            try {
                VBox itemCard = createFoodItemCard(item);
                menuItemsGrid.add(itemCard, col, row);
                
                col++;
                if (col >= columnCount) {
                    col = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("Error creating card for item: " + item.getName() + " - " + e.getMessage());
                // Continue with other items
            }
        }
    }

    private VBox createFoodItemCard(FoodItem item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("item-card");
        card.setPrefWidth(200);
        card.setMaxWidth(200);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(event -> showItemDetails(item));


        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("item-image");
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Debug: Print image path
        System.out.println("[CanteenView] Loading image for " + item.getName() + 
                          ": '" + item.getImage() + "'");

        // Use ImageCache to load image efficiently
        ImageCache.getImage(item.getImage(), true, image -> {
            imageView.setImage(image);
            System.out.println("[CanteenView] Image loaded for " + item.getName() + 
                              ": " + (image != null ? "SUCCESS" : "FAILED"));
        });

        // Name
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("item-name");

        // Price
        Label priceLabel = new Label(String.format("৳%.2f", item.getPrice()));
        priceLabel.getStyleClass().add("item-price");

        // Stock
        Label stockLabel = new Label("Stock: " + item.getStockQuantity());
        stockLabel.getStyleClass().add("item-stock");

        // Add to Cart Button
        Button addButton = new Button("Add");
        addButton.getStyleClass().add("add-to-cart-button");
        addButton.setOnAction(event -> addToCart(item));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, addButton);
        return card;
    }

    private void showItemDetails(FoodItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/item-details.fxml"));
            Parent root = loader.load();

            ItemDetailsController controller = loader.getController();
            controller.setItem(item);
            controller.setParentController(this);


            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Item Details");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load item details");
        }
    }

    private void setupCartTable() {
        // Set items
        cartTable.setItems(cartItems);
        
        // Configure columns
        cartItemColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        quantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        itemPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getUnitPrice()).asObject());
        totalColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotal()).asObject());
        
        // Setup remove column
        setupRemoveFromCartColumn();
        
        // Refresh table
        cartTable.refresh();
    }

    private void setupRemoveFromCartColumn() {
        removeColumn.setCellFactory(param -> new TableCell<CartItem, Void>() {
            private final HBox container = new HBox(5);
            private final Button decrementButton = new Button("-");
            private final Button removeButton = new Button("×");

            {
                decrementButton.getStyleClass().clear();
                removeButton.getStyleClass().clear();
                decrementButton.getStyleClass().add("table-button-remove");
                removeButton.getStyleClass().add("table-button-remove");
                
                decrementButton.setOnAction(event -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        if (item.getQuantity() > 1) {
                            item.decrementQuantity();
                            cartTable.refresh();
                            updateTotalAmount();
                        } else {
                            cartItems.remove(item);
                            updateTotalAmount();
                        }
                    }
                });

                removeButton.setOnAction(event -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        cartItems.remove(item);
                        updateTotalAmount();
                    }
                });

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(decrementButton, removeButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
                setContentDisplay(ContentDisplay.CENTER);
            }
        });
    }

    private void setupFilterListener() {
        filterComboBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> loadMenu()
        );
    }

    private void loadMenu() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        // Run database operation in background thread
        new Thread(() -> {
            try {
                String category = filterComboBox.getValue();
                final List<FoodItem> items;

                if (category == null || category.equals("All")) {
                    items = foodItemDAO.getAvailableItems(); // Only get available items
                } else {
                    items = foodItemDAO.getByCategory(category);
                    // Filter to only available items
                    items.removeIf(item -> !item.isAvailable() || item.getStockQuantity() <= 0);
                }

                // Debug: Print loaded items and their stock
                System.out.println("DEBUG: Loaded " + items.size() + " items from database:");
                for (FoodItem item : items) {
                    System.out.println("DEBUG: " + item.getName() + " - Stock: " + item.getStockQuantity());
                }

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        if (items == null || items.isEmpty()) {
                            showInformation("No Items", "No food items available in this category");
                            menuItemsGrid.getChildren().clear();
                        } else {
                            displayFoodItems(items);
                        }
                    } catch (Exception e) {
                        showError("Display Error", "Error displaying menu items: " + e.getMessage());
                    } finally {
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisible(false);
                        }
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    showError("Database Error", "Failed to load menu items: " + e.getMessage());
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
                e.printStackTrace();
            }
        }).start();
    }

    public void addToCart(FoodItem item) {
        // Check if item is available and has stock
        if (!item.isAvailable() || item.getStockQuantity() <= 0) {
            showError("Unavailable", item.getName() + " is currently out of stock.");
            return;
        }

        // Look for existing item in cart
        for (CartItem cartItem : cartItems) {
            if (cartItem.getFoodItemId() == item.getId()) {
                if (cartItem.getQuantity() < item.getStockQuantity()) {
                    cartItem.incrementQuantity();
                    cartTable.refresh();
                    updateTotalAmount();
                    showInformation("Added to Cart", item.getName() + " quantity increased!");
                } else {
                    showError("Stock Limit", "Cannot add more. Only " + item.getStockQuantity() + " available.");
                }
                return;
            }
        }
        
        // Add new item to cart
        cartItems.add(new CartItem(item));
        cartTable.refresh();
        updateTotalAmount();
        showInformation("Added to Cart", item.getName() + " added to your cart!");
    }

    private void updateTotalAmount() {
        totalAmount = cartItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
            .sum();
        
        // Update CartManager with proper type conversion
        ObservableList<Object> cartObjects = FXCollections.observableArrayList();
        cartObjects.addAll(cartItems);
        cartManager.setCanteenCartItems(cartObjects);
        cartManager.setCanteenTotalAmount(totalAmount);
        
        // Update UI
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(String.format("%.2f TK", totalAmount));
        }
    }

    @FXML
    protected void handlePlaceOrder() {
        if (cartItems.isEmpty()) {
            showError("Error", "Your cart is empty");
            return;
        }

        // Navigate to payment page instead of directly placing order
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/payment-view.fxml"));
            Parent root = loader.load();
            
            SimplePaymentController paymentController = loader.getController();
            paymentController.setCurrentUser(currentUser);
            paymentController.setCartItems(cartItems);
            
            Stage stage = (Stage) cartTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Payment - OneStopUIU");
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to open payment page: " + e.getMessage());
        }
    }

    @FXML
    protected void handleViewOrders() {
        loadView("my-orders.fxml");
    }

    @FXML
    protected void handleBack() {
        // Save cart state before navigating back (already done in updateTotalAmount)
        updateTotalAmount(); // Ensure cart is saved
        loadView("section-selector.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            // Debug: Check current user before navigation
            if (currentUser == null) {
                System.err.println("[CanteenView] Warning: currentUser is null when navigating to " + fxmlFile);
                showError("Session Error", "User session has expired. Please login again.");
                loadView("login.fxml");
                return;
            }
            
            URL fxmlUrl = getClass().getResource("/com/example/onestopuiu/" + fxmlFile);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file: " + fxmlFile);
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            // Create root and scene with screen dimensions
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            // Get the stage and set the new scene
            Stage stage = (Stage) filterComboBox.getScene().getWindow();
            stage.setScene(scene);
            
            // Configure stage properties
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.centerOnScreen();

            // Pass user data to the next controller
            Object controller = fxmlLoader.getController();
            if (controller instanceof CustomerBaseController) {
                System.out.println("[CanteenView] Passing user data to " + controller.getClass().getSimpleName() + 
                                 ": " + (currentUser != null ? currentUser.getUsername() : "null"));
                ((CustomerBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // Helper class for cart items
    private static class CartItem {
        private final int foodItemId;
        private final String name;
        private final double unitPrice;
        private int quantity;

        public CartItem(FoodItem item) {
            this.foodItemId = item.getId();
            this.name = item.getName();
            this.unitPrice = item.getPrice();
            this.quantity = 1;
        }

        public int getFoodItemId() { return foodItemId; }
        public String getName() { return name; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return unitPrice * quantity; }

        public void incrementQuantity() { quantity++; }
        public void decrementQuantity() { if (quantity > 1) quantity--; }
    }
} 
