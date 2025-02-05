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
        setSize(500, 300);
        setLayout(new FlowLayout());
        setIconImage(new ImageIcon("src/main/resources/twitch_icon.png").getImage());
        getContentPane().setBackground(Color.decode("#5e5e5e"));

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2-getSize().width/2, dim.height/2-getSize().height/2);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1)); // Tetszőleges számú sor, egy oszlop,
        panel.setBackground(Color.decode("#5e5e5e"));
        panel.setForeground(Color.decode("#8400ff"));

        panel.add(new JLabel("Channel Name:"));
        channelNameField = new JTextField(15);
        panel.add(channelNameField);
        checkButton = new JButton("Check");
        panel.add(checkButton);
        panel.add(new JLabel("Enter a Twitch channel name to check..."));
        resultLabel = new JLabel("");
        resultLabel.setOpaque(true);
        resultLabel.setBackground(Color.decode("#5e5e5e"));
        panel.add(resultLabel);

        add(panel);

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
            String url = "https://www.twitch.tv/" + channelName.toLowerCase();
            Document doc = Jsoup.connect(url).get();
            boolean isLive = doc.html().contains("isLiveBroadcast");

            if (isLive) {
                resultLabel.setText("Stream is live!");

                Timer timer = new Timer(1500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        openStreamInChrome(url);
                    }
                });
                timer.setRepeats(false);
                timer.start();
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