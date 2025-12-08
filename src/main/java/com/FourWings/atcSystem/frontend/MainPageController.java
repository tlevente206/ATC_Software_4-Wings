package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.service.AuthService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;

@Component
public class MainPageController {

    private final AuthService authService;
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;

    // --- Preferences kulcsok ---
    private static final String PREF_USERNAME = "atc_username";
    private static final String PREF_PASSWORD = "atc_password";
    private static final String PREF_REMEMBER = "atc_remember_me";

    // Ez jelenik meg a mezőben (8 db csillag/pötty), de NEM ez az igazi jelszó
    private static final String DUMMY_PASSWORD = "********";

    public MainPageController(AuthService authService) {
        this.authService = authService;
    }

    @FXML private Label statusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label motivation;
    @FXML private CheckBox rememberMeCheckBox;

    @FXML
    public void initialize() {
        loadRandomMotivation();
        loadSavedCredentials(); // Csak kitöltjük a mezőket
    }

    // --- 1. PRE-FILL (Csak a dummy szöveg megjelenítése) ---
    private void loadSavedCredentials() {
        Preferences prefs = Preferences.userNodeForPackage(MainPageController.class);
        boolean remember = prefs.getBoolean(PREF_REMEMBER, false);

        if (remember) {
            String savedUser = prefs.get(PREF_USERNAME, "");
            // Ellenőrizzük, hogy van-e mentett jelszó, de NEM írjuk ki a képernyőre az igazit
            String savedPassEncrypted = prefs.get(PREF_PASSWORD, "");

            if (!savedUser.isEmpty() && !savedPassEncrypted.isEmpty()) {
                usernameField.setText(savedUser);

                // ITT A TRÜKK: Csak a 8 csillagot írjuk be
                passwordField.setText(DUMMY_PASSWORD);

                rememberMeCheckBox.setSelected(true);
            }
        }
    }

    // --- 2. LOGIN LOGIKA (Itt döntjük el, melyik jelszót használjuk) ---
    @FXML
    private void onLogin(ActionEvent event) {
        String usernameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText();

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            statusLabel.setText("Add meg a felhasználónevet és jelszót.");
            return;
        }

        // --- A JELSZÓ KIVÁLASZTÁSA ---
        String passwordToUse;

        // Ha a mezőben a dummy szöveg van, akkor a mentett jelszót használjuk
        if (passwordInput.equals(DUMMY_PASSWORD)) {
            Preferences prefs = Preferences.userNodeForPackage(MainPageController.class);
            String savedPassEncrypted = prefs.get(PREF_PASSWORD, "");

            if (savedPassEncrypted.isEmpty()) {
                statusLabel.setText("Hiba: Nincs mentett jelszó, kérlek írd be újra.");
                return;
            }
            // Dekódoljuk a mentett jelszót a loginhez
            passwordToUse = new String(Base64.getDecoder().decode(savedPassEncrypted));
        } else {
            // Ha a felhasználó átírta a mezőt, akkor azt használjuk, amit beírt
            passwordToUse = passwordInput;
        }
        // -----------------------------

        setControlsDisabled(true);
        statusLabel.setText("Bejelentkezés folyamatban...");

        // Fontos: A Taskhoz egy final változót vagyunk kénytelenek átadni,
        // ezért létrehozunk egy végleges referenciát a kiválasztott jelszóra.
        final String finalPassword = passwordToUse;

        Task<User> task = new Task<>() {
            @Override
            protected User call() {
                // A kiválasztott jelszóval próbálunk belépni
                return authService.loginAndGetUser(usernameInput, finalPassword);
            }
        };

        task.setOnSucceeded(e -> {
            setControlsDisabled(false);
            User user = task.getValue();

            if (user == null) {
                // Ha sikertelen, töröljük a dummy szöveget, hogy a user lássa, baj van
                if (passwordField.getText().equals(DUMMY_PASSWORD)) {
                    passwordField.clear();
                }
                statusLabel.setText("Hibás felhasználónév vagy jelszó");
                return;
            }

            // Ha sikeres, és be van pipálva, elmentjük (az igazit, nem a dummy-t!)
            saveCredentials(usernameInput, finalPassword, rememberMeCheckBox.isSelected());

            statusLabel.setText("Sikeres bejelentkezés!");
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            navigateByUserRole(stage, user);
        });

        task.setOnFailed(e -> {
            setControlsDisabled(false);
            Throwable ex = task.getException();
            ex.printStackTrace();
            statusLabel.setText("Hiba: " + ex.getMessage());
        });

        new Thread(task).start();
    }

    private void saveCredentials(String username, String password, boolean remember) {
        Preferences prefs = Preferences.userNodeForPackage(MainPageController.class);
        if (remember) {
            prefs.put(PREF_USERNAME, username);
            String encodedPass = Base64.getEncoder().encodeToString(password.getBytes());
            prefs.put(PREF_PASSWORD, encodedPass);
            prefs.putBoolean(PREF_REMEMBER, true);
        } else {
            prefs.remove(PREF_USERNAME);
            prefs.remove(PREF_PASSWORD);
            prefs.remove(PREF_REMEMBER);
        }
    }

    // --- SETUP ÉS NAVIGÁCIÓ (Változatlan) ---

    private void setControlsDisabled(boolean disabled) {
        loginButton.setDisable(disabled);
        registerButton.setDisable(disabled);
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        rememberMeCheckBox.setDisable(disabled);
    }

    private void navigateByUserRole(Stage stage, User user) {
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
    }

    private void loadRandomMotivation() {
        try {
            InputStream is = getClass().getResourceAsStream("/random.txt");
            if (is == null) {
                if (motivation != null) motivation.setText("Sikeres napot!");
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
                if (motivation != null) {
                    motivation.setText(randomQuote);
                    motivation.setWrapText(true);
                }
            }
        } catch (Exception e) {
            if (motivation != null) motivation.setText("Légy a legjobb!");
        }
    }

    private void openUserHomePage(Stage stage, User user) throws Exception {
        var ctrl = SceneManager.switchTo("HomePage.fxml", "ATC – Dashboard", WIDTH, HEIGHT);
        // ctrl.initWithUser(user);
    }

    private void openAdminPage(Stage stage, User user) throws Exception {
        var ctrl = SceneManager.switchTo("AdminPage.fxml", "ATC – Admin Dashboard", 600, 400);
    }

    private void openControllerHomePage(Stage stage, User user) throws Exception {
        var ctrl = SceneManager.switchTo("ControllerHomePage.fxml", "ATC – Irányító munkaállomás", 1100, 700);
    }

    @FXML
    private void goToRegister(ActionEvent event) throws Exception {
        SceneManager.switchTo("RegistrationPage.fxml", "ATC – Regisztráció", 600, 400);
    }
}