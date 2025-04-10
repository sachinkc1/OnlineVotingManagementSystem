package services;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to fix database schema issues
 */
public class DatabaseSchemaFixer {
    
    /**
     * Fix the database schema by ensuring all required columns exist
     * Specifically addresses the missing candidate_id column in votes table
     */
    public static void fixSchema() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Get database connection
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            
            // Check if votes table exists, if not create it
            String createVotesTable = "CREATE TABLE IF NOT EXISTS votes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "poll_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "candidate_id INT NOT NULL, " +
                "FOREIGN KEY (poll_id) REFERENCES polls(id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "UNIQUE KEY user_poll_unique (user_id, poll_id))";
            stmt.executeUpdate(createVotesTable);
            
            // To safely add a column if it doesn't exist, we need to use ALTER TABLE 
            // with a condition, which requires dynamic SQL in MySQL
            try {
                // Try to add candidate_id column if it doesn't exist
                stmt.executeUpdate("ALTER TABLE votes ADD COLUMN candidate_id INT NOT NULL");
                System.out.println("Added missing candidate_id column to votes table");
            } catch (SQLException e) {
                // Column likely already exists
                if (e.getMessage().contains("Duplicate column name")) {
                    System.out.println("candidate_id column already exists in votes table");
                } else {
                    throw e; // Re-throw if it's a different error
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error fixing database schema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (stmt != null) stmt.close();
                // Don't close the connection as it may be used elsewhere
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}