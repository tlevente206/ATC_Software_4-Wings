package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ArrivalsDialogController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightNumberColumn;
    @FXML private TableColumn<Flight, String> airlineColumn;
    @FXML private TableColumn<Flight, String> originColumn;
    @FXML private TableColumn<Flight, String> schedArrColumn;
    @FXML private TableColumn<Flight, String> estArrColumn;
    @FXML private TableColumn<Flight, String> gateColumn;
    @FXML private TableColumn<Flight, String> statusColumn;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        if (flightsTable == null) {
            return;
        }

        // Járatszám
        flightNumberColumn.setCellValueFactory(cellData -> {
            Flight f = cellData.getValue();
            String value = (f != null && f.getFlightNumber() != null)
                    ? f.getFlightNumber()
                    : "";
            return new SimpleStringProperty(value);
        });

        // Légitársaság neve
        airlineColumn.setCellValueFactory(cellData -> {
            Flight f = cellData.getValue();
            String value = "";
            if (f != null && f.getAirline() != null) {
                try {
                    value = f.getAirline().getName();
                } catch (Exception ignored) {
                    value = "";
                }
            }
            return new SimpleStringProperty(value);
        });

        // Kiindulási repülőtér neve
        originColumn.setCellValueFactory(cellData -> {
            Flight f = cellData.getValue();
            String value = "";
            if (f != null && f.getDepartureAirport() != null) {
                try {
                    value = f.getDepartureAirport().getName();
                } catch (Exception ignored) {
                    value = "";
                }
            }
            return new SimpleStringProperty(value);
        });

        // Menetrend szerinti érkezés (idő)
        schedArrColumn.setCellValueFactory(cellData -> {
            Flight f = cellData.getValue();
            LocalDateTime dt = (f != null) ? f.getScheduledArrival() : null;
            String value = formatTime(dt);
            return new SimpleStringProperty(value);
        });

        // Várható érkezés (idő)
        estArrColumn.setCellValueFactory(cellData -> {
            Flight f = cellData.getValue();
            LocalDateTime dt = (f != null) ? f.getEstimatedArrival() : null;
            String value = formatTime(dt);
            return new SimpleStringProperty(value);
        });

        // Kapu kód
        gateColumn.setCellValueFactory(cellData -> {
            Flight f = cellData.getValue();
            String value = "";
            if (f != null && f.getGate() != null) {
                try {
                    value = f.getGate().getCode();
                } catch (Exception ignored) {
                    value = "";
                }
            }
            return new SimpleStringProperty(value);
        });

        // Státusz
        statusColumn.setCellValueFactory(cellData -> {
            Flight f = cellData.getValue();
            String value = "";
            if (f != null && f.getStatus() != null) {
                try {
                    // ha van displayName, akkor azt, különben toString
                    value = f.getStatus().toString();
                } catch (Exception ignored) {
                    value = "";
                }
            }
            return new SimpleStringProperty(value);
        });
    }

    private String formatTime(LocalDateTime dt) {
        return dt != null ? TIME_FORMATTER.format(dt) : "";
    }

    public void init(Airports airport, List<Flight> arrivals) {
        String icao = "";
        String name = "";

        try {
            if (airport != null && airport.getIcaoCode() != null) {
                icao = airport.getIcaoCode();
            }
        } catch (Exception ignored) {}

        try {
            if (airport != null && airport.getName() != null) {
                name = airport.getName();
            }
        } catch (Exception ignored) {}

        titleLabel.setText("Érkező járatok – " + icao);
        subtitleLabel.setText("Reptér: " + icao + " – " + name);

        flightsTable.getItems().setAll(arrivals);
        statusLabel.setText("Találatok: " + arrivals.size());
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) flightsTable.getScene().getWindow();
        stage.close();
    }
}