package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArrivalsDialogController {

    // --- Spring service ---
    private final FlightService flightService;

    // --- Állapot ---
    private Airports currentAirport;
    private ObservableList<Flight> masterData;
    private FilteredList<Flight> filteredData;
    private Timeline refreshTimeline;

    // --- FXML mezők ---

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

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterBox;

    @FXML private Label selectedFlightLabel;

    // Repülőgép adatok
    @FXML private Label aircraftRegLabel;
    @FXML private Label aircraftTypeLabel;
    @FXML private Label aircraftSeatsLabel;
    @FXML private Label aircraftYearLabel;
    @FXML private Label aircraftStatusLabel;

    public ArrivalsDialogController(FlightService flightService) {
        this.flightService = flightService;
    }

    // ---------------------------------------------------
    // INITIALIZE
    // ---------------------------------------------------
    @FXML
    private void initialize() {
        // Táblázat oszlopainak bindolása a transiens mezőkhöz
        if (flightsTable != null) {
            flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
            airlineColumn.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
            originColumn.setCellValueFactory(new PropertyValueFactory<>("originName"));
            schedArrColumn.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));
            estArrColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedArrivalText"));
            gateColumn.setCellValueFactory(new PropertyValueFactory<>("gateCode"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusText"));

            masterData = FXCollections.observableArrayList();
            filteredData = new FilteredList<>(masterData, f -> true);
            flightsTable.setItems(filteredData);

            // Sor-színezés + dupla kattintás
            flightsTable.setRowFactory(tv -> {
                TableRow<Flight> row = new TableRow<>() {
                    @Override
                    protected void updateItem(Flight item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setStyle("");
                        } else {
                            String st = item.getStatusText() != null
                                    ? item.getStatusText().toLowerCase()
                                    : "";
                            if (st.contains("kés") || st.contains("DELAY")) {
                                setStyle("-fx-background-color: rgba(255,215,0,0.3);");
                            } else if (st.contains("töröl") || st.contains("CANCEL")) {
                                setStyle("-fx-background-color: rgba(255,0,0,0.2);");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                };

                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Flight selected = row.getItem();
                        openFlightDetails(selected);
                    }
                });

                return row;
            });

            // Kijelölt sor infósáv + repülőgép adatok
            flightsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSel, newSel) -> updateSelectedFlightDetails(newSel)
            );
        }

        // Státusz filter combobox feltöltése
        if (statusFilterBox != null) {
            statusFilterBox.setItems(FXCollections.observableArrayList(
                    "Mind", "Időben", "Késik", "Törölve"
            ));
            statusFilterBox.getSelectionModel().selectFirst();
            statusFilterBox.valueProperty().addListener((obs, o, n) -> applyFilters());
        }

        // Kereső mező
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        }

        // Kezdeti kijelölt sor info kiürítése
        updateSelectedFlightDetails(null);
    }

    // ---------------------------------------------------
    // INIT – kívülről hívva (HomePage controllerből)
    // ---------------------------------------------------
    public void init(Airports airport, List<Flight> arrivals) {
        this.currentAirport = airport;

        String icao = airport.getIcaoCode() != null ? airport.getIcaoCode() : "";
        String name = airport.getName() != null ? airport.getName() : "";

        titleLabel.setText("Érkező járatok – " + icao);
        subtitleLabel.setText("Reptér: " + icao + " – " + name);

        masterData.setAll(arrivals);
        statusLabel.setText("Találatok: " + arrivals.size());

        // Auto-refresh elindítása (30 mp-enként)
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> refreshData())
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    // ---------------------------------------------------
    // Auto-refresh
    // ---------------------------------------------------
    private void refreshData() {
        if (currentAirport == null) {
            return;
        }

        // Biztosan JavaFX thread-en történjen az UI frissítés
        Platform.runLater(() -> {
            List<Flight> fresh = flightService.getArrivalsForAirport(currentAirport);
            masterData.setAll(fresh);
            statusLabel.setText("Találatok: " + fresh.size());
            applyFilters();
        });
    }

    // ---------------------------------------------------
    // Szűrés – kereső + státusz
    // ---------------------------------------------------
    private void applyFilters() {
        if (filteredData == null) return;

        String text = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        String statusFilter = statusFilterBox != null && statusFilterBox.getValue() != null
                ? statusFilterBox.getValue()
                : "Mind";

        filteredData.setPredicate(f -> {
            // Szövegszűrés
            boolean matchText;
            if (text.isEmpty()) {
                matchText = true;
            } else {
                String flightNum = f.getFlightNumber() != null ? f.getFlightNumber().toLowerCase() : "";
                String airline = f.getAirlineName() != null ? f.getAirlineName().toLowerCase() : "";
                String origin = f.getOriginName() != null ? f.getOriginName().toLowerCase() : "";
                String dest = f.getDestinationName() != null ? f.getDestinationName().toLowerCase() : "";

                matchText = flightNum.contains(text)
                        || airline.contains(text)
                        || origin.contains(text)
                        || dest.contains(text);
            }

            // Státusz szűrés
            boolean matchStatus;
            String st = f.getStatusText() != null ? f.getStatusText() : "";
            if ("Mind".equalsIgnoreCase(statusFilter)) {
                matchStatus = true;
            } else if ("Időben".equalsIgnoreCase(statusFilter)) {
                // egyszerű példa – testreszabhatod
                matchStatus = st.equalsIgnoreCase("ON_TIME") || st.equalsIgnoreCase("SCHEDULED");
            } else if ("Késik".equalsIgnoreCase(statusFilter)) {
                matchStatus = st.toLowerCase().contains("delay");
            } else if ("Törölve".equalsIgnoreCase(statusFilter)) {
                matchStatus = st.toLowerCase().contains("cancel");
            } else {
                matchStatus = true;
            }

            return matchText && matchStatus;
        });
    }

    // ---------------------------------------------------
    // Kijelölt járat részletei + repülőgép adatok
    // ---------------------------------------------------
    private void updateSelectedFlightDetails(Flight f) {
        if (f == null) {
            selectedFlightLabel.setText("–");
            aircraftRegLabel.setText("–");
            aircraftTypeLabel.setText("–");
            aircraftSeatsLabel.setText("–");
            aircraftYearLabel.setText("–");
            aircraftStatusLabel.setText("–");
            return;
        }

        String origin = f.getOriginName() != null ? f.getOriginName() : "";
        String dest = f.getDestinationName() != null ? f.getDestinationName() : "";
        String airline = f.getAirlineName() != null ? f.getAirlineName() : "";
        String status = f.getStatusText() != null ? f.getStatusText() : "";

        selectedFlightLabel.setText(
                String.format("%s | %s → %s | %s | %s",
                        f.getFlightNumber() != null ? f.getFlightNumber() : "",
                        origin,
                        dest,
                        airline,
                        status
                )
        );

        aircraftRegLabel.setText(
                f.getAircraftRegistration() != null && !f.getAircraftRegistration().isBlank()
                        ? f.getAircraftRegistration()
                        : "–"
        );
        aircraftTypeLabel.setText(
                f.getAircraftTypeIcao() != null && !f.getAircraftTypeIcao().isBlank()
                        ? f.getAircraftTypeIcao()
                        : "–"
        );
        aircraftSeatsLabel.setText(
                f.getAircraftMaxSeatCapacity() != null
                        ? f.getAircraftMaxSeatCapacity().toString()
                        : "–"
        );
        aircraftYearLabel.setText(
                f.getAircraftManufactureYear() != null
                        ? f.getAircraftManufactureYear().toString()
                        : "–"
        );
        aircraftStatusLabel.setText(
                f.getAircraftStatusText() != null && !f.getAircraftStatusText().isBlank()
                        ? f.getAircraftStatusText()
                        : "–"
        );
    }

    private void openFlightDetails(Flight flight) {
        // Most csak a kijelölt infósávot frissítjük – később ide tehetsz külön dialogot is.
        updateSelectedFlightDetails(flight);
    }

    // ---------------------------------------------------
    // Bezárás
    // ---------------------------------------------------
    @FXML
    private void onClose() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        Stage stage = (Stage) flightsTable.getScene().getWindow();
        stage.close();
    }
}