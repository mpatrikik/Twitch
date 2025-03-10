package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2-getSize().width/2, dim.height/2-getSize().height/2);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        JLabel channelNameLabel = new JLabel("Channel Name:");
        channelNameLabel.setForeground(Color.decode("#8400ff"));
        panel.add(channelNameLabel);

        channelNameField = new JTextField(15);
        panel.add(channelNameField);
        channelNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String channelName = channelNameField.getText();
                    checkStreamStatus(channelName);
                }
            }
        });
        checkButton = new JButton("Check");
        checkButton.setBackground(Color.decode("#e4c7ff"));
        checkButton.setForeground(Color.decode("#8400ff"));
        panel.add(checkButton);
        resultLabel = new JLabel("");
        resultLabel.setForeground(Color.decode("#8400ff"));
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
                int response = JOptionPane.showConfirmDialog(this, "Open stream in Chrome?", "Stream Online", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    openStreamInChrome(url);
                }
            } else {
                resultLabel.setText("Stream is offline.");
            }
        } catch (Exception ex) {
            resultLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openStreamInChrome(String url) {
        if (Desktop.isDesktopSupported()){
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showConfirmDialog(this, "Failed to open browser. Please open manually: " + url);
        }
    }

    public static void main(String[] args) {
        new TwitchStreamChecker();
    }
}