package services;

import javafx.collections.ObservableList;
import models.User;

public interface UserService {
    ObservableList<User> getAllUsers();
    int addUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(int userId);
    int getUserCount();
}