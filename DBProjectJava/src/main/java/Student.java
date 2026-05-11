public class Student {
    private int studentID;
    private String Fname;
    private String Lname;
    private String email;
    private String phoneNB;
    private String address;

    public Student(int studentID, String Fname, String Lname,
                   String email, String phoneNB, String address) {
        this.studentID = studentID;
        this.Fname = Fname;
        this.Lname = Lname;
        this.email = email;
        this.phoneNB = phoneNB;
        this.address = address;
    }

    public int getStudentID() {
        return studentID;
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
}
