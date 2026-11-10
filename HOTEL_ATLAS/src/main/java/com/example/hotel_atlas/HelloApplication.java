package com.example.hotel_atlas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/example/hotel_atlas/login-view.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 768);
        stage.setTitle("Hotel Atlas | Hospedagem e experiências");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.setMaximized(true); // abre em tela cheia; as outras telas herdam o estado da janela
        stage.show();
    }
    public static void main(String[] args) { launch(); }
}
