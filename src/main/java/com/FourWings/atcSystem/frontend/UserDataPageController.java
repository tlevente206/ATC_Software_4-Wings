package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.airline.Airline;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.gate.Gate;
import com.FourWings.atcSystem.model.terminal.Terminal;
import com.FourWings.atcSystem.model.user.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
public class UserDataPageController {
    @FXML
    private Label emailLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label phoneLabel;

    private User loggedUser;
    private Airports airports;
    private Aircraft aircraft;
    private Airline airline;
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", referencedColumnName = "terminal_id")
    private Terminal terminal;
    private Gate gate;
    private Flight flight;

    public void initWithUser(User user) {
        this.loggedUser = user;

        if (user != null) {
            nameLabel.setText(user.getName());
            phoneLabel.setText(user.getPhone());
            emailLabel.setText(user.getEmail());
        } else {
            System.out.println("initWithUser() null user-rel hívva");
        }
    }

    public void setLastAirport(Airports airports) {
        this.airports = airports;
        System.out.println(airports.toString());
    }

    public void setLastAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
        System.out.println(aircraft.toString());
    }

    public void setLastAirline(Airline airline) {
        this.airline = airline;
        System.out.println(airline.toString());
    }

    public void setLastFlight(Flight flight) {
        this.flight = flight;
        System.out.println("Flight id=" + flight.getId());
    }

    public void setLastGate(Gate gate) {
        this.gate = gate;
        System.out.println("Gate id=" + gate.getId() + ", code=" + gate.getCode());
    }

    public void setLastTerminal(Terminal terminal) {
        this.terminal = terminal;
        System.out.println("Terminal id=" + terminal.getId());
    }

    @FXML
    public void openProfileEditor(ActionEvent event) {
        if (loggedUser == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hiba");
            alert.setHeaderText("Nincs betöltve felhasználó");
            alert.setContentText("A profil szerkesztéséhez előbb be kell jelentkezni.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/UserSelfEditDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            UserSelfEditDialogController ctrl = loader.getController();
            ctrl.setUser(loggedUser);

            Stage dialogStage = new Stage();
            dialogStage.initOwner(nameLabel.getScene().getWindow());
            dialogStage.setTitle("Profil szerkesztése");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // visszatérés után frissítjük a label-eket
            if (loggedUser != null) {
                nameLabel.setText( loggedUser.getName() );
                phoneLabel.setText( loggedUser.getPhone() );
                emailLabel.setText( loggedUser.getEmail() );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void toHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            // Stage megszerzése az eseményt kiváltó gombból
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("ATC – Dashboard");
            stage.show();
            stage.centerOnScreen();
        } catch (Exception ex) {
            System.out.println("To Home error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
