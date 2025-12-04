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

    // Oszlopok definíciója (A típusokat igazítottam a Modelhez: Short, Integer, Long)
    @FXML private TableColumn<Aircraft, Long> idColumn;
    @FXML private TableColumn<Aircraft, String> registrationColumn;
    @FXML private TableColumn<Aircraft, String> typeIcaoColumn;
    @FXML private TableColumn<Aircraft, Long> airlineIdColumn;
    @FXML private TableColumn<Aircraft, String> statusColumn; // Enum -> String konverzió
    @FXML private TableColumn<Aircraft, String> msnColumn;
    @FXML private TableColumn<Aircraft, Short> seatCapacityColumn; // Short a modelben
    @FXML private TableColumn<Aircraft, Integer> cargoCapacityColumn; // Integer a modelben
    @FXML private TableColumn<Aircraft, Long> baseAirportIdColumn;
    @FXML private TableColumn<Aircraft, Short> manufactureYearColumn; // Short a modelben

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

    // --- 1. TÁBLÁZAT OSZLOPOK BEÁLLÍTÁSA (JAVÍTVA A MEZŐNEVEK) ---
    private void setupTable() {
        if (aircraftTable == null) return;

        // 1. ID: A modelben "id" a neve (nem aircraftId)
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // 2. Registration
        registrationColumn.setCellValueFactory(new PropertyValueFactory<>("registration"));

        // 3. Type ICAO
        typeIcaoColumn.setCellValueFactory(new PropertyValueFactory<>("typeIcao"));

        // 4. Airline ID
        airlineIdColumn.setCellValueFactory(new PropertyValueFactory<>("airlineId"));

        // 5. Status (ENUM kezelése)
        statusColumn.setCellValueFactory(cellData -> {
            var statusEnum = cellData.getValue().getStatus();
            return new SimpleStringProperty(statusEnum != null ? statusEnum.name() : "");
        });

        // 6. MSN
        msnColumn.setCellValueFactory(new PropertyValueFactory<>("msn"));

        // 7. Seat Capacity: A modelben "maxSeatCapacity" a neve!
        seatCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("maxSeatCapacity"));

        // 8. Cargo Capacity: A modelben "cargoCapacityBase" a neve!
        cargoCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("cargoCapacityBase"));

        // 9. Base Airport ID
        baseAirportIdColumn.setCellValueFactory(new PropertyValueFactory<>("baseAirportId"));

        // 10. Manufacture Year
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