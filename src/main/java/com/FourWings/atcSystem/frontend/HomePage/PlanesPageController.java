package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.aircraft.AircraftService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class AircraftPageController {

    private final AircraftService aircraftService;

    // --- FXML Elemek ---
    @FXML private ComboBox<String> menuComboBox;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Button logoutButton;

    // --- TÁBLÁZAT ---
    @FXML private TableView<Aircraft> aircraftTable;

    // --- OSZLOPOK (Mind a 10 kért mező) ---
    @FXML private TableColumn<Aircraft, Long> idColumn;             // aircraft_id
    @FXML private TableColumn<Aircraft, String> registrationColumn; // registration
    @FXML private TableColumn<Aircraft, String> typeIcaoColumn;     // type_icao
    @FXML private TableColumn<Aircraft, Long> airlineIdColumn;      // airline_id
    @FXML private TableColumn<Aircraft, String> statusColumn;       // status (ENUM!)
    @FXML private TableColumn<Aircraft, String> msnColumn;          // msn
    @FXML private TableColumn<Aircraft, Integer> seatCapacityColumn;// seat_capacity
    @FXML private TableColumn<Aircraft, Integer> cargoCapacityColumn;// cargo_capacity
    @FXML private TableColumn<Aircraft, Long> baseAirportIdColumn;  // base_airport_id
    @FXML private TableColumn<Aircraft, Integer> manufactureYearColumn; // manufacture_year

    // Adatkezelés
    private final ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();
    private FilteredList<Aircraft> filteredAircraft;

    public AircraftPageController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @FXML
    public void initialize() {
        setupMenuNavigation();
        setupTable();
        loadData();
        setupSearch();
    }

    // --- 1. TÁBLÁZAT OSZLOPOK BEÁLLÍTÁSA ---
    private void setupTable() {
        if (aircraftTable == null) return;

        // 1. aircraft_id -> feltételezem 'aircraftId' vagy 'id' a neve a Java osztályban
        // Ha nálad simán 'id', akkor írd át "id"-ra!
        idColumn.setCellValueFactory(new PropertyValueFactory<>("aircraftId"));

        // 2. registration
        registrationColumn.setCellValueFactory(new PropertyValueFactory<>("registration"));

        // 3. type_icao -> typeIcao
        typeIcaoColumn.setCellValueFactory(new PropertyValueFactory<>("typeIcao"));

        // 4. airline_id -> airlineId
        airlineIdColumn.setCellValueFactory(new PropertyValueFactory<>("airlineId"));

        // 5. status (ENUM kezelése)
        statusColumn.setCellValueFactory(cellData -> {
            var statusEnum = cellData.getValue().getStatus();
            return new SimpleStringProperty(statusEnum != null ? statusEnum.name() : "");
        });

        // 6. msn
        msnColumn.setCellValueFactory(new PropertyValueFactory<>("msn"));

        // 7. seat_capacity -> seatCapacity
        seatCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("seatCapacity"));

        // 8. cargo_capacity -> cargoCapacity
        cargoCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("cargoCapacity"));

        // 9. base_airport_id -> baseAirportId
        baseAirportIdColumn.setCellValueFactory(new PropertyValueFactory<>("baseAirportId"));

        // 10. manufacture_year -> manufactureYear
        manufactureYearColumn.setCellValueFactory(new PropertyValueFactory<>("manufactureYear"));
    }

    // --- 2. ADATOK BETÖLTÉSE ---
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

    // --- 3. KERESÉS ---
    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (filteredAircraft == null) return;
                String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();

                filteredAircraft.setPredicate(aircraft -> {
                    if (filter.isEmpty()) return true;

                    String reg = aircraft.getRegistration() != null ? aircraft.getRegistration().toLowerCase() : "";
                    String type = aircraft.getTypeIcao() != null ? aircraft.getTypeIcao().toLowerCase() : "";
                    String msn = aircraft.getMsn() != null ? aircraft.getMsn().toLowerCase() : "";
                    // Enum kezelése a keresésben:
                    String status = aircraft.getStatus() != null ? aircraft.getStatus().name().toLowerCase() : "";

                    return reg.contains(filter) || type.contains(filter) || status.contains(filter) || msn.contains(filter);
                });

                if (statusLabel != null) statusLabel.setText("Találatok: " + filteredAircraft.size());
            });
        }
    }

    // --- 4. MENÜ ---
    private void setupMenuNavigation() {
        if (menuComboBox != null) {
            menuComboBox.getSelectionModel().select("Repülők");
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                switch (newVal) {
                    case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", 800, 600);
                    case "Repülők" -> {}
                    case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", 1200, 600);
                    case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", 800, 600);
                    case "Kapuk(Ez inkább a repterekhez menne)" -> SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", 800, 600);
                    case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 800, 600);
                }
            });
        }
    }

    @FXML private void onRefresh() { loadData(); }
    @FXML private void onLogout() { SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400); }
}