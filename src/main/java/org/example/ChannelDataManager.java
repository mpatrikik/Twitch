package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChannelDataManager {

    private static final String FILE_PATH = "src/main/resources/channels.json";
    private static final Gson gson = new Gson();

    public static List<String> loadChannels() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveChannels(List<String> channels) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            gson.toJson(channels, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
