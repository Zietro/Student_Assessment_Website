import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/course-assessment")
public class CourseAssessmentServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String ccrnParam = request.getParameter("ccrn");
        Integer ccrn = null;
        
        if (ccrnParam != null) {
            try {
                ccrn = Integer.parseInt(ccrnParam);
            } catch (NumberFormatException e) {
                ServletUtil.sendErrorResponse(response, 400, "Invalid ccrn parameter");
                return;
            }
        }
        
        if (ccrn == null) {
            ServletUtil.sendErrorResponse(response, 400, "Missing ccrn parameter");
            return;
        }
        
        List<CourseAssessment> assessments;
        try {
            assessments = CourseAssessmentRepository.findByCourse(ccrn);
        } catch (Exception e) {
            ServletUtil.sendErrorResponse(response, 500, "Database error");
            return;
        }
        
        // Build JSON response
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < assessments.size(); i++) {
            CourseAssessment a = assessments.get(i);
            
            json.append("{")
                .append("\"studentID\":").append(a.getStudentID()).append(",")
                .append("\"CCRN\":").append(a.getCCRN()).append(",")
                .append("\"courseAssessmentDate\":\"").append(a.getCourseAssessmentDate()).append("\",")
                .append("\"rating1\":").append(a.getRating1()).append(",")
                .append("\"rating2\":").append(a.getRating2()).append(",")
                .append("\"rating3\":").append(a.getRating3()).append(",")
                .append("\"rating4\":").append(a.getRating4()).append(",")
                .append("\"recommendations\":\"").append(escapeJson(a.getRecommendations())).append("\"")
                .append("}");
            
            if (i < assessments.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        
        ServletUtil.sendJsonResponse(response, 200, json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String ccrnParam = request.getParameter("ccrn");

        if (username == null || username.isBlank() || ccrnParam == null || ccrnParam.isBlank()) {
            ServletUtil.sendErrorResponse(response, 400, "Missing username or ccrn parameter");
            return;
        }

        int ccrn;
        try {
            ccrn = Integer.parseInt(ccrnParam);
        } catch (NumberFormatException e) {
            ServletUtil.sendErrorResponse(response, 400, "Invalid ccrn parameter");
            return;
        }

        int rating1 = parseIntOrZero(request.getParameter("rating1"));
        int rating2 = parseIntOrZero(request.getParameter("rating2"));
        int rating3 = parseIntOrZero(request.getParameter("rating3"));
        int rating4 = parseIntOrZero(request.getParameter("rating4"));
        String recommendations = request.getParameter("recommendations");

        try {
            Integer studentId = StudentRepository.findStudentIdByUsername(username);
            if (studentId == null) {
                ServletUtil.sendErrorResponse(response, 404, "Unknown username");
                return;
            }

            if (CourseAssessmentRepository.existsByStudentAndCourse(studentId, ccrn)) {
                ServletUtil.sendErrorResponse(response, 409, "Course assessment already submitted");
                return;
            }

            CourseAssessmentRepository.insert(studentId, ccrn, null, rating1, rating2, rating3, rating4, recommendations);
            ServletUtil.sendJsonResponse(response, 201, "{\"success\":true}");
        } catch (Exception e) {
            ServletUtil.sendErrorResponse(response, 500, "Database error");
        }
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

    private int parseIntOrZero(String s) {
        if (s == null) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}



