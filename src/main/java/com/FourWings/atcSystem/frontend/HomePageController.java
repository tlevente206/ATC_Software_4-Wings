package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
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

    public void onGoToPersonalData(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDataPage.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            UserDataPageController ctrl = loader.getController();
            ctrl.initWithUser(loggedUser);   // <-- EZ A FONTOS SOR

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("ATC – Saját adatok");
            stage.show();
            stage.centerOnScreen();
        }
        catch (Exception ex) {
            System.out.println("User Data Page Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}