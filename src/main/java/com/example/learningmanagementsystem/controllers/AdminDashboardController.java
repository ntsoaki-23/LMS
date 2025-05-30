package com.example.learningmanagementsystem.controllers;

import com.example.learningmanagementsystem.DBConnection;
import com.example.learningmanagementsystem.models.User;
import com.example.learningmanagementsystem.models.Course;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class AdminDashboardController implements LoginController.DashboardController {

    // Table Views
    @FXML private TableView<User> usersTable;
    @FXML private TableView<CourseWrapper> coursesTable;
    @FXML private TableView<EnrollmentWrapper> enrollmentsTable;

    // Pagination Labels
    @FXML private Label usersPageInfoLabel;
    @FXML private Label coursesPageInfoLabel;

    // Pagination Variables
    private int currentUsersPage = 1;
    private int totalUsersPages = 1;
    private int currentCoursesPage = 1;
    private int totalCoursesPages = 1;
    private final int itemsPerPage = 10;

    private int userId;

    @FXML
    private void initialize() {
        loadUsers();
        loadCourses();
        loadEnrollments();
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // User Pagination Methods
    @FXML
    private void firstPageUsers() {
        currentUsersPage = 1;
        loadUsers();
    }

    @FXML
    private void previousPageUsers() {
        if (currentUsersPage > 1) {
            currentUsersPage--;
            loadUsers();
        }
    }

    @FXML
    private void nextPageUsers() {
        if (currentUsersPage < totalUsersPages) {
            currentUsersPage++;
            loadUsers();
        }
    }

    @FXML
    private void lastPageUsers() {
        currentUsersPage = totalUsersPages;
        loadUsers();
    }

    private void loadUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            // Get total count
            String countSql = "SELECT COUNT(*) AS total FROM users";
            try (PreparedStatement countStmt = conn.prepareStatement(countSql);
                 ResultSet countRs = countStmt.executeQuery()) {
                if (countRs.next()) {
                    int totalItems = countRs.getInt("total");
                    totalUsersPages = (int) Math.ceil((double) totalItems / itemsPerPage);
                    if (totalUsersPages == 0) totalUsersPages = 1;
                }
            }

            // Get paginated data
            String sql = "SELECT user_id, full_name, email, role FROM users LIMIT ? OFFSET ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, itemsPerPage);
                stmt.setInt(2, (currentUsersPage - 1) * itemsPerPage);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(new User(
                                rs.getInt("user_id"),
                                rs.getString("full_name"),
                                rs.getString("email"),
                                "",
                                rs.getString("role")
                        ));
                    }
                }
            }
            usersTable.setItems(users);
            updateUsersPageInfo();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void updateUsersPageInfo() {
        usersPageInfoLabel.setText(String.format("Page %d of %d", currentUsersPage, totalUsersPages));
        updatePaginationButtons((HBox) usersPageInfoLabel.getParent(), currentUsersPage, totalUsersPages);
    }

    // Course Pagination Methods
    @FXML
    private void firstPageCourses() {
        currentCoursesPage = 1;
        loadCourses();
    }

    @FXML
    private void previousPageCourses() {
        if (currentCoursesPage > 1) {
            currentCoursesPage--;
            loadCourses();
        }
    }

    @FXML
    private void nextPageCourses() {
        if (currentCoursesPage < totalCoursesPages) {
            currentCoursesPage++;
            loadCourses();
        }
    }

    @FXML
    private void lastPageCourses() {
        currentCoursesPage = totalCoursesPages;
        loadCourses();
    }

    private void loadCourses() {
        ObservableList<CourseWrapper> courses = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            // Get total count
            String countSql = "SELECT COUNT(*) AS total FROM courses";
            try (PreparedStatement countStmt = conn.prepareStatement(countSql);
                 ResultSet countRs = countStmt.executeQuery()) {
                if (countRs.next()) {
                    int totalItems = countRs.getInt("total");
                    totalCoursesPages = (int) Math.ceil((double) totalItems / itemsPerPage);
                    if (totalCoursesPages == 0) totalCoursesPages = 1;
                }
            }

            // Get paginated data
            String sql = "SELECT c.course_id, c.course_name, c.instructor_user_id, u.full_name AS instructor_name " +
                    "FROM courses c LEFT JOIN users u ON c.instructor_user_id = u.user_id " +
                    "ORDER BY c.course_id LIMIT ? OFFSET ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, itemsPerPage);
                stmt.setInt(2, (currentCoursesPage - 1) * itemsPerPage);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Course course = new Course(
                                rs.getInt("course_id"),
                                rs.getString("course_name"),
                                rs.getInt("instructor_user_id")
                        );
                        String instructorName = rs.getString("instructor_name") != null ?
                                rs.getString("instructor_name") : "Unassigned";
                        courses.add(new CourseWrapper(course, instructorName));
                    }
                }
            }
            coursesTable.setItems(courses);
            updateCoursesPageInfo();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load courses: " + e.getMessage());
        }
    }

    private void updateCoursesPageInfo() {
        coursesPageInfoLabel.setText(String.format("Page %d of %d", currentCoursesPage, totalCoursesPages));
        updatePaginationButtons((HBox) coursesPageInfoLabel.getParent(), currentCoursesPage, totalCoursesPages);
    }

    private void updatePaginationButtons(HBox paginationBox, int currentPage, int totalPages) {
        if (paginationBox.getChildren().size() >= 5) {
            Button firstButton = (Button) paginationBox.getChildren().get(0);
            Button prevButton = (Button) paginationBox.getChildren().get(1);
            Button nextButton = (Button) paginationBox.getChildren().get(3);
            Button lastButton = (Button) paginationBox.getChildren().get(4);

            firstButton.setDisable(currentPage == 1);
            prevButton.setDisable(currentPage == 1);
            nextButton.setDisable(currentPage == totalPages);
            lastButton.setDisable(currentPage == totalPages);
        }
    }

    private void loadEnrollments() {
        ObservableList<EnrollmentWrapper> enrollments = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT e.enrollment_id, e.user_id, e.course_id, e.progress, " +
                    "u.full_name AS student_name, c.course_name " +
                    "FROM enrollments e JOIN users u ON e.user_id = u.user_id " +
                    "JOIN courses c ON e.course_id = c.course_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(new EnrollmentWrapper(
                            rs.getInt("enrollment_id"),
                            rs.getInt("user_id"),
                            rs.getInt("course_id"),
                            rs.getDouble("progress"),
                            rs.getString("student_name"),
                            rs.getString("course_name")
                    ));
                }
            }
            enrollmentsTable.setItems(enrollments);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load enrollments: " + e.getMessage());
        }
    }

    @FXML
    private void manageUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/learningmanagementsystem/views/admin/UserManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Management");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load user management: " + e.getMessage());
        }
    }

    @FXML
    private void deleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to delete.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedUser.getUserId());
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    loadUsers();
                    showAlert("Success", "User deleted successfully.");
                } else {
                    showAlert("Error", "Failed to delete user.");
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void manageCourses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/learningmanagementsystem/views/admin/CourseManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) coursesTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Course Management");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load course management: " + e.getMessage());
        }
    }

    @FXML
    private void deleteCourse() {
        CourseWrapper selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert("Error", "Please select a course to delete.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "DELETE FROM courses WHERE course_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedCourse.getCourseId());
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    loadCourses();
                    showAlert("Success", "Course deleted successfully.");
                } else {
                    showAlert("Error", "Failed to delete course.");
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void manageStudentEnrollments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/learningmanagementsystem/views/admin/StudentEnrollmentManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) enrollmentsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Enrollment Management");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load enrollment management: " + e.getMessage());
        }
    }

    @FXML
    private void deleteEnrollment() {
        EnrollmentWrapper selectedEnrollment = enrollmentsTable.getSelectionModel().getSelectedItem();
        if (selectedEnrollment == null) {
            showAlert("Error", "Please select an enrollment to delete.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedEnrollment.getEnrollmentId());
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    loadEnrollments();
                    showAlert("Success", "Enrollment deleted successfully.");
                } else {
                    showAlert("Error", "Failed to delete enrollment.");
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/example/learningmanagementsystem/HomePage.fxml"));
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load home page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Wrapper class for Course with instructor name
    public static class CourseWrapper {
        private final Course course;
        private final StringProperty instructorName;

        public CourseWrapper(Course course, String instructorName) {
            this.course = course;
            this.instructorName = new SimpleStringProperty(instructorName);
        }

        // Getter methods
        public int getCourseId() { return course.getCourseId(); }
        public String getCourseName() { return course.getCourseName(); }
        public int getInstructorUserId() { return course.getInstructorUserId(); }
        public String getInstructorName() { return instructorName.get(); }

        // Property methods for JavaFX binding
        public IntegerProperty courseIdProperty() { return course.courseIdProperty(); }
        public StringProperty courseNameProperty() { return course.courseNameProperty(); }
        public IntegerProperty instructorUserIdProperty() { return course.instructorUserIdProperty(); }
        public StringProperty instructorNameProperty() { return instructorName; }
    }

    // Wrapper class for Enrollment with student and course names
    public static class EnrollmentWrapper {
        private final int enrollmentId;
        private final int userId;
        private final int courseId;
        private final double progress;
        private final StringProperty studentName;
        private final StringProperty courseName;

        public EnrollmentWrapper(int enrollmentId, int userId, int courseId,
                                 double progress, String studentName, String courseName) {
            this.enrollmentId = enrollmentId;
            this.userId = userId;
            this.courseId = courseId;
            this.progress = progress;
            this.studentName = new SimpleStringProperty(studentName);
            this.courseName = new SimpleStringProperty(courseName);
        }

        // Getter methods
        public int getEnrollmentId() { return enrollmentId; }
        public int getUserId() { return userId; }
        public int getCourseId() { return courseId; }
        public double getProgress() { return progress; }
        public String getStudentName() { return studentName.get(); }
        public String getCourseName() { return courseName.get(); }

        // Property methods for JavaFX binding
        public IntegerProperty enrollmentIdProperty() { return new SimpleIntegerProperty(enrollmentId); }
        public IntegerProperty userIdProperty() { return new SimpleIntegerProperty(userId); }
        public IntegerProperty courseIdProperty() { return new SimpleIntegerProperty(courseId); }
        public DoubleProperty progressProperty() { return new SimpleDoubleProperty(progress); }
        public StringProperty studentNameProperty() { return studentName; }
        public StringProperty courseNameProperty() { return courseName; }
    }
}