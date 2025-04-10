package services;

import javafx.collections.ObservableList;
import models.Poll;

/**
 * Service interface for Poll operations
 */
public interface PollService {
    /**
     * Get all polls
     * 
     * @return List of all polls
     */
    ObservableList<Poll> getAllPolls();
    
    /**
     * Create a new poll
     * 
     * @param poll Poll to create
     * @return ID of the created poll
     */
    int createPoll(Poll poll);
    
    /**
     * Update an existing poll
     * 
     * @param poll Poll to update
     * @return true if successful
     */
    boolean updatePoll(Poll poll);
    
    /**
     * Delete a poll
     * 
     * @param pollId ID of poll to delete
     * @return true if successful
     */
    boolean deletePoll(int pollId);
    
    /**
     * Activate a poll
     * 
     * @param pollId ID of poll to activate
     * @return true if successful
     */
    boolean activatePoll(int pollId);
    
    /**
     * Complete a poll
     * 
     * @param pollId ID of poll to complete
     * @return true if successful
     */
    boolean completePoll(int pollId);
    
    /**
     * Get the total count of polls
     * 
     * @return Number of polls
     */
    int getPollCount();
    
    /**
     * Get the count of active polls
     * 
     * @return Number of active polls
     */
    int getActivePollCount();
    
    /**
     * Get the count of completed polls
     * 
     * @return Number of completed polls
     */
    int getCompletedPollCount();
    
    /**
     * Get the total number of votes across all polls
     * 
     * @return Total vote count
     */
    int getTotalVoteCount();
}