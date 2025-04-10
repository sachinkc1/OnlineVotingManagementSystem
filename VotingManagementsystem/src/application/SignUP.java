package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import services.DatabaseService;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class SignUP extends Application {
    
    private TextField fullNameTextField;
    private ComboBox<String> genderCombo;
    private TextField phoneTextField;
    private TextField emailTextField;
    private PasswordField passwordField;
    private Label validationLabel;
    private DatabaseService dbService;
    
    public SignUP() {
        this.dbService = new DatabaseService();
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("We Votes SignUP");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        
        // Main container with modern styling
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");
        
        // Create card-like container for signup
        StackPane cardContainer = new StackPane();
        cardContainer.setPadding(new Insets(20));
        cardContainer.setMaxWidth(1000);
        cardContainer.setMaxHeight(600);
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(3);
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        
        // Create card background
        Rectangle cardBg = new Rectangle();
        cardBg.setWidth(1000);
        cardBg.setHeight(600);
        cardBg.setArcWidth(20);
        cardBg.setArcHeight(20);
        cardBg.setFill(Color.WHITE);
        cardBg.setEffect(shadow);
        
        // Layout for content
        HBox contentLayout = new HBox();
        
        // Left Section - Branding
        VBox leftSection = createLeftSection();
        
        // Right Section - Signup Form
        VBox rightSection = createRightFormSection(primaryStage);
        
        // Add left and right sections to the content layout
        contentLayout.getChildren().addAll(leftSection, rightSection);
        
        // Add all elements to the card container
        cardContainer.getChildren().addAll(cardBg, contentLayout);
        
        // Center the card in the root pane
        root.setCenter(cardContainer);
        
        // Add footer with copyright info
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        Text footerText = new Text("© 2023 We Votes. All rights reserved.");
        footerText.setFill(Color.rgb(100, 100, 100));
        footer.getChildren().add(footerText);
        root.setBottom(footer);
        BorderPane.setMargin(footer, new Insets(10));
        
        Scene scene = new Scene(root, 1200, 800);
        // Load CSS - adjusted for your file structure
        scene.getStylesheets().add(getClass().getResource("/resources/application.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createLeftSection() {
        VBox leftSection = new VBox(30);
        leftSection.setAlignment(Pos.CENTER);
        leftSection.setPrefWidth(500);
        leftSection.setPadding(new Insets(40));
        leftSection.setStyle("-fx-background-color: linear-gradient(to bottom right, #6A5ACD, #4B0082); " +
                             "-fx-background-radius: 10 0 0 10;");
        
        // Logo or App Icon
        Circle logoCircle = new Circle(60);
        logoCircle.setFill(Color.WHITE);
        logoCircle.setOpacity(0.2);
        
        Text logoText = new Text("WV");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        logoText.setFill(Color.WHITE);
        
        StackPane logoStack = new StackPane();
        logoStack.getChildren().addAll(logoCircle, logoText);
        
        // Welcome Text
        Text welcomeTitle = new Text("JOIN US ON");
        welcomeTitle.setFont(Font.font("Arial", FontWeight.LIGHT, 24));
        welcomeTitle.setFill(Color.WHITE);
        
        Text appName = new Text("WE VOTES");
        appName.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        appName.setFill(Color.WHITE);
        
        // Description or tagline
        Text tagline = new Text("Create an account to participate in transparent voting");
        tagline.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        tagline.setFill(Color.WHITE);
        tagline.setWrappingWidth(400);
        tagline.setTextAlignment(TextAlignment.CENTER);
        
        // Benefits list
        VBox benefitsBox = new VBox(15);
        benefitsBox.setAlignment(Pos.CENTER_LEFT);
        benefitsBox.setPadding(new Insets(20, 0, 0, 40));
        
        String[] benefits = {
            "Participate in voting",
            "Access your voting",
        };
        
        for (String benefit : benefits) {
            HBox benefitRow = new HBox(10);
            benefitRow.setAlignment(Pos.CENTER_LEFT);
            
            Text bulletPoint = new Text("•");
            bulletPoint.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            bulletPoint.setFill(Color.WHITE);
            
            Text benefitText = new Text(benefit);
            benefitText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            benefitText.setFill(Color.WHITE);
            
            benefitRow.getChildren().addAll(bulletPoint, benefitText);
            benefitsBox.getChildren().add(benefitRow);
        }
        
        leftSection.getChildren().addAll(logoStack, welcomeTitle, appName, tagline, benefitsBox);
        return leftSection;
    }
    
    private VBox createRightFormSection(Stage primaryStage) {
        VBox rightSection = new VBox(18);
        rightSection.setAlignment(Pos.CENTER);
        rightSection.setPrefWidth(500);
        rightSection.setPadding(new Insets(40, 60, 40, 60));
        
        // Signup Title
        Text signupTitle = new Text("Create Account");
        signupTitle.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        signupTitle.setFill(Color.rgb(74, 20, 140));
        
        Text signupSubtitle = new Text("Please fill in your information to create an account");
        signupSubtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        signupSubtitle.setFill(Color.rgb(100, 100, 100));
        
        // Full Name field
        VBox fullNameBox = new VBox(6);
        Label fullNameLabel = new Label("Full Name");
        fullNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        fullNameLabel.setTextFill(Color.rgb(80, 80, 80));
        
        fullNameTextField = new TextField();
        fullNameTextField.setPromptText("Enter your full name");
        fullNameTextField.setPrefHeight(40);
        fullNameTextField.setStyle("-fx-background-color: #f5f5f5; " +
                                "-fx-border-color: #e0e0e0; " +
                                "-fx-border-radius: 5; " +
                                "-fx-background-radius: 5; " +
                                "-fx-font-size: 14px; " +
                                "-fx-padding: 8;");
        
        fullNameBox.getChildren().addAll(fullNameLabel, fullNameTextField);
        
        // Gender field
        VBox genderBox = new VBox(6);
        Label genderLabel = new Label("Gender");
        genderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        genderLabel.setTextFill(Color.rgb(80, 80, 80));
        
        genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female");
        genderCombo.setPromptText("Select your gender");
        genderCombo.setPrefHeight(40);
        genderCombo.setPrefWidth(Double.MAX_VALUE);
        genderCombo.setStyle("-fx-background-color: #f5f5f5; " +
                          "-fx-border-color: #e0e0e0; " +
                          "-fx-border-radius: 5; " +
                          "-fx-background-radius: 5; " +
                          "-fx-font-size: 14px;");
        
        genderBox.getChildren().addAll(genderLabel, genderCombo);
        
        // Phone Number field
        VBox phoneBox = new VBox(6);
        Label phoneLabel = new Label("Phone Number");
        phoneLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        phoneLabel.setTextFill(Color.rgb(80, 80, 80));
        
        phoneTextField = new TextField();
        phoneTextField.setPromptText("Enter your phone number");
        phoneTextField.setPrefHeight(40);
        phoneTextField.setStyle("-fx-background-color: #f5f5f5; " +
                             "-fx-border-color: #e0e0e0; " +
                             "-fx-border-radius: 5; " +
                             "-fx-background-radius: 5; " +
                             "-fx-font-size: 14px; " +
                             "-fx-padding: 8;");
        
        phoneBox.getChildren().addAll(phoneLabel, phoneTextField);
        
        // Email field
        VBox emailBox = new VBox(6);
        Label emailLabel = new Label("Email Address");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        emailLabel.setTextFill(Color.rgb(80, 80, 80));
        
        emailTextField = new TextField();
        emailTextField.setPromptText("Enter your email address");
        emailTextField.setPrefHeight(40);
        emailTextField.setStyle("-fx-background-color: #f5f5f5; " +
                             "-fx-border-color: #e0e0e0; " +
                             "-fx-border-radius: 5; " +
                             "-fx-background-radius: 5; " +
                             "-fx-font-size: 14px; " +
                             "-fx-padding: 8;");
        
        emailBox.getChildren().addAll(emailLabel, emailTextField);
        
        // Password field
        VBox passwordBox = new VBox(6);
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        passwordLabel.setTextFill(Color.rgb(80, 80, 80));
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Create a strong password (min. 6 characters)");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-color: #f5f5f5; " +
                            "-fx-border-color: #e0e0e0; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 8;");
        
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        // Validation Label
        validationLabel = new Label();
        validationLabel.setTextFill(Color.RED);
        validationLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        validationLabel.setWrapText(true);
        validationLabel.setMaxWidth(380);
        
        // Sign Up button
        Button signUpButton = new Button("CREATE ACCOUNT");
        signUpButton.setPrefHeight(45);
        signUpButton.setPrefWidth(Double.MAX_VALUE);
        signUpButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        signUpButton.setStyle("-fx-background-color: linear-gradient(to right, #6A5ACD, #4B0082); " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 5;");
        
        // Add hover effect
        signUpButton.setOnMouseEntered(e -> 
            signUpButton.setStyle("-fx-background-color: linear-gradient(to right, #5A4ABD, #3A0071); " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 5;")
        );
        
        signUpButton.setOnMouseExited(e -> 
            signUpButton.setStyle("-fx-background-color: linear-gradient(to right, #6A5ACD, #4B0082); " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 5;")
        );
        
        signUpButton.setOnAction(e -> handleSignUp());
        
        // Already have an account section
        HBox loginBox = new HBox(5);
        loginBox.setAlignment(Pos.CENTER);
        Text haveAccountText = new Text("Already have an account?");
        haveAccountText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        haveAccountText.setFill(Color.rgb(100, 100, 100));
        
        Button loginButton = new Button("Log In");
        loginButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B0082; -fx-font-weight: bold; -fx-cursor: hand;");
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        loginButton.setOnAction(e -> {
            // Open the login page
            Login loginApp = new Login();
            try {
                loginApp.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        loginBox.getChildren().addAll(haveAccountText, loginButton);
        
        rightSection.getChildren().addAll(
            signupTitle,
            signupSubtitle,
            fullNameBox,
            genderBox,
            phoneBox,
            emailBox,
            passwordBox,
            validationLabel,
            signUpButton,
            loginBox
        );
        
        return rightSection;
    }
    
    private void handleSignUp() {
        String fullName = fullNameTextField.getText().trim();
        String gender = genderCombo.getValue();
        String phone = phoneTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String password = passwordField.getText();
        
        // Clear previous validation messages
        validationLabel.setText("");
        
        // Validation
        StringBuilder validationErrors = new StringBuilder();
        if (fullName.isEmpty()) {
            validationErrors.append("• Full Name is required\n");
        }
        if (gender == null) {
            validationErrors.append("• Gender selection is required\n");
        }
        if (phone.isEmpty()) {
            validationErrors.append("• Phone Number is required\n");
        } else if (!isValidPhone(phone)) {
            validationErrors.append("• Invalid Phone Number format\n");
        }
        if (email.isEmpty()) {
            validationErrors.append("• Email is required\n");
        } else if (!isValidEmail(email)) {
            validationErrors.append("• Invalid Email format\n");
        }
        if (password.isEmpty()) {
            validationErrors.append("• Password is required\n");
        } else if (password.length() < 6) {
            validationErrors.append("• Password must be at least 6 characters\n");
        }
        
        if (validationErrors.length() > 0) {
            // Show validation errors
            validationLabel.setText(validationErrors.toString());
            return;
        }
        
        try {
            boolean success = dbService.registerUser(fullName, gender, phone, email, password);
            if (success) {
                showCustomSuccessPopup(fullName);
                clearFields();
            } else {
                showErrorPopup("Sign Up Failed", "Unable to create user account. Please try again.");
            }
        } catch (SQLException ex) {
            showErrorPopup("Database Error", "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
    
    private boolean isValidPhone(String phone) {
        String phoneRegex = "^\\d{10,15}$";
        return Pattern.compile(phoneRegex).matcher(phone).matches();
    }
    
    private void showCustomSuccessPopup(String userName) {
        // Create a custom stage for the success popup
        Stage successStage = new Stage();
        successStage.initModality(Modality.APPLICATION_MODAL);
        successStage.initStyle(StageStyle.TRANSPARENT);
        successStage.setTitle("Sign Up Successful");
        
        // Create the content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 15;");
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(15);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        content.setEffect(dropShadow);
        
        // Success icon (checkmark in a circle)
        StackPane checkPane = new StackPane();
        Circle checkCircle = new Circle(50);
        checkCircle.setFill(Color.rgb(75, 0, 130, 0.9));
        
        Text checkmark = new Text("✓");
        checkmark.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        checkmark.setFill(Color.WHITE);
        
        checkPane.getChildren().addAll(checkCircle, checkmark);
        
        // Success text
        Text successHeaderText = new Text("Account Created Successfully!");
        successHeaderText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        successHeaderText.setFill(Color.rgb(75, 0, 130));
        
        Text welcomeText = new Text("Welcome to We Votes, " + userName + "!");
        welcomeText.setFont(Font.font("Arial", FontWeight.MEDIUM, 18));
        welcomeText.setFill(Color.rgb(80, 80, 80));
        
        Text messageText = new Text("Your account has been created successfully.\nYou can now log in to access all features.");
        messageText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        messageText.setFill(Color.rgb(100, 100, 100));
        messageText.setTextAlignment(TextAlignment.CENTER);
        
        // Close button
        Button closeButton = new Button("Continue to Login");
        closeButton.setStyle("-fx-background-color: linear-gradient(to right, #6A5ACD, #4B0082); " +
                          "-fx-text-fill: white; " +
                          "-fx-font-weight: bold; " +
                          "-fx-padding: 12 24; " +
                          "-fx-background-radius: 5;");
        closeButton.setPrefWidth(200);
        
        // Add hover effect
        closeButton.setOnMouseEntered(e -> 
            closeButton.setStyle("-fx-background-color: linear-gradient(to right, #5A4ABD, #3A0071); " +
                              "-fx-text-fill: white; " +
                              "-fx-font-weight: bold; " +
                              "-fx-padding: 12 24; " +
                              "-fx-background-radius: 5;")
        );
        
        closeButton.setOnMouseExited(e -> 
            closeButton.setStyle("-fx-background-color: linear-gradient(to right, #6A5ACD, #4B0082); " +
                              "-fx-text-fill: white; " +
                              "-fx-font-weight: bold; " +
                              "-fx-padding: 12 24; " +
                              "-fx-background-radius: 5;")
        );
        
        closeButton.setOnAction(e -> {
            successStage.close();
            // Open login page
            try {
                Login loginApp = new Login();
                loginApp.start(new Stage());
                
                // Get the stage from any control in the SignUP scene
                Stage currentStage = (Stage) fullNameTextField.getScene().getWindow();
                currentStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Add all elements to the content pane
        content.getChildren().addAll(checkPane, successHeaderText, welcomeText, messageText, closeButton);
        
        // Create and set the scene
        Scene scene = new Scene(content);
        scene.setFill(Color.TRANSPARENT);
        successStage.setScene(scene);
        
        // Size the stage
        successStage.setWidth(450);
        successStage.setHeight(450);
        
        // Center on parent
        successStage.centerOnScreen();
        
        // Show the stage
        successStage.showAndWait();
    }
    
    private void showErrorPopup(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.getStyleClass().add("modern-dialog");
        
        // Add a custom style class
        Scene scene = dialogPane.getScene();
        scene.getStylesheets().add(getClass().getResource("/resources/application.css").toExternalForm());
        
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
    
    private void clearFields() {
        fullNameTextField.clear();
        genderCombo.getSelectionModel().clearSelection();
        phoneTextField.clear();
        emailTextField.clear();
        passwordField.clear();
        validationLabel.setText("");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}