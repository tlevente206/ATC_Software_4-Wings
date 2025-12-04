package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AirportsPageController {

    private final AirportsService airportService;

    @FXML
    private ComboBox<String> menuComboBox;

    @FXML
    private TableView<Airports> airportsTable;

    @FXML
    private TableColumn<Airports, String> icaoCodeColumn;

    @FXML
    private TableColumn<Airports, String> nameColumn;

    @FXML
    private TableColumn<Airports, String> cityColumn;

    @FXML
    private TableColumn<Airports, Integer> elevationColumn; // Airports-ben Integer az elevation

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    private final ObservableList<Airports> airports = FXCollections.observableArrayList();
    private FilteredList<Airports> filteredAirports;

    public AirportsPageController(AirportsService airportService) {
        this.airportService = airportService;
    }

    @FXML
    public void initialize() {
        setupAirportTable();
        loadAirportData();
        setupSearchFunctionality();
        setupMenuNavigation();
    }

    // ----------------- Menü navigáció -----------------

    private void setupMenuNavigation() {
        if (menuComboBox != null) {

            // ha nem FXML-ben töltöd fel az opciókat, akkor:
            // menuComboBox.getItems().setAll(
            //         "Főoldal",
            //         "Repülők",
            //         "Repterek",
            //         "Repülőutak",
            //         "Kapuk(Ez inkább a repterekhez menne)",
            //         "Terminál(Ez is inkább reptér)"
            // );

            // 1) aktuális oldal kijelölése
            menuComboBox.getSelectionModel().select("Repterek");

            // 2) utána listener
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;

                switch (newVal) {
                    case "Főoldal" ->
                            SceneManager.switchTo("HomePage/HomePage.fxml", "ATC – Főoldal", 800, 600);
                    case "Repülők" ->
                            SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 800, 600);
                    case "Repterek" ->
                    // már ezen az oldalon vagyunk → ne töltsük újra
                    {}
                    case "Repülőutak" ->
                            SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", 800, 600);
                    case "Kapuk(Ez inkább a repterekhez menne)" ->
                            SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", 800, 600);
                    case "Terminál(Ez is inkább reptér)" ->
                            SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 800, 600);
                }
            });
        }
    }

    // ----------------- Táblázat beállítása -----------------

    private void setupAirportTable() {
        if (airportsTable == null) return;

        icaoCodeColumn.setCellValueFactory(new PropertyValueFactory<>("icaoCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        elevationColumn.setCellValueFactory(new PropertyValueFactory<>("elevation"));

        // Ha nincs activeRunway mező az entity-ben, NE használd:
        // activeRunwayColumn.setCellValueFactory(new PropertyValueFactory<>("activeRunway"));
    }

    // ----------------- Adatok betöltése -----------------

    private void loadAirportData() {
        if (airportsTable == null) return;

        try {
            List<Airports> airportList = airportService.getAllAirports();
            airports.setAll(airportList);

            filteredAirports = new FilteredList<>(airports, a -> true);
            airportsTable.setItems(filteredAirports);

            if (statusLabel != null) {
                statusLabel.setText("Repülőterek betöltve: " + airports.size());
            }

        } catch (Exception e) {
            System.err.println("Hiba a repülőtér adatok betöltésekor: " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Hiba a repülőterek betöltésekor.");
            }
        }
    }

    // ----------------- Keresés -----------------

    private void setupSearchFunctionality() {
        if (searchField == null) return;

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredAirports == null) return; // ha valamiért nem töltődött be

            String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();

            filteredAirports.setPredicate(airport -> {
                if (filter.isEmpty()) {
                    return true;
                }

                String icao = airport.getIcaoCode() != null ? airport.getIcaoCode().toLowerCase() : "";
                String name = airport.getName() != null ? airport.getName().toLowerCase() : "";
                String city = airport.getCity() != null ? airport.getCity().toLowerCase() : "";

                return icao.contains(filter)
                        || name.contains(filter)
                        || city.contains(filter);
            });

            if (statusLabel != null) {
                statusLabel.setText("Találatok: " + filteredAirports.size() + " / Összes: " + airports.size());
            }
        });
    }

    // ----------------- Gombok -----------------

    @FXML
    private void onRefresh() {
        loadAirportData();
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}