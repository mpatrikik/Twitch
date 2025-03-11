package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChannelDataManager {

    private static final String FILE_PATH = "/channels.json";
    private static final Gson gson = new Gson();

    public static List<String> loadChannels() {
        try (InputStream inputStream =
                     ChannelDataManager.class.getResourceAsStream(FILE_PATH);
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void saveChannels(List<String> channels) {
        try (FileWriter writer = new FileWriter("scr/main/resources" + FILE_PATH)) {
            gson.toJson(channels, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
