package models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class representing a poll in the system
 */
public class Poll {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty candidate1;
    private final StringProperty candidate2;
    private final StringProperty status;
    private final IntegerProperty totalVotes;
    private final IntegerProperty createdBy;
    
    /**
     * Create a new Poll with all fields
     */
    public Poll(int id, String name, String candidate1, String candidate2, 
                String status, int totalVotes, int createdBy) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.candidate1 = new SimpleStringProperty(candidate1);
        this.candidate2 = new SimpleStringProperty(candidate2);
        this.status = new SimpleStringProperty(status);
        this.totalVotes = new SimpleIntegerProperty(totalVotes);
        this.createdBy = new SimpleIntegerProperty(createdBy);
    }
    
    /**
     * Create a new Poll with essential fields
     */
    public Poll(int id, String name, String candidate1, String candidate2, String status, int totalVotes) {
        this(id, name, candidate1, candidate2, status, totalVotes, 1); // Default creator is admin (ID 1)
    }
    
    /**
     * Create a minimal new Poll (for creation)
     */
    public Poll(String name, String candidate1, String candidate2) {
        this(0, name, candidate1, candidate2, "Draft", 0, 1); // Default creator is admin (ID 1)
    }
    
    // Getters
    public int getId() {
        return id.get();
    }
    
    public String getName() {
        return name.get();
    }
    
    public String getCandidate1() {
        return candidate1.get();
    }
    
    public String getCandidate2() {
        return candidate2.get();
    }
    
    public String getStatus() {
        return status.get();
    }
    
    public int getTotalVotes() {
        return totalVotes.get();
    }
    
    public int getCreatedBy() {
        return createdBy.get();
    }
    
    // Setters
    public void setName(String name) {
        this.name.set(name);
    }
    
    public void setCandidate1(String candidate1) {
        this.candidate1.set(candidate1);
    }
    
    public void setCandidate2(String candidate2) {
        this.candidate2.set(candidate2);
    }
    
    public void setStatus(String status) {
        this.status.set(status);
    }
    
    public void setTotalVotes(int totalVotes) {
        this.totalVotes.set(totalVotes);
    }
    
    // Property getters for JavaFX binding
    public IntegerProperty idProperty() {
        return id;
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public StringProperty candidate1Property() {
        return candidate1;
    }
    
    public StringProperty candidate2Property() {
        return candidate2;
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    public IntegerProperty totalVotesProperty() {
        return totalVotes;
    }
    
    public IntegerProperty createdByProperty() {
        return createdBy;
    }
}