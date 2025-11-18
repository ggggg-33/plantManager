module org.example.plantmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.plantmanager to javafx.fxml;
    opens org.example.plantmanager.business to javafx.fxml;
    opens org.example.plantmanager.ui to javafx.fxml;
    opens org.example.plantmanager.service to javafx.fxml;
    opens org.example.plantmanager.data to javafx.fxml;

    exports org.example.plantmanager;
    exports org.example.plantmanager.business;
    exports org.example.plantmanager.ui;
    exports org.example.plantmanager.service;
    exports org.example.plantmanager.data;
}