package Resources.Heart;

public class HeartRateMonitor {

    private final HeartRateSensorSimulator sensor;
    private final HeartRateAlert alert;
    private boolean monitoring = false;
    private int noPulseCounter = 0;
    private int lastHeartRate = 0;
    private boolean preAlertActive = false;

    public HeartRateMonitor() {
        this.sensor = new HeartRateSensorSimulator();
        this.alert = new HeartRateAlert();
    }

    public void startMonitoring() {
        monitoring = true;
        preAlertActive = false;
    }

    public HeartMonitorState readNextPulse() {
        if (!monitoring) {
            return new HeartMonitorState(false, lastHeartRate, noPulseCounter, preAlertActive, false, "El monitor no esta activo.");
        }

        int heartRate = sensor.readHeartRate();
        lastHeartRate = heartRate;

        if (heartRate == 0) {
            noPulseCounter++;
        } else {
            noPulseCounter = 0;
        }

        if (noPulseCounter >= 3) {
            preAlertActive = true;
            monitoring = false;
            return alert.createAlertState(false, lastHeartRate, noPulseCounter);
        }

        return new HeartMonitorState(
                true,
                heartRate,
                noPulseCounter,
                false,
                false,
                heartRate == 0
                        ? "Lectura sin pulso detectado. Se incrementa el contador preventivo."
                        : "Frecuencia cardiaca dentro de la simulacion normal."
        );
    }

    public HeartMonitorState confirmUserIsSafe() {
        HeartMonitorState state = alert.createAlertState(true, lastHeartRate, noPulseCounter);
        noPulseCounter = 0;
        preAlertActive = false;
        return state;
    }

    public HeartMonitorState stopMonitoring() {
        monitoring = false;
        return new HeartMonitorState(false, lastHeartRate, noPulseCounter, preAlertActive, false, "Monitor detenido.");
    }

    public HeartMonitorState getCurrentState() {
        return new HeartMonitorState(
                monitoring,
                lastHeartRate,
                noPulseCounter,
                preAlertActive,
                false,
                monitoring ? "Monitor activo." : (preAlertActive ? "Prealerta activa." : "Monitor detenido.")
        );
    }
}
