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

public class LoginController {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final UserService userService;

    public LoginController() {
        this.userService = new UserService();
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            UserData user = userService.login(userField.getText(), passwordField.getText());
            UserSession.setUser(user);
            statusLabel.setText("Login correcto");
            openScene(event, "/GUI/Views/main-view.fxml");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void goToRegister(ActionEvent event) {
        try {
            openScene(event, "/GUI/Views/Register-view.fxml");
        } catch (Exception e) {
            statusLabel.setText("No se pudo abrir el registro");
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
