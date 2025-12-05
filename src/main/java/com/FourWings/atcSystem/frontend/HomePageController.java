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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HomePageController {

    // --- SERVICE-EK (Adatbázis kapcsolat) ---
    private final AirportsService airportService;
    private final FlightService flightService;
    private final GateService gateService;

    // --- FXML ELEMEK ---
    @FXML private ComboBox<String> menuComboBox;
    @FXML private Button dataButton;
    @FXML private Button logoutButton;

    // --- ÚJ DASHBOARD ELEMEK ---
    @FXML private ComboBox<Airports> airportSelector;
    @FXML private Label totalFlightsLabel;
    @FXML private Label delayedFlightsLabel;
    @FXML private Label freeGatesLabel;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> trafficBarChart;

    // --- FELHASZNÁLÓ ---
    private User loggedUser;

    // Konstruktor a Spring Injection-höz
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
        // 1. A te eredeti navigációd
        setupMenuNavigation();

        setupAirportSelector();
        loadAirports();
    }

    // -----------------------------------------------------------
    // 1. NAVIGÁCIÓ
    // -----------------------------------------------------------
    private void setupMenuNavigation() {
        if (menuComboBox != null) {
            menuComboBox.setValue("Főoldal"); // Alapértelmezett

            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                System.out.println("Kiválasztott menü: " + newVal);

                switch (newVal) {
                    case "Főoldal":
                        break; // Már itt vagyunk
                    case "Repülők":
                        SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 800, 600);
                        break;
                    case "Repterek":
                        SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", 800, 600);
                        break;
                    case "Repülőutak":
                        SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", 1200, 600);
                        break;
                    case "Kapuk(Ez inkább a repterekhez menne)":
                        SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", 800, 600);
                        break;
                    case "Terminál(Ez is inkább reptér)":
                        SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 800, 600);
                        break;
                    default:
                        System.out.println("Nincs oldal ehhez: " + newVal);
                }
            });
        }
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    @FXML
    public void onGoToPersonalData(ActionEvent event) {
        UserDataPageController ctrl = SceneManager.switchTo("UserDataPage.fxml", "ATC – Saját adatok", 600, 400);
        if (ctrl != null) {
            ctrl.initWithUser(loggedUser);
        }
    }

    // -----------------------------------------------------------
    // 2. DASHBOARD LOGIKA (ÚJ)
    // -----------------------------------------------------------
    private void updateDashboard(Airports airport) {
        if (airport == null) return;

        // Adatok lekérése
        List<Flight> departures = flightService.getDeparturesForAirport(airport);
        List<Flight> arrivals = flightService.getArrivalsForAirport(airport);
        List<Gate> gates = gateService.getGatesForAirport(airport);

        List<Flight> allFlights = new java.util.ArrayList<>();
        allFlights.addAll(departures);
        allFlights.addAll(arrivals);

        // --- KPI KÁRTYÁK ---
        totalFlightsLabel.setText(String.valueOf(allFlights.size()));

        long delayedCount = allFlights.stream()
                .filter(f -> isStatus(f, "DELAYED"))
                .count();
        delayedFlightsLabel.setText(String.valueOf(delayedCount));

        long freeGates = gates.stream()
                .filter(g -> g.getStatus() != null &&
                        (g.getStatus().name().equals("OPEN") || g.getStatus().name().equals("FREE")))
                .count();
        freeGatesLabel.setText(String.valueOf(freeGates));

        // --- KÖRDIAGRAM ---
        long landed = allFlights.stream().filter(f -> isStatus(f, "LANDED", "ARRIVED")).count();
        long active = allFlights.stream().filter(f -> isStatus(f, "AIRBORNE", "TAXING", "BOARDING", "DEPARTED")).count();
        long cancelled = allFlights.stream().filter(f -> isStatus(f, "CANCELLED")).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Landolt (" + landed + ")", landed),
                new PieChart.Data("Aktív (" + active + ")", active),
                new PieChart.Data("Késik (" + delayedCount + ")", delayedCount),
                new PieChart.Data("Törölve (" + cancelled + ")", cancelled)
        );
        statusPieChart.setData(pieData);
        statusPieChart.setTitle("Mai státuszok");

        // --- OSZLOPDIAGRAM ---
        trafficBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Járatszám");
        series.getData().add(new XYChart.Data<>("Induló", departures.size()));
        series.getData().add(new XYChart.Data<>("Érkező", arrivals.size()));
        trafficBarChart.getData().add(series);
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

    @FXML
    private void onRefresh() {
        updateDashboard(airportSelector.getSelectionModel().getSelectedItem());
    }
}