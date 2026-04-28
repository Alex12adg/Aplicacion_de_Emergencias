package GUI.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label welcomeTitleLabel;

    @FXML
    private Label welcomeSubtitleLabel;

    private Runnable openEmergencyAction;
    private Runnable openHealthAction;
    private Runnable openVoiceAction;
    private Runnable openDangerAction;
    private Runnable openReservationsAction;
    private Runnable openCentersAction;
    private Runnable openMedicalAction;
    private Runnable openSettingsAction;

    @FXML
    public void initialize() {
        welcomeTitleLabel.setText("Panel de asistencia");
        welcomeSubtitleLabel.setText("Accede rapidamente a emergencias, historial medico y reservas de atencion desde un mismo panel.");
    }

    public void setWelcomeName(String name) {
        String safeName = name == null || name.isBlank() ? "Usuario" : name;
        welcomeTitleLabel.setText("Hola, " + safeName);
    }

    public void setNavigationActions(
            Runnable openEmergencyAction,
            Runnable openHealthAction,
            Runnable openVoiceAction,
            Runnable openDangerAction,
            Runnable openReservationsAction,
            Runnable openCentersAction,
            Runnable openMedicalAction,
            Runnable openSettingsAction
    ) {
        this.openEmergencyAction = openEmergencyAction;
        this.openHealthAction = openHealthAction;
        this.openVoiceAction = openVoiceAction;
        this.openDangerAction = openDangerAction;
        this.openReservationsAction = openReservationsAction;
        this.openCentersAction = openCentersAction;
        this.openMedicalAction = openMedicalAction;
        this.openSettingsAction = openSettingsAction;
    }

    @FXML
    private void openEmergency() {
        runAction(openEmergencyAction);
    }

    @FXML
    private void openHealth() {
        runAction(openHealthAction);
    }

    @FXML
    private void openVoice() {
        runAction(openVoiceAction);
    }

    @FXML
    private void openDanger() {
        runAction(openDangerAction);
    }

    @FXML
    private void openReservations() {
        runAction(openReservationsAction);
    }

    @FXML
    private void openCenters() {
        runAction(openCentersAction);
    }

    @FXML
    private void openMedical() {
        runAction(openMedicalAction);
    }

    @FXML
    private void openSettings() {
        runAction(openSettingsAction);
    }

    private void runAction(Runnable action) {
        if (action != null) {
            action.run();
        }
    }
}
