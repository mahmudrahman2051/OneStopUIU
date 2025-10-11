package com.example.onestopuiu.util;

import javafx.scene.image.Image;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Utility class for caching and efficiently loading images.
 * Uses a thread pool for parallel loading and a concurrent hash map for caching.
 */
public class ImageCache {
    private static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final ExecutorService imageLoadExecutor = Executors.newFixedThreadPool(4);
    private static final Image DEFAULT_FOOD_IMAGE;
    
    static {
        // Load default images once at startup
        Image defaultFood = null;
        try {
            defaultFood = new Image(ImageCache.class.getResourceAsStream("/com/example/onestopuiu/images/default-item.png"));
        } catch (Exception e) {
            System.err.println("Error loading default images: " + e.getMessage());
        }
        DEFAULT_FOOD_IMAGE = defaultFood;
    }
    
    /**
     * Get an image from cache or load it asynchronously with default thumbnail size.
     * 
     * @param imageUrl The URL of the image to load
     * @param isFood Whether this is a food item image (for default fallback)
     * @param imageConsumer Consumer that will be called when the image is loaded
     */
    public static void getImage(String rawImageUrl, boolean isFood, Consumer<Image> imageConsumer) {
        // Use default thumbnail size
        getImage(rawImageUrl, isFood, 180, 120, imageConsumer);
    }
    
    /**
     * Get an image from cache or load it asynchronously with specified dimensions.
     * 
     * @param rawImageUrl The URL of the image to load
     * @param isFood Whether this is a food item image (for default fallback)
     * @param width Target width for the image view
     * @param height Target height for the image view
     * @param imageConsumer Consumer that will be called when the image is loaded
     */
    public static void getImage(String rawImageUrl, boolean isFood, double width, double height, Consumer<Image> imageConsumer) {
        // Format the image URL
        String imageUrl = ImageUtils.formatImageUrl(rawImageUrl);
        
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // Return default image if URL is invalid
            imageConsumer.accept(DEFAULT_FOOD_IMAGE);
            return;
        }
        
        // Check if image is already in cache
        Image cachedImage = imageCache.get(imageUrl);
        if (cachedImage != null) {
            imageConsumer.accept(cachedImage);
            return;
        }
        
        // Load default image first
        imageConsumer.accept(DEFAULT_FOOD_IMAGE);
        
        // Load actual image in background
        imageLoadExecutor.submit(() -> {
            try {
                Image loadedImage = null;
                
                // Check if this is a local resource path (starts with /com/example/onestopuiu/uploads/)
                if (imageUrl.startsWith("/com/example/onestopuiu/uploads/")) {
                    // Convert to local file path and create file URL
                    String localPath = LocalImageUploader.getLocalPath(imageUrl);
                    java.io.File imageFile = new java.io.File(localPath);
                    
                    if (imageFile.exists()) {
                        System.out.println("[ImageCache] Loading local image: " + imageFile.getAbsolutePath());
                        loadedImage = new Image(imageFile.toURI().toString(), 0, 0, true, true, true);
                    } else {
                        System.err.println("[ImageCache] Local image file not found: " + localPath);
                    }
                } else {
                    // Try to load as external URL
                    System.out.println("[ImageCache] Loading external image: " + imageUrl);
                    loadedImage = new Image(imageUrl, 0, 0, true, true, true);
                }
                
                // Only cache and update image if loading was successful
                if (loadedImage != null && !loadedImage.isError()) {
                    final Image finalImage = loadedImage;
                    imageCache.put(imageUrl, finalImage);
                    javafx.application.Platform.runLater(() -> imageConsumer.accept(finalImage));
                } else {
                    System.err.println("[ImageCache] Failed to load image: " + imageUrl);
                }
            } catch (Exception e) {
                System.err.println("[ImageCache] Error loading image from " + imageUrl + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Clears the image cache to free up memory.
     */
    public static void clearCache() {
        imageCache.clear();
    }
    
    /**
     * Shuts down the image loading executor service.
     * Call this when your application is shutting down.
     */
    public static void shutdown() {
        imageLoadExecutor.shutdown();
    }
} 