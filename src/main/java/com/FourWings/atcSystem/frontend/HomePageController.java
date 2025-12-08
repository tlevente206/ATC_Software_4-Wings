package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import com.FourWings.atcSystem.model.gate.Gate;
import com.FourWings.atcSystem.model.gate.GateService;
import com.FourWings.atcSystem.model.user.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HomePageController {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;

    private final AirportsService airportService;
    private final FlightService flightService;
    private final GateService gateService;

    @FXML private ComboBox<String> menuComboBox;
    @FXML private Button dataButton;
    @FXML private Button logoutButton;

    // --- DASHBOARD ELEMEK ---
    @FXML private ComboBox<Airports> airportSelector;
    @FXML private CheckBox filterTodayCheckBox;
    @FXML private Label totalFlightsLabel;
    @FXML private Label delayedFlightsLabel;
    @FXML private Label freeGatesLabel;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> trafficBarChart;

    private User loggedUser;

    public HomePageController(AirportsService airportService, FlightService flightService, GateService gateService) {
        this.airportService = airportService;
        this.flightService = flightService;
        this.gateService = gateService;
    }

    public void initWithUser(User user) {
        this.loggedUser = user;
    }

    @FXML
    public void initialize() {
        setupMenuNavigation();
        setupAirportSelector();

        if (filterTodayCheckBox != null) {
            filterTodayCheckBox.setSelected(true);
            filterTodayCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onRefresh());
        }

        loadAirports();
    }

    private void updateDashboard(Airports airport) {
        if (airport == null) return;

        // 1. Minden adat lekérése (Nyers adatok)
        List<Flight> allDepartures = flightService.getDeparturesForAirport(airport);
        List<Flight> allArrivals = flightService.getArrivalsForAirport(airport);
        List<Gate> gates = gateService.getGatesForAirport(airport);

        // 2. Szűrés a diagramokhoz és a forgalmi adatokhoz (Ma / Összes)
        List<Flight> filteredDepartures;
        List<Flight> filteredArrivals;

        if (filterTodayCheckBox != null && filterTodayCheckBox.isSelected()) {
            LocalDate today = LocalDate.now();
            filteredDepartures = allDepartures.stream()
                    .filter(f -> isSameDay(f.getActualDeparture(), today))
                    .collect(Collectors.toList());
            filteredArrivals = allArrivals.stream()
                    .filter(f -> isSameDay(f.getActualArrival(), today))
                    .collect(Collectors.toList());
        } else {
            filteredDepartures = new ArrayList<>(allDepartures);
            filteredArrivals = new ArrayList<>(allArrivals);
        }

        // Ez a lista a képernyőn megjelenő statisztikákhoz (diagramok) kell
        List<Flight> displayedFlights = new ArrayList<>();
        displayedFlights.addAll(filteredDepartures);
        displayedFlights.addAll(filteredArrivals);

        // --- KPI 1 & 2: Ezeknek reagálniuk kell a szűrőre ---
        totalFlightsLabel.setText(String.valueOf(displayedFlights.size()));

        long delayedCount = displayedFlights.stream()
                .filter(f -> isStatus(f, "DELAYED"))
                .count();
        delayedFlightsLabel.setText(String.valueOf(delayedCount));

        // --- KPI 3: SZABAD KAPUK (JAVÍTÁS: MINDIG AZ ÖSSZES ADATBÓL) ---
        // A kapu fizikai állapota nem függ attól, hogy mire szűrünk a képernyőn.
        // Ezért itt létrehozunk egy listát az ÖSSZES aktív járatról (szűrés nélkül).
        List<Flight> allFlightsPhysical = new ArrayList<>();
        allFlightsPhysical.addAll(allDepartures);
        allFlightsPhysical.addAll(allArrivals);

        Set<Long> occupiedGateIds = new HashSet<>();

        // Itt az allFlightsPhysical-on megyünk végig, NEM a displayedFlights-on!
        for (Flight f : allFlightsPhysical) {
            if (f.getGate() != null && isFlightOccupyingGate(f)) {
                occupiedGateIds.add(f.getGate().getId());
            }
        }

        long freeGates = gates.stream()
                .filter(g -> g.getStatus() != null &&
                        (g.getStatus().name().equals("OPEN") ||
                                g.getStatus().name().equals("FREE") ||
                                g.getStatus().name().equals("ACTIVE")))
                .filter(g -> !occupiedGateIds.contains(g.getId()))
                .count();

        freeGatesLabel.setText(String.valueOf(freeGates));

        // --- KÖRDIAGRAM (Marad a szűrt listánál) ---
        long landed = displayedFlights.stream().filter(f -> isStatus(f, "LANDED", "ARRIVED")).count();
        long active = displayedFlights.stream().filter(f -> isStatus(f, "AIRBORNE", "TAXING", "BOARDING", "DEPARTED")).count();
        long cancelled = displayedFlights.stream().filter(f -> isStatus(f, "CANCELLED")).count();
        long scheduled = displayedFlights.stream().filter(f -> isStatus(f, "SCHEDULED", "READY")).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (landed > 0) pieData.add(new PieChart.Data("Landolt (" + landed + ")", landed));
        if (active > 0) pieData.add(new PieChart.Data("Aktív (" + active + ")", active));
        if (delayedCount > 0) pieData.add(new PieChart.Data("Késik (" + delayedCount + ")", delayedCount));
        if (cancelled > 0) pieData.add(new PieChart.Data("Törölve (" + cancelled + ")", cancelled));
        if (scheduled > 0) pieData.add(new PieChart.Data("Tervezett (" + scheduled + ")", scheduled));

        statusPieChart.setData(pieData);
        statusPieChart.setTitle(filterTodayCheckBox.isSelected() ? "Mai státuszok" : "Összesített státuszok");
        if (pieData.isEmpty()) statusPieChart.setTitle("Nincs megjeleníthető adat");

        // --- OSZLOPDIAGRAM (Marad a szűrt listánál) ---
        trafficBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Járatszám");
        series.getData().add(new XYChart.Data<>("Induló", filteredDepartures.size()));
        series.getData().add(new XYChart.Data<>("Érkező", filteredArrivals.size()));
        trafficBarChart.getData().add(series);
    }

    // --- SEGÉDMETÓDUSOK ---

    // Ez dönti el, hogy egy repülő ÉPPEN elfoglalja-e a kaput
    private boolean isFlightOccupyingGate(Flight f) {
        if (f.getStatus() == null) return false;
        String s = f.getStatus().name().toUpperCase();

        // Ezek a státuszok jelentik azt, hogy a gép fizikailag a kapunál van:
        return s.equals("BOARDING") ||
                s.equals("LANDED") ||   // Épp leszállt, begurult
                s.equals("READY") ||    // Indulásra kész
                s.equals("LOADING") ||  // Rakodás
                s.equals("DELAYED");    // Késik, de valószínűleg ott áll (vagy várakozik rá)

        // Ami NEM foglalja a kaput:
        // SCHEDULED (még nincs ott)
        // AIRBORNE (már/még levegőben)
        // DEPARTED (felszállt)
        // CANCELLED (törölve)
    }

    private boolean isSameDay(LocalDateTime dateTime, LocalDate today) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate().equals(today);
    }

    private boolean isStatus(Flight f, String... searchStrings) {
        if (f.getStatus() == null) return false;
        String s = f.getStatus().name().toUpperCase();
        for (String search : searchStrings) {
            if (s.contains(search)) return true;
        }
        return false;
    }

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
            updateDashboard(newVal);
        });
    }

    private void loadAirports() {
        try {
            airportSelector.setItems(FXCollections.observableArrayList(airportService.getAllAirports()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMenuNavigation() {
        if (menuComboBox != null) {
            menuComboBox.setValue("Főoldal");
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                switch (newVal) {
                    case "Főoldal" -> {}
                    case "Repülők" -> SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", WIDTH, HEIGHT);
                    case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                    case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", WIDTH, HEIGHT);
                    case "Kapuk(Ez inkább a repterekhez menne)" -> SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", WIDTH, HEIGHT);
                    case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", WIDTH, HEIGHT);
                }
            });
        }
    }

    @FXML private void onRefresh() {
        updateDashboard(airportSelector.getSelectionModel().getSelectedItem());
    }

    @FXML private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    @FXML public void onGoToPersonalData(ActionEvent event) {
        UserDataPageController ctrl = SceneManager.switchTo("UserDataPage.fxml", "ATC – Saját adatok", WIDTH, HEIGHT);
        if (ctrl != null) ctrl.initWithUser(loggedUser);
    }
}