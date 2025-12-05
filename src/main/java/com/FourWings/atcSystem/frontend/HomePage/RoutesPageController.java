package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight; // Fontos: Egyes szám!
import com.FourWings.atcSystem.model.flight.FlightService; // Fontos: Egyes szám!
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoutesPageController {

    private final AirportsService airportService;
    private final FlightService flightService; // A te FlightService osztályod

    // --- FXML Elemek ---

    @FXML
    private ComboBox<String> menuComboBox;

    @FXML
    private ComboBox<Airports> airportSelector;

    @FXML
    private Label statusLabel;

    // -- Induló járatok (Departures) --
    @FXML
    private TableView<Flight> departuresTable;
    @FXML
    private TableColumn<Flight, String> depFlightNumCol;
    @FXML
    private TableColumn<Flight, String> depAirlineCol;     // airlineName
    @FXML
    private TableColumn<Flight, String> depDestinationCol; // destinationName
    @FXML
    private TableColumn<Flight, String> depTimeCol;        // scheduledDepartureText
    @FXML
    private TableColumn<Flight, String> depStatusCol;      // statusText
    @FXML
    private TableColumn<Flight, String> depGateCol;        // gateCode

    // -- Érkező járatok (Arrivals) --
    @FXML
    private TableView<Flight> arrivalsTable;
    @FXML
    private TableColumn<Flight, String> arrFlightNumCol;
    @FXML
    private TableColumn<Flight, String> arrAirlineCol;     // airlineName
    @FXML
    private TableColumn<Flight, String> arrOriginCol;      // originName
    @FXML
    private TableColumn<Flight, String> arrTimeCol;        // scheduledArrivalText
    @FXML
    private TableColumn<Flight, String> arrStatusCol;      // statusText
    @FXML
    private TableColumn<Flight, String> arrGateCol;        // gateCode

    // --- Adatlisták ---
    private final ObservableList<Airports> airportList = FXCollections.observableArrayList();
    private final ObservableList<Flight> departureList = FXCollections.observableArrayList();
    private final ObservableList<Flight> arrivalList = FXCollections.observableArrayList();

    public RoutesPageController(AirportsService airportService, FlightService flightService) {
        this.airportService = airportService;
        this.flightService = flightService;
    }

    @FXML
    public void initialize() {
        setupMenuNavigation();
        setupTables();
        setupAirportSelector();
        loadAirports();
    }

    // ----------------- Táblák beállítása -----------------
    private void setupTables() {
        // A PropertyValueFactory stringeknek pontosan meg kell egyezniük
        // a Flight osztályodban lévő mezőnevekkel (amiket a prepareFlightForView tölt fel).

        // --- INDULÓK ---
        depFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        depAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        depDestinationCol.setCellValueFactory(new PropertyValueFactory<>("destinationName"));
        depTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDepartureText"));
        depStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        depGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        departuresTable.setItems(departureList);
        departuresTable.setPlaceholder(new Label("Válassz repteret az induló járatokhoz!"));

        // --- ÉRKEZŐK ---
        arrFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        arrAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        arrOriginCol.setCellValueFactory(new PropertyValueFactory<>("originName"));
        arrTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));
        arrStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        arrGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        arrivalsTable.setItems(arrivalList);
        arrivalsTable.setPlaceholder(new Label("Válassz repteret az érkező járatokhoz!"));
    }

    // ----------------- Útvonalak betöltése -----------------
    private void loadRoutesForAirport(Airports airport) {
        if (airport == null) return;

        statusLabel.setText("Útvonalak betöltése: " + airport.getName());

        try {
            // A te Service metódusaidat hívjuk, amik Airports objektumot várnak
            List<Flight> deps = flightService.getDeparturesForAirport(airport);
            List<Flight> arrs = flightService.getArrivalsForAirport(airport);

            departureList.setAll(deps);
            arrivalList.setAll(arrs);

            statusLabel.setText(String.format("%s (%s) betöltve: %d induló, %d érkező.",
                    airport.getName(), airport.getIcaoCode(), deps.size(), arrs.size()));

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Hiba az útvonalak lekérdezésekor.");
        }
    }

    // ----------------- Reptér választó -----------------
    private void setupAirportSelector() {
        airportSelector.setConverter(new StringConverter<Airports>() {
            @Override
            public String toString(Airports airport) {
                return (airport == null) ? "" : airport.getIcaoCode() + " - " + airport.getName();
            }

            @Override
            public Airports fromString(String string) { return null; }
        });

        airportSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadRoutesForAirport(newVal);
            }
        });
    }

    private void loadAirports() {
        try {
            List<Airports> allAirports = airportService.getAllAirports();
            airportList.setAll(allAirports);
            airportSelector.setItems(airportList);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Hiba a repterek betöltésekor!");
        }
    }

    // ----------------- Menü navigáció -----------------
    private void setupMenuNavigation() {
        if (menuComboBox == null) return;
        menuComboBox.setValue("Repülőutak");
        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", 800, 600);
                case "Repülők" -> SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 800, 600);
                case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", 800, 600);
                case "Kapuk(Ez inkább a repterekhez menne)" -> SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", 800, 600);
                case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 800, 600);
            }
        });
    }

    @FXML
    private void onRefresh() {
        Airports selected = airportSelector.getSelectionModel().getSelectedItem();
        if (selected != null) loadRoutesForAirport(selected);
        else loadAirports();
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}