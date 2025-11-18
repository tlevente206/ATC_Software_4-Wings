package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class UserEditDialogController {

    private final UserService userService;

    public UserEditDialogController(UserService userService) {
        this.userService = userService;
    }

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private CheckBox adminCheckBox;
    @FXML private Label errorLabel;
    @FXML private PasswordField passwordField;

    private User user;

    // Eredeti username – hogy tudjuk, változott-e
    private String originalUsername;

    @Getter
    private boolean edited = false;

    public void setUser(User user) {
        this.user = user;

        if (user.getId() != 0) {
            // meglévő user → előtöltés
            idField.setText(String.valueOf(user.getId()));
            nameField.setText(user.getName());
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
            adminCheckBox.setSelected(user.isAdmin());

            passwordField.clear();

            originalUsername = user.getUsername();
        } else {
            // új user → üres mezők
            idField.clear();
            nameField.clear();
            usernameField.clear();
            emailField.clear();
            phoneField.clear();
            adminCheckBox.setSelected(false);
            passwordField.clear();

            originalUsername = null;
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (user == null) {
            errorLabel.setText("Nincs kiválasztott felhasználó.");
            return;
        }

        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();

        if (name.isEmpty() || username.isEmpty()) {
            errorLabel.setText("Név és felhasználónév kötelező.");
            return;
        }

        // --- FELHASZNÁLÓNÉV ÜTKÖZÉS ELLENŐRZÉSE ---
        boolean isNewUser = (user.getId() == 0);
        boolean usernameChanged =
                isNewUser ||
                        (originalUsername != null && !originalUsername.equalsIgnoreCase(username));

        if (usernameChanged && !userService.isUsernameAvailable(username)) {
            String msg = "Már létezik ilyen felhasználónév: " + username;
            errorLabel.setText(msg);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Felhasználónév foglalt");
            alert.setHeaderText("A megadott felhasználónév már létezik.");
            alert.setContentText(msg);
            alert.showAndWait();

            return;
        }

        // --- ADATOK VISSZAÍRÁSA ---
        user.setName(name);
        user.setUsername(username);
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());
        user.setAdmin(adminCheckBox.isSelected());

        String rawPassword = passwordField.getText();
        String tempPasswordForInfo = null;

        // ÚJ USER
        if (user.getId() == 0) {
            if (rawPassword == null || rawPassword.isBlank()) {
                tempPasswordForInfo = userService.generateTempPassword();
                rawPassword = tempPasswordForInfo;
            }
        } else {
            // MEGLÉVŐ USER
            if (rawPassword == null || rawPassword.isBlank()) {
                rawPassword = null; // nem változtatjuk a jelszót
            }
        }

        try {
            userService.saveFromAdmin(user, rawPassword);
        } catch (Exception ex) {
            // ha mégis valami DB-hiba, azt is popupban jelezzük
            errorLabel.setText("Mentési hiba: " + ex.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Mentési hiba");
            alert.setHeaderText("Nem sikerült menteni a felhasználót.");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            return;
        }

        // Ideiglenes jelszó infó
        if (tempPasswordForInfo != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ideiglenes jelszó");
            alert.setHeaderText("Új felhasználó ideiglenes jelszava");
            alert.setContentText(
                    "Felhasználónév: " + user.getUsername() + "\n" +
                            "Ideiglenes jelszó: " + tempPasswordForInfo + "\n\n" +
                            "Add át a felhasználónak, és kérd meg, hogy az első belépés után változtassa meg."
            );
            alert.showAndWait();
        }

        edited = true;
        closeWindow();
    }

    @FXML
    private void onCancel(ActionEvent event) {
        edited = false;
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) idField.getScene().getWindow();
        stage.close();
    }
}