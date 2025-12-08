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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MainPageController {

    private final AuthService authService;
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;

    public MainPageController(AuthService authService) {
        this.authService = authService;
    }

    @FXML private Label statusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label motivation;

    @FXML
    public void initialize() {
        loadRandomMotivation();
    }

    private void loadRandomMotivation() {
        try {
            // Fájl beolvasása a resources mappából
            InputStream is = getClass().getResourceAsStream("/random.txt");
            if (is == null) {
                motivation.setText("Sikeres napot!"); // Alapértelmezett, ha nincs fájl
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }

            if (!lines.isEmpty()) {
                Random random = new Random();
                String randomQuote = lines.get(random.nextInt(lines.size()));

                motivation.setText(randomQuote);
                motivation.setWrapText(true); // Hogy sortörjön, ha hosszú a szöveg
            }

        } catch (Exception e) {
            e.printStackTrace();
            motivation.setText("Légy a legjobb!"); // Hiba esetén fallback szöveg
        }
    }

    // ---------------------- NAVIGÁCIÓK ----------------------

    /** Sima USER főoldal */
    private void openUserHomePage(Stage stage, User user) throws Exception {
        // Ellenőrizd a csomagot és importot, mert az eredeti kódban csak HomePageController volt
        // De a switch to metódusnak Stringet adunk át, a return típusnak kell jónak lennie
        var ctrl = SceneManager.switchTo(
                "HomePage.fxml",
                "ATC – Dashboard",
                WIDTH, HEIGHT
        );
    }

    /** ADMIN dashboard */
    private void openAdminPage(Stage stage, User user) throws Exception {
        // Feltételezve, hogy létezik az AdminPageController és castolható
        var ctrl = SceneManager.switchTo(
                "AdminPage.fxml",
                "ATC – Admin Dashboard",
                600, 400
        );
    }

    /** CONTROLLER főoldal (irányító munkaállomás) */
    private void openControllerHomePage(Stage stage, User user) throws Exception {
        // Itt castolás szükséges lehet, attól függően, hogy a SceneManager mit ad vissza
        // Eredetileg: ControllerHomePageController ctrl = ...
        var ctrl = SceneManager.switchTo(
                "ControllerHomePage.fxml",
                "ATC – Irányító munkaállomás",
                1100, 700
        );
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
                if (user.isAdmin()) {
                    openAdminPage(stage, user);
                } else if (user.isController()) {
                    openControllerHomePage(stage, user);
                } else {
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