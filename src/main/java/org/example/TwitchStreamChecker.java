package org.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Desktop;

public class TwitchStreamChecker extends Application {

    private TextField channelNameField;
    private Label resultLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Twitch Stream Checker");

        Label channelNameLabel = new Label("Channel Name:");
        channelNameField = new TextField();
        Button checkButton = new Button("Check");
        resultLabel = new Label();

        checkButton.setOnAction(e -> checkStreamStatus(channelNameField.getText()));
        channelNameField.setOnAction(e -> checkStreamStatus(channelNameField.getText()));

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(channelNameLabel, channelNameField, checkButton, resultLabel);

        Scene scene = new Scene(layout, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void checkStreamStatus(String channelName) {
        try {
            String url = "https://www.twitch.tv/" + channelName.toLowerCase();
            Document doc = Jsoup.connect(url).get();
            boolean isLive = doc.html().contains("isLiveBroadcast");

            if (isLive) {
                resultLabel.setText("Stream is live!");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "The stream is live! Do you want to open it?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        openStreamInBrowser(url);
                    }
                });
            } else {
                resultLabel.setText("Stream is offline.");
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open browser. Please open manually: " + url, ButtonType.OK);
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
