package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports; // Feltételezett modell a repülőtér adatokhoz
import com.FourWings.atcSystem.model.airport.AirportsService; // Feltételezett szolgáltatás a DB hozzáféréshez
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Controller a felhasználói oldal "Repterek" nézetéhez.
 * Listázza a repülőterek adatait egy táblázatban, és kezeli a navigációt.
 */
@Component
public class AirportsPageController {

    private final AirportsService airportService;

    // --- FXML Elemek ---

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
    private TableColumn<Airports, Double> elevationColumn; // Magasság

    @FXML
    private TableColumn<Airports, String> activeRunwayColumn; // Aktív Futópálya

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    // --- Adatkezelés ---
    private final ObservableList<Airports> airports = FXCollections.observableArrayList();
    private FilteredList<Airports> filteredAirports;

    // KONSTRUKTOR: Spring injektálja az AirportService-t
    public AirportsPageController(AirportsService airportService) {
        this.airportService = airportService;
    }

    // ---------------------------------------------------------
    // INIT
    // ---------------------------------------------------------

    @FXML
    public void initialize() {
        setupMenuNavigation();
        setupAirportTable();
        loadAirportData();
        setupSearchFunctionality();
    }

    /**
     * Beállítja a menüben történő navigációt (ComboBox).
     */
    private void setupMenuNavigation() {
        if (menuComboBox != null) {
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    switch (newVal) {
                        case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", 600, 400);
                        case "Repülők" -> SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 600, 400);
                        // A "Repterek" pont nem csinál semmit, mert már ezen az oldalon vagyunk
                        case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", 600, 400);
                        case "Kapuk(Ez inkább a repterekhez menne)" -> SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", 600, 400);
                        case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 600, 400);
                    }
                }
            });
            menuComboBox.getSelectionModel().select("Repterek");
        }
    }

    /**
     * Beállítja a TableView oszlopait.
     */
    private void setupAirportTable() {
        if (airportsTable != null) {
            // PropertyValueFactory: A TableColumn a megadott nevű metódust hívja
            // az Airport objektumon (pl. "icaoCode" -> getIcaoCode())
            icaoCodeColumn.setCellValueFactory(new PropertyValueFactory<>("icaoCode"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
            elevationColumn.setCellValueFactory(new PropertyValueFactory<>("elevation"));
            activeRunwayColumn.setCellValueFactory(new PropertyValueFactory<>("activeRunway"));
        }
    }

    /**
     * Lekéri a repülőtér adatokat a Service rétegből és beállítja a táblázatot.
     */
    private void loadAirportData() {
        if (airportsTable != null) {
            try {
                // Adatok lekérése a DB-ből
                List<Airports> airportList = airportService.getAllAirports();
                airports.setAll(airportList);

                // FilteredList létrehozása: kezdetben minden látszik
                filteredAirports = new FilteredList<>(airports, a -> true);
                airportsTable.setItems(filteredAirports);

                if (statusLabel != null) {
                    statusLabel.setText("Repülőterek betöltve: " + airports.size());
                }
            } catch (Exception e) {
                // Hiba kezelése (pl. ha a DB nem elérhető)
                System.err.println("Hiba a repülőtér adatok betöltésekor: " + e.getMessage());
                if (statusLabel != null) {
                    statusLabel.setText("Hiba a repülőterek betöltésekor.");
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * Beállítja a keresőmező funkcionalitását.
     */
    private void setupSearchFunctionality() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();

                // Predicate (szűrő feltétel) beállítása
                filteredAirports.setPredicate(airport -> {
                    if (filter.isEmpty()) {
                        // Üres kereső → minden elem látszik
                        return true;
                    }

                    // Keresés az ICAO kód, név és város mezőkben
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
    }

    // ---------------------------------------------------------
    // GOMBOK
    // ---------------------------------------------------------

    @FXML
    private void onRefresh() {
        loadAirportData(); // Újratölti az adatokat a DB-ből
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}