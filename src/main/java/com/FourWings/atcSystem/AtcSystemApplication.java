package com.FourWings.atcSystem;

import com.FourWings.atcSystem.config.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class AtcSystemApplication extends Application {

    private ConfigurableApplicationContext applicationContext;
    private Stage loadingStage;

    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. LÉPÉS: Loading Screen mutatása (Ennek nem kell AuthService, így nem fagy le)
        showLoadingScreen(primaryStage);

        // 2. LÉPÉS: Spring indítása a háttérben
        Task<ConfigurableApplicationContext> springTask = new Task<>() {
            @Override
            protected ConfigurableApplicationContext call() {
                return new SpringApplicationBuilder(AtcSystemApplication.class)
                        .web(WebApplicationType.NONE)
                        .run();
            }
        };

        // 3. LÉPÉS: Amikor a Spring végzett (SIKER)
        springTask.setOnSucceeded(e -> {
            applicationContext = springTask.getValue();

            // FONTOS: Beállítjuk a kontextust, hogy a SceneManager tudjon Controllert gyártani
            SceneManager.setApplicationContext(applicationContext);
            SceneManager.init(new Stage()); // Új stage a fő alkalmazásnak

            // Loading bezárása
            if (loadingStage != null) {
                loadingStage.close();
            }

            // CSAK MOST nyitjuk meg a MainPage-et, amikor már van Spring Context!
            showMainPage();
        });

        // 4. LÉPÉS: Ha hiba van
        springTask.setOnFailed(e -> {
            System.err.println("Kritikus hiba: Nem sikerült elindítani a Springet!");
            springTask.getException().printStackTrace();
            Platform.exit();
        });

        // Szál indítása
        new Thread(springTask).start();
    }

    // Loading képernyő (Direkt betöltés, SceneManager nélkül, hogy ne kelljen neki Context)
    private void showLoadingScreen(Stage stage) throws IOException {
        this.loadingStage = stage;
        // Figyelj: itt direktben hívjuk az FXML-t
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoadingPage.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/1.png")));
        } catch (Exception ignored) {}

        stage.show();
    }

    // Főoldal megnyitása (Itt már használjuk a SceneManager-t)
    private void showMainPage() {
        try {
            // Itt már a SceneManager.switchTo fog működni, mert a kontextus be van állítva
            SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);

            // Ikon beállítása az új ablakra
            try {
                if (SceneManager.primaryStage != null) {
                    SceneManager.primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/1.png")));
                }
            } catch (Exception ignored) {}

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
        Platform.exit();
    }
}