package com.example.learningmanagementsystem.controllers;

import com.example.learningmanagementsystem.DBConnection;
import com.example.learningmanagementsystem.models.Enrollment;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.sql.Timestamp;

public class StudentDashboardController implements LoginController.DashboardController {

    @FXML private TableView<EnrollmentWrapper> enrollmentsTable;
    @FXML private ComboBox<EnrollmentWrapper> materialsCourseComboBox;
    @FXML private TableView<Material> materialsTable;
    @FXML private ComboBox<EnrollmentWrapper> assignmentsCourseComboBox;
    @FXML private TableView<AssignmentWrapper> assignmentsTable;
    @FXML private TableView<Submission> submissionsTable;

    private int userId;
    private ObservableList<EnrollmentWrapper> enrollmentsList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Defer loading until userId is set
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
        loadEnrollments();
        setupCourseComboBoxes();
        loadSubmissions();
    }

    private void loadEnrollments() {
        enrollmentsList.clear();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT e.enrollment_id, e.user_id, e.course_id, e.enrollment_date, e.progress, c.course_name " +
                    "FROM enrollments e JOIN courses c ON e.course_id = c.course_id WHERE e.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Enrollment enrollment = new Enrollment(rs.getInt("enrollment_id"), rs.getInt("user_id"),
                        rs.getInt("course_id"), rs.getString("enrollment_date"), rs.getDouble("progress"));
                String courseName = rs.getString("course_name");
                enrollmentsList.add(new EnrollmentWrapper(enrollment, courseName));
            }
            enrollmentsTable.setItems(enrollmentsList);
        } catch (SQLException e) {
            System.err.println("Failed to load enrollments: " + e.getMessage());
        }
    }

    private void setupCourseComboBoxes() {
        materialsCourseComboBox.setItems(enrollmentsList);
        assignmentsCourseComboBox.setItems(enrollmentsList);

        materialsCourseComboBox.setCellFactory(lv -> new ListCell<EnrollmentWrapper>() {
            @Override
            protected void updateItem(EnrollmentWrapper enrollment, boolean empty) {
                super.updateItem(enrollment, empty);
                setText(empty || enrollment == null ? null : enrollment.getCourseName());
            }
        });
        materialsCourseComboBox.setButtonCell(new ListCell<EnrollmentWrapper>() {
            @Override
            protected void updateItem(EnrollmentWrapper enrollment, boolean empty) {
                super.updateItem(enrollment, empty);
                setText(empty || enrollment == null ? null : enrollment.getCourseName());
            }
        });

        assignmentsCourseComboBox.setCellFactory(lv -> new ListCell<EnrollmentWrapper>() {
            @Override
            protected void updateItem(EnrollmentWrapper enrollment, boolean empty) {
                super.updateItem(enrollment, empty);
                setText(empty || enrollment == null ? null : enrollment.getCourseName());
            }
        });
        assignmentsCourseComboBox.setButtonCell(new ListCell<EnrollmentWrapper>() {
            @Override
            protected void updateItem(EnrollmentWrapper enrollment, boolean empty) {
                super.updateItem(enrollment, empty);
                setText(empty || enrollment == null ? null : enrollment.getCourseName());
            }
        });

        materialsCourseComboBox.setOnAction(event -> loadMaterials());
        assignmentsCourseComboBox.setOnAction(event -> loadAssignments());
    }

    private void loadMaterials() {
        EnrollmentWrapper selectedEnrollment = materialsCourseComboBox.getSelectionModel().getSelectedItem();
        if (selectedEnrollment == null) return;

        ObservableList<Material> materials = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT material_id, title, file_name FROM course_materials WHERE course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedEnrollment.getCourseId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                materials.add(new Material(rs.getInt("material_id"), rs.getString("title"),
                        rs.getString("file_name")));
            }
            materialsTable.setItems(materials);
        } catch (SQLException e) {
            System.err.println("Failed to load materials: " + e.getMessage());
        }
    }

    private void loadAssignments() {
        EnrollmentWrapper selectedEnrollment = assignmentsCourseComboBox.getSelectionModel().getSelectedItem();
        if (selectedEnrollment == null) return;

        ObservableList<AssignmentWrapper> assignments = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT a.assignment_id, a.assignment_title, a.due_date, s.submission_id " +
                    "FROM assignments a LEFT JOIN submissions s ON a.assignment_id = s.assignment_id AND s.user_id = ? " +
                    "WHERE a.course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, selectedEnrollment.getCourseId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String status = rs.getInt("submission_id") > 0 ? "Submitted" : "Not Submitted";
                assignments.add(new AssignmentWrapper(rs.getInt("assignment_id"), rs.getString("assignment_title"),
                        rs.getTimestamp("due_date"), status));
            }
            assignmentsTable.setItems(assignments);
        } catch (SQLException e) {
            System.err.println("Failed to load assignments: " + e.getMessage());
        }
    }

    @FXML
    private void submitAssignment() {
        AssignmentWrapper selectedAssignment = assignmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            showAlert("Error", "Please select an assignment!");
            return;
        }

        if (selectedAssignment.getStatus().equals("Submitted")) {
            showAlert("Error", "You have already submitted this assignment!");
            return;
        }

        TextInputDialog fileDialog = new TextInputDialog();
        fileDialog.setTitle("Submit Assignment");
        fileDialog.setHeaderText("Enter File Name (e.g., submission.pdf)");
        fileDialog.setContentText("File Name:");
        fileDialog.showAndWait().ifPresent(fileName -> {
            try (Connection conn = DBConnection.connect()) {
                String sql = "INSERT INTO submissions (assignment_id, user_id, submission_date, file_name, score) " +
                        "VALUES (?, ?, ?, ?, 0.0)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, selectedAssignment.getAssignmentId());
                stmt.setInt(2, userId);
                stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                stmt.setString(4, fileName);
                stmt.executeUpdate();
                loadAssignments();
                loadSubmissions();
                showAlert("Success", "Assignment submitted successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to submit assignment: " + e.getMessage());
            }
        });
    }

    private void loadSubmissions() {
        ObservableList<Submission> submissions = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT a.assignment_id, a.assignment_title, s.score, c.course_name " +
                    "FROM submissions s JOIN assignments a ON s.assignment_id = a.assignment_id " +
                    "JOIN courses c ON a.course_id = c.course_id WHERE s.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                submissions.add(new Submission(rs.getInt("assignment_id"), rs.getString("course_name"),
                        rs.getString("assignment_title"), rs.getDouble("score")));
            }
            submissionsTable.setItems(submissions);
        } catch (SQLException e) {
            System.err.println("Failed to load submissions: " + e.getMessage());
        }
    }

    @FXML
    private void goToHome() {
        try {
            Parent homeParent = FXMLLoader.load(getClass().getResource("/com/example/learningmanagementsystem/HomePage.fxml"));
            Scene homeScene = new Scene(homeParent);
            Stage stage = (Stage) enrollmentsTable.getScene().getWindow();
            stage.setScene(homeScene);
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

    public static class EnrollmentWrapper {
        private final Enrollment enrollment;
        private final StringProperty courseName;

        public EnrollmentWrapper(Enrollment enrollment, String courseName) {
            this.enrollment = enrollment;
            this.courseName = new SimpleStringProperty(courseName);
        }

        public int getEnrollmentId() { return enrollment.getEnrollmentId(); }
        public int getUserId() { return enrollment.getUserId(); }
        public int getCourseId() { return enrollment.getCourseId(); }
        public String getEnrollmentDate() { return enrollment.getEnrollmentDate(); }
        public double getProgress() { return enrollment.getProgress(); }
        public String getCourseName() { return courseName.get(); }

        public IntegerProperty enrollmentIdProperty() { return enrollment.enrollmentIdProperty(); }
        public IntegerProperty userIdProperty() { return enrollment.userIdProperty(); }
        public IntegerProperty courseIdProperty() { return enrollment.courseIdProperty(); }
        public StringProperty enrollmentDateProperty() { return enrollment.enrollmentDateProperty(); }
        public StringProperty courseNameProperty() { return courseName; }
    }

    public static class Material {
        private final int materialId;
        private final StringProperty title;
        private final StringProperty fileName;

        public Material(int materialId, String title, String fileName) {
            this.materialId = materialId;
            this.title = new SimpleStringProperty(title);
            this.fileName = new SimpleStringProperty(fileName);
        }

        public int getMaterialId() { return materialId; }
        public String getTitle() { return title.get(); }
        public String getFileName() { return fileName.get(); }

        public IntegerProperty materialIdProperty() { return new SimpleIntegerProperty(materialId); }
        public StringProperty titleProperty() { return title; }
        public StringProperty fileNameProperty() { return fileName; }
    }

    public static class AssignmentWrapper {
        private final int assignmentId;
        private final StringProperty assignmentTitle;
        private final StringProperty dueDate;
        private final StringProperty status;

        public AssignmentWrapper(int assignmentId, String assignmentTitle, Timestamp dueDate, String status) {
            this.assignmentId = assignmentId;
            this.assignmentTitle = new SimpleStringProperty(assignmentTitle);
            this.dueDate = new SimpleStringProperty(dueDate != null ? dueDate.toString() : "");
            this.status = new SimpleStringProperty(status);
        }

        public int getAssignmentId() { return assignmentId; }
        public String getAssignmentTitle() { return assignmentTitle.get(); }
        public String getDueDate() { return dueDate.get(); }
        public String getStatus() { return status.get(); }

        public IntegerProperty assignmentIdProperty() { return new SimpleIntegerProperty(assignmentId); }
        public StringProperty assignmentTitleProperty() { return assignmentTitle; }
        public StringProperty dueDateProperty() { return dueDate; }
        public StringProperty statusProperty() { return status; }
    }

    public static class Submission {
        private final int assignmentId;
        private final StringProperty courseName;
        private final StringProperty assignmentTitle;
        private final double score;

        public Submission(int assignmentId, String courseName, String assignmentTitle, double score) {
            this.assignmentId = assignmentId;
            this.courseName = new SimpleStringProperty(courseName);
            this.assignmentTitle = new SimpleStringProperty(assignmentTitle);
            this.score = score;
        }

        public int getAssignmentId() { return assignmentId; }
        public String getCourseName() { return courseName.get(); }
        public String getAssignmentTitle() { return assignmentTitle.get(); }
        public double getScore() { return score; }

        public IntegerProperty assignmentIdProperty() { return new SimpleIntegerProperty(assignmentId); }
        public StringProperty courseNameProperty() { return courseName; }
        public StringProperty assignmentTitleProperty() { return assignmentTitle; }
    }
}