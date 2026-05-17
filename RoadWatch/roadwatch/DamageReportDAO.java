package roadwatch;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


 //DamageReportDAO – CRUD for damage reports.
 //v2: citizen_id linkage + optional photo BLOB.
 //Photo is NOT fetched in list queries (performance); only fetched in getById().
 
public class DamageReportDAO {

    // INSERT 

    /** Guest insert (no account, no photo). */
    public String insert(DamageReport r) {
        return insertFull(r, null, null);
    }

    
     //Full insert — links a citizen account and/or attaches a photo.
     //@param citizenId  null = guest
     //@param photoBytes null = no photo
     //@return generated report code, or null on failure
     

    public String insertFull(DamageReport r, Integer citizenId, byte[] photoBytes) {
        String code = generateReportCode();
        String sql  = "INSERT INTO damage_reports "
                    + "(report_code, citizen_id, reporter_name, contact_number, "
                    + " damage_type, location, severity, description, photo, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending')";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code);
            if (citizenId != null) ps.setInt(2, citizenId);
            else                   ps.setNull(2, Types.INTEGER);
            ps.setString(3, r.getReporterName());
            ps.setString(4, r.getContactNumber());
            ps.setString(5, r.getDamageType());
            ps.setString(6, r.getLocation());
            ps.setString(7, r.getSeverity());
            ps.setString(8, r.getDescription());
            if (photoBytes != null) ps.setBytes(9, photoBytes);
            else                    ps.setNull(9, Types.BLOB);
            ps.executeUpdate();
            return code;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // READ 

    public List<DamageReport> getAll() { return getByFilter("ALL"); }

    public List<DamageReport> getByFilter(String statusFilter) {
        List<DamageReport> list = new ArrayList<>();
        String sql = "SELECT id, report_code, citizen_id, reporter_name, contact_number, "
                   + "damage_type, location, severity, description, status, "
                   + "admin_remarks, submitted_at, updated_at "
                   + "FROM damage_reports"
                   + ("ALL".equals(statusFilter) ? "" : " WHERE status = '" + statusFilter + "'")
                   + " ORDER BY submitted_at DESC";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRowNoPhoto(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Returns only reports submitted by a specific citizen (for their dashboard). Photo NOT loaded. */
    public List<DamageReport> getByCitizenId(int citizenId) {
        List<DamageReport> list = new ArrayList<>();
        String sql = "SELECT id, report_code, citizen_id, reporter_name, contact_number, "
                   + "damage_type, location, severity, description, status, "
                   + "admin_remarks, submitted_at, updated_at "
                   + "FROM damage_reports WHERE citizen_id = ? ORDER BY submitted_at DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, citizenId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRowNoPhoto(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Full report by ID — includes photo BLOB. */
    public DamageReport getById(int id) {
        String sql = "SELECT * FROM damage_reports WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRowFull(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    //  UPDATE 

    public boolean updateStatus(int id, String status, String adminRemarks) {
        String sql = "UPDATE damage_reports SET status = ?, admin_remarks = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, adminRemarks);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // DELETE 

    public boolean delete(int id) {
        String sql = "DELETE FROM damage_reports WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // DUPLICATE CHECK 

    public boolean isDuplicate(String location, String damageType) {
        String sql = "SELECT COUNT(*) FROM damage_reports "
                   + "WHERE LOWER(location) = LOWER(?) AND damage_type = ? "
                   + "AND submitted_at >= NOW() - INTERVAL 48 HOUR";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, location.trim());
            ps.setString(2, damageType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // SUMMARY 

    public int[] getSummaryCounts() {
        int[] c = new int[4];
        String sql = "SELECT COUNT(*) total, "
                   + "SUM(status='Pending') pending, "
                   + "SUM(status='In Progress') in_progress, "
                   + "SUM(status='Resolved') resolved "
                   + "FROM damage_reports";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                c[0] = rs.getInt("total");  c[1] = rs.getInt("pending");
                c[2] = rs.getInt("in_progress"); c[3] = rs.getInt("resolved");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return c;
    }

    public List<String[]> getSummaryByType() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT damage_type, COUNT(*) cnt FROM damage_reports "
                   + "GROUP BY damage_type ORDER BY cnt DESC";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new String[]{ rs.getString("damage_type"), rs.getString("cnt") });
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // HELPERS 

    private DamageReport mapRowNoPhoto(ResultSet rs) throws SQLException {
        DamageReport r = new DamageReport();
        r.setId(rs.getInt("id"));
        r.setReportCode(rs.getString("report_code"));
        r.setCitizenId(rs.getInt("citizen_id"));
        r.setReporterName(rs.getString("reporter_name"));
        r.setContactNumber(rs.getString("contact_number"));
        r.setDamageType(rs.getString("damage_type"));
        r.setLocation(rs.getString("location"));
        r.setSeverity(rs.getString("severity"));
        r.setDescription(rs.getString("description"));
        r.setStatus(rs.getString("status"));
        r.setAdminRemarks(rs.getString("admin_remarks"));
        r.setSubmittedAt(rs.getTimestamp("submitted_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }

    private DamageReport mapRowFull(ResultSet rs) throws SQLException {
        DamageReport r = mapRowNoPhoto(rs);
        r.setPhoto(rs.getBytes("photo"));
        return r;
    }

    private String generateReportCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = 1;
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM damage_reports WHERE report_code LIKE 'RPT-" + date + "-%'")) {
            if (rs.next()) seq = rs.getInt(1) + 1;
        } catch (SQLException e) { e.printStackTrace(); }
        return String.format("RPT-%s-%04d", date, seq);
    }
}
