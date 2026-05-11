import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ServletUtil {
    
    /**
     * Sets CORS headers to allow frontend requests
     */
    public static void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
    
    /**
     * Sends a JSON response
     */
    public static void sendJsonResponse(HttpServletResponse response, int statusCode, String json) throws IOException {
        setCorsHeaders(response);
        response.setContentType("application/json; charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
            out.flush();
        }
    }
    
    /**
     * Sends a plain text response
     */
    public static void sendTextResponse(HttpServletResponse response, int statusCode, String text) throws IOException {
        setCorsHeaders(response);
        response.setContentType("text/plain; charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        
        try (PrintWriter out = response.getWriter()) {
            out.print(text);
            out.flush();
        }
    }
    
    /**
     * Sends an error response
     */
    public static void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        String json = "{\"error\":\"" + escapeJson(errorMessage) + "\"}";
        sendJsonResponse(response, statusCode, json);
    }
    
    /**
     * Escapes special characters for JSON
     */
    private static String escapeJson(String str) {
        return JsonUtil.escapeJson(str);
    }
}

