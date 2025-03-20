package models;

import javafx.beans.property.*;

public class Poll {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty candidate1 = new SimpleStringProperty();
    private final StringProperty candidate2 = new SimpleStringProperty();
    private final IntegerProperty totalVotes = new SimpleIntegerProperty();

    public Poll(int id, String name, String candidate1, String candidate2, int totalVotes) {
        setId(id);
        setName(name);
        setCandidate1(candidate1);
        setCandidate2(candidate2);
        setTotalVotes(totalVotes);
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public String getCandidate1() { return candidate1.get(); }
    public void setCandidate1(String value) { candidate1.set(value); }
    public StringProperty candidate1Property() { return candidate1; }

    public String getCandidate2() { return candidate2.get(); }
    public void setCandidate2(String value) { candidate2.set(value); }
    public StringProperty candidate2Property() { return candidate2; }

    public int getTotalVotes() { return totalVotes.get(); }
    public void setTotalVotes(int value) { totalVotes.set(value); }
    public IntegerProperty totalVotesProperty() { return totalVotes; }
}