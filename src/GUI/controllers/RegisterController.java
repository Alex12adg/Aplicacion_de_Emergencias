package GUI.controllers;

import GUI.AppLayout;
import Resources.Session.UserSession;
import Resources.User.UserData;
import Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final UserService userService;

    public RegisterController() {
        this.userService = new UserService();
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            UserData createdUser = userService.register(
                    nameField.getText(),
                    phoneField.getText(),
                    emailField.getText(),
                    passwordField.getText()
            );

            UserSession.setUser(createdUser);
            UserSession.setNewUser(true);
            statusLabel.setText("Usuario registrado correctamente");
            openScene(event, "/GUI/Views/Medical-form-view.fxml");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            openScene(event, "/GUI/Views/Login-view.fxml");
        } catch (Exception e) {
            statusLabel.setText("No se pudo abrir el login");
        }
    }

    private void openScene(ActionEvent event, String resourcePath) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, AppLayout.MOBILE_WIDTH, AppLayout.MOBILE_HEIGHT));
        stage.show();
    }
}
