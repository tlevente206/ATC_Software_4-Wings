package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // ÚJ: Szűrők
    @FXML private CheckBox filterTodayCheckBox;
    @FXML private TextField searchField;

    // -- Induló járatok --
    @FXML private TableView<Flight> departuresTable;
    @FXML private TableColumn<Flight, String> depFlightNumCol;
    @FXML private TableColumn<Flight, String> depAirlineCol;
    @FXML private TableColumn<Flight, String> depDestinationCol;
    @FXML private TableColumn<Flight, String> depTimeCol;
    @FXML private TableColumn<Flight, String> depStatusCol;
    @FXML private TableColumn<Flight, String> depGateCol;

    // -- Érkező járatok --
    @FXML private TableView<Flight> arrivalsTable;
    @FXML private TableColumn<Flight, String> arrFlightNumCol;
    @FXML private TableColumn<Flight, String> arrAirlineCol;
    @FXML private TableColumn<Flight, String> arrOriginCol;
    @FXML private TableColumn<Flight, String> arrTimeCol;
    @FXML private TableColumn<Flight, String> arrStatusCol;
    @FXML private TableColumn<Flight, String> arrGateCol;

    // --- Adatlisták (Nyers adatok) ---
    private final ObservableList<Airports> airportList = FXCollections.observableArrayList();
    private final ObservableList<Flight> departureList = FXCollections.observableArrayList();
    private final ObservableList<Flight> arrivalList = FXCollections.observableArrayList();

    // --- Szűrt listák (Ezek mennek a táblázatba) ---
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
        setupFilters(); // ÚJ: Szűrők beállítása
        loadAirports();
    }

    // ----------------- Szűrési logika (ÚJ) -----------------

    private void setupFilters() {
        // Alapértelmezett szűrt listák létrehozása a nyers listákból
        filteredDepartures = new FilteredList<>(departureList, p -> true);
        filteredArrivals = new FilteredList<>(arrivalList, p -> true);

        // Bekötjük a táblázatokba a szűrt listákat
        departuresTable.setItems(filteredDepartures);
        arrivalsTable.setItems(filteredArrivals);

        // CheckBox figyelése (Mai nap)
        if (filterTodayCheckBox != null) {
            filterTodayCheckBox.setSelected(true); // Alapból bepipálva
            filterTodayCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        }

        // Keresőmező figyelése
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        }
    }

    private void updateFilters() {
        // 1. Dátum szűrés állapota
        boolean onlyToday = filterTodayCheckBox != null && filterTodayCheckBox.isSelected();
        LocalDate today = LocalDate.now();

        // 2. Keresési szöveg
        String searchText = (searchField != null && searchField.getText() != null)
                ? searchField.getText().toLowerCase().trim()
                : "";

        // Predikátum (feltétel) gyártása
        java.util.function.Predicate<Flight> filterPredicate = flight -> {
            // A. Dátum szűrés
            boolean dateMatch = true;
            if (onlyToday) {
                // Megnézzük, induló vagy érkező időt kell-e vizsgálni
                // (Mivel ez közös predikátum, itt trükkös lehet, de a Flight objektumból tudjuk, mi a releváns idő?
                // A legegyszerűbb, ha külön predikátumot írunk, vagy feltételezzük, hogy a departureList-ben a depTime, az arrivalList-ben az arrTime számít)

                // Jobb megoldás: Külön kezeljük a két listát lentebb.
                // Itt csak a szöveges keresést és az általános dátumot nézzük.
                // De mivel a FilteredList külön van, a `setPredicate`-nél külön logikát adhatunk.
                return false; // Ezt a logikát lejjebb írom meg egyedileg
            }
            return true;
        };

        // --- Indulók szűrése ---
        filteredDepartures.setPredicate(flight -> {
            // 1. Dátum (Indulás ideje)
            if (onlyToday) {
                if (flight.getScheduledDeparture() == null ||
                        !flight.getScheduledDeparture().toLocalDate().equals(today)) {
                    return false;
                }
            }
            // 2. Szöveges keresés
            return matchesSearch(flight, searchText, true);
        });

        // --- Érkezők szűrése ---
        filteredArrivals.setPredicate(flight -> {
            // 1. Dátum (Érkezés ideje)
            if (onlyToday) {
                if (flight.getScheduledArrival() == null ||
                        !flight.getScheduledArrival().toLocalDate().equals(today)) {
                    return false;
                }
            }
            // 2. Szöveges keresés
            return matchesSearch(flight, searchText, false);
        });

        updateStatusLabel();
    }

    private boolean matchesSearch(Flight flight, String searchText, boolean isDeparture) {
        if (searchText.isEmpty()) return true;

        String flightNum = flight.getFlightNumber() != null ? flight.getFlightNumber().toLowerCase() : "";
        String airline = flight.getAirlineName() != null ? flight.getAirlineName().toLowerCase() : "";
        String status = flight.getStatusText() != null ? flight.getStatusText().toLowerCase() : "";

        // Célállomás (ha induló) vagy Indulási hely (ha érkező)
        String location = "";
        if (isDeparture) {
            location = flight.getDestinationName() != null ? flight.getDestinationName().toLowerCase() : "";
        } else {
            location = flight.getOriginName() != null ? flight.getOriginName().toLowerCase() : "";
        }

        return flightNum.contains(searchText) ||
                airline.contains(searchText) ||
                location.contains(searchText) ||
                status.contains(searchText);
    }

    private void updateStatusLabel() {
        if (statusLabel != null) {
            statusLabel.setText(String.format("Megjelenítve: %d induló, %d érkező",
                    filteredDepartures.size(), filteredArrivals.size()));
        }
    }

    // ----------------- Táblák beállítása -----------------
    private void setupTables() {
        // --- 1. INDULÓK OSZLOPOK ---
        depFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        depAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        depDestinationCol.setCellValueFactory(new PropertyValueFactory<>("destinationName"));
        depTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDepartureText"));
        depStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        depGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        // Nem itt állítjuk be az items-et, hanem a setupFilters-ben a filteredList-re!
        departuresTable.setPlaceholder(new Label("Válassz repteret az induló járatokhoz!"));
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

        // Nem itt állítjuk be az items-et!
        arrivalsTable.setPlaceholder(new Label("Válassz repteret az érkező járatokhoz!"));
        arrivalsTable.setRowFactory(tv -> new TableRow<Flight>() {
            @Override
            protected void updateItem(Flight item, boolean empty) {
                super.updateItem(item, empty);
                applyRowColor(this, item, empty);
            }
        });
    }

    // ... applyRowColor metódus marad a régi ... (itt most kihagyom a hossza miatt, de másold be a régiből)
    private void applyRowColor(TableRow<Flight> row, Flight item, boolean empty) {
        if (item == null || empty) {
            row.setStyle("");
        } else {
            String status = item.getStatusText() != null ? item.getStatusText().toLowerCase() : "";
            if (status.contains("landed") || status.contains("arrived")) row.setStyle("-fx-background-color: #c8e6c9;");
            else if (status.contains("airborne") || status.contains("flying")) row.setStyle("-fx-background-color: #b3e5fc;");
            else if (status.contains("taxi")) row.setStyle("-fx-background-color: #ffe0b2;");
            else if (status.contains("boarding")) row.setStyle("-fx-background-color: #b2dfdb;");
            else if (status.contains("delayed")) row.setStyle("-fx-background-color: #fff9c4;");
            else if (status.contains("cancelled")) row.setStyle("-fx-background-color: #b00b1e; -fx-text-fill: white;");
            else if (status.contains("sched")) row.setStyle("-fx-background-color: #e0f7fa;");
            else row.setStyle("");
        }
    }

    // ----------------- Útvonalak betöltése -----------------
    private void loadRoutesForAirport(Airports airport) {
        if (airport == null) return;

        statusLabel.setText("Adatok betöltése...");

        try {
            List<Flight> deps = flightService.getDeparturesForAirport(airport);
            List<Flight> arrs = flightService.getArrivalsForAirport(airport);

            // Frissítjük a nyers listákat
            departureList.setAll(deps);
            arrivalList.setAll(arrs);

            // A szűrő automatikusan frissül, mert a FilteredList figyeli az ObservableList-et!
            updateFilters(); // Kézzel is meghívjuk, hogy biztosan lefusson a szűrés az új adatokon

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
            airportList.setAll(airportService.getAllAirports());
            airportSelector.setItems(airportList);
            if (!airportList.isEmpty()) {
                airportSelector.getSelectionModel().selectFirst(); // Opcionális: első kiválasztása
            }
        } catch (Exception e) {
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
                case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", WIDTH, HEIGHT);
                case "Repülők" -> SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", WIDTH, HEIGHT);
                case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Útvonalak", WIDTH, HEIGHT);
                case "Kapuk(Ez inkább a repterekhez menne)" -> SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", WIDTH, HEIGHT);
                case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", WIDTH, HEIGHT);
            }
        });
    }
    @FXML private void onRefresh() {
        Airports selected = airportSelector.getSelectionModel().getSelectedItem();
        if (selected != null) loadRoutesForAirport(selected);
        else loadAirports();
    }

    @FXML private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}