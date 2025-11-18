package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

    // az összes user, amit a DB-ből hozunk
    private final ObservableList<User> users = FXCollections.observableArrayList();
    // erre szűrünk kereséskor
    private FilteredList<User> filteredUsers;

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

        // adatok betöltése + FilteredList létrehozása
        users.setAll(userService.getAllUsers());
        filteredUsers = new FilteredList<>(users, u -> true);   // kezdetben mindent mutat
        usersTable.setItems(filteredUsers);

        if (statusLabel != null) {
            statusLabel.setText("Felhasználók betöltve: " + users.size());
        }

        // KERESÉS: ahogy gépelsz, úgy szűr
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();

                filteredUsers.setPredicate(user -> {
                    if (filter.isEmpty()) {
                        // üres kereső → minden elem látszik
                        return true;
                    }
                    String name = user.getName() != null ? user.getName().toLowerCase() : "";
                    String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                    String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";

                    return name.contains(filter)
                            || username.contains(filter)
                            || email.contains(filter);
                });

                if (statusLabel != null) {
                    statusLabel.setText("Találatok: " + filteredUsers.size() + " / Összes: " + users.size());
                }
            });
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

        // ENTER-rel is megnyitjuk a szerkesztőt
        usersTable.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    User selected = usersTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        openUserEditDialog(selected);
                    }
                    event.consume();
                }
            }
        });

        // kijelölt user nevét kiírjuk a státusz sorba
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && statusLabel != null) {
                statusLabel.setText("Kiválasztott felhasználó: " + newSel.getUsername());
            }
        });
    }

    // ---------------------------------------------------------
    // SEGÉDMETÓDUS: lista újratöltése
    // ---------------------------------------------------------

    private void reloadUsers() {
        users.setAll(userService.getAllUsers());

        // ha már van filter, tartsuk meg a mostani keresőt
        if (filteredUsers == null) {
            filteredUsers = new FilteredList<>(users, u -> true);
            usersTable.setItems(filteredUsers);
        } else {
            String currentFilter = (searchField != null && searchField.getText() != null)
                    ? searchField.getText().trim().toLowerCase()
                    : "";

            filteredUsers.setPredicate(user -> {
                if (currentFilter.isEmpty()) return true;
                String name = user.getName() != null ? user.getName().toLowerCase() : "";
                String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                return name.contains(currentFilter)
                        || username.contains(currentFilter)
                        || email.contains(currentFilter);
            });
        }

        if (statusLabel != null) {
            statusLabel.setText("Lista frissítve. Találatok: "
                    + filteredUsers.size() + " / Összes: " + users.size());
        }
    }

    // ---------------------------------------------------------
    // GOMBOK
    // ---------------------------------------------------------

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

    @FXML
    private void onAddNewUser() {
        User newUser = new User();
        newUser.setAdmin(false);
        openUserEditDialog(newUser);
    }

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

    @FXML
    private void onRefresh() {
        reloadUsers();
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

            // ha történt mentés, bent van az edited flag-ben
            if (ctrl.isEdited()) {
                reloadUsers();
                if (statusLabel != null) {
                    statusLabel.setText("Felhasználó frissítve: " + user.getUsername());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Hiba a szerkesztő megnyitásakor: " + ex.getMessage());
            }
        }
    }
    @FXML
    private void onBackToAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminPage.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("ATC – Admin Dashboard");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Nem sikerült visszalépni az admin felületre: " + ex.getMessage());
            }
        }
    }
}