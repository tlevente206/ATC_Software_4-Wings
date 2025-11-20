package com.FourWings.atcSystem.config;

import com.FourWings.atcSystem.config.SpringContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    public static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static <T> T switchTo(String fxml, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/" + fxml));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
            primaryStage.show();

            return loader.getController();

        } catch (Exception ex) {
            throw new RuntimeException("Nem sikerült betölteni: " + fxml, ex);
        }
    }
}