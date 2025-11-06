package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.AtcSystemApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;


public class JavaFXApplication extends Application {
    private ConfigurableApplicationContext ctx;

    @Override
    public void start(Stage stage) {
        try {
            ctx = SpringApplication.run(AtcSystemApplication.class);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPage.fxml"));
            loader.setControllerFactory(ctx::getBean);
            Parent root = loader.load();

            stage.setTitle("ATC System");
            stage.setScene(new Scene(root, 600, 400));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (ctx != null) ctx.close();
        Platform.exit();
    }
}
