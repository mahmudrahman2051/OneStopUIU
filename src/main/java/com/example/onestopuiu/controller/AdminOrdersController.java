package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.FoodOrderDAO;
import com.example.onestopuiu.dao.PaymentDAO;
import com.example.onestopuiu.dao.UserDAO;
import com.example.onestopuiu.model.FoodOrder;
import com.example.onestopuiu.model.FoodOrderItem;
import com.example.onestopuiu.model.Payment;
import com.example.onestopuiu.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminOrdersController {

    @FXML private Button backButton;
    @FXML private Button refreshButton;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private Button applyFilterButton;
    @FXML private TextField customerSearchField;
    
    @FXML private TableView<OrderDisplayItem> ordersTable;
    @FXML private TableColumn<OrderDisplayItem, String> orderIdColumn;
    @FXML private TableColumn<OrderDisplayItem, String> customerColumn;
    @FXML private TableColumn<OrderDisplayItem, String> itemsColumn;
    @FXML private TableColumn<OrderDisplayItem, String> totalColumn;
    @FXML private TableColumn<OrderDisplayItem, String> paymentMethodColumn;
    @FXML private TableColumn<OrderDisplayItem, String> statusColumn;
    @FXML private TableColumn<OrderDisplayItem, String> typeColumn;
    @FXML private TableColumn<OrderDisplayItem, String> contactColumn;
    @FXML private TableColumn<OrderDisplayItem, String> addressColumn;
    @FXML private TableColumn<OrderDisplayItem, String> orderDateColumn;
    @FXML private TableColumn<OrderDisplayItem, String> actionsColumn;
    
    @FXML private VBox orderDetailsPanel;
    @FXML private VBox orderDetailsContent;
    @FXML private Button markReadyButton;
    @FXML private Button markCompletedButton;
    @FXML private Button cancelOrderButton;
    @FXML private Button hideDetailsButton;

    private FoodOrderDAO orderDAO;
    private PaymentDAO paymentDAO;
    private UserDAO userDAO;
    private ObservableList<OrderDisplayItem> ordersList;
    private OrderDisplayItem selectedOrder;

    public void initialize() {
        orderDAO = new FoodOrderDAO();
        paymentDAO = new PaymentDAO();
        userDAO = new UserDAO();
        ordersList = FXCollections.observableArrayList();
        
        setupTable();
        setupFilters();
        loadOrders();
    }

    private void setupTable() {
        orderIdColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().orderId)));
        customerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().customerName));
        itemsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().itemsSummary));
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty("৳" + String.format("%.2f", data.getValue().totalAmount)));
        paymentMethodColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().paymentMethod));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().orderType));
        contactColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().contactNumber));
        addressColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().address));
        orderDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().orderDate));
        
        // Actions column with View Details, Edit, and Delete Customer buttons
        actionsColumn.setCellFactory(column -> new TableCell<OrderDisplayItem, String>() {
            private final Button viewButton = new Button("👁");
            private final Button editButton = new Button("✏");
            private final Button deleteCustomerButton = new Button("🗑");
            private final HBox buttonBox = new HBox(3, viewButton, editButton, deleteCustomerButton);
            
            {
                viewButton.setOnAction(event -> {
                    OrderDisplayItem order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });
                
                editButton.setOnAction(event -> {
                    OrderDisplayItem order = getTableView().getItems().get(getIndex());
                    handleEditOrder(order);
                });
                
                deleteCustomerButton.setOnAction(event -> {
                    OrderDisplayItem order = getTableView().getItems().get(getIndex());
                    handleDeleteCustomer(order);
                });
                
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-pref-width: 30;");
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-pref-width: 30;");
                deleteCustomerButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-pref-width: 30;");
                
                viewButton.setTooltip(new Tooltip("View Details"));
                editButton.setTooltip(new Tooltip("Edit Order"));
                deleteCustomerButton.setTooltip(new Tooltip("Delete Customer"));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
        
        ordersTable.setItems(ordersList);
    }

    private void setupFilters() {
        statusFilterCombo.getItems().addAll("All Orders", "PENDING", "IN_PROGRESS", "READY", "COMPLETED", "CANCELLED");
        statusFilterCombo.setValue("All Orders");
        
        typeFilterCombo.getItems().addAll("All Types", "CASH_ON_DELIVERY", "PICKUP_FROM_CANTEEN", "MOBILE_BANKING", "CARD");
        typeFilterCombo.setValue("All Types");
    }

    private void loadOrders() {
        try {
            ordersList.clear();
            List<FoodOrder> orders = orderDAO.getAll();
            
            for (FoodOrder order : orders) {
                OrderDisplayItem displayItem = new OrderDisplayItem();
                displayItem.orderId = order.getId();
                displayItem.status = order.getStatus();
                displayItem.totalAmount = order.getTotal();
                displayItem.orderDate = order.getOrderTime().toString();
                
                // Get customer info
                Optional<User> userOpt = userDAO.get(order.getUserId());
                displayItem.customerName = userOpt.map(User::getUsername).orElse("Unknown");
                
                // Get payment info
                List<Payment> payments = paymentDAO.getPaymentsByOrderId(order.getId());
                if (!payments.isEmpty()) {
                    Payment payment = payments.get(0);
                    displayItem.paymentMethod = payment.getPaymentMethod();
                    displayItem.contactNumber = getContactFromPayment(payment);
                    displayItem.address = getAddressFromPayment(payment);
                    displayItem.orderType = getOrderTypeFromPayment(payment);
                } else {
                    displayItem.paymentMethod = "N/A";
                    displayItem.contactNumber = "N/A";
                    displayItem.address = "N/A";
                    displayItem.orderType = "N/A";
                }
                
                // Create items summary
                StringBuilder itemsBuilder = new StringBuilder();
                for (FoodOrderItem item : order.getItems()) {
                    if (itemsBuilder.length() > 0) itemsBuilder.append(", ");
                    String itemName = item.getFoodItemName() != null ? item.getFoodItemName() : "Item #" + item.getFoodItemId();
                    itemsBuilder.append(item.getQuantity()).append("x ").append(itemName);
                }
                displayItem.itemsSummary = itemsBuilder.toString();
                
                ordersList.add(displayItem);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load orders: " + e.getMessage());
        }
    }

    private String getContactFromPayment(Payment payment) {
        if (payment.getMobileNumber() != null && !payment.getMobileNumber().trim().isEmpty()) {
            return payment.getMobileNumber();
        }
        return "N/A";
    }

    private String getAddressFromPayment(Payment payment) {
        if (payment.getBillingAddress() != null && !payment.getBillingAddress().trim().isEmpty()) {
            return payment.getBillingAddress();
        }
        return "N/A";
    }

    private String getOrderTypeFromPayment(Payment payment) {
        String method = payment.getPaymentMethod();
        if ("CASH_ON_DELIVERY".equals(method)) {
            return "🚚 Delivery";
        } else if ("PICKUP_FROM_CANTEEN".equals(method)) {
            return "🏫 Pickup";
        } else {
            return "💳 " + method;
        }
    }

    private void showOrderDetails(OrderDisplayItem order) {
        selectedOrder = order;
        orderDetailsContent.getChildren().clear();
        
        // Add order details
        addDetailRow("Order ID:", String.valueOf(order.orderId));
        addDetailRow("Customer:", order.customerName);
        addDetailRow("Items:", order.itemsSummary);
        addDetailRow("Total Amount:", "৳" + String.format("%.2f", order.totalAmount));
        addDetailRow("Payment Method:", order.paymentMethod);
        addDetailRow("Order Type:", order.orderType);
        addDetailRow("Contact Number:", order.contactNumber);
        addDetailRow("Address/Info:", order.address);
        addDetailRow("Status:", order.status);
        addDetailRow("Order Date:", order.orderDate);
        
        orderDetailsPanel.setVisible(true);
    }

    private void addDetailRow(String label, String value) {
        Label detailLabel = new Label(label + " " + value);
        detailLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5 0;");
        if (label.contains("Status") && value.equals("PENDING")) {
            detailLabel.setStyle(detailLabel.getStyle() + " -fx-text-fill: #f39c12;");
        } else if (label.contains("Status") && value.equals("COMPLETED")) {
            detailLabel.setStyle(detailLabel.getStyle() + " -fx-text-fill: #27ae60;");
        }
        orderDetailsContent.getChildren().add(detailLabel);
    }

    private void handleEditOrder(OrderDisplayItem order) {
        // Simple edit functionality for order status
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Order Status");
        dialog.setHeaderText("Edit status for Order #" + order.orderId);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("PENDING", "IN_PROGRESS", "READY", "COMPLETED", "CANCELLED");
        statusField.setValue(order.status);

        dialog.getDialogPane().setContent(statusField);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return statusField.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStatus -> {
            try {
                orderDAO.updateOrderStatus(order.orderId, newStatus);
                loadOrders();
                showAlert("Success", "Order status updated successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to update order status: " + e.getMessage());
            }
        });
    }

    private void handleDeleteCustomer(OrderDisplayItem order) {
        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle("⚠️ Delete Customer Data");
        confirmAlert.setHeaderText("Delete ALL data for customer: " + order.customerName);
        
        ButtonType deleteAllButton = new ButtonType("🗑️ Delete Everything", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteOrderOnlyButton = new ButtonType("📦 Delete This Order Only", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButton = new ButtonType("❌ Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        confirmAlert.getButtonTypes().setAll(deleteAllButton, deleteOrderOnlyButton, cancelButton);
        
        confirmAlert.setContentText("Choose deletion type for customer: " + order.customerName + "\n\n" +
                                   "🗑️ DELETE EVERYTHING will permanently remove:\n" +
                                   "   • Customer account (" + order.customerName + ")\n" +
                                   "   • ALL orders by this customer\n" +
                                   "   • ALL payment records\n\n" +
                                   "📦 DELETE THIS ORDER ONLY will remove:\n" +
                                   "   • Only Order #" + order.orderId + "\n" +
                                   "   • Associated payment record\n\n" +
                                   "⚠️ WARNING: These actions CANNOT be undone!");
        
        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == deleteAllButton) {
                deleteCustomerCompletely(order.customerName);
            } else if (buttonType == deleteOrderOnlyButton) {
                deleteOrderOnly(order);
            }
        });
    }

    private void deleteCustomerCompletely(String customerName) {
        try {
            Optional<User> userOpt = userDAO.findByUsername(customerName);
            if (!userOpt.isPresent()) {
                showAlert("Error", "Customer not found: " + customerName);
                return;
            }
            
            int userId = userOpt.get().getId();
            List<FoodOrder> customerOrders = orderDAO.getByUserId(userId);
            
            // Delete orders one by one (safer approach)
            int deletedOrders = 0;
            for (FoodOrder order : customerOrders) {
                try {
                    // Delete payment records first
                    List<Payment> payments = paymentDAO.getPaymentsByOrderId(order.getId());
                    for (Payment payment : payments) {
                        paymentDAO.deletePayment(payment.getPaymentId());
                    }
                    
                    // Delete the order
                    orderDAO.safeDeleteOrder(order.getId());
                    deletedOrders++;
                } catch (Exception e) {
                    System.err.println("Error deleting order " + order.getId() + ": " + e.getMessage());
                }
            }
            
            // Delete user account
            try {
                userDAO.delete(userId);
                showAlert("Deletion Successful", 
                         "Customer '" + customerName + "' has been deleted:\n\n" +
                         "✅ Deleted " + deletedOrders + " orders\n" +
                         "✅ Deleted payment records\n" +
                         "✅ Deleted user account");
            } catch (Exception e) {
                showAlert("Partial Deletion", 
                         "Deleted " + deletedOrders + " orders but failed to delete user account.");
            }
            
            loadOrders();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Deletion Error", "Failed to delete customer: " + e.getMessage());
        }
    }

    private void deleteOrderOnly(OrderDisplayItem order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Order Deletion");
        confirmAlert.setHeaderText("Delete Order #" + order.orderId);
        confirmAlert.setContentText("Are you sure you want to delete this order?");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Delete payment records first
                List<Payment> payments = paymentDAO.getPaymentsByOrderId(order.orderId);
                for (Payment payment : payments) {
                    paymentDAO.deletePayment(payment.getPaymentId());
                }
                
                // Delete the order
                orderDAO.safeDeleteOrder(order.orderId);
                
                showAlert("Order Deleted", "Order #" + order.orderId + " has been successfully deleted.");
                loadOrders();
                
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Deletion Error", "Failed to delete order: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/admin-dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard - OneStopUIU");
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
    }

    @FXML
    private void handleApplyFilter() {
        String statusFilter = statusFilterCombo.getValue();
        String typeFilter = typeFilterCombo.getValue();
        
        ObservableList<OrderDisplayItem> filteredList = FXCollections.observableArrayList();
        
        for (OrderDisplayItem order : ordersList) {
            boolean statusMatch = "All Orders".equals(statusFilter) || order.status.equals(statusFilter);
            boolean typeMatch = "All Types".equals(typeFilter) || order.paymentMethod.equals(typeFilter);
            
            if (statusMatch && typeMatch) {
                filteredList.add(order);
            }
        }
        
        ordersTable.setItems(filteredList);
    }

    @FXML
    private void handleSearchCustomer() {
        String searchTerm = customerSearchField.getText().toLowerCase().trim();
        
        if (searchTerm.isEmpty()) {
            ordersTable.setItems(ordersList);
            return;
        }
        
        ObservableList<OrderDisplayItem> filteredList = FXCollections.observableArrayList();
        
        for (OrderDisplayItem order : ordersList) {
            if (order.customerName.toLowerCase().contains(searchTerm)) {
                filteredList.add(order);
            }
        }
        
        ordersTable.setItems(filteredList);
        
        if (filteredList.isEmpty()) {
            showAlert("No Results", "No orders found for customer containing: " + searchTerm);
        }
    }

    @FXML
    private void handleDeleteSelectedCustomer() {
        OrderDisplayItem selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("No Selection", "Please select an order to delete the associated customer.");
            return;
        }

        deleteCustomerCompletely(selectedOrder.customerName);
    }

    @FXML
    private void handleAddPaymentRecord() {
        // Create a dialog for adding payment records
        Dialog<Payment> dialog = new Dialog<>();
        dialog.setTitle("Add Payment Record");
        dialog.setHeaderText("Add a new payment record");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField orderIdField = new TextField();
        orderIdField.setPromptText("Order ID");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        ComboBox<String> methodField = new ComboBox<>();
        methodField.getItems().addAll("CASH_ON_DELIVERY", "PICKUP_FROM_CANTEEN", "MOBILE_BANKING", "CARD");
        methodField.setPromptText("Payment Method");
        TextField mobileField = new TextField();
        mobileField.setPromptText("Mobile Number");
        TextField addressField = new TextField();
        addressField.setPromptText("Billing Address");

        grid.add(new Label("Order ID:"), 0, 0);
        grid.add(orderIdField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Payment Method:"), 0, 2);
        grid.add(methodField, 1, 2);
        grid.add(new Label("Mobile Number:"), 0, 3);
        grid.add(mobileField, 1, 3);
        grid.add(new Label("Billing Address:"), 0, 4);
        grid.add(addressField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Payment payment = new Payment();
                    payment.setOrderId(Integer.parseInt(orderIdField.getText()));
                    payment.setAmount(Double.parseDouble(amountField.getText()));
                    payment.setPaymentMethod(methodField.getValue());
                    payment.setMobileNumber(mobileField.getText());
                    payment.setBillingAddress(addressField.getText());
                    return payment;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers for Order ID and Amount.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(payment -> {
            try {
                // Here you would typically save to database using PaymentDAO
                // For now, just show success message
                showAlert("Success", "Payment record would be added for Order #" + payment.getOrderId());
                loadOrders(); // Refresh the orders list
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to add payment record: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleMarkReady() {
        if (selectedOrder != null) {
            updateOrderStatus(selectedOrder.orderId, "READY");
        }
    }

    @FXML
    private void handleMarkCompleted() {
        if (selectedOrder != null) {
            updateOrderStatus(selectedOrder.orderId, "COMPLETED");
        }
    }

    @FXML
    private void handleCancelOrder() {
        if (selectedOrder != null) {
            updateOrderStatus(selectedOrder.orderId, "CANCELLED");
        }
    }

    @FXML
    private void handleHideDetails() {
        orderDetailsPanel.setVisible(false);
        selectedOrder = null;
    }

    private void updateOrderStatus(int orderId, String newStatus) {
        try {
            orderDAO.updateOrderStatus(orderId, newStatus);
            loadOrders();
            orderDetailsPanel.setVisible(false);
            showAlert("Success", "Order status updated to " + newStatus);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update order status: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper class for table display
    public static class OrderDisplayItem {
        public int orderId;
        public String customerName;
        public String itemsSummary;
        public double totalAmount;
        public String paymentMethod;
        public String status;
        public String orderType;
        public String contactNumber;
        public String address;
        public String orderDate;
    }
}