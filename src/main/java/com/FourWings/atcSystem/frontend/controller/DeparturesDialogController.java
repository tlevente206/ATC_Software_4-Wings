package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.config.SpringContext;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeparturesDialogController {

    private final FlightService flightService;

    private Airports currentAirport;
    private ObservableList<Flight> masterData;
    private FilteredList<Flight> filteredData;
    private Timeline refreshTimeline;

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightNumberColumn;
    @FXML private TableColumn<Flight, String> airlineColumn;
    @FXML private TableColumn<Flight, String> destinationColumn;
    @FXML private TableColumn<Flight, String> schedDepColumn;
    @FXML private TableColumn<Flight, String> estDepColumn;
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

    public DeparturesDialogController(FlightService flightService) {
        this.flightService = flightService;
    }

    @FXML
    private void initialize() {
        if (flightsTable != null) {
            flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
            airlineColumn.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
            destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destinationName"));
            schedDepColumn.setCellValueFactory(new PropertyValueFactory<>("scheduledDepartureText"));
            estDepColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedDepartureText"));
            gateColumn.setCellValueFactory(new PropertyValueFactory<>("gateCode"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusText"));

            masterData = FXCollections.observableArrayList();
            filteredData = new FilteredList<>(masterData, f -> true);
            flightsTable.setItems(filteredData);

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
                            if (st.contains("kés") || st.contains("delay")) {
                                setStyle("-fx-background-color: rgba(255,215,0,0.3);");
                            } else if (st.contains("töröl") || st.contains("cancel")) {
                                setStyle("-fx-background-color: rgba(255,0,0,0.2);");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                };

                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        openFlightDetails(row.getItem());
                    }
                });

                return row;
            });

            flightsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSel, newSel) -> updateSelectedFlightDetails(newSel)
            );
        }

        if (statusFilterBox != null) {
            statusFilterBox.setItems(FXCollections.observableArrayList(
                    "Mind", "Időben", "Késik", "Törölve"
            ));
            statusFilterBox.getSelectionModel().selectFirst();
            statusFilterBox.valueProperty().addListener((obs, o, n) -> applyFilters());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        }

        updateSelectedFlightDetails(null);
    }

    public void init(Airports airport, List<Flight> departures) {
        this.currentAirport = airport;

        String icao = airport.getIcaoCode() != null ? airport.getIcaoCode() : "";
        String name = airport.getName() != null ? airport.getName() : "";

        titleLabel.setText("Induló járatok – " + icao);
        subtitleLabel.setText("Reptér: " + icao + " – " + name);

        masterData.setAll(departures);
        statusLabel.setText("Találatok: " + departures.size());

        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> refreshData())
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void refreshData() {
        if (currentAirport == null) return;

        Platform.runLater(() -> {
            List<Flight> fresh = flightService.getDeparturesForAirport(currentAirport);
            masterData.setAll(fresh);
            statusLabel.setText("Találatok: " + fresh.size());
            applyFilters();
        });
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String text = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        String statusFilter = statusFilterBox != null && statusFilterBox.getValue() != null
                ? statusFilterBox.getValue()
                : "Mind";

        filteredData.setPredicate(f -> {
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

            boolean matchStatus;
            String st = f.getStatusText() != null ? f.getStatusText() : "";
            if ("Mind".equalsIgnoreCase(statusFilter)) {
                matchStatus = true;
            } else if ("Időben".equalsIgnoreCase(statusFilter)) {
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
        updateSelectedFlightDetails(flight);
    }

    // ÚJ: Új járat gomb
    @FXML
    private void onNewFlight() {
        openCreateFlightDialog(null);
    }

    // ÚJ: Kijelölt szerkesztése
    @FXML
    private void onEditFlight() {
        Flight selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Nincs kiválasztva");
            a.setHeaderText("Nincs kijelölt járat.");
            a.setContentText("Válassz ki egy járatot a szerkesztéshez.");
            a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            a.showAndWait();
            return;
        }
        openCreateFlightDialog(selected);
    }

    private void openCreateFlightDialog(Flight flight) {
        if (currentAirport == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/CreateFlightDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            CreateFlightDialogController ctrl = loader.getController();
            ctrl.init(currentAirport, flight);

            Stage dialog = new Stage();
            dialog.initOwner(flightsTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle((flight == null ? "Új induló járat" : "Járat szerkesztése") +
                    " – " + currentAirport.getIcaoCode());
            dialog.setScene(new Scene(root, 520, 420));
            dialog.centerOnScreen();
            dialog.showAndWait();

            // Mentés után frissítsük a listát
            refreshData();

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Hiba");
            a.setHeaderText("Nem sikerült megnyitni a járat szerkesztő ablakot.");
            a.setContentText(ex.getMessage());
            a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            a.showAndWait();
        }
    }

    @FXML
    private void onClose() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        Stage stage = (Stage) flightsTable.getScene().getWindow();
        stage.close();
    }
}