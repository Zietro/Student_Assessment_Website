import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentRepository {

    public static Integer findStudentIdByUsername(String username) throws SQLException {
        if (username == null || username.isBlank()) return null;

        String sql = "SELECT studentID FROM STUDENT WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("studentID");
            }
        }
    }
}



