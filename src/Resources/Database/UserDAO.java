package Resources.Database;

import Resources.User.UserData;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    public UserData login(String input, String password) throws SQLException {

        String sql = """
            SELECT u.id, u.name, u.phone, u.role, u.email, u.password,
                   m.allergies, m.conditions, m.medications
            FROM users u
            LEFT JOIN medical_info m ON u.id = m.user_id
            WHERE (u.name = ? OR u.email = ?) AND u.password = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, input);
            stmt.setString(2, input);
            stmt.setString(3, password);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }

            return new UserData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("allergies"),
                    rs.getString("conditions"),
                    rs.getString("medications")
            );
        }
    }

    public UserData registerUser(UserData user) throws SQLException {

        String sql = "INSERT INTO users (name, phone, role, email, password) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getNombre());
            stmt.setString(2, user.getPhone());
            stmt.setString(3, "user");
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPassword());

            int rows = stmt.executeUpdate();

            if (rows <= 0) {
                return null;
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se pudo obtener el id del usuario registrado");
                }

                return new UserData(
                        keys.getInt(1),
                        user.getNombre(),
                        user.getPhone(),
                        "user",
                        user.getEmail(),
                        user.getPassword(),
                        null,
                        null,
                        null
                );
            }
        }
    }

    public boolean verifyPassword(int userId, String password) throws SQLException {

        String sql = """
            SELECT 1
            FROM users
            WHERE id = ? AND password = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean emailExistsForOtherUser(int userId, String email) throws SQLException {

        String sql = """
            SELECT 1
            FROM users
            WHERE email = ? AND id <> ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public UserData updateAccount(int userId, String name, String email, String password) throws SQLException {

        String updateSql = """
            UPDATE users
            SET name = ?, email = ?, password = ?
            WHERE id = ?
        """;

        String selectSql = """
            SELECT u.id, u.name, u.phone, u.role, u.email, u.password,
                   m.allergies, m.conditions, m.medications
            FROM users u
            LEFT JOIN medical_info m ON u.id = m.user_id
            WHERE u.id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            updateStmt.setString(1, name);
            updateStmt.setString(2, email);
            updateStmt.setString(3, password);
            updateStmt.setInt(4, userId);

            if (updateStmt.executeUpdate() <= 0) {
                return null;
            }

            selectStmt.setInt(1, userId);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new UserData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("allergies"),
                        rs.getString("conditions"),
                        rs.getString("medications")
                );
            }
        }
    }

    public boolean deleteUserAccount(int userId) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                deleteIfTableExists(conn, "bookings", "DELETE FROM bookings WHERE user_id = ?", userId);
                deleteIfTableExists(conn, "emergencies", "DELETE FROM emergencies WHERE user_id = ?", userId);
                deleteIfTableExists(conn, "contacts", "DELETE FROM contacts WHERE user_id = ?", userId);
                deleteIfTableExists(conn, "medical_info", "DELETE FROM medical_info WHERE user_id = ?", userId);

                int deletedUsers = deleteRows(conn, "DELETE FROM users WHERE id = ?", userId);
                conn.commit();
                return deletedUsers > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void deleteIfTableExists(Connection conn, String tableName, String sql, int userId) throws SQLException {
        if (!tableExists(conn, tableName)) {
            return;
        }

        deleteRows(conn, sql, userId);
    }

    private int deleteRows(Connection conn, String sql, int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, tableName, null)) {
            if (rs.next()) {
                return true;
            }
        }

        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, tableName.toUpperCase(), null)) {
            return rs.next();
        }
    }
}
