package org.example.server.persistence;

import com.google.gson.*;
import org.example.server.model.User;
import org.example.server.model.Room;
import org.example.server.model.Message;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles persistence of server data to JSON files.
 * Manages loading and saving of users, rooms, and messages.
 */
public class PersistenceManager {
    /**
     * Base directory used for persistence. Defaults to the `data` folder but can be
     * overridden at runtime by setting the system property `persistence.dir` or by
     * calling {@link #setDataDir(String)} from tests.
     */
    private static String DATA_DIR = System.getProperty("persistence.dir", "data");
    private static final String USERS_FILE = "users.json";
    private static final String ROOMS_FILE = "rooms.json";
    private static final String MESSAGES_DIR = "messages";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Initializes the persistence system by creating necessary directories.
     */
    public static void initialize() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(DATA_DIR, MESSAGES_DIR));
            System.out.println("Persistence system initialized");
        } catch (IOException e) {
            System.err.println("Error initializing persistence: " + e.getMessage());
        }
    }

    /**
     * Allows tests to override the persistence directory programmatically so the
     * test-suite can remain isolated from developer data.
     *
     * Note: call this before any other PersistenceManager methods in tests.
     */
    public static void setDataDir(String dataDir) {
        DATA_DIR = dataDir;
    }

    /**
     * Loads all registered users from persistence storage.
     *
     * @return map of username to User objects
     */
    public static Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        Path filePath = Paths.get(DATA_DIR, USERS_FILE);

        if (!Files.exists(filePath)) {
            System.out.println("Users file not found, starting with empty user list");
            return users;
        }

        try {
            String content = new String(Files.readAllBytes(filePath));
            JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject json = element.getAsJsonObject();
                String username = json.get("username").getAsString();
                long key = json.get("key").getAsLong();
                User user = new User(username, key);

                if (json.has("registrationDate")) {
                    user.setRegistrationDate(
                            LocalDateTime.parse(json.get("registrationDate").getAsString()));
                }

                users.put(username, user);
            }

            System.out.println("Loaded " + users.size() + " users from storage");
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        return users;
    }

    /**
     * Saves all registered users to persistence storage.
     *
     * @param users map of username to User objects
     */
    public static void saveUsers(Map<String, User> users) {
        try {
            JsonArray jsonArray = new JsonArray();

            for (User user : users.values()) {
                JsonObject json = new JsonObject();
                json.addProperty("username", user.getUsername());
                json.addProperty("key", user.getKey());
                json.addProperty("registrationDate", user.getRegistrationDate().toString());
                jsonArray.add(json);
            }

            Path filePath = Paths.get(DATA_DIR, USERS_FILE);
            Files.write(filePath, gson.toJson(jsonArray).getBytes());
            System.out.println("Saved " + users.size() + " users to storage");
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    /**
     * Loads all rooms and their metadata from persistence storage.
     *
     * @return map of room name to Room objects
     */
    public static Map<String, Room> loadRooms() {
        Map<String, Room> rooms = new HashMap<>();
        Path filePath = Paths.get(DATA_DIR, ROOMS_FILE);

        if (!Files.exists(filePath)) {
            System.out.println("Rooms file not found, starting with empty room list");
            return rooms;
        }

        try {
            String content = new String(Files.readAllBytes(filePath));
            JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject json = element.getAsJsonObject();
                String roomName = json.get("name").getAsString();
                Room room = new Room(roomName);

                if (json.has("messageCount")) {
                    room.setMessageCount(json.get("messageCount").getAsLong());
                }

                rooms.put(roomName, room);
            }

            System.out.println("Loaded " + rooms.size() + " rooms from storage");
        } catch (IOException e) {
            System.err.println("Error loading rooms: " + e.getMessage());
        }

        return rooms;
    }

    /**
     * Saves all rooms and their metadata to persistence storage.
     *
     * @param rooms map of room name to Room objects
     */
    public static void saveRooms(Map<String, Room> rooms) {
        try {
            JsonArray jsonArray = new JsonArray();

            for (Room room : rooms.values()) {
                JsonObject json = new JsonObject();
                json.addProperty("name", room.getName());
                json.addProperty("messageCount", room.getMessageCount());
                jsonArray.add(json);
            }

            Path filePath = Paths.get(DATA_DIR, ROOMS_FILE);
            Files.write(filePath, gson.toJson(jsonArray).getBytes());
            System.out.println("Saved " + rooms.size() + " rooms to storage");
        } catch (IOException e) {
            System.err.println("Error saving rooms: " + e.getMessage());
        }
    }

    /**
     * Loads messages for a specific room.
     *
     * @param roomName the name of the room
     * @return list of messages for the room
     */
    public static List<Message> loadRoomMessages(String roomName) {
        List<Message> messages = new ArrayList<>();
        Path filePath = Paths.get(DATA_DIR, MESSAGES_DIR, roomName + ".json");

        if (!Files.exists(filePath)) {
            return messages;
        }

        try {
            String content = new String(Files.readAllBytes(filePath));
            JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject json = element.getAsJsonObject();
                String sender = json.get("sender").getAsString();
                String messageContent = json.get("content").getAsString();
                Message message = new Message(sender, messageContent, roomName, null);

                if (json.has("timestamp")) {
                    message.setTimestamp(
                            LocalDateTime.parse(json.get("timestamp").getAsString()));
                }

                messages.add(message);
            }

            System.out.println("Loaded " + messages.size() + " messages for room: " + roomName);
        } catch (IOException e) {
            System.err.println("Error loading messages for room " + roomName + ": " + e.getMessage());
        }

        return messages;
    }

    /**
     * Saves messages for a specific room.
     *
     * @param roomName the name of the room
     * @param messages list of messages to save
     */
    public static void saveRoomMessages(String roomName, List<Message> messages) {
        try {
            JsonArray jsonArray = new JsonArray();

            for (Message message : messages) {
                JsonObject json = new JsonObject();
                json.addProperty("sender", message.getSender());
                json.addProperty("content", message.getContent());
                json.addProperty("timestamp", message.getTimestamp().toString());
                jsonArray.add(json);
            }

            Path filePath = Paths.get(DATA_DIR, MESSAGES_DIR, roomName + ".json");
            Files.write(filePath, gson.toJson(jsonArray).getBytes());
        } catch (IOException e) {
            System.err.println("Error saving messages for room " + roomName + ": " + e.getMessage());
        }
    }

    /**
     * Gets the next available user key value.
     *
     * @return the next user key
     */
    public static long getNextUserKey() {
        Map<String, User> users = loadUsers();
        long maxKey = 100000;
        for (User user : users.values()) {
            if (user.getKey() > maxKey) {
                maxKey = user.getKey();
            }
        }
        return maxKey + 1;
    }
}

