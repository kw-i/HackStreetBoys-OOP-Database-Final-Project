package roadwatch;

import java.sql.*;

// AdminDAO – Data Access Object for admin authentication.

public class AdminDAO {

    //Authenticates an admin by username + password.
    // Admin object if credentials match, null otherwise.
     
    public Admin authenticate(String username, String password) {
        String sql = "SELECT id, username, full_name FROM admins "
                   + "WHERE username = ? AND password = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Admin(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("full_name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
