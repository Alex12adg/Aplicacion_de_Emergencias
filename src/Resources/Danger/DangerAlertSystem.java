package Resources.Danger;

import Resources.Emergency.AlertSender;
import Resources.Emergency.EmergencyEvent;
import Resources.Emergency.EmergencyManager;
import Resources.Location.GPSModule;
import Resources.Session.UserSession;
import Resources.User.UserData;

public class DangerAlertSystem {

    private static final int MAX_ATTEMPTS = 3;

    private boolean alertActive = false;
    private int attemptsRemaining = 0;
    private String currentLocation = "";
    private final GPSModule gpsModule = new GPSModule();

    public void activateAlert(EmergencyManager manager) {
        startAlert();
    }

    public DangerAlertState startAlert() {
        alertActive = true;
        attemptsRemaining = MAX_ATTEMPTS;
        currentLocation = gpsModule.getAutoLocation();

        return new DangerAlertState(
                true,
                false,
                currentLocation,
                attemptsRemaining,
                "Alerta iniciada. Se ha detectado una situacion de peligro y se espera confirmacion del usuario."
        );
    }

    public DangerAlertState confirmSafe() {
        if (!alertActive) {
            return new DangerAlertState(false, false, currentLocation, attemptsRemaining, "No hay ninguna alerta activa.");
        }

        alertActive = false;
        return new DangerAlertState(
                false,
                false,
                currentLocation,
                attemptsRemaining,
                "El usuario ha confirmado que esta bien. La alerta se ha cancelado."
        );
    }

    public DangerAlertState registerNoConfirmation(EmergencyManager manager) {
        if (!alertActive) {
            return new DangerAlertState(false, false, currentLocation, attemptsRemaining, "No hay ninguna alerta activa.");
        }

        attemptsRemaining--;

        if (attemptsRemaining > 0) {
            return new DangerAlertState(
                true,
                false,
                currentLocation,
                attemptsRemaining,
                "Sin confirmacion del usuario. Se mantiene la alerta preventiva."
            );
        }

        alertActive = false;
        boolean sent = sendEmergencyAlert(manager, currentLocation);

        return new DangerAlertState(
                false,
                sent,
                currentLocation,
                0,
                sent
                        ? "No hubo respuesta. Se ha escalado a emergencia real."
                        : "No hubo respuesta, pero la emergencia no pudo enviarse."
        );
    }

    public DangerAlertState getCurrentState() {
        return new DangerAlertState(
                alertActive,
                false,
                currentLocation,
                attemptsRemaining,
                alertActive ? "Alerta activa en espera de confirmacion." : "Sin alerta activa."
        );
    }

    public boolean isAlertActive() {
        return alertActive;
    }

    private boolean sendEmergencyAlert(EmergencyManager manager, String location) {
        try {
            UserData user = UserSession.getUser();

            if (user == null) {
                throw new Exception("No hay usuario en sesion");
            }

            EmergencyEvent event = new EmergencyEvent("Peligro inminente", location, user, 3);
            new AlertSender().sendAlert(event);
            return true;
        } catch (Exception e) {
            System.out.println("Error al activar sistema de alerta: " + e.getMessage());
            return false;
        }
    }
}
