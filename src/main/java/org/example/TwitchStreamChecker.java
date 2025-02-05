package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TwitchStreamChecker extends JFrame {

    private JTextField channelNameField;
    private JButton checkButton;
    private JLabel resultLabel;

    public TwitchStreamChecker() {
        setTitle("Twitch Stream Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLayout(new FlowLayout());

        channelNameField = new JTextField(20);
        checkButton = new JButton("Check");
        resultLabel = new JLabel("");

        add(new JLabel("Channel Name:"));
        add(channelNameField);
        add(checkButton);
        add(resultLabel);

        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String channelName = channelNameField.getText();
                checkStreamStatus(channelName);
            }
        });

        setVisible(true);
    }

    private void checkStreamStatus(String channelName) {
        try {
            String url = "https://www.twitch.tv/" + channelName;
            Document doc = Jsoup.connect(url).get();
            boolean isLive = doc.html().contains("isLiveBroadcast");

            if (isLive) {
                resultLabel.setText("Stream is live!");
                openStreamInChrome(url);
            } else {
                resultLabel.setText("Stream is offline.");
            }
        } catch (Exception ex) {
            resultLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openStreamInChrome(String url) {
        System.setProperty("web-driver.chrome.driver", "path/to/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get(url);
    }

    public static void main(String[] args) {
        new TwitchStreamChecker();
    }
}