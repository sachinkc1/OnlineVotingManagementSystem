package services;

import models.Poll;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class DatabasePollService implements PollService {
    private final DatabaseConnection dbConnection;

    public DatabasePollService(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public ObservableList<Poll> getAllPolls() {
        ObservableList<Poll> polls = FXCollections.observableArrayList();
        String query = "SELECT * FROM polls";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                polls.add(new Poll(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("candidate1"),
                    rs.getString("candidate2"),
                    rs.getInt("total_votes")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching polls", e);
        }
        return polls;
    }

    @Override
    public void createPoll(Poll poll) {
        String query = "INSERT INTO polls (name, candidate1, candidate2, total_votes) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, poll.getName());
            pstmt.setString(2, poll.getCandidate1());
            pstmt.setString(3, poll.getCandidate2());
            pstmt.setInt(4, poll.getTotalVotes());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    poll.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating poll", e);
        }
    }

    @Override
    public void updatePoll(Poll poll) {
        String query = "UPDATE polls SET name = ?, candidate1 = ?, candidate2 = ?, total_votes = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, poll.getName());
            pstmt.setString(2, poll.getCandidate1());
            pstmt.setString(3, poll.getCandidate2());
            pstmt.setInt(4, poll.getTotalVotes());
            pstmt.setInt(5, poll.getId());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating poll", e);
        }
    }

    @Override
    public void voteForCandidate(int pollId, String candidate) {
        String query = "UPDATE polls SET total_votes = total_votes + 1 WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pollId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating vote count", e);
        }
    }
}