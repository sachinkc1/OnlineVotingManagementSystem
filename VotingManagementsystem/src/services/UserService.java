package services;

import javafx.collections.ObservableList;
import models.User;

public interface UserService {
    ObservableList<User> getAllUsers();
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(int userId);
}