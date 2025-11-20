package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
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
    public void goToUserAdminPage(ActionEvent event) throws Exception{
        SceneManager.switchTo("UserAdminPage.fxml", "Bejelentkezés", 1280, 720);
    }

    @FXML
    private void onLogout(ActionEvent event) {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }


}