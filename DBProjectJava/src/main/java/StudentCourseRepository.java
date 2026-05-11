import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentCourseRepository {

    public static List<Course> findCoursesByStudentID(int studentID) throws SQLException {
        List<Course> courses = new ArrayList<>();

        String sql =
                "SELECT c.CCRN, c.programID, i.Fname, i.Lname, c.courseName, c.credits, c.courseDesc " +
                        "FROM STUDENT_REGISTRATION sr " +
                        "JOIN Registration r ON sr.registrationID = r.registrationID " +
                        "JOIN COURSE c ON r.CCRN = c.CCRN " +
                        "JOIN INSTRUCTOR i ON r.instructorID = i.instructorID " +
                        "WHERE sr.studentID = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int CCRN = rs.getInt("CCRN");
                    int programID = rs.getInt("programID");
                    String Fname = rs.getString("Fname");
                    String Lname = rs.getString("Lname");
                    String courseName = rs.getString("courseName");
                    int credits = rs.getInt("credits");
                    String courseDesc = rs.getString("courseDesc");

                    Course c = new Course(CCRN, programID, Fname, Lname, courseName, credits, courseDesc);
                    courses.add(c);
                }
            }
        }

        return courses;
    }

    public static List<Course> findCoursesByStudentUsername(String username) throws SQLException {
        List<Course> courses = new ArrayList<>();
    
        String sql = "SELECT c.CCRN, c.programID, i.Fname, i.Lname, c.courseName, c.credits, c.courseDesc " +
                     "FROM STUDENT_REGISTRATION sr " +
                     "JOIN Registration r ON sr.registrationID = r.registrationID " +
                     "JOIN COURSE c ON r.CCRN = c.CCRN " +
                     "JOIN INSTRUCTOR i ON r.instructorID = i.instructorID " +
                     "JOIN STUDENT s ON sr.studentID = s.studentID " +
                     "WHERE s.username = ?";
    
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
    
            ps.setString(1, username);
    
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int CCRN = rs.getInt("CCRN");
                    int programID = rs.getInt("programID");
                    String Fname = rs.getString("Fname");
                    String Lname = rs.getString("Lname");
                    String courseName = rs.getString("courseName");
                    int credits = rs.getInt("credits");
                    String courseDesc = rs.getString("courseDesc");
    
                    Course c = new Course(CCRN, programID, Fname, Lname, courseName, credits, courseDesc);
                    courses.add(c);
                }
            }
        }
    
        return courses;
    }

    /**
     * Returns the instructorID that is associated with a (student, course) pair in Registration/studentRegistration.
     * This is used when submitting an instructor assessment from the UI (UI only knows username + course CCRN).
     */
    public static Integer findInstructorIdForStudentUsernameAndCCRN(String username, int ccrn) throws SQLException {
        if (username == null || username.isBlank()) return null;
        if (ccrn <= 0) return null;

        String sql = "SELECT r.instructorID " +
                "FROM STUDENT_REGISTRATION sr " +
                "JOIN Registration r ON sr.registrationID = r.registrationID " +
                "JOIN STUDENT s ON sr.studentID = s.studentID " +
                "WHERE s.username = ? AND r.CCRN = ? " +
                "LIMIT 1";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setInt(2, ccrn);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("instructorID");
            }
        }
    }

    /**
     * Returns a map from CCRN -> instructorID for the given student username, using Registration/studentRegistration.
     * Used by the UI to mark "Instructor Evaluation" as done per course (since the assessment table is keyed by instructorID).
     */
    public static Map<Integer, Integer> findCourseInstructorMapByStudentUsername(String username) throws SQLException {
        Map<Integer, Integer> map = new HashMap<>();
        if (username == null || username.isBlank()) return map;

        String sql = "SELECT r.CCRN, r.instructorID " +
                "FROM STUDENT_REGISTRATION sr " +
                "JOIN Registration r ON sr.registrationID = r.registrationID " +
                "JOIN STUDENT s ON sr.studentID = s.studentID " +
                "WHERE s.username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("CCRN"), rs.getInt("instructorID"));
                }
            }
        }

        return map;
    }
    
}