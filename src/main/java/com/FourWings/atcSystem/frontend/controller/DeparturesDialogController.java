package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import com.FourWings.atcSystem.model.flight.FlightStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeparturesDialogController {

    // --- FELSŐ CÍMEK ---
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label totalDeparturesLabel;

    // --- TÁBLA + OSZLOPOK ---
    @FXML private TableView<Flight> departuresTable;

    @FXML private TableColumn<Flight, String> arrivalAirportCol;
    @FXML private TableColumn<Flight, String> airlineCol;
    @FXML private TableColumn<Flight, String> flightNumberCol;
    @FXML private TableColumn<Flight, String> scheduledDepartureCol;
    @FXML private TableColumn<Flight, String> scheduledArrivalCol;
    @FXML private TableColumn<Flight, FlightStatus> statusCol;
    @FXML private TableColumn<Flight, String> estimatedDepartureCol;
    @FXML private TableColumn<Flight, String> estimatedArrivalCol;
    @FXML private TableColumn<Flight, String> actualDepartureCol;
    @FXML private TableColumn<Flight, String> actualArrivalCol;
    @FXML private TableColumn<Flight, String> aircraftCol;
    @FXML private TableColumn<Flight, String> gateCol;
    @FXML private TableColumn<Flight, String> updatedAtCol;

    private final FlightService flightService;

    public DeparturesDialogController(FlightService flightService) {
        this.flightService = flightService;
    }

    @FXML
    public void initialize() {
        setupTable();
        departuresTable.setEditable(true);
        statusCol.setEditable(true);
        estimatedArrivalCol.setEditable(true);
        actualArrivalCol.setEditable(true);
    }

    /**
     * Ezt hívjuk a ControllerHomePageController-ből,
     * amikor megnyitjuk a dialogot.
     */
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
            subtitleLabel.setText("Mai induló járatok áttekintése");
        }

        // 🔹 Összes induló járat az adott reptérről
        List<Flight> departures = flightService.getDeparturesForAirport(airport);
        LocalDate today = LocalDate.now();

        // 🔹 Csak a mai indulások
        List<Flight> todayDepartures = departures.stream()
                .filter(f -> f.getScheduledDeparture() != null &&
                        f.getScheduledDeparture().toLocalDate().equals(today))
                .collect(Collectors.toList());

        // 🔹 Tábla feltöltése
        if (departuresTable != null) {
            departuresTable.getItems().setAll(todayDepartures);
        }

        // 🔹 Darabszám kiírása
        if (totalDeparturesLabel != null) {
            totalDeparturesLabel.setText(String.valueOf(todayDepartures.size()));
        }
    }

    private void setupTable() {
        // Érkezési reptér (destinationName)
        arrivalAirportCol.setCellValueFactory(new PropertyValueFactory<>("destinationName"));

        // Légitársaság
        airlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));

        // Járatszám
        flightNumberCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));

        // Tervezett indulás / érkezés (formázott szöveg – FlightService tölti)
        scheduledDepartureCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDepartureText"));
        scheduledArrivalCol.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));

        // Státusz szöveg
        // Státusz (enum)
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Becsült indulás / érkezés
        estimatedDepartureCol.setCellValueFactory(new PropertyValueFactory<>("estimatedDepartureText"));
        estimatedArrivalCol.setCellValueFactory(new PropertyValueFactory<>("estimatedArrivalText"));

        // Tényleges indulás / érkezés – itt nyers LocalDateTime toString megy
        actualDepartureCol.setCellValueFactory(new PropertyValueFactory<>("actualDepartureText"));
        actualArrivalCol.setCellValueFactory(new PropertyValueFactory<>("actualArrival"));

        // Légijármű: típus ICAO kód
        aircraftCol.setCellValueFactory(new PropertyValueFactory<>("aircraftTypeIcao"));

        // Kapu kód
        gateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        // Utolsó frissítés – updatedAt (LocalDateTime -> toString)
        updatedAtCol.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        statusCol.setCellFactory(
                ComboBoxTableCell.forTableColumn(FlightStatus.values())
        );

        statusCol.setOnEditCommit(event -> {
            Flight flight = event.getRowValue();
            flight.setStatus(event.getNewValue());
            flightService.save(flight);
            departuresTable.refresh();
        });

        estimatedArrivalCol.setCellFactory(
                javafx.scene.control.cell.TextFieldTableCell.forTableColumn()
        );

        estimatedArrivalCol.setOnEditCommit(event -> {
            Flight flight = event.getRowValue();
            flight.setEstimatedArrivalFromText(event.getNewValue());
            flightService.save(flight);
        });

        actualArrivalCol.setCellFactory(
                javafx.scene.control.cell.TextFieldTableCell.forTableColumn()
        );

        actualArrivalCol.setOnEditCommit(event -> {
            Flight flight = event.getRowValue();
            flight.setActualArrivalFromText(event.getNewValue());
            flightService.save(flight);
        });

        estimatedDepartureCol.setCellFactory(
                javafx.scene.control.cell.TextFieldTableCell.forTableColumn()
        );

        estimatedDepartureCol.setOnEditCommit(event -> {
            Flight flight = event.getRowValue();
            flight.setEstimatedDepartureFromText(event.getNewValue());
            flightService.save(flight);
        });

        actualDepartureCol.setCellFactory(
                javafx.scene.control.cell.TextFieldTableCell.forTableColumn()
        );

        actualDepartureCol.setOnEditCommit(event -> {
            Flight flight = event.getRowValue();
            flight.setActualDepartureFromText(event.getNewValue());
            flightService.save(flight);
        });
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