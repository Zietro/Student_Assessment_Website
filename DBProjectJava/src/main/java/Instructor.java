public class Instructor {
    private int instructorID;
    private String Fname;
    private String Lname;
    private String email;
    private String phoneNB;
    private String address;
    private int departmentID;

    public Instructor(int instructorID, String Fname, String Lname, String email, String phoneNB, String address,
                      int departmentID) {
        this.instructorID = instructorID;
        this.Fname = Fname;
        this.Lname = Lname;
        this.email = email;
        this.phoneNB = phoneNB;
        this.address = address;
        this.departmentID = departmentID;
    }

    public int getInstructorID() {
        return instructorID;
    }
    public String getFname() {
        return Fname;
    }
    public String getLname() {
        return Lname;
    }
    public String getEmail() {
        return email;
    }
    public String getPhoneNB() {
        return phoneNB;
    }
    public String getAddress() {
        return address;
    }
    public int getDepartmentID() {
        return departmentID;
    }
}
