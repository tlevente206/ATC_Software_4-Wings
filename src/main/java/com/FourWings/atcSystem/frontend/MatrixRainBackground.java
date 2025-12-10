package com.FourWings.atcSystem.frontend;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Random;

public class MatrixRainBackground {

    public static void apply(Pane root) {

        Canvas canvas = new Canvas(root.getPrefWidth(), root.getPrefHeight());
        root.getChildren().add(0, canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        Font font = new Font("monospace", 20);
        gc.setFont(font);

        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();

        int columns = width / 20;
        int[] yPositions = new int[columns];
        Random random = new Random();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                gc.setFill(Color.rgb(0, 0, 0, 0.05));
                gc.fillRect(0, 0, width, height);

                gc.setFill(Color.web("#00ff88"));

                for (int i = 0; i < columns; i++) {
                    String character = String.valueOf((char) (0x30A0 + random.nextInt(96)));
                    gc.fillText(character, i * 20, yPositions[i] * 20);

                    if (yPositions[i] * 20 > height && random.nextDouble() > 0.975) {
                        yPositions[i] = 0;
                    }
                    yPositions[i]++;
                }
            }
        };
        timer.start();
    }
}