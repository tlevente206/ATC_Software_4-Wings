package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.service.dto.AirportWeatherInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class WeatherDetailsDialogController {

    @FXML private Label airportTitleLabel;

    @FXML private Label weatherEmojiLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label conditionLabel;
    @FXML private Label feelsLikeLabel;

    @FXML private Label windLabel;
    @FXML private Label visibilityLabel;
    @FXML private Label pressureLabel;
    @FXML private Label updatedAtLabel;

    @FXML private TextArea metarLabel;

    // Ezt hívhatod a ControllerHomePageController-ből,
    // ha majd később valódi adatokat akarsz betölteni.
    public void init(Airports airport, AirportWeatherInfo info) {
        if (airport != null && airportTitleLabel != null) {
            String city = airport.getCity() != null ? airport.getCity() : "";
            airportTitleLabel.setText(
                    safe(airport.getIcaoCode()) + " – " +
                            safe(airport.getName()) +
                            (city.isBlank() ? "" : " (" + city + ")")
            );
        }

        if (info != null) {
            if (weatherEmojiLabel != null) weatherEmojiLabel.setText(info.emoji());
            if (temperatureLabel != null) temperatureLabel.setText(String.format("%.0f °C", info.temperatureC()));
            if (conditionLabel != null) conditionLabel.setText(info.conditionText());
            if (feelsLikeLabel != null) feelsLikeLabel.setText(info.feelsLikeText());
            if (windLabel != null) windLabel.setText(info.windText());
            if (visibilityLabel != null) visibilityLabel.setText(info.visibilityText());
            if (pressureLabel != null) pressureLabel.setText(info.pressureText());
            if (updatedAtLabel != null) updatedAtLabel.setText(info.updatedAtText());
            if (metarLabel != null) metarLabel.setText(info.metarRaw());
        }
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    @FXML
    private void onClose() {
        // Kis NPE-safe megoldás
        if (airportTitleLabel != null && airportTitleLabel.getScene() != null) {
            Stage stage = (Stage) airportTitleLabel.getScene().getWindow();
            stage.close();
        }
    }
}