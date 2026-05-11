import java.time.LocalDate;

public class InstructorAssessment {
    private int studentID;
    private int instructorID;
    private LocalDate instructorAssessmentDate;
    private int rating1, rating2, rating3, rating4;
    private String recommendations;
    private String Fname;
    private String Lname;

    public InstructorAssessment(int studentID, int instructorID, LocalDate instructorAssessmentDate,
                                int rating1, int rating2, int rating3, int rating4, String recommendations, String Fname, String Lname) {
        this.studentID = studentID;
        this.instructorID = instructorID;
        this.instructorAssessmentDate = instructorAssessmentDate;
        this.rating1 = rating1;
        this.rating2 = rating2;
        this.rating3 = rating3;
        this.rating4 = rating4;
        this.recommendations = recommendations;
        this.Fname = Fname;
        this.Lname = Lname;
    }

    public int getStudentID() {
        return studentID;
    }
    public int getInstructorID() {
        return instructorID;
    }
    public LocalDate getInstructorAssessmentDate() {
        return instructorAssessmentDate;
    }
    public int getRating1() {
        return rating1;
    }
    public int getRating2() {
        return rating2;
    }
    public int getRating3() {
        return rating3;
    }
    public int getRating4() {
        return rating4;
    }
    public String getRecommendations() {
        return recommendations;
    }
    public String getFname() {
        return Fname;
    }
    public String getLname() {
        return Lname;
    }
}
