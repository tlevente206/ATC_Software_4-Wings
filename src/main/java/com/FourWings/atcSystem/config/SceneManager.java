package com.FourWings.atcSystem.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class SceneManager {

    public static Stage primaryStage;
    private static ConfigurableApplicationContext applicationContext;

    // Ezt hívod meg az Application start() elején vagy a loading után
    public static void init(Stage stage) {
        primaryStage = stage;
    }

    // Ezt hívod meg, amint a Spring elindult (Task onSucceeded)
    public static void setApplicationContext(ConfigurableApplicationContext context) {
        applicationContext = context;
    }

    public static <T> T switchTo(String fxml, String title, int width, int height) {
        // --- 1. OKOS ÚTVONAL KEZELÉS ---
        String path = fxml;

        // Ha nincs perjel az elején, teszünk
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // Ha nincs benne az "fxml" mappa, beletesszük
        if (!path.contains("/fxml/")) {
            path = "/fxml" + path;
        }

        System.out.println("SceneManager: FXML betöltése innen: " + path);

        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));

            // --- 2. JAVÍTOTT SPRING INTEGRÁCIÓ ---
            // Csak akkor használjuk a Springet bean-gyártásra, ha már létezik a kontextus!
            if (applicationContext != null) {
                loader.setControllerFactory(applicationContext::getBean);
            } else {
                // Ha még nincs Spring (pl. Loading képernyőnél vagy tesztnél),
                // akkor hagyjuk, hogy a JavaFX a default konstruktort használja.
                // Ez megakadályozza a NullPointerException-t a getBean hívásnál.
                System.out.println("FIGYELEM: Spring Context még nincs beállítva, alap példányosítás történik.");
            }

            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);

            if (primaryStage == null) {
                primaryStage = new Stage();
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
            primaryStage.show();

            return loader.getController();

        } catch (Exception ex) {
            // Részletes hibaüzenet, hogy tudd, mi történt
            System.err.println("KRITIKUS HIBA a(z) " + path + " betöltésekor!");
            ex.printStackTrace();
            throw new RuntimeException("Nem sikerült betölteni: " + path, ex);
        }
    }
}