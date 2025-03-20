package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;

public class VotingDashboard extends Application {

    private BorderPane root;
    private Map<String, Poll> polls = new HashMap<>();
    private String currentPoll;
    private ObservableList<Voter> votersData = FXCollections.observableArrayList();

    // Color Scheme
    private final Color PRIMARY_COLOR = Color.web("#2A2A72");
    private final Color SECONDARY_COLOR = Color.web("#009FFD");
    private final Color BACKGROUND_COLOR = Color.web("#F5F6FA");

    @Override
    public void start(Stage primaryStage) {
        initializeSamplePolls();
        
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + toHex(BACKGROUND_COLOR) + ";");
        
        // Top Navigation
        HBox topBar = createTopBar();
        root.setTop(topBar);
        
        // Left Side Menu
        VBox leftMenu = createLeftMenu();
        root.setLeft(leftMenu);
        
        // Initial Dashboard View
        showDashboard();
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Polling Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopBar() {
        Label title = new Label("Polling System");
        title.setFont(Font.font("Arial", 20));
        title.setTextFill(PRIMARY_COLOR);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: " + toHex(PRIMARY_COLOR) + "; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> System.exit(0));

        HBox topBar = new HBox(20, title, logoutBtn);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.ALWAYS);
        return topBar;
    }

    private VBox createLeftMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(20));
        menu.setStyle("-fx-background-color: white; -fx-border-color: #eeeeee;");
        menu.setPrefWidth(200);

        Button dashboardBtn = createMenuButton("Dashboard");
        dashboardBtn.setOnAction(e -> showDashboard());

        Button aboutBtn = createMenuButton("About Us");
        aboutBtn.setOnAction(e -> showAboutUs());

        menu.getChildren().addAll(dashboardBtn, aboutBtn);
        return menu;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-size: 14;");
        btn.setPrefWidth(160);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f5f5f5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent;"));
        return btn;
    }

    private void showDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));

        Label title = new Label("Available Polls");
        title.setFont(Font.font("Arial", 18));
        title.setTextFill(PRIMARY_COLOR);

        FlowPane pollsContainer = new FlowPane();
        pollsContainer.setHgap(20);
        pollsContainer.setVgap(20);

        polls.forEach((name, poll) -> {
            VBox pollCard = createPollCard(poll);
            pollsContainer.getChildren().add(pollCard);
        });

        dashboard.getChildren().addAll(title, pollsContainer);
        root.setCenter(dashboard);
    }

    private VBox createPollCard(Poll poll) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefSize(300, 200);

        Label title = new Label(poll.getName());
        title.setFont(Font.font("Arial", 16));
        title.setTextFill(PRIMARY_COLOR);

        Label description = new Label(poll.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #666666;");

        Button viewBtn = new Button("View Results");
        viewBtn.setStyle("-fx-background-color: " + toHex(SECONDARY_COLOR) + "; -fx-text-fill: white;");
        viewBtn.setOnAction(e -> showPollStatistics(poll.getName()));

        card.getChildren().addAll(title, description, viewBtn);
        return card;
    }

    private void showPollStatistics(String pollName) {
        currentPoll = pollName;
        Poll poll = polls.get(pollName);

        VBox statsView = new VBox(20);
        statsView.setPadding(new Insets(20));

        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + toHex(PRIMARY_COLOR) + ";");
        backBtn.setOnAction(e -> showDashboard());

        Label title = new Label(poll.getName());
        title.setFont(Font.font("Arial", 20));
        title.setTextFill(PRIMARY_COLOR);

        if (poll.hasVoted("test@example.com")) { // Replace with actual user check
            statsView.getChildren().addAll(backBtn, title, createResultsView(poll));
        } else {
            statsView.getChildren().addAll(backBtn, title, createVotingForm(poll));
        }

        root.setCenter(statsView);
    }

    private VBox createVotingForm(Poll poll) {
        VBox form = new VBox(15);
        form.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8;");

        Label formTitle = new Label("Cast Your Vote");
        formTitle.setFont(Font.font("Arial", 16));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Other");
        genderCombo.setPromptText("Select Gender");

        ToggleGroup optionsGroup = new ToggleGroup();
        VBox optionsBox = new VBox(10);
        for (String option : poll.getOptions()) {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(optionsGroup);
            optionsBox.getChildren().add(rb);
        }

        Button submitBtn = new Button("Submit Vote");
        submitBtn.setStyle("-fx-background-color: " + toHex(SECONDARY_COLOR) + "; -fx-text-fill: white;");
        
        Label statusLabel = new Label();

        submitBtn.setOnAction(e -> {
            if (validateVoteForm(nameField, emailField, phoneField, genderCombo, optionsGroup, statusLabel)) {
                Voter voter = new Voter(
                    nameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    genderCombo.getValue(),
                    ((RadioButton)optionsGroup.getSelectedToggle()).getText()
                );
                
                if (poll.addVote(voter)) {
                    votersData.add(voter);
                    showPollStatistics(currentPoll);
                } else {
                    statusLabel.setText("This email/phone has already voted!");
                    statusLabel.setTextFill(Color.RED);
                }
            }
        });

        form.getChildren().addAll(formTitle, nameField, emailField, phoneField, 
                                genderCombo, optionsBox, submitBtn, statusLabel);
        return form;
    }

    private boolean validateVoteForm(TextField name, TextField email, TextField phone, 
                                    ComboBox<String> gender, ToggleGroup options, Label status) {
        if (name.getText().isEmpty() || email.getText().isEmpty() || 
            phone.getText().isEmpty() || gender.getValue() == null || 
            options.getSelectedToggle() == null) {
            
            status.setText("Please fill all fields!");
            status.setTextFill(Color.RED);
            return false;
        }
        return true;
    }

    private VBox createResultsView(Poll poll) {
        VBox results = new VBox(20);
        results.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8;");

        Label winnerLabel = new Label("Winner: " + poll.getWinner());
        winnerLabel.setStyle("-fx-font-size: 18; -fx-text-fill: " + toHex(PRIMARY_COLOR) + ";");

        BarChart<String, Number> chart = createChart(poll);

        Label votersTitle = new Label("Voter Details");
        votersTitle.setFont(Font.font("Arial", 14));

        TableView<Voter> voterTable = new TableView<>();
        voterTable.setItems(votersData);

        TableColumn<Voter, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Voter, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Voter, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Voter, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<Voter, String> voteCol = new TableColumn<>("Vote");
        voteCol.setCellValueFactory(new PropertyValueFactory<>("votedOption"));

        voterTable.getColumns().addAll(nameCol, emailCol, phoneCol, genderCol, voteCol);
        voterTable.setPrefHeight(200);

        results.getChildren().addAll(winnerLabel, chart, votersTitle, voterTable);
        return results;
    }

    private BarChart<String, Number> createChart(Poll poll) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        poll.getResults().forEach((option, votes) -> 
            series.getData().add(new XYChart.Data<>(option, votes))
        );
        
        chart.getData().add(series);
        chart.setLegendVisible(false);
        chart.setPrefSize(800, 500);
        return chart;
    }

    private void showAboutUs() {
        VBox aboutView = new VBox(20);
        aboutView.setPadding(new Insets(20));

        Label title = new Label("About Us");
        title.setFont(Font.font("Arial", 20));
        title.setTextFill(PRIMARY_COLOR);

        Label content = new Label("We are dedicated to providing transparent and secure polling solutions.\n\n" +
                "Our system ensures:\n- 100% Anonymous Voting\n- Real-time Results\n- Secure Transactions\n- User-friendly Interface");
        content.setStyle("-fx-text-fill: #666666; -fx-font-size: 14;");
        content.setLineSpacing(5);

        aboutView.getChildren().addAll(title, content);
        root.setCenter(aboutView);
    }

    private void initializeSamplePolls() {
        List<String> options = Arrays.asList("Security", "Performance", "Design", "Features");
        Poll featurePoll = new Poll("Favorite Feature", 
            "Which feature do you value most in our application?", options);
        polls.put(featurePoll.getName(), featurePoll);
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static void main(String[] args) {
        launch(args);
    }

    class Poll {
        private String name;
        private String description;
        private Map<String, Integer> results = new HashMap<>();
        private List<Voter> voters = new ArrayList<>();
        private Set<String> usedEmails = new HashSet<>();
        private Set<String> usedPhones = new HashSet<>();

        public Poll(String name, String description, List<String> options) {
            this.name = name;
            this.description = description;
            for (String option : options) {
                results.put(option, 0);
            }
        }

        public boolean addVote(Voter voter) {
            if (usedEmails.contains(voter.getEmail()) return false;
            if (usedPhones.contains(voter.getPhone())) return false;

            voters.add(voter);
            usedEmails.add(voter.getEmail());
            usedPhones.add(voter.getPhone());
            results.put(voter.getVotedOption(), results.get(voter.getVotedOption()) + 1);
            return true;
        }

        public String getWinner() {
            return Collections.max(results.entrySet(), Map.Entry.comparingByValue()).getKey();
        }

        public List<String> getOptions() {
            return new ArrayList<>(results.keySet());
        }

        public boolean hasVoted(String email) {
            return usedEmails.contains(email);
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public Map<String, Integer> getResults() { return results; }
    }

    public static class Voter {
        private String fullName;
        private String email;
        private String phone;
        private String gender;
        private String votedOption;

        public Voter(String fullName, String email, String phone, String gender, String votedOption) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.gender = gender;
            this.votedOption = votedOption;
        }

        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getGender() { return gender; }
        public String getVotedOption() { return votedOption; }
    }
}