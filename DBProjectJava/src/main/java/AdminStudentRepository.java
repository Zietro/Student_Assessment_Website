import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminStudentRepository {

    public static List<org.json.JSONObject> getAll() throws SQLException {
        List<org.json.JSONObject> list = new ArrayList<>();

        String sql = "SELECT studentID, Fname, Lname, email, phoneNB, address, username, password_hash FROM STUDENT";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("studentID", rs.getInt("studentID"));
                o.put("Fname", rs.getString("Fname"));
                o.put("Lname", rs.getString("Lname"));
                o.put("email", rs.getString("email"));
                o.put("phoneNB", rs.getString("phoneNB"));
                o.put("address", rs.getString("address"));
                o.put("username", rs.getString("username"));
                o.put("password_hash", rs.getString("password_hash"));
                list.add(o);
            }
        }

        return list;
    }

    public static void insert(int studentID,
                              String Fname,
                              String Lname,
                              String email,
                              String phoneNB,
                              String address,
                              String username,
                              String passwordHash) throws SQLException {

        String sql = "INSERT INTO STUDENT(studentID, Fname, Lname, email, phoneNB, address, username, password_hash) " +
                "VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentID);
            ps.setString(2, Fname);
            ps.setString(3, Lname);
            ps.setString(4, email);
            ps.setString(5, phoneNB);
            ps.setString(6, address);
            ps.setString(7, username);
            ps.setString(8, passwordHash);
            ps.executeUpdate();
        }
    }

    public static boolean update(int studentID,
                                 String Fname,
                                 String Lname,
                                 String email,
                                 String phoneNB,
                                 String address,
                                 String username,
                                 String passwordHash) throws SQLException {

        // 1. Determine if we are updating the password
    boolean updatePassword = (passwordHash != null && !passwordHash.trim().isEmpty());

    // 2. Build the SQL string dynamically
    String sql = "UPDATE STUDENT SET Fname=?, Lname=?, email=?, phoneNB=?, address=?, username=?";
    if (updatePassword) {
        sql += ", password_hash=?";
    }
    sql += " WHERE studentID=?";

    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, Fname);
        ps.setString(2, Lname);
        ps.setString(3, email);
        ps.setString(4, phoneNB);
        ps.setString(5, address);
        ps.setString(6, username);

        if (updatePassword) {
            ps.setString(7, passwordHash);
            ps.setInt(8, studentID); // studentID is now the 8th parameter
        } else {
            ps.setInt(7, studentID); // studentID is the 7th parameter
        }

        return ps.executeUpdate() == 1;
    }
    }

    public static boolean delete(int studentID) throws SQLException {
        String sql = "DELETE FROM STUDENT WHERE studentID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentID);
            return ps.executeUpdate() == 1;
        }
    }
}



