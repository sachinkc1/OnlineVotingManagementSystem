package application;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
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
import services.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
* inheritance
*/
public class Login extends Application {
  
	/**
	 * encapsulation
	 */
	private TextField emailField;
    private PasswordField passwordField;
    private Label errorLabel;
    private DatabaseService dbService;

    /** 
     * polymorphism
     */
    public Login() {
        this.dbService = new DatabaseService();
    }

    @Override
    public void start(Stage primaryStage) {        
        primaryStage.setTitle("Login");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        
        // Main container with modern styling
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");
        
        // Create card-like container for login
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
        
        // Left Section - Decorative/Branding section
        VBox leftSection = createLeftSection();
        
        // Right Section - Login Form
        VBox rightSection = createRightFormSection(primaryStage);
        
        // Add left and right sections to the content layout
        contentLayout.getChildren().addAll(leftSection, rightSection);
        
        // Add all elements to the card container
        cardContainer.getChildren().addAll(cardBg, contentLayout);
        
        // Center the card in the root pane
        root.setCenter(cardContainer);
        
        
        Scene scene = new Scene(root, 1200, 800);
        // Load CSS - adjusted for your file structure
        scene.getStylesheets().add(getClass().getResource("/resources/application.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Ensure the database tables exist
        try {
            dbService.setupDatabase();
        } catch (SQLException e) {
            showErrorAlert("Database Setup Error", "Failed to set up database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
    * Creates the left section of the login screen with branding elements
    * 
    * @return VBox containing the left section
    */
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
        Text welcomeTitle = new Text("WELCOME TO");
        welcomeTitle.setFont(Font.font("Arial", FontWeight.LIGHT, 24));
        welcomeTitle.setFill(Color.WHITE);
        Text appName = new Text("WE VOTES");
        appName.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        appName.setFill(Color.WHITE);
        
        // Description or tagline
        Text tagline = new Text("Best voting system");
        tagline.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        tagline.setFill(Color.WHITE);
        tagline.setWrappingWidth(400);
        tagline.setTextAlignment(TextAlignment.CENTER);
        
        // Features list
        VBox featuresBox = new VBox(15);
        featuresBox.setAlignment(Pos.CENTER_LEFT);
        featuresBox.setPadding(new Insets(20, 0, 0, 40));
        String[] features = {
            "Real-time results",
            "Easy to use interface",
            "Accessible from anywhere"
        };
        for (String feature : features) {
            HBox featureRow = new HBox(10);
            featureRow.setAlignment(Pos.CENTER_LEFT);
            Text bulletPoint = new Text("•");
            bulletPoint.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            bulletPoint.setFill(Color.WHITE);
            Text featureText = new Text(feature);
            featureText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            featureText.setFill(Color.WHITE);
            featureRow.getChildren().addAll(bulletPoint, featureText);
            featuresBox.getChildren().add(featureRow);
        }
        leftSection.getChildren().addAll(logoStack, welcomeTitle, appName, tagline, featuresBox);
        return leftSection;
    }

    /**
    * Creates the right section of the login screen with the login form
    * 
    * @param primaryStage The primary stage
    * @return VBox containing the right section
    */
    private VBox createRightFormSection(Stage primaryStage) {
        VBox rightSection = new VBox(25);
        rightSection.setAlignment(Pos.CENTER);
        rightSection.setPrefWidth(500);
        rightSection.setPadding(new Insets(60, 60, 60, 60));
        
        // Login Title
        Text loginTitle = new Text("Login");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        loginTitle.setFill(Color.rgb(74, 20, 140));
        Text loginSubtitle = new Text("Enter your credentials to access your account");
        loginSubtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        loginSubtitle.setFill(Color.rgb(100, 100, 100));
        
        // Error message label
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        errorLabel.setMaxWidth(380);
        errorLabel.setWrapText(true);
        
        // Email field
        VBox emailBox = new VBox(8);
        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        emailLabel.setTextFill(Color.rgb(80, 80, 80));
        emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setPrefHeight(45);
        emailField.setStyle("-fx-background-color: #f5f5f5; " +
        "-fx-border-color: #e0e0e0; " +
        "-fx-border-radius: 5; " +
        "-fx-background-radius: 5; " +
        "-fx-font-size: 14px; " +
        "-fx-padding: 10;");
        emailBox.getChildren().addAll(emailLabel, emailField);
        
        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        passwordLabel.setTextFill(Color.rgb(80, 80, 80));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(45);
        passwordField.setStyle("-fx-background-color: #f5f5f5; " +
        "-fx-border-color: #e0e0e0; " +
        "-fx-border-radius: 5; " +
        "-fx-background-radius: 5; " +
        "-fx-font-size: 14px; " +
        "-fx-padding: 10;");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
                
        // Login button
        Button loginButton = new Button("LOGIN");
        loginButton.setPrefHeight(45);
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #6A5ACD, #4B0082); " +
        "-fx-text-fill: white; " +
        "-fx-background-radius: 5;");
        
        // Add hover effect
        loginButton.setOnMouseEntered(e ->
            loginButton.setStyle("-fx-background-color: linear-gradient(to right, #5A4ABD, #3A0071); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5;")
        );
        loginButton.setOnMouseExited(e ->
            loginButton.setStyle("-fx-background-color: linear-gradient(to right, #6A5ACD, #4B0082); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5;")
        );
        
        // Add login action
        loginButton.setOnAction(e -> handleLogin(primaryStage));
        
        // Don't have an account section
        HBox signupBox = new HBox(5);
        signupBox.setAlignment(Pos.CENTER);
        Text noAccountText = new Text("Don't have an account?");
        noAccountText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        noAccountText.setFill(Color.rgb(100, 100, 100));
        Button signupButton = new Button("Sign Up");
        signupButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B0082; -fx-font-weight: bold; -fx-cursor: hand;");
        signupButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        signupButton.setOnAction(e -> {
            // Open the signup page
            SignUP signupApp = new SignUP();
            try {
                signupApp.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        signupBox.getChildren().addAll(noAccountText, signupButton);
        
        rightSection.getChildren().addAll(
            loginTitle,
            loginSubtitle,
            errorLabel,
            emailBox,
            passwordBox,
            loginButton,
            signupBox
        );
        return rightSection;
    }

    /**
    * Handle user login
    */
    private void handleLogin(Stage primaryStage) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Clear previous error messages
        errorLabel.setText("");
        
        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password");
            return;
        }
        
        try {
            // First, check if this is an admin user
            boolean isAdmin = dbService.authenticateAdmin(email, password);
            if (isAdmin) {
                // Get admin's name for welcome message
                String adminName = dbService.getAdminFullName(email);
                // Show success and redirect to admin dashboard
                showSuccessAlert("Admin Login Successful", "Welcome " + adminName + "! You have successfully logged in as an administrator.");
                // Open the AdminDashboard
                primaryStage.close();
                openAdminDashboard();
                return;
            }
            
            // If not admin, check if it's a regular user
            boolean isUser = dbService.authenticateUser(email, password);
            if (isUser) {
                // Get user's name for welcome message
                String userName = dbService.getUserFullName(email);
                // Show success and redirect to user dashboard
                showSuccessAlert("Login Successful", "Welcome " + userName + "! You have successfully logged in.");
                // Open the UserDashboard with the user's email
                primaryStage.close();
                openUserDashboard(email);
                return;
            }
            
            // If neither admin nor user, authentication failed
            errorLabel.setText("Invalid email or password. Please try again.");
        } catch (SQLException ex) {
            showErrorAlert("Login Error", "Database error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            showErrorAlert("System Error", "An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
    * Opens the AdminDashboard
    */
    private void openAdminDashboard() {
        try {
            AdminDashboard adminDashboard = new AdminDashboard();
            Stage adminStage = new Stage();
            adminDashboard.start(adminStage);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open Admin Dashboard: " + e.getMessage());
        }
    }

    /**
    * Opens the UserDashboard and passes the user's email
    * 
    * @param email The user's email address
    */
    private void openUserDashboard(String email) {
        try {
            UserDashboard userDashboard = new UserDashboard(email);
            Stage userStage = new Stage();
            userDashboard.start(userStage);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open User Dashboard: " + e.getMessage());
        }
    }

    /**
    * Shows a success alert dialog
    * 
    * @param title The dialog title
    * @param message The message to display
    */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
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
        
        alert.showAndWait();
    }

    /**
    * Shows an error alert dialog
    * 
    * @param title The dialog title
    * @param message The error message to display
    */
    private void showErrorAlert(String title, String message) {
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
        
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}