package roadwatch;

import java.sql.*;


 //CitizenDAO – Registration and authentication for citizen accounts.

public class CitizenDAO {

    /** Registers a new citizen. Returns false if email already taken. */
    public boolean register(String fullName, String email,
                            String contactNumber, String password) {
        String sql = "INSERT INTO citizens (full_name, email, contact_number, password) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, contactNumber.trim());
            ps.setString(4, password);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // duplicate email
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Returns Citizen on success, null on bad credentials. */
    public Citizen login(String email, String password) {
        String sql = "SELECT id, full_name, email, contact_number FROM citizens "
                   + "WHERE email = ? AND password = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Citizen(
                rs.getInt("id"), rs.getString("full_name"),
                rs.getString("email"), rs.getString("contact_number")
            );
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Returns true if an account with this email already exists.
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM citizens WHERE email = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
