package org.example;

import javafx.application.Platform;
import javafx.geometry.Pos;
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
        startBeeping(10);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(channelName + " stopped streaming!");
        alert.setHeaderText(null);
        Label contentLabel = new Label("The stream has ended!");
        contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        contentLabel.setAlignment(Pos.CENTER);
        alert.getDialogPane().setContent(contentLabel);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(alertIcon);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #6441A5; -fx-text-fill: white; -fx-font-size: 14px;");

        ButtonType shPCButton = new ButtonType("⏻  \uD83D\uDCBB", ButtonBar.ButtonData.YES);
        ButtonType clsChrome = new ButtonType("\uD83D\uDDD9 Chrome");
        ButtonType noButton = new ButtonType("Keep open everything", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(shPCButton, clsChrome, noButton);

        new Thread(() -> {
            try {
                Thread.sleep(10000); // 10 másodperc várakozás
                Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        sendStreamStoppedEmail(); // Ha az alert még mindig nyitva, küldjük az emailt
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        alert.showAndWait().ifPresent(response -> {
            if (response == shPCButton) {
                shPC();
            } else if (response == clsChrome) {
                clsChrome();
            }
        });
    }


    private void sendStreamStoppedEmail() {
        String emailBody = "<h3>The stream has ended!</h3>"
                + "<p>Choose what to do:</p>"
                + "<ul>"
                + "<li><a href='http://localhost:8080/action?closechrome'>Close Chrome</a></li>"
                + "<li><a href='http://localhost:8080/action?shutdown'>Close Chrome + IntelliJ + Shutdown PC</a></li>"
                + "<li><a href='http://localhost:8080/action?noop'>Do nothing</a></li>"
                + "</ul>";

        EmailSender.sendEmail("your-phone-email@gmail.com", "Stream Stopped!", emailBody);
    }


    private void startBeeping(int seconds) {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < seconds * 1000) {
                Toolkit.getDefaultToolkit().beep();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }


    public static void shPC() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process = null;

            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
                Runtime.getRuntime().exec("taskkill /F /IM idea64.exe");
                Runtime.getRuntime().exec("shutdown -s -t 0");
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


    public static void clsChrome() {
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
