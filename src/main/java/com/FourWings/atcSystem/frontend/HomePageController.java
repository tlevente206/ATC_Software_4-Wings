
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
        if (menuComboBox != null) {
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    System.out.println("Kiválasztott menü: " + newVal);
                    switch (newVal) {
                        case "Repülők":
                            SceneManager.switchTo("PlanesPage.fxml", "ATC – Repülők", 800, 600);
                            break;
                        case "Repterek":
                            SceneManager.switchTo("AirportsPage.fxml", "ATC – Repterek", 800, 600);
                            break;
                        case "Repülőutak":
                            SceneManager.switchTo("RoutesPage.fxml", "ATC – Repülőutak", 800, 600);
                            break;
                        case "Kapuk(Ez inkább a repterekhez menne)":
                            SceneManager.switchTo("GatesPage.fxml", "ATC – Kapuk", 800, 600);
                            break;
                        case "Terminál(Ez is inkább reptér)":
                            SceneManager.switchTo("TerminalPage.fxml", "ATC – Terminál", 800, 600);
                            break;
                        default:
                            System.out.println("Nincs oldal ehhez: " + newVal);
                    }
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
