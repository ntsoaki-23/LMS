package com.example.learningmanagementsystem.controllers;

import com.example.learningmanagementsystem.DBConnection;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML private StackPane rootStackPane;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button homeButton;
    @FXML private ImageView backgroundImage;

    @FXML
    private void initialize() {
        // Initialize role options
        roleComboBox.getItems().addAll("Admin", "Instructor", "Student");

        // Setup background image
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/background.jpg"));
            backgroundImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            // Fallback gradient background will remain visible
        }

        // Setup button animation
        setupFadeTransition();

        // Test database connection
        if (!DBConnection.testConnection()) {
            showAlert("Database Error", "Failed to connect to database");
        }
    }

    private void setupFadeTransition() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), homeButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.5);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.play();
    }

    @FXML
    private void registerUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Validation Error", "All fields are required");
            return;
        }

        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert("Validation Error", "Please enter a valid email address");
            return;
        }

        // Insert into database
        try (Connection conn = DBConnection.connect()) {
            String sql = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password); // Note: In production, hash the password
            stmt.setString(4, role);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert("Success", "Registration successful!");
                goToHome();
            } else {
                showAlert("Error", "Registration failed");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to register: " + e.getMessage());
        }
    }

    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/learningmanagementsystem/HomePage.fxml"));
            Stage stage = (Stage) rootStackPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load home page");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}