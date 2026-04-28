package Resources.Emergency;

import Resources.Location.GPSModule;
import Resources.Session.UserSession;
import Resources.User.UserData;

import java.util.Scanner;

public class EmergencyDetector {
    private int umbral;
    private GPSModule gps;

    public EmergencyDetector(int umbral) {
        this.umbral = umbral;
        this.gps = new GPSModule();
    }

    public EmergencyEvent detectEventInteractive() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Presiona 'E' para activar emergencia o cualquier otra tecla para cancelar:");
        String input = sc.nextLine();

        if (!input.equalsIgnoreCase("E")) return null;

        System.out.print("Tipo de emergencia (ej. Accidente, Salud): ");
        String tipo = sc.nextLine();

        System.out.print("Usar GPS automatico? (s/n): ");
        String resp = sc.nextLine();

        boolean automatico = resp.equalsIgnoreCase("s");
        String ubicacion = gps.getLocation(automatico);

        System.out.print("Nivel de gravedad (1-10): ");
        int gravedad;
        try {
            gravedad = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Entrada invalida para gravedad. Se asume 5.");
            gravedad = 5;
        }

        if (!validateSeverity(gravedad)) {
            System.out.println("Emergencia descartada: gravedad insuficiente.");
            return null;
        }

        UserData usuario = UserSession.getUser();

        if (usuario == null) {
            usuario = new UserData(
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

        return new EmergencyEvent(tipo, ubicacion, usuario, gravedad);
    }

    public boolean validateSeverity(int gravedad) {
        return gravedad >= umbral;
    }
}
