package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

@Component
public class HomePageController {

    @FXML
    private ComboBox<String> menuComboBox;


    private User loggedUser;

    public void initWithUser(User user) {
        this.loggedUser = user;
    }

    @FXML
    public void initialize() {
        // Listener a kiválasztott elemre
        if (menuComboBox != null) {
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    System.out.println("Kiválasztott menü: " + newVal);
                    // Itt lehet majd oldalváltás
                }
            });
        }
    }
}