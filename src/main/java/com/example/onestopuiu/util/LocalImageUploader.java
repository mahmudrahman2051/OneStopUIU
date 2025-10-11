package com.example.onestopuiu.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class LocalImageUploader {
    private static final String UPLOAD_DIR = "src/main/resources/com/example/onestopuiu/uploads/";
    private static final String RESOURCE_PATH = "/com/example/onestopuiu/uploads/";
    
    static {
        // Create upload directory if it doesn't exist
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }

    public static String uploadImage(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            throw new IOException("Image file does not exist");
        }

        // Validate file type
        String fileName = imageFile.getName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && 
            !fileName.endsWith(".png") && !fileName.endsWith(".gif")) {
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG, and GIF files are supported.");
        }

        // Validate file size (max 5MB)
        long fileSize = imageFile.length();
        if (fileSize > 5 * 1024 * 1024) {
            throw new IOException("File size too large. Maximum size is 5MB.");
        }

        try {
            // Generate unique filename with timestamp and UUID
            String fileExtension = fileName.substring(fileName.lastIndexOf("."));
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String uniqueFileName = "food_" + timestamp + "_" + uniqueId + fileExtension;

            // Create destination path
            Path sourcePath = imageFile.toPath();
            Path destinationPath = Paths.get(UPLOAD_DIR + uniqueFileName);

            // Copy file to upload directory
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // Return resource path that can be used by JavaFX
            return RESOURCE_PATH + uniqueFileName;

        } catch (IOException e) {
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    /**
     * Get the local file path for a resource path
     */
    public static String getLocalPath(String resourcePath) {
        if (resourcePath != null && resourcePath.startsWith(RESOURCE_PATH)) {
            return UPLOAD_DIR + resourcePath.substring(RESOURCE_PATH.length());
        }
        return resourcePath;
    }

    /**
     * Delete an uploaded image
     */
    public static boolean deleteImage(String resourcePath) {
        if (resourcePath == null || !resourcePath.startsWith(RESOURCE_PATH)) {
            return false;
        }

        try {
            String localPath = getLocalPath(resourcePath);
            Path filePath = Paths.get(localPath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if an image file exists
     */
    public static boolean imageExists(String resourcePath) {
        if (resourcePath == null || !resourcePath.startsWith(RESOURCE_PATH)) {
            return false;
        }

        String localPath = getLocalPath(resourcePath);
        return Files.exists(Paths.get(localPath));
    }
}