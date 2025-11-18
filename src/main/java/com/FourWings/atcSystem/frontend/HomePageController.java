package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
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

    @FXML
    private void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPage.fxml"));
            loader.setControllerFactory(com.FourWings.atcSystem.config.SpringContext::getBean);
            Parent root = loader.load();

            // Aktuális stage megszerzése
            Stage stage = (Stage) menuComboBox.getScene().getWindow();

            stage.setScene(new Scene(root, 800, 400));
            stage.setTitle("ATC – Bejelentkezés");
            stage.show();
            stage.centerOnScreen();
        }
        catch (Exception ex) {
            System.out.println("Logout error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}