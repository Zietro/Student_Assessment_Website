import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorRepository {

    public static List<Instructor> getAllInstructors() throws SQLException {
        List<Instructor> instructors = new ArrayList<>();

        String sql = "SELECT instructorID, Fname, Lname, email, phoneNB, address, departmentID FROM INSTRUCTOR";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int instructorID = rs.getInt("instructorID");
                String Fname = rs.getString("Fname");
                String Lname = rs.getString("Lname");
                String email = rs.getString("email");
                String phoneNB = rs.getString("phoneNB");
                String address = rs.getString("address");
                int departmentID = rs.getInt("departmentID");

                Instructor inst = new Instructor(
                        instructorID, Fname, Lname, email, phoneNB, address, departmentID
                );
                instructors.add(inst);
            }
        }

        return instructors;
    }

    public static List<Instructor> findByName(String searchTerm) throws SQLException {
        List<Instructor> instructors = new ArrayList<>();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllInstructors(); // fallback
        }
    
        String sql = "SELECT instructorID, Fname, Lname, email, phoneNB, address, departmentID " +
                     "FROM INSTRUCTOR " +
                     "WHERE Fname LIKE ? OR Lname LIKE ?";
    
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            String likePattern = "%" + searchTerm + "%";
            stmt.setString(1, likePattern);
            stmt.setString(2, likePattern);
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int instructorID = rs.getInt("instructorID");
                    String Fname = rs.getString("Fname");
                    String Lname = rs.getString("Lname");
                    String email = rs.getString("email");
                    String phoneNB = rs.getString("phoneNB");
                    String address = rs.getString("address");
                    int departmentID = rs.getInt("departmentID");
    
                    Instructor inst = new Instructor(
                            instructorID, Fname, Lname, email, phoneNB, address, departmentID
                    );
                    instructors.add(inst);
                }
            }
        }
    
        return instructors;
    }
    


}