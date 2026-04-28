package GUI.controllers;

import Resources.Emergency.EmergencyManager;
import Resources.Voice.VoiceDetectionState;
import Resources.Voice.VoiceDetector;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class VoiceController {

    private VoiceDetector detector;
    private EmergencyManager manager;

    @FXML
    private TextField keywordField;

    @FXML
    private TextField phraseField;

    @FXML
    private Label voiceStatusLabel;

    @FXML
    private Label keywordLabel;

    @FXML
    private Label lastInputLabel;

    @FXML
    private TextArea voiceDetailsArea;

    @FXML
    private Button startVoiceButton;

    @FXML
    private Button simulateVoiceButton;

    @FXML
    private Button stopVoiceButton;

    public void setVoiceDetector(VoiceDetector detector, EmergencyManager manager) {
        this.detector = detector;
        this.manager = manager;
        VoiceDetectionState state = detector.getCurrentState();
        keywordField.setText(state.getKeyword());
        renderState(state);
    }

    @FXML
    public void initialize() {
        voiceStatusLabel.setText("Escucha detenida.");
        keywordLabel.setText("Palabra clave: --");
        lastInputLabel.setText("Ultima frase: --");
        voiceDetailsArea.setText("Activa la escucha y simula frases para comprobar la deteccion por voz.");
        updateButtons(false);
    }

    @FXML
    private void handleStartVoice() {
        if (detector == null) {
            voiceStatusLabel.setText("El sistema de voz no esta disponible.");
            return;
        }

        VoiceDetectionState updatedKeywordState = detector.updateKeyword(keywordField.getText());
        renderState(updatedKeywordState);
        renderState(detector.startListeningState());
    }

    @FXML
    private void handleSimulateVoice() {
        if (detector == null || manager == null) {
            voiceStatusLabel.setText("No se puede procesar la entrada de voz.");
            return;
        }

        renderState(detector.processInput(phraseField.getText(), manager));
    }

    @FXML
    private void handleStopVoice() {
        if (detector == null) {
            return;
        }

        renderState(detector.stopState());
    }

    private void renderState(VoiceDetectionState state) {
        voiceStatusLabel.setText(state.getStatusMessage());
        keywordLabel.setText("Palabra clave: " + state.getKeyword());
        lastInputLabel.setText(state.getLastInput() == null || state.getLastInput().isBlank()
                ? "Ultima frase: --"
                : "Ultima frase: " + state.getLastInput());
        voiceDetailsArea.setText(
                "Escucha activa: " + (state.isListening() ? "Si" : "No") + System.lineSeparator()
                        + "Deteccion positiva: " + (state.isDetected() ? "Si" : "No") + System.lineSeparator()
                        + "Estado: " + state.getStatusMessage()
        );
        updateButtons(state.isListening());
    }

    private void updateButtons(boolean listening) {
        startVoiceButton.setDisable(listening);
        simulateVoiceButton.setDisable(!listening);
        stopVoiceButton.setDisable(!listening);
    }
}
