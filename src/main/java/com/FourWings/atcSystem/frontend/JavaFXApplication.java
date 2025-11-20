package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.AtcSystemApplication;
import com.FourWings.atcSystem.config.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext ctx;

    @Override
    public void start(Stage stage) {
        try {
            // 1) Spring Boot indítása
            ctx = SpringApplication.run(AtcSystemApplication.class);

            // 2) SceneManager inicializálása *A KAPOTT* stage-dzsel
            SceneManager.init(stage);

            // 3) Első oldal betöltése (amit szeretnél: MainPage vagy HomePage)
            SceneManager.switchTo("MainPage.fxml", "ATC System", 800, 400);

            // 4) Ikon beállítása (ha kell)
            stage.getIcons().add(
                    new Image(getClass()
                            .getResource("/images/1.png")
                            .toExternalForm())
            );

        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (ctx != null) {
            ctx.close();
        }
        Platform.exit();
    }
}