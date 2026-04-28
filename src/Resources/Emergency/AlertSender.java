package Resources.Emergency;

import java.io.FileWriter;
import java.io.IOException;

public class AlertSender {

    private final MessageSender messageSender = new MessageSender();

    public void sendAlert(EmergencyEvent event) {
        System.out.println("\nEnviando alerta al 112...");
        System.out.println(event.toString());

        try (FileWriter writer = new FileWriter("SRC/Resources/log.txt", true)) {
            writer.write(event.toString() + "\n");
            System.out.println("Alerta registrada en log.txt");
        } catch (IOException e) {
            System.out.println("Error escribiendo en log.txt: " + e.getMessage());
        }

        notifyContacts(event);
    }

    private void notifyContacts(EmergencyEvent event) {
        if (event.getUsuario() == null) {
            System.out.println("No hay usuario asociado a la emergencia.");
            return;
        }

        System.out.println("Notificando a contactos del usuario " + event.getUsuario().getNombre());

        if (event.getUsuario().getId() <= 0) {
            System.out.println("El usuario no tiene ID valido para buscar contactos en la base de datos.");
            return;
        }

        messageSender.sendMessageToUserContacts(
                event.getUsuario().getId(),
                "ALERTA: " + event.toString()
        );
    }
}
