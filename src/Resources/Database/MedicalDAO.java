package Resources.Database;

import Resources.Model.MedicalInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MedicalDAO {

    public boolean saveMedicalInfo(int userId, String allergies, String conditions, String medications) throws SQLException {

        String sql = """
            INSERT INTO medical_info (user_id, allergies, conditions, medications)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                allergies = VALUES(allergies),
                conditions = VALUES(conditions),
                medications = VALUES(medications)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, allergies);
            stmt.setString(3, conditions);
            stmt.setString(4, medications);

            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    public MedicalInfo getMedicalInfoByUser(int userId) throws SQLException {

        String sql = """
            SELECT user_id, allergies, conditions, medications
            FROM medical_info
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new MedicalInfo(
                        rs.getInt("user_id"),
                        rs.getString("allergies"),
                        rs.getString("conditions"),
                        rs.getString("medications")
                );
            }
        }
    }
}
