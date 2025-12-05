package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

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

    @FXML
    private void initialize() {
        if (flightsTable != null) {
            flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
            airlineColumn.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
            originColumn.setCellValueFactory(new PropertyValueFactory<>("originName"));

            schedArrColumn.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));
            estArrColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedArrivalText"));
            gateColumn.setCellValueFactory(new PropertyValueFactory<>("gateCode"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        }
    }

    public void init(Airports airport, List<Flight> arrivals) {
        String icao = airport.getIcaoCode() != null ? airport.getIcaoCode() : "";
        String name = airport.getName() != null ? airport.getName() : "";

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