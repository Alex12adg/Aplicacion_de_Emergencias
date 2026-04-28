package Resources.Heart;

public class HeartRateAlert {

    public HeartMonitorState createAlertState(boolean userConfirmed, int heartRate, int noPulseCounter) {
        if (userConfirmed) {
            return new HeartMonitorState(
                    false,
                    heartRate,
                    noPulseCounter,
                    false,
                    true,
                    "El usuario ha confirmado que esta bien. La prealerta se ha cancelado."
            );
        }

        return new HeartMonitorState(
                false,
                heartRate,
                noPulseCounter,
                true,
                false,
                "Posible ausencia de pulso detectada. Se requiere confirmacion del usuario."
        );
    }
}
