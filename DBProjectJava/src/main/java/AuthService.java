import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static boolean studentLogin(String username, String password) throws SQLException {
        String sql = "SELECT password_hash FROM STUDENT WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false; // username not found
                }
                String storedHash = rs.getString("password_hash");
                String inputHash = password;
                // String inputHash = hashPassword(password); // Encryption
                return storedHash != null && storedHash.equals(inputHash);
                
            }
        }
    }

    // use it in java main to insert new students with username and password
    public static boolean studentSignUp(Student s1, String username, String password) throws SQLException {
        String sql = "INSERT INTO STUDENT(studentID, Fname, Lname, email, phoneNB, address, username, password_hash) " +
                        " VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s1.getStudentID());
            ps.setString(2, s1.getFname());
            ps.setString(3, s1.getLname());
            ps.setString(4, s1.getEmail());
            ps.setString(5, s1.getPhoneNB());
            ps.setString(6, s1.getAddress());
            ps.setString(7, username);
            ps.setString(8, password);
            // ps.setString(8, hashPassword(password)); 
            // ^ for encryption !!!! MAKE SURE YOU USE ENCRYPTION IN studentLogin()

            int updated = ps.executeUpdate();
            return updated == 1;
        }
    }
}