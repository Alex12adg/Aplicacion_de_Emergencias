package Services;

import Resources.Database.ReservationDAO;
import Resources.Database.ReservationResourceDAO;
import Resources.Model.AppointmentReservation;
import Resources.Model.ReservationResource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReservationService {

    private final ReservationResourceDAO resourceDAO;
    private final ReservationDAO reservationDAO;

    public ReservationService() {
        this.resourceDAO = new ReservationResourceDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public void initializeModule() throws Exception {
        resourceDAO.initializeSchema();
        reservationDAO.initializeSchema();
        resourceDAO.seedDefaultResources();
    }

    public List<ReservationResource> getAvailableResources(int userId) throws Exception {
        validateUser(userId);
        List<ReservationResource> resources = new java.util.ArrayList<>();
        resources.addAll(resourceDAO.getPublicResources());
        resources.addAll(resourceDAO.getPrivateResourcesByUser(userId));
        return resources;
    }

    public List<ReservationResource> getPublicResources() throws Exception {
        return resourceDAO.getPublicResources();
    }

    public List<ReservationResource> getPrivateResourcesByUser(int userId) throws Exception {
        validateUser(userId);
        return resourceDAO.getPrivateResourcesByUser(userId);
    }

    public List<AppointmentReservation> getReservationsByUser(int userId) throws Exception {
        validateUser(userId);
        return reservationDAO.getReservationsByUser(userId);
    }

    public AppointmentReservation createReservation(int userId, ReservationResource resource, LocalDate appointmentDate,
                                                    String startTimeText, String endTimeText,
                                                    String purpose, String notes) throws Exception {
        validateUser(userId);
        validateReservationData(resource, appointmentDate, purpose);

        LocalTime startTime = parseTime(startTimeText, "Hora de inicio no valida. Usa formato HH:mm");
        LocalTime endTime = parseTime(endTimeText, "Hora de fin no valida. Usa formato HH:mm");
        validateTimeRange(startTime, endTime);

        if (reservationDAO.hasTimeConflict(resource.getId(), appointmentDate, startTime, endTime)) {
            throw new Exception("Ya existe una reserva activa en ese tramo horario");
        }

        AppointmentReservation created = reservationDAO.createReservation(
                userId,
                resource.getId(),
                appointmentDate,
                startTime,
                endTime,
                purpose.trim(),
                normalizeNotes(notes)
        );

        if (created == null) {
            throw new Exception("No se pudo registrar la reserva");
        }

        return created;
    }

    public AppointmentReservation updateReservation(int userId, AppointmentReservation reservation,
                                                    ReservationResource resource, LocalDate appointmentDate,
                                                    String startTimeText, String endTimeText,
                                                    String purpose, String notes) throws Exception {
        validateUser(userId);

        if (reservation == null || reservation.getId() <= 0) {
            throw new Exception("Selecciona una reserva para editar");
        }

        if (!"ACTIVA".equalsIgnoreCase(reservation.getStatus())) {
            throw new Exception("Solo se pueden editar reservas activas");
        }

        validateReservationData(resource, appointmentDate, purpose);

        LocalTime startTime = parseTime(startTimeText, "Hora de inicio no valida. Usa formato HH:mm");
        LocalTime endTime = parseTime(endTimeText, "Hora de fin no valida. Usa formato HH:mm");
        validateTimeRange(startTime, endTime);

        if (reservationDAO.hasTimeConflict(resource.getId(), appointmentDate, startTime, endTime, reservation.getId())) {
            throw new Exception("Ya existe una reserva activa en ese tramo horario");
        }

        AppointmentReservation updated = reservationDAO.updateReservation(
                userId,
                reservation.getId(),
                resource.getId(),
                appointmentDate,
                startTime,
                endTime,
                purpose.trim(),
                normalizeNotes(notes)
        );

        if (updated == null) {
            throw new Exception("No se pudo actualizar la reserva");
        }

        return updated;
    }

    public void deleteReservation(int userId, AppointmentReservation reservation) throws Exception {
        validateUser(userId);

        if (reservation == null || reservation.getId() <= 0) {
            throw new Exception("Selecciona una reserva para borrar");
        }

        if (!"ACTIVA".equalsIgnoreCase(reservation.getStatus())) {
            throw new Exception("La reserva seleccionada ya no esta disponible");
        }

        boolean deleted = reservationDAO.deleteReservation(userId, reservation.getId());

        if (!deleted) {
            throw new Exception("No se pudo borrar la reserva");
        }
    }

    public ReservationResource createPrivateResource(int userId, String name, String category, String location,
                                                     String description, String slotDurationText) throws Exception {
        validateUser(userId);
        validatePrivateResourceData(name, category, location, slotDurationText);

        ReservationResource created = resourceDAO.createPrivateResource(
                userId,
                name.trim(),
                category.trim(),
                location.trim(),
                normalizeNotes(description),
                parseSlotDuration(slotDurationText)
        );

        if (created == null) {
            throw new Exception("No se pudo crear el centro privado");
        }

        return created;
    }

    public ReservationResource updatePrivateResource(int userId, ReservationResource resource, String name,
                                                     String category, String location, String description,
                                                     String slotDurationText) throws Exception {
        validateUser(userId);

        if (resource == null || resource.getId() <= 0 || resource.isPublicResource()) {
            throw new Exception("Selecciona un centro privado para modificar");
        }

        validatePrivateResourceData(name, category, location, slotDurationText);

        ReservationResource updated = resourceDAO.updatePrivateResource(
                userId,
                resource.getId(),
                name.trim(),
                category.trim(),
                location.trim(),
                normalizeNotes(description),
                parseSlotDuration(slotDurationText)
        );

        if (updated == null) {
            throw new Exception("No se pudo modificar la informacion del centro");
        }

        return updated;
    }

    public void deletePrivateResource(int userId, ReservationResource resource) throws Exception {
        validateUser(userId);

        if (resource == null || resource.getId() <= 0 || resource.isPublicResource()) {
            throw new Exception("Selecciona un centro privado para borrar");
        }

        boolean deleted = resourceDAO.deletePrivateResource(userId, resource.getId());

        if (!deleted) {
            throw new Exception("No se pudo borrar el centro privado");
        }
    }

    private void validateUser(int userId) throws Exception {
        if (userId <= 0) {
            throw new Exception("Usuario invalido");
        }
    }

    private void validateReservationData(ReservationResource resource, LocalDate appointmentDate,
                                         String purpose) throws Exception {
        if (resource == null || resource.getId() <= 0) {
            throw new Exception("Selecciona un recurso para reservar");
        }

        if (appointmentDate == null) {
            throw new Exception("Selecciona una fecha");
        }

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new Exception("La fecha de la cita no puede estar en el pasado");
        }

        if (purpose == null || purpose.isBlank()) {
            throw new Exception("Indica el motivo de la cita");
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) throws Exception {
        if (!startTime.isBefore(endTime)) {
            throw new Exception("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private void validatePrivateResourceData(String name, String category, String location,
                                             String slotDurationText) throws Exception {
        if (name == null || name.isBlank()) {
            throw new Exception("Indica el nombre del centro");
        }

        if (category == null || category.isBlank()) {
            throw new Exception("Indica la categoria del centro");
        }

        if (location == null || location.isBlank()) {
            throw new Exception("Indica la ubicacion del centro");
        }

        parseSlotDuration(slotDurationText);
    }

    private int parseSlotDuration(String value) throws Exception {
        try {
            int minutes = Integer.parseInt(value == null ? "" : value.trim());

            if (minutes <= 0) {
                throw new Exception("La duracion del tramo debe ser mayor que cero");
            }

            return minutes;
        } catch (NumberFormatException e) {
            throw new Exception("Duracion no valida. Usa minutos enteros");
        }
    }

    private String normalizeNotes(String notes) {
        return notes == null ? "" : notes.trim();
    }

    private LocalTime parseTime(String value, String errorMessage) throws Exception {
        try {
            return LocalTime.parse(value == null ? "" : value.trim());
        } catch (DateTimeParseException e) {
            throw new Exception(errorMessage);
        }
    }
}
