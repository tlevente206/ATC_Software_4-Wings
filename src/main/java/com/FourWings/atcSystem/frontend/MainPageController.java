package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.frontend.controller.ControllerHomePageController;
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
    public static final int WIDTH = 1200; //Window szélesség
    public static final int HEIGHT = 600; //Window magasság

    public MainPageController(AuthService authService) {
        this.authService = authService;
    }

    @FXML private Label statusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // ---------------------- NAVIGÁCIÓK ----------------------

    /** Sima USER főoldal */
    private void openUserHomePage(Stage stage, User user) throws Exception {
        HomePageController ctrl = SceneManager.switchTo(
                "HomePage.fxml",
                "ATC – Dashboard",
                WIDTH, HEIGHT
        );
        ctrl.initWithUser(user);
    }

    /** ADMIN dashboard */
    private void openAdminPage(Stage stage, User user) throws Exception {
        AdminPageController ctrl = SceneManager.switchTo(
                "AdminPage.fxml",
                "ATC – Admin Dashboard",
                600, 400
        );
        ctrl.initWithUser(user);
    }

    /** CONTROLLER főoldal (irányító munkaállomás) */
    private void openControllerHomePage(Stage stage, User user) throws Exception {
        ControllerHomePageController ctrl = SceneManager.switchTo(
                "ControllerHomePage.fxml",          // <- FXML neve
                "ATC – Irányító munkaállomás",      // ablak címe
                1100, 700                           // nagyobb, „dashboard” méret
        );
        ctrl.initWithUser(user);
    }

    // ---------------------- LOGIN ----------------------

    @FXML
    private void onLogin(ActionEvent event) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Add meg a felhasználónevet és jelszót.");
            return;
        }

        // opcionális: gombok tiltása amíg fut a login
        loginButton.setDisable(true);
        registerButton.setDisable(true);
        statusLabel.setText("Bejelentkezés folyamatban...");

        Task<User> task = new Task<>() {
            @Override
            protected User call() {
                return authService.loginAndGetUser(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            loginButton.setDisable(false);
            registerButton.setDisable(false);

            User user = task.getValue();

            if (user == null) {
                statusLabel.setText("Hibás felhasználónév vagy jelszó");
                return;
            }

            statusLabel.setText("Sikeres bejelentkezés!");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            try {
                // --- SZEREPKÖR SZERINTI ÁGAK ---
                if (user.isAdmin()) {
                    // Teljes jogú admin
                    openAdminPage(stage, user);

                } else if (user.isController()) {
                    // Irányító – saját controller dashboard
                    openControllerHomePage(stage, user);

                } else {
                    // Alap USER
                    openUserHomePage(stage, user);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Navigációs hiba: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            loginButton.setDisable(false);
            registerButton.setDisable(false);

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