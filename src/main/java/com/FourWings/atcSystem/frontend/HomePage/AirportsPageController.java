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

    // Menü
    @FXML
    private ComboBox<String> menuComboBox;

    // Tábla + oszlopok
    @FXML
    private TableView<Airports> airportsTable;

    @FXML
    private TableColumn<Airports, String> icaoCodeColumn;
    @FXML
    private TableColumn<Airports, String> iataCodeColumn;
    @FXML
    private TableColumn<Airports, String> nameColumn;
    @FXML
    private TableColumn<Airports, String> cityColumn;
    @FXML
    private TableColumn<Airports, String> countryColumn;
    @FXML
    private TableColumn<Airports, String> timezoneColumn;

    @FXML
    private TableColumn<Airports, Number> latitudeColumn;
    @FXML
    private TableColumn<Airports, Number> longitudeColumn;
    @FXML
    private TableColumn<Airports, Integer> elevationColumn;

    @FXML
    private TableColumn<Airports, String> addressColumn;
    @FXML
    private TableColumn<Airports, String> postalCodeColumn;
    @FXML
    private TableColumn<Airports, String> websiteUrlColumn;
    @FXML
    private TableColumn<Airports, String> phoneMainColumn;
    @FXML
    private TableColumn<Airports, String> emailMainColumn;

    // Keresés + státusz
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
        if (menuComboBox == null) return;

        // Ha nem FXML-ből töltenéd, itt is lehetne:
        // menuComboBox.getItems().setAll("Főoldal", "Repülők", ... );

        // aktuális oldal kijelölése
        menuComboBox.getSelectionModel().select("Repterek");

        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            switch (newVal) {
                case "Főoldal" ->
                        SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", 800, 600);
                case "Repülők" ->
                        SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 800, 600);
                case "Repterek" ->
                { /* már ezen az oldalon vagyunk */ }
                case "Repülőutak" ->
                        SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", 800, 600);
                case "Kapuk(Ez inkább a repterekhez menne)" ->
                        SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", 800, 600);
                case "Terminál(Ez is inkább reptér)" ->
                        SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 800, 600);
            }
        });
    }

    // ----------------- Táblázat beállítása -----------------

    private void setupAirportTable() {
        if (airportsTable == null) return;

        // Itt a property neveknek a Airports entity mezőneveihez kell passzolniuk:
        // private String icaoCode; private String iataCode; stb.
        icaoCodeColumn.setCellValueFactory(new PropertyValueFactory<>("icaoCode"));
        iataCodeColumn.setCellValueFactory(new PropertyValueFactory<>("iataCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        timezoneColumn.setCellValueFactory(new PropertyValueFactory<>("timezone"));

        latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        longitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        elevationColumn.setCellValueFactory(new PropertyValueFactory<>("elevation"));

        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        postalCodeColumn.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        websiteUrlColumn.setCellValueFactory(new PropertyValueFactory<>("websiteUrl"));
        phoneMainColumn.setCellValueFactory(new PropertyValueFactory<>("phoneMain"));
        emailMainColumn.setCellValueFactory(new PropertyValueFactory<>("emailMain"));
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
            if (filteredAirports == null) return;

            String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();

            filteredAirports.setPredicate(airport -> {
                if (filter.isEmpty()) return true;

                String icao = airport.getIcaoCode() != null ? airport.getIcaoCode().toLowerCase() : "";
                String name = airport.getName() != null ? airport.getName().toLowerCase() : "";
                String city = airport.getCity() != null ? airport.getCity().toLowerCase() : "";

                return icao.contains(filter)
                        || name.contains(filter)
                        || city.contains(filter);
            });

            if (statusLabel != null) {
                statusLabel.setText("Találatok: "
                        + filteredAirports.size()
                        + " / Összes: "
                        + airports.size());
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