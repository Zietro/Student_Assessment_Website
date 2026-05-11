import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminCourseRepository {

    public static List<org.json.JSONObject> getAll() throws SQLException {
        List<org.json.JSONObject> list = new ArrayList<>();

        String sql = "SELECT CCRN, programID, coordinatorID, courseName, credits, courseDesc FROM COURSE";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("CCRN", rs.getInt("CCRN"));
                o.put("programID", rs.getInt("programID"));
                o.put("coordinatorID", rs.getInt("coordinatorID"));
                o.put("courseName", rs.getString("courseName"));
                o.put("credits", rs.getInt("credits"));
                o.put("courseDesc", rs.getString("courseDesc"));
                list.add(o);
            }
        }

        return list;
    }

    public static void insert(int ccrn,
                              int programID,
                              int coordinatorID,
                              String courseName,
                              int credits,
                              String courseDesc) throws SQLException {

        String sql = "INSERT INTO COURSE(CCRN, programID, coordinatorID, courseName, credits, courseDesc) VALUES(?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ccrn);
            ps.setInt(2, programID);
            ps.setInt(3, coordinatorID);
            ps.setString(4, courseName);
            ps.setInt(5, credits);
            ps.setString(6, courseDesc);
            ps.executeUpdate();
        }
    }

    public static boolean update(int ccrn,
                                 int programID,
                                 int coordinatorID,
                                 String courseName,
                                 int credits,
                                 String courseDesc) throws SQLException {

        String sql = "UPDATE COURSE SET programID=?, coordinatorID=?, courseName=?, credits=?, courseDesc=? WHERE CCRN=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programID);
            ps.setInt(2, coordinatorID);
            ps.setString(3, courseName);
            ps.setInt(4, credits);
            ps.setString(5, courseDesc);
            ps.setInt(6, ccrn);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean delete(int ccrn) throws SQLException {
        String sql = "DELETE FROM COURSE WHERE CCRN = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ccrn);
            return ps.executeUpdate() == 1;
        }
    }
}



