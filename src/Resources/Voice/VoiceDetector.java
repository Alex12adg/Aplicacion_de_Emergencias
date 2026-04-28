package Resources.Voice;

import Resources.Emergency.EmergencyManager;

public class VoiceDetector {

    private final VoiceConfig config;
    private boolean listening = false;
    private String lastInput = "";

    public VoiceDetector(VoiceConfig config) {
        this.config = config;
    }

    public void startListening(EmergencyManager manager) {
        listening = true;
    }

    public VoiceDetectionState startListeningState() {
        listening = true;
        return new VoiceDetectionState(true, config.getKeyword(), lastInput, false, "Escucha activada. Introduce una frase para simular la deteccion.");
    }

    public VoiceDetectionState processInput(String input, EmergencyManager manager) {
        if (!listening) {
            return new VoiceDetectionState(false, config.getKeyword(), lastInput, false, "Activa antes el modo escucha.");
        }

        lastInput = input == null ? "" : input.trim().toLowerCase();
        boolean detected = !lastInput.isBlank() && lastInput.contains(config.getKeyword());

        if (detected) {
            manager.triggerVoiceEmergency();
            return new VoiceDetectionState(true, config.getKeyword(), lastInput, true, "Palabra clave detectada. Se ha lanzado la emergencia por voz.");
        }

        return new VoiceDetectionState(true, config.getKeyword(), lastInput, false, "No se detecto la palabra clave en la frase simulada.");
    }

    public VoiceDetectionState updateKeyword(String keyword) {
        config.setKeyword(keyword);
        return new VoiceDetectionState(listening, config.getKeyword(), lastInput, false, "Palabra clave actualizada.");
    }

    public VoiceDetectionState stopState() {
        listening = false;
        return new VoiceDetectionState(false, config.getKeyword(), lastInput, false, "Escucha detenida.");
    }

    public VoiceDetectionState getCurrentState() {
        return new VoiceDetectionState(
                listening,
                config.getKeyword(),
                lastInput,
                false,
                listening ? "Escucha activa." : "Escucha detenida."
        );
    }

    public void stop() {
        listening = false;
    }
}
