package com.example.learningmanagementsystem.controllers;

import com.example.learningmanagementsystem.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private StackPane rootStackPane;   // Root pane for background image

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private int userId;

    @FXML
    private void initialize() {
        setBackgroundImage();

        if (!DBConnection.testConnection()) {
            showAlert("Error", "Database connection failed. Please check your configuration.");
        }
    }

    private void setBackgroundImage() {
        try {
            Image backgroundImage = new Image(getClass().getResource("/images/background.jpg").toExternalForm());
            BackgroundImage bgImg = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(1.0, 1.0, true, true, false, false)
            );
            rootStackPane.setBackground(new Background(bgImg));
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }
    }

    @FXML
    private void login() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT user_id, role FROM users WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password); // ⚠️ Use hashed passwords in production!
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("user_id");
                String role = rs.getString("role");

                switch (role) {
                    case "Admin":
                        loadDashboard("/com/example/learningmanagementsystem/views/admin/AdminDashboard.fxml", "Admin Dashboard");
                        break;
                    case "Instructor":
                        loadDashboard("/com/example/learningmanagementsystem/InstructorDashboard.fxml", "Instructor Dashboard");
                        break;
                    case "Student":
                        loadDashboard("/com/example/learningmanagementsystem/StudentDashboard.fxml", "Student Dashboard");
                        break;
                    default:
                        showAlert("Error", "Unknown user role: " + role);
                }
            } else {
                showAlert("Login Failed", "Invalid email or password.");
            }

        } catch (SQLException e) {
            showAlert("Error", "Login error: " + e.getMessage());
        }
    }

    private void loadDashboard(String fxmlPath, String title) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);

            // Pass userId to dashboard controller if supported
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setUserId(userId);
            }

            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Unable to load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void goToHome() {
        try {
            URL resource = getClass().getResource("/com/example/learningmanagementsystem/HomePage.fxml");
            if (resource == null) {
                throw new IOException("HomePage.fxml not found.");
            }
            Parent root = FXMLLoader.load(resource);
            Scene scene = new Scene(root);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Unable to load HomePage: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Interface for Dashboard controllers
    public interface DashboardController {
        void setUserId(int userId);
    }
}
