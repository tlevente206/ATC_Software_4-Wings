package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

@Component
public class RoutesPageController {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;

    private final AirportsService airportService;
    private final FlightService flightService;

    // --- FXML Elemek ---
    @FXML private ComboBox<String> menuComboBox;
    @FXML private ComboBox<Airports> airportSelector;
    @FXML private Label statusLabel;
    @FXML private CheckBox filterTodayCheckBox;
    @FXML private TextField searchField;

    @FXML private WebView mapWebView;
    private WebEngine webEngine;
    private boolean isMapLoaded = false;

    @FXML private TableView<Flight> departuresTable;
    @FXML private TableColumn<Flight, String> depFlightNumCol, depAirlineCol, depDestinationCol,
            depTimeCol, depStatusCol, depGateCol;

    @FXML private TableView<Flight> arrivalsTable;
    @FXML private TableColumn<Flight, String> arrFlightNumCol, arrAirlineCol, arrOriginCol,
            arrTimeCol, arrStatusCol, arrGateCol;

    // Adatlisták
    private final ObservableList<Airports> airportList = FXCollections.observableArrayList();
    private final ObservableList<Flight> departureList = FXCollections.observableArrayList();
    private final ObservableList<Flight> arrivalList = FXCollections.observableArrayList();

    private FilteredList<Flight> filteredDepartures;
    private FilteredList<Flight> filteredArrivals;

    public RoutesPageController(AirportsService airportService, FlightService flightService) {
        this.airportService = airportService;
        this.flightService = flightService;
    }

    @FXML
    public void initialize() {
        setupMenuNavigation();
        setupTables();
        setupAirportSelector();
        setupFilters();
        loadAirports();
        loadMap();

        // ha sort választasz, automatikusan frissíti a térképet
        departuresTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showFlightOnMap(newVal);
            }
        });
        arrivalsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showFlightOnMap(newVal);
            }
        });
    }

    // ----------------- TÉRKÉP KEZELÉS -----------------

    private void loadMap() {
        if (mapWebView == null) return;

        webEngine = mapWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        URL url = getClass().getResource("/web.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    isMapLoaded = true;
                    if (statusLabel != null) {
                        statusLabel.setText("Térkép sikeresen betöltve.");
                    }
                }
            });
        } else {
            if (statusLabel != null) {
                statusLabel.setText("HIBA: Térkép fájl (web.html) nem található!");
            }
        }
    }

    @FXML
    private void onShowMap() {
        Flight selectedFlight = departuresTable.getSelectionModel().getSelectedItem();
        if (selectedFlight == null) {
            selectedFlight = arrivalsTable.getSelectionModel().getSelectedItem();
        }

        if (selectedFlight == null) {
            if (statusLabel != null) {
                statusLabel.setText("Kérlek válassz ki egy járatot a táblázatból!");
            }
            Alert alert = new Alert(Alert.AlertType.WARNING, "Válassz ki egy járatot a listából!");
            alert.show();
            return;
        }

        showFlightOnMap(selectedFlight);
    }

    private void showFlightOnMap(Flight selectedFlight) {
        if (!isMapLoaded || webEngine == null) {
            if (statusLabel != null) {
                statusLabel.setText("A térkép még nem töltődött be!");
            }
            return;
        }
        if (selectedFlight == null) return;

        try {
            Airports originAirport = selectedFlight.getDepartureAirport() != null
                    ? selectedFlight.getDepartureAirport()
                    : findAirportInDb(selectedFlight.getOriginName());

            Airports destAirport = selectedFlight.getArrivalAirport() != null
                    ? selectedFlight.getArrivalAirport()
                    : findAirportInDb(selectedFlight.getDestinationName());

            if (originAirport != null && destAirport != null &&
                    originAirport.getLatitude() != null && originAirport.getLongitude() != null &&
                    destAirport.getLatitude() != null && destAirport.getLongitude() != null) {

                double lat1 = originAirport.getLatitude().doubleValue();
                double lon1 = originAirport.getLongitude().doubleValue();
                double lat2 = destAirport.getLatitude().doubleValue();
                double lon2 = destAirport.getLongitude().doubleValue();

                if (statusLabel != null) {
                    statusLabel.setText("Útvonal kirajzolása: " +
                            originAirport.getName() + " -> " + destAirport.getName());
                }

                String script = String.format(
                        "connectAirports(%f, %f, %f, %f)",
                        lat1, lon1, lat2, lon2
                );
                webEngine.executeScript(script);
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("Hiba: Nem találhatók koordináták a repterekhez.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Hiba a koordináták lekérdezésekor: " + e.getMessage());
            }
        }
    }

    // reptér keresése, ha Flight-ban csak név/kód van
    private Airports findAirportInDb(String airportNameOrCode) {
        if (airportNameOrCode == null) return null;
        List<Airports> all = airportService.getAllAirports();
        for (Airports a : all) {
            if (airportNameOrCode.equalsIgnoreCase(a.getName())
                    || airportNameOrCode.equalsIgnoreCase(a.getIcaoCode())
                    || airportNameOrCode.equalsIgnoreCase(a.getIataCode())) {
                return a;
            }
        }
        return null;
    }

    // ----------------- TÁBLÁZATOK, SZŰRÉS -----------------

    private void setupFilters() {
        filteredDepartures = new FilteredList<>(departureList, p -> true);
        filteredArrivals = new FilteredList<>(arrivalList, p -> true);
        departuresTable.setItems(filteredDepartures);
        arrivalsTable.setItems(filteredArrivals);

        if (filterTodayCheckBox != null) {
            filterTodayCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        }
    }

    private void updateFilters() {
        boolean onlyToday = filterTodayCheckBox != null && filterTodayCheckBox.isSelected();
        LocalDate today = LocalDate.now();
        String searchText = (searchField != null && searchField.getText() != null)
                ? searchField.getText().toLowerCase().trim()
                : "";

        filteredDepartures.setPredicate(flight -> {
            boolean dateMatch = !onlyToday ||
                    (flight.getScheduledDeparture() != null &&
                            flight.getScheduledDeparture().toLocalDate().equals(today));
            return dateMatch && matchesSearch(flight, searchText, true);
        });

        filteredArrivals.setPredicate(flight -> {
            boolean dateMatch = !onlyToday ||
                    (flight.getScheduledArrival() != null &&
                            flight.getScheduledArrival().toLocalDate().equals(today));
            return dateMatch && matchesSearch(flight, searchText, false);
        });

        if (statusLabel != null) {
            statusLabel.setText(String.format("Listázva: %d induló, %d érkező",
                    filteredDepartures.size(), filteredArrivals.size()));
        }
    }

    private boolean matchesSearch(Flight flight, String searchText, boolean isDeparture) {
        if (searchText.isEmpty()) return true;

        StringBuilder sb = new StringBuilder();
        if (flight.getFlightNumber() != null) sb.append(flight.getFlightNumber());
        if (flight.getAirlineName() != null) sb.append(flight.getAirlineName());
        if (flight.getStatusText() != null) sb.append(flight.getStatusText());

        String loc = isDeparture ? flight.getDestinationName() : flight.getOriginName();
        if (loc != null) sb.append(loc);

        return sb.toString().toLowerCase().contains(searchText);
    }

    private void setupTables() {
        // induló járatok
        depFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        depAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        depDestinationCol.setCellValueFactory(new PropertyValueFactory<>("destinationName"));
        depTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDepartureText"));
        depStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        depGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        // érkező járatok
        arrFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        arrAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        arrOriginCol.setCellValueFactory(new PropertyValueFactory<>("originName"));
        arrTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));
        arrStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        arrGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));
    }

    private void loadRoutesForAirport(Airports airport) {
        if (airport == null) return;
        try {
            departureList.setAll(flightService.getDeparturesForAirport(airport));
            arrivalList.setAll(flightService.getArrivalsForAirport(airport));
            updateFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAirportSelector() {
        airportSelector.setConverter(new StringConverter<Airports>() {
            @Override
            public String toString(Airports a) {
                return (a == null) ? "" : a.getIcaoCode() + " - " + a.getName();
            }
            @Override
            public Airports fromString(String s) {
                return null;
            }
        });

        airportSelector.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> loadRoutesForAirport(newVal)
        );
    }

    private void loadAirports() {
        try {
            airportList.setAll(airportService.getAllAirports());
            airportSelector.setItems(airportList);
            if (!airportList.isEmpty()) {
                airportSelector.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            if (statusLabel != null) {
                statusLabel.setText("Hiba repterek betöltésekor.");
            }
        }
    }

    private void setupMenuNavigation() {
        if (menuComboBox == null) return;
        menuComboBox.setValue("Repülőutak");
        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Főoldal" ->
                        SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", WIDTH, HEIGHT);
                case "Repülők" ->
                        SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", WIDTH, HEIGHT);
                case "Repterek" ->
                        SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                case "Repülőutak" ->
                        SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Útvonalak", WIDTH, HEIGHT);
            }
        });
    }

    @FXML
    private void onRefresh() {
        Airports selected = airportSelector.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadRoutesForAirport(selected);
        }
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}