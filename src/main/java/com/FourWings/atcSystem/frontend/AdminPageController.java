package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

    @FXML
    private void onOpenWeatherAssistant(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/FlightWeatherAssistantDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // ha van admin ablakod, beállíthatod ownernek:
            // dialogStage.initOwner(someNode.getScene().getWindow());
            dialogStage.setTitle("Időjárás asszisztens");
            dialogStage.setScene(new Scene(root, 600, 420));
            dialogStage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            // ide tehetsz Alert-et is, ha szeretnél
        }
    }


}