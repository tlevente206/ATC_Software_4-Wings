package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.FlightService;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.service.WeatherService;
import com.FourWings.atcSystem.service.dto.AirportWeatherInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class ControllerHomePageController {

    // --- állapot ---
    private User loggedUser;
    private Airports assignedAirport;
    private final WeatherService weatherService;
    private final FlightService flightService;

    // --- FXML elemek ---
    @FXML private BorderPane rootPane;
    @FXML private Label greetingLabel;
    @FXML private Label airportLabel;
    @FXML private Label statusLabel;
    @FXML private Label airportTitleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label airportBasicLabel;
    @FXML private javafx.scene.control.Button refreshButton;
    @FXML private javafx.scene.control.Button showDepartures;
    @FXML private javafx.scene.control.Button showArrivals;
    @FXML private javafx.scene.control.Button createFlight;
    @FXML private javafx.scene.control.Button openWeatherAssistant;
    @FXML private javafx.scene.control.Button logoutButton;

    @FXML private Label weatherEmojiLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label conditionLabel;
    @FXML private Label windLabel;
    @FXML private Label visibilityLabel;
    @FXML private Label pressureLabel;
    @FXML private Label feelsLikeLabel;
    @FXML private Label updatedAtLabel;
    @FXML private Label metarLabel;

    // 🔹 Dashboard kis kártyák számai
    @FXML private Label arrivalsCountLabel;
    @FXML private Label departuresCountLabel;

    private static final String NORMAL_STYLE =
            "-fx-background-color: rgba(248,250,252,0.06);" +
                    "-fx-text-fill: #e5e7eb;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 14 6 14;" +
                    "-fx-font-size: 13px;" +
                    "-fx-cursor: hand;";

    private static final String HOVER_STYLE =
            "-fx-background-color: rgba(248,250,252,0.18);" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 14 6 14;" +
                    "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;";

    private static final String CREATE_NORMAL_STYLE =
            "-fx-background-color: #2563eb;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 16 6 16;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-cursor: hand;";

    private static final String CREATE_HOVER_STYLE =
            "-fx-background-color: #2563eb;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 16 6 16;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;";

    private static final String LOGOUT_NORMAL_STYLE =
            "-fx-background-color: #dc2626;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 16 6 16;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-cursor: hand;";

    private static final String LOGOUT_HOVER_STYLE =
            "-fx-background-color: #dc2626;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 16 6 16;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;";

    public ControllerHomePageController(WeatherService weatherService,
                                        FlightService flightService) {
        this.weatherService = weatherService;
        this.flightService = flightService;
    }

    @FXML
    public void initialize() {

        // FULLSCREEN KOMPATIBILITÁS – NPE BIZTOS VERZIÓ
        if (rootPane != null) {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                        if (newWin instanceof Stage stage) {
                            stage.setMaximized(true);
                        }
                    });
                }
            });
        }

        if (statusLabel != null) {
            statusLabel.setText("Várakozás a felhasználói adatokra…");
        }

        setupButton(refreshButton, " ");
        setupButton(showDepartures, " ");
        setupButton(showArrivals, " ");
        setupButton(createFlight, "create");
        setupButton(openWeatherAssistant, " ");
        setupButton(logoutButton, "log");
    }

    // 🔹 Aznapi érkező járatok száma
    private void refreshTodayArrivalsCount() {
        if (assignedAirport == null || arrivalsCountLabel == null) return;

        try {
            var allArrivals = flightService.getArrivalsForAirport(assignedAirport);

            long todayCount = allArrivals.stream()
                    .filter(f -> f.getScheduledArrival() != null)
                    .filter(f -> f.getScheduledArrival().toLocalDate().equals(LocalDate.now()))
                    .count();

            arrivalsCountLabel.setText(String.valueOf(todayCount));

        } catch (Exception e) {
            e.printStackTrace();
            arrivalsCountLabel.setText("—");
        }
    }

    // 🔹 Aznapi induló járatok száma
    private void refreshTodayDeparturesCount() {
        if (assignedAirport == null || departuresCountLabel == null) return;

        try {
            var allDepartures = flightService.getDeparturesForAirport(assignedAirport);

            long todayCount = allDepartures.stream()
                    .filter(f -> f.getScheduledDeparture() != null)
                    .filter(f -> f.getScheduledDeparture().toLocalDate().equals(LocalDate.now()))
                    .count();

            departuresCountLabel.setText(String.valueOf(todayCount));

        } catch (Exception e) {
            e.printStackTrace();
            departuresCountLabel.setText("—");
        }
    }

    private void setupButton(javafx.scene.control.Button btn, String type) {
        if (btn == null) return;

        switch (type) {
            case " " -> {
                btn.setStyle(NORMAL_STYLE);
                btn.setOnMouseEntered(e -> btn.setStyle(HOVER_STYLE));
                btn.setOnMouseExited(e -> btn.setStyle(NORMAL_STYLE));
            }
            case "create" -> {
                btn.setStyle(CREATE_NORMAL_STYLE);
                btn.setOnMouseEntered(e -> btn.setStyle(CREATE_HOVER_STYLE));
                btn.setOnMouseExited(e -> btn.setStyle(CREATE_NORMAL_STYLE));
            }
            case "log" -> {
                btn.setStyle(LOGOUT_NORMAL_STYLE);
                btn.setOnMouseEntered(e -> btn.setStyle(LOGOUT_HOVER_STYLE));
                btn.setOnMouseExited(e -> btn.setStyle(LOGOUT_NORMAL_STYLE));
            }
        }
    }

    private String getTimeBasedGreeting() {
        int hour = LocalTime.now().getHour(); // 0–23

        if (hour >= 5 && hour < 10) {
            return "Jó reggelt";
        } else if (hour >= 10 && hour < 18) {
            return "Jó napot";
        } else if (hour >= 18 && hour < 22) {
            return "Jó estét";
        } else {
            return "Jó éjszakát";
        }
    }

    /**
     * Ezt hívja a MainPageController login után.
     */
    public void initWithUser(User user) {
        System.out.println("ControllerHomePage: initWithUser() hívva, user = " +
                (user != null ? user.getUsername() : "null"));

        this.loggedUser = user;

        if (user == null) {
            setStatus("Nincs bejelentkezett felhasználó.");
            return;
        }

        this.assignedAirport = user.getAssignedAirport();

        if (assignedAirport == null) {
            setStatus("Nincs hozzárendelt repülőtér a controller felhasználóhoz.");
        }

        // Köszönés
        if (greetingLabel != null) {
            String name = user.getName() != null ? user.getName() : user.getUsername();
            String greeting = getTimeBasedGreeting();
            greetingLabel.setText(greeting + ", " + name + "!");
        }

        // Reptér címek
        if (assignedAirport != null) {
            String icao = safe(assignedAirport.getIcaoCode());
            String airportName = safe(assignedAirport.getName());
            String city = safe(assignedAirport.getCity());

            if (airportTitleLabel != null) {
                airportTitleLabel.setText("Otthoni repülőtér: " + icao + " – " + airportName);
            }
            if (subtitleLabel != null) {
                subtitleLabel.setText("Áttekintés: időjárás, kapuk és mai járatok");
            }
            if (airportBasicLabel != null) {
                airportBasicLabel.setText(
                        icao + " – " + airportName + (city.isBlank() ? "" : " (" + city + ")")
                );
            }
        } else {
            if (airportTitleLabel != null) {
                airportTitleLabel.setText("Otthoni repülőtér: nincs hozzárendelve");
            }
            if (airportBasicLabel != null) {
                airportBasicLabel.setText("Nincs hozzárendelt repülőtér");
            }
        }

        setStatus("Controller dashboard alap verzió betöltve.");
        refreshWeather();
        refreshTodayArrivalsCount();
        refreshTodayDeparturesCount();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg != null ? msg : "");
        }
        System.out.println("ControllerHomePage: " + msg);
    }

    // --- Dialógusok megnyitása ---

    @FXML
    private void onShowDepartures() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/DeparturesDialog.fxml")
            );
            loader.setControllerFactory(SpringContext::getBean);

            Parent root = loader.load();

            DeparturesDialogController ctrl = loader.getController();
            if (assignedAirport != null) {
                ctrl.init(assignedAirport);
            }

            Stage dialog = new Stage();
            if (greetingLabel != null && greetingLabel.getScene() != null) {
                dialog.initOwner(greetingLabel.getScene().getWindow());
            }
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Induló járatok");
            dialog.setScene(new Scene(root, 1490, 700));
            dialog.setResizable(true);
            dialog.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onShowArrivals() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/ArrivalsDialog.fxml")
            );
            loader.setControllerFactory(SpringContext::getBean);

            Parent root = loader.load();

            ArrivalsDialogController ctrl = loader.getController();
            if (assignedAirport != null) {
                ctrl.init(assignedAirport);
            }

            Stage dialog = new Stage();
            if (greetingLabel != null && greetingLabel.getScene() != null) {
                dialog.initOwner(greetingLabel.getScene().getWindow());
            }
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Érkező járatok");
            dialog.setScene(new Scene(root, 1490, 700));
            dialog.setResizable(true);
            dialog.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshWeather() {
        if (assignedAirport == null) {
            setStatus("Nincs hozzárendelt repülőtér, nem tudok időjárást lekérni.");
            return;
        }

        if (weatherEmojiLabel == null ||
                temperatureLabel == null ||
                conditionLabel == null ||
                windLabel == null ||
                visibilityLabel == null ||
                pressureLabel == null ||
                feelsLikeLabel == null ||
                updatedAtLabel == null ||
                metarLabel == null) {

            System.out.println("ControllerHomePage: weather label-ek nem injektálódtak, kihagyom a frissítést.");
            return;
        }

        try {
            AirportWeatherInfo info = weatherService.getCurrentWeatherForAirport(assignedAirport);
            if (info == null) {
                setStatus("Nem sikerült időjárási adatokat lekérni.");
                return;
            }

            weatherEmojiLabel.setText(info.emoji());
            temperatureLabel.setText(String.format("%.0f °C", info.temperatureC()));
            conditionLabel.setText(info.conditionText());
            windLabel.setText(info.windText());
            visibilityLabel.setText(info.visibilityText());
            pressureLabel.setText(info.pressureText());
            feelsLikeLabel.setText(info.feelsLikeText());
            updatedAtLabel.setText(info.updatedAtText());
            metarLabel.setText(info.metarRaw());

            setStatus("Időjárás frissítve.");

        } catch (Exception ex) {
            ex.printStackTrace();
            setStatus("Hiba az időjárás lekérésekor: " + ex.getMessage());
        }
    }

    @FXML
    private void onOpenWeatherDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/WeatherDetailsDialog.fxml")
            );
            loader.setControllerFactory(SpringContext::getBean);

            Parent root = loader.load();

            WeatherDetailsDialogController ctrl = loader.getController();
            if (assignedAirport != null) {
                ctrl.init(assignedAirport);
            }

            Stage dialog = new Stage();
            if (statusLabel != null && statusLabel.getScene() != null) {
                dialog.initOwner(statusLabel.getScene().getWindow());
            }
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Részletes időjárás");
            dialog.setScene(new Scene(root, 1000, 720));
            dialog.setResizable(false);
            dialog.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // --- GOMBOK ---

    @FXML
    private void onDummyAction() {
        setStatus("Próba gomb megnyomva – a controller oldal működik. 🎉");
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    public void onRefreshDashboard(ActionEvent event) {
        System.out.println("ControllerHomePageController: onRefreshDashboard()");
        refreshWeather();
        refreshTodayArrivalsCount();
        refreshTodayDeparturesCount();
    }

    public void onCreateFlight(ActionEvent event) {
        System.out.println("ControllerHomePageController: onCreateFlight()");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/NewFlightDialog.fxml")
            );
            loader.setControllerFactory(SpringContext::getBean);

            Parent root = loader.load();

            NewFlightDialogController ctrl = loader.getController();
            if (assignedAirport != null) {
                ctrl.init(assignedAirport);
            }

            Stage dialog = new Stage();
            if (greetingLabel != null && greetingLabel.getScene() != null) {
                dialog.initOwner(greetingLabel.getScene().getWindow());
            }
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Új járat létrehozása");
            dialog.setScene(new Scene(root, 800, 650));
            dialog.setResizable(true);
            dialog.showAndWait();

            if (ctrl.isSavedSuccessfully()) {
                refreshTodayDeparturesCount();  // ← IDE azt írd, ami nálad tényleg tölti a táblát
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onOpenWeatherAssistant(ActionEvent event) {
        System.out.println("ControllerHomePageController: onOpenWeatherAssistant()");
    }
}