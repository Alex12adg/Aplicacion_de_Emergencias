package GUI.controllers;

import Resources.Emergency.EmergencyCenter;
import Resources.Emergency.EmergencyManager;
import Resources.Emergency.EmergencyProcessResult;
import Resources.Emergency.EmergencyRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class EmergencyController {

    private EmergencyManager manager;
    private final ObservableList<String> centerItems = FXCollections.observableArrayList();

    @FXML
    private VBox manualLocationBox;

    @FXML
    private TextField emergencyTypeField;

    @FXML
    private TextField severityField;

    @FXML
    private CheckBox automaticLocationCheck;

    @FXML
    private TextField manualLocationField;

    @FXML
    private Button sendEmergencyButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private TextArea detailsArea;

    @FXML
    private ListView<String> centersList;

    public void setEmergencyManager(EmergencyManager manager) {
        this.manager = manager;
        loadCenters();
    }

    @FXML
    public void initialize() {
        centersList.setItems(centerItems);
        automaticLocationCheck.setSelected(true);
        updateLocationMode();
        statusLabel.setText("Completa los datos para activar una emergencia.");
        summaryLabel.setText("Aun no se ha enviado ninguna alerta.");
        detailsArea.clear();
    }

    @FXML
    private void handleStartEmergency() {
        if (manager == null) {
            statusLabel.setText("El sistema de emergencia no esta disponible.");
            return;
        }

        EmergencyRequest request;

        try {
            request = new EmergencyRequest(
                    emergencyTypeField.getText(),
                    automaticLocationCheck.isSelected(),
                    manualLocationField.getText(),
                    Integer.parseInt(severityField.getText().trim())
            );
        } catch (NumberFormatException e) {
            statusLabel.setText("La gravedad debe ser un numero entre 1 y 10.");
            return;
        }

        setLoadingState(true);
        statusLabel.setText("Procesando emergencia...");

        Thread thread = new Thread(() -> {
            EmergencyProcessResult result = manager.processEmergency(request);
            Platform.runLater(() -> applyResult(result));
        });

        thread.setName("emergency-ui-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void updateLocationMode() {
        boolean automatic = automaticLocationCheck.isSelected();
        manualLocationBox.setManaged(!automatic);
        manualLocationBox.setVisible(!automatic);

        if (automatic) {
            manualLocationField.clear();
        }
    }

    private void applyResult(EmergencyProcessResult result) {
        setLoadingState(false);
        statusLabel.setText(result.getStatusMessage());
        locationLabel.setText(result.getLocationUsed().isBlank() ? "Sin ubicacion procesada" : result.getLocationUsed());

        if (result.isSuccess() && result.getEvent() != null) {
            summaryLabel.setText("Alerta activa: " + result.getEvent().getTipoEmergencia());
            detailsArea.setText(buildDetails(result));
        } else {
            summaryLabel.setText("La alerta no pudo activarse.");
            detailsArea.setText("");
        }

        renderCenters(result);
    }

    private String buildDetails(EmergencyProcessResult result) {
        return "Tipo: " + result.getEvent().getTipoEmergencia() + System.lineSeparator()
                + "Gravedad: " + result.getEvent().getGravedad() + System.lineSeparator()
                + "Ubicacion: " + result.getLocationUsed() + System.lineSeparator()
                + "Resumen: " + result.getEvent();
    }

    private void renderCenters(EmergencyProcessResult result) {
        centerItems.clear();

        for (EmergencyCenter center : result.getCenters()) {
            centerItems.add(center.getType() + " | " + center.getName() + " | " + center.getLatitude() + ", " + center.getLongitude());
        }

        if (centerItems.isEmpty()) {
            centerItems.add("No hay centros cargados para mostrar.");
        }
    }

    private void loadCenters() {
        if (manager == null) {
            return;
        }

        EmergencyProcessResult preload = new EmergencyProcessResult(
                true,
                "",
                "",
                null,
                manager.loadCenters()
        );
        renderCenters(preload);
    }

    private void setLoadingState(boolean loading) {
        sendEmergencyButton.setDisable(loading);
        emergencyTypeField.setDisable(loading);
        severityField.setDisable(loading);
        automaticLocationCheck.setDisable(loading);
        manualLocationField.setDisable(loading || automaticLocationCheck.isSelected());
    }
}
