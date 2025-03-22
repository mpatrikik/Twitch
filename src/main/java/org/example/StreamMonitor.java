package org.example;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class StreamMonitor extends Thread {
    private final String channelName;
    private boolean wasLive;
    private final Image alertIcon;
    private static final Set<String> activeMonitors = new HashSet<>();
    private boolean alertShown = false;

    public StreamMonitor(String channelName) {
        this.channelName = channelName;
        this.wasLive = true;
        this.alertIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/twitch_icon.png")));
    }

    @Override
    public void run() {
        synchronized (activeMonitors) {
            if (activeMonitors.contains(channelName)) {
                return;
            }
            activeMonitors.add(channelName);
        }

        try {
            while (true) {
                boolean isLive = TwitchAPIClient.isStreamLive(channelName);
                if (!isLive && wasLive) {
                    if (!alertShown) {
                        alertShown = true;
                        Platform.runLater(this::showStreamStoppedAlert);
                    }
                    break;
                }
                wasLive = isLive;
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            synchronized (activeMonitors) {
                activeMonitors.remove(channelName);
            }
        }
    }

    private void showStreamStoppedAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(channelName + "stopped streaming!");
        alert.setHeaderText(null);
        Label contentLabel = new Label("The stream has ended. Do you want to close Chrome?");
        contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        alert.getDialogPane().setContent(contentLabel);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(alertIcon);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #6441A5; -fx-text-fill: white; -fx-font-size: 14px;");

        ButtonType yesButton = new ButtonType("Close Chrome", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Keep Open", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                closeChrome();
            }
        });
    }

    private void closeChrome() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process = null;

            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
            }

            if (process != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
