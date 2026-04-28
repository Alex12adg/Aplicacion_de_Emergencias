package Resources.Contacts;

import java.util.ArrayList;
import java.util.List;

public class ContactManager {

    private final List<ContactData> contacts = new ArrayList<>();

    public void addContact(String nombre, String telefono, String relacion) {
        contacts.add(new ContactData(nombre, telefono, relacion));
    }

    public List<ContactData> getAllContacts() {
        return new ArrayList<>(contacts);
    }

    public List<ContactData> getContactsByRelation(String relation) {

        List<ContactData> result = new ArrayList<>();

        for (ContactData c : contacts) {
            if (c.getRelacion() != null &&
                    c.getRelacion().equalsIgnoreCase(relation)) {
                result.add(c);
            }
        }

        return result;
    }
}