package org.example;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.*;
import java.util.*;

import java.util.Base64;

public class EmailSender {
    private static final String APPLICATION_NAME = "Twitch Stream Notifier";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String SERVICE_ACCOUNT_KEY_PATH = "C:\\egyebek\\prog\\Java\\Twitch\\src\\main\\resources\\service_account.json";
    private static final String ADMIN_EMAIL = "m.patrik01@gmail.com";

    private static Gmail gmailService;

    static {
        try {
            gmailService = createGmailService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Gmail createGmailService() throws IOException {
        try {
            FileInputStream serviceAccountStream = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH);
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream)
                    .createScoped(Collections.singleton(GmailScopes.GMAIL_SEND))
                    .createDelegated(ADMIN_EMAIL);

            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            return new Gmail.Builder(httpTransport, JSON_FACTORY, (HttpRequestInitializer) credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Hiba történt a Gmail szolgáltatás inicializálásakor", e);
        }
    }

    public static void sendEmail(String to, String subject, String body) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress(ADMIN_EMAIL));
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
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
