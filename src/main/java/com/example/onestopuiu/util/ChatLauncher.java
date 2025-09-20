package com.example.onestopuiu.util;

import com.example.onestopuiu.controller.ChatController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatLauncher {
    public static void launchChat(String username, boolean isSeller, String chatId) {
        try {
            FXMLLoader loader = new FXMLLoader(ChatLauncher.class.getResource("/com/example/onestopuiu/chat-view.fxml"));
            Parent root = loader.load();
            
            ChatController controller = loader.getController();
            controller.setUserInfo(username, isSeller, chatId);
            
            Stage stage = new Stage();
            stage.setTitle(isSeller ? "Seller Chat - " + username : "Customer Chat - " + username);
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
            
            stage.setOnCloseRequest(e -> controller.closeConnection());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 