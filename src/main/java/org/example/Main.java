package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends Application {

    private TextField channelNameField;
    private Label resultLabel;
    private ComboBox<String> channelComboBox;
    private List<String> channels;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Twitch Stream Checker");

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/twitch_icon.png")));
        primaryStage.getIcons().add(icon);

        Label channelNameLabel = new Label("Please enter channel name:");
        channelNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        channelNameField = new TextField();
        channelComboBox = new ComboBox<>();
        Button checkButton = new Button("Check");
        resultLabel = new Label();

        channels = ChannelDataManager.loadChannels();
        if (channels == null) {
            channels = new ArrayList<>();
        }
        channelComboBox.getItems().addAll(channels);

        checkButton.setOnAction(e -> checkStreamStatus(channelNameField.getText()));
        channelNameField.setOnAction(e -> checkStreamStatus(channelNameField.getText()));
        channelComboBox.setOnAction(e -> {
                String selectedChannel = channelComboBox.getValue();
                if (selectedChannel != null) {
                    channelNameField.setText(selectedChannel);
                }
        });



        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));
        layout.setBackground(new Background(new BackgroundFill(Color.rgb(164, 119, 255), CornerRadii.EMPTY, Insets.EMPTY)));

        channelNameLabel.setTextFill(Color.WHITE);
        resultLabel.setTextFill(Color.WHITE);
        checkButton.setStyle("-fx-background-color: #6441a5 ; -fx-text-fill: white;");

        double commonWidth = 200;
        channelNameField.setPrefWidth(commonWidth);
        channelNameField.setMinWidth(commonWidth);
        channelNameField.setMaxWidth(commonWidth);

        channelComboBox.setPrefWidth(commonWidth);
        channelComboBox.setMinWidth(commonWidth);
        channelComboBox.setMaxWidth(commonWidth);

        checkButton.setPrefWidth(commonWidth);
        checkButton.setMinWidth(commonWidth);
        checkButton.setMaxWidth(commonWidth);

        VBox.setMargin(channelNameLabel, new Insets(0, 10, 0, 10));
        VBox.setMargin(channelNameField, new Insets(0, 50, 0, 50));
        VBox.setMargin(channelComboBox, new Insets(0, 10, 0, 10));
        VBox.setMargin(checkButton, new Insets(20, 0, 0, 0));
        VBox.setMargin(resultLabel, new Insets(10, 0, 0, 0));

        layout.getChildren().addAll(channelNameLabel, channelNameField, channelComboBox, checkButton, resultLabel);
        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(layout, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> { layout.setPrefWidth(newVal.doubleValue()); });
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> { layout.setPrefHeight(newVal.doubleValue()); });
    }

    public void checkStreamStatus(String channelName) {
        try {
            boolean isLive = TwitchAPIClient.isStreamLive(channelName);
            String url = "https://www.twitch.tv/" + channelName.toLowerCase();

            if (isLive) {
                resultLabel.setText("Channel is streaming :)");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Stream is live!");
                alert.setHeaderText(null);
                Label contentLabel = new Label("Streaming! Do you want to open in the browser?");
                contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                alert.getDialogPane().setContent(contentLabel);

                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/twitch_icon.png"))));

                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #6441A5; -fx-text-fill: white; -fx-font-size: 14px;");

                ButtonType yesButton = new ButtonType("Open Stream", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
                alert.getButtonTypes().setAll(yesButton, noButton);

                alert.showAndWait().ifPresent(response -> {
                    if (response == yesButton) {
                        openStreamInBrowser(url);
                        new StreamMonitor(channelName).start();
                    }
                });

                if (!channels.contains(channelName)) {
                    channels.add(channelName);
                    ChannelDataManager.saveChannels(channels);
                    channelComboBox.getItems().add(channelName);
                }
            } else {
                resultLabel.setText("Stream is offline :(");
            }
        } catch (Exception ex) {
            resultLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openStreamInBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open: " + url, ButtonType.OK);
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
