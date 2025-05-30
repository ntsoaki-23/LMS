package com.example.learningmanagementsystem.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Course {

    private final IntegerProperty courseId;
    private final StringProperty courseName;
    private final IntegerProperty instructorUserId;

    public Course(int courseId, String courseName, int instructorUserId) {
        this.courseId = new SimpleIntegerProperty(courseId);
        this.courseName = new SimpleStringProperty(courseName);
        this.instructorUserId = new SimpleIntegerProperty(instructorUserId);
    }

    // Getters
    public int getCourseId() { return courseId.get(); }
    public String getCourseName() { return courseName.get(); }
    public int getInstructorUserId() { return instructorUserId.get(); }

    // Property Getters
    public IntegerProperty courseIdProperty() { return courseId; }
    public StringProperty courseNameProperty() { return courseName; }
    public IntegerProperty instructorUserIdProperty() { return instructorUserId; }
}