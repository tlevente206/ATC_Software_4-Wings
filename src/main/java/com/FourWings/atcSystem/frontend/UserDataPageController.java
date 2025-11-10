package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

    public void toHome(ActionEvent actionEvent) {
    }
}
