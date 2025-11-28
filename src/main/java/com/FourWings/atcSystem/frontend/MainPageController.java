package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.service.AuthService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
        // SceneManager betölti az FXML-t és visszaadja a controller-t
        HomePageController ctrl = SceneManager.switchTo(
                "HomePage.fxml",
                "ATC – Dashboard",
                600, 400
        );

        // Felhasználó átadása
        ctrl.initWithUser(user);
    }

    private void openAdminPage(Stage stage, User user) throws Exception {

        AdminPageController ctrl = SceneManager.switchTo(
                "AdminPage.fxml",
                "ATC – Admin Dashboard",
                600, 400
        );

        // Felhasználó átadása
        ctrl.initWithUser(user);
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
        SceneManager.switchTo("RegistrationPage.fxml", "ATC – Regisztráció", 600, 400);
    }
}