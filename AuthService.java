import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
// import DBManager; // Assuming DBManager is imported or in the same package

public class AuthService {

    /**
     * Authenticates the user and returns the user's ID and Type as a concatenated string.
     * @param username The string username (e.g., "john_doe").
     * @param password The user's password.
     * @return A string in the format "userId|userType" (e.g., "101|patient") on success, or null on failure.
     */
    public static String authenticateUser(String username, String password) {
        String authResult = null; // Will store "ID|TYPE" or be null
        long startTime = System.currentTimeMillis();

        System.out.println("\n--- AUTH SERVICE START ---");
        System.out.println("Attempting to authenticate user: " + username);

        // --- SQL Query: CRITICAL CHANGE - Select both user_id and user_type ---
        // NOTE: Replace 'username_col' with your actual username column name (e.g., 'email', 'login_name').
        String sql = "SELECT user_id, user_type FROM Users WHERE username = ? AND password = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Check 1: Connection Attempt
            System.out.println("1. Attempting to get database connection...");
            
            // NOTE: You must uncomment this line and ensure DBManager exists/works
             conn = DBManager.getConnection(); 
            
            System.out.println("2. Connection established successfully.");
            
            // Check 2: PreparedStatement Execution
            pstmt = conn.prepareStatement(sql);
            
            // Use setString for the string-based username
            pstmt.setString(1, username); 
            
            // Use setString for the password
            pstmt.setString(2, password);
            
            System.out.println("3. Executing query: " + sql);
            
            // Check 3: ResultSet Retrieval
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id"); // Get the integer ID
                String userType = rs.getString("user_type"); // Get the string Type
                
                // CRITICAL FIX: Concatenate and store the ID and Type
                authResult = userId + "|" + userType; 
                
                System.out.println("SUCCESS: User '" + username + "' logged in. Result: " + authResult);
            } else {
                System.out.println("FAILURE: Credentials mismatch in database.");
            }
            
        } catch (SQLTimeoutException e) {
            System.err.println("CRITICAL ERROR: Database connection timed out. Check if MySQL server is running and accessible.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("CRITICAL ERROR: SQL Exception occurred.");
            e.printStackTrace();
        } catch (Exception e) {
            // This catch is necessary if DBManager.getConnection() or similar throws a generic Exception
            System.err.println("CRITICAL ERROR: Unexpected error during authentication.");
            e.printStackTrace();
        } finally {
            // Close resources reliably
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
                System.out.println("4. Database resources closed.");
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("--- AUTH SERVICE END (Time taken: %.3f seconds) ---\n", (endTime - startTime) / 1000.0);
        
        return authResult; // Returns "ID|TYPE" or null
    }
}