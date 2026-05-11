import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    public static List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();

        String sql =
                "SELECT DISTINCT c.CCRN, c.programID, i.Fname, i.Lname, c.courseName, c.credits, c.courseDesc " +
                        "FROM STUDENT_REGISTRATION sr " +
                        "JOIN Registration r ON sr.registrationID = r.registrationID " +
                        "JOIN COURSE c ON r.CCRN = c.CCRN " +
                        "JOIN INSTRUCTOR i ON r.instructorID = i.instructorID";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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

        return courses;
    }
}