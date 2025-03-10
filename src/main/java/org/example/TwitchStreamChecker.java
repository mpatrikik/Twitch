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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class TwitchStreamChecker extends Application {

    private TextField channelNameField;
    private Label resultLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Twitch Stream Checker");

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/twitch_icon.png")));
        primaryStage.getIcons().add(icon);

        Label channelNameLabel = new Label("Please enter channel name:");
        channelNameField = new TextField();
        Button checkButton = new Button("Check");
        resultLabel = new Label();

        checkButton.setOnAction(e -> checkStreamStatus(channelNameField.getText()));
        channelNameField.setOnAction(e -> checkStreamStatus(channelNameField.getText()));

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(120));

        layout.setBackground(new Background(new BackgroundFill(Color.rgb(100, 65, 165), CornerRadii.EMPTY, Insets.EMPTY)));
        channelNameLabel.setTextFill(Color.WHITE);
        resultLabel.setTextFill(Color.WHITE);
        checkButton.setStyle("-fx-background-color: #9146FF; -fx-text-fill: white;");

        layout.getChildren().addAll(channelNameLabel, channelNameField, checkButton, resultLabel);

        Scene scene = new Scene(layout, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            layout.setPrefWidth(newVal.doubleValue());
        });
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            layout.setPrefHeight(newVal.doubleValue());
        });
    }

    private void checkStreamStatus(String channelName) {
        try {
            String url = "https://www.twitch.tv/" + channelName.toLowerCase();
            Document doc = Jsoup.connect(url).get();
            boolean isLive = doc.html().contains("isLiveBroadcast");

            if (isLive) {
                resultLabel.setText("Channel is streaming!");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Streaming, want to open?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        openStreamInBrowser(url);
                    }
                });
            } else {
                resultLabel.setText("The stream offline.");
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