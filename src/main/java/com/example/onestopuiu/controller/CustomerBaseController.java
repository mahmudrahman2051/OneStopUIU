package com.example.onestopuiu.controller;

import com.example.onestopuiu.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public abstract class CustomerBaseController {
    @FXML
    protected Label welcomeLabel;

    protected User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        if (welcomeLabel != null && user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        } else if (welcomeLabel != null && user == null) {
            welcomeLabel.setText("Welcome, Guest!");
        }
        
        // Call subclass initialization
        try {
            onInitialize();
        } catch (Exception e) {
            System.err.println("[CustomerBaseController] Error in onInitialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected abstract void onInitialize();
} 
