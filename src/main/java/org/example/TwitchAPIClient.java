package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TwitchAPIClient {
    private static final String CLIENT_ID = "z7hne9rwv09io02mh9b8sj33et9ly0";
    private static final String CLIENT_SECRET = "1osuv4rg1my553fg3qjlertbmezmhb";
    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
    public static final String STREAMS_URL = "https://api.twitch.tv/helix/streams?user_login=";
    private static String accessToken = "";

    private static void authenticate() {
        try {
            URL url = new URL(TOKEN_URL + "?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&grant_type=client_credentials");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            accessToken = json.get("access_token").getAsString();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isStreamLive(String channelName) {
        try {
            if (accessToken.isEmpty()) {
                authenticate();
            }
            URL url = new URL(STREAMS_URL + channelName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Client-ID", CLIENT_ID);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            return json.getAsJsonArray("data").size() > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
