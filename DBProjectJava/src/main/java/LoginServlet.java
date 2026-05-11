import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            String msg = "{\"success\":false,\"error\":\"Missing username or password\"}";
            ServletUtil.sendJsonResponse(response, 400, msg);
            return;
        }
        
        boolean success;
        try {
            success = AuthService.studentLogin(username.trim(), password);
        } catch (Exception e) {
            String msg = "{\"success\":false,\"error\":\"Server error\"}";
            ServletUtil.sendJsonResponse(response, 500, msg);
            return;
        }
        
        String responseJson = success ? "{\"success\":true}" : "{\"success\":false}";
        ServletUtil.sendJsonResponse(response, 200, responseJson);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            String msg = "{\"success\":false,\"error\":\"Missing username or password\"}";
            ServletUtil.sendJsonResponse(response, 400, msg);
            return;
        }
        
        boolean success;
        try {
            success = AuthService.studentLogin(username.trim(), password);
        } catch (Exception e) {
            String msg = "{\"success\":false,\"error\":\"Server error\"}";
            ServletUtil.sendJsonResponse(response, 500, msg);
            return;
        }
        
        String responseJson = success ? "{\"success\":true}" : "{\"success\":false}";
        ServletUtil.sendJsonResponse(response, 200, responseJson);
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        ServletUtil.setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}



