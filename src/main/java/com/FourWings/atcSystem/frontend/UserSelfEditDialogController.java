package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class UserSelfEditDialogController {

    private final UserService userService;

    public UserSelfEditDialogController(UserService userService) {
        this.userService = userService;
    }

    // --- Alapadat mezők ---
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel;

    private User user;

    @Getter
    private boolean edited = false;

    public void setUser(User user) {
        this.user = user;

        if (user != null) {
            nameField.setText(user.getName());
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
        } else {
            if (errorLabel != null) {
                errorLabel.setText("Nincs betöltött felhasználó.");
            }
        }
    }

    // === ALAPADATOK MENTÉSE (név, email, telefon) ===
    @FXML
    private void onSave(ActionEvent event) {
        if (user == null) {
            errorLabel.setText("Nincs felhasználó.");
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            errorLabel.setText("A név megadása kötelező.");
            return;
        }

        user.setName(name);
        // username-et normál esetben nem engedném itt módosítani,
        // de ha neked így kell, maradhat:
        user.setUsername(usernameField.getText().trim());
        user.setEmail(email);
        user.setPhone(phone);

        try {
            // Itt csak az alapadatokat mentjük, jelszó NEM változik → rawPasswordOrNull = null
            userService.saveFromAdmin(user, null);
            edited = true;
            closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Mentési hiba: " + ex.getMessage());
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        edited = false;
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    // === JELSZÓ MÓDOSÍTÁSA POPUP DIALÓGUSSAL ===
    @FXML
    private void onChangePassword(ActionEvent event) {
        if (user == null) {
            if (errorLabel != null) {
                errorLabel.setText("Nincs betöltött felhasználó a jelszó módosításához.");
            }
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ChangePasswordDialog.fxml"));
            loader.setControllerFactory(com.FourWings.atcSystem.config.SpringContext::getBean);
            Parent root = loader.load();

            ChangePasswordDialogController ctrl = loader.getController();
            ctrl.setUser(user);

            Stage dialogStage = new Stage();
            dialogStage.initOwner(nameField.getScene().getWindow());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Jelszó módosítása");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            if (errorLabel != null) {
                errorLabel.setText("Nem sikerült megnyitni a jelszó-módosítót: " + ex.getMessage());
            }
        }
    }
}