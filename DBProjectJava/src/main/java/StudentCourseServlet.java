import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Set;

@WebServlet("/student-course")
public class StudentCourseServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        
        if (username == null || username.isBlank()) {
            ServletUtil.sendErrorResponse(response, 400, "Missing username parameter");
            return;
        }
        
        List<Course> courses;
        try {
            courses = StudentCourseRepository.findCoursesByStudentUsername(username);
        } catch (Exception e) {
            ServletUtil.sendErrorResponse(response, 500, "Database error");
            return;
        }

        // DB-backed "Done" flags
        Integer studentId = null;
        Map<Integer, Integer> courseInstructorMap = null;
        Set<Integer> assessedCourseCcrns = null;
        Set<Integer> assessedInstructorIds = null;
        try {
            studentId = StudentRepository.findStudentIdByUsername(username);
            if (studentId != null) {
                courseInstructorMap = StudentCourseRepository.findCourseInstructorMapByStudentUsername(username);
                assessedCourseCcrns = CourseAssessmentRepository.findAssessedCourseCcrnsByStudent(studentId);
                assessedInstructorIds = InstructorAssessmentRepository.findAssessedInstructorIdsByStudent(studentId);
            }
        } catch (Exception ignored) {}
        
        // Build JSON response
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            boolean courseDone = (studentId != null && assessedCourseCcrns != null && assessedCourseCcrns.contains(c.getCCRN()));
            boolean instructorDone = false;
            if (studentId != null && courseInstructorMap != null && assessedInstructorIds != null) {
                Integer instrId = courseInstructorMap.get(c.getCCRN());
                instructorDone = (instrId != null && assessedInstructorIds.contains(instrId));
            }
            
            json.append("{")
                .append("\"CCRN\":").append(c.getCCRN()).append(",")
                .append("\"programID\":").append(c.getProgramID()).append(",")
                .append("\"instructorName\":\"").append(escapeJson(c.getFName() + " " + c.getLName())).append("\",")                .append("\"courseName\":\"").append(escapeJson(c.getCourseName())).append("\",")
                .append("\"credits\":").append(c.getCredits()).append(",")
                .append("\"courseDesc\":\"").append(escapeJson(c.getCourseDesc())).append("\",")
                .append("\"courseAssessmentDone\":").append(courseDone).append(",")
                .append("\"instructorAssessmentDone\":").append(instructorDone)
                .append("}");
            
            if (i < courses.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        
        ServletUtil.sendJsonResponse(response, 200, json.toString());
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        ServletUtil.setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    private String escapeJson(String str) {
        return JsonUtil.escapeJson(str);
    }
}



