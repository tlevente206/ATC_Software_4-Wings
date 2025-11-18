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

    @FXML private Label statusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // ---------------------- NAVIGÁCIÓ ----------------------

    private void openHomePage(Stage stage, User user) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
        loader.setControllerFactory(SpringContext::getBean);
        Parent root = loader.load();

        HomePageController ctrl = loader.getController();
        ctrl.initWithUser(user);   // átadjuk a bejelentkezett usert

        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("ATC – Dashboard");
        stage.show();
    }

    private void openAdminPage(Stage stage, User user) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminPage.fxml"));
        loader.setControllerFactory(SpringContext::getBean);
        Parent root = loader.load();

        AdminPageController ctrl = loader.getController();
        ctrl.initWithUser(user);   // itt is átadjuk

        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("ATC – Admin Dashboard");
        stage.show();
    }

    // ---------------------- LOGIN ----------------------

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
            User user = task.getValue();

            if (user == null) {
                statusLabel.setText("Hibás felhasználónév vagy jelszó");
                return;
            }

            statusLabel.setText("Sikeres bejelentkezés!");
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            try {
                if (user.isAdmin()) {      // <--- EZ A LÉNYEG
                    openAdminPage(stage, user);
                } else {
                    openHomePage(stage, user);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Navigációs hiba: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            statusLabel.setText("Hiba: " + ex.getMessage());
        });

        new Thread(task).start();
    }

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
}