public class Course {
    private int CCRN;
    private int programID;
    private String Fname;
    private String Lname;
    private String courseName;
    private int credits;
    private String courseDesc;

    public Course(int CCRN, int programID, String Fname, String Lname,
                  String courseName, int credits, String courseDesc) {
        this.CCRN = CCRN;
        this.programID = programID;
        this.Fname = Fname;
        this.Lname = Lname;
        this.courseName = courseName;
        this.credits = credits;
        this.courseDesc = courseDesc;
    }

    public int getCCRN() {
        return CCRN;
    }
    public int getProgramID() {
        return programID;
    }
    public String getFName() {
        return Fname;
    }
    public String getLName() {
        return Lname;
    }
    public String getCourseName() {
        return courseName;
    }
    public int getCredits() {
        return credits;
    }
    public String getCourseDesc() {
        return courseDesc;
    }
}
