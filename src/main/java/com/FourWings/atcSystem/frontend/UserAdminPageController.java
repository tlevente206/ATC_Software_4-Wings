package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class UserAdminPageController {

    private final UserService userService;

    public UserAdminPageController(UserService userService) {
        this.userService = userService;
    }

    // --- FXML elemek ----
    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Long> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, Boolean> adminColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    private final ObservableList<User> users = FXCollections.observableArrayList();

    // ---------------------------------------------------------
    // INIT
    // ---------------------------------------------------------

    @FXML
    public void initialize() {
        // oszlopok bindelése
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        adminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));

        // adatok betöltése
        reloadUsers();

        if (statusLabel != null) {
            statusLabel.setText("Felhasználók betöltve: " + users.size());
        }

        // dupla kattintás sorra → szerkesztő
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()
                        && event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2) {

                    User selected = row.getItem();
                    openUserEditDialog(selected);
                }
            });
            return row;
        });

        // --- ÚJ: egyszeri kattintás → státusz frissítés ---
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && statusLabel != null) {
                statusLabel.setText("Kijelölt felhasználó: " + newSel.getUsername());
            }
        });

        usersTable.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    User selected = usersTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        openUserEditDialog(selected);
                    }
                }
            }
        });
    }

    // ---------------------------------------------------------
    // SEGÉDMETÓDUS: lista újratöltése
    // ---------------------------------------------------------

    private void reloadUsers() {
        users.setAll(userService.getAllUsers());
        usersTable.setItems(users);

        if (statusLabel != null) {
            statusLabel.setText("Betöltött felhasználók: " + users.size());
        }
    }

    // ---------------------------------------------------------
    // GOMBOK
    // ---------------------------------------------------------

    // Toolbar: "Kijelölt szerkesztése" gomb
    @FXML
    private void onEditSelectedUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (statusLabel != null) {
                statusLabel.setText("Nincs kijelölt felhasználó.");
            }
            return;
        }
        openUserEditDialog(selected);
    }

    // Toolbar: "Új felhasználó" gomb
    @FXML
    private void onAddNewUser() {
        User newUser = new User();
        newUser.setAdmin(false);  // alapértelmezetten ne legyen admin

        openUserEditDialog(newUser);
    }

    // Toolbar: "Kijelölt törlése" gomb
    @FXML
    private void onDeleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (statusLabel != null) {
                statusLabel.setText("Nincs kijelölt felhasználó törléshez.");
            }
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Törlés megerősítése");
        confirm.setHeaderText("Biztosan törlöd a felhasználót?");
        confirm.setContentText("Felhasználó: " + selected.getUsername());

        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                userService.deleteUserById(selected.getId());
                reloadUsers();
                if (statusLabel != null) {
                    statusLabel.setText("Felhasználó törölve: " + selected.getUsername());
                }
            }
        });
    }

    // Toolbar: "Lista frissítése" gomb
    @FXML
    private void onRefresh() {
        reloadUsers();
        if (statusLabel != null) {
            statusLabel.setText("Lista frissítve. Jelenlegi felhasználók: " + users.size());
        }
    }

    // ---------------------------------------------------------
    // Szerkesztő dialógus
    // ---------------------------------------------------------

    private void openUserEditDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserEditDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            UserEditDialogController ctrl = loader.getController();
            ctrl.setUser(user);

            Stage dialogStage = new Stage();
            dialogStage.initOwner(usersTable.getScene().getWindow());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Felhasználó szerkesztése");
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();

            // Frissítsd a táblát
            reloadUsers();

            // --- Itt jön az okosság ---
            if (ctrl.isEdited()) {
                if (statusLabel != null) {
                    statusLabel.setText("Felhasználó frissítve: " + user.getUsername());
                }
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("Nem történt módosítás.");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Hiba a szerkesztő megnyitásakor: " + ex.getMessage());
            }
        }
    }

    // ---------------------------------------------------------
    // Vissza az admin főoldalra
    // ---------------------------------------------------------
    @FXML
    private void onBackToAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminPage.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400)); // vagy 1400x900, ha úgy használod
            stage.setTitle("ATC – Admin Dashboard");
            stage.show();

            if (statusLabel != null) {
                statusLabel.setText("Visszaléptél az admin főoldalra.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Nem sikerült visszalépni az admin felületre: " + ex.getMessage());
            }
        }
    }
}