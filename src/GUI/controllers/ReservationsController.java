package GUI.controllers;

import Resources.Model.AppointmentReservation;
import Resources.Model.ReservationResource;
import Resources.Session.UserSession;
import Resources.User.UserData;
import Services.ReservationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

public class ReservationsController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservationService reservationService;
    private final ObservableList<String> resourceTypeItems;
    private final ObservableList<ReservationResource> allResourceItems;
    private final ObservableList<ReservationResource> resourceItems;
    private final ObservableList<AppointmentReservation> reservationItems;
    private AppointmentReservation editingReservation;

    @FXML
    private ComboBox<String> resourceTypeComboBox;

    @FXML
    private ComboBox<ReservationResource> resourceComboBox;

    @FXML
    private DatePicker appointmentDatePicker;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private TextField purposeField;

    @FXML
    private TextArea notesArea;

    @FXML
    private Button submitReservationButton;

    @FXML
    private Button cancelEditButton;

    @FXML
    private ListView<AppointmentReservation> reservationsList;

    @FXML
    private Label selectedResourceLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label reservationSummaryLabel;

    @FXML
    private Label formTitleLabel;

    public ReservationsController() {
        this.reservationService = new ReservationService();
        this.resourceTypeItems = FXCollections.observableArrayList();
        this.allResourceItems = FXCollections.observableArrayList();
        this.resourceItems = FXCollections.observableArrayList();
        this.reservationItems = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        resourceTypeComboBox.setItems(resourceTypeItems);
        resourceComboBox.setItems(resourceItems);
        reservationsList.setItems(reservationItems);
        appointmentDatePicker.setValue(java.time.LocalDate.now().plusDays(1));
        startTimeField.setText("09:00");
        endTimeField.setText("09:30");
        selectedResourceLabel.setText("Selecciona un recurso del catalogo o del desplegable.");
        reservationSummaryLabel.setText("Todavia no hay reservas cargadas.");
        statusLabel.setText("El modulo prepara los recursos al abrirse.");
        setEditingReservation(null);

        resourceComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedResourceLabel.setText(buildResourceDetail(newValue));
            }
        });

        resourceTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->
                applyTypeFilter(resourceComboBox.getValue() != null ? resourceComboBox.getValue().getId() : null)
        );

        loadData();
    }

    @FXML
    public void handleReserve() {
        try {
            UserData user = requireUser();
            boolean editing = editingReservation != null;

            if (editing) {
                AppointmentReservation updated = reservationService.updateReservation(
                        user.getId(),
                        editingReservation,
                        resourceComboBox.getValue(),
                        appointmentDatePicker.getValue(),
                        startTimeField.getText(),
                        endTimeField.getText(),
                        purposeField.getText(),
                        notesArea.getText()
                );
                loadData();
                selectReservation(updated.getId());
                statusLabel.setText("Reserva actualizada correctamente");
            } else {
                reservationService.createReservation(
                        user.getId(),
                        resourceComboBox.getValue(),
                        appointmentDatePicker.getValue(),
                        startTimeField.getText(),
                        endTimeField.getText(),
                        purposeField.getText(),
                        notesArea.getText()
                );
                loadData();
                statusLabel.setText("Reserva registrada correctamente");
            }

            clearReservationForm();
            setEditingReservation(null);
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleEditReservation() {
        AppointmentReservation selected = reservationsList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("Selecciona una reserva para editar");
            return;
        }

        if (!"ACTIVA".equalsIgnoreCase(selected.getStatus())) {
            statusLabel.setText("Solo se pueden editar reservas activas");
            return;
        }

        ReservationResource resource = findResourceById(selected.getResourceId());

        if (resource == null) {
            statusLabel.setText("No se encontro el recurso asociado a la reserva");
            return;
        }

        resourceTypeComboBox.getSelectionModel().select(resource.getCategory());
        resourceComboBox.getSelectionModel().select(resource);
        appointmentDatePicker.setValue(selected.getAppointmentDate());
        startTimeField.setText(TIME_FORMATTER.format(selected.getStartTime()));
        endTimeField.setText(TIME_FORMATTER.format(selected.getEndTime()));
        purposeField.setText(selected.getPurpose());
        notesArea.setText(selected.getNotes() == null ? "" : selected.getNotes());
        setEditingReservation(selected);
        statusLabel.setText("Editando la reserva seleccionada");
    }

    @FXML
    public void handleDeleteReservation() {
        try {
            UserData user = requireUser();
            AppointmentReservation selected = reservationsList.getSelectionModel().getSelectedItem();
            reservationService.deleteReservation(user.getId(), selected);
            loadReservations(user.getId());
            if (editingReservation != null && selected != null && editingReservation.getId() == selected.getId()) {
                clearReservationForm();
                setEditingReservation(null);
            }
            statusLabel.setText("Reserva borrada correctamente");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleCancelEdit() {
        clearReservationForm();
        setEditingReservation(null);
        statusLabel.setText("Edicion cancelada");
    }

    @FXML
    public void handleReload() {
        loadData();
    }

    private void loadData() {
        try {
            UserData user = requireUser();
            reservationService.initializeModule();
            loadResources();
            loadReservations(user.getId());
            statusLabel.setText("Catalogo y reservas actualizados");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
            reservationSummaryLabel.setText("No se pudieron cargar las reservas.");
        }
    }

    private void loadResources() throws Exception {
        UserData user = requireUser();
        ReservationResource selected = resourceComboBox.getValue();
        allResourceItems.setAll(reservationService.getAvailableResources(user.getId()));
        loadResourceTypes();

        Integer selectedResourceId = selected == null ? null : selected.getId();

        if (selected != null) {
            resourceTypeComboBox.getSelectionModel().select(selected.getCategory());
        } else if (!resourceTypeItems.isEmpty() && resourceTypeComboBox.getValue() == null) {
            resourceTypeComboBox.getSelectionModel().selectFirst();
        }

        applyTypeFilter(selectedResourceId);
    }

    private void loadResourceTypes() {
        Set<String> types = new LinkedHashSet<>();

        for (ReservationResource resource : allResourceItems) {
            if (resource.getCategory() != null && !resource.getCategory().isBlank()) {
                types.add(resource.getCategory());
            }
        }

        resourceTypeItems.setAll(types);
    }

    private void applyTypeFilter(Integer preferredResourceId) {
        String selectedType = resourceTypeComboBox.getValue();
        ReservationResource preferredResource = null;

        resourceItems.clear();

        for (ReservationResource resource : allResourceItems) {
            if (selectedType == null || selectedType.equals(resource.getCategory())) {
                resourceItems.add(resource);
            }
        }

        if (preferredResourceId != null) {
            for (ReservationResource resource : resourceItems) {
                if (resource.getId() == preferredResourceId) {
                    preferredResource = resource;
                    break;
                }
            }
        }

        if (preferredResource != null) {
            resourceComboBox.getSelectionModel().select(preferredResource);
            return;
        }

        if (!resourceItems.isEmpty()) {
            resourceComboBox.getSelectionModel().selectFirst();
        } else {
            resourceComboBox.getSelectionModel().clearSelection();
            selectedResourceLabel.setText("No hay centros disponibles para el tipo de cita seleccionado.");
        }
    }

    private void loadReservations(int userId) throws Exception {
        reservationItems.setAll(reservationService.getReservationsByUser(userId));

        if (reservationItems.isEmpty()) {
            reservationSummaryLabel.setText("Aun no tienes citas reservadas.");
            return;
        }

        AppointmentReservation latest = reservationItems.get(0);
        reservationSummaryLabel.setText(
                "Ultima reserva: " + latest.getResourceName()
                        + " el " + DATE_FORMATTER.format(latest.getAppointmentDate())
                        + " de " + TIME_FORMATTER.format(latest.getStartTime())
                        + " a " + TIME_FORMATTER.format(latest.getEndTime())
                        + " [" + latest.getStatus() + "]"
        );
    }

    private UserData requireUser() throws Exception {
        UserData user = UserSession.getUser();

        if (user == null || user.getId() <= 0) {
            throw new Exception("Debes iniciar sesion para gestionar reservas");
        }

        return user;
    }

    private void clearReservationForm() {
        if (!resourceTypeItems.isEmpty()) {
            resourceTypeComboBox.getSelectionModel().selectFirst();
        }
        appointmentDatePicker.setValue(java.time.LocalDate.now().plusDays(1));
        startTimeField.setText("09:00");
        endTimeField.setText("09:30");
        purposeField.clear();
        notesArea.clear();
    }

    private void setEditingReservation(AppointmentReservation reservation) {
        this.editingReservation = reservation;
        boolean editing = reservation != null;

        formTitleLabel.setText(editing ? "Editar reserva" : "Nueva reserva");
        submitReservationButton.setText(editing ? "Guardar cambios" : "Reservar cita");
        cancelEditButton.setVisible(editing);
        cancelEditButton.setManaged(editing);
    }

    private ReservationResource findResourceById(int resourceId) {
        for (ReservationResource resource : allResourceItems) {
            if (resource.getId() == resourceId) {
                return resource;
            }
        }

        return null;
    }

    private void selectReservation(int reservationId) {
        for (AppointmentReservation reservation : reservationItems) {
            if (reservation.getId() == reservationId) {
                reservationsList.getSelectionModel().select(reservation);
                reservationsList.scrollTo(reservation);
                return;
            }
        }
    }

    private String buildResourceDetail(ReservationResource resource) {
        String description = resource.getDescription() == null || resource.getDescription().isBlank()
                ? "Sin descripcion adicional"
                : resource.getDescription();

        return (resource.isPublicResource() ? "Publico" : "Privado")
                + " | " + resource.getCategory()
                + " | " + resource.getName()
                + " | " + resource.getLocation()
                + " | " + resource.getSlotDurationMinutes()
                + " min | "
                + description;
    }

}
