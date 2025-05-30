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

public class EnrollmentManagementController {

    @FXML private TableView<CourseWrapper> availableCoursesTable;

    private int userId;

    @FXML
    private void initialize() {
        userId = getUserIdFromParentController(); // Use the DashboardController interface
        loadAvailableCourses();
    }

    private int getUserIdFromParentController() {
        // This should be passed via the DashboardController interface
        // For now, assume it's set by the calling controller (e.g., StudentDashboardController)
        return userId; // Replace with actual logic, e.g., ((DashboardController) loader.getController()).getUserId()
    }

    private void loadAvailableCourses() {
        ObservableList<CourseWrapper> courses = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT c.course_id, c.course_name, c.instructor_user_id, u.full_name AS instructor_name " +
                    "FROM courses c LEFT JOIN users u ON c.instructor_user_id = u.user_id " +
                    "WHERE c.course_id NOT IN (SELECT course_id FROM enrollments WHERE user_id = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Course course = new Course(rs.getInt("course_id"), rs.getString("course_name"),
                        rs.getInt("instructor_user_id"));
                String instructorName = rs.getString("instructor_name") != null ? rs.getString("instructor_name") : "Unassigned";
                courses.add(new CourseWrapper(course, instructorName));
            }
            availableCoursesTable.setItems(courses);
        } catch (SQLException e) {
            System.err.println("Failed to load available courses: " + e.getMessage());
        }
    }

    @FXML
    private void enrollCourse() {
        CourseWrapper selectedCourse = availableCoursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            try (Connection conn = DBConnection.connect()) {
                String sql = "INSERT INTO enrollments (user_id, course_id, progress) VALUES (?, ?, 0.00)";
                PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"enrollment_id"});
                stmt.setInt(1, userId);
                stmt.setInt(2, selectedCourse.getCourseId());
                stmt.executeUpdate();
                loadAvailableCourses();
                showAlert("Success", "Enrolled in course successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to enroll: " + e.getMessage());
            }
        } else {
            showAlert("Error", "Please select a course to enroll!");
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/learningmanagementsystem/views/student/StudentDashboard.fxml"));
            Parent dashboardParent = loader.load();
            Scene dashboardScene = new Scene(dashboardParent);
            Stage stage = (Stage) availableCoursesTable.getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("Student Dashboard");
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