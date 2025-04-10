package services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class that handles database operations
 */
public class DatabaseService {
    /**
     * Registers a new user in the database
     * 
     * @param fullName User's full name
     * @param gender User's gender
     * @param phone User's phone number
     * @param email User's email
     * @param password User's password
     * @return true if registration is successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean registerUser(String fullName, String gender, String phone, String email, String password) throws SQLException {
        // First check if email already exists
        if (emailExists(email)) {
            throw new SQLException("A user with this email already exists.");
        }
        String sql = "INSERT INTO users (full_name, gender, phone, email, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, gender);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);
            pstmt.setString(5, password); // Consider using password hashing in a real application
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Checks if an email already exists in the database
     * 
     * @param email Email to check
     * @return true if email exists, false otherwise
     * @throws SQLException If a database error occurs
     */
    private boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Authenticates a regular user
     * 
     * @param email User's email
     * @param password User's password
     * @return true if authentication is successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean authenticateUser(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password); // Consider using password verification with hashing in a real application
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // If result exists, authentication is successful
            }
        }
    }
    
    /**
     * Authenticates an admin user
     * 
     * @param email Admin's email
     * @param password Admin's password
     * @return true if admin authentication is successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean authenticateAdmin(String email, String password) throws SQLException {
        String sql = "SELECT * FROM admin WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // If result exists, authentication is successful
            }
        }
    }
    
    /**
     * Gets a user's full name by email
     * 
     * @param email User's email
     * @return User's full name or null if not found
     * @throws SQLException If a database error occurs
     */
    public String getUserFullName(String email) throws SQLException {
        String sql = "SELECT full_name FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        }
        return null;
    }
    
    /**
     * Gets an admin's full name by email
     * 
     * @param email Admin's email
     * @return Admin's full name or null if not found
     * @throws SQLException If a database error occurs
     */
    public String getAdminFullName(String email) throws SQLException {
        String sql = "SELECT full_name FROM admin WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        }
        return null;
    }
    
    /**
     * Gets a user's profile information by email
     * 
     * @param email User's email
     * @return Map containing user's profile information
     * @throws SQLException If a database error occurs
     */
    public Map<String, String> getUserProfile(String email) throws SQLException {
        Map<String, String> profile = new HashMap<>();
        String sql = "SELECT id, full_name, gender, phone, email FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    profile.put("id", String.valueOf(rs.getInt("id")));
                    profile.put("full_name", rs.getString("full_name"));
                    profile.put("gender", rs.getString("gender"));
                    profile.put("phone", rs.getString("phone"));
                    profile.put("email", rs.getString("email"));
                }
            }
        }
        return profile;
    }
    
    /**
     * Creates the necessary database tables if they don't exist
     * This method would be useful for initial setup
     * 
     * @throws SQLException If a database error occurs
     */
    public void setupDatabase() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            System.out.println("========== Setting up database ==========");
            
            // Create users table if it doesn't exist
            String createusersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "gender VARCHAR(10) NOT NULL, " +
                "phone VARCHAR(15) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(100) NOT NULL)";
            stmt.executeUpdate(createusersTable);
            System.out.println("users table created or already exists");
            
            // Create admin table if it doesn't exist
            String createAdminTable = "CREATE TABLE IF NOT EXISTS admin (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "gender VARCHAR(10) NOT NULL, " +
                "phone VARCHAR(15) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(100) NOT NULL)";
            stmt.executeUpdate(createAdminTable);
            System.out.println("Admin table created or already exists");
            
            // Check if admin account exists
            String checkAdmin = "SELECT COUNT(*) FROM admin WHERE email = 'admin@gmail.com'";
            ResultSet rs = stmt.executeQuery(checkAdmin);
            boolean adminExists = false;
            if (rs.next()) {
                adminExists = rs.getInt(1) > 0;
            }
            
            // If admin doesn't exist, create the admin user
            if (!adminExists) {
                String insertAdmin = "INSERT INTO admin (full_name, gender, phone, email, password) " +
                    "VALUES ('admin', 'male', '9847698000', 'admin@gmail.com', 'admin@1')";
                stmt.executeUpdate(insertAdmin);
                System.out.println("Admin user created successfully");
            }
            
            // Create polls table if it doesn't exist
            String createPollsTable = "CREATE TABLE IF NOT EXISTS polls (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " + 
                "name VARCHAR(100) NOT NULL, " +
                "candidate1 VARCHAR(100) NOT NULL, " +
                "candidate2 VARCHAR(100) NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "total_votes INT DEFAULT 0, " +
                "created_by INT, " +
                "FOREIGN KEY (created_by) REFERENCES admin(id))";
            stmt.executeUpdate(createPollsTable);
            System.out.println("Polls table created or already exists");
            
            // Check if votes table exists and has the right structure
            boolean recreateVotesTable = false;
            try {
                // Check if votes table exists
                ResultSet votesTable = conn.getMetaData().getTables(null, null, "votes", null);
                if (!votesTable.next()) {
                    // Table doesn't exist
                    System.out.println("Votes table doesn't exist - will create it");
                    recreateVotesTable = true;
                } else {
                    // Table exists, check if it has the right structure
                    try {
                        ResultSet columns = stmt.executeQuery("DESCRIBE votes");
                        boolean hasCandidate = false;
                        while (columns.next()) {
                            if ("candidate_id".equals(columns.getString("Field"))) {
                                hasCandidate = true;
                                break;
                            }
                        }
                        if (!hasCandidate) {
                            System.out.println("Votes table is missing candidate_id column - will recreate");
                            recreateVotesTable = true;
                        } else {
                            System.out.println("Votes table exists with proper structure - keeping existing votes");
                        }
                    } catch (SQLException e) {
                        // Can't check structure, recreate to be safe
                        System.out.println("Could not verify votes table structure - will recreate: " + e.getMessage());
                        recreateVotesTable = true;
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error checking votes table - will recreate: " + e.getMessage());
                recreateVotesTable = true;
            }
            
            // Only recreate votes table if needed
            if (recreateVotesTable) {
                try {
                    // First check if table exists before dropping
                    ResultSet votesTable = conn.getMetaData().getTables(null, null, "votes", null);
                    if (votesTable.next()) {
                        // Try to drop any foreign keys if table exists
                        try {
                            stmt.executeUpdate("ALTER TABLE votes DROP FOREIGN KEY votes_ibfk_1");
                        } catch (SQLException e) {
                            // Ignore - constraint may not exist
                        }
                        try {
                            stmt.executeUpdate("ALTER TABLE votes DROP FOREIGN KEY votes_ibfk_2");
                        } catch (SQLException e) {
                            // Ignore - constraint may not exist
                        }
                        
                        // Only drop if we need to recreate
                        stmt.executeUpdate("DROP TABLE IF EXISTS votes");
                        System.out.println("Dropped existing votes table to recreate with proper schema");
                    }
                } catch (SQLException e) {
                    System.out.println("Note: Could not drop votes table completely: " + e.getMessage());
                }
                
                // Create votes table with proper constraints
                String createVotesTable = "CREATE TABLE votes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "poll_id INT NOT NULL, " +
                    "user_id INT NOT NULL, " +
                    "candidate_id INT NOT NULL, " +
                    "FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id), " +
                    "UNIQUE KEY user_poll_unique (user_id, poll_id))";
                stmt.executeUpdate(createVotesTable);
                System.out.println("Created votes table with UNIQUE constraint on user_id+poll_id");
            } else {
                // Make sure the unique constraint exists
                try {
                    stmt.execute("ALTER TABLE votes ADD CONSTRAINT user_poll_unique UNIQUE (user_id, poll_id)");
                    System.out.println("Added unique constraint to existing votes table");
                } catch (SQLException e) {
                    // If constraint already exists, that's fine
                    if (e.getMessage().contains("Duplicate") || e.getMessage().contains("already exists")) {
                        System.out.println("Unique constraint already exists on votes table");
                    } else {
                        System.err.println("Error adding constraint: " + e.getMessage());
                    }
                }
            }
            
            // Simple check of columns
            System.out.println("\n===== Verifying Votes Table Structure =====");
            try {
                ResultSet columns = stmt.executeQuery("DESCRIBE votes");
                while (columns.next()) {
                    String columnName = columns.getString("Field");
                    String dataType = columns.getString("Type");
                    String isNullable = columns.getString("Null");
                    String key = columns.getString("Key");
                    System.out.println("  " + columnName + " (" + dataType + ")" + 
                                      " Nullable: " + isNullable + " Key: " + key);
                }
                columns.close();
            } catch (SQLException e) {
                System.out.println("Error describing votes table: " + e.getMessage());
            }
            
            // Try to verify indexes differently
            System.out.println("\n===== Verifying Votes Table Indexes =====");
            try {
                ResultSet indexes = stmt.executeQuery("SHOW INDEXES FROM votes");
                while (indexes.next()) {
                    String keyName = indexes.getString("Key_name");
                    String columnName = indexes.getString("Column_name");
                    boolean nonUnique = indexes.getInt("Non_unique") == 1;
                    System.out.println("  " + keyName + " on column " + columnName + 
                                      " (Unique: " + !nonUnique + ")");
                }
                indexes.close();
            } catch (SQLException e) {
                System.out.println("Error showing indexes: " + e.getMessage());
            }
            
            System.out.println("========== Database setup complete ==========\n");
        } finally {
            // Close resources
            if (stmt != null) {
                stmt.close();
            }
            // Don't close the connection as it may be used elsewhere
        }
    }
}