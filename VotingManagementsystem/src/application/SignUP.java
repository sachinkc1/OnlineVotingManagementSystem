package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUP extends Application {

    private TextField fullNameTextField;
    private ComboBox<String> genderCombo;
    private TextField phoneTextField;
    private TextField emailTextField;
    private PasswordField passwordField;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("We Votes SignUP");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);

        // Left Section
        VBox leftSection = createLeftSection();

        // Right Section
        GridPane rightSection = createRightSection();

        // Main Layout
        HBox mainLayout = new HBox();
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(leftSection, rightSection);

        VBox squareLayout = new VBox();
        squareLayout.setAlignment(Pos.CENTER);
        squareLayout.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        squareLayout.setPadding(new Insets(20));
        squareLayout.getChildren().add(mainLayout);

        BorderPane root = new BorderPane();
        root.setCenter(squareLayout);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createLeftSection() {
        VBox leftSection = new VBox();
        leftSection.setAlignment(Pos.CENTER);
        leftSection.setStyle("-fx-background-color: #4B0082;");
        leftSection.setPrefWidth(600);
        leftSection.setPrefHeight(800);

        Text welcomeText = new Text("WELCOME\nTO\nSignUp");
        welcomeText.setFill(Color.WHITE);
        welcomeText.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        welcomeText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        leftSection.getChildren().add(welcomeText);

        return leftSection;
    }

    private GridPane createRightSection() {
        GridPane rightSection = new GridPane();
        rightSection.setAlignment(Pos.CENTER);
        rightSection.setHgap(10);
        rightSection.setVgap(20);
        rightSection.setPadding(new Insets(25, 25, 25, 25));
        rightSection.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        rightSection.setPrefWidth(600);
        rightSection.setPrefHeight(800);

        Text sceneTitle = new Text("Sign Up");
        sceneTitle.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        sceneTitle.setFill(Color.PURPLE);
        rightSection.add(sceneTitle, 0, 0, 2, 1);

        // Full Name
        fullNameTextField = addField(rightSection, "Full Name:", 1);

        // Gender
        genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female");
        addComboBox(rightSection, "Sex:", genderCombo, 2);

        // Phone
        phoneTextField = addField(rightSection, "Phone Number:", 3);

        // Email
        emailTextField = addField(rightSection, "Email:", 4);

        // Password
        passwordField = new PasswordField();
        addPasswordField(rightSection, "Password:", passwordField, 5);

        // Sign Up Button
        Button signUpBtn = new Button("Sign Up");
        signUpBtn.setStyle("-fx-background-color: #4B0082; -fx-text-fill: white;");
        signUpBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        signUpBtn.setPrefHeight(40);
        signUpBtn.setOnAction(e -> handleSignUp());

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(signUpBtn);
        rightSection.add(hbBtn, 0, 7, 2, 1);

        return rightSection;
    }

    private TextField addField(GridPane grid, String labelText, int row) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        grid.add(label, 0, row);

        TextField textField = new TextField();
        textField.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        textField.setPrefHeight(40);
        grid.add(textField, 1, row);
        return textField;
    }

    private void addComboBox(GridPane grid, String labelText, ComboBox<String> comboBox, int row) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        grid.add(label, 0, row);

        comboBox.setPromptText("Select Gender");
        comboBox.setMaxWidth(300);
        comboBox.setStyle("-fx-font-size: 18px;");
        grid.add(comboBox, 1, row);
    }

    private void addPasswordField(GridPane grid, String labelText, PasswordField passwordField, int row) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        grid.add(label, 0, row);

        passwordField.setPromptText("Password");
        passwordField.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        passwordField.setPrefHeight(40);
        grid.add(passwordField, 1, row);
    }

    private void handleSignUp() {
        String fullName = fullNameTextField.getText();
        String gender = genderCombo.getValue();
        String phone = phoneTextField.getText();
        String email = emailTextField.getText();
        String password = passwordField.getText();

        if (fullName.isEmpty() || gender == null || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "All fields are required!");
            return;
        }

        try {
            boolean success = insertUser(fullName, gender, phone, email, password);
            if (success) {
                showAlert("Success", "Sign Up Successful!");
                clearFields();
            } else {
                showAlert("Error", "Sign Up Failed!");
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean insertUser(String fullName, String gender, String phone, String email, String password) {
        String sql = "INSERT INTO signup (full_name, gender, phone, email, password) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, gender);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);
            pstmt.setString(5, password);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException ex) {
            showAlert("Database Error", "Error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        fullNameTextField.clear();
        genderCombo.getSelectionModel().clearSelection();
        phoneTextField.clear();
        emailTextField.clear();
        passwordField.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}