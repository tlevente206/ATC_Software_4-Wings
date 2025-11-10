package com.FourWings.atcSystem.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class HomePageController {

    @FXML
    private ComboBox<String> menuComboBox;

    @FXML
    public void initialize() {
        // Listener a kiválasztott elemre
        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Kiválasztott menü: " + newVal);
                // Itt hívhatod meg az oldalváltást vagy más logikát
            }
        });
    }
}