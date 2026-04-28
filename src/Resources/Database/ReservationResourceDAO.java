package Resources.Database;

import Resources.Model.ReservationResource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReservationResourceDAO {

    public void initializeSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS booking_resources (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(120) NOT NULL,
                category VARCHAR(80) NOT NULL,
                location VARCHAR(120) NOT NULL,
                description VARCHAR(255),
                slot_duration_minutes INT NOT NULL DEFAULT 30,
                active BOOLEAN NOT NULL DEFAULT TRUE,
                public_resource BOOLEAN NOT NULL DEFAULT TRUE,
                owner_user_id INT NULL,
                CONSTRAINT fk_booking_resources_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
            )
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            ensureColumnExists(conn, stmt, "booking_resources", "public_resource",
                    "ALTER TABLE booking_resources ADD COLUMN public_resource BOOLEAN NOT NULL DEFAULT TRUE");
            ensureColumnExists(conn, stmt, "booking_resources", "owner_user_id",
                    "ALTER TABLE booking_resources ADD COLUMN owner_user_id INT NULL");
            dropIndexIfExists(conn, stmt, "booking_resources", "uq_booking_resources_name_location");
        }
    }

    public void seedDefaultResources() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            upsertPublicResource(conn, "Consulta de medicina general", "Cita medica", "Centro de salud Norte",
                    "Revision general, recetas y seguimiento de sintomas.", 30, true);
            upsertPublicResource(conn, "Pediatria", "Cita medica", "Centro de salud Infantil",
                    "Atencion pediatrica para menores y controles basicos.", 30, true);
            upsertPublicResource(conn, "Psicologia clinica", "Cita medica", "Centro integral de bienestar",
                    "Atencion psicologica y seguimiento emocional.", 45, true);
            upsertPublicResource(conn, "Atencion en comisaria", "Comisaria", "Comisaria Centro",
                    "Tramites, denuncias y consultas presenciales.", 20, true);
            upsertPublicResource(conn, "Renovacion de documentacion", "Comisaria", "Comisaria Distrito Norte",
                    "Reserva para tramites administrativos y documentacion.", 20, true);
            upsertPublicResource(conn, "Trabajo social", "Atencion social", "Oficina municipal",
                    "Orientacion social y derivacion de servicios.", 40, true);
        }
    }

    public List<ReservationResource> getPublicResources() throws SQLException {
        List<ReservationResource> resources = new ArrayList<>();
        String sql = """
            SELECT id, name, category, location, description, slot_duration_minutes, active,
                   public_resource, owner_user_id
            FROM booking_resources
            WHERE active = TRUE AND public_resource = TRUE
            ORDER BY category, name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resources.add(mapResource(rs));
            }
        }

        return resources;
    }

    public List<ReservationResource> getPrivateResourcesByUser(int userId) throws SQLException {
        List<ReservationResource> resources = new ArrayList<>();
        String sql = """
            SELECT id, name, category, location, description, slot_duration_minutes, active,
                   public_resource, owner_user_id
            FROM booking_resources
            WHERE active = TRUE AND public_resource = FALSE AND owner_user_id = ?
            ORDER BY category, name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resources.add(mapResource(rs));
                }
            }
        }

        return resources;
    }

    public ReservationResource createPrivateResource(int userId, String name, String category, String location,
                                                     String description, int slotDurationMinutes) throws SQLException {
        String sql = """
            INSERT INTO booking_resources
                (name, category, location, description, slot_duration_minutes, active, public_resource, owner_user_id)
            VALUES (?, ?, ?, ?, ?, TRUE, FALSE, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setString(3, location);
            stmt.setString(4, description);
            stmt.setInt(5, slotDurationMinutes);
            stmt.setInt(6, userId);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    return null;
                }

                return getPrivateResourceById(userId, keys.getInt(1));
            }
        }
    }

    public ReservationResource updatePrivateResource(int userId, int resourceId, String name, String category,
                                                     String location, String description,
                                                     int slotDurationMinutes) throws SQLException {
        String sql = """
            UPDATE booking_resources
            SET name = ?, category = ?, location = ?, description = ?, slot_duration_minutes = ?
            WHERE id = ? AND owner_user_id = ? AND public_resource = FALSE
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setString(3, location);
            stmt.setString(4, description);
            stmt.setInt(5, slotDurationMinutes);
            stmt.setInt(6, resourceId);
            stmt.setInt(7, userId);

            if (stmt.executeUpdate() == 0) {
                return null;
            }

            return getPrivateResourceById(userId, resourceId);
        }
    }

    public boolean deletePrivateResource(int userId, int resourceId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            if (resourceHasBookings(conn, resourceId)) {
                String disableSql = """
                    UPDATE booking_resources
                    SET active = FALSE
                    WHERE id = ? AND owner_user_id = ? AND public_resource = FALSE
                """;

                try (PreparedStatement stmt = conn.prepareStatement(disableSql)) {
                    stmt.setInt(1, resourceId);
                    stmt.setInt(2, userId);
                    return stmt.executeUpdate() > 0;
                }
            }

            String deleteSql = """
                DELETE FROM booking_resources
                WHERE id = ? AND owner_user_id = ? AND public_resource = FALSE
            """;

            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, resourceId);
                stmt.setInt(2, userId);
                return stmt.executeUpdate() > 0;
            }
        }
    }

    private ReservationResource getPrivateResourceById(int userId, int resourceId) throws SQLException {
        String sql = """
            SELECT id, name, category, location, description, slot_duration_minutes, active,
                   public_resource, owner_user_id
            FROM booking_resources
            WHERE id = ? AND owner_user_id = ? AND public_resource = FALSE
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapResource(rs);
            }
        }
    }

    private ReservationResource mapResource(ResultSet rs) throws SQLException {
        Integer ownerUserId = rs.getObject("owner_user_id") == null ? null : rs.getInt("owner_user_id");

        return new ReservationResource(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("location"),
                rs.getString("description"),
                rs.getInt("slot_duration_minutes"),
                rs.getBoolean("active"),
                rs.getBoolean("public_resource"),
                ownerUserId
        );
    }

    private void upsertPublicResource(Connection conn, String name, String category, String location,
                                      String description, int slotDuration, boolean active) throws SQLException {
        String selectSql = """
            SELECT id
            FROM booking_resources
            WHERE public_resource = TRUE AND name = ? AND location = ?
        """;

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, name);
            selectStmt.setString(2, location);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    String updateSql = """
                        UPDATE booking_resources
                        SET category = ?, description = ?, slot_duration_minutes = ?, active = TRUE, public_resource = TRUE
                        WHERE id = ?
                    """;

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, category);
                        updateStmt.setString(2, description);
                        updateStmt.setInt(3, slotDuration);
                        updateStmt.setInt(4, rs.getInt("id"));
                        updateStmt.executeUpdate();
                    }

                    return;
                }
            }
        }

        String insertSql = """
            INSERT INTO booking_resources
                (name, category, location, description, slot_duration_minutes, active, public_resource, owner_user_id)
            VALUES (?, ?, ?, ?, ?, ?, TRUE, NULL)
        """;

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, name);
            insertStmt.setString(2, category);
            insertStmt.setString(3, location);
            insertStmt.setString(4, description);
            insertStmt.setInt(5, slotDuration);
            insertStmt.setBoolean(6, active);
            insertStmt.executeUpdate();
        }
    }

    private boolean resourceHasBookings(Connection conn, int resourceId) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS total
            FROM bookings
            WHERE resource_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resourceId);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("total") > 0;
            }
        }
    }

    private void ensureColumnExists(Connection conn, Statement stmt, String tableName,
                                    String columnName, String alterSql) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            if (rs.next()) {
                return;
            }
        }

        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName.toUpperCase(), columnName.toUpperCase())) {
            if (rs.next()) {
                return;
            }
        }

        stmt.execute(alterSql);
    }

    private void dropIndexIfExists(Connection conn, Statement stmt, String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getIndexInfo(conn.getCatalog(), null, tableName, false, false)) {
            while (rs.next()) {
                String currentIndex = rs.getString("INDEX_NAME");

                if (indexName.equalsIgnoreCase(currentIndex)) {
                    stmt.execute("ALTER TABLE " + tableName + " DROP INDEX " + indexName);
                    return;
                }
            }
        }
    }
}
