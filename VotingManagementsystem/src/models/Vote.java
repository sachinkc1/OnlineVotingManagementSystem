package models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Model class representing a vote cast by a user
 */
public class Vote {
    private final IntegerProperty id;
    private final IntegerProperty pollId;
    private final IntegerProperty userId;
    private final IntegerProperty candidateId;

    /**
     * Create a new Vote with all fields
     */
    public Vote(int id, int pollId, int userId, int candidateId) {
        this.id = new SimpleIntegerProperty(id);
        this.pollId = new SimpleIntegerProperty(pollId);
        this.userId = new SimpleIntegerProperty(userId);
        this.candidateId = new SimpleIntegerProperty(candidateId);
    }

    /**
     * Create a new Vote for submission (ID will be generated by database)
     */
    public Vote(int pollId, int userId, int candidateId) {
        this(0, pollId, userId, candidateId);
    }

    // Getters
    public int getId() {
        return id.get();
    }

    public int getPollId() {
        return pollId.get();
    }

    public int getUserId() {
        return userId.get();
    }

    public int getCandidateId() {
        return candidateId.get();
    }

    // Property getters for JavaFX binding
    public IntegerProperty idProperty() {
        return id;
    }

    public IntegerProperty pollIdProperty() {
        return pollId;
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public IntegerProperty candidateIdProperty() {
        return candidateId;
    }
}