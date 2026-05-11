# Servlet Setup Guide

This guide explains how to connect your frontend with the backend using servlets.

## What Was Changed

### 1. **Created Servlet Classes**
   - `ServletUtil.java` - Utility class for CORS headers and JSON responses
   - `HelloServlet.java` - Health check endpoint (`/hello`)
   - `CoursesServlet.java` - Returns all courses (`/courses`)
   - `InstructorsServlet.java` - Returns all instructors (`/instructors`)
   - `LoginServlet.java` - Handles student login (`/login`)
   - `CourseAssessmentServlet.java` - Returns course assessments (`/course-assessment?ccrn=...`)
   - `InstructorAssessmentServlet.java` - Returns instructor assessments (`/instructor-assessment?instructorID=...`)
   - `StudentCourseServlet.java` - Returns student's courses (`/student-course?studentID=...`)

### 2. **Created web.xml**
   - Located at `src/main/webapp/WEB-INF/web.xml`
   - Configures the web application

### 3. **Updated pom.xml**
   - Added MySQL Connector dependency
   - Added Tomcat Maven plugin for easy deployment

### 4. **Updated Frontend JavaScript**
   - `handleLogin()` - Now calls `/login` servlet
   - `populateCoursesTable()` - Now fetches courses from `/courses` servlet
   - `searchCourse()` - Now calls `/course-assessment` servlet
   - `searchInstructor()` - Now calls `/instructor-assessment` servlet
   - Updated to work with actual database data structure

## How to Run

### Option 1: Using Tomcat Maven Plugin (Recommended)

1. **Build the project:**
   ```bash
   mvn clean package
   ```

2. **Run with Tomcat:**
   ```bash
   mvn tomcat7:run
   ```

3. **Access the application:**
   - Frontend: `http://localhost:8080/index.html`
   - API: `http://localhost:8080/hello` (test endpoint)

### Option 2: Deploy to External Tomcat

1. **Build WAR file:**
   ```bash
   mvn clean package
   ```

2. **Deploy the WAR:**
   - Copy `target/DBProjectJava-1.0-SNAPSHOT.war` to Tomcat's `webapps/` directory
   - Start Tomcat server
   - Access at `http://localhost:8080/DBProjectJava-1.0-SNAPSHOT/`

## API Endpoints

All endpoints support CORS and return JSON:

- `GET /hello` - Health check
- `GET /courses` - Get all courses
- `GET /instructors` - Get all instructors
- `POST /login` - Login (parameters: username, password)
- `GET /login?username=...&password=...` - Login via GET (for testing)
- `GET /course-assessment?ccrn=123` - Get assessments for a course
- `GET /instructor-assessment?instructorID=5` - Get assessments for an instructor
- `GET /student-course?studentID=202305297` - Get courses for a student

## Frontend File Location

Your frontend files are in `src/main/resources/`:
- `index.html`
- `script.js`
- `styles.css`

**Note:** For production, these should ideally be moved to `src/main/webapp/` (standard servlet location), but they will work from `resources/` as well.

## Database Configuration

Make sure your database connection in `Database.java` is correct:
- URL: `jdbc:mysql://localhost:3306/studentassessment`
- Username: `root`
- Password: Update as needed

## Testing

1. **Test the backend:**
   ```bash
   curl http://localhost:8080/hello
   curl http://localhost:8080/courses
   ```

2. **Test login:**
   ```bash
   curl -X POST http://localhost:8080/login -d "username=test&password=test"
   ```

3. **Open the frontend:**
   - Navigate to `http://localhost:8080/index.html`
   - Try logging in with valid credentials from your database

## Troubleshooting

1. **Port already in use:**
   - Change port in `pom.xml` (tomcat7-maven-plugin configuration)

2. **CORS errors:**
   - All servlets include CORS headers via `ServletUtil.setCorsHeaders()`

3. **Database connection errors:**
   - Check MySQL is running
   - Verify database credentials in `Database.java`
   - Ensure MySQL JDBC driver is in classpath (added via pom.xml)

4. **404 errors:**
   - Make sure you're accessing the correct URL
   - Check that servlets are deployed correctly

## Next Steps

- The old `Main.java` with HttpServer can be removed or kept for reference
- Consider adding servlets for submitting evaluations (currently only read operations)
- Add session management for better authentication
- Move frontend files to `src/main/webapp/` for better organization






