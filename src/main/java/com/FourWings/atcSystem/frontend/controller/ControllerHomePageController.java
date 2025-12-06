package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import com.FourWings.atcSystem.model.flight.FlightStatus;
import com.FourWings.atcSystem.model.gate.Gate;
import com.FourWings.atcSystem.model.gate.GateService;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserRole;
import com.FourWings.atcSystem.service.WeatherService;
import com.FourWings.atcSystem.service.dto.AirportWeatherInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ControllerHomePageController {

    private final WeatherService weatherService;
    private final FlightService flightService;
    private final GateService gateService;

    private User loggedUser;
    private Airports assignedAirport;

    public ControllerHomePageController(WeatherService weatherService,
                                        FlightService flightService,
                                        GateService gateService) {
        this.weatherService = weatherService;
        this.flightService = flightService;
        this.gateService = gateService;
    }

    // --- FXML elemek ---

    @FXML private Label greetingLabel;
    @FXML private Label airportTitleLabel;
    @FXML private Label subtitleLabel;

    @FXML private Label airportBasicLabel;

    // Időjárás
    @FXML private Label weatherEmojiLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label conditionLabel;
    @FXML private Label windLabel;
    @FXML private Label visibilityLabel;
    @FXML private Label pressureLabel;
    @FXML private Label feelsLikeLabel;
    @FXML private Label updatedAtLabel;
    @FXML private Label metarLabel;

    // Stat kártyák
    @FXML private Label flightsTodayLabel;
    @FXML private Label delaysTodayLabel;
    @FXML private Label freeGatesLabel;

    // Diagramok
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> trafficBarChart;

    // Alsó státusz
    @FXML private Label statusLabel;

    // ---------------- ÉLETCIKLUS ----------------

    @FXML
    public void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("Várakozás a felhasználói adatokra…");
        }
        setupDashboardIfReady();
    }

    /** Login után hívja a router. */
    public void initWithUser(User user) {
        this.loggedUser = user;

        if (user == null) {
            showError("Nincs betöltött felhasználó.",
                    "A vezérlő oldal megnyitásához be kell jelentkezni.");
            return;
        }

        if (user.getRole() != UserRole.CONTROLLER) {
            showError("Jogosultsági hiba",
                    "Ez az oldal csak 'CONTROLLER' felhasználók számára érhető el.");
            return;
        }

        this.assignedAirport = user.getAssignedAirport();
        if (assignedAirport == null) {
            showError("Nincs hozzárendelt repülőtér",
                    "A controller felhasználóhoz nincs repülőtér rendelve.");
            return;
        }

        setupDashboardIfReady();
    }

    private void setupDashboardIfReady() {
        if (loggedUser == null || assignedAirport == null) return;
        if (greetingLabel == null) return; // FXML még nem injektálódott

        String name = loggedUser.getName() != null ? loggedUser.getName() : loggedUser.getUsername();
        greetingLabel.setText("Szia, " + name + "!");
        airportTitleLabel.setText("Otthoni repülőtér: " +
                safe(assignedAirport.getIcaoCode()) + " – " +
                safe(assignedAirport.getName()));
        subtitleLabel.setText("Az aktuális időjárás és a járatok áttekintése a " +
                safe(assignedAirport.getName()) + " repülőtérre.");

        String city = safe(assignedAirport.getCity());
        airportBasicLabel.setText(
                safe(assignedAirport.getIcaoCode()) + " – " +
                        safe(assignedAirport.getName()) +
                        (city.isBlank() ? "" : " (" + city + ")")
        );

        refreshWeather();
        refreshFlightStatsAndCharts();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    // ---------------- IDŐJÁRÁS ----------------

    private void refreshWeather() {
        if (assignedAirport == null) return;

        try {
            AirportWeatherInfo info = weatherService.getCurrentWeatherForAirport(assignedAirport);
            if (info == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Nem sikerült időjárási adatokat lekérni.");
                }
                return;
            }

            weatherEmojiLabel.setText(info.emoji());
            temperatureLabel.setText(String.format("%.0f°C", info.temperatureC()));
            conditionLabel.setText(info.conditionText());
            windLabel.setText(info.windText());
            visibilityLabel.setText(info.visibilityText());
            pressureLabel.setText(info.pressureText());
            feelsLikeLabel.setText(info.feelsLikeText());
            updatedAtLabel.setText(info.updatedAtText());
            metarLabel.setText(info.metarRaw());

        } catch (Exception ex) {
            ex.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Hiba az időjárás lekérésekor: " + ex.getMessage());
            }
        }
    }

    // ---------------- STATISZTIKÁK + DIAGRAMOK ----------------

    private void refreshFlightStatsAndCharts() {
        if (assignedAirport == null) return;

        try {
            LocalDate today = LocalDate.now(ZoneId.systemDefault());

            List<Flight> departures = flightService.getDeparturesForAirport(assignedAirport);
            List<Flight> arrivals   = flightService.getArrivalsForAirport(assignedAirport);

            List<Flight> depToday = departures.stream()
                    .filter(f -> f.getScheduledDeparture() != null &&
                            f.getScheduledDeparture().toLocalDate().equals(today))
                    .toList();

            List<Flight> arrToday = arrivals.stream()
                    .filter(f -> f.getScheduledArrival() != null &&
                            f.getScheduledArrival().toLocalDate().equals(today))
                    .toList();

            long depCount = depToday.size();
            long arrCount = arrToday.size();
            long total    = depCount + arrCount;

            long delayedCount = depToday.stream()
                    .filter(f -> f.getStatus() == FlightStatus.DELAYED)
                    .count()
                    +
                    arrToday.stream()
                            .filter(f -> f.getStatus() == FlightStatus.DELAYED)
                            .count();

            if (flightsTodayLabel != null) {
                flightsTodayLabel.setText(String.valueOf(total));
            }
            if (delaysTodayLabel != null) {
                delaysTodayLabel.setText(String.valueOf(delayedCount));
            }

            // Szabad kapuk – mint a sima HomePage-en
            if (freeGatesLabel != null) {
                long freeGates = 0;
                try {
                    List<Gate> gates = gateService.getGatesForAirport(assignedAirport);

                    List<Flight> allToday = new ArrayList<>();
                    allToday.addAll(depToday);
                    allToday.addAll(arrToday);

                    Set<Long> occupiedGateIds = allToday.stream()
                            .filter(f -> f.getGate() != null)
                            .map(f -> f.getGate().getId())
                            .collect(Collectors.toSet());

                    freeGates = gates.stream()
                            .filter(g -> g.getStatus() != null &&
                                    (g.getStatus().name().equals("OPEN") ||
                                            g.getStatus().name().equals("FREE") ||
                                            g.getStatus().name().equals("ACTIVE")))
                            .filter(g -> !occupiedGateIds.contains(g.getId()))
                            .count();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                freeGatesLabel.setText(String.valueOf(freeGates));
            }

            // PieChart – csoportosítva: Landolt / Aktív / Késik / Törölve
            if (statusPieChart != null) {
                List<Flight> allToday = new ArrayList<>();
                allToday.addAll(depToday);
                allToday.addAll(arrToday);

                long landed = allToday.stream()
                        .filter(f -> isStatus(f, "LANDED", "ARRIVED"))
                        .count();

                long active = allToday.stream()
                        .filter(f -> isStatus(f, "AIRBORNE", "TAXI", "TAXING", "BOARDING", "DEPARTED"))
                        .count();

                long delayedTotal = allToday.stream()
                        .filter(f -> isStatus(f, "DELAYED"))
                        .count();

                long cancelled = allToday.stream()
                        .filter(f -> isStatus(f, "CANCELLED"))
                        .count();

                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                        new PieChart.Data("Landolt (" + landed + ")", landed),
                        new PieChart.Data("Aktív (" + active + ")", active),
                        new PieChart.Data("Késik (" + delayedTotal + ")", delayedTotal),
                        new PieChart.Data("Törölve (" + cancelled + ")", cancelled)
                );

                statusPieChart.setData(pieData);
                statusPieChart.setTitle("Mai státuszok");
            }

            // BarChart – induló / érkező
            if (trafficBarChart != null) {
                trafficBarChart.getData().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Járatszám");
                series.getData().add(new XYChart.Data<>("Induló", depCount));
                series.getData().add(new XYChart.Data<>("Érkező", arrCount));

                trafficBarChart.getData().add(series);
            }

            if (statusLabel != null) {
                statusLabel.setText("Dashboard frissítve. Ma induló: " +
                        depCount + ", érkező: " + arrCount + ", késés: " + delayedCount + ".");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Hiba a járat statisztikák frissítésekor: " + ex.getMessage());
            }
        }
    }

    // Helper: státusznévben substring keresés
    private boolean isStatus(Flight f, String... searchStrings) {
        if (f.getStatus() == null) return false;
        String s = f.getStatus().name().toUpperCase();
        for (String search : searchStrings) {
            if (s.contains(search.toUpperCase())) return true;
        }
        return false;
    }

    @FXML
    private void onRefreshDashboard() {
        refreshWeather();
        refreshFlightStatsAndCharts();
    }

    // ---------------- GOMBOK ----------------

    @FXML
    private void onOpenWeatherAssistant() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/FlightWeatherAssistantDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initOwner(statusLabel.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("ATC – Időjárás asszisztens");
            dialog.setScene(new Scene(root, 600, 420));
            dialog.centerOnScreen();
            dialog.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Hiba",
                    "Nem sikerült megnyitni az időjárás asszisztenst: " + ex.getMessage());
        }
    }

    @FXML
    private void onShowDepartures() {
        if (!checkReady()) return;

        try {
            List<Flight> departures = flightService.getDeparturesForAirport(assignedAirport);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/DeparturesDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            DeparturesDialogController ctrl = loader.getController();
            ctrl.init(assignedAirport, departures);

            Stage dialog = new Stage();
            dialog.initOwner(statusLabel.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Induló járatok – " + safe(assignedAirport.getIcaoCode()));
            dialog.setScene(new Scene(root, 900, 500));
            dialog.centerOnScreen();
            dialog.showAndWait();

            refreshFlightStatsAndCharts();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Hiba", "Nem sikerült megnyitni az induló járatok ablakot: " + ex.getMessage());
        }
    }

    @FXML
    private void onShowArrivals() {
        if (!checkReady()) return;

        try {
            List<Flight> arrivals = flightService.getArrivalsForAirport(assignedAirport);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/ArrivalsDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            ArrivalsDialogController ctrl = loader.getController();
            ctrl.init(assignedAirport, arrivals);

            Stage dialog = new Stage();
            dialog.initOwner(statusLabel.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Érkező járatok – " + safe(assignedAirport.getIcaoCode()));
            dialog.setScene(new Scene(root, 900, 500));
            dialog.centerOnScreen();
            dialog.showAndWait();

            refreshFlightStatsAndCharts();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Hiba", "Nem sikerült megnyitni az érkező járatok ablakot: " + ex.getMessage());
        }
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    private boolean checkReady() {
        if (loggedUser == null || assignedAirport == null) {
            showError("Nem elérhető", "Nincs bejelentkezett controller vagy hozzárendelt repülőtér.");
            return false;
        }
        return true;
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Hiba");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
        if (statusLabel != null) {
            statusLabel.setText(header + " – " + content);
        }
    }
}