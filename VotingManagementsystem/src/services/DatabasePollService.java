package services;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Poll;
import java.sql.*;

/**
 * Implementation of PollService that uses a database
 */
public class DatabasePollService implements PollService {
    private ObservableList<Poll> cachedPolls;
    
    /**
     * Create a new DatabasePollService
     */
    public DatabasePollService() {
        // Initialize with empty list
        this.cachedPolls = FXCollections.observableArrayList();
    }
    
    /**
     * Create a new DatabasePollService with a specific connection
     *
     * @param dbConnection Database connection parameter (not used since connection is handled statically)
     */
    public DatabasePollService(DatabaseConnection dbConnection) {
        // Initialize with empty list
        this.cachedPolls = FXCollections.observableArrayList();
    }
    
    /**
     * Get a specific poll by its ID
     * 
     * @param pollId The ID of the poll to retrieve
     * @return The Poll object if found, null otherwise
     */
    public Poll getPollById(int pollId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, name, candidate1, candidate2, status, total_votes, created_by " + 
                 "FROM polls WHERE id = ?")) {
            
            stmt.setInt(1, pollId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Poll(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("candidate1"),
                        rs.getString("candidate2"),
                        rs.getString("status"),
                        rs.getInt("total_votes"),
                        rs.getInt("created_by")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving poll by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;  // Poll not found
    }
    
    /**
     * Get active polls from the database
     * @return ObservableList of active polls
     */
    public ObservableList<Poll> getActivePolls() {
        ObservableList<Poll> activePolls = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, name, candidate1, candidate2, status, total_votes, created_by FROM polls WHERE status = 'Active'")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Poll poll = new Poll(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("candidate1"),
                    rs.getString("candidate2"),
                    rs.getString("status"),
                    rs.getInt("total_votes"),
                    rs.getInt("created_by")
                );
                activePolls.add(poll);
            }
        } catch (SQLException e) {
            System.err.println("Error loading active polls: " + e.getMessage());
            e.printStackTrace();
        }
        return activePolls;
    }
    
    /**
     * Get completed polls from the database
     * @return ObservableList of completed polls
     */
    public ObservableList<Poll> getCompletedPolls() {
        ObservableList<Poll> completedPolls = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, name, candidate1, candidate2, status, total_votes, created_by " +
                "FROM polls WHERE status = 'Completed'")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Poll poll = new Poll(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("candidate1"),
                    rs.getString("candidate2"),
                    rs.getString("status"),
                    rs.getInt("total_votes"),
                    rs.getInt("created_by")
                );
                completedPolls.add(poll);
            }
        } catch (SQLException e) {
            System.err.println("Error loading completed polls: " + e.getMessage());
            e.printStackTrace();
        }
        return completedPolls;
    }
    
    @Override
    public ObservableList<Poll> getAllPolls() {
        // Clear the cache and reload from database
        cachedPolls.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT id, name, candidate1, candidate2, status, total_votes, created_by FROM polls")) {
            while (rs.next()) {
                Poll poll = new Poll(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("candidate1"),
                    rs.getString("candidate2"),
                    rs.getString("status"),
                    rs.getInt("total_votes"),
                    rs.getInt("created_by")
                );
                cachedPolls.add(poll);
            }
        } catch (SQLException e) {
            System.err.println("Error loading polls: " + e.getMessage());
            e.printStackTrace();
        }
        return cachedPolls;
    }
    
    @Override
    public int createPoll(Poll poll) {
        int generatedId = -1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO polls (name, candidate1, candidate2, status, total_votes, created_by) " + 
                "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, poll.getName());
            stmt.setString(2, poll.getCandidate1());
            stmt.setString(3, poll.getCandidate2());
            stmt.setString(4, poll.getStatus() != null ? poll.getStatus() : "Draft");
            stmt.setInt(5, poll.getTotalVotes());
            stmt.setInt(6, poll.getCreatedBy());  // Set the creator ID
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
            // Refresh the cached list
            getAllPolls();
        } catch (SQLException e) {
            System.err.println("Error creating poll: " + e.getMessage());
            e.printStackTrace();
        }
        return generatedId;
    }
    
    @Override
    public boolean updatePoll(Poll poll) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE polls SET name = ?, candidate1 = ?, candidate2 = ?, status = ? WHERE id = ?")) {
            stmt.setString(1, poll.getName());
            stmt.setString(2, poll.getCandidate1());
            stmt.setString(3, poll.getCandidate2());
            stmt.setString(4, poll.getStatus());
            stmt.setInt(5, poll.getId());
            int affectedRows = stmt.executeUpdate();
            // Refresh the cached list
            getAllPolls();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating poll: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deletePoll(int pollId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            // Try to delete any votes for this poll first (if the table exists)
            try {
                PreparedStatement deleteVotes = conn.prepareStatement("DELETE FROM votes WHERE poll_id = ?");
                deleteVotes.setInt(1, pollId);
                deleteVotes.executeUpdate();
                deleteVotes.close();
            } catch (SQLException e) {
                // Votes table might not exist yet, continue with poll deletion
                System.err.println("Note: Could not delete votes (table may not exist): " + e.getMessage());
            }
            // Then delete the poll itself
            PreparedStatement deletePoll = conn.prepareStatement("DELETE FROM polls WHERE id = ?");
            deletePoll.setInt(1, pollId);
            int affectedRows = deletePoll.executeUpdate();
            deletePoll.close();
            // Refresh the cached list
            getAllPolls();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting poll: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean activatePoll(int pollId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE polls SET status = 'Active' WHERE id = ?")) {
            stmt.setInt(1, pollId);
            int affectedRows = stmt.executeUpdate();
            // Refresh the cached list
            getAllPolls();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error activating poll: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean completePoll(int pollId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE polls SET status = 'Completed' WHERE id = ?")) {
            stmt.setInt(1, pollId);
            int affectedRows = stmt.executeUpdate();
            // Refresh the cached list
            getAllPolls();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error completing poll: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int getPollCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM polls")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting polls: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int getActivePollCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM polls WHERE status = 'Active'")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting active polls: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int getCompletedPollCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM polls WHERE status = 'Completed'")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting completed polls: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int getTotalVoteCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(total_votes) FROM polls")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total votes: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}