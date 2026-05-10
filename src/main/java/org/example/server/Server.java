package org.example.server;

import org.example.model.Room;
import org.example.model.User;
import org.example.model.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main server class managing the messaging system.
 * Handles client connections, room management, and message routing.
 */
public class Server {
    private static final int DEFAULT_PORT = 5000;
    private static final int MAX_CLIENTS = 100;
    private static final long HEARTBEAT_TIMEOUT_MINUTES = 5;
    private static final long HEARTBEAT_CHECK_INTERVAL_MINUTES = 1;

    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private Map<String, User> registeredUsers;
    private Map<String, ClientHandler> connectedClients;
    private Map<String, Room> rooms;
    private volatile boolean acceptingClients;
    private volatile boolean maintenanceMode;
    private long nextUserKey;

    /**
     * Constructs a new Server instance.
     */
    public Server() {
        this.registeredUsers = Collections.synchronizedMap(new LinkedHashMap<>());
        this.connectedClients = Collections.synchronizedMap(new ConcurrentHashMap<>());
        this.rooms = Collections.synchronizedMap(new LinkedHashMap<>());
        this.clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        this.acceptingClients = true;
        this.maintenanceMode = false;
        this.nextUserKey = 100000;

        initializePredefinedRooms();
    }

    /**
     * Initializes the predefined rooms as specified in requirements.
     */
    private void initializePredefinedRooms() {
        String[] predefinedRoomNames = {"IA", "Deportes", "Therian", "Manga", "UEMC"};
        for (String roomName : predefinedRoomNames) {
            rooms.put(roomName, new Room(roomName));
        }
    }

    /**
     * Starts the server, listening for incoming client connections.
     *
     * @param port the port to listen on
     * @throws IOException if an I/O error occurs
     */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        System.out.println("Predefined rooms initialized: " + rooms.keySet());

        // Start heartbeat monitoring thread
        startHeartbeatMonitor();

        // Accept client connections
        while (true) {
            if (!acceptingClients) {
                System.out.println("Server is not accepting new clients");
                Thread.yield();
                continue;
            }

            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientThreadPool.execute(handler);
                System.out.println("New client connection accepted");
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Starts a thread that monitors client heartbeats and removes timed-out clients.
     */
    private void startHeartbeatMonitor() {
        Thread heartbeatThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(HEARTBEAT_CHECK_INTERVAL_MINUTES * 60 * 1000);
                    checkClientHeartbeats();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    /**
     * Checks all connected clients for heartbeat timeouts.
     */
    private synchronized void checkClientHeartbeats() {
        long now = System.currentTimeMillis();
        long timeoutMillis = HEARTBEAT_TIMEOUT_MINUTES * 60 * 1000;

        List<String> timedOutClients = new ArrayList<>();
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            ClientHandler handler = entry.getValue();
            if (now - handler.getLastHeartbeat() > timeoutMillis) {
                timedOutClients.add(entry.getKey());
            }
        }

        for (String username : timedOutClients) {
            System.out.println("Client timeout: " + username);
            disconnectClient(username);
        }
    }

    // ============ User Management ============

    /**
     * Registers a new user with a unique username.
     *
     * @param username the desired username
     * @return the auto-generated numeric key
     * @throws Exception if username already exists
     */
    public synchronized long registerUser(String username) throws Exception {
        if (registeredUsers.containsKey(username)) {
            throw new Exception("USERNAME_EXISTS");
        }

        long key = nextUserKey++;
        User user = new User(username, key);
        registeredUsers.put(username, user);

        return key;
    }

    /**
     * Authenticates a user with username and key.
     *
     * @param username the username
     * @param key the authentication key
     * @return the authenticated User
     * @throws Exception if credentials are invalid
     */
    public synchronized User loginUser(String username, long key) throws Exception {
        User user = registeredUsers.get(username);
        if (user == null || user.getKey() != key) {
            throw new Exception("INVALID_CREDENTIALS");
        }

        return user;
    }

    /**
     * Connects a user to the server.
     *
     * @param username the username
     * @param handler the client handler for this user
     */
    public synchronized void connectClient(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        User user = registeredUsers.get(username);
        if (user != null) {
            user.setConnected(true);
        }
        System.out.println("User connected: " + username);
    }

    /**
     * Disconnects a user from the server.
     *
     * @param username the username
     */
    public synchronized void disconnectClient(String username) {
        connectedClients.remove(username);
        User user = registeredUsers.get(username);
        if (user != null) {
            user.setConnected(false);
        }

        // Remove user from all rooms
        for (Room room : rooms.values()) {
            room.removeUser(username);
        }

        System.out.println("User disconnected: " + username);
    }

    // ============ Room Management ============

    /**
     * Gets all available rooms.
     *
     * @return a collection of all rooms
     */
    public Collection<Room> getRooms() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * Gets a specific room by name.
     *
     * @param roomName the name of the room
     * @return the Room, or null if not found
     */
    public Room getRoom(String roomName) {
        return rooms.get(roomName);
    }

    /**
     * Adds a user to a room.
     *
     * @param username the username
     * @param roomName the room name
     * @throws Exception if room not found
     */
    public synchronized void addUserToRoom(String username, String roomName) throws Exception {
        Room room = rooms.get(roomName);
        if (room == null) {
            throw new Exception("ROOM_NOT_FOUND");
        }

        room.addUser(username);
        System.out.println("User " + username + " added to room " + roomName);
    }

    /**
     * Removes a user from a room.
     *
     * @param username the username
     * @param roomName the room name
     */
    public synchronized void removeUserFromRoom(String username, String roomName) {
        Room room = rooms.get(roomName);
        if (room != null) {
            room.removeUser(username);
            System.out.println("User " + username + " removed from room " + roomName);
        }
    }

    // ============ Message Handling ============

    /**
     * Stores a message in a room.
     *
     * @param message the message to store
     * @throws Exception if room not found or message invalid
     */
    public void storeRoomMessage(Message message) throws Exception {
        if (!message.isValid()) {
            throw new Exception("MESSAGE_TOO_LONG");
        }

        Room room = getRoom(message.getRoom());
        if (room == null) {
            throw new Exception("ROOM_NOT_FOUND");
        }

        room.addMessage(message);
    }

    /**
     * Broadcasts a message to all users in a room.
     *
     * @param message the message to broadcast
     * @param senderUsername the username of the sender
     */
    public void broadcastRoomMessage(Message message, String senderUsername) {
        Room room = getRoom(message.getRoom());
        if (room == null) return;

        String messageJson = convertMessageToJson(message);
        for (String username : room.getConnectedUsers()) {
            if (!username.equals(senderUsername) || true) { // Send to all including sender for confirmation
                ClientHandler handler = connectedClients.get(username);
                if (handler != null) {
                    handler.sendMessage(messageJson);
                }
            }
        }
    }

    /**
     * Sends a private message from one user to another.
     *
     * @param message the private message
     * @throws Exception if receiver is not connected
     */
    public void sendPrivateMessage(Message message) throws Exception {
        ClientHandler receiverHandler = connectedClients.get(message.getReceiver());
        if (receiverHandler == null) {
            throw new Exception("USER_NOT_CONNECTED");
        }

        String messageJson = convertMessageToJson(message);
        receiverHandler.sendMessage(messageJson);
    }

    /**
     * Checks if a user is connected.
     *
     * @param username the username to check
     * @return true if user is connected, false otherwise
     */
    public boolean isUserConnected(String username) {
        return connectedClients.containsKey(username);
    }

    /**
     * Gets the client handler for a connected user.
     *
     * @param username the username
     * @return the ClientHandler, or null if not connected
     */
    public ClientHandler getClientHandler(String username) {
        return connectedClients.get(username);
    }

    // ============ Server Administration ============

    /**
     * Stops accepting new client connections.
     */
    public synchronized void stopAcceptingClients() {
        acceptingClients = false;
        System.out.println("Server stopped accepting new clients");
    }

    /**
     * Starts accepting new client connections.
     */
    public synchronized void startAcceptingClients() {
        acceptingClients = true;
        System.out.println("Server started accepting new clients");
    }

    /**
     * Enables maintenance mode (blocks message broadcasting).
     */
    public synchronized void enableMaintenanceMode() {
        maintenanceMode = true;
        System.out.println("Maintenance mode enabled");
    }

    /**
     * Disables maintenance mode.
     */
    public synchronized void disableMaintenanceMode() {
        maintenanceMode = false;
        System.out.println("Maintenance mode disabled");
    }

    /**
     * Checks if maintenance mode is enabled.
     *
     * @return true if in maintenance mode
     */
    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    /**
     * Gets statistics about the server.
     *
     * @return a map containing statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("registered_users", registeredUsers.size());
        stats.put("connected_users", connectedClients.size());
        stats.put("total_rooms", rooms.size());

        Map<String, Integer> roomStats = new LinkedHashMap<>();
        Map<String, Long> roomMessageCounts = new LinkedHashMap<>();

        for (Room room : rooms.values()) {
            roomStats.put(room.getName(), room.getUserCount());
            roomMessageCounts.put(room.getName(), room.getMessageCount());
        }

        stats.put("room_user_counts", roomStats);
        stats.put("room_message_counts", roomMessageCounts);

        return stats;
    }

    /**
     * Shuts down the server gracefully.
     */
    public void shutdown() {
        System.out.println("Shutting down server...");
        try {
            // Disconnect all clients
            List<String> clientUsernames = new ArrayList<>(connectedClients.keySet());
            for (String username : clientUsernames) {
                disconnectClient(username);
            }

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Shutdown thread pool
            clientThreadPool.shutdown();
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientThreadPool.shutdownNow();
            }

            System.out.println("Server shutdown complete");
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    // ============ Utility Methods ============

    private String convertMessageToJson(Message message) {
        // This will be improved in protocol stage
        return message.toString();
    }

    /**
     * Gets the number of registered users.
     *
     * @return count of registered users
     */
    public int getRegisteredUserCount() {
        return registeredUsers.size();
    }

    /**
     * Gets the number of connected users.
     *
     * @return count of connected users
     */
    public int getConnectedUserCount() {
        return connectedClients.size();
    }

    public Map<String, User> getRegisteredUsers() {
        return new HashMap<>(registeredUsers);
    }
}

