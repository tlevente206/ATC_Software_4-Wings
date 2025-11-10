package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.aircraft.AircraftService;
import com.FourWings.atcSystem.model.aircraft.AircraftStatus;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.service.AuthService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MainPageController {

    private final AuthService authService;
    private final AirportsService airportsService;
    private final AircraftService aircraftService;

    public MainPageController(AuthService authService, AirportsService airportsService, AircraftService aircraftService) {
        this.authService = authService;
        this.airportsService = airportsService;
        this.aircraftService = aircraftService;
    }

    @FXML Label statusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    private void goToRegister(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegistrationPage.fxml"));
        loader.setControllerFactory(SpringContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Regisztráció");
        stage.show();
    }

    private void openUserDataPage(Stage currentStage, Object loggedInUser, Object lastAirport, Object lastAircraft) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDataPage.fxml"));
            loader.setControllerFactory(com.FourWings.atcSystem.config.SpringContext::getBean);
            Parent root = loader.load();

            UserDataPageController ctrl = loader.getController();
            if (loggedInUser != null) {
                ctrl.initWithUser((User) loggedInUser);
                ctrl.setLastAirport((Airports) lastAirport);
                ctrl.setLastAircraft((Aircraft)  lastAircraft);
            }

            currentStage.setScene(new Scene(root, 600, 400));
            currentStage.setTitle("ATC – Dashboard");
            currentStage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Nem sikerült megnyitni a Dashboardot: " + ex.getMessage());
        }
    }

    /*private void openHomePage(Stage currentStage, Object loggedInUser, Object lastAirport, Object lastAircraft) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            HomePageController ctrl = loader.getController();
            if (loggedInUser != null) {
                ctrl.initWithUser((User) loggedInUser);
                ctrl.setLastAirport((Airports) lastAirport);
                ctrl.setLastAircraft((Aircraft)  lastAircraft);
            }

            currentStage.setScene(new Scene(root, 600, 400));
            currentStage.setTitle("ATC – Dashboard");
            currentStage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Nem sikerült megnyitni a Dashboardot: " + ex.getMessage());
        }
    }*/

    @FXML
    private void openHomePage(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
        loader.setControllerFactory(SpringContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Regisztráció");
        stage.show();
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        record LoginOutcome(User user, Airports lastAirport, Aircraft lastAircraft) {}

        Task<LoginOutcome> task = new Task<>() {
            @Override
            protected LoginOutcome call() {
                User u = authService.loginAndGetUser(username, password);
                if (u == null) return new LoginOutcome(null, null, null);
                Airports lastAirport = airportsService.getLastAdded();
                Aircraft lastAircraft = aircraftService.getLastAdded();
                return new LoginOutcome(u, lastAirport, lastAircraft);
            }
        };

        task.setOnSucceeded(e -> {
            LoginOutcome out = task.getValue();
            if (out.user() != null) {
                statusLabel.setText("Sikeres bejelentkezés!");
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                openHomePage(stage);  // <-- átadod!!!
            } else {
                statusLabel.setText("Hibás felhasználónév vagy jelszó");
            }
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            statusLabel.setText("Hiba: " + (ex != null ? ex.getMessage() : "ismeretlen"));
        });

        new Thread(task).start();
    }
}
