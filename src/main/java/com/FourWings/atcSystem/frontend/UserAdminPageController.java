package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class UserAdminPageController {

    private final UserService userService;

    public UserAdminPageController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private TableView<User> usersTable;   // <-- FXML-ben fx:id="usersTable"

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

    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Oszlop bindingok
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        adminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));

        // Adatok betöltése
        users.setAll(userService.getAllUsers());
        usersTable.setItems(users);

        System.out.println("Betöltött userek száma: " + users.size());

        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    User selected = row.getItem();
                    openUserEditDialog(selected);
                }
            });
            return row;
        });
    }

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

            // mentés után frissítjük a táblát
            users.setAll(userService.getAllUsers());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onEditSelectedUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            // opcionálisan statusLabel.setText("Nincs kijelölve felhasználó!");
            return;
        }
        openUserEditDialog(selected);
    }

    @FXML
    private void onAddNewUser() {
        User newUser = new User();
        newUser.setAdmin(false);
        newUser.setPassword(null); // a default jelszó majd admin mentésnél kerül hozzá

        openUserEditDialog(newUser);
    }
}