package models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class representing a user in the system
 * Maps to the signup table in the database
 */
public class User {
    private final IntegerProperty id;
    private final StringProperty fullName;
    private final StringProperty gender;
    private final StringProperty phone;
    private final StringProperty email;
    private final StringProperty password;
    
    /**
     * Create a new User with all fields
     */
    public User(int id, String fullName, String gender, String phone, String email, 
                String password) {
        this.id = new SimpleIntegerProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.gender = new SimpleStringProperty(gender);
        this.phone = new SimpleStringProperty(phone);
        this.email = new SimpleStringProperty(email);
        this.password = new SimpleStringProperty(password);
    }
    
    /**
     * Create a new User with essential fields (for display in admin panel)
     */
    public User(int id, String fullName, String gender, String phone, String email) {
        this(id, fullName, gender, phone, email, "");
    }
    
    // Getters
    public int getId() {
        return id.get();
    }
    
    public String getFullName() {
        return fullName.get();
    }
    
    public String getGender() {
        return gender.get();
    }
    
    public String getPhone() {
        return phone.get();
    }
    
    public String getEmail() {
        return email.get();
    }
    
    public String getPassword() {
        return password.get();
    }
    
    // Setters
    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }
    
    public void setGender(String gender) {
        this.gender.set(gender);
    }
    
    public void setPhone(String phone) {
        this.phone.set(phone);
    }
    
    public void setEmail(String email) {
        this.email.set(email);
    }
    
    public void setPassword(String password) {
        this.password.set(password);
    }
    
    // Property getters for JavaFX binding
    public IntegerProperty idProperty() {
        return id;
    }
    
    public StringProperty fullNameProperty() {
        return fullName;
    }
    
    public StringProperty genderProperty() {
        return gender;
    }
    
    public StringProperty phoneProperty() {
        return phone;
    }
    
    public StringProperty emailProperty() {
        return email;
    }

	public void setId(int i) {
		// TODO Auto-generated method stub
		
	}
}