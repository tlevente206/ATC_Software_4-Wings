package com.FourWings.atcSystem.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class WeatherDetailsDialogController {

    @FXML private Label airportLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label conditionLabel;
    @FXML private Label windLabel;
    @FXML private Label visibilityLabel;
    @FXML private Label pressureLabel;
    @FXML private Label feelsLikeLabel;
    @FXML private Label updatedAtLabel;
    @FXML private TextArea metarTextArea;
    @FXML private Button closeButton;

    // Egyelőre csak UI – adatokat később töltjük be
    public void initStatic() {
        // Ha szeretnéd, itt később átadhatunk adatokat a fő controllerből
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}