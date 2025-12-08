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

    // Térkép
    @FXML private WebView mapWebView;
    private WebEngine webEngine;
    private boolean isMapLoaded = false;

    // Táblázatok (kicsit rövidítve a kódot, a deklarációk maradnak)
    @FXML private TableView<Flight> departuresTable;
    @FXML private TableColumn<Flight, String> depFlightNumCol, depAirlineCol, depDestinationCol, depTimeCol, depStatusCol, depGateCol;

    @FXML private TableView<Flight> arrivalsTable;
    @FXML private TableColumn<Flight, String> arrFlightNumCol, arrAirlineCol, arrOriginCol, arrTimeCol, arrStatusCol, arrGateCol;

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

        // Térkép betöltése indításkor
        loadMap();
    }

    // ----------------- TÉRKÉP KEZELÉS (ÚJ RÉSZ) -----------------

    private void loadMap() {
        webEngine = mapWebView.getEngine();

        // Betöltjük a web.html-t a resources mappából
        URL url = getClass().getResource("/web.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());

            // Figyeljük, hogy betöltődött-e
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    isMapLoaded = true;
                    System.out.println("Térkép sikeresen betöltve.");
                }
            });
        } else {
            statusLabel.setText("HIBA: Térkép fájl (web.html) nem található!");
        }
    }

    @FXML
    private void onShowMap() {
        if (!isMapLoaded) {
            statusLabel.setText("A térkép még nem töltődött be!");
            return;
        }

        // 1. Megnézzük, melyik járat van kiválasztva (induló vagy érkező)
        Flight selectedFlight = departuresTable.getSelectionModel().getSelectedItem();
        if (selectedFlight == null) {
            selectedFlight = arrivalsTable.getSelectionModel().getSelectedItem();
        }

        if (selectedFlight == null) {
            statusLabel.setText("Kérlek válassz ki egy járatot a táblázatból!");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Válassz ki egy járatot a listából!");
            alert.show();
            return;
        }

        // 2. Koordináták beszerzése az adatbázisból
        try {
            // Feltételezzük, hogy a Flight objektumban van ICAO kód vagy maga az Airport objektum.
            // Ha a Flight objektum csak stringet tárol (pl. "LHBP"), akkor le kell kérni a Service-től a teljes objektumot.

            Airports originAirport = null;
            Airports destAirport = null;

            // Opció A: Ha a Flight objektumban már benne vannak az Airport objektumok
            if (selectedFlight.getDepartureAirport() != null) {
                originAirport = selectedFlight.getDepartureAirport();
            } else {
                // Opció B: Ha csak a név/kód van meg, lekérjük az adatbázisból (Implementáld a service-ben!)
                // Példa: originAirport = airportService.findAirportByNameOrCode(selectedFlight.getOriginName());
                // Mivel nem látom a Flight osztályodat, itt most feltételezem, hogy a Service tud segíteni.

                // Demo céljából: Keressük meg a listából (ami már be van töltve a selectorba, ha szerencsénk van)
                // De a helyes út az adatbázis lekérdezés lenne ICAO kód alapján.
                originAirport = findAirportInDb(selectedFlight.getOriginName()); // Segédfüggvény lent
            }

            if (selectedFlight.getArrivalAirport() != null) {
                destAirport = selectedFlight.getArrivalAirport();
            } else {
                destAirport = findAirportInDb(selectedFlight.getDestinationName());
            }

            // 3. Ha megvannak az adatok, rajzoljunk
            if (originAirport != null && destAirport != null) {
                BigDecimal lat1 = originAirport.getLatitude();
                BigDecimal lon1 = originAirport.getLongitude();
                BigDecimal lat2 = destAirport.getLatitude();
                BigDecimal lon2 = destAirport.getLongitude();

                statusLabel.setText("Útvonal kirajzolása: " + originAirport.getName() + " -> " + destAirport.getName());

                // JavaScript hívás
                String script = String.format("connectAirports(%s, %s, %s, %s)", lat1, lon1, lat2, lon2);
                webEngine.executeScript(script);

            } else {
                statusLabel.setText("Hiba: Nem találhatók koordináták a repterekhez.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Hiba a koordináták lekérdezésekor: " + e.getMessage());
        }
    }

    // Segédfüggvény a reptér kereséshez (Ha a Flight objektumban csak String név van)
    private Airports findAirportInDb(String airportNameOrCode) {
        if (airportNameOrCode == null) return null;

        // IGAZI MEGOLDÁS: Service hívás
        // return airportService.findByCode(airportNameOrCode);

        // Mivel nincs meg a Service kódod erre, itt egy ideiglenes megoldás:
        // Végigmegyünk az összes reptéren, amit ismerünk
        for (Airports a : airportService.getAllAirports()) { // Ez lehet lassú, ha sok van, jobb lenne célzott SQL query
            if (a.getName().equalsIgnoreCase(airportNameOrCode) || a.getIcaoCode().equalsIgnoreCase(airportNameOrCode)) {
                return a;
            }
        }
        return null;
    }

    // ----------------- Meglévő logikák (Szűrés, Táblázat) -----------------
    // Ezek változatlanok maradnak a te kódodból, csak a helyhiány miatt nem másolom be újra mindet.
    // A setupFilters, updateFilters, setupTables metódusaidat hagyd meg úgy, ahogy voltak!

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
        boolean onlyToday = filterTodayCheckBox.isSelected();
        LocalDate today = LocalDate.now();
        String searchText = (searchField.getText() != null) ? searchField.getText().toLowerCase().trim() : "";

        filteredDepartures.setPredicate(flight -> {
            boolean dateMatch = !onlyToday || (flight.getScheduledDeparture() != null && flight.getScheduledDeparture().toLocalDate().equals(today));
            return dateMatch && matchesSearch(flight, searchText, true);
        });

        filteredArrivals.setPredicate(flight -> {
            boolean dateMatch = !onlyToday || (flight.getScheduledArrival() != null && flight.getScheduledArrival().toLocalDate().equals(today));
            return dateMatch && matchesSearch(flight, searchText, false);
        });

        // Státusz frissítése
        if(statusLabel != null) statusLabel.setText(String.format("Listázva: %d induló, %d érkező", filteredDepartures.size(), filteredArrivals.size()));
    }

    private boolean matchesSearch(Flight flight, String searchText, boolean isDeparture) {
        if (searchText.isEmpty()) return true;
        String fullData = (flight.getFlightNumber() + flight.getAirlineName() + flight.getStatusText()).toLowerCase();
        String loc = isDeparture ? flight.getDestinationName() : flight.getOriginName();
        if (loc != null) fullData += loc.toLowerCase();
        return fullData.contains(searchText);
    }

    private void setupTables() {
        // A te eredeti setupTables kódod...
        depFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        depAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        depDestinationCol.setCellValueFactory(new PropertyValueFactory<>("destinationName"));
        depTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDepartureText"));
        depStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        depGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        arrFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        arrAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        arrOriginCol.setCellValueFactory(new PropertyValueFactory<>("originName"));
        arrTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));
        arrStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        arrGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        // Színezés (row factory) is maradhat...
    }

    private void loadRoutesForAirport(Airports airport) {
        if (airport == null) return;
        try {
            departureList.setAll(flightService.getDeparturesForAirport(airport));
            arrivalList.setAll(flightService.getArrivalsForAirport(airport));
            updateFilters();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupAirportSelector() {
        airportSelector.setConverter(new StringConverter<Airports>() {
            @Override public String toString(Airports a) { return (a == null) ? "" : a.getIcaoCode() + " - " + a.getName(); }
            @Override public Airports fromString(String s) { return null; }
        });
        airportSelector.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> loadRoutesForAirport(newVal));
    }

    private void loadAirports() {
        try {
            airportList.setAll(airportService.getAllAirports());
            airportSelector.setItems(airportList);
            if (!airportList.isEmpty()) airportSelector.getSelectionModel().selectFirst();
        } catch (Exception e) { statusLabel.setText("Hiba repterek betöltésekor."); }
    }

    private void setupMenuNavigation() {
        // A te eredeti menü kódod...
        if (menuComboBox == null) return;
        menuComboBox.setValue("Repülőutak");
        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", WIDTH, HEIGHT);
                case "Repülők" -> SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", WIDTH, HEIGHT);
                case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Útvonalak", WIDTH, HEIGHT);
            }
        });
    }

    @FXML private void onRefresh() {
        Airports selected = airportSelector.getSelectionModel().getSelectedItem();
        if (selected != null) loadRoutesForAirport(selected);
    }

    @FXML private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}