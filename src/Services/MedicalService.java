package Services;

import Resources.Database.ContactDAO;
import Resources.Database.MedicalDAO;
import Resources.Model.Contact;
import Resources.Model.MedicalInfo;

import java.util.List;

public class MedicalService {

    private final MedicalDAO medicalDAO;
    private final ContactDAO contactDAO;

    public MedicalService() {
        this.medicalDAO = new MedicalDAO();
        this.contactDAO = new ContactDAO();
    }

    public void saveMedicalInfo(int userId, String allergies, String conditions, String medications) throws Exception {

        if (userId <= 0) {
            throw new Exception("Usuario invalido");
        }

        boolean medicalSaved = medicalDAO.saveMedicalInfo(userId, allergies, conditions, medications);

        if (!medicalSaved) {
            throw new Exception("No se pudo guardar el perfil medico");
        }
    }

    public void addContact(int userId, String contactName, String contactPhone, String contactRelation) throws Exception {

        if (userId <= 0) {
            throw new Exception("Usuario invalido");
        }

        if (contactName == null || contactName.isBlank()) {
            throw new Exception("El nombre del contacto es obligatorio");
        }

        if (contactPhone == null || contactPhone.isBlank()) {
            throw new Exception("El telefono del contacto es obligatorio");
        }

        boolean contactSaved = contactDAO.addContact(userId, contactName, contactPhone, contactRelation);

        if (!contactSaved) {
            throw new Exception("No se pudo guardar el contacto");
        }
    }

    public MedicalInfo getMedicalInfo(int userId) throws Exception {

        if (userId <= 0) {
            throw new Exception("Usuario invalido");
        }

        return medicalDAO.getMedicalInfoByUser(userId);
    }

    public void deleteContact(int userId, int contactId) throws Exception {

        if (userId <= 0) {
            throw new Exception("Usuario invalido");
        }

        if (contactId <= 0) {
            throw new Exception("Contacto invalido");
        }

        boolean deleted = contactDAO.deleteContact(userId, contactId);

        if (!deleted) {
            throw new Exception("No se pudo borrar el contacto");
        }
    }

    public List<Contact> getContacts(int userId) throws Exception {

        if (userId <= 0) {
            throw new Exception("Usuario invalido");
        }

        return contactDAO.getContactsByUser(userId);
    }
}
