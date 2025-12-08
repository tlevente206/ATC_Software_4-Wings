package com.FourWings.atcSystem.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class DeparturesDialogController {

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<?> departuresTable;

    @FXML
    private Button closeButton;

    @FXML
    private void initialize() {
        if (titleLabel != null) {
            titleLabel.setText("Induló járatok – csak UI, még nincs funkcionalitás 🙂");
        }
        if (departuresTable != null) {
            departuresTable.setPlaceholder(
                    new Label("Jelenleg nincs megjelenítendő adat. Később ide jönnek az induló járatok.")
            );
        }
    }

    @FXML
    private void onClose() {
        if (closeButton != null && closeButton.getScene() != null) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }
}