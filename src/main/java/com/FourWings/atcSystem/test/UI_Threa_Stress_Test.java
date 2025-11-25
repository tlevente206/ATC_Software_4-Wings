package com.FourWings.atcSystem.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class UI_Threa_Stress_Test extends Application {

    private long lastUpdate = System.nanoTime();

    @Override
    public void start(Stage primaryStage) {
        Label statusLabel = new Label("Teszt indul...");
        VBox root = new VBox(statusLabel);
        Scene scene = new Scene(root, 400, 200);

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX UI Thread Teszt");
        primaryStage.show();

        // UI frissítés 60 FPS körül
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(16); // kb. 60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    long now = System.nanoTime();
                    long diffMs = (now - lastUpdate) / 1_000_000;
                    statusLabel.setText("UI frissítés késése: " + diffMs + " ms");
                    lastUpdate = now;
                });
            }
        }).start();

        // Szimuláljunk egy nagy számítást az UI thread-en (NE CSINÁLD ÉLESBEN!)
        // Ha ezt bekapcsolod, látni fogod, hogy az UI megakad.

        Platform.runLater(() -> {
            for (int i = 0; i < 1_000_000_000; i++) {
                Math.sqrt(i);
            }
        });

    }

    public static void main(String[] args) {
        launch(args);
    }
}