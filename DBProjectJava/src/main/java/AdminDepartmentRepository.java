import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminDepartmentRepository {

    public static List<org.json.JSONObject> getAll() throws SQLException {
        List<org.json.JSONObject> list = new ArrayList<>();

        String sql = "SELECT departmentID, departmentName FROM DEPARTMENT ORDER BY departmentID";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("departmentID", rs.getInt("departmentID"));
                o.put("departmentName", rs.getString("departmentName"));
                list.add(o);
            }
        }

        return list;
    }
}
