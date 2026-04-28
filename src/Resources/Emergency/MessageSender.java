package Resources.Emergency;

import Resources.Database.ContactDAO;
import Resources.Model.Contact;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageSender {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path LOG_PATH = Paths.get("src", "Resources", "messages_log.txt");

    private final ContactDAO contactDAO;

    public MessageSender() {
        this(new ContactDAO());
    }

    public MessageSender(ContactDAO contactDAO) {
        this.contactDAO = contactDAO;
    }

    public void sendMessage(Contact contact, String mensaje) {
        if (!hasPhone(contact)) {
            System.out.println("Contacto sin telefono. No se puede enviar el mensaje a " + getName(contact) + ".");
            return;
        }

        String line = String.format(
                "[%s] To: %s (%s) - %s",
                timestamp(),
                getName(contact),
                contact.getPhone(),
                mensaje
        );

        System.out.println("-> Enviando a " + getName(contact) + " (" + contact.getPhone() + "): " + mensaje);
        appendToLog(line);
    }

    public void callContact(Contact contact) {
        if (!hasPhone(contact)) {
            System.out.println("Contacto sin telefono. No se puede llamar a " + getName(contact) + ".");
            return;
        }

        String line = String.format("[%s] LLAMADA a: %s (%s)", timestamp(), getName(contact), contact.getPhone());
        System.out.println("-> Llamando a " + getName(contact) + " (" + contact.getPhone() + ")...");
        appendToLog(line);
    }

    public void sendMessageToUserContacts(int userId, String mensaje) {
        List<Contact> contacts;

        try {
            contacts = contactDAO.getContactsByUser(userId);
        } catch (SQLException e) {
            System.err.println("No se pudieron cargar los contactos del usuario " + userId + ": " + e.getMessage());
            return;
        }

        if (contacts.isEmpty()) {
            System.out.println("No hay contactos en la base de datos para el usuario " + userId + ".");
            return;
        }

        for (Contact contact : contacts) {
            sendMessage(contact, mensaje);
        }
    }

    public void callUserContacts(int userId) {
        List<Contact> contacts;

        try {
            contacts = contactDAO.getContactsByUser(userId);
        } catch (SQLException e) {
            System.err.println("No se pudieron cargar los contactos del usuario " + userId + ": " + e.getMessage());
            return;
        }

        if (contacts.isEmpty()) {
            System.out.println("No hay contactos en la base de datos para el usuario " + userId + ".");
            return;
        }

        for (Contact contact : contacts) {
            callContact(contact);
        }
    }

    private void appendToLog(String line) {
        try {
            Files.createDirectories(LOG_PATH.getParent());
            Files.writeString(
                    LOG_PATH,
                    line + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Error escribiendo messages_log.txt: " + e.getMessage());
        }
    }

    private String timestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }

    private boolean hasPhone(Contact contact) {
        return contact != null && contact.getPhone() != null && !contact.getPhone().isBlank();
    }

    private String getName(Contact contact) {
        if (contact == null || contact.getName() == null || contact.getName().isBlank()) {
            return "Contacto desconocido";
        }

        return contact.getName();
    }
}
