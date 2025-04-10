package services;

import models.Vote;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Implementation of VoteService that uses a database with enhanced double-voting prevention
 */
public class DatabaseVoteService implements VoteService {
    
    // In-memory cache to reduce database calls for repeated vote checks
    private final Map<String, Boolean> voteCache = new ConcurrentHashMap<>();
    
    /**
     * Ensure votes table has unique constraint to prevent double voting
     */
    private void ensureVotesTableConstraint() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Checking for unique constraint on votes table...");
            
            // Try to create the constraint - will fail if it already exists
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE votes ADD CONSTRAINT user_poll_unique UNIQUE (user_id, poll_id)");
                System.out.println("Added unique constraint to votes table to prevent double voting");
            } catch (SQLException e) {
                // If it's because constraint already exists, that's ok
                if (e.getMessage().contains("Duplicate") || e.getMessage().contains("already exists")) {
                    System.out.println("Unique constraint for votes already exists");
                } else {
                    // Otherwise, it's a different error
                    System.err.println("Error creating constraint: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public DatabaseVoteService() {
        // Ensure the database has the constraint to prevent double voting
        ensureVotesTableConstraint();
    }
    
    @Override
    public boolean castVote(Vote vote) {
        System.out.println("Attempting to cast vote: User " + vote.getUserId() + 
                           " for Poll " + vote.getPollId() + 
                           " selecting Candidate " + vote.getCandidateId());
                           
        // Build cache key
        String cacheKey = vote.getUserId() + "_" + vote.getPollId();
        
        // Check cache first for performance
        if (voteCache.containsKey(cacheKey) && voteCache.get(cacheKey)) {
            System.out.println("Cache hit: User " + vote.getUserId() + " has already voted in poll " + 
                              vote.getPollId() + ". Preventing duplicate vote.");
            return false;
        }
        
        // Now check database - CRITICAL check
        if (hasUserVoted(vote.getUserId(), vote.getPollId())) {
            System.out.println("User " + vote.getUserId() + " has already voted in poll " + 
                              vote.getPollId() + ". Preventing duplicate vote.");
            
            // Update cache
            voteCache.put(cacheKey, true);
            return false;
        }
        
        // User hasn't voted - proceed with insert in a transaction
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Use transaction to ensure vote is recorded properly
            conn.setAutoCommit(false);
            
            try {
                // First insert the vote
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO votes (poll_id, user_id, candidate_id) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    
                    insertStmt.setInt(1, vote.getPollId());
                    insertStmt.setInt(2, vote.getUserId());
                    insertStmt.setInt(3, vote.getCandidateId());
                    
                    int insertResult = insertStmt.executeUpdate();
                    System.out.println("Vote insert result: " + insertResult + " row(s) affected");
                    
                    if (insertResult > 0) {
                        // Now update total votes in the poll using a subquery for accuracy
                        try (PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE polls SET total_votes = (SELECT COUNT(*) FROM votes WHERE poll_id = ?) WHERE id = ?")) {
                            
                            updateStmt.setInt(1, vote.getPollId());
                            updateStmt.setInt(2, vote.getPollId());
                            
                            int updateResult = updateStmt.executeUpdate();
                            System.out.println("Poll update result: " + updateResult + " row(s) affected");
                            
                            // If all operations were successful, commit
                            conn.commit();
                            System.out.println("Vote transaction committed successfully!");
                            
                            // Update cache on successful vote
                            voteCache.put(cacheKey, true);
                            
                            return true;
                        }
                    } else {
                        // Something went wrong with the insert
                        conn.rollback();
                        System.out.println("Vote insert failed, transaction rolled back");
                        return false;
                    }
                }
            } catch (SQLException ex) {
                // Handle constraint violation specifically
                conn.rollback();
                
                // Check for unique constraint violation (varies by database system)
                if (ex.getMessage().contains("user_poll_unique") || 
                    ex.getMessage().contains("Duplicate entry") ||
                    ex.getSQLState().equals("23000") ||  // MySQL
                    ex.getSQLState().equals("23505")) {  // PostgreSQL
                    
                    System.out.println("Duplicate vote detected by database constraint: " + ex.getMessage());
                    
                    // Update cache to prevent future attempts
                    voteCache.put(cacheKey, true);
                    
                    return false;
                }
                
                System.err.println("Error in vote transaction: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            } finally {
                // Always reset auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a user has already voted in a poll
     * 
     * @param userId User ID
     * @param pollId Poll ID
     * @return true if the user has already voted
     */
    @Override
    public boolean hasUserVoted(int userId, int pollId) {
        if (userId <= 0 || pollId <= 0) {
            System.out.println("Invalid userId or pollId in hasUserVoted check: userId=" + userId + ", pollId=" + pollId);
            return false;
        }
        
        System.out.println("Checking if user " + userId + " has voted in poll " + pollId);
        
        // First try checking with a COUNT query for efficiency
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM votes WHERE user_id = ? AND poll_id = ?")) {
                 
            stmt.setInt(1, userId);
            stmt.setInt(2, pollId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    boolean hasVoted = count > 0;
                    
                    System.out.println("Database count check: User " + userId + 
                                      (hasVoted ? " HAS voted" : " has NOT voted") + 
                                      " in poll " + pollId + " (count=" + count + ")");
                    
                    // If count says they voted, double-check by fetching actual record
                    if (hasVoted) {
                        PreparedStatement verifyStmt = conn.prepareStatement(
                            "SELECT id, candidate_id FROM votes WHERE user_id = ? AND poll_id = ?");
                        verifyStmt.setInt(1, userId);
                        verifyStmt.setInt(2, pollId);
                        
                        ResultSet verifyRs = verifyStmt.executeQuery();
                        if (verifyRs.next()) {
                            System.out.println("Verified vote: ID=" + verifyRs.getInt("id") + 
                                              ", Candidate=" + verifyRs.getInt("candidate_id"));
                        } else {
                            System.out.println("Warning: Count showed votes but none found in direct query!");
                            hasVoted = false;  // Correct the result
                        }
                        verifyRs.close();
                        verifyStmt.close();
                    }
                    
                    return hasVoted;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking if user voted: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Clear the vote cache (useful for testing or after database changes)
     */
    public void clearVoteCache() {
        voteCache.clear();
        System.out.println("Vote cache cleared");
    }
    
    /**
     * Get votes for a specific poll
     * 
     * @param pollId Poll ID
     * @return List of votes for the poll
     */
    @Override
    public List<Vote> getVotesForPoll(int pollId) {
        List<Vote> votes = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, poll_id, user_id, candidate_id FROM votes WHERE poll_id = ?")) {
                 
            stmt.setInt(1, pollId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    votes.add(new Vote(
                        rs.getInt("id"),
                        rs.getInt("poll_id"),
                        rs.getInt("user_id"),
                        rs.getInt("candidate_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting votes for poll: " + e.getMessage());
            e.printStackTrace();
        }
        return votes;
    }
    
    /**
     * Get the count of votes for a specific candidate in a poll
     * 
     * @param pollId Poll ID
     * @param candidateId Candidate ID (1 or 2)
     * @return Number of votes
     */
    @Override
    public int getVoteCountForCandidate(int pollId, int candidateId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM votes WHERE poll_id = ? AND candidate_id = ?")) {
                 
            stmt.setInt(1, pollId);
            stmt.setInt(2, candidateId);
            
            System.out.println("Executing SQL: SELECT COUNT(*) FROM votes WHERE poll_id = " + 
                              pollId + " AND candidate_id = " + candidateId);
                              
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Vote count for candidate " + candidateId + 
                                      " in poll " + pollId + ": " + count);
                    return count;
                }
            }
            
            System.out.println("No votes found");
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting vote count for candidate: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get all polls that the user has voted in
     * 
     * @param userId User ID
     * @return List of poll IDs
     */
    @Override
    public List<Integer> getPollsVotedByUser(int userId) {
        List<Integer> votedPolls = new ArrayList<>();
        
        if (userId <= 0) {
            System.out.println("Invalid userId in getPollsVotedByUser: " + userId);
            return votedPolls;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT DISTINCT poll_id FROM votes WHERE user_id = ?")) {
                 
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int pollId = rs.getInt("poll_id");
                    votedPolls.add(pollId);
                    
                    // Update cache for each poll
                    voteCache.put(userId + "_" + pollId, true);
                    
                    System.out.println("Found vote by user " + userId + " for poll " + pollId);
                }
            }
            
            System.out.println("User " + userId + " has voted in " + votedPolls.size() + " polls: " + votedPolls);
        } catch (SQLException e) {
            System.err.println("Error getting polls voted by user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return votedPolls;
    }
    
    /**
     * Get user's vote for a specific poll
     * 
     * @param userId User ID
     * @param pollId Poll ID
     * @return User's vote or null if not found
     */
    @Override
    public Vote getUserVote(int userId, int pollId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, poll_id, user_id, candidate_id FROM votes WHERE user_id = ? AND poll_id = ?")) {
                 
            stmt.setInt(1, userId);
            stmt.setInt(2, pollId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // If vote is found, update the cache
                    voteCache.put(userId + "_" + pollId, true);
                    
                    return new Vote(
                        rs.getInt("id"),
                        rs.getInt("poll_id"),
                        rs.getInt("user_id"),
                        rs.getInt("candidate_id")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user vote: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}