package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Base64;


public class EmailSender {
    private static final String APPLICATION_NAME = "Twitch Stream Notifier";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String USER_EMAIL = "m.patrik01@gmail.com";
    private static Gmail gmailService;

    static {
        try {
            Credential credential = GoogleCredential.fromStream(new FileInputStream("resources/credentials.json"))
                    .createScoped(GmailScopes.all());
            gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendEmail(String to, String subject, String body) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress(USER_EMAIL));
            email.addRecipient(jakarta.mail.Message.RecipientType.CC, new InternetAddress(USER_EMAIL));
            email.setSubject(subject);
            email.setContent(body, "text/html; charset=utf-8");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

            Message message = new Message();
            message.setRaw(encodedEmail);
            gmailService.users().messages().send("me", message).execute();

            System.out.println("📧 Email sent to " + to);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
