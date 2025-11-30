package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.springframework.stereotype.Component;

@Component
public class RoutesPageController {

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
                            break;
                        case "Kapuk(Ez inkább a repterekhez menne)":
                            SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", 600, 400);
                            break;
                        case "Terminál(Ez is inkább reptér)":
                            SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 600, 400);
                            break;
                    }
                }
            });
            menuComboBox.getSelectionModel().select("Repülőutak");
        }
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

}