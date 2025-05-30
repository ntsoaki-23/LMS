module com.example.learningmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens com.example.learningmanagementsystem to javafx.fxml;
    opens com.example.learningmanagementsystem.controllers to javafx.fxml;

    exports com.example.learningmanagementsystem;
    exports com.example.learningmanagementsystem.models;
    exports com.example.learningmanagementsystem.controllers;
}