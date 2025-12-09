package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.user.User;
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

import java.time.LocalTime;

@Component
public class ControllerHomePageController {

    // --- állapot ---
    private User loggedUser;
    private Airports assignedAirport;

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
            "-fx-font-weight: bold;" + "-fx-font-size: 13px;" +
            "-fx-cursor: hand;";

    private static final String CREATE_HOVER_STYLE =
            "-fx-background-color: #2563eb;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 16 6 16;" +
                    "-fx-font-weight: bold;" + "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;";

    private static final String LOGOUT_NORMAL_STYLE =
            "-fx-background-color: #dc2626;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 16 6 16;" +
                    "-fx-font-weight: bold;" + "-fx-font-size: 13px;" +
                    "-fx-cursor: hand;";

    private static final String LOGOUT_HOVER_STYLE =
            "-fx-background-color: #dc2626;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 6 16 6 16;" +
                    "-fx-font-weight: bold;" + "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;";

    public ControllerHomePageController() {
        // üres konstruktor – Spring tölti be
    }

    @FXML
    public void initialize() {

        // Itt még NINCS user/airport, csak az FXML elemek léteznek
        if (statusLabel != null) {
            statusLabel.setText("Várakozás a felhasználói adatokra…");
        }

        setupButton(refreshButton, " ");
        setupButton(showDepartures, " ");
        setupButton(showArrivals, " ");
        setupButton(createFlight, "create");
        setupButton(openWeatherAssistant, " ");
        setupButton(logoutButton, "log");

        System.out.println("ControllerHomePage: initialize() lefutott");
    }

    private void setupButton(javafx.scene.control.Button btn, String type) {

        if (type.equals(" ")) {
            if (btn == null) return;
            btn.setStyle(NORMAL_STYLE);
            btn.setOnMouseEntered(e -> btn.setStyle(HOVER_STYLE));
            btn.setOnMouseExited(e -> btn.setStyle(NORMAL_STYLE));
        }else if (type.equals("create")) {
            if (btn == null) return;
            btn.setStyle(CREATE_NORMAL_STYLE);
            btn.setOnMouseEntered(e -> btn.setStyle(CREATE_HOVER_STYLE));
            btn.setOnMouseExited(e -> btn.setStyle(CREATE_NORMAL_STYLE));
        }else if (type.equals("log")) {
            if (btn == null) return;
            btn.setStyle(LOGOUT_NORMAL_STYLE);
            btn.setOnMouseEntered(e -> btn.setStyle(LOGOUT_HOVER_STYLE));
            btn.setOnMouseExited(e -> btn.setStyle(LOGOUT_NORMAL_STYLE));
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
            String greeting = getTimeBasedGreeting();   // napszakhoz illő köszönés
            greetingLabel.setText(greeting + ", " + name + "!");
        }

        // Reptér címek – ITT ÍRATJUK KI AZ OTTHONI REPTÉRT
        if (assignedAirport != null) {
            String icao = safe(assignedAirport.getIcaoCode());
            String airportName = safe(assignedAirport.getName());
            String city = safe(assignedAirport.getCity());

            if (airportTitleLabel != null) {
                airportTitleLabel.setText("Otthoni repülőtér: " + icao + " – " + airportName);
            }
            if (subtitleLabel != null) {
                subtitleLabel.setText("Áttekintés: időjárás, kapuk és mai járatok a "
                        + airportName + " repülőtéren.");
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

    @FXML
    private void onShowDepartures() {
        try {
            // FXML betöltése – egyszerű, még nem adunk át semmit
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/DeparturesDialog.fxml")
            );
            loader.setControllerFactory(SpringContext::getBean);

            Parent root = loader.load();

            Stage dialog = new Stage();
            // ownernek használjuk pl. a greetingLabel-t (bármelyik már beinjektált Node jó)
            if (greetingLabel != null && greetingLabel.getScene() != null) {
                dialog.initOwner(greetingLabel.getScene().getWindow());
            }
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Induló járatok");
            dialog.setScene(new Scene(root));
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

            Stage dialog = new Stage();
            // ugyanúgy, mint az indulóknál: használjuk ownernek a greetingLabel-t
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

    @FXML
    private void onOpenWeatherDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Controller/WeatherDetailsDialog.fxml")
            );
            loader.setControllerFactory(SpringContext::getBean);

            Parent root = loader.load();

            Stage dialog = new Stage();
            if (statusLabel != null && statusLabel.getScene() != null) {
                dialog.initOwner(statusLabel.getScene().getWindow());
            }
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Részletes időjárás");
            dialog.setScene(new Scene(root, 500, 600));
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
    }

    public void onCreateFlight(ActionEvent event) {
        System.out.println("ControllerHomePageController: onCreateFlight()");
    }

    public void onOpenWeatherAssistant(ActionEvent event) {
        System.out.println("ControllerHomePageController: onOpenWeatherAssistant()");
    }

}