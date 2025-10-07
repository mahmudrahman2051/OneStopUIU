package com.example.onestopuiu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class OneStopUIUApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            System.out.println("[OneStopUIU] Application starting...");
            FXMLLoader fxmlLoader = new FXMLLoader(OneStopUIUApplication.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
            
            stage.setTitle("OneStopUIU - Professional Campus Solution");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Set icon
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("images/app-icon.png")));
            } catch (Exception e) {
                System.out.println("[OneStopUIU] Warning: Could not load app icon - " + e.getMessage());
            }
            
            stage.show();
            System.out.println("[OneStopUIU] Application started successfully!");
        } catch (Exception e) {
            System.err.println("[OneStopUIU] Fatal error during application startup:");
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch();
    }
} 