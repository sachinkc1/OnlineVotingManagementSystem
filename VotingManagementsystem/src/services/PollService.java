package services;

import javafx.collections.ObservableList;
import models.Poll;

public interface PollService {
    ObservableList<Poll> getAllPolls();
    void createPoll(Poll poll);
    void updatePoll(Poll poll);
    void voteForCandidate(int pollId, String candidate);
}