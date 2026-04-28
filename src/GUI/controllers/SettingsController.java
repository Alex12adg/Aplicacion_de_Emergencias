package GUI.controllers;

import GUI.AppLayout;
import Resources.Session.UserSession;
import Resources.User.UserData;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.Consumer;

public class SettingsController {

    @FXML
    private Label accountNameLabel;

    @FXML
    private Label accountEmailLabel;

    @FXML
    private TextField newNameField;

    @FXML
    private TextField newEmailField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private PasswordField deletePasswordField;

    @FXML
    private Label settingsStatusLabel;

    private final UserService userService;
    private Consumer<String> profileNameUpdateHandler;

    public SettingsController() {
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        refreshAccountSummary();
        settingsStatusLabel.setText("Verifica tu contrasena actual para editar el perfil o eliminar la cuenta.");
    }

    public void setProfileNameUpdateHandler(Consumer<String> profileNameUpdateHandler) {
        this.profileNameUpdateHandler = profileNameUpdateHandler;
    }

    @FXML
    public void handleUpdateAccount() {
        try {
            UserData currentUser = requireUser();
            UserData updatedUser = userService.updateAccount(
                    currentUser,
                    confirmPasswordField.getText(),
                    newNameField.getText(),
                    newEmailField.getText(),
                    newPasswordField.getText()
            );

            UserSession.setUser(updatedUser);
            refreshAccountSummary();
            clearUpdateInputs();
            notifyProfileNameUpdated(updatedUser.getNombre());
            settingsStatusLabel.setText("Cuenta actualizada correctamente");
        } catch (Exception e) {
            settingsStatusLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleDeleteAccount() {
        try {
            UserData currentUser = requireUser();

            if (deletePasswordField.getText() == null || deletePasswordField.getText().isBlank()) {
                throw new Exception("Debes introducir la contrasena actual para eliminar la cuenta");
            }

            Alert confirmationAlert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Esta accion eliminara tu cuenta y los datos asociados. Quieres continuar?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            confirmationAlert.setTitle("Eliminar cuenta");
            confirmationAlert.setHeaderText("Confirmacion requerida");
            confirmationAlert.initOwner(settingsStatusLabel.getScene().getWindow());

            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isEmpty() || result.get() != ButtonType.YES) {
                settingsStatusLabel.setText("El borrado de la cuenta se ha cancelado");
                return;
            }

            userService.deleteAccount(currentUser, deletePasswordField.getText());
            UserSession.clear();
            openLoginScene();
        } catch (Exception e) {
            settingsStatusLabel.setText(e.getMessage());
        }
    }

    private void refreshAccountSummary() {
        UserData user = UserSession.getUser();

        if (user == null) {
            accountNameLabel.setText("No hay usuario en sesion");
            accountEmailLabel.setText("No disponible");
            return;
        }

        accountNameLabel.setText(user.getNombre());
        accountEmailLabel.setText(user.getEmail());
    }

    private UserData requireUser() throws Exception {
        UserData user = UserSession.getUser();

        if (user == null || user.getId() <= 0) {
            throw new Exception("No hay usuario en sesion");
        }

        return user;
    }

    private void notifyProfileNameUpdated(String updatedName) {
        if (profileNameUpdateHandler != null) {
            profileNameUpdateHandler.accept(updatedName);
        }
    }

    private void clearUpdateInputs() {
        newNameField.clear();
        newEmailField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void openLoginScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Views/Login-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) settingsStatusLabel.getScene().getWindow();
        stage.setScene(new Scene(root, AppLayout.MOBILE_WIDTH, AppLayout.MOBILE_HEIGHT));
        stage.show();
    }
}
