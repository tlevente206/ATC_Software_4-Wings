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
                if (newVal == null) return;

                System.out.println("Kiválasztott menü: " + newVal);

                switch (newVal) {
                    case "Főoldal":
                        // MÁR ITT VAGYUNK → NEM KELL ÚJRA BETÖLTENI
                        break;

                    case "Repülők":
                        SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 600, 400);
                        break;

                    case "Repterek":
                        SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", 600, 400);
                        break;

                    case "Repülőutak":
                        SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Repülőutak", 600, 400);
                        break;

                    case "Kapuk(Ez inkább a repterekhez menne)":
                        SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", 600, 400);
                        break;

                    case "Terminál(Ez is inkább reptér)":
                        SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 600, 400);
                        break;

                    default:
                        System.out.println("Nincs oldal ehhez: " + newVal);
                }
            });

            // Nyugodtan maradhat, csak most már nem okoz végtelen ciklust
            menuComboBox.getSelectionModel().select("Főoldal");
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