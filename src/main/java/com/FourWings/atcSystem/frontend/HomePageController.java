package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.springframework.stereotype.Component;

@Component
public class HomePageController {

    @FXML
    private ComboBox<String> menuComboBox;
    
    @FXML
    private Button dataButton;

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

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    public void onGoToPersonalData(ActionEvent event) {
        UserDataPageController ctrl =
                SceneManager.switchTo("UserDataPage.fxml", "ATC – Saját adatok", 600, 400);
        ctrl.initWithUser(loggedUser);
    }

}