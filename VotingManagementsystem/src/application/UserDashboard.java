package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import models.Poll;
import models.Vote;
import services.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.application.Platform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *inheritance: public class userdashboard
 */
public class UserDashboard extends Application {
    private TableView<Poll> pollTableView;
    
   /**
    * composition
    */
    private StackPane contentArea;
    private VBox pollListView;
    private VBox pollDetailsView;
    private VBox profileView;
    
    /**
     * encapsultion: private instance
     */
    // User information
    private String userEmail;
    private int userId;
    private String currentUserName = "";
    private Map<String, String> userProfile = new HashMap<>();
    private Poll selectedPoll;
    private Button activeSidebarButton;
    /**
     * polymorphism
     */
    private DatabaseService dbService;
    private PollService pollService;
    private VoteService voteService;
    
    // Map to track which polls the user has voted in
    private List<Integer> userVotedPolls;
    
    // Scheduler for periodic UI updates
    private ScheduledExecutorService scheduler;
    
    public UserDashboard() {
        this.dbService = new DatabaseService();
        this.pollService = new DatabasePollService();
        this.voteService = new DatabaseVoteService();
        this.userVotedPolls = new ArrayList<>(); // Initialize to empty list
    }
    
    // Constructor that accepts the user's email
    public UserDashboard(String userEmail) {
        this();
        this.userEmail = userEmail;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load user profile if email is available
            if (userEmail != null && !userEmail.isEmpty()) {
                loadUserProfile();
                
                // Make sure we have a valid userId before loading votes
                if (userId > 0) {
                    // Debug votes table
                    debugCheckVotes();
                    
                    // Load the list of polls the user has voted in
                    refreshVotedPolls();
                    System.out.println("Loaded " + userVotedPolls.size() + " voted polls for user " + userId);
                } else {
                    System.err.println("Invalid userId: " + userId + " - cannot load voted polls");
                    userVotedPolls = new ArrayList<>();
                }
            } else {
                userVotedPolls = new ArrayList<>();
            }
            
            setupUI(primaryStage);
            
            // Start scheduler for periodic updates
            startPeriodicUpdates();
            
            // Set up close handler to shut down scheduler
            primaryStage.setOnCloseRequest(e -> {
                stopPeriodicUpdates();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Start periodic updates for real-time poll data
     */
 
    private void startPeriodicUpdates() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Schedule a task to refresh poll data every 30 seconds
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                // Only refresh if userId is valid
                if (userId > 0) {
                    refreshPollData();
                    
                    // If on poll details view, update it
                    if (contentArea.getChildren().contains(pollDetailsView) && selectedPoll != null) {
                        int currentPollId = selectedPoll.getId();
                        // Refresh the poll object with latest data
                        Poll updatedPoll = ((DatabasePollService)pollService).getPollById(currentPollId);
                        if (updatedPoll != null) {
                            selectedPoll = updatedPoll;
                            updatePollDetailsView();
                        }
                    }
                }
            });
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Stop periodic updates when application closes
     */
    private void stopPeriodicUpdates() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Error shutting down scheduler: " + e.getMessage());
            }
        }
    }
    
    /**
     * Debug method to directly check votes in database
     */
    private void debugCheckVotes() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("\n=== DEBUGGING VOTES TABLE CONTENT ===");
            // Show all votes to check if they're being saved properly
            ResultSet allVotes = stmt.executeQuery("SELECT * FROM votes");
            System.out.println("All votes in database:");
            while (allVotes.next()) {
                System.out.println("Vote ID: " + allVotes.getInt("id") + 
                                  " | User: " + allVotes.getInt("user_id") + 
                                  " | Poll: " + allVotes.getInt("poll_id") + 
                                  " | Candidate: " + allVotes.getInt("candidate_id"));
            }
            // Check specific user's votes
            ResultSet userVotes = stmt.executeQuery("SELECT * FROM votes WHERE user_id = " + userId);
            System.out.println("\nCurrent user's votes (user_id = " + userId + "):");
            int count = 0;
            while (userVotes.next()) {
                count++;
                System.out.println("Vote ID: " + userVotes.getInt("id") + 
                                  " | Poll: " + userVotes.getInt("poll_id") + 
                                  " | Candidate: " + userVotes.getInt("candidate_id"));
            }
            System.out.println("Total votes for user " + userId + ": " + count);
            // Check which polls user has voted in
            ResultSet distinctPolls = stmt.executeQuery(
                "SELECT DISTINCT poll_id FROM votes WHERE user_id = " + userId);
            System.out.println("\nDistinct polls voted by user " + userId + ":");
            List<Integer> pollsVoted = new ArrayList<>();
            while (distinctPolls.next()) {
                int pollId = distinctPolls.getInt("poll_id");
                pollsVoted.add(pollId);
                System.out.println("Poll ID: " + pollId);
            }
            System.out.println("Total distinct polls voted: " + pollsVoted.size());
            System.out.println("=== END OF DEBUGGING INFO ===\n");
        } catch (SQLException e) {
            System.err.println("Error debugging votes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load user profile from database
     */
    private void loadUserProfile() {
        try {
            System.out.println("\n=== USER LOGIN INFORMATION ===");
            System.out.println("Email being used for login: " + userEmail);
            
            // If you're using dbService.getUserProfile
            userProfile = dbService.getUserProfile(userEmail);
            
            if (userProfile != null && !userProfile.isEmpty()) {
                currentUserName = userProfile.get("full_name");
                userId = Integer.parseInt(userProfile.get("id"));
                System.out.println("Successfully loaded profile - UserID: " + userId + ", Name: " + currentUserName);
                
                // Debug: Check if this user has any votes
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "SELECT COUNT(*) FROM votes WHERE user_id = ?")) {
                         
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        int voteCount = rs.getInt(1);
                        System.out.println("This user has " + voteCount + " votes recorded in the database");
                        
                        // If they have votes, list the polls they've voted in
                        if (voteCount > 0) {
                            PreparedStatement pollStmt = conn.prepareStatement(
                                "SELECT p.id, p.name FROM votes v " +
                                "JOIN polls p ON v.poll_id = p.id " +
                                "WHERE v.user_id = ?");
                            pollStmt.setInt(1, userId);
                            ResultSet pollRs = pollStmt.executeQuery();
                            
                            System.out.println("Polls voted in:");
                            while (pollRs.next()) {
                                System.out.println("  - Poll ID: " + pollRs.getInt("id") + 
                                                  ", Name: " + pollRs.getString("name"));
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error checking user votes: " + e.getMessage());
                }
                
            } else {
                System.err.println("Could not load user profile for email: " + userEmail);
            }
            System.out.println("=== END USER LOGIN INFO ===\n");
        } catch (SQLException e) {
            System.err.println("Error loading user profile: " + e.getMessage());
        }
    }
    
    /**
     * Refresh the user's voted polls list from the database
     */
    private void refreshVotedPolls() {
        if (userId > 0) {
            userVotedPolls = voteService.getPollsVotedByUser(userId);
            System.out.println("Refreshed voted polls for user " + userId + ": " + userVotedPolls.size() + " polls");
        } else {
            // Initialize to empty list if userId is invalid
            userVotedPolls = new ArrayList<>();
            System.out.println("Cannot refresh voted polls: Invalid userId " + userId);
        }
    }
    
    /**
     * Refresh all poll data from the database
     */
    private void refreshPollData() {
        try {
            // Refresh voted polls list
            refreshVotedPolls();
            
            // Reload current table data based on what's currently shown
            if (pollTableView.getItems() != null) {
                if (pollTableView.getItems() == FXCollections.observableArrayList(((DatabasePollService)pollService).getAllPolls())) {
                    loadPollData();
                } else if (pollTableView.getItems() == FXCollections.observableArrayList(((DatabasePollService)pollService).getActivePolls())) {
                    loadActivePolls();
                } else if (pollTableView.getItems() == FXCollections.observableArrayList(((DatabasePollService)pollService).getCompletedPolls())) {
                    loadCompletedPolls();
                } else {
                    // Must be showing voted polls
                    loadVotedPolls();
                }
            }
        } catch (Exception e) {
            System.err.println("Error refreshing poll data: " + e.getMessage());
        }
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
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle("-fx-background-color: #F5F7FA;");
        root.setCenter(contentArea);
        
        // Create the views
        pollListView = createPollListView();
        pollDetailsView = createPollDetailsView();
        profileView = createProfileView();
        
        // Show poll list view by default
        showPollListView();
        
        Scene scene = new Scene(root, 1200, 800);
        
        // Load CSS if available, otherwise use inline styles
        try {
            scene.getStylesheets().add(getClass().getResource("/resources/style1.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, using inline styles");
        }
        
        stage.setTitle("Voter Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.show();
    }
    
    /**
     * Create the header component for the dashboard
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");
        
        // App logo/title
        Label appTitle = new Label("Online Voting Management System");
        appTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // User info and logout button
        HBox userInfo = new HBox(10);
        userInfo.setAlignment(Pos.CENTER);
        
        // User circle with initials
        StackPane userCircle = new StackPane();
        userCircle.setPrefSize(35, 35);
        userCircle.setStyle("-fx-background-color: #1976D2; -fx-background-radius: 50%;");
        
        // Get initials
        String initials = "";
        if (currentUserName != null && !currentUserName.isEmpty()) {
            String[] nameParts = currentUserName.split(" ");
            if (nameParts.length > 0) {
                initials += nameParts[0].substring(0, 1).toUpperCase();
                if (nameParts.length > 1) {
                    initials += nameParts[nameParts.length - 1].substring(0, 1).toUpperCase();
                }
            }
        } else {
            initials = "U";
        }
        
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        userCircle.getChildren().add(initialsLabel);
        
        // User name
        Label userName = new Label(currentUserName.isEmpty() ? "User" : currentUserName);
        userName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF5252; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> handleLogout());
        
        userInfo.getChildren().addAll(userCircle, userName, logoutBtn);
        header.getChildren().addAll(appTitle, spacer, userInfo);
        
        return header;
    }
    
    /**
     * Create the sidebar navigation component
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #2C3E50;");
        
        // Create header for sidebar with user welcome
        VBox sidebarHeader = new VBox(5);
        sidebarHeader.setPadding(new Insets(30, 20, 20, 20));
        sidebarHeader.setStyle("-fx-border-color: #3c5268; -fx-border-width: 0 0 1 0;");
        
        // Welcome text
        Text welcomeText = new Text("Welcome");
        welcomeText.setStyle("-fx-fill: #B0BEC5; -fx-font-size: 14px;");
        
        // User name
        Text userNameText = new Text(currentUserName.isEmpty() ? "User" : currentUserName);
        userNameText.setStyle("-fx-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        sidebarHeader.getChildren().addAll(welcomeText, userNameText);
        
        // Create navigation menu
        VBox navMenu = new VBox();
        navMenu.setPadding(new Insets(20, 0, 0, 0));
        
        // Create navigation buttons
        Button pollsBtn = createNavButton("Polls", "📋");
        Button profileBtn = createNavButton("My Profile", "👤");
        
        // Set active button styling on click
        pollsBtn.setOnAction(e -> {
            setActiveButton(pollsBtn);
            showPollListView();
        });
        
        profileBtn.setOnAction(e -> {
            setActiveButton(profileBtn);
            showProfileView();
        });
        
        // Add buttons to menu
        navMenu.getChildren().addAll(pollsBtn, profileBtn);
        
        // Set polls as active by default
        setActiveButton(pollsBtn);
        
        // Add footer with version info
        VBox footer = new VBox();
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.BOTTOM_LEFT);
        
        Text versionText = new Text("Version 1.0");
        versionText.setStyle("-fx-fill: #78909C; -fx-font-size: 12px;");
        footer.getChildren().add(versionText);
        
        // Spacer to push footer to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        sidebar.getChildren().addAll(sidebarHeader, navMenu, spacer, footer);
        return sidebar;
    }
    
    /**
     * Create a navigation button for the sidebar
     */
    private Button createNavButton(String text, String icon) {
        Button button = new Button(icon + " " + text);
        button.setPrefWidth(220);
        button.setPrefHeight(50);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(0, 0, 0, 20));
        button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #B0BEC5;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;"
        );
        
        // Hover effect
        button.setOnMouseEntered(e -> {
            if (button != activeSidebarButton) {
                button.setStyle(
                    "-fx-background-color: #3c5268;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;"
                );
            }
        });
        
        button.setOnMouseExited(e -> {
            if (button != activeSidebarButton) {
                button.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #B0BEC5;" +
                    "-fx-font-size: 14px;"
                );
            }
        });
        
        return button;
    }
    
    /**
     * Set the active sidebar button
     */
    private void setActiveButton(Button button) {
        // Reset previous active button
        if (activeSidebarButton != null) {
            activeSidebarButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #B0BEC5;" +
                "-fx-font-size: 14px;"
            );
        }
        
        // Style the new active button
        button.setStyle(
            "-fx-background-color: #1976D2;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;"
        );
        
        activeSidebarButton = button;
    }
    
    /**
     * Create the poll list view
     */
    private VBox createPollListView() {
        VBox pane = new VBox(25);
        pane.setPadding(new Insets(0));
        
        // Header section with title
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);
        
        VBox titleSection = new VBox(5);
        Label title = new Label("Available Polls");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        Label subtitle = new Label("Browse available polls and cast your vote");
        subtitle.setStyle("-fx-text-fill: #78909C; -fx-font-size: 14px;");
        titleSection.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_RIGHT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search polls...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 8px 12px; -fx-pref-height: 38px; -fx-font-size: 14px;");
        Button searchBtn = createButton("Search");
        searchBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");
        searchBox.getChildren().addAll(searchField, searchBtn);
        
        header.getChildren().addAll(titleSection, spacer, searchBox);
        
        // Stats section with cards in a row
        HBox statsBox = new HBox(15);
        statsBox.setPrefHeight(100);
        
        VBox totalPollsCard = createStatsCard("Available Polls",
            String.valueOf(pollService.getActivePollCount()), "📋", "#1976D2");
        
        VBox votedPollsCard = createStatsCard("Polls You Voted",
            String.valueOf(userVotedPolls != null ? userVotedPolls.size() : 0), "✅", "#4CAF50");
        
        VBox pendingPollsCard = createStatsCard("Polls Pending",
            String.valueOf(pollService.getActivePollCount() -
            (userVotedPolls != null ? userVotedPolls.size() : 0)), "❎", "#FF9800");
        
        VBox completedPollsCard = createStatsCard("Completed Polls",
            String.valueOf(pollService.getCompletedPollCount()), "🏁", "#FF5252");
        
        statsBox.getChildren().addAll(totalPollsCard, votedPollsCard, pendingPollsCard, completedPollsCard);
        HBox.setHgrow(totalPollsCard, Priority.ALWAYS);
        HBox.setHgrow(votedPollsCard, Priority.ALWAYS);
        HBox.setHgrow(pendingPollsCard, Priority.ALWAYS);
        HBox.setHgrow(completedPollsCard, Priority.ALWAYS);
        
        // Filter section
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-font-weight: bold;");
        
        ToggleGroup filterGroup = new ToggleGroup();
     
        
        RadioButton activeRadio = new RadioButton("Active");
        activeRadio.setToggleGroup(filterGroup);
        
        RadioButton votedRadio = new RadioButton("Voted");
        votedRadio.setToggleGroup(filterGroup);
        
        RadioButton completedRadio = new RadioButton("Completed");
        completedRadio.setToggleGroup(filterGroup);
        
        filterBox.getChildren().addAll(
            filterLabel, activeRadio, votedRadio, completedRadio
        );
        
        // Add filter functionality
        activeRadio.setOnAction(e -> loadActivePolls());
        votedRadio.setOnAction(e -> loadVotedPolls());
        completedRadio.setOnAction(e -> loadCompletedPolls());
        
        // Table with card styling
        VBox tableCard = createCard();
        tableCard.setSpacing(15);
        
        // Table header
        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tableTitle = new Label("Polls");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        tableHeader.getChildren().add(tableTitle);
        
        // Create poll table
        pollTableView = new TableView<>();
        pollTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pollTableView.setPlaceholder(new Label("No polls found"));
        setupPollTableColumns();
        
        // Double-click event handler for table rows
        pollTableView.setRowFactory(tv -> {
            TableRow<Poll> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Poll poll = row.getItem();
                    showPollDetailsView(poll);
                }
            });
            return row;
        });
        
        tableCard.getChildren().addAll(tableHeader, pollTableView);
        
        // Create a section for completed polls and results
        VBox completedPollsSection = createCompletedPollsSection();
        
        Label instructionLabel = new Label("Double-click on a poll to view details and cast your vote");
        instructionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C;");
        instructionLabel.setPadding(new Insets(10, 0, 0, 0));
        
        // Add all components to main pane
        pane.getChildren().addAll(header, statsBox, filterBox, tableCard, completedPollsSection, instructionLabel);
        
        return pane;
    }
    
    /**
     * Create a section displaying completed polls with results
     */
    private VBox createCompletedPollsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 0, 0, 0));
        
        Label sectionTitle = new Label("Completed Polls");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label sectionDesc = new Label("View final results from completed polls");
        sectionDesc.setStyle("-fx-text-fill: #78909C; -fx-font-size: 14px;");
        
        // Create a flow pane to display completed poll cards
        FlowPane completedPollsPane = new FlowPane();
        completedPollsPane.setHgap(15);
        completedPollsPane.setVgap(15);
        completedPollsPane.setPrefWrapLength(1000); // Wrap at 1000px width
        
     // Force refresh of completed polls from database to ensure we have latest status
        ObservableList<Poll> completedPolls = ((DatabasePollService)pollService).getCompletedPolls();
        System.out.println("Found " + (completedPolls != null ? completedPolls.size() : 0) + " completed polls to display");

        if (completedPolls == null || completedPolls.isEmpty()) {
            // Show message if no completed polls
            Label noCompletedLabel = new Label("No completed polls available yet.");
            noCompletedLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C;");
            completedPollsPane.getChildren().add(noCompletedLabel);
        } else {
            // Create a card for each completed poll
            for (Poll poll : completedPolls) {
                if (poll != null) { // Add null check
                    VBox pollCard = createCompletedPollCard(poll);
                    completedPollsPane.getChildren().add(pollCard);
                }
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(completedPollsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        section.getChildren().addAll(sectionTitle, sectionDesc, scrollPane);
        return section;
    }
    
    /**
     * Create a card displaying a completed poll with results
     */
    private VBox createCompletedPollCard(Poll poll) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);
        card.setPrefHeight(220);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-width: 1px;"
        );
        
        // Poll name
        Label nameLabel = new Label(poll.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        
        // Status badge
        Label statusLabel = new Label("COMPLETED");
        statusLabel.setStyle(
            "-fx-background-color: #FF5252;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 2px 8px;" +
            "-fx-background-radius: 4px;"
        );
        
        // Horizontal layout for poll name and status
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(nameLabel, spacer, statusLabel);
        
        // Candidate results
        int totalVotes = poll.getTotalVotes();
        int candidate1Votes = voteService.getVoteCountForCandidate(poll.getId(), 1);
        int candidate2Votes = voteService.getVoteCountForCandidate(poll.getId(), 2);
        double candidate1Percentage = totalVotes > 0 ? (double)candidate1Votes / totalVotes * 100 : 0;
        double candidate2Percentage = totalVotes > 0 ? (double)candidate2Votes / totalVotes * 100 : 0;
        
        // Candidate 1 result
        Label cand1Label = new Label(poll.getCandidate1());
        cand1Label.setStyle("-fx-font-weight: bold;");
        ProgressBar cand1Bar = new ProgressBar(candidate1Percentage / 100);
        cand1Bar.setPrefWidth(Double.MAX_VALUE);
        cand1Bar.setPrefHeight(10);
        cand1Bar.setStyle("-fx-accent: #1976D2;");
        Label cand1VotesLabel = new Label(String.format("%d votes (%.1f%%)",
            candidate1Votes, candidate1Percentage));
        cand1VotesLabel.setStyle("-fx-text-fill: #78909C; -fx-font-size: 12px;");
        
        // Candidate 2 result
        Label cand2Label = new Label(poll.getCandidate2());
        cand2Label.setStyle("-fx-font-weight: bold;");
        ProgressBar cand2Bar = new ProgressBar(candidate2Percentage / 100);
        cand2Bar.setPrefWidth(Double.MAX_VALUE);
        cand2Bar.setPrefHeight(10);
        cand2Bar.setStyle("-fx-accent: #FF5252;");
        Label cand2VotesLabel = new Label(String.format("%d votes (%.1f%%)",
                candidate2Votes, candidate2Percentage));
            cand2VotesLabel.setStyle("-fx-text-fill: #78909C; -fx-font-size: 12px;");
            
            // Total votes
            Label totalVotesLabel = new Label("Total Votes: " + totalVotes);
            totalVotesLabel.setStyle("-fx-font-weight: bold;");
            
            // Winner label
            Label winnerLabel = new Label();
            if (candidate1Votes > candidate2Votes) {
                winnerLabel.setText("Winner: " + poll.getCandidate1() + " 🏆");
                winnerLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else if (candidate2Votes > candidate1Votes) {
                winnerLabel.setText("Winner: " + poll.getCandidate2() + " 🏆");
                winnerLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else {
                winnerLabel.setText("Result: Tie");
                winnerLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            }
            
            // View results button
            Button viewResultsBtn = new Button("View Detailed Results");
            viewResultsBtn.setStyle(
                "-fx-background-color: #1976D2;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-pref-width: 100%;"
            );
            viewResultsBtn.setPrefWidth(Double.MAX_VALUE);
            viewResultsBtn.setOnAction(e -> showResultsPopup(poll, candidate1Votes, candidate2Votes));
            
            // Check if user has voted in this poll
            boolean hasVoted = voteService.hasUserVoted(userId, poll.getId());
            Label votedLabel = null;
            if (hasVoted) {
                votedLabel = new Label("You voted in this poll");
                votedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-style: italic; -fx-font-size: 12px;");
            }
            
            // Add all components to card
            VBox content = new VBox(5);
            content.getChildren().addAll(
                headerBox,
                new Separator(),
                cand1Label,
                cand1Bar,
                cand1VotesLabel,
                cand2Label,
                cand2Bar,
                cand2VotesLabel,
                totalVotesLabel,
                winnerLabel
            );
            
            if (votedLabel != null) {
                content.getChildren().add(votedLabel);
            }
            
            content.getChildren().add(viewResultsBtn);
            card.getChildren().add(content);
            
            return card;
        }
        
        /**
         * Load only active polls into the table
         */
        private void loadActivePolls() {
            pollTableView.setItems(((DatabasePollService)pollService).getActivePolls());
        }
        
        /**
         * Load only polls the user has voted in
         */
        private void loadVotedPolls() {
            // Refresh the user's voted polls list first to ensure accuracy
            if (userId > 0) {
                userVotedPolls = voteService.getPollsVotedByUser(userId);
            }
            
            ObservableList<Poll> allPolls = ((DatabasePollService)pollService).getAllPolls();
            ObservableList<Poll> votedPolls = FXCollections.observableArrayList();
            
            for (Poll poll : allPolls) {
                if (voteService.hasUserVoted(userId, poll.getId())) {
                    votedPolls.add(poll);
                }
            }
            
            pollTableView.setItems(votedPolls);
        }
        
        /**
         * Load only completed polls
         */
        private void loadCompletedPolls() {
            pollTableView.setItems(((DatabasePollService)pollService).getCompletedPolls());
        }
        
        /**
         * Load all polls into the table
         */
        private void loadPollData() {
            pollTableView.setItems(((DatabasePollService)pollService).getAllPolls());
        }
        
        /**
         * Show the poll list view
         */
        private void showPollListView() {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(pollListView);
            
            // Refresh the user's voted polls list before loading polls
            if (userId > 0) {
                userVotedPolls = voteService.getPollsVotedByUser(userId);
            }
            
            // Refresh the table with active polls by default
            loadActivePolls();
        }
        
        /**
         * Show the profile view
         */
        private void showProfileView() {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(profileView);
        }
        
        /**
         * Shows a statistics popup with voting percentages and a results button
         *
         * @param poll The poll to show statistics for
         */
        private void showPollStatisticsPopup(Poll poll) {
            // Check if user can view statistics
            boolean isCompleted = "Completed".equals(poll.getStatus());
            boolean hasVoted = voteService.hasUserVoted(userId, poll.getId());
            
            // If poll is active and user hasn't voted, don't show detailed stats
            if (!isCompleted && !hasVoted) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Statistics Unavailable");
                alert.setHeaderText("You must vote to see statistics");
                alert.setContentText("You need to cast your vote in this poll to view its statistics, or wait until the poll is completed.");
                alert.showAndWait();
                return;
            }
            
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
            
            // Get actual vote counts from database
            int totalVotes = poll.getTotalVotes();
            int candidate1Votes = voteService.getVoteCountForCandidate(poll.getId(), 1);
            int candidate2Votes = voteService.getVoteCountForCandidate(poll.getId(), 2);
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
            
            // Check if user voted in this poll
            if (hasVoted) {
                Vote userVote = voteService.getUserVote(userId, poll.getId());
                if (userVote != null) {
                    String votedForCandidate = userVote.getCandidateId() == 1 ?
                        poll.getCandidate1() : poll.getCandidate2();
                    HBox userVoteBox = new HBox(10);
                    userVoteBox.setAlignment(Pos.CENTER_LEFT);
                    userVoteBox.setPadding(new Insets(10, 0, 0, 0));
                    Label voteIcon = new Label("✓");
                    voteIcon.setStyle(
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #4CAF50;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 25px;" +
                        "-fx-min-height: 25px;" +
                        "-fx-alignment: center;"
                    );
                    Label userVotedLabel = new Label("You voted for: " + votedForCandidate);
                    userVotedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    userVoteBox.getChildren().addAll(voteIcon, userVotedLabel);
                    chartContainer.getChildren().add(userVoteBox);
                } else {
                    Label userVotedLabel = new Label("You have voted in this poll");
                    userVotedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    chartContainer.getChildren().add(userVotedLabel);
                }
            }
            
            // Bottom buttons
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            // Results button (shows who won in a popup)
            Button resultsBtn = createButton("Show Results");
            resultsBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            resultsBtn.setPrefWidth(150);
            resultsBtn.setOnAction(e -> showResultsPopup(poll, candidate1Votes, candidate2Votes));
            
            // Close button
            Button closeBtn = createButton("Close");
            closeBtn.setStyle("-fx-background-color: #78909C; -fx-text-fill: white; -fx-font-weight: bold;");
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
         * Enhanced to only show detailed results for completed polls or if user has voted
         *
         * @param poll The poll to show results for
         * @param candidate1Votes Votes for candidate 1
         * @param candidate2Votes Votes for candidate 2
         */
        private void showResultsPopup(Poll poll, int candidate1Votes, int candidate2Votes) {
            // Check if user can view results
            boolean isCompleted = "Completed".equals(poll.getStatus());
            boolean hasVoted = voteService.hasUserVoted(userId, poll.getId());
            
            // If poll is active and user hasn't voted, don't show results
            if (!isCompleted && !hasVoted) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Results Unavailable");
                alert.setHeaderText("Results not available");
                alert.setContentText("You need to cast your vote in this poll to view partial results, or wait until the poll is completed by the administrator.");
                alert.showAndWait();
                return;
            }
            
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
            
            // Different content based on whether poll is completed
            if (isCompleted) {
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
                container.getChildren().addAll(titleLabel, pollNameLabel, winnerBox);
            } else {
                // Show partial results for active polls
                VBox partialResultsBox = new VBox(10);
                partialResultsBox.setAlignment(Pos.CENTER);
                partialResultsBox.setPadding(new Insets(20));
                partialResultsBox.setStyle(
                    "-fx-background-color: #F8F9FA;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-color: #EAEAEA;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 10px;"
                );
                
                Label partialLabel = new Label("Partial Results");
                partialLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FF9800;");
                
                Label cand1VotesLabel = new Label(poll.getCandidate1() + ": " + candidate1Votes + " votes");
                cand1VotesLabel.setStyle("-fx-font-size: 16px;");
                
                Label cand2VotesLabel = new Label(poll.getCandidate2() + ": " + candidate2Votes + " votes");
                cand2VotesLabel.setStyle("-fx-font-size: 16px;");
                
                Label totalLabel = new Label("Total votes so far: " + poll.getTotalVotes());
                totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                
                Label disclaimerLabel = new Label("These are partial results. Final results will be available when the poll is completed by the administrator.");
                disclaimerLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C; -fx-font-size: 12px;");
                disclaimerLabel.setWrapText(true);
                
                partialResultsBox.getChildren().addAll(partialLabel, cand1VotesLabel, 
                                                    cand2VotesLabel, totalLabel, disclaimerLabel);
                
                container.getChildren().addAll(titleLabel, pollNameLabel, partialResultsBox);
            }
            
            // Check if user voted
            if (hasVoted) {
                Vote userVote = voteService.getUserVote(userId, poll.getId());
                if (userVote != null) {
                    String votedForCandidate = userVote.getCandidateId() == 1 ?
                        poll.getCandidate1() : poll.getCandidate2();
                    Label userVotedLabel = new Label("You voted for: " + votedForCandidate);
                    userVotedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-style: italic;");
                    container.getChildren().add(userVotedLabel);
                } else {
                    Label userVotedLabel = new Label("You participated in this poll");
                    userVotedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-style: italic;");
                    container.getChildren().add(userVotedLabel);
                }
            }
            
            // Close button
            Button closeBtn = createButton("Close");
            closeBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");
            closeBtn.setPrefWidth(120);
            closeBtn.setOnAction(e -> resultsStage.close());
            
            // Add components to main container
            container.getChildren().add(closeBtn);
            
            // Create and show scene
            Scene scene = new Scene(container);
            resultsStage.setScene(scene);
            resultsStage.show();
        }
        
        /**
         * Create a new profile view that shows user information
         */
        private VBox createProfileView() {
            VBox pane = new VBox(25);
            pane.setPadding(new Insets(0));
            
            // Header section with title
            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            header.setSpacing(15);
            VBox titleSection = new VBox(5);
            Label title = new Label("My Profile");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            Label subtitle = new Label("View your account details");
            subtitle.setStyle("-fx-text-fill: #78909C; -fx-font-size: 14px;");
            titleSection.getChildren().addAll(title, subtitle);
            header.getChildren().add(titleSection);
            
            // Profile information card
            VBox profileCard = createCard();
            profileCard.setSpacing(20);
            profileCard.setPadding(new Insets(30));
            
            // Profile header with avatar
            HBox profileHeader = new HBox(20);
            profileHeader.setAlignment(Pos.CENTER_LEFT);
            
            // Avatar circle
            StackPane avatarPane = new StackPane();
            avatarPane.setPrefSize(100, 100);
            avatarPane.setStyle("-fx-background-color: #6A5ACD; -fx-background-radius: 50%;");
            
            // Get initials for avatar
            String initials = "";
            if (userProfile.containsKey("full_name") && !userProfile.get("full_name").isEmpty()) {
                String[] nameParts = userProfile.get("full_name").split(" ");
                if (nameParts.length > 0) {
                    initials += nameParts[0].substring(0, 1).toUpperCase();
                    if (nameParts.length > 1) {
                        initials += nameParts[nameParts.length - 1].substring(0, 1).toUpperCase();
                    }
                }
            } else {
                initials = "U";
            }
            
            Label initialsLabel = new Label(initials);
            initialsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");
            avatarPane.getChildren().add(initialsLabel);
            
            // Profile name and info
            VBox profileInfo = new VBox(5);
            Label profileName = new Label(userProfile.getOrDefault("full_name", "User"));
            profileName.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            Label profileEmail = new Label(userProfile.getOrDefault("email", ""));
            profileEmail.setStyle("-fx-font-size: 14px; -fx-text-fill: #78909C;");
            profileInfo.getChildren().addAll(profileName, profileEmail);
            profileHeader.getChildren().addAll(avatarPane, profileInfo);
            
            // Separator
            Separator separator = new Separator();
            separator.setStyle("-fx-opacity: 0.3;");
            
            // Profile details grid
            GridPane detailsGrid = new GridPane();
            detailsGrid.setHgap(30);
            detailsGrid.setVgap(20);
            detailsGrid.setPadding(new Insets(20, 0, 20, 0));
            
            // Add profile details
            int row = 0;
            // Full Name
            detailsGrid.add(createProfileFieldLabel("Full Name"), 0, row);
            detailsGrid.add(createProfileValueLabel(userProfile.getOrDefault("full_name", "Not provided")), 1, row);
            row++;
            
            // Email
            detailsGrid.add(createProfileFieldLabel("Email Address"), 0, row);
            detailsGrid.add(createProfileValueLabel(userProfile.getOrDefault("email", "Not provided")), 1, row);
            row++;
            
            // Gender
            detailsGrid.add(createProfileFieldLabel("Gender"), 0, row);
            detailsGrid.add(createProfileValueLabel(userProfile.getOrDefault("gender", "Not provided")), 1, row);
            row++;
            
            // Phone
            detailsGrid.add(createProfileFieldLabel("Phone Number"), 0, row);
            detailsGrid.add(createProfileValueLabel(userProfile.getOrDefault("phone", "Not provided")), 1, row);
            row++;
            
            // Account Created
            detailsGrid.add(createProfileFieldLabel("Account ID"), 0, row);
            detailsGrid.add(createProfileValueLabel(userProfile.getOrDefault("id", "Not available")), 1, row);
            
            // Voting stats
            row++;
            detailsGrid.add(createProfileFieldLabel("Polls Voted"), 0, row);
            detailsGrid.add(createProfileValueLabel(String.valueOf(userVotedPolls != null ? userVotedPolls.size() : 0)), 1, row);
            
            // Make columns responsive
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(30);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(70);
            detailsGrid.getColumnConstraints().addAll(col1, col2);
            
            // Recent activity section
            VBox activitySection = new VBox(10);
            Label activityTitle = new Label("Recent Activity");
            activityTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            // Get user's voted polls for recent activity
            VBox activityList = new VBox(10);
            ObservableList<Poll> allPolls = ((DatabasePollService)pollService).getAllPolls();
            int activityCount = 0;
            for (Poll poll : allPolls) {
                if (voteService.hasUserVoted(userId, poll.getId()) && activityCount < 5) {
                    HBox activityItem = new HBox(10);
                    activityItem.setAlignment(Pos.CENTER_LEFT);
                    Label activityIcon = new Label("✓");
                    activityIcon.setStyle(
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #4CAF50;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 25px;" +
                        "-fx-min-height: 25px;" +
                        "-fx-alignment: center;"
                    );
                    VBox activityDetails = new VBox(2);
                    Label activityLabel = new Label("You voted in \"" + poll.getName() + "\"");
                    activityLabel.setStyle("-fx-font-weight: bold;");
                    Label activityStatus = new Label("Status: " + poll.getStatus());
                    activityStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #78909C;");
                    activityDetails.getChildren().addAll(activityLabel, activityStatus);
                    activityItem.getChildren().addAll(activityIcon, activityDetails);
                    activityList.getChildren().add(activityItem);
                    activityCount++;
                }
            }
            
            if (activityCount == 0) {
                Label noActivityLabel = new Label("No recent voting activity");
                noActivityLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C;");
                activityList.getChildren().add(noActivityLabel);
            }
            
            activitySection.getChildren().addAll(activityTitle, activityList);
            
            // Action buttons
            HBox actionButtons = new HBox(15);
            actionButtons.setAlignment(Pos.CENTER_RIGHT);
            Button goToPollsBtn = createButton("Go to Polls");
            goToPollsBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");
            goToPollsBtn.setOnAction(e -> {
                // Find the polls button in the sidebar and set it active
                for (javafx.scene.Node node : ((VBox)((BorderPane)contentArea.getScene().getRoot()).getLeft()).getChildren()) {
                    if (node instanceof VBox) {
                        for (javafx.scene.Node child : ((VBox)node).getChildren()) {
                            if (child instanceof Button && ((Button)child).getText().contains("Polls")) {
                                setActiveButton((Button)child);
                                break;
                            }
                        }
                    }
                }
                showPollListView();
            });
            actionButtons.getChildren().add(goToPollsBtn);
            
            // Add all components to profile card
            profileCard.getChildren().addAll(
                profileHeader,
                separator,
                detailsGrid,
                new Separator(),
                activitySection,
                actionButtons
            );
            
            // Add all components to main pane
            pane.getChildren().addAll(header, profileCard);
            
            return pane;
        }
        
        private Label createProfileFieldLabel(String text) {
            Label label = new Label(text + ":");
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            return label;
        }
        
        private Label createProfileValueLabel(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-size: 14px;");
            return label;
        }
        
        /**
         * Create the poll details view
         */
        private VBox createPollDetailsView() {
            VBox pane = new VBox(25);
            pane.setPadding(new Insets(0));
            // Empty by default, will be populated in updatePollDetailsView
            return pane;
        }
        
        /**
         * Show the poll details view for a selected poll
         */
        private void showPollDetailsView(Poll poll) {
            if (poll != null) {
                // Always get the latest poll data from database to ensure it's up to date
                Poll latestPoll = ((DatabasePollService)pollService).getPollById(poll.getId());
                if (latestPoll != null) {
                    selectedPoll = latestPoll;
                } else {
                    selectedPoll = poll;
                }
                
                updatePollDetailsView();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(pollDetailsView);
            } else {
                showNotification("Error: Unable to display poll details", true);
            }
        }
        
        /**
         * Update poll details view with selected poll information
         */
        private void updatePollDetailsView() {
            VBox pane = pollDetailsView;
            pane.getChildren().clear();
            
            if (selectedPoll == null) {
                Label noSelectionLabel = new Label("No poll selected");
                pane.getChildren().add(noSelectionLabel);
                return;
            }
            
            // Always get fresh data about whether the user has voted
            boolean hasVoted = voteService.hasUserVoted(userId, selectedPoll.getId());
            
            // Get the poll status
            boolean isActive = "Active".equals(selectedPoll.getStatus());
            boolean isCompleted = "Completed".equals(selectedPoll.getStatus());
            
            // Header with back button
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            Button backButton = createButton("← Back to Polls");
            backButton.setStyle("-fx-background-color: #78909C; -fx-text-fill: white; -fx-font-weight: bold;");
            backButton.setOnAction(e -> showPollListView());
            
            VBox titleSection = new VBox(5);
            Label title = new Label(selectedPoll.getName());
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            
            // Status with color formatting
            Label statusLabel = createStatusLabel(selectedPoll.getStatus());
            statusLabel.setText("Status: " + selectedPoll.getStatus());
            titleSection.getChildren().addAll(title, statusLabel);
            header.getChildren().addAll(backButton, titleSection);
            
            // Main content card
            VBox pollCard = createCard();
            pollCard.setSpacing(20);
            
            // Check if poll is completed - show results prominently
            if (isCompleted) {
                Label resultsTitle = new Label("Final Results");
                resultsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
                
                // Get the vote counts
                int totalVotes = selectedPoll.getTotalVotes();
                int candidate1Votes = voteService.getVoteCountForCandidate(selectedPoll.getId(), 1);
                int candidate2Votes = voteService.getVoteCountForCandidate(selectedPoll.getId(), 2);
                double candidate1Percentage = totalVotes > 0 ? (double)candidate1Votes / totalVotes * 100 : 0;
                double candidate2Percentage = totalVotes > 0 ? (double)candidate2Votes / totalVotes * 100 : 0;
                
                // Create results UI
                VBox resultsBox = new VBox(20);
                resultsBox.setPadding(new Insets(15));
                resultsBox.setStyle(
                    "-fx-background-color: #F8F9FA;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-color: #E0E0E0;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 10px;"
                );
                
                // Winner section
                HBox winnerBox = new HBox(15);
                winnerBox.setAlignment(Pos.CENTER);
                winnerBox.setPadding(new Insets(15));
                winnerBox.setStyle(
                    "-fx-background-color: #E8F5E9;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: #C8E6C9;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;"
                );
                
                Label trophyLabel = new Label("🏆");
                trophyLabel.setStyle("-fx-font-size: 40px;");
                
                VBox winnerInfoBox = new VBox(5);
                winnerInfoBox.setAlignment(Pos.CENTER_LEFT);
                Label winnerTitleLabel = new Label("WINNER");
                winnerTitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");
                Label winnerNameLabel = new Label();
                
                if (candidate1Votes > candidate2Votes) {
                    winnerNameLabel.setText(selectedPoll.getCandidate1());
                } else if (candidate2Votes > candidate1Votes) {
                    winnerNameLabel.setText(selectedPoll.getCandidate2());
                } else {
                    winnerNameLabel.setText("Tie - No Winner");
                    winnerTitleLabel.setText("RESULT");
                    winnerBox.setStyle(
                        "-fx-background-color: #FFF3E0;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #FFE0B2;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;"
                    );
                    winnerTitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF9800;");
                }
                
                winnerNameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
                String voteText = totalVotes == 1 ? "vote" : "votes";
                Label winnerVotesLabel = new Label("Final vote count: " + totalVotes + " " + voteText);
                winnerVotesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #78909C;");
                winnerInfoBox.getChildren().addAll(winnerTitleLabel, winnerNameLabel, winnerVotesLabel);
                winnerBox.getChildren().addAll(trophyLabel, winnerInfoBox);
                
                // Results charts
                VBox chartBox = new VBox(15);
                
                // Candidate 1
                VBox cand1Box = createCandidateResultBox(
                    selectedPoll.getCandidate1(),
                    candidate1Votes,
                    candidate1Percentage,
                    "#1976D2"
                );
                
                // Candidate 2
                VBox cand2Box = createCandidateResultBox(
                    selectedPoll.getCandidate2(),
                    candidate2Votes,
                    candidate2Percentage,
                    "#FF5252"
                );
                
                // Check if user voted in this poll
                HBox userVoteBox = new HBox(10);
                userVoteBox.setAlignment(Pos.CENTER_LEFT);
                
                if (hasVoted) {
                    Vote userVote = voteService.getUserVote(userId, selectedPoll.getId());
                    if (userVote != null) {
                        String candidateName = userVote.getCandidateId() == 1 ?
                            selectedPoll.getCandidate1() : selectedPoll.getCandidate2();
                        Label voteIcon = new Label("✓");
                        voteIcon.setStyle(
                            "-fx-text-fill: white;" +
                            "-fx-background-color: #4CAF50;" +
                            "-fx-background-radius: 50%;" +
                            "-fx-min-width: 25px;" +
                            "-fx-min-height: 25px;" +
                            "-fx-alignment: center;"
                        );
                        Label userVotedLabel = new Label("You voted for: " + candidateName);
                        userVotedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        userVoteBox.getChildren().addAll(voteIcon, userVotedLabel);
                    } else {
                        Label userVotedLabel = new Label("You voted in this poll");
                        userVotedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        userVoteBox.getChildren().add(userVotedLabel);
                    }
                } else {
                    Label userVoteLabel = new Label("You did not vote in this poll");
                    userVoteLabel.setStyle("-fx-text-fill: #78909C;");
                    userVoteBox.getChildren().add(userVoteLabel);
                }
                
                chartBox.getChildren().addAll(cand1Box, cand2Box, userVoteBox);
                resultsBox.getChildren().addAll(winnerBox, new Separator(), chartBox);
                
                // Add export/share button
                Button shareBtn = createButton("Share Results");
                shareBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");
                shareBtn.setPrefWidth(200);
                shareBtn.setOnAction(e -> shareResults(selectedPoll));
                
                HBox shareBox = new HBox();
                shareBox.setAlignment(Pos.CENTER);
                shareBox.getChildren().add(shareBtn);
                
                // Add everything to the poll card
                pollCard.getChildren().addAll(resultsTitle, resultsBox, shareBox);
                
            } else {
                // For active polls - voting UI
                Label descriptionLabel = new Label("Cast Your Vote");
                descriptionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                
                // Check if user has already voted or if poll is not active
                if (!isActive) {
                    // Poll is not active
                    Label notActiveLabel = new Label("This poll is not currently active.");
                    notActiveLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #FF5252;");
                    
                    Label noticeLabel = new Label("Results will be available once the administrator completes this poll.");
                    noticeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #78909C;");
                    
                    VBox infoBox = new VBox(10);
                    infoBox.getChildren().addAll(notActiveLabel, noticeLabel);
                    
                    pollCard.getChildren().addAll(descriptionLabel, infoBox);
                } else if (hasVoted) {
                    // User has already voted - show clearly what they voted for
                    VBox votedInfo = new VBox(10);
                    Label alreadyVotedLabel = new Label("You have already cast your vote for this poll.");
                    alreadyVotedLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    votedInfo.getChildren().add(alreadyVotedLabel);
                    
                    // Try to find which candidate they voted for
                    Vote userVote = voteService.getUserVote(userId, selectedPoll.getId());
                    if (userVote != null) {
                        String candidateName = userVote.getCandidateId() == 1 ?
                            selectedPoll.getCandidate1() : selectedPoll.getCandidate2();
                        
                        // Create a styled box showing which candidate they voted for
                        HBox voteDetailsBox = new HBox(10);
                        voteDetailsBox.setAlignment(Pos.CENTER_LEFT);
                        voteDetailsBox.setPadding(new Insets(10));
                        voteDetailsBox.setStyle(
                            "-fx-background-color: #E8F5E9;" +
                            "-fx-background-radius: 5px;" +
                            "-fx-border-color: #C8E6C9;" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-radius: 5px;"
                        );
                        
                        Label voteIcon = new Label("✓");
                        voteIcon.setStyle(
                            "-fx-text-fill: white;" +
                            "-fx-background-color: #4CAF50;" +
                            "-fx-background-radius: 50%;" +
                            "-fx-min-width: 25px;" +
                            "-fx-min-height: 25px;" +
                            "-fx-alignment: center;"
                        );
                        
                        Label votedForLabel = new Label("You voted for: " + candidateName);
                        votedForLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                        voteDetailsBox.getChildren().addAll(voteIcon, votedForLabel);
                        votedInfo.getChildren().add(voteDetailsBox);
                    }
                    
                    // Add note about results visibility
                    Label resultsNoteLabel = new Label("Results will be available when the poll is completed by the administrator.");
                    resultsNoteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C;");
                    votedInfo.getChildren().add(resultsNoteLabel);
                    
                    pollCard.getChildren().addAll(descriptionLabel, votedInfo);
                } else {
                    // User can vote
                    VBox votingArea = new VBox(20);
                    votingArea.setPadding(new Insets(10, 0, 10, 0));
                    
                    Label questionLabel = new Label("Please select a candidate:");
                    questionLabel.setStyle("-fx-font-size: 16px;");
                    
                    // Radio buttons for candidates
                    ToggleGroup candidateGroup = new ToggleGroup();
                    RadioButton candidate1Radio = new RadioButton(selectedPoll.getCandidate1());
                    candidate1Radio.setToggleGroup(candidateGroup);
                    candidate1Radio.setUserData(1); // Candidate 1 ID
                    candidate1Radio.setStyle("-fx-font-size: 16px; -fx-padding: 10px 0;");
                    
                    RadioButton candidate2Radio = new RadioButton(selectedPoll.getCandidate2());
                    candidate2Radio.setToggleGroup(candidateGroup);
                    candidate2Radio.setUserData(2); // Candidate 2 ID
                    candidate2Radio.setStyle("-fx-font-size: 16px; -fx-padding: 10px 0;");
                    
                    // Vote button
                    Button voteBtn = createButton("Cast Vote");
                    voteBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                    voteBtn.setPrefWidth(200);
                    voteBtn.setDisable(true); // Disable until a selection is made
                    
                    // Enable vote button when a candidate is selected
                    candidateGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                        voteBtn.setDisable(newVal == null);
                    });
                    
                    // Vote button action - cast actual vote in database
                    voteBtn.setOnAction(e -> {
                        Toggle selectedToggle = candidateGroup.getSelectedToggle();
                        if (selectedToggle != null) {
                            int candidateId = (int) selectedToggle.getUserData();
                            
                            // Double-check vote hasn't been cast yet through direct database check
                            if (voteService.hasUserVoted(userId, selectedPoll.getId())) {
                                showNotification("You have already voted in this poll. Each user can only vote once.", true);
                                updatePollDetailsView();
                                return;
                            }
                            
                            // Cast vote with enhanced double-vote prevention
                            castVote(selectedPoll.getId(), candidateId);
                        }
                    });
                    
                    HBox buttonBox = new HBox();
                    buttonBox.setAlignment(Pos.CENTER);
                    buttonBox.getChildren().add(voteBtn);
                    
                    // Add notice about results
                    Label noticeLabel = new Label("Note: Results will be available when the poll is completed.");
                    noticeLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C;");
                    
                    votingArea.getChildren().addAll(questionLabel, candidate1Radio, candidate2Radio, buttonBox, noticeLabel);
                    pollCard.getChildren().addAll(descriptionLabel, votingArea);
                }
            }
            
            // Results card (live results) - Only show actual results for completed polls or if user has voted
            VBox resultsCard = createCard();
            resultsCard.setSpacing(15);
            Label resultsTitle = new Label("Current Results");
            resultsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            // Chart representation
            VBox chartBox = new VBox(15);
            chartBox.setPadding(new Insets(10));
            chartBox.setStyle(
                "-fx-background-color: #F5F7FA;" +
                "-fx-border-color: #E0E0E0;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 5px;" +
                "-fx-padding: 15px;"
            );
            
            // Get actual vote counts from database
            int totalVotes = selectedPoll.getTotalVotes();
            int candidate1Votes = voteService.getVoteCountForCandidate(selectedPoll.getId(), 1);
            int candidate2Votes = voteService.getVoteCountForCandidate(selectedPoll.getId(), 2);
            double candidate1Percentage = totalVotes > 0 ? (double)candidate1Votes / totalVotes * 100 : 0;
            double candidate2Percentage = totalVotes > 0 ? (double)candidate2Votes / totalVotes * 100 : 0;
            
            // For active polls, only show results if user has voted or admin has completed poll
            if (isCompleted || hasVoted) {
                // Candidate 1 bar
                Label cand1Name = new Label(selectedPoll.getCandidate1());
                cand1Name.setStyle("-fx-font-weight: bold;");
                HBox cand1Bar = new HBox();
                cand1Bar.setPrefHeight(30);
                cand1Bar.setPrefWidth(Math.max(10, (candidate1Percentage / 100) * 400));
                cand1Bar.setStyle(
                    "-fx-background-color: #1976D2;" +
                    "-fx-background-radius: 5px;"
                );
                Label cand1Percentage = new Label(String.format("%.1f%%", candidate1Percentage));
                cand1Percentage.setPadding(new Insets(0, 0, 0, 10));
                cand1Percentage.setStyle("-fx-font-weight: bold;");
                VBox cand1Box = new VBox(5);
                cand1Box.getChildren().addAll(
                    cand1Name,
                    new Label(candidate1Votes + " votes"),
                    new HBox(10, cand1Bar, cand1Percentage)
                );
                
                // Candidate 2 bar
                Label cand2Name = new Label(selectedPoll.getCandidate2());
                cand2Name.setStyle("-fx-font-weight: bold;");
                HBox cand2Bar = new HBox();
                cand2Bar.setPrefHeight(30);
                cand2Bar.setPrefWidth(Math.max(10, (candidate2Percentage / 100) * 400));
                cand2Bar.setStyle(
                    "-fx-background-color: #FF5252;" +
                    "-fx-background-radius: 5px;"
                );
                Label cand2Percentage = new Label(String.format("%.1f%%", candidate2Percentage));
                cand2Percentage.setPadding(new Insets(0, 0, 0, 10));
                cand2Percentage.setStyle("-fx-font-weight: bold;");
                VBox cand2Box = new VBox(5);
                cand2Box.getChildren().addAll(
                    cand2Name,
                    new Label(candidate2Votes + " votes"),
                    new HBox(10, cand2Bar, cand2Percentage)
                );
                
                Label totalVotesLabel = new Label("Total Votes: " + totalVotes);
                totalVotesLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10px 0 0 0;");
                chartBox.getChildren().addAll(cand1Box, cand2Box, totalVotesLabel);
                
                // Add note depending on poll status
                if (isCompleted) {
                    Label completedLabel = new Label("This poll has been completed. Final results are shown above.");
                    completedLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C;");
                    resultsCard.getChildren().addAll(resultsTitle, chartBox, completedLabel);
                } else {
                    Label liveLabel = new Label("Partial results are shown above. Final results will be available when the poll is completed.");
                    liveLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C;");
                    resultsCard.getChildren().addAll(resultsTitle, chartBox, liveLabel);
                }
            } else {
                // If user hasn't voted yet, don't show the actual results
                Label noResultsLabel = new Label("You need to cast your vote to see the current results.");
                noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #78909C;");
                Label votingReminderLabel = new Label("After voting, you'll be able to see the partial results.");
                votingReminderLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #78909C;");
                chartBox.getChildren().addAll(noResultsLabel, votingReminderLabel);
                resultsCard.getChildren().addAll(resultsTitle, chartBox);
            }
            
            // Poll information card
            VBox infoCard = createCard();
            infoCard.setSpacing(15);
            Label infoTitle = new Label("Poll Information");
            infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(15);
            infoGrid.setVgap(10);
            infoGrid.addRow(0, createInfoLabel("Poll ID:"), new Label(String.valueOf(selectedPoll.getId())));
            infoGrid.addRow(1, createInfoLabel("Status:"), createStatusLabel(selectedPoll.getStatus()));
            infoGrid.addRow(2, createInfoLabel("Total Votes:"), new Label(String.valueOf(selectedPoll.getTotalVotes())));
            infoGrid.addRow(3, createInfoLabel("Created by:"), new Label("Election Administrator"));
            
            // Check if user has voted - use the hasVoted we determined earlier
            if (hasVoted) {
                Vote userVote = voteService.getUserVote(userId, selectedPoll.getId());
                if (userVote != null) {
                    String candidateName = userVote.getCandidateId() == 1 ?
                        selectedPoll.getCandidate1() : selectedPoll.getCandidate2();
                    infoGrid.addRow(4, createInfoLabel("Your Vote:"),
                        new Label("You voted for " + candidateName));
                } else {
                    infoGrid.addRow(4, createInfoLabel("Your Vote:"),
                        new Label("You have voted in this poll"));
                }
            } else {
                if (isActive) {
                    infoGrid.addRow(4, createInfoLabel("Your Vote:"),
                        new Label("You have not voted yet"));
                } else {
                    infoGrid.addRow(4, createInfoLabel("Your Vote:"),
                        new Label("You did not vote in this poll"));
                }
            }
            
            infoCard.getChildren().addAll(infoTitle, infoGrid);
            
            // Add all components to main pane
            pane.getChildren().addAll(header, pollCard, resultsCard, infoCard);
        }
        
        /**
         * Cast a vote for the current user with enhanced double-voting prevention
         */
        private void castVote(int pollId, int candidateId) {
            // Use synchronized block to prevent concurrent vote attempts
            synchronized (this) {
                try {
                    System.out.println("\n==== VOTE ATTEMPT DETAILS ====");
                    System.out.println("User ID: " + userId);
                    System.out.println("Poll ID: " + pollId);
                    System.out.println("Candidate ID: " + candidateId);
                    
                    // Double-check that user ID is valid
                    if (userId <= 0) {
                        System.out.println("ERROR: Invalid user ID - cannot vote");
                        showNotification("User ID is invalid. Please logout and login again.", true);
                        return;
                    }
                    
                    // Double-check with fresh database query if user has already voted
                    boolean hasVotedInDb = voteService.hasUserVoted(userId, pollId);
                    System.out.println("Pre-vote database check - Has user voted: " + hasVotedInDb);
                    
                    if (hasVotedInDb) {
                        System.out.println("VOTE PREVENTED: User has already voted according to database check");
                        showNotification("You have already voted in this poll. Each user can only vote once.", true);
                        // Refresh UI immediately
                        refreshVotedPolls();
                        updatePollDetailsView();
                        return;
                    }
                    
                    // Now try to cast the vote
                    Vote vote = new Vote(pollId, userId, candidateId);
                    boolean success = voteService.castVote(vote);
                    
                    if (success) {
                        System.out.println("VOTE SUCCESS: Vote was recorded in database");
                        
                        // Update our list of voted polls
                        if (!userVotedPolls.contains(pollId)) {
                            userVotedPolls.add(pollId);
                        }
                        
                        // Refresh poll data from database
                        selectedPoll = ((DatabasePollService)pollService).getPollById(pollId);
                        
                        // Show success message
                        showNotification("Your vote has been cast successfully!");
                        
                        // Update UI
                        refreshPollData();
                        updatePollDetailsView();
                    } else {
                        // Check if vote was actually recorded despite 'failure' return
                        boolean checkAfterAttempt = voteService.hasUserVoted(userId, pollId);
                        System.out.println("Post-vote database check - Has user voted now: " + checkAfterAttempt);
                        
                        if (checkAfterAttempt) {
                            // Vote was recorded, but return value was false
                            System.out.println("VOTE ANOMALY: Vote appears to be recorded despite failure return");
                            if (!userVotedPolls.contains(pollId)) {
                                userVotedPolls.add(pollId);
                            }
                            showNotification("Your vote may have been recorded.", true);
                            updatePollDetailsView();
                        } else {
                            // Genuinely failed to vote
                            System.out.println("VOTE FAILURE: Vote failed - not recorded in database");
                            showNotification("Failed to cast your vote. Please try again later.", true);
                        }
                    }
                    
                    System.out.println("==== END VOTE ATTEMPT ====\n");
                } catch (Exception e) {
                    System.err.println("Error during vote casting: " + e.getMessage());
                    e.printStackTrace();
                    showNotification("Error: " + e.getMessage(), true);
                }
            }
        }
        
        /**
         * Create a box showing candidate results
         */
        private VBox createCandidateResultBox(String candidateName, int votes, double percentage, String color) {
            VBox box = new VBox(5);
            HBox topRow = new HBox(10);
            topRow.setAlignment(Pos.CENTER_LEFT);
            Label nameLabel = new Label(candidateName);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            Label percentLabel = new Label(String.format("%.1f%%", percentage));
            percentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            topRow.getChildren().addAll(nameLabel, spacer, percentLabel);
            
            // Create progress bar
            ProgressBar progressBar = new ProgressBar(percentage / 100);
            progressBar.setPrefWidth(Double.MAX_VALUE);
            progressBar.setPrefHeight(15);
            progressBar.setStyle("-fx-accent: " + color + ";");
            Label votesLabel = new Label(votes + (votes == 1 ? " vote" : " votes"));
            votesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #78909C;");
            box.getChildren().addAll(topRow, progressBar, votesLabel);
            return box;
        }
        
        /**
         * Share poll results (simulated functionality)
         */
        private void shareResults(Poll poll) {
            Alert shareAlert = new Alert(Alert.AlertType.INFORMATION);
            shareAlert.setTitle("Share Results");
            shareAlert.setHeaderText("Share Poll Results");
            
            // Calculate results for the message
            int totalVotes = poll.getTotalVotes();
            int candidate1Votes = voteService.getVoteCountForCandidate(poll.getId(), 1);
            int candidate2Votes = voteService.getVoteCountForCandidate(poll.getId(), 2);
            String winnerName;
            
            if (candidate1Votes > candidate2Votes) {
                winnerName = poll.getCandidate1();
            } else if (candidate2Votes > candidate1Votes) {
                winnerName = poll.getCandidate2();
            } else {
                winnerName = "No winner (tie)";
            }
            
            StringBuilder message = new StringBuilder();
            message.append("Poll: ").append(poll.getName()).append("\n\n");
            message.append("Results:\n");
            message.append(poll.getCandidate1()).append(": ").append(candidate1Votes).append(" votes\n");
            message.append(poll.getCandidate2()).append(": ").append(candidate2Votes).append(" votes\n\n");
            message.append("Total Votes: ").append(totalVotes).append("\n");
            message.append("Winner: ").append(winnerName).append("\n\n");
            message.append("This result has been copied to clipboard and can be shared.");
            
            shareAlert.setContentText(message.toString());
            
            // Simulate copying to clipboard
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(message.toString());
            clipboard.setContent(content);
            
            shareAlert.showAndWait();
        }
        
        /**
         * Setup poll table columns
         */
        private void setupPollTableColumns() {
            // Poll name column
            TableColumn<Poll, String> nameCol = new TableColumn<>("Poll Name");
            nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            nameCol.setPrefWidth(300);
            
            // Candidates column (combined to save space)
            TableColumn<Poll, String> candidatesCol = new TableColumn<>("Candidates");
            candidatesCol.setCellValueFactory(cellData -> {
                String candidates = cellData.getValue().getCandidate1() + " vs. " +
                    cellData.getValue().getCandidate2();
                return new javafx.beans.property.SimpleStringProperty(candidates);
            });
            candidatesCol.setPrefWidth(250);
            
            // Total votes column
            TableColumn<Poll, Number> votesCol = new TableColumn<>("Votes");
            votesCol.setCellValueFactory(cellData -> cellData.getValue().totalVotesProperty());
            votesCol.setPrefWidth(80);
            votesCol.setStyle("-fx-alignment: CENTER-RIGHT;");
            
            // Status column with custom styling
            TableColumn<Poll, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            statusCol.setPrefWidth(100);
            
            // Set cell factory for status column to add colors
            statusCol.setCellFactory(column -> {
                return new TableCell<Poll, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            if ("Active".equals(item)) {
                                setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            } else if ("Completed".equals(item)) {
                                setStyle("-fx-text-fill: #FF5252; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #78909C; -fx-font-weight: bold;");
                            }
                        }
                    }
                };
            });
            
            // Vote status column - shows if the user has voted in this poll
            TableColumn<Poll, String> voteStatusCol = new TableColumn<>("Your Vote");
            voteStatusCol.setCellValueFactory(cellData -> {
                // Always get fresh vote status directly from database for reliability
                boolean hasVoted = voteService.hasUserVoted(userId, cellData.getValue().getId());
                
                // Update userVotedPolls list if needed
                if (hasVoted && !userVotedPolls.contains(cellData.getValue().getId())) {
                    userVotedPolls.add(cellData.getValue().getId());
                }
                
                return new javafx.beans.property.SimpleStringProperty(
                    hasVoted ? "Voted ✓" : "Not Voted");
            });
            voteStatusCol.setPrefWidth(100);
            
            // Set cell factory for vote status column to add colors
            voteStatusCol.setCellFactory(column -> {
                return new TableCell<Poll, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            if (item.startsWith("Voted")) {
                                setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #78909C;");
                            }
                        }
                    }
                };
            });
            
            // Add "Action" column with "Vote" and "Stats" buttons
            TableColumn<Poll, Void> actionCol = new TableColumn<>("Action");
            actionCol.setPrefWidth(180); // Increased width to accommodate both buttons
            actionCol.setCellFactory(col -> {
                return new TableCell<Poll, Void>() {
                    private final Button voteBtn = new Button("Vote Now");
                    private final Button statsBtn = new Button("Stats");
                    private final HBox buttonBox = new HBox(5);
                    
                    {
                        // Set up vote button
                        voteBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");
                        voteBtn.setPrefHeight(30);
                        voteBtn.setOnAction(event -> {
                            Poll poll = getTableView().getItems().get(getIndex());
                            showPollDetailsView(poll);
                        });
                        
                        // Set up stats button
                        statsBtn.setStyle("-fx-background-color: #78909C; -fx-text-fill: white; -fx-font-weight: bold;");
                        statsBtn.setPrefHeight(30);
                        statsBtn.setOnAction(event -> {
                            Poll poll = getTableView().getItems().get(getIndex());
                            showPollStatisticsPopup(poll);
                        });
                        
                        // Add buttons to container with spacing
                        buttonBox.setAlignment(Pos.CENTER);
                        buttonBox.getChildren().addAll(voteBtn, statsBtn);
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Poll poll = getTableView().getItems().get(getIndex());
                            
                            // Always get fresh vote status directly from database
                            boolean hasVoted = voteService.hasUserVoted(userId, poll.getId());
                            
                            // Update the userVotedPolls list if needed
                            if (hasVoted && !userVotedPolls.contains(poll.getId())) {
                                userVotedPolls.add(poll.getId());
                            }
                            
                            boolean isActive = "Active".equals(poll.getStatus());
                            
                            // Update vote button state
                            if (hasVoted || !isActive) {
                                voteBtn.setDisable(true);
                                voteBtn.setText(hasVoted ? "Voted" : "Closed");
                                if (hasVoted) {
                                    voteBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                                } else {
                                    voteBtn.setStyle("-fx-background-color: #78909C; -fx-text-fill: white; -fx-font-weight: bold;");
                                }
                            } else {
                                voteBtn.setDisable(false);
                                voteBtn.setText("Vote");
                                voteBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");
                            }
                            
                            setGraphic(buttonBox);
                        }
                    }
                };
            });
            
            pollTableView.getColumns().addAll(nameCol, candidatesCol, votesCol, statusCol, voteStatusCol, actionCol);
        }
        
        /**
         * Create a stats card with a value and label
         */
        private VBox createStatsCard(String label, String value, String emoji, String color) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(20));
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 8px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 0 0 0 5px;" +
                "-fx-border-color: " + color + ";"
            );
            
            Label iconLabel = new Label(emoji);
            iconLabel.setStyle("-fx-font-size: 20px;");
            
            Label valueLabel = new Label(value);
            valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            
            Label titleLabel = new Label(label);
            titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #78909C;");
            
            card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
            return card;
        }
        
        /**
         * Create an info label
         */
        private Label createInfoLabel(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-weight: bold;");
            return label;
        }
        
        /**
         * Create a status label with appropriate color
         */
        private Label createStatusLabel(String status) {
            Label label = new Label(status);
            if ("Active".equals(status)) {
                label.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else if ("Completed".equals(status)) {
                label.setStyle("-fx-text-fill: #FF5252; -fx-font-weight: bold;");
            } else {
                label.setStyle("-fx-text-fill: #78909C; -fx-font-weight: bold;");
            }
            return label;
        }
        
        /**
         * Create a styled card container
         */
        private VBox createCard() {
            VBox card = new VBox();
            card.setPadding(new Insets(20));
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 8px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-color: #E0E0E0;" +
                "-fx-border-width: 1px;"
            );
            return card;
        }
        
        /**
         * Create a styled button
         */
        private Button createButton(String text) {
            Button button = new Button(text);
            button.setPrefHeight(38);
            button.setPadding(new Insets(0, 20, 0, 20));
            button.setStyle(
                "-fx-background-radius: 4px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
            );
            return button;
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
                        // Stop periodic updates
                        stopPeriodicUpdates();
                        
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
            
            // Add an appropriate emoji to the dialog pane
            String emoji = isError ? "❌ " : "✅ ";
            Label label = new Label(emoji);
            label.setStyle("-fx-font-size: 40px;");
            label.setPrefWidth(80);
            label.setAlignment(Pos.CENTER);
            
            // Create a new layout with the emoji and the default content
            BorderPane contentPane = new BorderPane();
            contentPane.setLeft(label);
            contentPane.setCenter(alert.getDialogPane().getContent());
            alert.getDialogPane().setContent(contentPane);
            
            alert.show();
        }
    }
                