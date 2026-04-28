package Resources.Danger;

public class DangerAlertState {

    private final boolean active;
    private final boolean emergencySent;
    private final String location;
    private final int attemptsRemaining;
    private final String statusMessage;

    public DangerAlertState(boolean active, boolean emergencySent, String location, int attemptsRemaining, String statusMessage) {
        this.active = active;
        this.emergencySent = emergencySent;
        this.location = location;
        this.attemptsRemaining = attemptsRemaining;
        this.statusMessage = statusMessage;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEmergencySent() {
        return emergencySent;
    }

    public String getLocation() {
        return location;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
