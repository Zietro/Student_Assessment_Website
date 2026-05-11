import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final Set<String> ADMIN_TOKENS = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws IOException {

        // start HTTP server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/courses", new CoursesHandler());
        server.createContext("/instructors", new InstructorsHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/course-assessment", new CourseAssessmentHandler());
        server.createContext("/instructor-assessment", new InstructorAssessmentHandler());
        server.createContext("/student-course", new StudentCourseHandler());
        server.createContext("/admin/students", new AdminStudentsHandler());
        server.createContext("/admin/instructors", new AdminInstructorsHandler());
        server.createContext("/admin/coordinators", new AdminCoordinatorsHandler());
        server.createContext("/admin/programs", new AdminProgramsHandler());
        server.createContext("/admin/departments", new AdminDepartmentsHandler());
        server.createContext("/admin/courses", new AdminCoursesHandler());
        server.createContext("/admin/registrations", new AdminRegistrationsHandler());
        server.createContext("/admin/student-registrations", new AdminStudentRegistrationsHandler());

        // Student s = new Student(202305297, "Pietro", "Ziade", "pietro.ziade@uni.edu","+96176649007","Mastita");
        // studentSignUp(s, "pietro.ziade", "123");
        /* 
        // inserts the student into database
        // but you have to make sure there are no duplicate studentID and username
        */
        server.setExecutor(null);
        server.start();
        System.out.println("Server running at: http://localhost:8000");



    }

    // ---------- /courses (reads from DB) ----------
    static class CoursesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<Course> courses;
            try {
                courses = CourseRepository.getAllCourses();   // from DB
            } catch (Exception e) {
                String error = "Error loading courses from database";
                byte[] errBytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, errBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errBytes);
                }
                return;
            }

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

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // ---------- /instructors (reads from DB) ----------
    static class InstructorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            addCorsHeaders(exchange);

            String query = null;
            String rawQuery = exchange.getRequestURI().getQuery(); // full query string
            if (rawQuery != null) {
                for (String param : rawQuery.split("&")) {
                    String[] parts = param.split("=", 2);
                    if (parts.length == 2 && "instructorName".equalsIgnoreCase(parts[0])) {
                        query = java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                        break;
                    }
                }
            }

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<Instructor> instructors;
            try {
                if (query != null && !query.trim().isEmpty()) {
                    instructors = InstructorRepository.findByName(query);
                } else {
                    instructors = InstructorRepository.getAllInstructors();
                }
                
            } catch (Exception e) {
                String error = "Error loading instructors from database";
                byte[] errBytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, errBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errBytes);
                }
                return;
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < instructors.size(); i++) {
                Instructor inst = instructors.get(i);

                json.append("{")
                        .append("\"instructorID\":").append(inst.getInstructorID()).append(",")
                        .append("\"Fname\":\"").append(escapeJson(inst.getFname())).append("\",")
                        .append("\"Lname\":\"").append(escapeJson(inst.getLname())).append("\",")
                        .append("\"instructorName\":\"").append(escapeJson(inst.getFname())).append(" ").append(escapeJson(inst.getLname())).append("\",")
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

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // ---------- /login?username=...&password=... ----------
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String username = null;  // get it from front end
            String password = null;  // get it from front end

            if (query != null) {
                String[] parts = query.split("&");
                for (String part : parts) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2) {
                        if (kv[0].equals("username")) {
                            username = kv[1];
                        } else if (kv[0].equals("password")) {
                            password = kv[1];
                        }
                    }
                }
            }

            if (username == null || password == null) {
                String msg = "{\"success\":false,\"error\":\"Missing username or password\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(400, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            // Admin login (hardcoded admin/admin)
            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                String token = UUID.randomUUID().toString();
                ADMIN_TOKENS.add(token);
                String msg = "{\"success\":true,\"role\":\"admin\",\"token\":\"" + escapeJson(token) + "\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            boolean success;
            try {
                success = AuthService.studentLogin(username, password);
            } catch (Exception e) {
                String msg = "{\"success\":false,\"error\":\"Server error\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            String response = success ? "{\"success\":true,\"role\":\"student\"}" : "{\"success\":false}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // ---------- /admin/students (admin CRUD) ----------
    static class AdminStudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!requireAdmin(exchange)) return;

            try {
                String method = exchange.getRequestMethod();

                if ("GET".equalsIgnoreCase(method)) {
                    var students = AdminStudentRepository.getAll();
                    org.json.JSONArray arr = new org.json.JSONArray(students);
                    sendJson(exchange, 200, arr.toString());
                    return;
                }

                String body = readRequestBody(exchange);
                JSONObject json = body.isBlank() ? new JSONObject() : new JSONObject(body);

                if ("POST".equalsIgnoreCase(method)) {
                    int studentID = json.getInt("studentID");
                    AdminStudentRepository.insert(
                            studentID,
                            json.optString("Fname", ""),
                            json.optString("Lname", ""),
                            json.optString("email", ""),
                            json.optString("phoneNB", ""),
                            json.optString("address", ""),
                            json.optString("username", ""),
                            json.optString("password_hash", json.optString("password", ""))
                    );
                    sendJson(exchange, 201, "{\"success\":true}");
                    return;
                }

                if ("PUT".equalsIgnoreCase(method)) {
                    int studentID = json.getInt("studentID");
                    boolean ok = AdminStudentRepository.update(
                            studentID,
                            json.optString("Fname", ""),
                            json.optString("Lname", ""),
                            json.optString("email", ""),
                            json.optString("phoneNB", ""),
                            json.optString("address", ""),
                            json.optString("username", ""),
                            json.optString("password_hash", json.optString("password", ""))
                    );
                    sendJson(exchange, ok ? 200 : 404, ok ? "{\"success\":true}" : "{\"error\":\"student not found\"}");
                    return;
                }

                if ("DELETE".equalsIgnoreCase(method)) {
                    int studentID = json.getInt("studentID");
                    boolean ok = AdminStudentRepository.delete(studentID);
                    sendJson(exchange, ok ? 200 : 404, ok ? "{\"success\":true}" : "{\"error\":\"student not found\"}");
                    return;
                }

                sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
            } catch (SQLIntegrityConstraintViolationException e) {
                sendJson(exchange, 409, "{\"error\":\"duplicate entry or constraint violation\"}");
            } catch (Exception e) {
                e.printStackTrace(); // Log the error for debugging
                sendJson(exchange, 500, "{\"error\":\"server error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // ---------- /admin/coordinators (get all coordinators) ----------
    static class AdminCoordinatorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!requireAdmin(exchange)) return;

            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    var coordinators = AdminInstructorRepository.getAllCoordinators();
                    org.json.JSONArray arr = new org.json.JSONArray(coordinators);
                    sendJson(exchange, 200, arr.toString());
                    return;
                }

                exchange.sendResponseHeaders(405, -1);
            } catch (Exception e) {
                String error = "Error loading coordinators: " + e.getMessage();
                byte[] errBytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, errBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errBytes);
                }
            }
        }
    }

    // ---------- /admin/programs (get all programs) ----------
    static class AdminProgramsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!requireAdmin(exchange)) return;

            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    var programs = AdminProgramRepository.getAll();
                    org.json.JSONArray arr = new org.json.JSONArray(programs);
                    sendJson(exchange, 200, arr.toString());
                    return;
                }

                exchange.sendResponseHeaders(405, -1);
            } catch (Exception e) {
                String error = "Error loading programs: " + e.getMessage();
                byte[] errBytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, errBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errBytes);
                }
            }
        }
    }

    // ---------- /admin/departments (get all departments) ----------
    static class AdminDepartmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!requireAdmin(exchange)) return;

            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    var departments = AdminDepartmentRepository.getAll();
                    org.json.JSONArray arr = new org.json.JSONArray(departments);
                    sendJson(exchange, 200, arr.toString());
                    return;
                }

                exchange.sendResponseHeaders(405, -1);
            } catch (Exception e) {
                String error = "Error loading departments: " + e.getMessage();
                byte[] errBytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, errBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errBytes);
                }
            }
        }
    }

    // ---------- /admin/instructors (admin CRUD) ----------
    static class AdminInstructorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!requireAdmin(exchange)) return;

            try {
                String method = exchange.getRequestMethod();

                if ("GET".equalsIgnoreCase(method)) {
                    var instructors = AdminInstructorRepository.getAll();
                    org.json.JSONArray arr = new org.json.JSONArray(instructors);
                    sendJson(exchange, 200, arr.toString());
                    return;
                }

                String body = readRequestBody(exchange);
                JSONObject json = body.isBlank() ? new JSONObject() : new JSONObject(body);

                if ("POST".equalsIgnoreCase(method)) {
                    AdminInstructorRepository.insert(
                            json.getInt("instructorID"),
                            json.optString("Fname", ""),
                            json.optString("Lname", ""),
                            json.optString("email", ""),
                            json.optString("phoneNB", ""),
                            json.optString("address", ""),
                            json.optInt("departmentID", 0)
                    );
                    sendJson(exchange, 201, "{\"success\":true}");
                    return;
                }

                if ("PUT".equalsIgnoreCase(method)) {
                    boolean ok = AdminInstructorRepository.update(
                            json.getInt("instructorID"),
                            json.optString("Fname", ""),
                            json.optString("Lname", ""),
                            json.optString("email", ""),
                            json.optString("phoneNB", ""),
                            json.optString("address", ""),
                            json.optInt("departmentID", 0)
                    );
                    sendJson(exchange, ok ? 200 : 404, ok ? "{\"success\":true}" : "{\"error\":\"instructor not found\"}");
                    return;
                }

                if ("DELETE".equalsIgnoreCase(method)) {
                    boolean ok = AdminInstructorRepository.delete(json.getInt("instructorID"));
                    sendJson(exchange, ok ? 200 : 404, ok ? "{\"success\":true}" : "{\"error\":\"instructor not found\"}");
                    return;
                }

                sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
            } catch (SQLIntegrityConstraintViolationException e) {
                sendJson(exchange, 409, "{\"error\":\"duplicate entry or constraint violation\"}");
            } catch (Exception e) {
                e.printStackTrace(); // Log the error for debugging
                sendJson(exchange, 500, "{\"error\":\"server error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // ---------- /admin/courses (admin CRUD) ----------
    static class AdminCoursesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!requireAdmin(exchange)) return;

            try {
                String method = exchange.getRequestMethod();

                if ("GET".equalsIgnoreCase(method)) {
                    var courses = AdminCourseRepository.getAll();
                    org.json.JSONArray arr = new org.json.JSONArray(courses);
                    sendJson(exchange, 200, arr.toString());
                    return;
                }

                String body = readRequestBody(exchange);
                JSONObject json = body.isBlank() ? new JSONObject() : new JSONObject(body);

                if ("POST".equalsIgnoreCase(method)) {
                    AdminCourseRepository.insert(
                            json.getInt("CCRN"),
                            json.optInt("programID", 0),
                            json.optInt("coordinatorID", 0),
                            json.optString("courseName", ""),
                            json.optInt("credits", 0),
                            json.optString("courseDesc", "")
                    );
                    sendJson(exchange, 201, "{\"success\":true}");
                    return;
                }

                if ("PUT".equalsIgnoreCase(method)) {
                    boolean ok = AdminCourseRepository.update(
                            json.getInt("CCRN"),
                            json.optInt("programID", 0),
                            json.optInt("coordinatorID", 0),
                            json.optString("courseName", ""),
                            json.optInt("credits", 0),
                            json.optString("courseDesc", "")
                    );
                    sendJson(exchange, ok ? 200 : 404, ok ? "{\"success\":true}" : "{\"error\":\"course not found\"}");
                    return;
                }

                if ("DELETE".equalsIgnoreCase(method)) {
                    boolean ok = AdminCourseRepository.delete(json.getInt("CCRN"));
                    sendJson(exchange, ok ? 200 : 404, ok ? "{\"success\":true}" : "{\"error\":\"course not found\"}");
                    return;
                }

                sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
            } catch (SQLIntegrityConstraintViolationException e) {
                sendJson(exchange, 409, "{\"error\":\"duplicate entry or constraint violation\"}");
            } catch (Exception e) {
                e.printStackTrace(); // Log the error for debugging
                sendJson(exchange, 500, "{\"error\":\"server error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // ---------- /admin/registrations (admin CRUD) ----------
    static class AdminRegistrationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!requireAdmin(exchange)) return;

            try {
                String method = exchange.getRequestMethod();

                if ("GET".equalsIgnoreCase(method)) {
                    var registrations = RegistrationRepository.getAll();
                    org.json.JSONArray arr = new org.json.JSONArray(registrations);
                    sendJson(exchange, 200, arr.toString());
                    return;
                }

                String body = readRequestBody(exchange);
                JSONObject json = body.isBlank() ? new JSONObject() : new JSONObject(body);

                if ("POST".equalsIgnoreCase(method)) {
                    int CCRN = json.getInt("CCRN");
                    int instructorID = json.getInt("instructorID");
                    RegistrationRepository.insert(CCRN, instructorID);
                    sendJson(exchange, 201, "{\"success\":true}");
                    return;
                }

                if ("DELETE".equalsIgnoreCase(method)) {
                    int registrationID = json.getInt("registrationID");
                    boolean ok = RegistrationRepository.delete(registrationID);
                    sendJson(exchange, ok ? 200 : 404, ok ? "{\"success\":true}" : "{\"error\":\"registration not found\"}");
                    return;
                }

                sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
            } catch (SQLIntegrityConstraintViolationException e) {
                sendJson(exchange, 409, "{\"error\":\"duplicate entry or constraint violation\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"server error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // ---------- /admin/student-registrations (admin CRUD) ----------
    static class AdminStudentRegistrationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!requireAdmin(exchange)) return;

            try {
                String method = exchange.getRequestMethod();

                if ("GET".equalsIgnoreCase(method)) {
                    var studentRegistrations = StudentRegistrationRepository.getAll();
                    org.json.JSONArray arr = new org.json.JSONArray(studentRegistrations);
                    sendJson(exchange, 200, arr.toString());
                    return;
                }

                String body = readRequestBody(exchange);
                JSONObject json = body.isBlank() ? new JSONObject() : new JSONObject(body);

                if ("POST".equalsIgnoreCase(method)) {
                    int studentID = json.getInt("studentID");
                    int registrationID = json.getInt("registrationID");
                    StudentRegistrationRepository.insert(studentID, registrationID);
                    sendJson(exchange, 201, "{\"success\":true}");
                    return;
                }

                if ("DELETE".equalsIgnoreCase(method)) {
                    int studentID = json.getInt("studentID");
                    int registrationID = json.getInt("registrationID");
                    boolean ok = StudentRegistrationRepository.delete(studentID, registrationID);
                    sendJson(exchange, ok ? 200 : 404, ok ? "{\"success\":true}" : "{\"error\":\"student registration not found\"}");
                    return;
                }

                sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
            } catch (SQLIntegrityConstraintViolationException e) {
                sendJson(exchange, 409, "{\"error\":\"duplicate entry or constraint violation\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"server error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // ----------    /course-assessments?    ----------
    static class CourseAssessmentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleCourseAssessmentSubmit(exchange);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Integer ccrn = null; // get ccrn of the course we choose in the front end website

            if (query != null) {
                String[] parts = query.split("&");
                for (String part : parts) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("ccrn")) {
                        try {
                            ccrn = Integer.parseInt(kv[1]);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            if (ccrn == null) {
                String msg = "{\"error\":\"missing ccrn parameter\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(400, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            List<CourseAssessment> assessments;
            try {
                assessments = CourseAssessmentRepository.findByCourse(ccrn);
            } catch (Exception e) {
                String msg = "{\"error\":\"database error\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

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

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private void handleCourseAssessmentSubmit(HttpExchange exchange) throws IOException {
            try {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String username = json.optString("username", null);
                int ccrn = json.optInt("ccrn", -1);
                int rating1 = json.optInt("rating1", 0);
                int rating2 = json.optInt("rating2", 0);
                int rating3 = json.optInt("rating3", 0);
                int rating4 = json.optInt("rating4", 0);
                String recommendations = json.optString("recommendations", "");

                if (username == null || username.isBlank() || ccrn <= 0) {
                    sendJson(exchange, 400, "{\"error\":\"missing username or ccrn\"}");
                    return;
                }

                Integer studentId = StudentRepository.findStudentIdByUsername(username);
                if (studentId == null) {
                    sendJson(exchange, 404, "{\"error\":\"unknown username\"}");
                    return;
                }

                if (CourseAssessmentRepository.existsByStudentAndCourse(studentId, ccrn)) {
                    sendJson(exchange, 409, "{\"error\":\"course assessment already submitted\"}");
                    return;
                }

                CourseAssessmentRepository.insert(studentId, ccrn, null, rating1, rating2, rating3, rating4, recommendations);
                sendJson(exchange, 201, "{\"success\":true}");
            } catch (SQLIntegrityConstraintViolationException e) {
                sendJson(exchange, 409, "{\"error\":\"course assessment already submitted\"}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    // ---------- /instructor-assessments?instructorID=123 ----------
    static class InstructorAssessmentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleInstructorAssessmentSubmit(exchange);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String instructorName = null; // get it from frontend

            if (query != null) {
                for (String part : query.split("&")) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("instructorName")) {
                        try {
                            instructorName = kv[1];
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            if (instructorName == null) {
                String msg = "{\"error\":\"missing Name of Instructor parameter\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(400, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            List<InstructorAssessment> assessments;

            try {
                assessments = InstructorAssessmentRepository.findByInstructor(instructorName);
            } catch (Exception e) {
                String msg = "{\"error\":\"database error\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            // Convert to JSON
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < assessments.size(); i++) {
                InstructorAssessment a = assessments.get(i);

                json.append("{")
                        .append("\"studentID\":").append(a.getStudentID()).append(",")
                        .append("\"instructorID\":").append(a.getInstructorID()).append(",")
                        .append("\"instructorAssessmentDate\":\"").append(a.getInstructorAssessmentDate()).append("\",")
                        .append("\"rating1\":").append(a.getRating1()).append(",")
                        .append("\"rating2\":").append(a.getRating2()).append(",")
                        .append("\"rating3\":").append(a.getRating3()).append(",")
                        .append("\"rating4\":").append(a.getRating4()).append(",")
                        .append("\"recommendations\":\"").append(escapeJson(a.getRecommendations())).append("\"")
                        .append("}");

                if (i < assessments.size() - 1) json.append(",");
            }
            json.append("]");

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private void handleInstructorAssessmentSubmit(HttpExchange exchange) throws IOException {
            try {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String username = json.optString("username", null);
                int ccrn = json.optInt("ccrn", -1);
                int rating1 = json.optInt("rating1", 0);
                int rating2 = json.optInt("rating2", 0);
                int rating3 = json.optInt("rating3", 0);
                int rating4 = json.optInt("rating4", 0);
                String recommendations = json.optString("recommendations", "");

                if (username == null || username.isBlank() || ccrn <= 0) {
                    sendJson(exchange, 400, "{\"error\":\"missing username or ccrn\"}");
                    return;
                }

                Integer studentId = StudentRepository.findStudentIdByUsername(username);
                if (studentId == null) {
                    sendJson(exchange, 404, "{\"error\":\"unknown username\"}");
                    return;
                }

                Integer instructorId = StudentCourseRepository.findInstructorIdForStudentUsernameAndCCRN(username, ccrn);
                if (instructorId == null) {
                    sendJson(exchange, 404, "{\"error\":\"no instructor found for this student/course\"}");
                    return;
                }

                if (InstructorAssessmentRepository.existsByStudentAndInstructor(studentId, instructorId)) {
                    sendJson(exchange, 409, "{\"error\":\"instructor assessment already submitted\"}");
                    return;
                }

                InstructorAssessmentRepository.insert(studentId, instructorId, null, rating1, rating2, rating3, rating4, recommendations);
                sendJson(exchange, 201, "{\"success\":true}");
            } catch (SQLIntegrityConstraintViolationException e) {
                sendJson(exchange, 409, "{\"error\":\"instructor assessment already submitted\"}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    // ---------- /student-courses?studentID=123 ----------
    static class StudentCourseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
            }

            String query = exchange.getRequestURI().getQuery();
            String username = null;

            if (query != null) {
                for (String part : query.split("&")) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("username")) {
                        username = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    }
                }
            }

            if (username == null || username.isBlank()) {
                String msg = "{\"error\":\"missing username parameter\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            

            List<Course> courses;
            try {
                courses = StudentCourseRepository.findCoursesByStudentUsername(username);
            } catch (Exception e) {
                String msg = "{\"error\":\"database error\"}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            // DB-backed "Done" flags (so refresh still shows Done)
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
            } catch (Exception ignored) {
                // If something goes wrong here, we still return courses without Done flags (defaults to false).
            }

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
                        .append("\"instructorName\":\"").append(escapeJson(c.getFName() + " " + c.getLName())).append("\",")
                        .append("\"courseName\":\"").append(escapeJson(c.getCourseName())).append("\",")
                        .append("\"credits\":").append(c.getCredits()).append(",")
                        .append("\"courseDesc\":\"").append(escapeJson(c.getCourseDesc())).append("\",")
                        .append("\"courseAssessmentDone\":").append(courseDone).append(",")
                        .append("\"instructorAssessmentDone\":").append(instructorDone)
                        .append("}");

                if (i < courses.size() - 1) json.append(",");
            }
            json.append("]");

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, X-Admin-Token");
    }

    private static String escapeJson(String str) {
        return JsonUtil.escapeJson(str);
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static boolean requireAdmin(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("X-Admin-Token");
        if (token == null || token.isBlank() || !ADMIN_TOKENS.contains(token)) {
            sendJson(exchange, 401, "{\"error\":\"admin unauthorized\"}");
            return false;
        }
        return true;
    }
    

}