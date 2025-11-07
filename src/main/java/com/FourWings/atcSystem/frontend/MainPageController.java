package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.service.AuthService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MainPageController {

    private final AuthService authService;

    public MainPageController(AuthService authService) {
        this.authService = authService;
    }

    @FXML Label statusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    private void goToRegister(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegistrationPage.fxml"));
        loader.setControllerFactory(SpringContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Regisztráció");
        stage.show();
    }

    private void openUserDataPage(Stage currentStage, Object loggedInUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDataPage.fxml"));
            loader.setControllerFactory(com.FourWings.atcSystem.config.SpringContext::getBean);
            Parent root = loader.load();

            UserDataPageController ctrl = loader.getController();
            if (loggedInUser != null) {
                ctrl.initWithUser((User) loggedInUser);
            }

            currentStage.setScene(new Scene(root, 600, 400));
            currentStage.setTitle("ATC – Dashboard");
            currentStage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Nem sikerült megnyitni a Dashboardot: " + ex.getMessage());
        }
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        Task<User> task = new Task<>() {
            @Override
            protected User call() {
                return authService.loginAndGetUser(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            User loggedUser = task.getValue();
            if (loggedUser != null) {
                statusLabel.setText("Sikeres bejelentkezés!");
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                openUserDataPage(stage, loggedUser);  // <-- átadod!!!
            } else {
                statusLabel.setText("Hibás felhasználónév vagy jelszó");
            }
        });

        new Thread(task).start();
    }
}
