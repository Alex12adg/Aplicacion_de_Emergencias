package Resources.Database;

import Resources.Model.AppointmentReservation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public void initializeSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS bookings (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                resource_id INT NOT NULL,
                appointment_date DATE NOT NULL,
                start_time TIME NOT NULL,
                end_time TIME NOT NULL,
                purpose VARCHAR(120) NOT NULL,
                notes VARCHAR(255),
                status VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
                CONSTRAINT fk_bookings_resource FOREIGN KEY (resource_id) REFERENCES booking_resources(id)
            )
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public AppointmentReservation createReservation(int userId, int resourceId, LocalDate appointmentDate,
                                                    LocalTime startTime, LocalTime endTime,
                                                    String purpose, String notes) throws SQLException {
        String sql = """
            INSERT INTO bookings (user_id, resource_id, appointment_date, start_time, end_time, purpose, notes, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVA')
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, resourceId);
            stmt.setDate(3, Date.valueOf(appointmentDate));
            stmt.setTime(4, Time.valueOf(startTime));
            stmt.setTime(5, Time.valueOf(endTime));
            stmt.setString(6, purpose);
            stmt.setString(7, notes);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    return null;
                }

                return getReservationById(userId, keys.getInt(1));
            }
        }
    }

    public AppointmentReservation updateReservation(int userId, int reservationId, int resourceId,
                                                    LocalDate appointmentDate, LocalTime startTime,
                                                    LocalTime endTime, String purpose,
                                                    String notes) throws SQLException {
        String sql = """
            UPDATE bookings
            SET resource_id = ?, appointment_date = ?, start_time = ?, end_time = ?, purpose = ?, notes = ?
            WHERE id = ? AND user_id = ? AND status = 'ACTIVA'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            stmt.setDate(2, Date.valueOf(appointmentDate));
            stmt.setTime(3, Time.valueOf(startTime));
            stmt.setTime(4, Time.valueOf(endTime));
            stmt.setString(5, purpose);
            stmt.setString(6, notes);
            stmt.setInt(7, reservationId);
            stmt.setInt(8, userId);

            if (stmt.executeUpdate() == 0) {
                return null;
            }

            return getReservationById(userId, reservationId);
        }
    }

    public AppointmentReservation getReservationById(int userId, int reservationId) throws SQLException {
        String sql = """
            SELECT b.id, b.user_id, b.resource_id, b.appointment_date, b.start_time, b.end_time,
                   b.purpose, b.notes, b.status, r.name, r.category, r.location
            FROM bookings b
            INNER JOIN booking_resources r ON r.id = b.resource_id
            WHERE b.user_id = ? AND b.id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapReservation(rs);
            }
        }
    }

    public List<AppointmentReservation> getReservationsByUser(int userId) throws SQLException {
        List<AppointmentReservation> reservations = new ArrayList<>();
        String sql = """
            SELECT b.id, b.user_id, b.resource_id, b.appointment_date, b.start_time, b.end_time,
                   b.purpose, b.notes, b.status, r.name, r.category, r.location
            FROM bookings b
            INNER JOIN booking_resources r ON r.id = b.resource_id
            WHERE b.user_id = ?
            ORDER BY b.appointment_date DESC, b.start_time DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapReservation(rs));
                }
            }
        }

        return reservations;
    }

    public boolean hasTimeConflict(int resourceId, LocalDate appointmentDate, LocalTime startTime,
                                   LocalTime endTime) throws SQLException {
        return hasTimeConflict(resourceId, appointmentDate, startTime, endTime, null);
    }

    public boolean hasTimeConflict(int resourceId, LocalDate appointmentDate, LocalTime startTime,
                                   LocalTime endTime, Integer excludedReservationId) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS total
            FROM bookings
            WHERE resource_id = ?
              AND appointment_date = ?
              AND status = 'ACTIVA'
              AND (? IS NULL OR id <> ?)
              AND NOT (end_time <= ? OR start_time >= ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            stmt.setDate(2, Date.valueOf(appointmentDate));
            if (excludedReservationId == null) {
                stmt.setNull(3, java.sql.Types.INTEGER);
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, excludedReservationId);
                stmt.setInt(4, excludedReservationId);
            }
            stmt.setTime(5, Time.valueOf(startTime));
            stmt.setTime(6, Time.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("total") > 0;
            }
        }
    }

    public boolean deleteReservation(int userId, int reservationId) throws SQLException {
        String sql = """
            DELETE FROM bookings
            WHERE id = ? AND user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    private AppointmentReservation mapReservation(ResultSet rs) throws SQLException {
        return new AppointmentReservation(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("resource_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("location"),
                rs.getDate("appointment_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(),
                rs.getString("purpose"),
                rs.getString("notes"),
                rs.getString("status")
        );
    }
}
