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

    public ControllerHomePageController() {
        // üres konstruktor – Spring tölti be
    }

    @FXML
    public void initialize() {
        // Itt még NINCS user/airport, csak az FXML elemek léteznek
        if (statusLabel != null) {
            statusLabel.setText("Várakozás a felhasználói adatokra…");
        }
        System.out.println("ControllerHomePage: initialize() lefutott");
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

        // Repülőtér basic info
        if (airportLabel != null) {
            if (assignedAirport != null) {
                airportLabel.setText(
                        safe(assignedAirport.getIcaoCode()) + " – " +
                                safe(assignedAirport.getName())
                );
            } else {
                airportLabel.setText("Nincs hozzárendelt repülőtér");
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
            dialog.setScene(new Scene(root));
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
            dialog.setScene(new Scene(root, 500, 400));
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