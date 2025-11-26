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
                            SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", 800, 600);
                            break;
                        case "Repülők":
                            SceneManager.switchTo("PlanesPage.fxml", "ATC – Repülők", 800, 600);
                            break;
                        case "Repterek":
                            SceneManager.switchTo("AirportsPage.fxml", "ATC – Repterek", 800, 600);
                            break;
                        case "Repülőutak":
                            SceneManager.switchTo("RoutesPage.fxml", "ATC – Repülőutak", 800, 600);
                            break;
                        case "Kapuk(Ez inkább a repterekhez menne)":
                            SceneManager.switchTo("GatesPage.fxml", "ATC – Kapuk", 800, 600);
                            break;
                        case "Terminál(Ez is inkább reptér)":
                            SceneManager.switchTo("TerminalPage.fxml", "ATC – Terminál", 800, 600);
                            break;
                    }
                }
            });
            menuComboBox.getSelectionModel().select("Kapuk(Ez inkább a repterekhez menne)");
        }
    }
}