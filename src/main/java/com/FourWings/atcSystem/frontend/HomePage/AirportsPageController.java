package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;

@Component
public class AirportsPageController {

    @FXML
    private ComboBox<String> menuComboBox;

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<String> airportsTable; // Ide jön az Airport model, cseréld arra

    @FXML
    public void initialize() {
        // ComboBox menü kezelése
        if (menuComboBox != null) {
            menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    switch (newVal) {
                        case "Főoldal":
                            SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", 600, 400);
                            break;
                        case "Repülők":
                            SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 600, 400);
                            break;
                        case "Repterek":
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
                            System.out.println("Sikertelen oldal átirányítás!");
                    }
                }
            });
            menuComboBox.getSelectionModel().select("Repterek");
        }

        // Teszt adatok a TableView-hoz (később Airport model lesz)
        if (airportsTable != null) {
            ObservableList<String> data = FXCollections.observableArrayList(
                    "Budapest Liszt Ferenc", "Debrecen Airport", "London Heathrow"
            );
            airportsTable.setItems(data);
        }
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }
}