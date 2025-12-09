package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.service.WeatherService;
import com.FourWings.atcSystem.service.dto.AirportWeatherInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class WeatherDetailsDialogController {

    private final WeatherService weatherService;
    private Airports airport;

    @FXML private Label titleLabel;
    @FXML private Label emojiLabel;
    @FXML private Label tempLabel;
    @FXML private Label conditionLabel;
    @FXML private Label windLabel;
    @FXML private Label visibilityLabel;
    @FXML private Label pressureLabel;
    @FXML private Label feelsLikeLabel;
    @FXML private Label updatedAtLabel;
    @FXML private TextArea metarLabel;

    public WeatherDetailsDialogController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public void init(Airports airport) {
        this.airport = airport;
        loadWeather();
    }

    private void loadWeather() {
        if (airport == null) return;

        try {
            AirportWeatherInfo info = weatherService.getCurrentWeatherForAirport(airport);
            if (info == null) return;

            if (titleLabel != null) {
                titleLabel.setText("Időjárás – " +
                        (airport.getIcaoCode() != null ? airport.getIcaoCode() : "") +
                        " – " +
                        (airport.getName() != null ? airport.getName() : ""));
            }

            emojiLabel.setText(info.emoji());
            tempLabel.setText(String.format("%.1f °C", info.temperatureC()));
            conditionLabel.setText(info.conditionText());
            windLabel.setText(info.windText());
            visibilityLabel.setText(info.visibilityText());
            pressureLabel.setText(info.pressureText());
            feelsLikeLabel.setText(info.feelsLikeText());
            updatedAtLabel.setText(info.updatedAtText());
            metarLabel.setText(info.metarRaw());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onClose() {
        if (titleLabel != null && titleLabel.getScene() != null) {
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.close();
        }
    }
}