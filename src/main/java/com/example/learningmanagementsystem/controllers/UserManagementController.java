package com.example.learningmanagementsystem.controllers;

import com.example.learningmanagementsystem.DBConnection;
import com.example.learningmanagementsystem.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserManagementController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TableView<User> usersTable;

    private int selectedUserId = -1;

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll("Admin", "Instructor", "Student");
        loadUsers();
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUserId = newSelection.getUserId();
                fullNameField.setText(newSelection.getFullName());
                emailField.setText(newSelection.getEmail());
                passwordField.setText(newSelection.getPassword());
                roleComboBox.setValue(newSelection.getRole());
            }
        });
    }

    private void loadUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT user_id, full_name, email, role FROM users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("user_id"), rs.getString("full_name"),
                        rs.getString("email"), "", rs.getString("role")));
            }
            usersTable.setItems(users);
        } catch (SQLException e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
    }

    @FXML
    private void addUser() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Error", "All fields are required!");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"user_id"});
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, password); // To be hashed in production
            stmt.setString(4, role);
            stmt.executeUpdate();
            clearFields();
            loadUsers();
            showAlert("Success", "User added successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add user: " + e.getMessage());
        }
    }

    @FXML
    private void updateUser() {
        if (selectedUserId == -1) {
            showAlert("Error", "Please select a user to update!");
            return;
        }

        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Error", "All fields are required!");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "UPDATE users SET full_name = ?, email = ?, password = ?, role = ? WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, password); // To be hashed in production
            stmt.setString(4, role);
            stmt.setInt(5, selectedUserId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                clearFields();
                loadUsers();
                showAlert("Success", "User updated successfully!");
            } else {
                showAlert("Error", "Failed to update user.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to update user: " + e.getMessage());
        }
    }

    @FXML
    private void deleteUser() {
        if (selectedUserId == -1) {
            showAlert("Error", "Please select a user to delete!");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedUserId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                clearFields();
                loadUsers();
                showAlert("Success", "User deleted successfully!");
            } else {
                showAlert("Error", "Failed to delete user.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete user: " + e.getMessage());
        }
    }

    private void clearFields() {
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        selectedUserId = -1;
        usersTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/learningmanagementsystem/views/admin/AdminDashboard.fxml"));
            Parent dashboardParent = loader.load();
            Scene dashboardScene = new Scene(dashboardParent);
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
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