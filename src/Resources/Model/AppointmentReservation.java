package Resources.Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AppointmentReservation {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final int id;
    private final int userId;
    private final int resourceId;
    private final String resourceName;
    private final String resourceCategory;
    private final String resourceLocation;
    private final LocalDate appointmentDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String purpose;
    private final String notes;
    private final String status;

    public AppointmentReservation(int id, int userId, int resourceId, String resourceName, String resourceCategory,
                                  String resourceLocation, LocalDate appointmentDate, LocalTime startTime,
                                  LocalTime endTime, String purpose, String notes, String status) {
        this.id = id;
        this.userId = userId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceCategory = resourceCategory;
        this.resourceLocation = resourceLocation;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.notes = notes;
        this.status = status;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getResourceId() { return resourceId; }
    public String getResourceName() { return resourceName; }
    public String getResourceCategory() { return resourceCategory; }
    public String getResourceLocation() { return resourceLocation; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getPurpose() { return purpose; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return DATE_FORMATTER.format(appointmentDate)
                + " | " + TIME_FORMATTER.format(startTime)
                + "-" + TIME_FORMATTER.format(endTime)
                + " | " + resourceCategory
                + " | " + resourceName
                + " | " + status;
    }
}
