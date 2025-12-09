package com.FourWings.atcSystem.config;

import com.FourWings.atcSystem.config.SpringContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    //  LEVIII EZ ITT AZ ELSŐ
    private static String lastLoadedFxml;
    private static String lastLoadedTitle;
    private static int lastWidth;
    private static int lastHeight;

    private static final String DARK_THEME =
            ThemeManager.class.getResource("/styles/dark-theme.css").toExternalForm();
    private static final String LIGHT_THEME =
            ThemeManager.class.getResource("/styles/light-theme.css").toExternalForm();
    //  LEVIII AZ ELSŐ EDDIG TART


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

            // LEVIII EZ ITT A MÁSODIK
            // Theme alkalmazása
            scene.getStylesheets().clear();
            if (ThemeManager.isDarkMode()) {
                scene.getStylesheets().add(DARK_THEME);
            } else {
                scene.getStylesheets().add(LIGHT_THEME);
            }
            // LEVIII EDDIG TART A MÁSODIK

            // LEVIII EZ ITT A HARMADIK
            // --- Save last loaded scene info ---
            lastLoadedFxml = fxml;
            lastLoadedTitle = title;
            lastWidth = width;
            lastHeight = height;
            // LEVIII EDDIG TART A HARMADIK


            return loader.getController();

        } catch (Exception ex) {
            throw new RuntimeException("Nem sikerült betölteni: " + fxml + "    " + "/fxml/" + fxml, ex);
        }
    }

    public static void reloadCurrentScene() {
        if (lastLoadedFxml == null) return; // Safety
        switchTo(lastLoadedFxml, lastLoadedTitle, lastWidth, lastHeight);
    }

}