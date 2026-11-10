module com.example.hotel_atlas {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    // Permite que o JavaFX acesse os controladores e os modelos
    opens com.example.hotel_atlas to javafx.fxml;
    opens com.hotel to javafx.fxml, javafx.base;
    opens com.hotel.model to javafx.base, com.google.gson;
    opens com.hotel.service to javafx.base;

    exports com.example.hotel_atlas;
    exports com.hotel;
    exports com.hotel.model;
}