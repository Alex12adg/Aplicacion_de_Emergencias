package Resources.Heart;

public class HeartMonitorState {

    private final boolean monitoring;
    private final int heartRate;
    private final int noPulseCounter;
    private final boolean preAlertActive;
    private final boolean userConfirmed;
    private final String statusMessage;

    public HeartMonitorState(
            boolean monitoring,
            int heartRate,
            int noPulseCounter,
            boolean preAlertActive,
            boolean userConfirmed,
            String statusMessage
    ) {
        this.monitoring = monitoring;
        this.heartRate = heartRate;
        this.noPulseCounter = noPulseCounter;
        this.preAlertActive = preAlertActive;
        this.userConfirmed = userConfirmed;
        this.statusMessage = statusMessage;
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public int getNoPulseCounter() {
        return noPulseCounter;
    }

    public boolean isPreAlertActive() {
        return preAlertActive;
    }

    public boolean isUserConfirmed() {
        return userConfirmed;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
