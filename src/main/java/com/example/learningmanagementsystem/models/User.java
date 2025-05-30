package com.example.learningmanagementsystem.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

    private final IntegerProperty userId;
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty password;
    private final StringProperty role;

    public User(int userId, String fullName, String email, String password, String role) {
        this.userId = new SimpleIntegerProperty(userId);
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.password = new SimpleStringProperty(password); // To be hashed in production
        this.role = new SimpleStringProperty(role);
    }

    // Getters
    public int getUserId() { return userId.get(); }
    public String getFullName() { return fullName.get(); }
    public String getEmail() { return email.get(); }
    public String getPassword() { return password.get(); }
    public String getRole() { return role.get(); }

    // Property Getters
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty roleProperty() { return role; }
}