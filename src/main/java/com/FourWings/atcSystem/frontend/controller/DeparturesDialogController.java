package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DeparturesDialogController {

    @FXML
    private Label airportLabel;

    @FXML
    private TableView<Flight> departuresTable;

    @FXML
    private TableColumn<Flight, String> colFlightNumber;

    @FXML
    private TableColumn<Flight, String> colAirline;

    @FXML
    private TableColumn<Flight, String> colDestination;

    @FXML
    private TableColumn<Flight, String> colScheduledDep;

    @FXML
    private TableColumn<Flight, String> colEstimatedDep;

    @FXML
    private TableColumn<Flight, String> colGate;

    @FXML
    private TableColumn<Flight, String> colStatus;

    @FXML
    private Button closeButton;

    private final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        // Járatszám
        colFlightNumber.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getFlightNumber() != null
                                ? cell.getValue().getFlightNumber()
                                : ""
                )
        );

        // Légitársaság neve
        colAirline.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getAirline() != null &&
                                cell.getValue().getAirline().getName() != null
                                ? cell.getValue().getAirline().getName()
                                : ""
                )
        );

        // Célállomás – érkező repülőtér neve
        colDestination.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getArrivalAirport() != null &&
                                cell.getValue().getArrivalAirport().getName() != null
                                ? cell.getValue().getArrivalAirport().getName()
                                : ""
                )
        );

        // Menetrend szerinti indulás (scheduledDeparture)
        colScheduledDep.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getScheduledDeparture() != null
                                ? TIME_FMT.format(cell.getValue().getScheduledDeparture())
                                : ""
                )
        );

        // Várható indulás (estimatedDeparture)
        colEstimatedDep.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getEstimatedDeparture() != null
                                ? TIME_FMT.format(cell.getValue().getEstimatedDeparture())
                                : ""
                )
        );

        // Kapu kód
        colGate.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getGate() != null &&
                                cell.getValue().getGate().getCode() != null
                                ? cell.getValue().getGate().getCode()
                                : ""
                )
        );

        // Státusz
        colStatus.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getStatus() != null
                                ? cell.getValue().getStatus().name()
                                : ""
                )
        );
    }

    /**
     * Ezt hívod a ControllerHomePageController-ből:
     *
     * ctrl.init(assignedAirport, departures);
     */
    public void init(Airports airport, List<Flight> flights) {
        if (airport != null) {
            String icao = airport.getIcaoCode() != null ? airport.getIcaoCode() : "";
            String name = airport.getName() != null ? airport.getName() : "";
            airportLabel.setText(icao + " – " + name);
        } else {
            airportLabel.setText("Ismeretlen repülőtér");
        }

        departuresTable.setItems(FXCollections.observableArrayList(flights));
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}