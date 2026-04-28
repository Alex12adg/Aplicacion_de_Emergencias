package Resources.Emergency;

public class EmergencyRequest {

    private final String emergencyType;
    private final boolean automaticLocation;
    private final String manualLocation;
    private final int severity;

    public EmergencyRequest(String emergencyType, boolean automaticLocation, String manualLocation, int severity) {
        this.emergencyType = emergencyType;
        this.automaticLocation = automaticLocation;
        this.manualLocation = manualLocation;
        this.severity = severity;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public boolean isAutomaticLocation() {
        return automaticLocation;
    }

    public String getManualLocation() {
        return manualLocation;
    }

    public int getSeverity() {
        return severity;
    }
}
