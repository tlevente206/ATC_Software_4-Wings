package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.aircraft.AircraftRepository;
import com.FourWings.atcSystem.model.airline.Airline;
import com.FourWings.atcSystem.model.airline.AirlineService;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class RoutesPageController {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;

    private final AirportsService airportService;
    private final FlightService flightService;
    private final AircraftRepository aircraftRepository;
    private final AirlineService airlineService;

    // --- FXML Elemek ---
    @FXML private ComboBox<String> menuComboBox;
    @FXML private ComboBox<Airports> airportSelector;
    @FXML private Label statusLabel;
    @FXML private CheckBox filterTodayCheckBox;
    @FXML private TextField searchField;

    @FXML private WebView mapWebView;
    private WebEngine webEngine;
    private boolean isMapLoaded = false;

    @FXML private TableView<Flight> departuresTable;
    @FXML private TableColumn<Flight, String> depFlightNumCol, depAirlineCol, depDestinationCol,
            depTimeCol, depStatusCol, depGateCol;

    @FXML private TableView<Flight> arrivalsTable;
    @FXML private TableColumn<Flight, String> arrFlightNumCol, arrAirlineCol, arrOriginCol,
            arrTimeCol, arrStatusCol, arrGateCol;

    // Adatlisták
    private final ObservableList<Airports> airportList = FXCollections.observableArrayList();
    private final ObservableList<Flight> departureList = FXCollections.observableArrayList();
    private final ObservableList<Flight> arrivalList = FXCollections.observableArrayList();

    private FilteredList<Flight> filteredDepartures;
    private FilteredList<Flight> filteredArrivals;

    public RoutesPageController(AirportsService airportService,
                                FlightService flightService,
                                AircraftRepository aircraftRepository,
                                AirlineService airlineService) {
        this.airportService = airportService;
        this.flightService = flightService;
        this.aircraftRepository = aircraftRepository;
        this.airlineService = airlineService;
    }

    @FXML
    public void initialize() {
        setupMenuNavigation();
        setupTables();
        setupAirportSelector();
        setupFilters();
        loadAirports();
        loadMap();

        // ha sort választasz, automatikusan frissíti a térképet
        departuresTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showFlightOnMap(newVal);
            }
        });
        arrivalsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showFlightOnMap(newVal);
            }
        });

        // --- ÚJ: dupla kattintás a járatra -> részletek dialógus ---
        departuresTable.setRowFactory(tv -> {
            TableRow<Flight> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showFlightDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        arrivalsTable.setRowFactory(tv -> {
            TableRow<Flight> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showFlightDetailsDialog(row.getItem());
                }
            });
            return row;
        });
    }

    // ----------------- TÉRKÉP KEZELÉS -----------------

    private void loadMap() {
        if (mapWebView == null) return;

        webEngine = mapWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        URL url = getClass().getResource("/web.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    isMapLoaded = true;
                    if (statusLabel != null) {
                        statusLabel.setText("Térkép sikeresen betöltve.");
                    }
                }
            });
        } else {
            if (statusLabel != null) {
                statusLabel.setText("HIBA: Térkép fájl (web.html) nem található!");
            }
        }
    }

    @FXML
    private void onShowMap() {
        Flight selectedFlight = departuresTable.getSelectionModel().getSelectedItem();
        if (selectedFlight == null) {
            selectedFlight = arrivalsTable.getSelectionModel().getSelectedItem();
        }

        if (selectedFlight == null) {
            if (statusLabel != null) {
                statusLabel.setText("Kérlek válassz ki egy járatot a táblázatból!");
            }
            Alert alert = new Alert(Alert.AlertType.WARNING, "Válassz ki egy járatot a listából!");
            alert.show();
            return;
        }

        showFlightOnMap(selectedFlight);
    }

    private void showFlightOnMap(Flight selectedFlight) {
        if (!isMapLoaded || webEngine == null) {
            if (statusLabel != null) {
                statusLabel.setText("A térkép még nem töltődött be!");
            }
            return;
        }
        if (selectedFlight == null) return;

        try {
            Airports originAirport = selectedFlight.getDepartureAirport() != null
                    ? selectedFlight.getDepartureAirport()
                    : findAirportInDb(selectedFlight.getOriginName());

            Airports destAirport = selectedFlight.getArrivalAirport() != null
                    ? selectedFlight.getArrivalAirport()
                    : findAirportInDb(selectedFlight.getDestinationName());

            if (originAirport != null && destAirport != null &&
                    originAirport.getLatitude() != null && originAirport.getLongitude() != null &&
                    destAirport.getLatitude() != null && destAirport.getLongitude() != null) {

                double lat1 = originAirport.getLatitude().doubleValue();
                double lon1 = originAirport.getLongitude().doubleValue();
                double lat2 = destAirport.getLatitude().doubleValue();
                double lon2 = destAirport.getLongitude().doubleValue();

                if (statusLabel != null) {
                    statusLabel.setText("Útvonal kirajzolása: " +
                            originAirport.getName() + " -> " + destAirport.getName());
                }

                String script = String.format(
                        "connectAirports(%f, %f, %f, %f)",
                        lat1, lon1, lat2, lon2
                );
                webEngine.executeScript(script);
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("Hiba: Nem találhatók koordináták a repterekhez.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Hiba a koordináták lekérdezésekor: " + e.getMessage());
            }
        }
    }

    // reptér keresése, ha Flight-ban csak név/kód van
    private Airports findAirportInDb(String airportNameOrCode) {
        if (airportNameOrCode == null) return null;
        List<Airports> all = airportService.getAllAirports();
        for (Airports a : all) {
            if (airportNameOrCode.equalsIgnoreCase(a.getName())
                    || airportNameOrCode.equalsIgnoreCase(a.getIcaoCode())
                    || airportNameOrCode.equalsIgnoreCase(a.getIataCode())) {
                return a;
            }
        }
        return null;
    }

    // ----------------- ÚJ: JÁRAT DIALÓGUS – AIRCRAFT + AIRLINE TABOK -----------------


    private void showFlightDetailsDialog(Flight flight) {
        if (flight == null) return;

        Aircraft aircraft = resolveAircraft(flight);
        Airline airline = resolveAirline(flight);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/HomePage/FlightDetailsDialog.fxml")
            );
            Parent root = loader.load();

            FlightDetailsDialogController controller = loader.getController();
            controller.setData(flight, aircraft, airline);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Járat részletei – " + safe(flight.getFlightNumber()));
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setContent(root);

// >>> IDE JÖN A MÉRET BEÁLLÍTÁS <<<
            double DIALOG_WIDTH = 800;
            double DIALOG_HEIGHT = 700;

            dialog.getDialogPane().setMinWidth(DIALOG_WIDTH);
            dialog.getDialogPane().setMinHeight(DIALOG_HEIGHT);
            dialog.getDialogPane().setPrefWidth(DIALOG_WIDTH);
            dialog.getDialogPane().setPrefHeight(DIALOG_HEIGHT);
            dialog.getDialogPane().setMaxWidth(DIALOG_WIDTH);
            dialog.getDialogPane().setMaxHeight(DIALOG_HEIGHT);

// hogy ne legyen átméretezhető:
            dialog.setResizable(false);
// <<< IDÁIG >>>

            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Hiba a járat részleteinek megjelenítésekor: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }

    // aircraft adatlap
    private GridPane buildAircraftPane(Aircraft aircraft) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        if (aircraft == null) {
            grid.add(new Label("Nincs repülőgép adat ehhez a járathoz."), 0, 0);
            return grid;
        }

        int row = 0;
        grid.add(new Label("Lajstromjel:"), 0, row);
        grid.add(new Label(safe(aircraft.getRegistration())), 1, row++);

        grid.add(new Label("Típus ICAO:"), 0, row);
        grid.add(new Label(safe(aircraft.getTypeIcao())), 1, row++);

        grid.add(new Label("Max. utaskapacitás:"), 0, row);
        grid.add(new Label(safe(aircraft.getMaxSeatCapacity())), 1, row++);

        grid.add(new Label("Cargo kapacitás (alap):"), 0, row);
        grid.add(new Label(safe(aircraft.getCargoCapacityBase())), 1, row++);

        grid.add(new Label("Bázis reptér ID:"), 0, row);
        grid.add(new Label(safe(aircraft.getBaseAirportId())), 1, row++);

        grid.add(new Label("Gyártási év:"), 0, row);
        grid.add(new Label(safe(aircraft.getManufactureYear())), 1, row++);

        grid.add(new Label("Státusz:"), 0, row);
        grid.add(new Label(safe(aircraft.getStatus())), 1, row++);

        grid.add(new Label("Megjegyzés:"), 0, row);
        grid.add(new Label(safe(aircraft.getNote())), 1, row++);

        return grid;
    }

    // airline adatlap
    private GridPane buildAirlinePane(Airline airline) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        if (airline == null) {
            grid.add(new Label("Nincs légitársaság adat ehhez a járathoz."), 0, 0);
            return grid;
        }

        int row = 0;
        grid.add(new Label("Név:"), 0, row);
        grid.add(new Label(safe(airline.getName())), 1, row++);

        grid.add(new Label("ICAO kód:"), 0, row);
        grid.add(new Label(safe(airline.getIcaoCode())), 1, row++);

        grid.add(new Label("IATA kód:"), 0, row);
        grid.add(new Label(safe(airline.getIataCode())), 1, row++);

        grid.add(new Label("Ország:"), 0, row);
        grid.add(new Label(safe(airline.getCountry())), 1, row++);

        grid.add(new Label("Alapítás éve:"), 0, row);
        grid.add(new Label(safe(airline.getFoundedYear())), 1, row++);

        grid.add(new Label("Aktív:"), 0, row);
        grid.add(new Label(airline.getActive() != null && airline.getActive() ? "Igen" : "Nem"), 1, row++);

        grid.add(new Label("Üzleti modell:"), 0, row);
        grid.add(new Label(safe(airline.getBusinessMode())), 1, row++);

        grid.add(new Label("Bázis reptér:"), 0, row);
        grid.add(new Label(airline.getBaseAirport() != null
                ? safe(airline.getBaseAirport().getName())
                : ""), 1, row++);

        grid.add(new Label("Weboldal:"), 0, row);
        grid.add(new Label(safe(airline.getWebsiteUrl())), 1, row++);

        grid.add(new Label("Telefon:"), 0, row);
        grid.add(new Label(safe(airline.getPhoneMain())), 1, row++);

        grid.add(new Label("Email:"), 0, row);
        grid.add(new Label(safe(airline.getEmailMain())), 1, row++);

        grid.add(new Label("Központ címe:"), 0, row);
        grid.add(new Label(safe(airline.getHeadquartersAddress())), 1, row++);

        grid.add(new Label("Megjegyzés:"), 0, row);
        grid.add(new Label(safe(airline.getNote())), 1, row++);

        return grid;
    }

    // Aircraft feloldása: először próbáljuk getAircraft()-tal, ha nincs, getAircraftId() + repo
    private Aircraft resolveAircraft(Flight flight) {
        if (flight == null) return null;

        // 1) ha van getAircraft() metódus
        try {
            Method m = flight.getClass().getMethod("getAircraft");
            Object obj = m.invoke(flight);
            if (obj instanceof Aircraft a) {
                return a;
            }
        } catch (Exception ignored) {}

        // 2) ha van getAircraftId() és long/Long-ot ad vissza
        Long id = null;
        try {
            Method m = flight.getClass().getMethod("getAircraftId");
            Object obj = m.invoke(flight);
            if (obj instanceof Number n) {
                id = n.longValue();
            }
        } catch (Exception ignored) {}

        if (id != null) {
            return aircraftRepository.findById(id).orElse(null);
        }

        return null;
    }

    // Airline feloldása: először getAirline(), aztán ICAO/IATA, végül airlineId
    private Airline resolveAirline(Flight flight) {
        if (flight == null) return null;

        // 1) ha van getAirline() metódus és közvetlenül visszaad egy Airline-t
        try {
            Method m = flight.getClass().getMethod("getAirline");
            Object obj = m.invoke(flight);
            if (obj instanceof Airline a) {
                return a;
            }
        } catch (Exception ignored) {
            // ha nincs ilyen metódus vagy hiba van, lépünk tovább
        }

        // 2) Próbáljuk ICAO kóddal
        String code = tryGetStringProperty(flight, "getAirlineIcao");
        if (code == null) code = tryGetStringProperty(flight, "getAirlineIcaoCode");
        if (code != null && !code.isBlank()) {
            Optional<Airline> found = airlineService.findByIcao(code);
            if (found.isPresent()) return found.get();
        }

        // 3) Ha nincs ICAO, próbáljuk IATA-val
        code = tryGetStringProperty(flight, "getAirlineIata");
        if (code == null) code = tryGetStringProperty(flight, "getAirlineIataCode");
        if (code != null && !code.isBlank()) {
            Optional<Airline> found = airlineService.findByIata(code);
            if (found.isPresent()) return found.get();
        }

        // 4) airlineId alapján (ha van ilyen meződ a Flight-ban)
        Long id = null;
        try {
            Method m = flight.getClass().getMethod("getAirlineId");
            Object v = m.invoke(flight);
            if (v instanceof Number n) {
                id = n.longValue();
            }
        } catch (Exception ignored) {}

        if (id != null) {
            return airlineService.findByIdWithAirport(id).orElse(null);
        }

        // semmilyen info nincs róla
        return null;
    }

    private String tryGetStringProperty(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            Object v = m.invoke(obj);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ----------------- TÁBLÁZATOK, SZŰRÉS -----------------

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
        boolean onlyToday = filterTodayCheckBox != null && filterTodayCheckBox.isSelected();
        LocalDate today = LocalDate.now();
        String searchText = (searchField != null && searchField.getText() != null)
                ? searchField.getText().toLowerCase().trim()
                : "";

        filteredDepartures.setPredicate(flight -> {
            boolean dateMatch = !onlyToday ||
                    (flight.getScheduledDeparture() != null &&
                            flight.getScheduledDeparture().toLocalDate().equals(today));
            return dateMatch && matchesSearch(flight, searchText, true);
        });

        filteredArrivals.setPredicate(flight -> {
            boolean dateMatch = !onlyToday ||
                    (flight.getScheduledArrival() != null &&
                            flight.getScheduledArrival().toLocalDate().equals(today));
            return dateMatch && matchesSearch(flight, searchText, false);
        });

        if (statusLabel != null) {
            statusLabel.setText(String.format("Listázva: %d induló, %d érkező",
                    filteredDepartures.size(), filteredArrivals.size()));
        }
    }

    private boolean matchesSearch(Flight flight, String searchText, boolean isDeparture) {
        if (searchText.isEmpty()) return true;

        StringBuilder sb = new StringBuilder();
        if (flight.getFlightNumber() != null) sb.append(flight.getFlightNumber());
        if (flight.getAirlineName() != null) sb.append(flight.getAirlineName());
        if (flight.getStatusText() != null) sb.append(flight.getStatusText());

        String loc = isDeparture ? flight.getDestinationName() : flight.getOriginName();
        if (loc != null) sb.append(loc);

        return sb.toString().toLowerCase().contains(searchText);
    }

    private void setupTables() {
        // induló járatok
        depFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        depAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        depDestinationCol.setCellValueFactory(new PropertyValueFactory<>("destinationName"));
        depTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDepartureText"));
        depStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        depGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));

        // érkező járatok
        arrFlightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        arrAirlineCol.setCellValueFactory(new PropertyValueFactory<>("airlineName"));
        arrOriginCol.setCellValueFactory(new PropertyValueFactory<>("originName"));
        arrTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledArrivalText"));
        arrStatusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        arrGateCol.setCellValueFactory(new PropertyValueFactory<>("gateCode"));
    }

    private void loadRoutesForAirport(Airports airport) {
        if (airport == null) return;
        try {
            departureList.setAll(flightService.getDeparturesForAirport(airport));
            arrivalList.setAll(flightService.getArrivalsForAirport(airport));
            updateFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAirportSelector() {
        airportSelector.setConverter(new StringConverter<Airports>() {
            @Override
            public String toString(Airports a) {
                return (a == null) ? "" : a.getIcaoCode() + " - " + a.getName();
            }
            @Override
            public Airports fromString(String s) {
                return null;
            }
        });

        airportSelector.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> loadRoutesForAirport(newVal)
        );
    }

    private void loadAirports() {
        try {
            airportList.setAll(airportService.getAllAirports());
            airportSelector.setItems(airportList);
            if (!airportList.isEmpty()) {
                airportSelector.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            if (statusLabel != null) {
                statusLabel.setText("Hiba repterek betöltésekor.");
            }
        }
    }

    private void setupMenuNavigation() {
        if (menuComboBox == null) return;
        menuComboBox.setValue("Repülőutak");
        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Főoldal" ->
                        SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", WIDTH, HEIGHT);
                case "Repülők" ->
                        SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", WIDTH, HEIGHT);
                case "Repterek" ->
                        SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                case "Repülőutak" ->
                        SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Útvonalak", WIDTH, HEIGHT);
            }
        });
    }

    @FXML
    private void onRefresh() {
        Airports selected = airportSelector.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadRoutesForAirport(selected);
        }
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}