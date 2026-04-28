package Resources.Database;

import Resources.Emergency.EmergencyData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmergencyDAO {

    public boolean createEmergency(EmergencyData emergency) throws SQLException {

        String sql = "INSERT INTO emergencies (user_id, type, description) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, emergency.getUserId());
            stmt.setString(2, emergency.getType());
            stmt.setString(3, emergency.getDescription());

            return stmt.executeUpdate() > 0;
        }
    }

    public List<EmergencyData> getByUser(int userId) throws SQLException {

        String sql = "SELECT * FROM emergencies WHERE user_id = ?";

        List<EmergencyData> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new EmergencyData(
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("description")
                ));
            }
        }

        return list;
    }
}
