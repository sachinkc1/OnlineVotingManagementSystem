package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

public class SignUP extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("We Votes SignUP");

        // Open in mid-size and disable minimize button
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);

        // Left Section
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

        // Right Section
        GridPane rightSection = new GridPane();
        rightSection.setAlignment(Pos.CENTER);
        rightSection.setHgap(10);
        rightSection.setVgap(20);
        rightSection.setPadding(new Insets(25, 25, 25, 25));
        rightSection.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        rightSection.setPrefWidth(600);
        rightSection.setPrefHeight(800);

        Text sceneTitle = new Text("Admin Login");
        sceneTitle.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        sceneTitle.setFill(Color.PURPLE);
        rightSection.add(sceneTitle, 0, 0, 2, 1);

        // Full Name Field
        Label fullNameLabel = new Label("Full Name:");
        fullNameLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        rightSection.add(fullNameLabel, 0, 1);

        TextField fullNameTextField = new TextField();
        fullNameTextField.setPromptText("Full Name");
        fullNameTextField.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        fullNameTextField.setPrefHeight(40);
        rightSection.add(fullNameTextField, 1, 1);

        // Gender Field
        Label genderLabel = new Label("Sex:");
        genderLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        rightSection.add(genderLabel, 0, 2);

        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female");
        genderCombo.setPromptText("Select Gender");
        genderCombo.setMaxWidth(300);
        genderCombo.setStyle("-fx-font-size: 18px;");
        rightSection.add(genderCombo, 1, 2);

        // Phone Number Field
        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        rightSection.add(phoneLabel, 0, 3);

        TextField phoneTextField = new TextField();
        phoneTextField.setPromptText("Phone Number");
        phoneTextField.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        phoneTextField.setPrefHeight(40);
        rightSection.add(phoneTextField, 1, 3);

        // Email Field
        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        rightSection.add(emailLabel, 0, 4);

        TextField emailTextField = new TextField();
        emailTextField.setPromptText("Email");
        emailTextField.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        emailTextField.setPrefHeight(40);
        rightSection.add(emailTextField, 1, 4);

        // Username Field
        Label userName = new Label("Password:");
        userName.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        rightSection.add(userName, 0, 5);

        TextField userTextField = new TextField();
        userTextField.setPromptText("Password");
        userTextField.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        userTextField.setPrefHeight(40);
        rightSection.add(userTextField, 1, 5);


        // Log In Button
        Button btn = new Button("Log In");
        btn.setStyle("-fx-background-color: #4B0082; -fx-text-fill: white;");
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setPrefHeight(40);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(btn);
        rightSection.add(hbBtn, 0, 7, 2, 1);

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

        Scene scene = new Scene(root, 1200, 800); // Set the scene size to be mid-sized
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
