package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.springframework.stereotype.Component;

@Component
public class TerminalPageController {

    public static final int WIDTH = 1200; //Window szélesség
    public static final int HEIGHT = 600; //Window magasság
    @FXML
    private ComboBox<String> menuComboBox;

    @FXML
    public void initialize() {
        if (menuComboBox != null) {
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    switch (newVal) {
                        case "Főoldal":
                            SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", WIDTH, HEIGHT);
                            break;
                        case "Repülők":
                            SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", WIDTH, HEIGHT);
                            break;
                        case "Repterek":
                            SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                            break;
                        case "Repülőutak":
                            SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", WIDTH, HEIGHT);
                            break;
                        case "Kapuk(Ez inkább a repterekhez menne)":
                            SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", WIDTH, HEIGHT);
                            break;
                        case "Terminál(Ez is inkább reptér)":
                            break;
                    }
                }
            });
            menuComboBox.getSelectionModel().select("Terminál(Ez is inkább reptér)");
        }
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

}