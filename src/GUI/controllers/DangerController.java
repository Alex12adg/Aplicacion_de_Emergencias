package GUI.controllers;

import Resources.Danger.DangerAlertState;
import Resources.Danger.DangerAlertSystem;
import Resources.Emergency.EmergencyManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class DangerController {

    private DangerAlertSystem system;
    private EmergencyManager manager;
    private DangerAlertModalHandler modalHandler;

    @FXML
    private Label dangerStatusLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private TextArea detailArea;

    @FXML
    private Button activateButton;

    public void setDangerSystem(DangerAlertSystem system, EmergencyManager manager) {
        this.system = system;
        this.manager = manager;
        renderState(system.getCurrentState());
    }

    public void setModalHandler(DangerAlertModalHandler modalHandler) {
        this.modalHandler = modalHandler;
    }

    @FXML
    public void initialize() {
        detailArea.setText("Activa una alerta de peligro para bloquear la aplicacion y mostrar una alarma modal en el centro de la escena.");
        locationLabel.setText("Sin ubicacion activa");
        dangerStatusLabel.setText("Sin alerta activa.");
    }

    @FXML
    private void handleDangerAlert() {
        if (system == null) {
            dangerStatusLabel.setText("El sistema de peligro no esta disponible.");
            return;
        }

        if (system.isAlertActive()) {
            dangerStatusLabel.setText("Ya hay una alerta activa bloqueando la aplicacion.");
            return;
        }

        DangerAlertState state = system.startAlert();
        renderState(state);

        if (modalHandler != null) {
            modalHandler.onDangerAlertStarted(state);
        }
    }

    private void renderState(DangerAlertState state) {
        dangerStatusLabel.setText(state.getStatusMessage());
        locationLabel.setText(state.getLocation() == null || state.getLocation().isBlank()
                ? "Sin ubicacion activa"
                : state.getLocation());

        StringBuilder detail = new StringBuilder();
        detail.append("Alerta activa: ").append(state.isActive() ? "Si" : "No").append(System.lineSeparator());
        detail.append("Emergencia enviada: ").append(state.isEmergencySent() ? "Si" : "No").append(System.lineSeparator());
        detail.append("Ubicacion: ").append(locationLabel.getText()).append(System.lineSeparator());
        detail.append("Estado: ").append(state.getStatusMessage());
        detailArea.setText(detail.toString());
        activateButton.setDisable(state.isActive());
    }
}
