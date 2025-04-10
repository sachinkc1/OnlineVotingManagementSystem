package services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;
import java.sql.*;

/**
 * Implementation of UserService that uses a database
 */
public class DatabaseUserService implements UserService {
    private ObservableList<User> cachedUsers;
    
    /**
     * Create a new DatabaseUserService
     */
    public DatabaseUserService() {
        // Initialize with empty list
        this.cachedUsers = FXCollections.observableArrayList();
    }
    
    /**
     * Create a new DatabaseUserService with a specific connection
     *
     * @param dbConnection Database connection parameter (not used since connection is handled statically)
     */
    public DatabaseUserService(DatabaseConnection dbConnection) {
        // Initialize with empty list - connection is static in your implementation
        this.cachedUsers = FXCollections.observableArrayList();
    }
    
    @Override
    public ObservableList<User> getAllUsers() {
        // Clear the cache and reload from database
        cachedUsers.clear();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, full_name, gender, phone, email FROM users")) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("gender"),
                    rs.getString("phone"),
                    rs.getString("email")
                );
                cachedUsers.add(user);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cachedUsers;
    }
    
    @Override
    public int addUser(User user) {
        int generatedId = -1;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO users (full_name, gender, phone, email, password) VALUES (?, ?, ?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getGender());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPassword());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
            
            // Refresh the cached list
            getAllUsers();
            
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return generatedId;
    }
    
    @Override
    public boolean updateUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE users SET full_name = ?, gender = ?, phone = ?, email = ? WHERE id = ?")) {
            
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getGender());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getEmail());
            stmt.setInt(5, user.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            // Refresh the cached list
            getAllUsers();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM users WHERE id = ?")) {
            
            stmt.setInt(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            // Refresh the cached list
            getAllUsers();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int getUserCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
}