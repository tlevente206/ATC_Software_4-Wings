package com.FourWings.atcSystem.frontend;

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
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

        nameLabel.setText(loggedUser.getName());
        phoneLabel.setText(loggedUser.getPhone());
        emailLabel.setText(loggedUser.getEmail());
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
}
