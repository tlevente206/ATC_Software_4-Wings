// src/main/java/com/FourWings/atcSystem/frontend/FlightWeatherAssistantDialogController.java
package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.service.weather.FlightWeatherAdvice;
import com.FourWings.atcSystem.service.weather.FlightWeatherAdvisorService;
import com.FourWings.atcSystem.model.airport.AirportsService;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlightWeatherAssistantDialogController {

    private final AirportsService airportsService;
    private final FlightWeatherAdvisorService advisorService;

    public FlightWeatherAssistantDialogController(AirportsService airportsService,
                                                  FlightWeatherAdvisorService advisorService) {
        this.airportsService = airportsService;
        this.advisorService = advisorService;
    }

    @FXML private ComboBox<Airports> airportCombo;
    @FXML private Label statusLabel;
    @FXML private TextArea resultArea;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        // Repterek betöltése DB-ből
        List<Airports> airports = airportsService.getAllAirports(); // ha más a metódus neve, írd át
        airportCombo.setItems(FXCollections.observableArrayList(airports));

        // hogyan jelenjen meg a ComboBox-ban
        airportCombo.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Airports item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // pl. "LHBP – Budapest" jelleggel
                    setText(item.getCode() + " – " + item.getCity());
                }
            }
        });

        airportCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Airports item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCode() + " – " + item.getCity());
                }
            }
        });
    }

    @FXML
    private void onAnalyzeWeather() {
        errorLabel.setText("");
        statusLabel.setText("Státusz: ...");
        resultArea.clear();

        Airports selected = airportCombo.getValue();
        if (selected == null) {
            errorLabel.setText("Válassz egy repülőteret.");
            return;
        }

        Task<FlightWeatherAdvice> task = new Task<>() {
            @Override
            protected FlightWeatherAdvice call() throws Exception {
                return advisorService.analyzeForAirport(selected);
            }
        };

        task.setOnSucceeded(e -> {
            FlightWeatherAdvice advice = task.getValue();
            statusLabel.setText("Státusz: " + advice.status());
            resultArea.setText(advice.explanation());

            // opcionális színezés
            switch (advice.status()) {
                case "GO"      -> statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                case "CAUTION" -> statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                case "DELAY"   -> statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                default        -> statusLabel.setStyle("");
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            errorLabel.setText("Hiba az elemzés közben: " +
                    (ex != null ? ex.getMessage() : "ismeretlen"));
            ex.printStackTrace();
            statusLabel.setText("Státusz: HIBA");
        });

        new Thread(task, "weather-analyze").start();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}