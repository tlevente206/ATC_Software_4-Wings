package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
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
    private final FlightService flightService;

    // --- FXML Elemek ---

    @FXML
    private ComboBox<String> menuComboBox;

    @FXML
    private ComboBox<Airports> airportSelector;

    @FXML
    private Label statusLabel;

    // -- Induló járatok --
    @FXML
    private TableView<Flight> departuresTable;
    @FXML
    private TableColumn<Flight, String> depFlightNumCol;
    @FXML
    private TableColumn<Flight, String> depAirlineCol;
    @FXML
    private TableColumn<Flight, String> depDestinationCol;
    @FXML
    private TableColumn<Flight, String> depTimeCol;
    @FXML
    private TableColumn<Flight, String> depStatusCol;
    @FXML
    private TableColumn<Flight, String> depGateCol;

    // -- Érkező járatok --
    @FXML
    private TableView<Flight> arrivalsTable;
    @FXML
    private TableColumn<Flight, String> arrFlightNumCol;
    @FXML
    private TableColumn<Flight, String> arrAirlineCol;
    @FXML
    private TableColumn<Flight, String> arrOriginCol;
    @FXML
    private TableColumn<Flight, String> arrTimeCol;
    @FXML
    private TableColumn<Flight, String> arrStatusCol;
    @FXML
    private TableColumn<Flight, String> arrGateCol;

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

    // ----------------- Táblák beállítása & SZÍNEZÉS -----------------
    private void setupTables() {
        // --- 1. INDULÓK OSZLOPOK ---
        depFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        depAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        depDestinationCol.setCellValueFactory(new PropertyValueFactory<>("destinationName"));
        depTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDepartureText"));
        depStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        depGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        departuresTable.setItems(departureList);
        departuresTable.setPlaceholder(new Label("Válassz repteret az induló járatokhoz!"));

        // *** SZÍNEZÉS LOGIKA (Departures) ***
        departuresTable.setRowFactory(tv -> new TableRow<Flight>() {
            @Override
            protected void updateItem(Flight item, boolean empty) {
                super.updateItem(item, empty);
                applyRowColor(this, item, empty);
            }
        });

        // --- 2. ÉRKEZŐK OSZLOPOK ---
        arrFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        arrAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        arrOriginCol.setCellValueFactory(new PropertyValueFactory<>("originName"));
        arrTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));
        arrStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        arrGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        arrivalsTable.setItems(arrivalList);
        arrivalsTable.setPlaceholder(new Label("Válassz repteret az érkező járatokhoz!"));

        // *** SZÍNEZÉS LOGIKA (Arrivals) ***
        arrivalsTable.setRowFactory(tv -> new TableRow<Flight>() {
            @Override
            protected void updateItem(Flight item, boolean empty) {
                super.updateItem(item, empty);
                applyRowColor(this, item, empty);
            }
        });
    }

    private void applyRowColor(TableRow<Flight> row, Flight item, boolean empty) {
        if (item == null || empty) {
            row.setStyle("");
        } else {
            String status = "";
            try {
                // Ha van getStatusText metódusod, használd azt közvetlenül!
                // status = item.getStatusText();

                // Reflexió (biztonsági tartalék, ha nem tudom a metódusnevet):
                java.lang.reflect.Method method = item.getClass().getMethod("getStatusText");
                status = (String) method.invoke(item);

            } catch (Exception e) {
                status = "Unknown";
            }

            if (status == null) status = "";

            // Kisbetűssé alakítjuk a könnyebb vizsgálathoz és contains-t használunk,
            // hogy pl. a "Taxiing" és "Taxi" is működjön.
            String s = status.toLowerCase();

            // --- 1. ZÖLD: Landolt / Érkezett ---
            if (s.contains("landed") || s.contains("arrived")) {
                row.setStyle("-fx-background-color: #c8e6c9;");
            }
            // --- 2. KÉK: Levegőben (Airborne) ---
            else if (s.contains("airborne") || s.contains("flying")) {
                row.setStyle("-fx-background-color: #b3e5fc;");
            }
            // --- 3. NARANCS: Gurul (Taxi) ---
            else if (s.contains("taxi")) {
                row.setStyle("-fx-background-color: #ffe0b2;");
            }
            // --- 4. TÜRKIZ: Beszállás (Boarding) ---
            else if (s.contains("boarding") || s.contains("go to gate")) {
                row.setStyle("-fx-background-color: #b2dfdb;");
            }
            // --- 5. SÁRGA: Késik (Delayed) ---
            else if (s.contains("delayed") || s.contains("late")) {
                row.setStyle("-fx-background-color: #fff9c4;");
            }
            // --- 6. PIROS: Törölve (Cancelled) ---
            else if (s.contains("canceled")) {
                row.setStyle("-fx-background-color: #ffcdd2;");
            }
            // --- 7. SZÜRKE: Tervezett (Scheduled) ---
            else if (s.contains("sched")) {
                row.setStyle("-fx-background-color: #f5f5f5;");
            }
            // --- EGYÉB (Alapértelmezett) ---
            else {
                row.setStyle("");
            }
        }
    }

    // ----------------- Útvonalak betöltése -----------------
    private void loadRoutesForAirport(Airports airport) {
        if (airport == null) return;

        statusLabel.setText("Útvonalak betöltése: " + airport.getName());

        try {
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