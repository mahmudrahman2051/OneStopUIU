package com.example.onestopuiu.controller;

import com.example.onestopuiu.dao.SellerRequestDAO;
import com.example.onestopuiu.dao.UserDAO;
import com.example.onestopuiu.model.SellerRequest;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AdminSellerRequestsController extends AdminBaseController implements Initializable {
    @FXML private TableView<SellerRequest> requestsTable;
    @FXML private TableColumn<SellerRequest, String> idColumn;
    @FXML private TableColumn<SellerRequest, String> usernameColumn;
    @FXML private TableColumn<SellerRequest, String> dateColumn;
    @FXML private TableColumn<SellerRequest, String> reasonColumn;
    @FXML private TableColumn<SellerRequest, String> statusColumn;
    @FXML private TableColumn<SellerRequest, Void> actionsColumn;
    @FXML private ProgressIndicator loadingIndicator;
    
    private final SellerRequestDAO sellerRequestDAO = new SellerRequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadSellerRequests();
    }
    
    private void setupTableColumns() {
        // Set up table columns with cell value factories
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        
        usernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(dateFormat.format(cellData.getValue().getRequestDate())));
        
        reasonColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getReason()));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Configure action column with approve/reject/edit/delete buttons
        actionsColumn.setCellFactory(new Callback<TableColumn<SellerRequest, Void>, TableCell<SellerRequest, Void>>() {
            @Override
            public TableCell<SellerRequest, Void> call(TableColumn<SellerRequest, Void> param) {
                return new TableCell<SellerRequest, Void>() {
                    private final Button approveButton = new Button("✓");
                    private final Button rejectButton = new Button("✗");
                    private final Button editButton = new Button("✏");
                    private final Button deleteButton = new Button("🗑");
                    private final HBox pendingPane = new HBox(3, approveButton, rejectButton, editButton, deleteButton);
                    private final HBox processedPane = new HBox(3, editButton, deleteButton);
                    
                    {
                        // Style the buttons
                        approveButton.getStyleClass().add("success-button");
                        rejectButton.getStyleClass().add("danger-button");
                        editButton.getStyleClass().add("info-button");
                        deleteButton.getStyleClass().add("danger-button");
                        
                        // Set button sizes
                        approveButton.setPrefWidth(30);
                        rejectButton.setPrefWidth(30);
                        editButton.setPrefWidth(30);
                        deleteButton.setPrefWidth(30);
                        
                        // Set tooltips
                        approveButton.setTooltip(new Tooltip("Approve Request"));
                        rejectButton.setTooltip(new Tooltip("Reject Request"));
                        editButton.setTooltip(new Tooltip("Edit Request"));
                        deleteButton.setTooltip(new Tooltip("Delete Request"));
                        
                        // Set button actions
                        approveButton.setOnAction(event -> {
                            SellerRequest request = getTableView().getItems().get(getIndex());
                            handleApprove(request);
                        });
                        
                        rejectButton.setOnAction(event -> {
                            SellerRequest request = getTableView().getItems().get(getIndex());
                            handleReject(request);
                        });
                        
                        editButton.setOnAction(event -> {
                            SellerRequest request = getTableView().getItems().get(getIndex());
                            handleEdit(request);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            SellerRequest request = getTableView().getItems().get(getIndex());
                            handleDelete(request);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            SellerRequest request = getTableView().getItems().get(getIndex());
                            if ("pending".equals(request.getStatus())) {
                                setGraphic(pendingPane);
                            } else {
                                setGraphic(processedPane);
                            }
                        }
                    }
                };
            }
        });
    }
    
    private void loadSellerRequests() {
        loadingIndicator.setVisible(true);
        
        new Thread(() -> {
            try {
                List<SellerRequest> requests = sellerRequestDAO.getAll();
                
                javafx.application.Platform.runLater(() -> {
                    requestsTable.setItems(FXCollections.observableArrayList(requests));
                    loadingIndicator.setVisible(false);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showError("Error", "Failed to load seller requests: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }
    
    private void handleApprove(SellerRequest request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Approval");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to approve this seller request?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            loadingIndicator.setVisible(true);
            
            new Thread(() -> {
                boolean success = sellerRequestDAO.approveRequest(request.getId(), request.getUserId());
                
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    if (success) {
                        showInformation("Success", "Seller request approved successfully!");
                        loadSellerRequests();
                    } else {
                        showError("Error", "Failed to approve seller request");
                    }
                });
            }).start();
        }
    }
    
    private void handleReject(SellerRequest request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Rejection");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to reject this seller request?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            loadingIndicator.setVisible(true);
            
            new Thread(() -> {
                boolean success = sellerRequestDAO.rejectRequest(request.getId());
                
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    if (success) {
                        showInformation("Success", "Seller request rejected successfully!");
                        loadSellerRequests();
                    } else {
                        showError("Error", "Failed to reject seller request");
                    }
                });
            }).start();
        }
    }
    
    private void handleEdit(SellerRequest request) {
        // Create edit dialog
        Dialog<SellerRequest> dialog = new Dialog<>();
        dialog.setTitle("Edit Seller Request");
        dialog.setHeaderText("Edit seller request details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField usernameField = new TextField(request.getUsername());
        usernameField.setPromptText("Username");
        usernameField.setEditable(false); // Username should not be editable
        
        TextArea reasonField = new TextArea(request.getReason());
        reasonField.setPromptText("Reason for becoming a seller");
        reasonField.setPrefRowCount(3);
        
        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("pending", "approved", "rejected");
        statusField.setValue(request.getStatus());

        // Create layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Status:"), 0, 1);
        grid.add(statusField, 1, 1);
        grid.add(new Label("Reason:"), 0, 2);
        grid.add(reasonField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a SellerRequest when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                request.setReason(reasonField.getText());
                request.setStatus(statusField.getValue());
                return request;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editedRequest -> {
            loadingIndicator.setVisible(true);
            
            new Thread(() -> {
                try {
                    sellerRequestDAO.update(editedRequest);
                    
                    // If status changed to approved, also update user role
                    if ("approved".equals(editedRequest.getStatus()) && !"approved".equals(request.getStatus())) {
                        sellerRequestDAO.approveRequest(editedRequest.getId(), editedRequest.getUserId());
                    }
                    
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        showInformation("Success", "Seller request updated successfully!");
                        loadSellerRequests();
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        showError("Error", "Failed to update seller request: " + e.getMessage());
                    });
                }
            }).start();
        });
    }
    
    private void handleDelete(SellerRequest request) {
        // Create custom dialog with options
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Options");
        alert.setHeaderText("Choose deletion type for: " + request.getUsername());
        
        ButtonType deleteRequestOnly = new ButtonType("Delete Request Only");
        ButtonType deleteUserAndRequest = new ButtonType("Delete User & Request");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(deleteRequestOnly, deleteUserAndRequest, cancel);
        
        alert.setContentText("Choose what you want to delete:\n\n" +
                            "• Delete Request Only: Removes only the seller request\n" +
                            "• Delete User & Request: Removes the user account entirely\n\n" +
                            "Username: " + request.getUsername() + "\n" +
                            "Status: " + request.getStatus());
        
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == deleteRequestOnly) {
                deleteRequestOnly(request);
            } else if (buttonType == deleteUserAndRequest) {
                deleteUserAndRequest(request);
            }
        });
    }
    
    private void deleteRequestOnly(SellerRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Request Deletion");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this seller request?\n\n" +
                                   "The user account will remain but the seller request will be removed.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            loadingIndicator.setVisible(true);
            
            new Thread(() -> {
                try {
                    sellerRequestDAO.delete(request.getId());
                    
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        showInformation("Success", "Seller request deleted successfully!");
                        loadSellerRequests();
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        showError("Error", "Failed to delete seller request: " + e.getMessage());
                    });
                }
            }).start();
        }
    }
    
    private void deleteUserAndRequest(SellerRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle("Confirm User Deletion");
        confirmAlert.setHeaderText("⚠️ DANGER: Complete User Deletion");
        confirmAlert.setContentText("Are you sure you want to DELETE THE ENTIRE USER ACCOUNT?\n\n" +
                                   "Username: " + request.getUsername() + "\n\n" +
                                   "This will:\n" +
                                   "• Delete the user account permanently\n" +
                                   "• Delete the seller request\n" +
                                   "• Remove all user data\n\n" +
                                   "⚠️ THIS ACTION CANNOT BE UNDONE!");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            loadingIndicator.setVisible(true);
            
            new Thread(() -> {
                try {
                    // First delete the seller request
                    sellerRequestDAO.delete(request.getId());
                    
                    // Then delete the user account
                    boolean userDeleted = userDAO.deleteUserByUsername(request.getUsername());
                    
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        if (userDeleted) {
                            showInformation("Success", "User account and seller request deleted successfully!");
                        } else {
                            showInformation("Partial Success", "Seller request deleted, but user account deletion failed.");
                        }
                        loadSellerRequests();
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        showError("Error", "Failed to delete: " + e.getMessage());
                    });
                }
            }).start();
        }
    }
    
    @FXML
    protected void handleBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/admin-dashboard.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) requestsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
            
            AdminDashboardController controller = fxmlLoader.getController();
            controller.initData(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not return to admin dashboard");
        }
    }
    
    @FXML
    protected void handleRefresh() {
        loadSellerRequests();
        showInformation("Refresh", "Seller requests refreshed successfully!");
    }
    
    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/onestopuiu/login.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) requestsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not log out");
        }
    }
    
    @Override
    protected void onInitialize() {
        // Not needed since we're implementing Initializable
    }
} 
