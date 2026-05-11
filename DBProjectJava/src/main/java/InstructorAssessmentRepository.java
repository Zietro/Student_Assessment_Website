import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstructorAssessmentRepository {

    public static List<InstructorAssessment> findByInstructor(String instructorName) throws SQLException {
        List<InstructorAssessment> list = new ArrayList<>();

        String sql = "SELECT ia.studentID, ia.instructorID, ia.instructorAssessmentDate, " +
                        "ia.rating1, ia.rating2, ia.rating3, ia.rating4, ia.recommendations, " +
                        "i.Fname, i.Lname " +
                        "FROM INSTRUCTOR_ASSESSMENT ia " +
                        "JOIN INSTRUCTOR i ON ia.instructorID = i.instructorID " +
                        "WHERE CONCAT(i.Fname, ' ', i.Lname) LIKE ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + instructorName + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    int studentID = rs.getInt("studentID");
                    int instrID = rs.getInt("instructorID");
                    LocalDate date = rs.getDate("instructorAssessmentDate").toLocalDate();
                    int rating1 = rs.getInt("rating1");
                    int rating2 = rs.getInt("rating2");
                    int rating3 = rs.getInt("rating3");
                    int rating4 = rs.getInt("rating4");
                    String recommendations = rs.getString("recommendations");
                    String Fname = rs.getString("Fname");
                    String Lname = rs.getString("Lname");
                    InstructorAssessment a = new InstructorAssessment(
                            studentID, instrID, date, rating1, rating2, rating3, rating4, recommendations, Fname, Lname
                    );

                    list.add(a);
                }
            }
        }

        return list;
    }

    public static boolean existsByStudentAndInstructor(int studentID, int instructorID) throws SQLException {
        String sql = "SELECT 1 FROM INSTRUCTOR_ASSESSMENT WHERE studentID = ? AND instructorID = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentID);
            ps.setInt(2, instructorID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void insert(int studentID,
                              int instructorID,
                              LocalDate instructorAssessmentDate,
                              int rating1,
                              int rating2,
                              int rating3,
                              int rating4,
                              String recommendations) throws SQLException {

        String sql = "INSERT INTO INSTRUCTOR_ASSESSMENT " +
                "(studentID, instructorID, instructorAssessmentDate, rating1, rating2, rating3, rating4, recommendations) " +
                "VALUES (?,?,?,?,?,?,?,?)";

        LocalDate date = (instructorAssessmentDate == null) ? LocalDate.now() : instructorAssessmentDate;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentID);
            ps.setInt(2, instructorID);
            ps.setDate(3, Date.valueOf(date));
            ps.setInt(4, rating1);
            ps.setInt(5, rating2);
            ps.setInt(6, rating3);
            ps.setInt(7, rating4);
            ps.setString(8, recommendations == null ? "" : recommendations);

            ps.executeUpdate();
        }
    }

    public static Set<Integer> findAssessedInstructorIdsByStudent(int studentID) throws SQLException {
        Set<Integer> instructorIds = new HashSet<>();

        String sql = "SELECT DISTINCT instructorID FROM INSTRUCTOR_ASSESSMENT WHERE studentID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    instructorIds.add(rs.getInt("instructorID"));
                }
            }
        }

        return instructorIds;
    }
}