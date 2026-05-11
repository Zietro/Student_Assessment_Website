import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminInstructorRepository {

    public static List<org.json.JSONObject> getAll() throws SQLException {
        List<org.json.JSONObject> list = new ArrayList<>();

        String sql = "SELECT instructorID, Fname, Lname, email, phoneNB, address, departmentID FROM INSTRUCTOR";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("instructorID", rs.getInt("instructorID"));
                o.put("Fname", rs.getString("Fname"));
                o.put("Lname", rs.getString("Lname"));
                o.put("email", rs.getString("email"));
                o.put("phoneNB", rs.getString("phoneNB"));
                o.put("address", rs.getString("address"));
                o.put("departmentID", rs.getInt("departmentID"));
                list.add(o);
            }
        }

        return list;
    }

    public static void insert(int instructorID,
                              String Fname,
                              String Lname,
                              String email,
                              String phoneNB,
                              String address,
                              int departmentID) throws SQLException {

        String sql = "INSERT INTO INSTRUCTOR(instructorID, Fname, Lname, email, phoneNB, address, departmentID) " +
                "VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, instructorID);
            ps.setString(2, Fname);
            ps.setString(3, Lname);
            ps.setString(4, email);
            ps.setString(5, phoneNB);
            ps.setString(6, address);
            ps.setInt(7, departmentID);
            ps.executeUpdate();
        }
    }

    public static boolean update(int instructorID,
                                 String Fname,
                                 String Lname,
                                 String email,
                                 String phoneNB,
                                 String address,
                                 int departmentID) throws SQLException {

        String sql = "UPDATE INSTRUCTOR SET Fname=?, Lname=?, email=?, phoneNB=?, address=?, departmentID=? " +
                "WHERE instructorID=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Fname);
            ps.setString(2, Lname);
            ps.setString(3, email);
            ps.setString(4, phoneNB);
            ps.setString(5, address);
            ps.setInt(6, departmentID);
            ps.setInt(7, instructorID);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean delete(int instructorID) throws SQLException {
        String sql = "DELETE FROM INSTRUCTOR WHERE instructorID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorID);
            return ps.executeUpdate() == 1;
        }
    }

    public static List<org.json.JSONObject> getAllCoordinators() throws SQLException {
        List<org.json.JSONObject> list = new ArrayList<>();

        String sql = "SELECT i.instructorID, i.Fname, i.Lname, i.email, i.phoneNB, i.address, i.departmentID " +
                     "FROM INSTRUCTOR i " +
                     "INNER JOIN COORDINATOR c ON i.instructorID = c.coordinatorID " +
                     "ORDER BY i.instructorID";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("instructorID", rs.getInt("instructorID"));
                o.put("coordinatorID", rs.getInt("instructorID")); // Same as instructorID
                o.put("Fname", rs.getString("Fname"));
                o.put("Lname", rs.getString("Lname"));
                o.put("email", rs.getString("email"));
                o.put("phoneNB", rs.getString("phoneNB"));
                o.put("address", rs.getString("address"));
                o.put("departmentID", rs.getInt("departmentID"));
                list.add(o);
            }
        }

        return list;
    }
}



