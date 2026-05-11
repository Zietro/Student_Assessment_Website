import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/courses")
public class CoursesServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Course> courses;
        try {
            courses = CourseRepository.getAllCourses();
        } catch (Exception e) {
            ServletUtil.sendErrorResponse(response, 500, "Error loading courses from database");
            return;
        }
        
        // Build JSON response
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            
            json.append("{")
                .append("\"CCRN\":").append(c.getCCRN()).append(",")
                .append("\"programID\":").append(c.getProgramID()).append(",")
                .append("\"instructorName\":\"").append(escapeJson(c.getFName() + " " + c.getLName())).append("\",")
                .append("\"courseName\":\"").append(escapeJson(c.getCourseName())).append("\",")
                .append("\"credits\":").append(c.getCredits()).append(",")
                .append("\"courseDesc\":\"").append(escapeJson(c.getCourseDesc())).append("\"")
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



