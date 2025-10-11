package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.PaymentDAO;
import com.example.onestopuiu.model.Payment;
import com.example.onestopuiu.model.User;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;

public class PaymentController {

    @FXML private Label totalAmountLabel;
    @FXML private RadioButton cardPaymentRadio;
    @FXML private RadioButton mobileBankingRadio;
    @FXML private RadioButton cashOnDeliveryRadio;
    @FXML private ToggleGroup paymentMethodGroup;
    
    // Card Payment Fields
    @FXML private VBox cardPaymentFields;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardHolderNameField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private TextArea billingAddressField;
    
    // Mobile Banking Fields
    @FXML private VBox mobileBankingFields;
    @FXML private ComboBox<String> mobileProviderCombo;
    @FXML private TextField mobileNumberField;
    @FXML private PasswordField mobilePinField;
    
    // Cash on Delivery Fields
    @FXML private VBox codFields;
    @FXML private TextArea deliveryAddressField;
    @FXML private TextField phoneNumberField;
    
    @FXML private Button payNowButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label deliveryFeeLabel;
    @FXML private Label totalSummaryLabel;

    private PaymentDAO paymentDAO;
    private double totalAmount;
    private int orderId;
    private int customerId;
    private User currentUser;
    private List<?> cartItems;

    // Simple CartItem class for payment processing
    public static class CartItem {
        private int foodItemId;
        private String name;
        private double price;
        private int quantity;

        public CartItem(int foodItemId, String name, double price, int quantity) {
            this.foodItemId = foodItemId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public int getFoodItemId() { return foodItemId; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }

    public void initialize() {
        paymentDAO = new PaymentDAO();
        
        // Initialize payment method selection
        paymentMethodGroup = new ToggleGroup();
        cardPaymentRadio.setToggleGroup(paymentMethodGroup);
        mobileBankingRadio.setToggleGroup(paymentMethodGroup);
        cashOnDeliveryRadio.setToggleGroup(paymentMethodGroup);
        
        // Set default selection
        cardPaymentRadio.setSelected(true);
        showCardPaymentFields();
        
        // Add listeners for payment method selection
        paymentMethodGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                hideAllPaymentFields();
                if (newVal == cardPaymentRadio) {
                    showCardPaymentFields();
                } else if (newVal == mobileBankingRadio) {
                    showMobileBankingFields();
                } else if (newVal == cashOnDeliveryRadio) {
                    showCodFields();
                }
            }
        });
        
        // Initialize mobile providers
        if (mobileProviderCombo != null) {
            mobileProviderCombo.getItems().addAll(
                "bKash", "Nagad", "Rocket", "Upay", "SureCash"
            );
            mobileProviderCombo.setValue("bKash");
        }
        
        progressIndicator.setVisible(false);
    }

    public void setPaymentDetails(double amount, int orderId, int customerId) {
        this.totalAmount = amount;
        this.orderId = orderId;
        this.customerId = customerId;
        totalAmountLabel.setText(String.format("Total Amount: ৳%.2f", amount));
    }

    private void hideAllPaymentFields() {
        if (cardPaymentFields != null) cardPaymentFields.setVisible(false);
        if (mobileBankingFields != null) mobileBankingFields.setVisible(false);
        if (codFields != null) codFields.setVisible(false);
    }

    private void showCardPaymentFields() {
        if (cardPaymentFields != null) cardPaymentFields.setVisible(true);
    }

    private void showMobileBankingFields() {
        if (mobileBankingFields != null) mobileBankingFields.setVisible(true);
    }

    private void showCodFields() {
        if (codFields != null) codFields.setVisible(true);
    }

    @FXML
    private void handlePayNow() {
        if (!validatePaymentInfo()) {
            return;
        }

        // Create payment object
        Payment payment = new Payment(orderId, customerId, totalAmount, getSelectedPaymentMethod());
        
        // Set payment-specific details
        if (cardPaymentRadio.isSelected()) {
            payment.setCardNumber(cardNumberField.getText());
            payment.setBillingAddress(billingAddressField.getText());
        } else if (mobileBankingRadio.isSelected()) {
            payment.setMobileNumber(mobileNumberField.getText());
        } else if (cashOnDeliveryRadio.isSelected()) {
            payment.setBillingAddress(deliveryAddressField.getText());
            payment.setMobileNumber(phoneNumberField.getText());
        }

        // Show progress and disable button
        progressIndicator.setVisible(true);
        payNowButton.setDisable(true);
        statusLabel.setText("Processing payment...");

        // Process payment asynchronously
        Task<Boolean> paymentTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Create payment record
                boolean created = paymentDAO.createPayment(payment);
                if (!created) {
                    return false;
                }

                // Process payment (simulate payment gateway)
                if (cashOnDeliveryRadio.isSelected()) {
                    // COD doesn't need immediate processing
                    payment.setPaymentStatus("PENDING");
                    paymentDAO.updatePaymentStatus(payment.getPaymentId(), "PENDING");
                    return true;
                } else {
                    // Process card/mobile banking payment
                    return paymentDAO.processPayment(payment);
                }
            }
        };

        paymentTask.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            payNowButton.setDisable(false);
            
            boolean success = paymentTask.getValue();
            if (success) {
                showSuccessDialog(payment);
            } else {
                statusLabel.setText("Payment failed. Please try again.");
                showAlert("Payment Failed", "Your payment could not be processed. Please try again.", Alert.AlertType.ERROR);
            }
        });

        paymentTask.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            payNowButton.setDisable(false);
            statusLabel.setText("Payment failed. Please try again.");
            showAlert("Payment Error", "An error occurred while processing your payment.", Alert.AlertType.ERROR);
        });

        new Thread(paymentTask).start();
    }

    private boolean validatePaymentInfo() {
        if (cardPaymentRadio.isSelected()) {
            if (cardNumberField.getText().trim().isEmpty() || 
                cardHolderNameField.getText().trim().isEmpty() ||
                expiryDateField.getText().trim().isEmpty() ||
                cvvField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please fill in all card details.", Alert.AlertType.WARNING);
                return false;
            }
            
            // Basic card number validation
            String cardNumber = cardNumberField.getText().replaceAll("\\s+", "");
            if (cardNumber.length() < 13 || cardNumber.length() > 19) {
                showAlert("Validation Error", "Please enter a valid card number.", Alert.AlertType.WARNING);
                return false;
            }
        } else if (mobileBankingRadio.isSelected()) {
            if (mobileNumberField.getText().trim().isEmpty() || 
                mobilePinField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please fill in mobile banking details.", Alert.AlertType.WARNING);
                return false;
            }
        } else if (cashOnDeliveryRadio.isSelected()) {
            if (deliveryAddressField.getText().trim().isEmpty() || 
                phoneNumberField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please fill in delivery details.", Alert.AlertType.WARNING);
                return false;
            }
        }
        return true;
    }

    private String getSelectedPaymentMethod() {
        if (cardPaymentRadio.isSelected()) {
            return "CARD";
        } else if (mobileBankingRadio.isSelected()) {
            return "MOBILE_BANKING";
        } else if (cashOnDeliveryRadio.isSelected()) {
            return "CASH_ON_DELIVERY";
        }
        return "CARD"; // default
    }

    private void showSuccessDialog(Payment payment) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Successful");
        alert.setHeaderText("Your payment has been processed successfully!");
        
        String message = String.format(
            "Transaction ID: %s\nAmount: ৳%.2f\nPayment Method: %s\nStatus: %s",
            payment.getTransactionId(),
            payment.getAmount(),
            payment.getPaymentMethod().replace("_", " "),
            payment.getPaymentStatus()
        );
        
        alert.setContentText(message);
        alert.showAndWait();
        
        // Close payment window and return to main app
        closeWindow();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    // Format card number input
    @FXML
    private void formatCardNumber() {
        String text = cardNumberField.getText().replaceAll("\\s+", "");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(text.charAt(i));
        }
        
        cardNumberField.setText(formatted.toString());
        cardNumberField.positionCaret(formatted.length());
    }

    // Format expiry date input
    @FXML
    private void formatExpiryDate() {
        String text = expiryDateField.getText().replaceAll("/", "");
        if (text.length() >= 2) {
            text = text.substring(0, 2) + "/" + text.substring(2);
        }
        expiryDateField.setText(text);
        expiryDateField.positionCaret(text.length());
    }
    
    // Setter methods for integration
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            this.customerId = user.getId();
        }
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
                    // Use reflection to get price and quantity if available
                    double price = (Double) item.getClass().getMethod("getUnitPrice").invoke(item);
                    int quantity = (Integer) item.getClass().getMethod("getQuantity").invoke(item);
                    subtotal += price * quantity;
                } catch (Exception e) {
                    // Fallback if reflection fails
                    System.err.println("Could not calculate item total: " + e.getMessage());
                }
            }
        }
        
        double deliveryFee = 50.0;
        this.totalAmount = subtotal + deliveryFee;
        
        // Update all summary labels
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(String.format("Total Amount: ৳%.2f", totalAmount));
        }
        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("৳%.2f", subtotal));
        }
        if (deliveryFeeLabel != null) {
            deliveryFeeLabel.setText(String.format("৳%.2f", deliveryFee));
        }
        if (totalSummaryLabel != null) {
            totalSummaryLabel.setText(String.format("৳%.2f", totalAmount));
        }
    }
}