package com.example.learningmanagementsystem.controllers;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class HomePageController {

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private StackPane rootStackPane;

    @FXML
    private void initialize() {
        // Apply background image
        if (rootStackPane != null) {
            Image backgroundImage = new Image(getClass().getResource("/images/background.jpg").toExternalForm());
            BackgroundImage bgImg = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            rootStackPane.setBackground(new Background(bgImg));
        }

        // Animate buttons
        playDramaticAnimation(loginButton);
        playDramaticAnimation(registerButton);
    }

    private void playDramaticAnimation(Button button) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), button);
        scaleUp.setToX(1.3);
        scaleUp.setToY(1.3);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        RotateTransition rotate = new RotateTransition(Duration.millis(600), button);
        rotate.setByAngle(360);

        SequentialTransition seq = new SequentialTransition(scaleUp, scaleDown, rotate);
        seq.setCycleCount(2);
        seq.play();
    }

    @FXML
    private void goToLogin() {
        loadScene("Login.fxml");
    }

    @FXML
    private void goToRegister() {
        loadScene("Register.fxml");
    }

    @FXML
    private void exitApplication() {
        System.exit(0);
    }

    @FXML
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About LMS");
        alert.setHeaderText("About the Learning Management System");
        alert.setContentText("""
                Welcome to the LMS Portal!

                This system enables students, teachers, and administrators to manage:
                - Courses and assignments
                - Registrations and academic results

                Version: 1.2.3
                Developed by: 3 IDIOTS Team
                © 2025 LMS Inc.
                """);
        alert.showAndWait();
    }

    private void loadScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/learningmanagementsystem/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
