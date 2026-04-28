package GUI.controllers;

import Resources.Heart.HeartMonitorState;
import Resources.Heart.HeartRateMonitor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class HealthController {

    private HeartRateMonitor monitor;

    @FXML
    private Label healthStatusLabel;

    @FXML
    private Label rateLabel;

    @FXML
    private Label noPulseLabel;

    @FXML
    private TextArea healthDetailsArea;

    @FXML
    private Button startMonitorButton;

    @FXML
    private Button readPulseButton;

    @FXML
    private Button confirmSafeButton;

    @FXML
    private Button stopMonitorButton;

    public void setHeartMonitor(HeartRateMonitor monitor) {
        this.monitor = monitor;
        renderState(monitor.getCurrentState());
    }

    @FXML
    public void initialize() {
        healthStatusLabel.setText("Monitor detenido.");
        rateLabel.setText("Ultima lectura: --");
        noPulseLabel.setText("Lecturas sin pulso consecutivas: 0");
        healthDetailsArea.setText("Inicia el monitor y genera lecturas simuladas para comprobar el flujo de salud.");
        updateButtons(false, false);
    }

    @FXML
    private void handleStartMonitoring() {
        if (monitor == null) {
            healthStatusLabel.setText("El monitor de salud no esta disponible.");
            return;
        }

        monitor.startMonitoring();
        renderState(monitor.getCurrentState());
    }

    @FXML
    private void handleReadPulse() {
        if (monitor == null) {
            return;
        }

        renderState(monitor.readNextPulse());
    }

    @FXML
    private void handleConfirmSafe() {
        if (monitor == null) {
            return;
        }

        renderState(monitor.confirmUserIsSafe());
    }

    @FXML
    private void handleStopMonitoring() {
        if (monitor == null) {
            return;
        }

        renderState(monitor.stopMonitoring());
    }

    private void renderState(HeartMonitorState state) {
        healthStatusLabel.setText(state.getStatusMessage());
        rateLabel.setText(state.getHeartRate() > 0 ? "Ultima lectura: " + state.getHeartRate() + " bpm" : "Ultima lectura: sin pulso");
        noPulseLabel.setText("Lecturas sin pulso consecutivas: " + state.getNoPulseCounter());

        StringBuilder detail = new StringBuilder();
        detail.append("Monitor activo: ").append(state.isMonitoring() ? "Si" : "No").append(System.lineSeparator());
        detail.append("Prealerta activa: ").append(state.isPreAlertActive() ? "Si" : "No").append(System.lineSeparator());
        detail.append("Confirmacion del usuario: ").append(state.isUserConfirmed() ? "Si" : "No").append(System.lineSeparator());
        detail.append("Estado: ").append(state.getStatusMessage());
        healthDetailsArea.setText(detail.toString());

        updateButtons(state.isMonitoring(), state.isPreAlertActive());
    }

    private void updateButtons(boolean monitoring, boolean preAlert) {
        startMonitorButton.setDisable(monitoring || preAlert);
        readPulseButton.setDisable(!monitoring);
        confirmSafeButton.setDisable(!preAlert);
        stopMonitorButton.setDisable(!monitoring && !preAlert);
    }
}
