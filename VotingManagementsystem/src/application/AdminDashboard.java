package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import models.User;
import models.Poll;
import services.DatabaseConnection;
import services.UserService;
import services.PollService;
import services.DatabaseUserService;
import services.DatabasePollService;

public class AdminDashboard extends Application {
    private UserService userService;
    private PollService pollService;
    private TableView<User> userTableView;
    private TableView<Poll> pollTableView;
    private TabPane tabPane;
    
    // Color scheme
    private final String PRIMARY_COLOR = "#3498db";
    private final String SECONDARY_COLOR = "#2c3e50";
    private final String ACCENT_COLOR = "#e74c3c";
    private final String BACKGROUND_COLOR = "#ecf0f1";
    private final String CARD_COLOR = "#ffffff";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initializeServices();
        setupUI(primaryStage);
    }

    private void initializeServices() {
        DatabaseConnection dbConnection = new DatabaseConnection();
        userService = new DatabaseUserService(dbConnection);
        pollService = new DatabasePollService(dbConnection);
    }

    private void setupUI(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        root.setTop(createHeader());
        root.setLeft(createSidebar());
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        Label title = new Label("Election Admin Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = createButton("Logout", ACCENT_COLOR);
        
        header.getChildren().addAll(title, spacer, logoutBtn);
        
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(220);
        sidebar.setSpacing(5);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        Label dashboardLabel = new Label("DASHBOARD");
        dashboardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        dashboardLabel.setTextFill(Color.LIGHTGRAY);
        dashboardLabel.setPadding(new Insets(10, 0, 10, 10));
        
        Button usersBtn = createSidebarButton("User Management", true);
        Button pollsBtn = createSidebarButton("Poll Management", false);
        Button reportsBtn = createSidebarButton("Reports", false);
        
        // Event handlers for sidebar buttons
        usersBtn.setOnAction(e -> {
            tabPane.getSelectionModel().select(0); // Select User Management tab
            highlightSidebarButton(usersBtn, pollsBtn, reportsBtn);
        });
        
        pollsBtn.setOnAction(e -> {
            tabPane.getSelectionModel().select(1); // Select Poll Management tab
            highlightSidebarButton(pollsBtn, usersBtn, reportsBtn);
        });
        
        sidebar.getChildren().addAll(
            dashboardLabel,
            usersBtn, 
            pollsBtn, 
            reportsBtn
        );
        
        return sidebar;
    }
    
    private void highlightSidebarButton(Button active, Button... inactive) {
        // Style active button
        active.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5;"
        );
        
        // Style inactive buttons
        for (Button button : inactive) {
            button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 5;"
            );
        }
    }
    
    private Button createSidebarButton(String text, boolean selected) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(40);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(0, 0, 0, 10));
        
        if (selected) {
            button.setStyle(
                "-fx-background-color: " + PRIMARY_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 5;"
            );
        } else {
            button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 5;"
            );
        }
        
        button.setOnMouseEntered(e -> {
            if (!button.getStyle().contains(PRIMARY_COLOR)) {
                button.setStyle(
                    "-fx-background-color: rgba(52, 152, 219, 0.3);" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 5;"
                );
            }
        });
        
        button.setOnMouseExited(e -> {
            if (!button.getStyle().contains(PRIMARY_COLOR)) {
                button.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 5;"
                );
            }
        });
        
        return button;
    }
    
    private TabPane createMainContent() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("floating");
        tabPane.setStyle("-fx-background-color: transparent;");
        
        // User Management Tab
        Tab userTab = new Tab("User Management");
        userTab.setContent(createUserManagementPane());
        
        // Poll Management Tab
        Tab pollTab = new Tab("Poll Management");
        pollTab.setContent(createPollManagementPane());
        
        tabPane.getTabs().addAll(userTab, pollTab);
        
        // Add listener to update sidebar button highlights when tab changes
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            // This would access the sidebar buttons and update their styles
            // For now, we'll handle this from the sidebar button click events
        });
        
        return tabPane;
    }
    
    private VBox createUserManagementPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(25));
        pane.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Header section
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("User Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.valueOf(SECONDARY_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        TextField searchField = createTextField("Search users...");
        Button searchBtn = createButton("Search", PRIMARY_COLOR);
        
        header.getChildren().addAll(title, spacer, searchField, searchBtn);
        header.setSpacing(10);

        // Table with card styling
        VBox tableCard = createCard();
        
        // Create user table
        userTableView = new TableView<>();
        userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTableView.setPlaceholder(new Label("No users found"));
        setupUserTableColumns();
        
        tableCard.getChildren().add(userTableView);

        // Form card
        VBox formCard = createCard();
        formCard.setSpacing(15);
        
        Label formTitle = new Label("Add/Edit User");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Form fields in grid
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(15, 0, 15, 0));
        
        TextField nameField = createTextField("Full Name");
        TextField emailField = createTextField("Email Address");
        TextField phoneField = createTextField("Phone Number");
        ComboBox<String> genderCombo = new ComboBox<>(FXCollections.observableArrayList("Male", "Female", "Other"));
        genderCombo.setPromptText("Select Gender");
        styleComboBox(genderCombo);
        
        // Add form fields to grid
        form.addRow(0, createFormLabel("Name:"), nameField);
        form.addRow(1, createFormLabel("Email:"), emailField);
        form.addRow(2, createFormLabel("Phone:"), phoneField);
        form.addRow(3, createFormLabel("Gender:"), genderCombo);
        
        // Make fields expand to fill available space
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHgrow(phoneField, Priority.ALWAYS);
        GridPane.setHgrow(genderCombo, Priority.ALWAYS);
        
        // Action buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button clearBtn = createButton("Clear", "#95a5a6");
        Button addBtn = createButton("Add User", PRIMARY_COLOR);
        Button updateBtn = createButton("Update", "#f39c12");
        Button deleteBtn = createButton("Delete", ACCENT_COLOR);
        
        buttons.getChildren().addAll(clearBtn, deleteBtn, updateBtn, addBtn);
        
        // Event Handlers
        addBtn.setOnAction(e -> addUser(nameField, emailField, phoneField, genderCombo));
        updateBtn.setOnAction(e -> updateUser(nameField, emailField, phoneField, genderCombo));
        deleteBtn.setOnAction(e -> deleteUser());
        clearBtn.setOnAction(e -> {
            nameField.clear();
            emailField.clear();
            phoneField.clear();
            genderCombo.getSelectionModel().clearSelection();
        });
        
        // User selection handling
        userTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getFullName());
                emailField.setText(newSelection.getEmail());
                phoneField.setText(newSelection.getPhone());
                genderCombo.setValue(newSelection.getGender());
            }
        });

        formCard.getChildren().addAll(formTitle, form, buttons);

        // Add all components to main pane
        pane.getChildren().addAll(header, tableCard, formCard);
        loadUserData();
        
        return pane;
    }
    
    private VBox createCard() {
        VBox card = new VBox();
        card.setStyle(
            "-fx-background-color: " + CARD_COLOR + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 15px;"
        );
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.1));
        card.setEffect(dropShadow);
        
        return card;
    }
    
    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        return label;
    }
    
    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setPrefHeight(35);
        textField.setStyle(
            "-fx-background-radius: 4px;" +
            "-fx-border-radius: 4px;" +
            "-fx-border-color: #dcdde1;" +
            "-fx-border-width: 1px;" +
            "-fx-padding: 5px 10px;"
        );
        return textField;
    }
    
    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setPrefHeight(35);
        comboBox.setStyle(
            "-fx-background-radius: 4px;" +
            "-fx-border-radius: 4px;" +
            "-fx-border-color: #dcdde1;" +
            "-fx-border-width: 1px;" +
            "-fx-padding: 5px 10px;"
        );
    }
    
    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefHeight(35);
        button.setPadding(new Insets(0, 15, 0, 15));
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4px;"
        );
        
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: derive(" + color + ", -10%);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;"
            )
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;"
            )
        );
        
        return button;
    }

    private void setupUserTableColumns() {
        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        
        TableColumn<User, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        
        TableColumn<User, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
        
        userTableView.getColumns().addAll(nameCol, emailCol, phoneCol, genderCol);
    }

    private void addUser(TextField name, TextField email, TextField phone, ComboBox<String> gender) {
        User user = new User(0, name.getText(), email.getText(), phone.getText(), gender.getValue());
        userService.addUser(user);
        loadUserData();
        
        // Show success notification
        showNotification("User added successfully");
    }

    private void updateUser(TextField name, TextField email, TextField phone, ComboBox<String> gender) {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setFullName(name.getText());
            selected.setEmail(email.getText());
            selected.setPhone(phone.getText());
            selected.setGender(gender.getValue());
            userService.updateUser(selected);
            loadUserData();
            
            // Show success notification
            showNotification("User updated successfully");
        } else {
            showNotification("Please select a user to update", true);
        }
    }

    private void deleteUser() {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Create confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete User");
            alert.setContentText("Are you sure you want to delete " + selected.getFullName() + "?");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                userService.deleteUser(selected.getId());
                loadUserData();
                showNotification("User deleted successfully");
            }
        } else {
            showNotification("Please select a user to delete", true);
        }
    }

    private void loadUserData() {
        userTableView.setItems(userService.getAllUsers());
    }

    private VBox createPollManagementPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(25));
        pane.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Header section
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Poll Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.valueOf(SECONDARY_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button newPollBtn = createButton("Create New Poll", PRIMARY_COLOR);
        newPollBtn.setOnAction(e -> {
            // Auto-scroll to the form section
            // In a real app, you might want to use a ScrollPane and scroll to the form
        });
        
        header.getChildren().addAll(title, spacer, newPollBtn);

        // Stats cards
        HBox statsBox = new HBox(15);
        statsBox.setPrefHeight(100);
        
        VBox totalPollsCard = createStatsCard("Total Polls", "12", "#3498db");
        VBox activePollsCard = createStatsCard("Active Polls", "8", "#2ecc71");
        VBox completedPollsCard = createStatsCard("Completed", "4", "#e74c3c");
        VBox totalVotesCard = createStatsCard("Total Votes", "2,548", "#f39c12");
        
        statsBox.getChildren().addAll(totalPollsCard, activePollsCard, completedPollsCard, totalVotesCard);
        HBox.setHgrow(totalPollsCard, Priority.ALWAYS);
        HBox.setHgrow(activePollsCard, Priority.ALWAYS);
        HBox.setHgrow(completedPollsCard, Priority.ALWAYS);
        HBox.setHgrow(totalVotesCard, Priority.ALWAYS);

        // Poll table card
        VBox tableCard = createCard();
        
        // Create poll table
        pollTableView = new TableView<>();
        pollTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pollTableView.setPlaceholder(new Label("No polls found"));
        setupPollTableColumns();
        
        tableCard.getChildren().add(pollTableView);

        // Form card
        VBox formCard = createCard();
        formCard.setSpacing(15);
        
        Label formTitle = new Label("Create/Edit Poll");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Form fields in grid
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(15, 0, 15, 0));
        
        TextField nameField = createTextField("Poll Name");
        TextField candidate1Field = createTextField("Candidate 1 Name");
        TextField candidate2Field = createTextField("Candidate 2 Name");
        
        // Status combobox
        ComboBox<String> statusCombo = new ComboBox<>(
            FXCollections.observableArrayList("Draft", "Active", "Completed")
        );
        statusCombo.setPromptText("Select Status");
        styleComboBox(statusCombo);
        
        // Date selector
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("End Date");
        datePicker.setPrefHeight(35);
        datePicker.setStyle(
            "-fx-background-radius: 4px;" +
            "-fx-border-radius: 4px;" +
            "-fx-border-color: #dcdde1;" +
            "-fx-border-width: 1px;" +
            "-fx-padding: 5px 10px;"
        );
        
        // Add form fields to grid
        form.addRow(0, createFormLabel("Poll Name:"), nameField);
        form.addRow(1, createFormLabel("Candidate 1:"), candidate1Field);
        form.addRow(2, createFormLabel("Candidate 2:"), candidate2Field);
        form.addRow(3, createFormLabel("Status:"), statusCombo);
        form.addRow(4, createFormLabel("End Date:"), datePicker);
        
        // Make fields expand to fill available space
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(candidate1Field, Priority.ALWAYS);
        GridPane.setHgrow(candidate2Field, Priority.ALWAYS);
        GridPane.setHgrow(statusCombo, Priority.ALWAYS);
        GridPane.setHgrow(datePicker, Priority.ALWAYS);
        
        // Action buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button clearBtn = createButton("Clear", "#95a5a6");
        Button createBtn = createButton("Create Poll", PRIMARY_COLOR);
        Button updateBtn = createButton("Update", "#f39c12");
        
        buttons.getChildren().addAll(clearBtn, updateBtn, createBtn);
        
        // Event Handlers
        createBtn.setOnAction(e -> createPoll(nameField, candidate1Field, candidate2Field));
        updateBtn.setOnAction(e -> updatePoll(nameField, candidate1Field, candidate2Field));
        clearBtn.setOnAction(e -> {
            nameField.clear();
            candidate1Field.clear();
            candidate2Field.clear();
            statusCombo.getSelectionModel().clearSelection();
            datePicker.setValue(null);
        });
        
        // Poll selection handling
        pollTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                candidate1Field.setText(newSelection.getCandidate1());
                candidate2Field.setText(newSelection.getCandidate2());
            }
        });

        formCard.getChildren().addAll(formTitle, form, buttons);

        // Add all components to main pane
        pane.getChildren().addAll(header, statsBox, tableCard, formCard);
        loadPollData();
        
        return pane;
    }
    
    private VBox createStatsCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 0 0 0 5px;"
        );
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.1));
        card.setEffect(dropShadow);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.valueOf(color));
        
        Label titleLabel = new Label(label);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(Color.GRAY);
        
        card.getChildren().addAll(valueLabel, titleLabel);
        
        return card;
    }

    private void setupPollTableColumns() {
        TableColumn<Poll, String> nameCol = new TableColumn<>("Poll Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(150);
        
        TableColumn<Poll, String> cand1Col = new TableColumn<>("Candidate 1");
        cand1Col.setCellValueFactory(cellData -> cellData.getValue().candidate1Property());
        cand1Col.setPrefWidth(120);
        
        TableColumn<Poll, String> cand2Col = new TableColumn<>("Candidate 2");
        cand2Col.setCellValueFactory(cellData -> cellData.getValue().candidate2Property());
        cand2Col.setPrefWidth(120);
        
        TableColumn<Poll, Number> votesCol = new TableColumn<>("Total Votes");
        votesCol.setCellValueFactory(cellData -> cellData.getValue().totalVotesProperty());
        votesCol.setPrefWidth(100);
        
        // Add status column
        TableColumn<Poll, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            // This would be a property in the Poll class
            // Here we're simulating it for demonstration
            int votes = cellData.getValue().getTotalVotes();
            if (votes > 0) {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "Active");
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "Draft");
            }
        });
        statusCol.setPrefWidth(100);
        
        // Add actions column
        TableColumn<Poll, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(120);
        
        pollTableView.getColumns().addAll(nameCol, cand1Col, cand2Col, votesCol, statusCol, actionCol);
    }

    private void createPoll(TextField name, TextField cand1, TextField cand2) {
        if (name.getText().isEmpty() || cand1.getText().isEmpty() || cand2.getText().isEmpty()) {
            showNotification("Please fill in all fields", true);
            return;
        }
        
        Poll poll = new Poll(0, name.getText(), cand1.getText(), cand2.getText(), 0);
        pollService.createPoll(poll);
        loadPollData();
        
        // Show success notification
        showNotification("Poll created successfully");
        
        // Clear form fields
        name.clear();
        cand1.clear();
        cand2.clear();
    }

    private void updatePoll(TextField name, TextField cand1, TextField cand2) {
        Poll selected = pollTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (name.getText().isEmpty() || cand1.getText().isEmpty() || cand2.getText().isEmpty()) {
                showNotification("Please fill in all fields", true);
                return;
            }
            
            selected.setName(name.getText());
            selected.setCandidate1(cand1.getText());
            selected.setCandidate2(cand2.getText());
            pollService.updatePoll(selected);
            loadPollData();
            
            // Show success notification
            showNotification("Poll updated successfully");
        } else {
            showNotification("Please select a poll to update", true);
        }
    }

    private void loadPollData() {
        pollTableView.setItems(pollService.getAllPolls());
    }
    
    private void showNotification(String message) {
        showNotification(message, false);
    }
    
    private void showNotification(String message, boolean isError) {
        // This would be implemented with a toast notification
        // For now, we'll use an alert
        Alert alert = new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(isError ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}