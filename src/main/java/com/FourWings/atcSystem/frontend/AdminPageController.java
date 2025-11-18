package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

@Component
public class AdminPageController {

    @FXML
    private Button FelhasznalokButton;

    @FXML
    private Label adminWelcomeLabel;

    private User loggedUser;

    public void initWithUser(User user) {
        this.loggedUser = user;
    }

    @FXML
    void goToUserAdminPage(ActionEvent event) {
        // ide jön majd a felhasználókezelő oldalra lépés
    }
}