package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import com.FourWings.atcSystem.model.flight.FlightService;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.stream.Collectors;

import java.util.List;

@Component
public class ArrivalsDialogController {

    @FXML private TableColumn<Flight, String> departureAirportCol;
    @FXML private TableColumn<Flight, String> airlineCol;
    @FXML private TableColumn<Flight, String> flightNumberCol;
    @FXML private TableColumn<Flight, String> scheduledArrivalCol;
    @FXML private TableColumn<Flight, String> statusCol;
    @FXML private TableColumn<Flight, String> estimatedArrivalCol;
    @FXML private TableColumn<Flight, String> actualArrivalCol;
    @FXML private TableColumn<Flight, String> aircraftCol;
    @FXML private TableColumn<Flight, String> gateCol;
    @FXML private TableColumn<Flight, String> updatedAtCol;

    private final FlightService flightService;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    @FXML private TableView<Flight> arrivalsTable;
    @FXML private Label totalArrivalsLabel;

    public ArrivalsDialogController(FlightService flightService) {
        this.flightService = flightService;
    }

    public void init(Airports airport) {

        if (airport == null) return;

        String city = airport.getCity() != null ? airport.getCity() : "";

        if (titleLabel != null) {
            titleLabel.setText(
                    safe(airport.getIcaoCode()) + " – " +
                            safe(airport.getName()) +
                            (city.isBlank() ? "" : " (" + city + ")")
            );
        }

        if (subtitleLabel != null) {
            subtitleLabel.setText("Mai érkező járatok áttekintése");
        }

        // 🔹 Lekérjük az adott reptérre érkező járatokat
        List<Flight> arrivals = flightService.getArrivalsForAirport(airport);
        LocalDate today = LocalDate.now();

        // 🔹 Csak a maiak
        List<Flight> todayArrivals = arrivals.stream()
                .filter(f -> f.getScheduledArrival() != null &&
                        f.getScheduledArrival().toLocalDate().equals(today))
                .collect(Collectors.toList());

        // 🔹 Tábla feltöltése
        if (arrivalsTable != null) {
            arrivalsTable.getItems().setAll(todayArrivals);
        }

        // 🔹 Darabszám kiírása
        if (totalArrivalsLabel != null) {
            totalArrivalsLabel.setText(String.valueOf(todayArrivals.size()));
        }
    }

    @FXML
    public void initialize() {
        setupTable();
    }

    private void setupTable() {

        departureAirportCol.setCellValueFactory(new PropertyValueFactory<>("originName"));
        airlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        flightNumberCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        scheduledArrivalCol.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        estimatedArrivalCol.setCellValueFactory(new PropertyValueFactory<>("estimatedArrivalText"));
        aircraftCol.setCellValueFactory(new PropertyValueFactory<>("aircraftTypeIcao"));
        gateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));
        updatedAtCol.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    @FXML
    private void onClose() {
        if (titleLabel != null && titleLabel.getScene() != null) {
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.close();
        }
    }
}