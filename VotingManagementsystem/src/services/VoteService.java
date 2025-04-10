package services;

import models.Vote;
import java.util.List;

/**
 * Service interface for Vote operations
 */
public interface VoteService {
    /**
     * Cast a vote for a specific poll and candidate
     * 
     * @param vote Vote to cast
     * @return true if successful
     */
    boolean castVote(Vote vote);
    
    /**
     * Check if a user has already voted in a poll
     * 
     * @param userId User ID
     * @param pollId Poll ID
     * @return true if the user has already voted
     */
    boolean hasUserVoted(int userId, int pollId);
    
    /**
     * Get votes for a specific poll
     * 
     * @param pollId Poll ID
     * @return List of votes for the poll
     */
    List<Vote> getVotesForPoll(int pollId);
    
    /**
     * Get the count of votes for a specific candidate in a poll
     * 
     * @param pollId Poll ID
     * @param candidateId Candidate ID (1 or 2)
     * @return Number of votes
     */
    int getVoteCountForCandidate(int pollId, int candidateId);
    
    /**
     * Get all polls that the user has voted in
     * 
     * @param userId User ID
     * @return List of poll IDs
     */
    List<Integer> getPollsVotedByUser(int userId);
    
    /**
     * Get user's vote for a specific poll
     * 
     * @param userId User ID
     * @param pollId Poll ID
     * @return User's vote or null if not found
     */
    Vote getUserVote(int userId, int pollId);
}