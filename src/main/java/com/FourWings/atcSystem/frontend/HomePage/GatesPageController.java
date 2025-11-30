package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.springframework.stereotype.Component;

@Component
public class GatesPageController {

    @FXML
    private ComboBox<String> menuComboBox;

    @FXML
    public void initialize() {
        if (menuComboBox != null) {
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    switch (newVal) {
                        case "Főoldal":
                            SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", 600, 400);
                            break;
                        case "Repülők":
                            SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 600, 400);
                            break;
                        case "Repterek":
                            SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", 800, 400);
                            break;
                        case "Repülőutak":
                            SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", 600, 400);
                            break;
                        case "Kapuk(Ez inkább a repterekhez menne)":
                            break;
                        case "Terminál(Ez is inkább reptér)":
                            SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 600, 400);
                            break;
                    }
                }
            });
            menuComboBox.getSelectionModel().select("Kapuk(Ez inkább a repterekhez menne)");
        }
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}