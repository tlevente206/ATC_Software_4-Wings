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
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import com.FourWings.atcSystem.config.SpringContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserAdminPage.fxml"));
        loader.setControllerFactory(SpringContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Bejelentkezés");
        stage.show();
    }

    @FXML
    private void onLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPage.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            // Stage megszerzése az eseményt kiváltó gombból
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 800, 400));
            stage.setTitle("ATC – Bejelentkezés");
            stage.show();
        }
        catch (Exception ex) {
            System.out.println("Logout error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


}