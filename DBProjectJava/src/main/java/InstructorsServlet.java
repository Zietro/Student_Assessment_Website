import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/instructors")
public class InstructorsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {


        String search = request.getParameter("instructorName");
        List<Instructor> instructors;
        
        
        try {
            if (search != null && !search.isEmpty()) {
                instructors = InstructorRepository.findByName(search);
            } else {
                instructors = InstructorRepository.getAllInstructors();
            }
        } catch (Exception e) {
            ServletUtil.sendErrorResponse(response, 500, "Error loading instructors from database");
            return;
        }
        
        // Build JSON response
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < instructors.size(); i++) {
            Instructor inst = instructors.get(i);
            
            json.append("{")
                .append("\"instructorID\":").append(inst.getInstructorID()).append(",")
                .append("\"Fname\":\"").append(escapeJson(inst.getFname())).append("\",")
                .append("\"Lname\":\"").append(escapeJson(inst.getLname())).append("\",")
                .append("\"email\":\"").append(escapeJson(inst.getEmail())).append("\",")
                .append("\"phoneNB\":\"").append(escapeJson(inst.getPhoneNB())).append("\",")
                .append("\"address\":\"").append(escapeJson(inst.getAddress())).append("\",")
                .append("\"departmentID\":").append(inst.getDepartmentID())
                .append("}");
            
            if (i < instructors.size() - 1) {
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



