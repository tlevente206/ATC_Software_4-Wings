package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserRole;
import com.FourWings.atcSystem.service.dto.AirportWeatherInfo;
import com.FourWings.atcSystem.service.WeatherService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ControllerHomePageController {

    private final WeatherService weatherService;
    private final FlightService flightService;

    private User loggedUser;
    private Airports assignedAirport;

    public ControllerHomePageController(WeatherService weatherService,
                                        FlightService flightService) {
        this.weatherService = weatherService;
        this.flightService = flightService;
    }

    // --- FXML elemek ---

    @FXML private Label greetingLabel;
    @FXML private Label airportTitleLabel;
    @FXML private Label subtitleLabel;

    @FXML private Label airportBasicLabel;

    @FXML private Label weatherEmojiLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label conditionLabel;
    @FXML private Label windLabel;
    @FXML private Label visibilityLabel;
    @FXML private Label pressureLabel;
    @FXML private Label feelsLikeLabel;
    @FXML private Label updatedAtLabel;
    @FXML private Label metarLabel;

    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("Várakozás a felhasználói adatokra…");
        }
    }

    /**
     * Ezt hívja a login után a router:
     *
     * Controller felhasználó:
     * SceneManager.switchTo("Controller/ControllerHomePage.fxml", ...)
     *  → ctrl.initWithUser(loggedUser);
     */
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

        // Üdvözlés
        String name = user.getName() != null ? user.getName() : user.getUsername();
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
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    // ---------------- IDŐJÁRÁS BETÖLTÉSE ----------------

    private void refreshWeather() {
        if (assignedAirport == null) return;

        try {
            AirportWeatherInfo info = weatherService.getCurrentWeatherForAirport(assignedAirport);

            if (info == null) {
                statusLabel.setText("Nem sikerült időjárási adatokat lekérni.");
                return;
            }

            weatherEmojiLabel.setText(info.emoji());
            temperatureLabel.setText(String.format("%.0f°C", info.temperatureC()));
            conditionLabel.setText(info.conditionText());

            windLabel.setText(info.windText());            // pl. "230° / 14 kt"
            visibilityLabel.setText(info.visibilityText()); // pl. "9999 m"
            pressureLabel.setText(info.pressureText());     // pl. "1015 hPa"
            feelsLikeLabel.setText(info.feelsLikeText());   // pl. "+19°C / +13°C"
            updatedAtLabel.setText(info.updatedAtText());   // "2025-12-05 11:42 UTC"
            metarLabel.setText(info.metarRaw());

            statusLabel.setText("Időjárás frissítve.");

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Hiba az időjárás lekérésekor: " + ex.getMessage());
        }
    }

    // ---------------- GOMBOK ----------------

    /**
     * ✈️ Időjárás asszisztens megnyitása – modális dialógusban
     */
    @FXML
    private void onOpenWeatherAssistant() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/FlightWeatherAssistantDialog.fxml")
            );
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