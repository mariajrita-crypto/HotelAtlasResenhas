package com.example.hotel_atlas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/example/hotel_atlas/login-view.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 768);
        stage.initStyle(StageStyle.UNDECORATED);              // sem bordas nem barra de título
        stage.setTitle("Hotel Atlas | Hospedagem e experiências");
        stage.setScene(scene);
        stage.setFullScreenExitHint("");                      // sem aviso de "pressione ESC"
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // ESC não sai da tela cheia
        stage.setFullScreen(true);                            // as outras telas herdam o estado da janela
        stage.show();
    }
    public static void main(String[] args) { launch(); }
}
