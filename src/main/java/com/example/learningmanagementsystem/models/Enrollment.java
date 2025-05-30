package com.example.learningmanagementsystem.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;

public class Enrollment {

    private final IntegerProperty enrollmentId;
    private final IntegerProperty userId;
    private final IntegerProperty courseId;
    private final StringProperty enrollmentDate;
    private final Double progress;

    public Enrollment(int enrollmentId, int userId, int courseId, String enrollmentDate, double progress) {
        this.enrollmentId = new SimpleIntegerProperty(enrollmentId);
        this.userId = new SimpleIntegerProperty(userId);
        this.courseId = new SimpleIntegerProperty(courseId);
        this.enrollmentDate = new SimpleStringProperty(enrollmentDate); // Simplified as String for now
        this.progress = progress;
    }

    // Getters
    public int getEnrollmentId() { return enrollmentId.get(); }
    public int getUserId() { return userId.get(); }
    public int getCourseId() { return courseId.get(); }
    public String getEnrollmentDate() { return enrollmentDate.get(); }
    public double getProgress() { return progress; }

    // Property Getters
    public IntegerProperty enrollmentIdProperty() { return enrollmentId; }
    public IntegerProperty userIdProperty() { return userId; }
    public IntegerProperty courseIdProperty() { return courseId; }
    public StringProperty enrollmentDateProperty() { return enrollmentDate; }
    // No progress property since it's a simple double for now
}