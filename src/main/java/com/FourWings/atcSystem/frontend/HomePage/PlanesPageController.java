package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.aircraft.AircraftService;
import com.FourWings.atcSystem.model.airline.Airline;
import com.FourWings.atcSystem.model.airline.AirlineRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class AircraftPageController {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 800; // Frissítettem 800-ra a dashboardhoz igazítva

    private final AircraftService aircraftService;
    private final AirlineRepository airlineRepository;

    // --- FXML Elemek ---
    @FXML private ComboBox<String> menuComboBox;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Button logoutButton;

    // --- TÁBLÁZAT ---
    @FXML private TableView<Aircraft> aircraftTable;

    // Oszlopok
    @FXML private TableColumn<Aircraft, Long> idColumn;
    @FXML private TableColumn<Aircraft, String> registrationColumn;
    @FXML private TableColumn<Aircraft, String> typeIcaoColumn;
    @FXML private TableColumn<Aircraft, String> airlineIdColumn; // String típusú a név miatt!
    @FXML private TableColumn<Aircraft, String> statusColumn;
    @FXML private TableColumn<Aircraft, String> msnColumn;
    @FXML private TableColumn<Aircraft, Short> seatCapacityColumn;
    @FXML private TableColumn<Aircraft, Integer> cargoCapacityColumn;
    @FXML private TableColumn<Aircraft, Long> baseAirportIdColumn;
    @FXML private TableColumn<Aircraft, Short> manufactureYearColumn;

    // Adatkezelés
    private final ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();
    private FilteredList<Aircraft> filteredAircraft;

    // Szótár: ID -> Név párosítás
    private Map<Long, String> airlineNameMap;

    // Konstruktor
    public AircraftPageController(AircraftService aircraftService, AirlineRepository airlineRepository) {
        this.aircraftService = aircraftService;
        this.airlineRepository = airlineRepository;
    }

    @FXML
    public void initialize() {
        // 1. Nevek betöltése a memóriába (gyorsítótár)
        preloadAirlineNames();

        // 2. Felület beállítása
        setupMenuNavigation();
        setupTable();

        // 3. Adatok betöltése
        loadData();

        // 4. Kereső beállítása
        setupSearch();
    }

    // --- SEGÉDMETÓDUSOK ---

    private void preloadAirlineNames() {
        List<Airline> allAirlines = airlineRepository.findAll();
        // Átalakítjuk Map-re: { 1 -> "WizzAir", 2 -> "Ryanair" }
        // Feltételezzük, hogy az Airline osztályban getId() a metódus neve
        airlineNameMap = allAirlines.stream()
                .collect(Collectors.toMap(Airline::getId, Airline::getName));
    }

    private void setupTable() {
        if (aircraftTable == null) return;

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        registrationColumn.setCellValueFactory(new PropertyValueFactory<>("registration"));
        typeIcaoColumn.setCellValueFactory(new PropertyValueFactory<>("typeIcao"));

        // LÉGITÁRSASÁG NÉV LOGIKA (Map-ből keresés)
        airlineIdColumn.setCellValueFactory(cellData -> {
            Long airlineId = cellData.getValue().getAirlineId();

            // Ha még nincs betöltve a map, vagy nincs ilyen ID
            if (airlineNameMap == null) return new SimpleStringProperty("-");

            String name = airlineNameMap.getOrDefault(airlineId, "Ismeretlen (" + airlineId + ")");
            return new SimpleStringProperty(name);
        });

        statusColumn.setCellValueFactory(cellData -> {
            var statusEnum = cellData.getValue().getStatus();
            return new SimpleStringProperty(statusEnum != null ? statusEnum.name() : "");
        });

        msnColumn.setCellValueFactory(new PropertyValueFactory<>("msn"));
        seatCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("maxSeatCapacity"));
        cargoCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("cargoCapacityBase"));
        baseAirportIdColumn.setCellValueFactory(new PropertyValueFactory<>("baseAirportId"));
        manufactureYearColumn.setCellValueFactory(new PropertyValueFactory<>("manufactureYear"));
    }

    private void loadData() {
        if (aircraftTable == null) return;
        try {
            List<Aircraft> data = aircraftService.getAllAircraft();
            aircraftList.setAll(data);

            filteredAircraft = new FilteredList<>(aircraftList, p -> true);
            aircraftTable.setItems(filteredAircraft);

            if (statusLabel != null) statusLabel.setText("Repülők betöltve: " + data.size());
        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Hiba az adatok lekérésekor!");
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (filteredAircraft == null) return;
                String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();

                filteredAircraft.setPredicate(aircraft -> {
                    if (filter.isEmpty()) return true;

                    // Adatok előkészítése a kereséshez
                    String reg = aircraft.getRegistration() != null ? aircraft.getRegistration().toLowerCase() : "";
                    String type = aircraft.getTypeIcao() != null ? aircraft.getTypeIcao().toLowerCase() : "";
                    String msn = aircraft.getMsn() != null ? aircraft.getMsn().toLowerCase() : "";
                    String status = aircraft.getStatus() != null ? aircraft.getStatus().name().toLowerCase() : "";

                    // Légitársaság nevében is keresünk
                    String airlineName = "";
                    if (airlineNameMap != null) {
                        airlineName = airlineNameMap.getOrDefault(aircraft.getAirlineId(), "").toLowerCase();
                    }

                    // Ha bármelyikben benne van a keresett szó
                    return reg.contains(filter) ||
                            type.contains(filter) ||
                            status.contains(filter) ||
                            msn.contains(filter) ||
                            airlineName.contains(filter);
                });

                if (statusLabel != null) statusLabel.setText("Találatok: " + filteredAircraft.size());
            });
        }
    }

    private void setupMenuNavigation() {
        if (menuComboBox != null) {
            menuComboBox.getSelectionModel().select("Repülők");
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                switch (newVal) {
                    case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", WIDTH, HEIGHT);
                    case "Repülők" -> {} // Már itt vagyunk
                    case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                    case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", WIDTH, HEIGHT);
                    case "Kapuk(Ez inkább a repterekhez menne)" -> SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", WIDTH, HEIGHT);
                    case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", WIDTH, HEIGHT);
                }
            });
        }
    }

    @FXML private void onRefresh() {
        preloadAirlineNames(); // Frissítéskor újratöltjük a neveket is, hátha változtak
        loadData();
    }

    @FXML private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}