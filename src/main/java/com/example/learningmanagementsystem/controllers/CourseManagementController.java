package com.example.learningmanagementsystem.controllers;

import com.example.learningmanagementsystem.DBConnection;
import com.example.learningmanagementsystem.models.Course;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import java.util.HashMap;
import java.util.Map;

public class CourseManagementController {

    @FXML private TextField courseNameField;
    @FXML private ComboBox<String> instructorComboBox;
    @FXML private TableView<CourseWrapper> coursesTable;

    private int selectedCourseId = -1;
    private Map<String, Integer> instructorNameToIdMap = new HashMap<>();

    @FXML
    private void initialize() {
        loadInstructors();
        loadCourses();
        coursesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedCourseId = newSelection.getCourseId();
                courseNameField.setText(newSelection.getCourseName());
                instructorComboBox.setValue(newSelection.getInstructorName());
            }
        });
    }

    private void loadInstructors() {
        ObservableList<String> instructorNames = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT user_id, full_name FROM users WHERE role = 'Instructor'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                instructorNames.add(fullName);
                instructorNameToIdMap.put(fullName, userId);
            }
            instructorComboBox.setItems(instructorNames);
        } catch (SQLException e) {
            System.err.println("Failed to load instructors: " + e.getMessage());
        }
    }

    private void loadCourses() {
        ObservableList<CourseWrapper> courses = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT c.course_id, c.course_name, c.instructor_user_id, u.full_name AS instructor_name " +
                    "FROM courses c LEFT JOIN users u ON c.instructor_user_id = u.user_id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Course course = new Course(rs.getInt("course_id"), rs.getString("course_name"),
                        rs.getInt("instructor_user_id"));
                String instructorName = rs.getString("instructor_name") != null ? rs.getString("instructor_name") : "Unassigned";
                courses.add(new CourseWrapper(course, instructorName));
            }
            coursesTable.setItems(courses);
        } catch (SQLException e) {
            System.err.println("Failed to load courses: " + e.getMessage());
        }
    }

    @FXML
    private void addCourse() {
        String courseName = courseNameField.getText();
        String instructorName = instructorComboBox.getValue();

        if (courseName.isEmpty() || instructorName == null) {
            showAlert("Error", "All fields are required!");
            return;
        }

        Integer instructorId = instructorNameToIdMap.get(instructorName);
        if (instructorId == null) {
            showAlert("Error", "Invalid instructor selected!");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "INSERT INTO courses (course_name, instructor_user_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"course_id"});
            stmt.setString(1, courseName);
            stmt.setInt(2, instructorId);
            stmt.executeUpdate();
            clearFields();
            loadCourses();
            showAlert("Success", "Course added successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add course: " + e.getMessage());
        }
    }

    @FXML
    private void updateCourse() {
        if (selectedCourseId == -1) {
            showAlert("Error", "Please select a course to update!");
            return;
        }

        String courseName = courseNameField.getText();
        String instructorName = instructorComboBox.getValue();

        if (courseName.isEmpty() || instructorName == null) {
            showAlert("Error", "All fields are required!");
            return;
        }

        Integer instructorId = instructorNameToIdMap.get(instructorName);
        if (instructorId == null) {
            showAlert("Error", "Invalid instructor selected!");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "UPDATE courses SET course_name = ?, instructor_user_id = ? WHERE course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseName);
            stmt.setInt(2, instructorId);
            stmt.setInt(3, selectedCourseId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                clearFields();
                loadCourses();
                showAlert("Success", "Course updated successfully!");
            } else {
                showAlert("Error", "Failed to update course.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to update course: " + e.getMessage());
        }
    }

    @FXML
    private void deleteCourse() {
        if (selectedCourseId == -1) {
            showAlert("Error", "Please select a course to delete!");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "DELETE FROM courses WHERE course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedCourseId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                clearFields();
                loadCourses();
                showAlert("Success", "Course deleted successfully!");
            } else {
                showAlert("Error", "Failed to delete course.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete course: " + e.getMessage());
        }
    }

    private void clearFields() {
        courseNameField.clear();
        instructorComboBox.setValue(null);
        selectedCourseId = -1;
        coursesTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/learningmanagementsystem/views/admin/AdminDashboard.fxml"));
            Parent dashboardParent = loader.load();
            Scene dashboardScene = new Scene(dashboardParent);
            Stage stage = (Stage) coursesTable.getScene().getWindow();
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

    // Wrapper class to include instructor name for display
    public static class CourseWrapper {
        private final Course course;
        private final StringProperty instructorName;

        public CourseWrapper(Course course, String instructorName) {
            this.course = course;
            this.instructorName = new SimpleStringProperty(instructorName);
        }

        public int getCourseId() { return course.getCourseId(); }
        public String getCourseName() { return course.getCourseName(); }
        public int getInstructorUserId() { return course.getInstructorUserId(); }
        public String getInstructorName() { return instructorName.get(); }

        public IntegerProperty courseIdProperty() { return course.courseIdProperty(); }
        public StringProperty courseNameProperty() { return course.courseNameProperty(); }
        public StringProperty instructorNameProperty() { return instructorName; }
    }
}