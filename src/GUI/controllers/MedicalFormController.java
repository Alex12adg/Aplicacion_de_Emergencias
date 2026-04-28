package GUI.controllers;

import GUI.AppLayout;
import Resources.Model.Contact;
import Resources.Model.MedicalInfo;
import Resources.Session.UserSession;
import Resources.User.UserData;
import Services.MedicalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MedicalFormController {

    @FXML
    private TextArea allergiesField;

    @FXML
    private TextArea conditionsField;

    @FXML
    private TextArea medicationsField;

    @FXML
    private TextField contactNameField;

    @FXML
    private TextField contactPhoneField;

    @FXML
    private TextField contactRelationField;

    @FXML
    private ListView<Contact> contactsList;

    @FXML
    private Label statusLabel;

    @FXML
    private Button editButton;

    @FXML
    private Button addContactButton;

    @FXML
    private Button toggleContactFormButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteContactButton;

    @FXML
    private VBox contactFormBox;

    private final MedicalService medicalService;
    private final ObservableList<Contact> contactItems;
    private final List<Contact> pendingContacts;
    private boolean editMode;

    public MedicalFormController() {
        this.medicalService = new MedicalService();
        this.contactItems = FXCollections.observableArrayList();
        this.pendingContacts = new ArrayList<>();
    }

    @FXML
    public void initialize() {
        contactsList.setItems(contactItems);
        loadCurrentData();

        if (UserSession.isNewUser()) {
            editMode = true;
            saveButton.setText("Guardar y continuar");
            editButton.setVisible(false);
            editButton.setManaged(false);
        } else {
            editMode = false;
        }

        applyEditMode();
    }

    @FXML
    public void enableEditMode() {
        editMode = true;
        statusLabel.setText("Modo edicion activado");
        applyEditMode();
    }

    @FXML
    public void toggleContactForm() {
        boolean showForm = !contactFormBox.isVisible();
        contactFormBox.setVisible(showForm);
        contactFormBox.setManaged(showForm);
        toggleContactFormButton.setText(showForm ? "Ocultar formulario de contacto" : "Mostrar formulario de contacto");
    }

    @FXML
    public void handleAddContact() {
        try {
            if (!editMode) {
                throw new Exception("Activa primero el modo edicion");
            }

            String name = contactNameField.getText();
            String phone = contactPhoneField.getText();
            String relation = contactRelationField.getText();

            if (name == null || name.isBlank()) {
                throw new Exception("El nombre del contacto es obligatorio");
            }

            if (phone == null || phone.isBlank()) {
                throw new Exception("El telefono del contacto es obligatorio");
            }

            Contact contact = new Contact(name, phone, relation);
            pendingContacts.add(contact);
            contactItems.add(contact);
            clearContactInputs();
            contactFormBox.setVisible(false);
            contactFormBox.setManaged(false);
            toggleContactFormButton.setText("Mostrar formulario de contacto");
            statusLabel.setText("Contacto preparado para guardar");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        try {
            UserData user = UserSession.getUser();

            if (user == null) {
                throw new Exception("No hay usuario en sesion");
            }

            if (!editMode) {
                throw new Exception("Activa primero el modo edicion");
            }

            boolean initialRegistration = UserSession.isNewUser();
            boolean hasExistingContacts = !medicalService.getContacts(user.getId()).isEmpty();

            if (initialRegistration && !hasExistingContacts && pendingContacts.isEmpty()) {
                throw new Exception("Debes anadir al menos un contacto");
            }

            medicalService.saveMedicalInfo(
                    user.getId(),
                    allergiesField.getText(),
                    conditionsField.getText(),
                    medicationsField.getText()
            );

            savePendingContacts(user.getId());

            user.setAllergies(allergiesField.getText());
            user.setConditions(conditionsField.getText());
            user.setMedications(medicationsField.getText());

            pendingContacts.clear();
            editMode = false;
            loadContacts();
            applyEditMode();
            statusLabel.setText("Datos guardados correctamente");

            if (initialRegistration) {
                UserSession.setNewUser(false);
                openScene(event, "/GUI/Views/main-view.fxml");
            }
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleDeleteContact() {
        try {
            if (!editMode) {
                throw new Exception("Activa primero el modo edicion");
            }

            Contact selectedContact = contactsList.getSelectionModel().getSelectedItem();

            if (selectedContact == null) {
                throw new Exception("Selecciona un contacto para borrar");
            }

            if (selectedContact.getId() <= 0) {
                pendingContacts.remove(selectedContact);
                contactItems.remove(selectedContact);
                statusLabel.setText("Contacto pendiente eliminado");
                return;
            }

            UserData user = UserSession.getUser();

            if (user == null || user.getId() <= 0) {
                throw new Exception("No hay usuario en sesion");
            }

            medicalService.deleteContact(user.getId(), selectedContact.getId());
            loadContacts();
            statusLabel.setText("Contacto borrado correctamente");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }

    private void savePendingContacts(int userId) throws Exception {
        for (Contact contact : pendingContacts) {
            medicalService.addContact(
                    userId,
                    contact.getName(),
                    contact.getPhone(),
                    contact.getRelation()
            );
        }
    }

    private void loadCurrentData() {
        try {
            UserData user = UserSession.getUser();

            if (user == null || user.getId() <= 0) {
                return;
            }

            MedicalInfo medicalInfo = medicalService.getMedicalInfo(user.getId());

            if (medicalInfo != null) {
                allergiesField.setText(nullSafe(medicalInfo.getAllergies()));
                conditionsField.setText(nullSafe(medicalInfo.getConditions()));
                medicationsField.setText(nullSafe(medicalInfo.getMedications()));
            } else {
                allergiesField.setText(nullSafe(user.getAllergies()));
                conditionsField.setText(nullSafe(user.getConditions()));
                medicationsField.setText(nullSafe(user.getMedications()));
            }

            loadContacts();
        } catch (Exception e) {
            statusLabel.setText("No se pudieron cargar los datos actuales");
        }
    }

    private void loadContacts() throws Exception {
        UserData user = UserSession.getUser();
        List<Contact> contacts = medicalService.getContacts(user.getId());

        contactItems.clear();
        contactItems.addAll(pendingContacts);

        for (Contact contact : contacts) {
            contactItems.add(contact);
        }
    }

    private void applyEditMode() {
        allergiesField.setEditable(editMode);
        conditionsField.setEditable(editMode);
        medicationsField.setEditable(editMode);

        contactNameField.setDisable(!editMode);
        contactPhoneField.setDisable(!editMode);
        contactRelationField.setDisable(!editMode);
        addContactButton.setDisable(!editMode);
        deleteContactButton.setDisable(!editMode);
        toggleContactFormButton.setDisable(!editMode);
        saveButton.setDisable(!editMode);

        if (!editMode) {
            contactFormBox.setVisible(false);
            contactFormBox.setManaged(false);
            toggleContactFormButton.setText("Mostrar formulario de contacto");
        }
    }

    private void clearContactInputs() {
        contactNameField.clear();
        contactPhoneField.clear();
        contactRelationField.clear();
    }

    private void openScene(ActionEvent event, String resourcePath) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, AppLayout.MOBILE_WIDTH, AppLayout.MOBILE_HEIGHT));
        stage.show();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
