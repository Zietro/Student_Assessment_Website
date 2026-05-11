import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CourseAssessmentRepository {

    public static List<CourseAssessment> findByCourse(int ccrn) throws SQLException {
        List<CourseAssessment> list = new ArrayList<>();

        String sql = "SELECT studentID, CCRN, courseAssessmentDate, rating1, rating2, rating3, rating4, recommendations " +
                "FROM COURSE_ASSESSMENT WHERE CCRN = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ccrn);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentID = rs.getInt("studentID");
                    int CCRN = rs.getInt("CCRN");
                    LocalDate date = rs.getDate("courseAssessmentDate").toLocalDate();
                    int rating1 = rs.getInt("rating1");
                    int rating2 = rs.getInt("rating2");
                    int rating3 = rs.getInt("rating3");
                    int rating4 = rs.getInt("rating4");
                    String recommendations = rs.getString("recommendations");

                    CourseAssessment ca = new CourseAssessment(
                            studentID, CCRN, date, rating1, rating2, rating3, rating4, recommendations
                    );
                    list.add(ca);
                }
            }
        }

        return list;
    }

    public static boolean existsByStudentAndCourse(int studentID, int ccrn) throws SQLException {
        String sql = "SELECT 1 FROM COURSE_ASSESSMENT WHERE studentID = ? AND CCRN = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentID);
            ps.setInt(2, ccrn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void insert(int studentID,
                              int ccrn,
                              LocalDate courseAssessmentDate,
                              int rating1,
                              int rating2,
                              int rating3,
                              int rating4,
                              String recommendations) throws SQLException {

        String sql = "INSERT INTO COURSE_ASSESSMENT " +
                "(studentID, CCRN, courseAssessmentDate, rating1, rating2, rating3, rating4, recommendations) " +
                "VALUES (?,?,?,?,?,?,?,?)";

        LocalDate date = (courseAssessmentDate == null) ? LocalDate.now() : courseAssessmentDate;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentID);
            ps.setInt(2, ccrn);
            ps.setDate(3, Date.valueOf(date));
            ps.setInt(4, rating1);
            ps.setInt(5, rating2);
            ps.setInt(6, rating3);
            ps.setInt(7, rating4);
            ps.setString(8, recommendations == null ? "" : recommendations);

            ps.executeUpdate();
        }
    }

    public static Set<Integer> findAssessedCourseCcrnsByStudent(int studentID) throws SQLException {
        Set<Integer> ccrns = new HashSet<>();

        String sql = "SELECT DISTINCT CCRN FROM COURSE_ASSESSMENT WHERE studentID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ccrns.add(rs.getInt("CCRN"));
                }
            }
        }

        return ccrns;
    }
}