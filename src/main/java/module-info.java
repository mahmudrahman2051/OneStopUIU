module com.example.onestopuiu {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires transitive java.sql;
    requires java.naming;
    requires java.desktop;

    opens com.example.onestopuiu to javafx.fxml;
    opens com.example.onestopuiu.controller to javafx.fxml;
    opens com.example.onestopuiu.model to javafx.base;
    opens com.example.onestopuiu.dao to javafx.base;
    opens com.example.onestopuiu.util to javafx.base;

    exports com.example.onestopuiu;
    exports com.example.onestopuiu.controller;
    exports com.example.onestopuiu.model;
    exports com.example.onestopuiu.dao;
    exports com.example.onestopuiu.util;
}