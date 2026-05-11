import java.time.LocalDate;

public class CourseAssessment {
    private int studentID;
    private int CCRN;
    private LocalDate courseAssessmentDate;
    private int rating1, rating2, rating3, rating4;
    private String recommendations;

    public CourseAssessment(int studentID, int CCRN,
                            LocalDate courseAssessmentDate,
                            int rating1, int rating2, int rating3, int rating4, String recommendations) {
        this.studentID = studentID;
        this.CCRN = CCRN;
        this.courseAssessmentDate = courseAssessmentDate;
        this.rating1 = rating1;
        this.rating2 = rating2;
        this.rating3 = rating3;
        this.rating4 = rating4;
        this.recommendations = recommendations;
    }

    public int getStudentID() {
        return studentID;
    }
    public int getCCRN() {
        return CCRN;
    }
    public LocalDate getCourseAssessmentDate() {
        return courseAssessmentDate;
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
}
