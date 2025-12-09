package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.service.WeatherService;
import com.FourWings.atcSystem.service.dto.AirportWeatherInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;

import java.net.URL;

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

    @FXML private WebView weatherMapView;
    private WebEngine weatherMapEngine;
    private boolean mapLoaded = false;

    public WeatherDetailsDialogController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public void init(Airports airport) {
        this.airport = airport;
        loadWeather();
        initWeatherMap();
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
            //metarLabel.setText(info.metarRaw());

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

    private void initWeatherMap() {
        if (weatherMapView == null) {
            return;
        }

        weatherMapEngine = weatherMapView.getEngine();
        weatherMapEngine.setJavaScriptEnabled(true);

        URL url = getClass().getResource("/weather-map.html");
        if (url == null) {
            System.out.println("[HIBA] Időjárási térkép HTML (weather-map.html) nem található.");
            return;
        }

        weatherMapEngine.load(url.toExternalForm());
        weatherMapEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                mapLoaded = true;
                showAirportOnMap();
            }
        });
    }

    private void showAirportOnMap() {
        if (!mapLoaded || weatherMapEngine == null || airport == null) {
            return;
        }
        if (airport.getLatitude() == null || airport.getLongitude() == null) {
            System.out.println("[INFO] A kiválasztott repülőtérhez nincs koordináta, nem rajzolható ki a térképre.");
            return;
        }

        double lat = airport.getLatitude().doubleValue();
        double lon = airport.getLongitude().doubleValue();

        String label = "";
        if (airport.getIcaoCode() != null && !airport.getIcaoCode().isBlank()) {
            label += airport.getIcaoCode() + " – ";
        }
        if (airport.getName() != null) {
            label += airport.getName();
        }

        String jsLabel = label.replace("'", "\\'");

        String script = String.format("showAirport(%f, %f, '%s');", lat, lon, jsLabel);
        try {
            weatherMapEngine.executeScript(script);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("[HIBA] Nem sikerült frissíteni a térképet: " + ex.getMessage());
        }
    }
}