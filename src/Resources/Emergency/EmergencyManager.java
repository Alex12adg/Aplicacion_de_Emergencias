package Resources.Emergency;
import Resources.Data.JsonDataLoader;
import Resources.Location.GPSModule;
import Resources.Session.UserSession;
import Resources.User.UserData;

import java.util.List;

public class EmergencyManager {

    private static final String CENTERS_RESOURCE_PATH = "/Resources/Location/Centers.json";
    private static final int DEFAULT_THRESHOLD = 5;

    private final GPSModule gpsModule;

    public EmergencyManager() {
        this.gpsModule = new GPSModule();
    }

    public void startSystemInteractive() {

        //Cargar centros de emergencia desde JSON
        List<EmergencyCenter> centers = loadCenters();

        System.out.println("======================================");
        System.out.println("   CENTROS DE EMERGENCIA DISPONIBLES");
        System.out.println("======================================");

        if (centers.isEmpty()) {
            System.out.println("No se han podido cargar centros desde el JSON.");
        } else {
            for (EmergencyCenter c : centers) {
                System.out.println(" - " + c);
            }
        }

        System.out.println("\n======================================");
        System.out.println("   SISTEMA DE DETECCIÓN DE EMERGENCIAS");
        System.out.println("======================================");

        //Sistema actual
        EmergencyDetector detector = new EmergencyDetector(DEFAULT_THRESHOLD);
        EmergencyEvent event = detector.detectEventInteractive();

        if (event != null) {
            AlertSender sender = new AlertSender();
            sender.sendAlert(event);

            //Mostrar servicios útiles tras la emergencia
            if (!centers.isEmpty()) {
                System.out.println("\n=== SERVICIOS DE EMERGENCIA EN LA ZONA ===");
                for (EmergencyCenter c : centers) {
                    System.out.println(c.getType() + " -> " + c.getName());
                }
            }

        } else {
            System.out.println("No se activó ninguna emergencia.");
        }
    }

    public EmergencyProcessResult processEmergency(EmergencyRequest request) {
        try {
            validateRequest(request);

            String location = request.isAutomaticLocation()
                    ? gpsModule.getAutoLocation()
                    : request.getManualLocation().trim();

            if (!new EmergencyDetector(DEFAULT_THRESHOLD).validateSeverity(request.getSeverity())) {
                return new EmergencyProcessResult(
                        false,
                        "La gravedad debe ser 5 o superior para activar la emergencia.",
                        location,
                        null,
                        loadCenters()
                );
            }

            UserData user = UserSession.getUser();

            if (user == null) {
                user = new UserData(
                        0,
                        "Usuario Simulado",
                        "600000000",
                        "user",
                        null,
                        null,
                        null,
                        null,
                        null
                );
            }

            EmergencyEvent event = new EmergencyEvent(
                    request.getEmergencyType().trim(),
                    location,
                    user,
                    request.getSeverity()
            );

            new AlertSender().sendAlert(event);

            return new EmergencyProcessResult(
                    true,
                    "Emergencia enviada correctamente al sistema.",
                    location,
                    event,
                    loadCenters()
            );
        } catch (Exception e) {
            return new EmergencyProcessResult(
                    false,
                    e.getMessage(),
                    "",
                    null,
                    loadCenters()
            );
        }
    }

    public void triggerVoiceEmergency() {

        System.out.println("Activación de emergencia mediante voz.");

        try {
            UserData user = UserSession.getUser();

            if (user == null) {
                throw new Exception("No hay usuario en sesión");
            }

            EmergencyEvent event = new EmergencyEvent(
                    "Activación por palabra clave",
                    "Ubicación pendiente",
                    user,
                    3
            );

            AlertSender sender = new AlertSender();
            sender.sendAlert(event);

        } catch (Exception e) {
            System.out.println("Error al activar emergencia: " + e.getMessage());
        }
    }

    public List<EmergencyCenter> loadCenters() {
        return JsonDataLoader.loadCentersFromResource(CENTERS_RESOURCE_PATH);
    }

    private void validateRequest(EmergencyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("No se recibio ninguna solicitud de emergencia.");
        }

        if (request.getEmergencyType() == null || request.getEmergencyType().isBlank()) {
            throw new IllegalArgumentException("Debes indicar el tipo de emergencia.");
        }

        if (request.getSeverity() < 1 || request.getSeverity() > 10) {
            throw new IllegalArgumentException("La gravedad debe estar entre 1 y 10.");
        }

        if (!request.isAutomaticLocation()) {
            String manualLocation = request.getManualLocation();

            if (manualLocation == null || manualLocation.isBlank()) {
                throw new IllegalArgumentException("Debes indicar una ubicacion manual.");
            }

            GPSModule.parseLatLon(manualLocation.trim());
        }
    }
}
