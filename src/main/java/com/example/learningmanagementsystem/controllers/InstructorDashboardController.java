package com.example.learningmanagementsystem.controllers;

import com.example.learningmanagementsystem.DBConnection;
import com.example.learningmanagementsystem.models.Course;
import com.example.learningmanagementsystem.models.User;
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
import java.time.LocalDateTime;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class InstructorDashboardController implements LoginController.DashboardController {

    @FXML private TableView<Course> coursesTable;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private TableView<User> studentsTable;
    @FXML private ComboBox<Course> materialsCourseComboBox;
    @FXML private TableView<Material> materialsTable;
    @FXML private ComboBox<Course> assignmentsCourseComboBox;
    @FXML private TableView<Assignment> assignmentsTable;
    @FXML private TableView<SubmissionWrapper> submissionsTable;

    private int userId;
    private ObservableList<Course> coursesList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Defer loading until userId is set
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
        loadCourses();
        setupCourseComboBoxes();
    }

    private void loadCourses() {
        System.out.println("Loading courses for instructor with userId: " + userId);
        coursesList.clear();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT course_id, course_name FROM courses WHERE instructor_user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                coursesList.add(new Course(rs.getInt("course_id"), rs.getString("course_name"), userId));
            }
            coursesTable.setItems(coursesList);
            if (coursesList.isEmpty()) {
                System.out.println("No courses found for instructor with userId: " + userId);
            } else {
                System.out.println("Found " + coursesList.size() + " courses for instructor with userId: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load courses: " + e.getMessage());
        }
    }

    private void setupCourseComboBoxes() {
        courseComboBox.setItems(coursesList);
        materialsCourseComboBox.setItems(coursesList);
        assignmentsCourseComboBox.setItems(coursesList);

        courseComboBox.setCellFactory(lv -> new ListCell<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getCourseName());
            }
        });
        courseComboBox.setButtonCell(new ListCell<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getCourseName());
            }
        });

        materialsCourseComboBox.setCellFactory(lv -> new ListCell<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getCourseName());
            }
        });
        materialsCourseComboBox.setButtonCell(new ListCell<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getCourseName());
            }
        });

        assignmentsCourseComboBox.setCellFactory(lv -> new ListCell<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getCourseName());
            }
        });
        assignmentsCourseComboBox.setButtonCell(new ListCell<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getCourseName());
            }
        });

        courseComboBox.setOnAction(event -> loadEnrolledStudents());
        materialsCourseComboBox.setOnAction(event -> loadMaterials());
        assignmentsCourseComboBox.setOnAction(event -> loadAssignments());
    }

    private void loadEnrolledStudents() {
        Course selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) return;

        ObservableList<User> students = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT u.user_id, u.full_name, u.email " +
                    "FROM users u JOIN enrollments e ON u.user_id = e.user_id " +
                    "WHERE e.course_id = ? AND u.role = 'Student'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedCourse.getCourseId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                students.add(new User(rs.getInt("user_id"), rs.getString("full_name"),
                        rs.getString("email"), "", "Student"));
            }
            studentsTable.setItems(students);
        } catch (SQLException e) {
            System.err.println("Failed to load enrolled students: " + e.getMessage());
        }
    }

    private void loadMaterials() {
        Course selectedCourse = materialsCourseComboBox.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) return;

        ObservableList<Material> materials = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT material_id, title, file_name FROM course_materials WHERE course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedCourse.getCourseId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                materials.add(new Material(rs.getInt("material_id"), rs.getString("title"),
                        rs.getString("fileName")));
            }
            materialsTable.setItems(materials);
        } catch (SQLException e) {
            System.err.println("Failed to load materials: " + e.getMessage());
        }
    }

    private void loadAssignments() {
        Course selectedCourse = assignmentsCourseComboBox.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) return;

        ObservableList<Assignment> assignments = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT assignment_id, assignment_title, due_date FROM assignments WHERE course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedCourse.getCourseId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                assignments.add(new Assignment(rs.getInt("assignment_id"), rs.getString("assignment_title"),
                        rs.getTimestamp("due_date")));
            }
            assignmentsTable.setItems(assignments);
        } catch (SQLException e) {
            System.err.println("Failed to load assignments: " + e.getMessage());
        }
    }

    @FXML
    private void createAssignment() {
        Course selectedCourse = assignmentsCourseComboBox.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert("Error", "Please select a course!");
            return;
        }

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Create Assignment");
        titleDialog.setHeaderText("Enter Assignment Title");
        titleDialog.setContentText("Title:");
        titleDialog.showAndWait().ifPresent(title -> {
            TextInputDialog dueDateDialog = new TextInputDialog(LocalDateTime.now().toString());
            dueDateDialog.setTitle("Create Assignment");
            dueDateDialog.setHeaderText("Enter Due Date (e.g., 2025-05-23 23:59:00)");
            dueDateDialog.setContentText("Due Date:");
            dueDateDialog.showAndWait().ifPresent(dueDateStr -> {
                try {
                    LocalDateTime dueDate = LocalDateTime.parse(dueDateStr);
                    try (Connection conn = DBConnection.connect()) {
                        String sql = "INSERT INTO assignments (course_id, assignment_title, due_date) VALUES (?, ?, ?)";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, selectedCourse.getCourseId());
                        stmt.setString(2, title);
                        stmt.setObject(3, dueDate);
                        stmt.executeUpdate();
                        loadAssignments();
                        showAlert("Success", "Assignment created successfully!");
                    } catch (SQLException e) {
                        showAlert("Error", "Failed to create assignment: " + e.getMessage());
                    }
                } catch (Exception e) {
                    showAlert("Error", "Invalid due date format. Use YYYY-MM-DD HH:MM:SS.");
                }
            });
        });
    }

    @FXML
    private void editAssignment() {
        Assignment selectedAssignment = assignmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            showAlert("Error", "Please select an assignment!");
            return;
        }

        TextInputDialog titleDialog = new TextInputDialog(selectedAssignment.getAssignmentTitle());
        titleDialog.setTitle("Edit Assignment");
        titleDialog.setHeaderText("Enter New Assignment Title");
        titleDialog.setContentText("Title:");
        titleDialog.showAndWait().ifPresent(newTitle -> {
            TextInputDialog dueDateDialog = new TextInputDialog(selectedAssignment.getDueDate().toString());
            dueDateDialog.setTitle("Edit Assignment");
            dueDateDialog.setHeaderText("Enter New Due Date (e.g., 2025-05-23 23:59:00)");
            dueDateDialog.setContentText("Due Date:");
            dueDateDialog.showAndWait().ifPresent(newDueDateStr -> {
                try {
                    LocalDateTime newDueDate = LocalDateTime.parse(newDueDateStr);
                    try (Connection conn = DBConnection.connect()) {
                        String sql = "UPDATE assignments SET assignment_title = ?, due_date = ? WHERE assignment_id = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setString(1, newTitle);
                        stmt.setObject(2, newDueDate);
                        stmt.setInt(3, selectedAssignment.getAssignmentId());
                        stmt.executeUpdate();
                        loadAssignments();
                        showAlert("Success", "Assignment updated successfully!");
                    } catch (SQLException e) {
                        showAlert("Error", "Failed to update assignment: " + e.getMessage());
                    }
                } catch (Exception e) {
                    showAlert("Error", "Invalid due date format. Use YYYY-MM-DD HH:MM:SS.");
                }
            });
        });
    }

    @FXML
    private void deleteAssignment() {
        Assignment selectedAssignment = assignmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            showAlert("Error", "Please select an assignment!");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String sql = "DELETE FROM assignments WHERE assignment_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedAssignment.getAssignmentId());
            stmt.executeUpdate();
            loadAssignments();
            showAlert("Success", "Assignment deleted successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete assignment: " + e.getMessage());
        }
    }

    @FXML
    private void gradeSubmissions() {
        Course selectedCourse = assignmentsCourseComboBox.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert("Error", "Please select a course!");
            return;
        }

        ObservableList<SubmissionWrapper> submissions = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT s.submission_id, s.user_id, s.score, s.submission_date, u.full_name AS student_name " +
                    "FROM submissions s JOIN users u ON s.user_id = u.user_id " +
                    "WHERE s.assignment_id IN (SELECT assignment_id FROM assignments WHERE course_id = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedCourse.getCourseId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String submitted = rs.getTimestamp("submission_date") != null ? "Yes" : "No";
                submissions.add(new SubmissionWrapper(rs.getInt("submission_id"), rs.getInt("user_id"),
                        rs.getDouble("score"), submitted, rs.getString("student_name")));
            }
            // Add unsubmitted assignments with grade 0
            String checkSql = "SELECT u.user_id, u.full_name FROM users u JOIN enrollments e ON u.user_id = e.user_id " +
                    "WHERE e.course_id = ? AND u.role = 'Student' AND u.user_id NOT IN " +
                    "(SELECT user_id FROM submissions s JOIN assignments a ON s.assignment_id = a.assignment_id WHERE a.course_id = ?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, selectedCourse.getCourseId());
            checkStmt.setInt(2, selectedCourse.getCourseId());
            ResultSet checkRs = checkStmt.executeQuery();
            while (checkRs.next()) {
                submissions.add(new SubmissionWrapper(-1, checkRs.getInt("user_id"), 0.0, "No", checkRs.getString("full_name")));
            }
            submissionsTable.setItems(submissions);
        } catch (SQLException e) {
            System.err.println("Failed to load submissions: " + e.getMessage());
        }

        // Open grading dialog
        if (!submissions.isEmpty()) {
            Stage gradeStage = new Stage();
            gradeStage.setTitle("Grade Submissions");
            VBox gradeVBox = new VBox(10);
            for (SubmissionWrapper sub : submissions) {
                HBox row = new HBox(10);
                Label studentLabel = new Label(sub.getStudentName());
                TextField scoreField = new TextField(String.valueOf(sub.getScore()));
                scoreField.setPrefWidth(50);
                row.getChildren().addAll(studentLabel, scoreField);
                gradeVBox.getChildren().add(row);
            }
            Button saveButton = new Button("Save Grades");
            saveButton.setOnAction(e -> {
                for (int i = 0; i < submissions.size(); i++) {
                    SubmissionWrapper sub = submissions.get(i);
                    TextField scoreField = (TextField) ((HBox) gradeVBox.getChildren().get(i)).getChildren().get(1);
                    try {
                        double score = Double.parseDouble(scoreField.getText());
                        if (score >= 0) {
                            try (Connection conn = DBConnection.connect()) {
                                if (sub.getSubmissionId() == -1) {
                                    String insertSql = "INSERT INTO submissions (assignment_id, user_id, score) " +
                                            "SELECT a.assignment_id, ?, ? FROM assignments a WHERE a.course_id = ? LIMIT 1";
                                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                                    insertStmt.setInt(1, sub.getUserId());
                                    insertStmt.setDouble(2, score);
                                    insertStmt.setInt(3, selectedCourse.getCourseId());
                                    insertStmt.executeUpdate();
                                } else {
                                    String updateSql = "UPDATE submissions SET score = ? WHERE submission_id = ?";
                                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                                    updateStmt.setDouble(1, score);
                                    updateStmt.setInt(2, sub.getSubmissionId());
                                    updateStmt.executeUpdate();
                                }
                            } catch (SQLException ex) {
                                showAlert("Error", "Failed to save grade: " + ex.getMessage());
                            }
                        } else {
                            showAlert("Error", "Score must be non-negative!");
                        }
                    } catch (NumberFormatException ex) {
                        showAlert("Error", "Invalid score format!");
                    }
                }
                loadAssignments(); // Refresh to reflect changes
                gradeStage.close();
                showAlert("Success", "Grades saved successfully!");
            });
            gradeVBox.getChildren().add(saveButton);
            gradeStage.setScene(new Scene(gradeVBox, 300, 400));
            gradeStage.show();
        } else {
            showAlert("Info", "No submissions to grade for this course.");
        }
    }

    @FXML
    private void uploadMaterial() {
        Course selectedCourse = materialsCourseComboBox.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert("Error", "Please select a course!");
            return;
        }

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Upload Material");
        titleDialog.setHeaderText("Enter Material Title");
        titleDialog.setContentText("Title:");
        titleDialog.showAndWait().ifPresent(title -> {
            TextInputDialog fileDialog = new TextInputDialog();
            fileDialog.setTitle("Upload Material");
            fileDialog.setHeaderText("Enter File Name (e.g., notes.pdf)");
            fileDialog.setContentText("File Name:");
            fileDialog.showAndWait().ifPresent(fileName -> {
                try (Connection conn = DBConnection.connect()) {
                    String sql = "INSERT INTO course_materials (course_id, title, file_name) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, selectedCourse.getCourseId());
                    stmt.setString(2, title);
                    stmt.setString(3, fileName);
                    stmt.executeUpdate();
                    loadMaterials();
                    showAlert("Success", "Material uploaded successfully!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to upload material: " + e.getMessage());
                }
            });
        });
    }

    @FXML
    private void goToHome() {
        try {
            Parent homeParent = FXMLLoader.load(getClass().getResource("/com/example/learningmanagementsystem/HomePage.fxml"));
            Scene homeScene = new Scene(homeParent);
            Stage stage = (Stage) coursesTable.getScene().getWindow();
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

    public static class Assignment {
        private final int assignmentId;
        private final StringProperty assignmentTitle;
        private final StringProperty dueDate;

        public Assignment(int assignmentId, String assignmentTitle, java.sql.Timestamp dueDate) {
            this.assignmentId = assignmentId;
            this.assignmentTitle = new SimpleStringProperty(assignmentTitle);
            this.dueDate = new SimpleStringProperty(dueDate != null ? dueDate.toString() : "");
        }

        public int getAssignmentId() { return assignmentId; }
        public String getAssignmentTitle() { return assignmentTitle.get(); }
        public String getDueDate() { return dueDate.get(); }

        public IntegerProperty assignmentIdProperty() { return new SimpleIntegerProperty(assignmentId); }
        public StringProperty assignmentTitleProperty() { return assignmentTitle; }
        public StringProperty dueDateProperty() { return dueDate; }
    }

    public static class SubmissionWrapper {
        private final int submissionId;
        private final int userId;
        private double score;
        private final StringProperty submitted;
        private final StringProperty studentName;

        public SubmissionWrapper(int submissionId, int userId, double score, String submitted, String studentName) {
            this.submissionId = submissionId;
            this.userId = userId;
            this.score = score;
            this.submitted = new SimpleStringProperty(submitted);
            this.studentName = new SimpleStringProperty(studentName);
        }

        public int getSubmissionId() { return submissionId; }
        public int getUserId() { return userId; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getSubmitted() { return submitted.get(); }
        public String getStudentName() { return studentName.get(); }

        public IntegerProperty submissionIdProperty() { return new SimpleIntegerProperty(submissionId); }
        public StringProperty studentNameProperty() { return studentName; }
        public StringProperty submittedProperty() { return submitted; }
    }
}