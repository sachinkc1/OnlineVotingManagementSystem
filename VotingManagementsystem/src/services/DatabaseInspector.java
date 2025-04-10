package services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import services.DatabaseConnection;

/**
 * Utility class to inspect and fix database structure
 */
public class DatabaseInspector {

    /**
     * Print the structure of the votes table to help debug issues
     */
    public static void inspectVotesTable() {
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            
            System.out.println("=== DATABASE TABLE INSPECTION ===");

            // Check if votes table exists
            rs = metaData.getTables(null, null, "votes", null);
            if (!rs.next()) {
                System.out.println("The 'votes' table does not exist!");
                return;
            }
            
            // List all columns in the votes table
            System.out.println("\nColumns in the votes table:");
            rs = metaData.getColumns(null, null, "votes", null);
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                String isNullable = rs.getString("IS_NULLABLE");
                String defaultValue = rs.getString("COLUMN_DEF");
                
                System.out.println("  " + columnName + " (" + dataType + ")" + 
                    " Nullable: " + isNullable + 
                    (defaultValue != null ? " Default: " + defaultValue : ""));
            }
            
        } catch (SQLException e) {
            System.err.println("Error inspecting database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                // Don't close the connection as it might be used elsewhere
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * Recreate the votes table with the correct structure
     */
    public static void recreateVotesTable() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            
            System.out.println("Attempting to fix the votes table...");
            
            // Drop foreign key constraints first (if they exist)
            try {
                stmt.executeUpdate("ALTER TABLE votes DROP FOREIGN KEY votes_ibfk_1");
                System.out.println("Dropped foreign key constraint votes_ibfk_1");
            } catch (SQLException e) {
                // Ignore if constraint doesn't exist
                System.out.println("Note: Could not drop foreign key votes_ibfk_1 (might not exist)");
            }
            
            try {
                stmt.executeUpdate("ALTER TABLE votes DROP FOREIGN KEY votes_ibfk_2");
                System.out.println("Dropped foreign key constraint votes_ibfk_2");
            } catch (SQLException e) {
                // Ignore if constraint doesn't exist
                System.out.println("Note: Could not drop foreign key votes_ibfk_2 (might not exist)");
            }
            
            // Drop the table if it exists
            stmt.executeUpdate("DROP TABLE IF EXISTS votes");
            System.out.println("Dropped existing votes table");
            
            // Create a new votes table with correct structure
            String createVotesTable = "CREATE TABLE votes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "poll_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "candidate_id INT NOT NULL, " +
                "FOREIGN KEY (poll_id) REFERENCES polls(id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "UNIQUE KEY user_poll_unique (user_id, poll_id))";
            
            stmt.executeUpdate(createVotesTable);
            System.out.println("Created new votes table with correct structure");
            
            // Show the new table structure
            inspectVotesTable();
            
        } catch (SQLException e) {
            System.err.println("Error recreating votes table: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                // Don't close the connection as it might be used elsewhere
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}