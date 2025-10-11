package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.FoodItemDAO;
import com.example.onestopuiu.model.FoodItem;
import com.example.onestopuiu.util.LocalImageUploader;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodItemsManagerController extends AdminBaseController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockQuantityField;
    @FXML private TextField restockQuantityField;
    @FXML private Button quickRestockButton;
    @FXML private Label restockStatusLabel;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private CheckBox availableCheckBox;
    @FXML private Button uploadImageButton;
    @FXML private Label imagePathLabel;
    @FXML private TextField imagePathField;
    @FXML private ImageView imagePreview;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TableView<FoodItem> foodItemsTable;
    @FXML private TableColumn<FoodItem, Integer> idColumn;
    @FXML private TableColumn<FoodItem, String> nameColumn;
    @FXML private TableColumn<FoodItem, Double> priceColumn;
    @FXML private TableColumn<FoodItem, String> categoryColumn;
    @FXML private TableColumn<FoodItem, Boolean> availableColumn;
    @FXML private TableColumn<FoodItem, Integer> stockQuantityColumn;
    @FXML private TableColumn<FoodItem, Void> actionsColumn;

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();
    private FoodItem selectedItem;
    private String selectedImagePath;
    // Upload directory - commented out as not currently used
    // private static final String UPLOAD_DIR = "src/main/resources/com/example/onestopuiu/uploads/";

    @Override
    protected void onInitialize() {
        setupTable();
        loadFoodItems();
        setupFilterListener();
        setupImageUpload();
        setupRestockFeature();
        
        // Initialize stock quantity column
        stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
    }

    private void setupRestockFeature() {
        // Clear restock status when typing
        restockQuantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            restockStatusLabel.setText("");
        });
        
        // Enable/disable quick restock button based on selection and input
        quickRestockButton.setDisable(true);
        restockQuantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            quickRestockButton.setDisable(selectedItem == null || newVal.trim().isEmpty());
        });
    }

    private void setupImageUpload() {
        uploadImageButton.setOnAction(event -> {
            handleImageBrowse();
        });
    }

    @FXML
    private void handleImageBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                loadingIndicator.setVisible(true);
                
                // Upload to local directory
                String imagePath = LocalImageUploader.uploadImage(selectedFile);
                selectedImagePath = imagePath;
                imagePathField.setText(selectedFile.getName());
                imagePathLabel.setText("Image uploaded successfully");
                
                // Preview the image using local file path
                String localPath = LocalImageUploader.getLocalPath(imagePath);
                File imageFile = new File(localPath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imagePreview.setImage(image);
                } else {
                    imagePathLabel.setText("Warning: Image uploaded but preview failed");
                }
                
            } catch (IOException e) {
                showError("File Upload Error", "Failed to upload image: " + e.getMessage());
                selectedImagePath = null;
                imagePathField.setText("");
                imagePathLabel.setText("Upload failed");
            } finally {
                loadingIndicator.setVisible(false);
            }
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        priceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        availableColumn.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isAvailable()));
        
        // Center align all columns
        idColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        nameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        // Format price column and center align
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("৳%.2f", price));
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        categoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        stockQuantityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        availableColumn.setCellFactory(column -> new TableCell<FoodItem, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.setDisable(false);
                checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        FoodItem item = getTableRow().getItem();
                        item.setAvailable(newVal);
                        try {
                            foodItemDAO.update(item);
                            loadFoodItems(); // Refresh the table
                        } catch (SQLException e) {
                            showError("Database Error", "Failed to update availability: " + e.getMessage());
                            checkBox.setSelected(oldVal); // Revert on error
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(available != null && available);
                    setGraphic(checkBox);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button restockButton = new Button("Restock");
            private final HBox container = new HBox(5, editButton, restockButton, deleteButton);

            {
                // Style buttons
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                restockButton.getStyleClass().add("restock-button");
                container.setStyle("-fx-alignment: center;");

                editButton.setOnAction(event -> {
                    FoodItem item = getTableRow().getItem();
                    if (item != null) {
                        populateForm(item);
                    }
                });

                restockButton.setOnAction(event -> {
                    FoodItem item = getTableRow().getItem();
                    if (item != null) {
                        showRestockDialog(item);
                    }
                });

                deleteButton.setOnAction(event -> {
                    FoodItem item = getTableRow().getItem();
                    if (item != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Delete Item");
                        alert.setHeaderText(null);
                        alert.setContentText("Are you sure you want to delete " + item.getName() + "?");
                        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            try {
                                foodItemDAO.delete(item.getId());
                                loadFoodItems();
                                clearForm(); // Clear form if the deleted item was being edited
                            } catch (SQLException e) {
                                showError("Database Error", "Failed to delete item: " + e.getMessage());
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void showRestockDialog(FoodItem item) {
        // Create a custom dialog for restocking
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Restock Item");
        dialog.setHeaderText("Restock: " + item.getName());
        dialog.setContentText("Current stock: " + item.getStockQuantity());

        // Create the dialog content
        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity to add");
        quantityField.setPrefWidth(200);
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Current stock: " + item.getStockQuantity()),
            new Label("Add quantity:"),
            quantityField
        );
        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType restockButtonType = new ButtonType("Restock", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(restockButtonType, ButtonType.CANCEL);

        // Enable/disable restock button based on input
        Button restockButton = (Button) dialog.getDialogPane().lookupButton(restockButtonType);
        restockButton.setDisable(true);

        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int quantity = Integer.parseInt(newValue.trim());
                restockButton.setDisable(quantity <= 0);
            } catch (NumberFormatException e) {
                restockButton.setDisable(true);
            }
        });

        // Convert result when restock button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == restockButtonType) {
                try {
                    return Integer.parseInt(quantityField.getText().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        // Show dialog and process result
        dialog.showAndWait().ifPresent(additionalStock -> {
            try {
                int currentStock = item.getStockQuantity();
                int newStock = currentStock + additionalStock;
                
                // Update the item
                item.setStockQuantity(newStock);
                
                // If item was unavailable due to 0 stock, make it available again
                if (currentStock == 0) {
                    item.setAvailable(true);
                }

                // Update in database
                foodItemDAO.update(item);
                
                // Refresh table and form if this item is selected
                loadFoodItems();
                if (selectedItem != null && selectedItem.getId() == item.getId()) {
                    populateForm(item);
                }
                
                showInformation("Restock Successful", 
                    item.getName() + " has been restocked!\n" +
                    "Previous stock: " + currentStock + "\n" +
                    "Added: " + additionalStock + "\n" +
                    "New stock: " + newStock);

            } catch (SQLException e) {
                showError("Database Error", "Failed to update stock: " + e.getMessage());
            }
        });
    }

    private void setupFilterListener() {
        filterComboBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> loadFoodItems()
        );
    }

    private void loadFoodItems() {
        String category = filterComboBox.getValue();
        List<FoodItem> items = new ArrayList<>();
        
        try {
            if (category == null || category.equals("All")) {
                items = foodItemDAO.getAll();
            } else {
                items = foodItemDAO.getByCategory(category);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load food items: " + e.getMessage());
        }
        
        foodItemsTable.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    protected void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            FoodItem item = selectedItem != null ? selectedItem : new FoodItem();
            item.setName(nameField.getText().trim());
            item.setPrice(Double.parseDouble(priceField.getText().trim()));
            int stock = Integer.parseInt(stockQuantityField.getText().trim());
            item.setStockQuantity(stock);
            item.setDescription(descriptionField.getText().trim());
            item.setCategory(categoryComboBox.getValue());
            // Set available: auto-false if stock is 0, else use checkbox
            if (stock == 0) {
                item.setAvailable(false);
            } else {
                item.setAvailable(availableCheckBox.isSelected());
            }
            if (selectedImagePath == null && selectedItem != null) {
                item.setImage(selectedItem.getImage());
            } else {
                item.setImage(selectedImagePath);
            }

            if (selectedItem == null) {
                foodItemDAO.save(item);
                showInformation("Success", "Food item added successfully!");
            } else {
                foodItemDAO.update(item);
                showInformation("Success", "Food item updated successfully!");
            }

            loadFoodItems();
            clearForm();
        } catch (SQLException e) {
            showError("Database Error", "Failed to save food item: " + e.getMessage());
        }
    }

    @FXML
    protected void handleQuickRestock() {
        if (selectedItem == null) {
            showError("No Item Selected", "Please select an item from the table to restock.");
            return;
        }

        try {
            int additionalStock = Integer.parseInt(restockQuantityField.getText().trim());
            if (additionalStock <= 0) {
                showError("Invalid Quantity", "Restock quantity must be greater than 0.");
                return;
            }

            // Get current stock and add the additional stock
            int currentStock = selectedItem.getStockQuantity();
            int newStock = currentStock + additionalStock;
            
            // Update the selected item
            selectedItem.setStockQuantity(newStock);
            
            // If item was unavailable due to 0 stock, make it available again
            if (currentStock == 0) {
                selectedItem.setAvailable(true);
            }

            // Update in database
            foodItemDAO.update(selectedItem);
            
            // Update form fields
            stockQuantityField.setText(String.valueOf(newStock));
            availableCheckBox.setSelected(selectedItem.isAvailable());
            
            // Clear restock field and show success message
            restockQuantityField.clear();
            restockStatusLabel.setText("✅ Added " + additionalStock + " items. New stock: " + newStock);
            restockStatusLabel.setStyle("-fx-text-fill: green;");
            
            // Refresh table
            loadFoodItems();
            
            showInformation("Restock Successful", 
                selectedItem.getName() + " has been restocked!\n" +
                "Previous stock: " + currentStock + "\n" +
                "Added: " + additionalStock + "\n" +
                "New stock: " + newStock);

        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter a valid number for restock quantity.");
        } catch (SQLException e) {
            showError("Database Error", "Failed to update stock: " + e.getMessage());
            restockStatusLabel.setText("❌ Restock failed");
            restockStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void handleClear() {
        clearForm();
    }

    @FXML
    protected void handleBack() {
        loadView("admin-dashboard.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
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
                ((CustomerBaseController) controller).initData(currentUser);
            } else if (controller instanceof AdminBaseController) {
                ((AdminBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }

    private void populateForm(FoodItem item) {
        selectedItem = item;
        nameField.setText(item.getName());
        priceField.setText(String.valueOf(item.getPrice()));
        stockQuantityField.setText(String.valueOf(item.getStockQuantity()));
        descriptionField.setText(item.getDescription());
        categoryComboBox.setValue(item.getCategory());
        availableCheckBox.setSelected(item.isAvailable());
        selectedImagePath = item.getImagePath();
        
        // Handle image display
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            // Extract filename for display
            String fileName = selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1);
            imagePathField.setText(fileName);
            imagePathLabel.setText("Current image: " + fileName);
            
            // Try to load image preview
            try {
                if (selectedImagePath.startsWith("/com/example/onestopuiu/uploads/")) {
                    // Local image - use local file path
                    String localPath = LocalImageUploader.getLocalPath(selectedImagePath);
                    File imageFile = new File(localPath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        imagePreview.setImage(image);
                    } else {
                        imagePreview.setImage(null);
                        imagePathLabel.setText("Image file not found: " + fileName);
                    }
                } else {
                    // External URL - try to load directly
                    Image image = new Image(selectedImagePath);
                    imagePreview.setImage(image);
                }
            } catch (Exception e) {
                imagePreview.setImage(null);
                imagePathLabel.setText("Could not load image preview: " + fileName);
            }
        } else {
            imagePathField.setText("");
            imagePathLabel.setText("No image selected");
            imagePreview.setImage(null);
        }
        
        // Clear restock status and enable/disable quick restock based on input
        restockStatusLabel.setText("");
        quickRestockButton.setDisable(restockQuantityField.getText().trim().isEmpty());
    }

    private void clearForm() {
        selectedItem = null;
        nameField.clear();
        priceField.clear();
        stockQuantityField.clear();
        descriptionField.clear();
        categoryComboBox.setValue(null);
        availableCheckBox.setSelected(false);
        selectedImagePath = null;
        imagePathField.clear();
        imagePathLabel.setText("No image selected");
        imagePreview.setImage(null);
        
        // Clear restock fields
        restockQuantityField.clear();
        restockStatusLabel.setText("");
        quickRestockButton.setDisable(true);
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();

        if (nameField.getText().trim().isEmpty()) {
            errors.add("Name is required");
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                errors.add("Price must be greater than 0");
            }
        } catch (NumberFormatException e) {
            errors.add("Price must be a valid number");
        }

        try {
            int stock = Integer.parseInt(stockQuantityField.getText().trim());
            if (stock < 0) {
                errors.add("Stock quantity cannot be negative");
            }
        } catch (NumberFormatException e) {
            errors.add("Stock quantity must be a valid number");
        }

        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().trim().isEmpty()) {
            errors.add("Category is required");
        }

        if (!errors.isEmpty()) {
            showError("Validation Error", String.join("\n", errors));
            return false;
        }

        return true;
    }
} 
