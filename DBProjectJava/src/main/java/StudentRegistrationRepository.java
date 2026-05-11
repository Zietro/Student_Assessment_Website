import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentRegistrationRepository {

    public static List<org.json.JSONObject> getAll() throws SQLException {
        List<org.json.JSONObject> list = new ArrayList<>();

        String sql = "SELECT sr.studentID, sr.registrationID, " +
                     "s.Fname, s.Lname, s.username, " +
                     "r.CCRN, c.courseName, " +
                     "i.instructorID, i.Fname as instructorFname, i.Lname as instructorLname " +
                     "FROM STUDENT_REGISTRATION sr " +
                     "JOIN STUDENT s ON sr.studentID = s.studentID " +
                     "JOIN Registration r ON sr.registrationID = r.registrationID " +
                     "JOIN COURSE c ON r.CCRN = c.CCRN " +
                     "JOIN INSTRUCTOR i ON r.instructorID = i.instructorID " +
                     "ORDER BY sr.studentID, sr.registrationID";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("studentID", rs.getInt("studentID"));
                o.put("registrationID", rs.getInt("registrationID"));
                o.put("studentName", rs.getString("Fname") + " " + rs.getString("Lname"));
                o.put("username", rs.getString("username"));
                o.put("CCRN", rs.getInt("CCRN"));
                o.put("courseName", rs.getString("courseName"));
                o.put("instructorID", rs.getInt("instructorID"));
                o.put("instructorName", rs.getString("instructorFname") + " " + rs.getString("instructorLname"));
                list.add(o);
            }
        }

        return list;
    }

    public static void insert(int studentID, int registrationID) throws SQLException {
        String sql = "INSERT INTO STUDENT_REGISTRATION(studentID, registrationID) VALUES(?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentID);
            ps.setInt(2, registrationID);
            ps.executeUpdate();
        }
    }

    public static boolean delete(int studentID, int registrationID) throws SQLException {
        String sql = "DELETE FROM STUDENT_REGISTRATION WHERE studentID = ? AND registrationID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentID);
            ps.setInt(2, registrationID);
            return ps.executeUpdate() == 1;
        }
    }
}

