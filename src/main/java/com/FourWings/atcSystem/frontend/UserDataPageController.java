package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.airline.Airline;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.gate.Gate;
import com.FourWings.atcSystem.model.terminal.Terminal;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class UserDataPageController {

    private final UserService userService;
    public static final int WIDTH = 1200; //Window szélesség
    public static final int HEIGHT = 600; //Window magasság

    public UserDataPageController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private Label emailLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private ImageView profileImageView;

    private User loggedUser;
    private Airports airports;
    private Aircraft aircraft;
    private Airline airline;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", referencedColumnName = "terminal_id")
    private Terminal terminal;

    private Gate gate;
    private Flight flight;

    public void initWithUser(User user) {
        this.loggedUser = user;

        if (user != null) {
            nameLabel.setText(user.getName());
            phoneLabel.setText(user.getPhone());
            emailLabel.setText(user.getEmail());
            refreshProfileImage();
        } else {
            System.out.println("initWithUser() null user-rel hívva");
        }
    }

    /**
     * A felhasználó profilképének frissítése.
     * A User.profileImagePath mezőben most már csak egy relatív resource útvonal van,
     * pl: "/images/avatars/avatar3.png"
     */
    private void refreshProfileImage() {
        if (profileImageView == null) return;
        if (loggedUser == null) return;

        String path = loggedUser.getProfileImagePath();   // <-- elérési út a User-ből
        if (path != null && !path.isBlank()) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    Image img = new Image(is);
                    profileImageView.setImage(img);
                } else {
                    System.out.println("Nem található profilkép resource: " + path);
                    profileImageView.setImage(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                profileImageView.setImage(null);
            }
        } else {
            profileImageView.setImage(null); // vagy később default kép
        }
    }

    public void setLastAirport(Airports airports) {
        this.airports = airports;
        System.out.println(airports.toString());
    }

    public void setLastAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
        System.out.println(aircraft.toString());
    }

    public void setLastAirline(Airline airline) {
        this.airline = airline;
        System.out.println(airline.toString());
    }

    public void setLastFlight(Flight flight) {
        this.flight = flight;
        System.out.println("Flight id=" + flight.getId());
    }

    public void setLastGate(Gate gate) {
        this.gate = gate;
        System.out.println("Gate id=" + gate.getId() + ", code=" + gate.getCode());
    }

    public void setLastTerminal(Terminal terminal) {
        this.terminal = terminal;
        System.out.println("Terminal id=" + terminal.getId());
    }

    // ======= PROFILKÉP MÓDOSÍTÁSA – UGYANAZ A POPUP, MINT REGISZTRÁCIÓ =======
    @FXML
    private void onChangeProfilePicture(ActionEvent event) {
        if (loggedUser == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Hiba");
            a.setHeaderText("Nincs bejelentkezett felhasználó");
            a.setContentText("Profilkép módosításához be kell jelentkezni.");
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/LogIn/AvatarPickerDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            AvatarPickerDialogController ctrl = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.initOwner(nameLabel.getScene().getWindow());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Profilkép módosítása");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // az AvatarPickerDialogController most már String elérési utat ad vissza
            String chosenPath = ctrl.getSelectedImagePath();
            if (chosenPath != null && !chosenPath.isBlank()) {
                // új kép elérési útjának beállítása, mentés DB-be
                loggedUser.setProfileImagePath(chosenPath);
                userService.saveFromAdmin(loggedUser, null); // jelszó változatlan

                // előnézet frissítése
                refreshProfileImage();

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Profilkép frissítve");
                ok.setHeaderText(null);
                ok.setContentText("A profilképed sikeresen frissült.");
                ok.showAndWait();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Hiba");
            err.setHeaderText("Nem sikerült megnyitni az avatar választót.");
            err.setContentText(ex.getMessage());
            err.showAndWait();
        }
    }

    @FXML
    public void openProfileEditor(ActionEvent event) {
        if (loggedUser == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hiba");
            alert.setHeaderText("Nincs betöltve felhasználó");
            alert.setContentText("A profil szerkesztéséhez előbb be kell jelentkezni.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/UserSelfEditDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            UserSelfEditDialogController ctrl = loader.getController();
            ctrl.setUser(loggedUser);

            Stage dialogStage = new Stage();
            dialogStage.initOwner(nameLabel.getScene().getWindow());
            dialogStage.setTitle("Profil szerkesztése");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // visszatérés után frissítjük a label-eket
            if (loggedUser != null) {
                nameLabel.setText(loggedUser.getName());
                phoneLabel.setText(loggedUser.getPhone());
                emailLabel.setText(loggedUser.getEmail());
                refreshProfileImage();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void toHome(ActionEvent event) {
        SceneManager.switchTo("HomePage.fxml", "ATC – Dashboard", WIDTH, HEIGHT);
    }
}