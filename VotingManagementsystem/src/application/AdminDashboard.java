package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.User;
import models.Poll;
import services.UserService;
import services.PollService;
import services.DatabaseUserService;
import services.DatabasePollService;

/**
 * Inheritance:public class adminDashboard extends Application {
 */
public class AdminDashboard extends Application {
  /**
   * encapsulation
   * polymorphism
   */
	
	
	private UserService userService;
    private PollService pollService;
    
    private TableView<User> userTableView;
    private TableView<Poll> pollTableView;
    private StackPane contentArea;
    private VBox userManagementView;
    private VBox pollManagementView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initializeServices();
        setupUI(primaryStage);
    }

    /**
     * Initialize database connections and service classes
     * encapsulation
     */
    private void initializeServices() {
        userService = new DatabaseUserService();
        pollService = new DatabasePollService();
    }

    /**
     * Set up the main UI components
     */
    private void setupUI(Stage stage) {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setLeft(createSidebar());

        // Create content area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        root.setCenter(contentArea);

        // Create the views
        userManagementView = createUserManagementPane();
        pollManagementView = createPollManagementPane();

        // Show poll management view by default
        showPollManagementView();

        Scene scene = new Scene(root, 1200, 800);
        // Load CSS
        scene.getStylesheets().add(getClass().getResource("/resources/style1.css").toExternalForm());

        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    /**
     * Create the header bar with title and logout button
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Admin Dashboard");
        title.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createButton("Logout", "button-accent");
        logoutBtn.setOnAction(e -> handleLogout());

        header.getChildren().addAll(title, spacer, logoutBtn);
        return header;
    }

    /**
     * Handle user logout
     */
    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
                "Are you sure you want to log out?", 
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Logout");
        confirmation.setHeaderText("Logout");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Open login screen
                    Login login = new Login();
                    login.start(new Stage());
                    
                    // Close this window
                    ((Stage) contentArea.getScene().getWindow()).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the sidebar with navigation buttons
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setSpacing(5);
        sidebar.setPrefWidth(220);

        Label dashboardLabel = new Label("DASHBOARD");
        dashboardLabel.getStyleClass().add("sidebar-label");

        Button usersBtn = createSidebarButton("User Management", false);
        Button pollsBtn = createSidebarButton("Poll Management", true);

        // Event handlers for sidebar buttons
        usersBtn.setOnAction(e -> {
            showUserManagementView();
            highlightSidebarButton(usersBtn, pollsBtn);
        });

        pollsBtn.setOnAction(e -> {
            showPollManagementView();
            highlightSidebarButton(pollsBtn, usersBtn);
        });

        sidebar.getChildren().addAll(
                dashboardLabel,
                usersBtn,
                pollsBtn 
        );

        return sidebar;
    }

    /**
     * Show the user management view
     */
    private void showUserManagementView() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(userManagementView);
        loadUserData();
    }

    /**
     * Show the poll management view
     */
    private void showPollManagementView() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pollManagementView);
        loadPollData();
    }

    /**
     * Highlight the active sidebar button and reset others
     */
    private void highlightSidebarButton(Button active, Button... inactive) {
        active.getStyleClass().add("sidebar-button-active");
        for (Button button : inactive) {
            button.getStyleClass().remove("sidebar-button-active");
        }
    }

    /**
     * abstraction Create a styled sidebar button
     */
    private Button createSidebarButton(String text, boolean selected) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-button");
        if (selected) {
            button.getStyleClass().add("sidebar-button-active");
        }
        return button;
    }

    /**
     * Create the user management panel
     */
    private VBox createUserManagementPane() {
        VBox pane = new VBox(20);
        pane.getStyleClass().add("content-area");

        // Header section
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        Label title = new Label("User Management");
        title.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        TextField searchField = createTextField("Search users...");
        Button searchBtn = createButton("Search", "button-primary");
        header.getChildren().addAll(title, spacer, searchField, searchBtn);

        // User count card
        VBox userCountCard = createStatsCard("Total Users", String.valueOf(userService.getUserCount()), "stats-primary");
        userCountCard.setPrefWidth(200);

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
        Label formTitle = new Label("Edit User");
        formTitle.getStyleClass().add("form-title");

        // Form fields in grid
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(15, 0, 15, 0));

        TextField nameField = createTextField("Full Name");
        TextField emailField = createTextField("Email Address");
        TextField phoneField = createTextField("Phone Number");
        ComboBox<String> genderCombo = new ComboBox<>(
                FXCollections.observableArrayList("Male", "Female")
        );
        genderCombo.setPromptText("Select Gender");
        genderCombo.getStyleClass().add("combo-box");

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
        Button clearBtn = createButton("Clear", "button-muted");
        Button updateBtn = createButton("Update", "button-warning");
        Button deleteBtn = createButton("Delete", "button-accent");
        buttons.getChildren().addAll(clearBtn, deleteBtn, updateBtn);

        // Event Handlers
        updateBtn.setOnAction(e -> updateUser(nameField, emailField, phoneField, genderCombo));
        deleteBtn.setOnAction(e -> deleteUser());
        clearBtn.setOnAction(e -> {
            nameField.clear();
            emailField.clear();
            phoneField.clear();
            genderCombo.getSelectionModel().clearSelection();
            userTableView.getSelectionModel().clearSelection();
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
        pane.getChildren().addAll(header, userCountCard, tableCard, formCard);
        return pane;
    }

    /**
     * Create a styled card container
     */
    private VBox createCard() {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        return card;
    }

    /**
     * Create a form field label
     */
    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    /**
     * Create a styled text field
     */
    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-field");
        return textField;
    }

    /**
     * Create a styled button
     */
    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    /**
     * Set up columns for the user table
     */
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

    /**
     * Update an existing user
     */
    private void updateUser(TextField name, TextField email, TextField phone, ComboBox<String> gender) {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Validate inputs
            if (name.getText().isEmpty() || email.getText().isEmpty() ||
                    phone.getText().isEmpty() || gender.getValue() == null) {
                showNotification("Please fill in all fields", true);
                return;
            }

            selected.setFullName(name.getText());
            selected.setEmail(email.getText());
            selected.setPhone(phone.getText());
            selected.setGender(gender.getValue());

            if (userService.updateUser(selected)) {
                loadUserData();
                showNotification("User updated successfully");
            } else {
                showNotification("Failed to update user", true);
            }
        } else {
            showNotification("Please select a user to update", true);
        }
    }

    /**
     * Delete a user
     */
    private void deleteUser() {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Create confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete User");
            alert.setContentText("Are you sure you want to delete " + selected.getFullName() + "?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (userService.deleteUser(selected.getId())) {
                    loadUserData();
                    showNotification("User deleted successfully");
                } else {
                    showNotification("Failed to delete user. The user may have active polls.", true);
                }
            }
        } else {
            showNotification("Please select a user to delete", true);
        }
    }

    /**
     * Load user data into the table
     */
    private void loadUserData() {
        userTableView.setItems(userService.getAllUsers());
    }

    /**
     * Create the poll management panel
     * composition
     */
    private VBox createPollManagementPane() {
        VBox pane = new VBox(20);
        pane.getStyleClass().add("content-area");

        // Header section
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        Label title = new Label("Poll Management");
        title.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button newPollBtn = createButton("Create New Poll", "button-primary");
        header.getChildren().addAll(title, spacer, newPollBtn);

        // Stats cards
        HBox statsBox = new HBox(15);
        statsBox.setPrefHeight(100);
        VBox totalPollsCard = createStatsCard("Total Polls", String.valueOf(pollService.getPollCount()), "stats-primary");
        VBox activePollsCard = createStatsCard("Active Polls", String.valueOf(pollService.getActivePollCount()), "stats-success");
        VBox completedPollsCard = createStatsCard("Completed", String.valueOf(pollService.getCompletedPollCount()), "stats-accent");
        VBox totalVotesCard = createStatsCard("Total Votes", String.valueOf(pollService.getTotalVoteCount()), "stats-warning");
        
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
        formTitle.getStyleClass().add("form-title");

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
        statusCombo.getStyleClass().add("combo-box");

        // Add form fields to grid
        form.addRow(0, createFormLabel("Poll Name:"), nameField);
        form.addRow(1, createFormLabel("Candidate 1:"), candidate1Field);
        form.addRow(2, createFormLabel("Candidate 2:"), candidate2Field);
        form.addRow(3, createFormLabel("Status:"), statusCombo);

        // Make fields expand to fill available space
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(candidate1Field, Priority.ALWAYS);
        GridPane.setHgrow(candidate2Field, Priority.ALWAYS);
        GridPane.setHgrow(statusCombo, Priority.ALWAYS);

        // Status change buttons
        HBox statusButtons = new HBox(10);
        statusButtons.setAlignment(Pos.CENTER_LEFT);
        statusButtons.setPadding(new Insets(10, 0, 0, 0));
        Button activateBtn = createButton("Activate Poll", "button-success");
        Button completeBtn = createButton("Complete Poll", "button-warning");
        statusButtons.getChildren().addAll(activateBtn, completeBtn);

        // Action buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button clearBtn = createButton("Clear", "button-muted");
        Button createBtn = createButton("Create Poll", "button-primary");
        Button updateBtn = createButton("Update", "button-warning");
        Button deleteBtn = createButton("Delete", "button-accent");
        buttons.getChildren().addAll(clearBtn, deleteBtn, updateBtn, createBtn);

        // Event Handlers
        createBtn.setOnAction(e -> createPoll(nameField, candidate1Field, candidate2Field, statusCombo));
        updateBtn.setOnAction(e -> updatePoll(nameField, candidate1Field, candidate2Field, statusCombo));
        deleteBtn.setOnAction(e -> deletePoll());
        clearBtn.setOnAction(e -> {
            nameField.clear();
            candidate1Field.clear();
            candidate2Field.clear();
            statusCombo.getSelectionModel().clearSelection();
            pollTableView.getSelectionModel().clearSelection();
        });
        activateBtn.setOnAction(e -> activatePoll());
        completeBtn.setOnAction(e -> completePoll());

        // Poll selection handling
        pollTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                candidate1Field.setText(newSelection.getCandidate1());
                candidate2Field.setText(newSelection.getCandidate2());
                statusCombo.setValue(newSelection.getStatus());
            }
        });

        Separator separator = new Separator();
        separator.getStyleClass().add("separator");
        formCard.getChildren().addAll(formTitle, form, statusButtons, separator, buttons);

        // Add all components to main pane
        pane.getChildren().addAll(header, statsBox, tableCard, formCard);
        return pane;
    }

    /**
     * Shows a statistics popup with voting percentages and a results button
     * 
     * @param poll The poll to show statistics for
     */
    private void showPollStatisticsPopup(Poll poll) {
        // Create a new stage for the statistics popup
        Stage statsStage = new Stage();
        statsStage.initModality(Modality.APPLICATION_MODAL);
        statsStage.initStyle(StageStyle.DECORATED);
        statsStage.setTitle("Poll Statistics: " + poll.getName());
        statsStage.setMinWidth(550);
        statsStage.setMinHeight(400);
        
        // Create the main container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: white;");
        
        // Header
        Label titleLabel = new Label("Poll Statistics");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        
        Label pollNameLabel = new Label(poll.getName());
        pollNameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");
        
        // Status indicator
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label statusTextLabel = new Label("Status:");
        statusTextLabel.setStyle("-fx-font-weight: bold;");
        
        Label statusValueLabel = new Label(poll.getStatus());
        if ("Active".equals(poll.getStatus())) {
            statusValueLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else if ("Completed".equals(poll.getStatus())) {
            statusValueLabel.setStyle("-fx-text-fill: #FF5252; -fx-font-weight: bold;");
        } else {
            statusValueLabel.setStyle("-fx-text-fill: #78909C; -fx-font-weight: bold;");
        }
        
        statusBox.getChildren().addAll(statusTextLabel, statusValueLabel);
        
        // Separator
        Separator separator = new Separator();
        separator.setStyle("-fx-opacity: 0.3;");
        
        // Vote distribution with percentages
        Label distributionLabel = new Label("Vote Distribution");
        distributionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        
        // Calculate percentages (for a real app, you would get actual votes)
        int totalVotes = poll.getTotalVotes();
        int candidate1Votes = totalVotes / 2 + (totalVotes % 2); // Just split votes for demonstration
        int candidate2Votes = totalVotes / 2;
        double candidate1Percentage = totalVotes > 0 ? (double)candidate1Votes / totalVotes * 100 : 0;
        double candidate2Percentage = totalVotes > 0 ? (double)candidate2Votes / totalVotes * 100 : 0;
        
        // Chart container
        VBox chartContainer = new VBox(20);
        chartContainer.setPadding(new Insets(15));
        chartContainer.setStyle(
                "-fx-background-color: #F5F7FA;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: #E0E0E0;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 8px;"
        );
        
        // Candidate 1 bar
        Label cand1NameLabel = new Label(poll.getCandidate1());
        cand1NameLabel.setStyle("-fx-font-weight: bold;");
        
        HBox cand1BarContainer = new HBox(10);
        cand1BarContainer.setAlignment(Pos.CENTER_LEFT);
        
        HBox cand1Bar = new HBox();
        cand1Bar.setPrefHeight(30);
        cand1Bar.setPrefWidth(Math.max(10, (candidate1Percentage / 100) * 350));
        cand1Bar.setStyle(
                "-fx-background-color: #1976D2;" +
                "-fx-background-radius: 5px;"
        );
        
        Label cand1PercentLabel = new Label(String.format("%.1f%%", candidate1Percentage));
        cand1PercentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976D2;");
        
        cand1BarContainer.getChildren().addAll(cand1Bar, cand1PercentLabel);
        
        Label cand1VotesLabel = new Label(candidate1Votes + " votes");
        cand1VotesLabel.setStyle("-fx-text-fill: #78909C;");
        
        VBox cand1Box = new VBox(5);
        cand1Box.getChildren().addAll(cand1NameLabel, cand1BarContainer, cand1VotesLabel);
        
        // Candidate 2 bar
        Label cand2NameLabel = new Label(poll.getCandidate2());
        cand2NameLabel.setStyle("-fx-font-weight: bold;");
        
        HBox cand2BarContainer = new HBox(10);
        cand2BarContainer.setAlignment(Pos.CENTER_LEFT);
        
        HBox cand2Bar = new HBox();
        cand2Bar.setPrefHeight(30);
        cand2Bar.setPrefWidth(Math.max(10, (candidate2Percentage / 100) * 350));
        cand2Bar.setStyle(
                "-fx-background-color: #FF5252;" +
                "-fx-background-radius: 5px;"
        );
        
        Label cand2PercentLabel = new Label(String.format("%.1f%%", candidate2Percentage));
        cand2PercentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF5252;");
        
        cand2BarContainer.getChildren().addAll(cand2Bar, cand2PercentLabel);
        
        Label cand2VotesLabel = new Label(candidate2Votes + " votes");
        cand2VotesLabel.setStyle("-fx-text-fill: #78909C;");
        
        VBox cand2Box = new VBox(5);
        cand2Box.getChildren().addAll(cand2NameLabel, cand2BarContainer, cand2VotesLabel);
        
        // Total votes
        Label totalVotesLabel = new Label("Total Votes: " + totalVotes);
        totalVotesLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10px 0 0 0;");
        
        chartContainer.getChildren().addAll(cand1Box, cand2Box, totalVotesLabel);
        
        // Bottom buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Results button (shows who won in a popup)
        Button resultsBtn = createButton("Show Results", "button-success");
        resultsBtn.setPrefWidth(150);
        resultsBtn.setOnAction(e -> showResultsPopup(poll, candidate1Votes, candidate2Votes));
        
        // Close button
        Button closeBtn = createButton("Close", "button-muted");
        closeBtn.setPrefWidth(120);
        closeBtn.setOnAction(e -> statsStage.close());
        
        buttonBox.getChildren().addAll(resultsBtn, closeBtn);
        
        // Add all components to the main container
        mainContainer.getChildren().addAll(
                titleLabel,
                pollNameLabel,
                statusBox,
                separator,
                distributionLabel,
                chartContainer,
                buttonBox
        );
        
        // Create and show the scene
        Scene scene = new Scene(mainContainer);
        statsStage.setScene(scene);
        statsStage.show();
    }
    
    /**
     * Shows a results popup indicating who won the voting
     * 
     * @param poll The poll to show results for
     * @param candidate1Votes Votes for candidate 1
     * @param candidate2Votes Votes for candidate 2
     */
    private void showResultsPopup(Poll poll, int candidate1Votes, int candidate2Votes) {
        // Create stage
        Stage resultsStage = new Stage();
        resultsStage.initModality(Modality.APPLICATION_MODAL);
        resultsStage.initStyle(StageStyle.DECORATED);
        resultsStage.setTitle("Poll Results");
        resultsStage.setWidth(400);
        resultsStage.setHeight(300);
        
        // Create container
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: white;");
        
        // Results header
        Label titleLabel = new Label("Poll Results");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        
        Label pollNameLabel = new Label(poll.getName());
        pollNameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
        
        // Determine winner
        String winnerName;
        String winnerMessage;
        
        if (candidate1Votes > candidate2Votes) {
            winnerName = poll.getCandidate1();
            winnerMessage = "is the winner!";
        } else if (candidate2Votes > candidate1Votes) {
            winnerName = poll.getCandidate2();
            winnerMessage = "is the winner!";
        } else {
            winnerName = "No candidate";
            winnerMessage = "It's a tie!";
        }
        
        // Create winner box
        VBox winnerBox = new VBox(10);
        winnerBox.setAlignment(Pos.CENTER);
        winnerBox.setPadding(new Insets(20));
        winnerBox.setStyle(
                "-fx-background-color: #F8F9FA;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-color: #EAEAEA;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 10px;"
        );
        
        // Winner emoji/icon
        Label trophyLabel = new Label("🏆");
        trophyLabel.setStyle("-fx-font-size: 40px;");
        
        // Winner name
        Label winnerNameLabel = new Label(winnerName);
        winnerNameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        // Winner message
        Label winnerMessageLabel = new Label(winnerMessage);
        winnerMessageLabel.setStyle("-fx-font-size: 16px;");
        
        // Vote counts
        Label voteCountsLabel = new Label(String.format(
                "Final vote count: %d to %d", 
                Math.max(candidate1Votes, candidate2Votes), 
                Math.min(candidate1Votes, candidate2Votes)
        ));
        voteCountsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #78909C;");
        
        winnerBox.getChildren().addAll(trophyLabel, winnerNameLabel, winnerMessageLabel, voteCountsLabel);
        
        // Close button
        Button closeBtn = createButton("Close", "button-primary");
        closeBtn.setPrefWidth(120);
        closeBtn.setOnAction(e -> resultsStage.close());
        
        // Add components to main container
        container.getChildren().addAll(titleLabel, pollNameLabel, winnerBox, closeBtn);
        
        // Create and show scene
        Scene scene = new Scene(container);
        resultsStage.setScene(scene);
        resultsStage.show();
    }

    /**
     * Create a stats card with a value and label
     */
    private VBox createStatsCard(String label, String value, String styleClass) {
        VBox card = new VBox(5);
        card.getStyleClass().addAll("stats-card", styleClass);
        card.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stats-value");

        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("stats-title");

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    /**
     * Set up columns for the poll table
     */
    private void setupPollTableColumns() {
        TableColumn<Poll, String> nameCol = new TableColumn<>("Poll Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(150);

        TableColumn<Poll, String> cand1Col = new TableColumn<>("Candidate 1");
        cand1Col.setCellValueFactory(cellData -> cellData.getValue().candidate1Property());
        cand1Col.setPrefWidth(100);

        TableColumn<Poll, String> cand2Col = new TableColumn<>("Candidate 2");
        cand2Col.setCellValueFactory(cellData -> cellData.getValue().candidate2Property());
        cand2Col.setPrefWidth(100);

        TableColumn<Poll, Number> votesCol = new TableColumn<>("Total Votes");
        votesCol.setCellValueFactory(cellData -> cellData.getValue().totalVotesProperty());
        votesCol.setPrefWidth(80);

        // Add status column with custom styling
        TableColumn<Poll, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setPrefWidth(80);

        // Set cell factory for status column to add colors
        statusCol.setCellFactory(column -> {
            return new TableCell<Poll, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        getStyleClass().removeAll("status-active", "status-completed", "status-draft");
                    } else {
                        setText(item);
                        getStyleClass().removeAll("status-active", "status-completed", "status-draft");
                        if ("Active".equals(item)) {
                            getStyleClass().add("status-active");
                        } else if ("Completed".equals(item)) {
                            getStyleClass().add("status-completed");
                        } else {
                            getStyleClass().add("status-draft");
                        }
                    }
                }
            };
        });
        
        // Add action column with Results and Stats buttons
        TableColumn<Poll, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(column -> {
            return new TableCell<Poll, Void>() {
                private final HBox container = new HBox(5);
                private final Button statsBtn = new Button("Stats");
                private final Button resultsBtn = new Button("Results");
                
                {
                    // Style the stats button
                    statsBtn.getStyleClass().add("button-info");
                    statsBtn.setPrefHeight(30);
                    statsBtn.setOnAction(e -> {
                        Poll poll = getTableView().getItems().get(getIndex());
                        showPollStatisticsPopup(poll);
                    });
                    
                    // Style the results button
                    resultsBtn.getStyleClass().add("button-success");
                    resultsBtn.setPrefHeight(30);
                    resultsBtn.setOnAction(e -> {
                        Poll poll = getTableView().getItems().get(getIndex());
                        // Calculate votes for demonstration
                        int totalVotes = poll.getTotalVotes();
                        int candidate1Votes = totalVotes / 2 + (totalVotes % 2);
                        int candidate2Votes = totalVotes / 2;
                        showResultsPopup(poll, candidate1Votes, candidate2Votes);
                    });
                    
                    // Set up container
                    container.setAlignment(Pos.CENTER);
                    container.setPadding(new Insets(0, 0, 0, 10));
                    container.getChildren().addAll(statsBtn, resultsBtn);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Poll poll = getTableView().getItems().get(getIndex());
                        // Enable/disable the results button based on status
                        boolean completed = "Completed".equals(poll.getStatus());
                        resultsBtn.setDisable(!completed);
                        
                        // Show both buttons
                        setGraphic(container);
                    }
                }
            };
        });

        pollTableView.getColumns().addAll(nameCol, cand1Col, cand2Col, votesCol, statusCol, actionCol);
    }

    /**
     * Create a new poll
     */
    private void createPoll(TextField name, TextField cand1, TextField cand2, ComboBox<String> status) {
        if (name.getText().isEmpty() || cand1.getText().isEmpty() || cand2.getText().isEmpty()) {
            showNotification("Please fill in all fields", true);
            return;
        }

        // Create a new poll with admin user ID (1) as the creator
        Poll poll = new Poll(
                0,  // ID will be auto-generated
                name.getText(), 
                cand1.getText(), 
                cand2.getText(),
                status.getValue() != null ? status.getValue() : "Draft",
                0,  // Total votes starts at 0
                1   // Admin user ID is 1
        );

        int result = pollService.createPoll(poll);
        if (result > 0) {
            loadPollData();
            showNotification("Poll created successfully");
            // Clear form fields
            name.clear();
            cand1.clear();
            cand2.clear();
            status.getSelectionModel().clearSelection();
        } else {
            showNotification("Failed to create poll", true);
        }
    }

    /**
     * Update an existing poll
     */
    private void updatePoll(TextField name, TextField cand1, TextField cand2, ComboBox<String> status) {
        Poll selected = pollTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (name.getText().isEmpty() || cand1.getText().isEmpty() || cand2.getText().isEmpty()) {
                showNotification("Please fill in all fields", true);
                return;
            }

            selected.setName(name.getText());
            selected.setCandidate1(cand1.getText());
            selected.setCandidate2(cand2.getText());
            if (status.getValue() != null) {
                selected.setStatus(status.getValue());
            }

            if (pollService.updatePoll(selected)) {
                loadPollData();
                showNotification("Poll updated successfully");
            } else {
                showNotification("Failed to update poll", true);
            }
        } else {
            showNotification("Please select a poll to update", true);
        }
    }

    /**
     * Delete a poll
     */
    private void deletePoll() {
        Poll selected = pollTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Create confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Poll");
            alert.setContentText("Are you sure you want to delete poll: " + selected.getName() + "?\nThis will delete all votes for this poll.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (pollService.deletePoll(selected.getId())) {
                    loadPollData();
                    showNotification("Poll deleted successfully");
                } else {
                    showNotification("Failed to delete poll", true);
                }
            }
        } else {
            showNotification("Please select a poll to delete", true);
        }
    }

    /**
     * Activate a poll
     */
    private void activatePoll() {
        Poll selected = pollTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if ("Active".equals(selected.getStatus())) {
                showNotification("Poll is already active", true);
                return;
            }

            if (pollService.activatePoll(selected.getId())) {
                loadPollData();
                showNotification("Poll activated successfully");
            } else {
                showNotification("Failed to activate poll", true);
            }
        } else {
            showNotification("Please select a poll to activate", true);
        }
    }

    /**
     * Complete a poll
     */
    private void completePoll() {
        Poll selected = pollTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if ("Completed".equals(selected.getStatus())) {
                showNotification("Poll is already completed", true);
                return;
            }

            if (pollService.completePoll(selected.getId())) {
                loadPollData();
                showNotification("Poll marked as completed");
            } else {
                showNotification("Failed to complete poll", true);
            }
        } else {
            showNotification("Please select a poll to complete", true);
        }
    }

    /**
     * Load poll data into the table
     */
    private void loadPollData() {
        pollTableView.setItems(pollService.getAllPolls());
    }

    /**
     * Show a notification to the user
     */
    private void showNotification(String message) {
        showNotification(message, false);
    }

    /**
     * Show a notification to the user with error option
     */
    private void showNotification(String message, boolean isError) {
        Alert alert = new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(isError ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}