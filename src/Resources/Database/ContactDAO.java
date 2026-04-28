package Resources.Database;

import Resources.Model.Contact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {

    public List<Contact> getContactsByUser(int userId) throws SQLException {

        List<Contact> contacts = new ArrayList<>();
        String sql = """
            SELECT id, contact_name, contact_phone, relation
            FROM contacts
            WHERE user_id = ?
            ORDER BY id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contacts.add(new Contact(
                            rs.getInt("id"),
                            rs.getString("contact_name"),
                            rs.getString("contact_phone"),
                            rs.getString("relation")
                    ));
                }
            }
        }

        return contacts;
    }

    public boolean addContact(int userId, String name, String phone, String relation) throws SQLException {

        String sql = """
            INSERT INTO contacts (user_id, contact_name, contact_phone, relation)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setString(3, phone);
            stmt.setString(4, relation);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteContact(int userId, int contactId) throws SQLException {

        String sql = """
            DELETE FROM contacts
            WHERE id = ? AND user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, contactId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }
}
