import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationRepository {

    public static List<org.json.JSONObject> getAll() throws SQLException {
        List<org.json.JSONObject> list = new ArrayList<>();

        String sql = "SELECT r.registrationID, r.CCRN, r.instructorID, " +
                     "i.Fname, i.Lname, c.courseName " +
                     "FROM Registration r " +
                     "JOIN INSTRUCTOR i ON r.instructorID = i.instructorID " +
                     "JOIN COURSE c ON r.CCRN = c.CCRN " +
                     "ORDER BY r.registrationID";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("registrationID", rs.getInt("registrationID"));
                o.put("CCRN", rs.getInt("CCRN"));
                o.put("instructorID", rs.getInt("instructorID"));
                o.put("instructorName", rs.getString("Fname") + " " + rs.getString("Lname"));
                o.put("courseName", rs.getString("courseName"));
                list.add(o);
            }
        }

        return list;
    }

    public static void insert(int CCRN, int instructorID) throws SQLException {
        String sql = "INSERT INTO Registration(CCRN, instructorID) VALUES(?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, CCRN);
            ps.setInt(2, instructorID);
            ps.executeUpdate();
        }
    }

    public static boolean delete(int registrationID) throws SQLException {
        // Database has ON DELETE CASCADE, so related student_registrations will be automatically deleted
        String sql = "DELETE FROM Registration WHERE registrationID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationID);
            return ps.executeUpdate() == 1;
        }
    }
}

