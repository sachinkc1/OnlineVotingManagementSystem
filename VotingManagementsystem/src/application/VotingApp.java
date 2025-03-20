package application;

import javafx.animation.FadeTransition;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class VotingApp extends Application {

    private User currentUser;
    private Map<String, Integer> pollResults;
    private ObservableList<XYChart.Data<String, Number>> chartData;
    private BarChart<String, Number> barChart;
    private ComboBox<String> pollsComboBox;
    private Map<String, Boolean> userVotes;

    // Color palette
    private final Color PRIMARY_COLOR = Color.web("#2A2A72");
    private final Color SECONDARY_COLOR = Color.web("#009FFD");
    private final Color BACKGROUND_COLOR = Color.web("#F5F6FA");
    private final Color CARD_COLOR = Color.WHITE;

    @Override
    public void start(Stage primaryStage) {
        currentUser = new User("John Doe", "john@example.com", "555-1234", "Male");
        initializePollData();
        userVotes = new HashMap<>();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + toHex(BACKGROUND_COLOR) + ";");

        // Header Section
        HBox header = createHeader();
        root.setTop(header);

        // Main Content
        GridPane mainContent = new GridPane();
        mainContent.setHgap(20);
        mainContent.setVgap(20);
        mainContent.setPadding(new Insets(20));

        // Left Column
        VBox userSection = createUserSection();
        mainContent.add(userSection, 0, 0);

        // Center Column
        VBox chartSection = createChartSection();
        mainContent.add(chartSection, 1, 0);

        // Right Column
        VBox votingSection = createVotingSection();
        mainContent.add(votingSection, 2, 0);

        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Modern Voting System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        Label title = new Label("Voting System");
        title.setFont(Font.font("Roboto", 28));
        title.setTextFill(PRIMARY_COLOR);

        HBox header = new HBox(title);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox createUserSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: " + toHex(CARD_COLOR) + ";"
                + "-fx-background-radius: 8;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        section.setPrefWidth(280);

        Label sectionTitle = new Label("User Profile");
        sectionTitle.setFont(Font.font("Roboto Medium", 18));
        sectionTitle.setTextFill(PRIMARY_COLOR);

        GridPane userInfo = new GridPane();
        userInfo.setVgap(10);
        userInfo.setHgap(10);
        
        addUserInfoRow(userInfo, "Name:", currentUser.getFullName(), 0);
        addUserInfoRow(userInfo, "Email:", currentUser.getEmail(), 1);
        addUserInfoRow(userInfo, "Phone:", currentUser.getPhoneNumber(), 2);
        addUserInfoRow(userInfo, "Gender:", currentUser.getGender(), 3);

        Label votingHistoryTitle = new Label("Voting History");
        votingHistoryTitle.setFont(Font.font("Roboto Medium", 14));
        votingHistoryTitle.setTextFill(Color.web("#666666"));

        ListView<String> historyList = new ListView<>();
        historyList.setItems(FXCollections.observableArrayList("No votes yet"));
        historyList.setStyle("-fx-control-inner-background: " + toHex(CARD_COLOR) + ";");
        historyList.setPrefHeight(200);

        section.getChildren().addAll(sectionTitle, userInfo, votingHistoryTitle, historyList);
        return section;
    }

    private void addUserInfoRow(GridPane grid, String label, String value, int row) {
        Label key = new Label(label);
        key.setTextFill(Color.web("#666666"));
        key.setFont(Font.font("Roboto", 14));

        Label val = new Label(value);
        val.setTextFill(Color.web("#333333"));
        val.setFont(Font.font("Roboto Medium", 14));

        grid.addRow(row, key, val);
    }

    private VBox createChartSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: " + toHex(CARD_COLOR) + ";"
                + "-fx-background-radius: 8;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        section.setPrefWidth(500);

        Label sectionTitle = new Label("Live Results");
        sectionTitle.setFont(Font.font("Roboto Medium", 18));
        sectionTitle.setTextFill(PRIMARY_COLOR);

        // Chart setup
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setTickLabelFill(Color.web("#666666"));
        xAxis.setTickLabelFont(Font.font("Roboto", 12));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelFill(Color.web("#666666"));
        yAxis.setTickLabelFont(Font.font("Roboto", 12));

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setStyle("-fx-background-color: transparent;");
        barChart.setCategoryGap(20);
        barChart.setPrefSize(450, 400);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        chartData = FXCollections.observableArrayList();
        pollResults.forEach((k, v) -> chartData.add(new XYChart.Data<>(k, v)));
        series.setData(chartData);
        barChart.getData().add(series);

        // Bar styling
        for (XYChart.Data<String, Number> item : chartData) {
            item.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + toHex(SECONDARY_COLOR) + ";");
                    newNode.setOpacity(0.8);
                }
            });
        }

        section.getChildren().addAll(sectionTitle, barChart);
        return section;
    }

    private VBox createVotingSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: " + toHex(CARD_COLOR) + ";"
                + "-fx-background-radius: 8;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        section.setPrefWidth(300);

        Label sectionTitle = new Label("Cast Your Vote");
        sectionTitle.setFont(Font.font("Roboto Medium", 18));
        sectionTitle.setTextFill(PRIMARY_COLOR);

        Label question = new Label("What's your favorite feature?");
        question.setWrapText(true);
        question.setTextFill(Color.web("#333333"));
        question.setFont(Font.font("Roboto", 14));

        VBox optionsBox = new VBox(10);
        ToggleGroup group = new ToggleGroup();

        pollResults.keySet().forEach(option -> {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(group);
            rb.setStyle("-fx-text-fill: #333333; -fx-font-family: Roboto;");
            rb.setPadding(new Insets(8));
            optionsBox.getChildren().add(rb);
        });

        Button submitBtn = new Button("Submit Vote");
        submitBtn.setStyle("-fx-background-color: " + toHex(PRIMARY_COLOR) + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-family: Roboto Medium;"
                + "-fx-font-size: 14;"
                + "-fx-background-radius: 4;"
                + "-fx-padding: 10 20;");
        
        // Button hover effect
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle(
                "-fx-background-color: " + toHex(PRIMARY_COLOR.darker()) + ";"
                + "-fx-text-fill: white;"
                + "-fx-cursor: hand;"));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle(
                "-fx-background-color: " + toHex(PRIMARY_COLOR) + ";"
                + "-fx-text-fill: white;"));

        Label statusLabel = new Label();
        statusLabel.setTextFill(SECONDARY_COLOR);
        statusLabel.setFont(Font.font("Roboto", 12));

        submitBtn.setOnAction(e -> handleVoteSubmission(group, statusLabel));

        section.getChildren().addAll(sectionTitle, question, optionsBox, submitBtn, statusLabel);
        return section;
    }

    private void handleVoteSubmission(ToggleGroup group, Label statusLabel) {
        RadioButton selected = (RadioButton) group.getSelectedToggle();
        if (selected == null) {
            showStatusMessage(statusLabel, "Please select an option first!", Color.RED);
            return;
        }

        if (userVotes.getOrDefault("Current Poll", false)) {
            showStatusMessage(statusLabel, "You already voted in this poll!", Color.ORANGE);
            return;
        }

        String option = selected.getText();
        pollResults.put(option, pollResults.get(option) + 1);
        chartData.forEach(data -> {
            if (data.getXValue().equals(option)) {
                data.setYValue(data.getYValue().intValue() + 1);
            }
        });

        userVotes.put("Current Poll", true);
        showStatusMessage(statusLabel, "✓ Vote submitted successfully!", SECONDARY_COLOR);
    }

    private void showStatusMessage(Label label, String text, Color color) {
        label.setText(text);
        label.setTextFill(color);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), label);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void initializePollData() {
        pollResults = new HashMap<>();
        pollResults.put("Security", 45);
        pollResults.put("Performance", 32);
        pollResults.put("Design", 28);
        pollResults.put("Features", 15);
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

    private static class User {
        private final String fullName, email, phoneNumber, gender;

        public User(String fullName, String email, String phoneNumber, String gender) {
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.gender = gender;
        }

        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getGender() { return gender; }
    }
}