package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserRole;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class UserEditDialogController {

    private final UserService userService;
    private final AirportsService airportsService;

    public UserEditDialogController(UserService userService,
                                    AirportsService airportsService) {
        this.userService = userService;
        this.airportsService = airportsService;
    }

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private ComboBox<Airports> airportComboBox;
    @FXML private Label airportLabel;

    private User user;
    private String originalUsername;

    @Getter
    private boolean edited = false;

    private final ObservableList<Airports> airportOptions = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Szerepkör lista
        if (roleComboBox != null) {
            roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
            roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateAirportVisibility(newVal);
            });
        }

        // Repterek betöltése
        if (airportComboBox != null) {
            airportOptions.setAll(airportsService.getAllAirports());
            airportComboBox.setItems(airportOptions);

            // Szebb megjelenítés
            airportComboBox.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Airports item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String icao = item.getIcaoCode() != null ? item.getIcaoCode() : "";
                        String name = item.getName() != null ? item.getName() : "";
                        setText(icao + " – " + name);
                    }
                }
            });
            airportComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Airports item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String icao = item.getIcaoCode() != null ? item.getIcaoCode() : "";
                        String name = item.getName() != null ? item.getName() : "";
                        setText(icao + " – " + name);
                    }
                }
            });
        }

        updateAirportVisibility(roleComboBox != null ? roleComboBox.getValue() : null);
    }

    private void updateAirportVisibility(UserRole role) {
        boolean controller = (role == UserRole.CONTROLLER);

        if (airportLabel != null) {
            airportLabel.setVisible(controller);
            airportLabel.setManaged(controller);
        }
        if (airportComboBox != null) {
            airportComboBox.setVisible(controller);
            airportComboBox.setManaged(controller);
        }
    }

    public void setUser(User user) {
        this.user = user;

        if (user.getId() != 0) {
            idField.setText(String.valueOf(user.getId()));
            nameField.setText(user.getName());
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());

            UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;
            if (roleComboBox != null) {
                roleComboBox.setValue(role);
            }

            // assignedAirport beállítása: ID alapján megkeressük a listában
            if (airportComboBox != null && user.getAssignedAirport() != null) {
                long assignedId = user.getAssignedAirport().getId();
                Airports match = airportOptions.stream()
                        .filter(a -> a.getId() == assignedId)
                        .findFirst()
                        .orElse(null);
                airportComboBox.setValue(match);
            }

            passwordField.clear();
            originalUsername = user.getUsername();

        } else {
            idField.clear();
            nameField.clear();
            usernameField.clear();
            emailField.clear();
            phoneField.clear();
            passwordField.clear();

            if (roleComboBox != null) {
                roleComboBox.setValue(UserRole.USER);
            }
            if (airportComboBox != null) {
                airportComboBox.setValue(null);
            }

            originalUsername = null;
        }

        if (roleComboBox != null) {
            updateAirportVisibility(roleComboBox.getValue());
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

        user.setName(name);
        user.setUsername(username);
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());

        UserRole selectedRole =
                (roleComboBox != null && roleComboBox.getValue() != null)
                        ? roleComboBox.getValue()
                        : UserRole.USER;
        user.setRole(selectedRole);

        if (selectedRole == UserRole.CONTROLLER) {
            Airports selectedAirport =
                    (airportComboBox != null ? airportComboBox.getValue() : null);

            if (selectedAirport == null) {
                errorLabel.setText("Controller szerepkörhöz ki kell választani egy repülőteret.");
                return;
            }
            user.setAssignedAirport(selectedAirport);
        } else {
            user.setAssignedAirport(null);
        }

        String rawPassword = passwordField.getText();
        String tempPasswordForInfo = null;

        if (user.getId() == 0) {
            if (rawPassword == null || rawPassword.isBlank()) {
                tempPasswordForInfo = userService.generateTempPassword();
                rawPassword = tempPasswordForInfo;
            }
        } else {
            if (rawPassword == null || rawPassword.isBlank()) {
                rawPassword = null;
            }
        }

        try {
            userService.saveFromAdmin(user, rawPassword);
        } catch (Exception ex) {
            errorLabel.setText("Mentési hiba: " + ex.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Mentési hiba");
            alert.setHeaderText("Nem sikerült menteni a felhasználót.");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            return;
        }

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