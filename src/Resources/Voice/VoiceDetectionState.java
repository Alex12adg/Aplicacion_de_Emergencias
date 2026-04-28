package Resources.Voice;

public class VoiceDetectionState {

    private final boolean listening;
    private final String keyword;
    private final String lastInput;
    private final boolean detected;
    private final String statusMessage;

    public VoiceDetectionState(boolean listening, String keyword, String lastInput, boolean detected, String statusMessage) {
        this.listening = listening;
        this.keyword = keyword;
        this.lastInput = lastInput;
        this.detected = detected;
        this.statusMessage = statusMessage;
    }

    public boolean isListening() {
        return listening;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getLastInput() {
        return lastInput;
    }

    public boolean isDetected() {
        return detected;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
