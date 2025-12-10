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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class HomePageController {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;

    private final AirportsService airportService;
    private final FlightService flightService;
    private final GateService gateService;
    private final com.FourWings.atcSystem.service.WeatherService weatherService;

    @FXML private ComboBox<String> menuComboBox;
    @FXML private Button dataButton;
    @FXML private Button logoutButton;

    // --- DASHBOARD ELEMEK ---
    @FXML private ComboBox<Airports> airportSelector;
    @FXML private CheckBox filterTodayCheckBox;
    @FXML private Label totalFlightsLabel;
    @FXML private Label delayedFlightsLabel;
    @FXML private Label freeGatesLabel;

    // ÚJ: időjárás + térkép kártya
    @FXML private Label weatherInfoLabel;
    @FXML private Pane airportMapPane;
    @FXML private Label airportMapInfoLabel;

    // --- IDŐJÁRÁS KÁRTYA (alsó sor bal oldala) ---
    @FXML private Label weatherTitleLabel;
    @FXML private Label weatherEmojiLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherConditionLabel;
    @FXML private Label weatherWindLabel;
    @FXML private Label weatherVisibilityLabel;
    @FXML private Label weatherPressureLabel;
    @FXML private Label weatherFeelsLikeLabel;
    @FXML private Label weatherUpdatedAtLabel;
    @FXML private Label weatherMetarLabel;

    // --- TÉRKÉP KÁRTYA (alsó sor jobb oldala) ---
    @FXML private javafx.scene.web.WebView airportMapWebView;

    private javafx.scene.web.WebEngine airportMapEngine;
    private boolean airportMapLoaded = false;

    private User loggedUser;

    // térképhez eltároljuk az aktuális reptér koordinátáit
    private Double mapLat;
    private Double mapLon;

    public HomePageController(AirportsService airportService,
                              FlightService flightService,
                              GateService gateService, com.FourWings.atcSystem.service.WeatherService weatherService) {
        this.airportService = airportService;
        this.flightService = flightService;
        this.gateService = gateService;
        this.weatherService = weatherService;
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
            // Mindig a mai napot nézzük, a checkbox csak információ
            filterTodayCheckBox.setDisable(true);
        }

        // térkép újrarajzolása, ha változik a pane mérete
        if (airportMapPane != null) {
            airportMapPane.widthProperty().addListener((obs, o, n) -> redrawAirportOnMap());
            airportMapPane.heightProperty().addListener((obs, o, n) -> redrawAirportOnMap());
        }

        loadAirports();
    }

    private void updateDashboard(Airports airport) {
        if (airport == null) {
            totalFlightsLabel.setText("0");
            delayedFlightsLabel.setText("0");
            freeGatesLabel.setText("0");
            updateWeatherCard(null);
            updateAirportMap(null);
            return;
        }

        // 1. Nyers adatok
        List<Flight> allDepartures = flightService.getDeparturesForAirport(airport);
        List<Flight> allArrivals = flightService.getArrivalsForAirport(airport);
        List<Gate> gates = gateService.getGatesForAirport(airport);

        // 2. Mai induló / érkező JÁRATSZÁM (menetrend szerint)
        LocalDate today = LocalDate.now();

        List<Flight> todayDepartures = allDepartures.stream()
                .filter(f -> f.getScheduledDeparture() != null &&
                        isSameDay(f.getScheduledDeparture(), today))
                .collect(Collectors.toList());

        List<Flight> todayArrivals = allArrivals.stream()
                .filter(f -> f.getScheduledArrival() != null &&
                        isSameDay(f.getScheduledArrival(), today))
                .collect(Collectors.toList());

        // BAL KPI: mai induló járatok
        totalFlightsLabel.setText(String.valueOf(todayDepartures.size()));

        // KÖZÉPSŐ KPI: mai érkező járatok
        delayedFlightsLabel.setText(String.valueOf(todayArrivals.size()));

        // 3. SZABAD KAPUK – maradhat az ÖSSZES járat alapján
        List<Flight> allFlightsPhysical = new ArrayList<>();
        allFlightsPhysical.addAll(allDepartures);
        allFlightsPhysical.addAll(allArrivals);

        Set<Long> occupiedGateIds = new HashSet<>();
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

        // 4. Időjárás + térkép kártyák
        updateWeatherCard(airport);
        updateAirportMap(airport);
    }

    // --- IDŐJÁRÁS KÁRTYA ---

    // ------------------------------
//  Időjárás kártya (OpenWeather)
// ------------------------------
    private void updateWeatherCard(Airports airport) {
        if (airport == null) {
            if (weatherTitleLabel != null) {
                weatherTitleLabel.setText("Nincs kiválasztott repülőtér");
            }
            if (weatherEmojiLabel != null)      weatherEmojiLabel.setText("—");
            if (weatherTempLabel != null)       weatherTempLabel.setText("—");
            if (weatherConditionLabel != null)  weatherConditionLabel.setText("—");
            if (weatherWindLabel != null)       weatherWindLabel.setText("—");
            if (weatherVisibilityLabel != null) weatherVisibilityLabel.setText("—");
            if (weatherPressureLabel != null)   weatherPressureLabel.setText("—");
            if (weatherFeelsLikeLabel != null)  weatherFeelsLikeLabel.setText("—");
            if (weatherUpdatedAtLabel != null)  weatherUpdatedAtLabel.setText("—");
            if (weatherMetarLabel != null)      weatherMetarLabel.setText("—");
            return;
        }

        try {
            var info = weatherService.getCurrentWeatherForAirport(airport);

            String city = airport.getCity() != null ? airport.getCity() : "";
            String title = airport.getIcaoCode() + " – " + airport.getName()
                    + (city.isBlank() ? "" : " (" + city + ")");

            if (weatherEmojiLabel != null)      weatherEmojiLabel.setText(info.emoji());
            if (weatherTempLabel != null)       weatherTempLabel.setText(String.format("%.1f °C", info.temperatureC()));
            if (weatherConditionLabel != null)  weatherConditionLabel.setText(info.conditionText());
            if (weatherWindLabel != null)       weatherWindLabel.setText(info.windText());
            if (weatherVisibilityLabel != null) weatherVisibilityLabel.setText("Látótávolság: " + info.visibilityText());
            if (weatherPressureLabel != null)   weatherPressureLabel.setText("Légnyomás: " + info.pressureText());
            if (weatherFeelsLikeLabel != null)  weatherFeelsLikeLabel.setText(info.feelsLikeText());
            if (weatherUpdatedAtLabel != null)  weatherUpdatedAtLabel.setText("Frissítve: " + info.updatedAtText());
            if (weatherMetarLabel != null)      weatherMetarLabel.setText(info.metarRaw());

        } catch (Exception ex) {
            ex.printStackTrace();
            if (weatherTitleLabel != null) {
                weatherTitleLabel.setText("Nem sikerült időjárási adatot lekérni.");
            }
        }
    }

    // --- TÉRKÉP KÁRTYA ---

    // ------------------------------
//  Térkép kártya – zöld pötty a reptérre
//  weather.html / showAirport(lat, lon, label)
// ------------------------------
    private void updateAirportMap(Airports airport) {
        if (airportMapWebView == null) {
            return;
        }

        if (airport == null) {
            // Ha akarod, itt ürítheted a térképet JS-sel (airportLayer.clearLayers())
            return;
        }

        java.math.BigDecimal latBD = airport.getLatitude();
        java.math.BigDecimal lonBD = airport.getLongitude();
        if (latBD == null || lonBD == null) {
            return;
        }

        double lat = latBD.doubleValue();
        double lon = lonBD.doubleValue();

        String city = airport.getCity() != null ? airport.getCity() : "";
        String label = airport.getIcaoCode() + " – " + airport.getName()
                + (city.isBlank() ? "" : " (" + city + ")");

        if (airportMapEngine == null) {
            airportMapEngine = airportMapWebView.getEngine();
            airportMapEngine.setJavaScriptEnabled(true);

            String url = getClass()
                    .getResource("/weather-map.html")   // IDE REFEReL a fenti HTML-ed
                    .toExternalForm();

            double finalLat = lat;
            double finalLon = lon;
            String finalLabel = label.replace("'", "\\'");

            airportMapEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
                if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                    airportMapLoaded = true;
                    callShowAirport(finalLat, finalLon, finalLabel);
                }
            });

            airportMapEngine.load(url);

        } else if (airportMapLoaded) {
            callShowAirport(lat, lon, label);
        }
    }

    private void callShowAirport(double lat, double lon, String label) {
        if (airportMapEngine == null) return;
        String safeLabel = label.replace("'", "\\'");
        String script = String.format(
                "showAirport(%f, %f, '%s')",
                lat, lon, safeLabel
        );
        airportMapEngine.executeScript(script);
    }

    private void redrawAirportOnMap() {
        if (airportMapPane == null) return;
        airportMapPane.getChildren().clear();

        if (mapLat == null || mapLon == null) return;

        double w = airportMapPane.getWidth();
        double h = airportMapPane.getHeight();
        if (w <= 0 || h <= 0) return;

        // Egyszerű "világtérkép" vetítés:
        // hosszúság: [-180 .. 180] -> [padding .. w-padding]
        // szélesség: [-90 .. 90]   -> [padding .. h-padding] (és felül az északi félteke)
        double padding = 30;

        double x = padding + (mapLon + 180.0) / 360.0 * (w - 2 * padding);
        double y = padding + (90.0 - mapLat) / 180.0 * (h - 2 * padding);

        Circle dot = new Circle(x, y, 7, Color.LIMEGREEN);
        dot.setStroke(Color.BLACK);
        dot.setStrokeWidth(1.5);

        airportMapPane.getChildren().add(dot);
    }

    // --- SEGÉDMETÓDUSOK ---

    private boolean isFlightOccupyingGate(Flight f) {
        if (f.getStatus() == null) return false;
        String s = f.getStatus().name().toUpperCase();

        return s.equals("BOARDING") ||
                s.equals("LANDED") ||
                s.equals("READY") ||
                s.equals("LOADING") ||
                s.equals("DELAYED");
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

    private String nullSafe(String s) {
        return s != null ? s : "";
    }
}