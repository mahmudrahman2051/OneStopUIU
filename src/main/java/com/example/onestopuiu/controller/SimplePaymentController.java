package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.PaymentDAO;
import com.example.onestopuiu.dao.FoodOrderDAO;
import com.example.onestopuiu.model.Payment;
import com.example.onestopuiu.model.User;
import com.example.onestopuiu.model.FoodOrder;
import com.example.onestopuiu.model.FoodOrderItem;
import com.example.onestopuiu.util.CartManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class SimplePaymentController {

    @FXML private Label totalAmountLabel;
    @FXML private Button codButton;
    @FXML private Button pickupButton;
    @FXML private Button mobileButton;
    @FXML private Button cardButton;
    
    @FXML private VBox paymentForm;
    @FXML private VBox mobileFields;
    @FXML private VBox cardFields;
    
    @FXML private ComboBox<String> mobileProviderCombo;
    @FXML private TextField mobileNumberField;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardHolderNameField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private TextField deliveryAddressField;
    @FXML private TextField phoneNumberField;
    
    @FXML private Button backButton;
    @FXML private Button confirmButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;

    private PaymentDAO paymentDAO;
    private FoodOrderDAO foodOrderDAO;
    private double totalAmount;
    private User currentUser;
    private List<?> cartItems;
    private String selectedPaymentMethod = "";

    public void initialize() {
        paymentDAO = new PaymentDAO();
        foodOrderDAO = new FoodOrderDAO();
        
        // Initialize mobile providers
        mobileProviderCombo.getItems().addAll("bKash", "Nagad", "Rocket", "Upay");
        mobileProviderCombo.setValue("bKash");
        
        progressIndicator.setVisible(false);
        updatePaymentSummary();
    }

    @FXML
    private void handleCashOnDelivery() {
        selectedPaymentMethod = "CASH_ON_DELIVERY";
        showForm();
        hideAllPaymentFields();
        confirmButton.setText("Confirm Order (Cash on Delivery)");
        statusLabel.setText("� Cash on Delivery selected. Fill delivery details below.");
        statusLabel.setStyle("-fx-text-fill: #FFD700;");
    }

    @FXML
    private void handlePickupFromCanteen() {
        selectedPaymentMethod = "PICKUP_FROM_CANTEEN";
        showForm();
        hideAllPaymentFields();
        confirmButton.setText("Confirm Order (Pickup from Canteen)");
        statusLabel.setText("🏫 Pickup from Canteen selected. Fill contact details below.");
        statusLabel.setStyle("-fx-text-fill: #FFD700;");
        
        // For pickup, we only need contact number, no delivery address
        deliveryAddressField.setPromptText("Student ID or Name (for pickup identification)");
    }

    @FXML
    private void handleMobileBanking() {
        selectedPaymentMethod = "MOBILE_BANKING";
        showForm();
        hideAllPaymentFields();
        mobileFields.setVisible(true);
        mobileFields.setManaged(true);
        confirmButton.setText("Pay with Mobile Banking");
        statusLabel.setText("📱 Mobile Banking selected. Enter your details below.");
        statusLabel.setStyle("-fx-text-fill: #FFD700;");
    }

    @FXML
    private void handleCardPayment() {
        selectedPaymentMethod = "CARD";
        showForm();
        hideAllPaymentFields();
        cardFields.setVisible(true);
        cardFields.setManaged(true);
        confirmButton.setText("Pay with Card");
        statusLabel.setText("💳 Card Payment selected. Enter card details below.");
        statusLabel.setStyle("-fx-text-fill: #FFD700;");
    }

    private void showForm() {
        paymentForm.setVisible(true);
        paymentForm.setManaged(true);
        confirmButton.setVisible(true);
        confirmButton.setManaged(true);
    }

    private void hideAllPaymentFields() {
        mobileFields.setVisible(false);
        mobileFields.setManaged(false);
        cardFields.setVisible(false);
        cardFields.setManaged(false);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/canteen-view.fxml"));
            Parent root = loader.load();
            
            // Pass the current user to maintain session
            CanteenViewController canteenController = loader.getController();
            canteenController.initData(currentUser);
            
            // Clear the cart (in case user goes back without completing payment)
            CartManager.getInstance().clearCanteenCart();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Canteen - OneStopUIU");
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error going back to canteen");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleConfirmPayment() {
        // Validate fields based on payment method
        if (!validateFields()) {
            return;
        }

        // Show progress
        progressIndicator.setVisible(true);
        confirmButton.setDisable(true);
        statusLabel.setText("Processing your order...");
        statusLabel.setStyle("-fx-text-fill: #3498db;");

        // Create the order first, then process payment
        Task<Boolean> orderTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    // Create food order
                    FoodOrder order = new FoodOrder();
                    order.setUserId(currentUser.getId());
                    
                    List<FoodOrderItem> orderItems = new ArrayList<>();
                    for (Object item : cartItems) {
                        FoodOrderItem orderItem = new FoodOrderItem();
                        orderItem.setFoodItemId((Integer) item.getClass().getMethod("getFoodItemId").invoke(item));
                        orderItem.setQuantity((Integer) item.getClass().getMethod("getQuantity").invoke(item));
                        orderItem.setUnitPrice((Double) item.getClass().getMethod("getUnitPrice").invoke(item));
                        orderItems.add(orderItem);
                    }
                    order.setItems(orderItems);
                    
                    // Save the order
                    int orderId = foodOrderDAO.save(order);
                    
                    // Create payment record
                    Payment payment = new Payment();
                    payment.setOrderId(orderId);
                    payment.setPaymentMethod(selectedPaymentMethod);
                    payment.setAmount(totalAmount);
                    
                    // Set additional details based on method
                    if ("CARD".equals(selectedPaymentMethod)) {
                        String cardNum = cardNumberField.getText().replaceAll("\\s", "");
                        payment.setCardNumber(cardNum.substring(Math.max(0, cardNum.length() - 4)));
                    } else if ("MOBILE_BANKING".equals(selectedPaymentMethod)) {
                        payment.setMobileNumber(mobileNumberField.getText());
                    }
                    payment.setBillingAddress(deliveryAddressField.getText());
                    
                    // Save payment and simulate processing
                    paymentDAO.createPayment(payment);
                    return paymentDAO.processPayment(payment);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        orderTask.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            confirmButton.setDisable(false);
            
            Boolean success = orderTask.getValue();
            if (success) {
                // Clear the cart after successful payment
                CartManager.getInstance().clearCanteenCart();
                
                statusLabel.setText("✅ Order confirmed! Redirecting...");
                statusLabel.setStyle("-fx-text-fill: #27ae60;");
                
                // Redirect back to canteen after 2 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/canteen-view.fxml"));
                                Parent root = loader.load();
                                
                                // Pass the current user to maintain session
                                CanteenViewController canteenController = loader.getController();
                                canteenController.initData(currentUser);
                                
                                // Clear the cart to ensure fresh start
                                CartManager.getInstance().clearCanteenCart();
                                
                                Stage stage = (Stage) confirmButton.getScene().getWindow();
                                Scene scene = new Scene(root);
                                stage.setScene(scene);
                                stage.setTitle("Canteen - OneStopUIU");
                                stage.show();
                                
                            } catch (IOException ioEx) {
                                ioEx.printStackTrace();
                            }
                        });
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
                
            } else {
                statusLabel.setText("❌ Payment failed. Please try again.");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        });

        orderTask.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            confirmButton.setDisable(false);
            statusLabel.setText("❌ Order failed. Please try again.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        });

        Thread orderThread = new Thread(orderTask);
        orderThread.setDaemon(true);
        orderThread.start();
    }

    private boolean validateFields() {
        // Contact number is required for all methods
        if (phoneNumberField.getText().trim().isEmpty()) {
            showError("Please enter contact number");
            return false;
        }

        // Address/ID field validation
        if ("PICKUP_FROM_CANTEEN".equals(selectedPaymentMethod)) {
            if (deliveryAddressField.getText().trim().isEmpty()) {
                showError("Please enter Student ID or Name for pickup identification");
                return false;
            }
        } else {
            // For delivery orders, address is required
            if (deliveryAddressField.getText().trim().isEmpty()) {
                showError("Please enter delivery address");
                return false;
            }
        }

        // Payment method specific validation
        if ("MOBILE_BANKING".equals(selectedPaymentMethod)) {
            if (mobileNumberField.getText().trim().isEmpty()) {
                showError("Please enter mobile number");
                return false;
            }
            if (!mobileNumberField.getText().matches("01[0-9]{9}")) {
                showError("Please enter valid mobile number (01XXXXXXXXX)");
                return false;
            }
        } else if ("CARD".equals(selectedPaymentMethod)) {
            if (cardNumberField.getText().trim().isEmpty()) {
                showError("Please enter card number");
                return false;
            }
            if (cardHolderNameField.getText().trim().isEmpty()) {
                showError("Please enter cardholder name");
                return false;
            }
            if (expiryDateField.getText().trim().isEmpty()) {
                showError("Please enter expiry date");
                return false;
            }
            if (cvvField.getText().trim().isEmpty()) {
                showError("Please enter CVV");
                return false;
            }
        }

        return true;
    }

    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    @FXML
    private void formatCardNumber() {
        String text = cardNumberField.getText().replaceAll("\\s+", "");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < text.length() && i < 16; i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(text.charAt(i));
        }
        
        cardNumberField.setText(formatted.toString());
        cardNumberField.positionCaret(formatted.length());
    }

    @FXML
    private void formatExpiryDate() {
        String text = expiryDateField.getText().replaceAll("/", "");
        if (text.length() >= 2) {
            text = text.substring(0, 2) + "/" + text.substring(2);
        }
        if (text.length() > 5) {
            text = text.substring(0, 5);
        }
        expiryDateField.setText(text);
        expiryDateField.positionCaret(text.length());
    }
    
    // Setter methods for integration
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public void setCartItems(List<?> items) {
        this.cartItems = items;
        updatePaymentSummary();
    }
    
    private void updatePaymentSummary() {
        double subtotal = 0.0;
        if (cartItems != null) {
            for (Object item : cartItems) {
                try {
                    double price = (Double) item.getClass().getMethod("getUnitPrice").invoke(item);
                    int quantity = (Integer) item.getClass().getMethod("getQuantity").invoke(item);
                    subtotal += price * quantity;
                } catch (Exception e) {
                    System.err.println("Could not calculate item total: " + e.getMessage());
                }
            }
        }
        
        // No delivery fee for pickup orders
        double deliveryFee = "PICKUP_FROM_CANTEEN".equals(selectedPaymentMethod) ? 0.0 : 50.0;
        this.totalAmount = subtotal + deliveryFee;
        
        if (totalAmountLabel != null) {
            String deliveryText = "PICKUP_FROM_CANTEEN".equals(selectedPaymentMethod) ? " (Pickup - No Delivery Fee)" : "";
            totalAmountLabel.setText(String.format("Total: ৳%.2f%s", totalAmount, deliveryText));
        }
    }
}