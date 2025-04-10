package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides database connection functionality for the application
 */
public class DatabaseConnection {
    
    // Database connection parameters - modify these to match your database setup
    private static final String URL = "jdbc:mysql://localhost:3307/votes_database";
    private static final String USER = "root";
    private static final String PASSWORD = "1928374650@Asd"; // Add your password if needed
    
    private static Connection connection = null;
    
    /**
     * Gets a connection to the database
     * @return A Connection object
     * @throws SQLException If connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create new connection if none exists or if current one is closed
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            throw new SQLException("Database connection error: " + e.getMessage());
        }
    }
    
    /**
     * Closes the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}