package org.example.server;

import org.example.protocol.ProtocolRequest;
import org.example.protocol.ProtocolResponse;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handles communication with a single client.
 * Each client connection runs in its own thread.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private String username;
    private BufferedReader input;
    private PrintWriter output;
    private long lastHeartbeat;
    private boolean authenticated;

    /**
     * Constructs a ClientHandler for a client connection.
     *
     * @param socket the client socket
     * @param server the server instance
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.lastHeartbeat = System.currentTimeMillis();
        this.authenticated = false;

        try {
            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            output = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        } catch (IOException e) {
            System.err.println("Error setting up client streams: " + e.getMessage());
            closeConnection();
        }
    }

    @Override
    public void run() {
        System.out.println("ClientHandler started for: " + socket.getInetAddress());

        try {
            String line;
            while ((line = input.readLine()) != null) {
                lastHeartbeat = System.currentTimeMillis();
                processMessage(line);
            }
        } catch (SocketException e) {
            // Normal disconnection
            System.out.println("Client disconnected: " + username);
        } catch (IOException e) {
            System.err.println("Error reading from client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Processes an incoming protocol message from the client.
     *
     * @param messageJson the JSON message string
     */
    private void processMessage(String messageJson) {
        try {
            ProtocolRequest request = ProtocolRequest.parse(messageJson);
            String command = request.getCommand();

            // Handle commands
            switch (command) {
                case "REGISTER":
                    handleRegister(request);
                    break;
                case "LOGIN":
                    handleLogin(request);
                    break;
                case "HEARTBEAT":
                    handleHeartbeat(request);
                    break;
                case "LIST_ROOMS":
                    handleListRooms(request);
                    break;
                case "JOIN_ROOM":
                    handleJoinRoom(request);
                    break;
                case "LEAVE_ROOM":
                    handleLeaveRoom(request);
                    break;
                case "SEND_ROOM_MSG":
                    handleSendRoomMessage(request);
                    break;
                case "SEND_PRIV_MSG":
                    handleSendPrivateMessage(request);
                    break;
                case "GET_HISTORY":
                    handleGetHistory(request);
                    break;
                case "LOGOUT":
                    handleLogout(request);
                    break;
                default:
                    sendResponse(ProtocolResponse.error("UNKNOWN_COMMAND"));
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            sendResponse(ProtocolResponse.error("SERVER_ERROR"));
        }
    }

    // ============ Command Handlers ============

    private void handleRegister(ProtocolRequest request) {
        try {
            JsonObject info = request.getInformation();
            String username = info.get("user").getAsString();

            long key = server.registerUser(username);

            JsonObject responseData = new JsonObject();
            responseData.addProperty("key", key);

            sendResponse(ProtocolResponse.ok("USER_REGISTERED", responseData));
        } catch (Exception e) {
            if (e.getMessage().equals("USERNAME_EXISTS")) {
                sendResponse(ProtocolResponse.error("USERNAME_EXISTS"));
            } else {
                sendResponse(ProtocolResponse.error("SERVER_ERROR"));
            }
        }
    }

    private void handleLogin(ProtocolRequest request) {
        try {
            JsonObject info = request.getInformation();
            String username = info.get("user").getAsString();
            long key = info.get("key").getAsLong();

            // Authenticate user
            server.loginUser(username, key);

            // Connect client
            this.username = username;
            this.authenticated = true;
            server.connectClient(username, this);

            sendResponse(ProtocolResponse.ok("LOGIN_SUCCESS"));
        } catch (Exception e) {
            if (e.getMessage().equals("INVALID_CREDENTIALS")) {
                sendResponse(ProtocolResponse.error("INVALID_CREDENTIALS"));
            } else {
                sendResponse(ProtocolResponse.error("SERVER_ERROR"));
            }
        }
    }

    private void handleLogout(ProtocolRequest request) {
        if (authenticated && username != null) {
            server.disconnectClient(username);
            authenticated = false;
            username = null;
            sendResponse(ProtocolResponse.ok("LOGOUT_SUCCESS"));
        }
    }

    private void handleHeartbeat(ProtocolRequest request) {
        lastHeartbeat = System.currentTimeMillis();
        // Update server-side user lastHeartbeat if authenticated
        if (authenticated && username != null) {
            server.updateUserHeartbeat(username);
        }
        sendResponse(ProtocolResponse.ok("HEARTBEAT_ACK"));
    }

    private void handleListRooms(ProtocolRequest request) {
        if (!authenticated) {
            sendResponse(ProtocolResponse.error("NOT_AUTHENTICATED"));
            return;
        }

        JsonObject responseData = new JsonObject();
        int count = 0;
        for (org.example.model.Room room : server.getRooms()) {
            JsonObject roomInfo = new JsonObject();
            roomInfo.addProperty("users", room.getUserCount());
            roomInfo.addProperty("messages", room.getMessageCount());
            responseData.add(room.getName(), roomInfo);
            count++;
        }

        if (count == 0) {
            sendResponse(ProtocolResponse.error("NO_ROOMS_AVAILABLE"));
        } else {
            sendResponse(ProtocolResponse.ok("ROOM_LIST", responseData));
        }
    }

    private void handleJoinRoom(ProtocolRequest request) {
        if (!authenticated || username == null) {
            sendResponse(ProtocolResponse.error("NOT_AUTHENTICATED"));
            return;
        }

        try {
            JsonObject info = request.getInformation();
            String roomName = info.get("room").getAsString();

            server.addUserToRoom(username, roomName);

            JsonObject notification = new JsonObject();
            notification.addProperty("type", "USER_JOINED");
            notification.addProperty("room", roomName);
            notification.addProperty("user", username);

            sendResponse(ProtocolResponse.ok("USER_IN_ROOM"));
        } catch (Exception e) {
            if (e.getMessage().equals("ROOM_NOT_FOUND")) {
                sendResponse(ProtocolResponse.error("ROOM_NOT_FOUND"));
            } else {
                sendResponse(ProtocolResponse.error("SERVER_ERROR"));
            }
        }
    }

    private void handleLeaveRoom(ProtocolRequest request) {
        if (!authenticated || username == null) {
            sendResponse(ProtocolResponse.error("NOT_AUTHENTICATED"));
            return;
        }

        try {
            JsonObject info = request.getInformation();
            String roomName = info.get("room").getAsString();

            server.removeUserFromRoom(username, roomName);
            sendResponse(ProtocolResponse.ok("USER_LEFT_ROOM"));
        } catch (Exception e) {
            sendResponse(ProtocolResponse.error("SERVER_ERROR"));
        }
    }

    private void handleSendRoomMessage(ProtocolRequest request) {
        if (!authenticated || username == null) {
            sendResponse(ProtocolResponse.error("NOT_AUTHENTICATED"));
            return;
        }

        if (server.isMaintenanceMode()) {
            sendResponse(ProtocolResponse.error("SERVER_MAINTENANCE"));
            return;
        }

        try {
            JsonObject info = request.getInformation();
            String roomName = info.get("room").getAsString();
            String content = info.get("content").getAsString();

            // Create message
            org.example.model.Message message = new org.example.model.Message(
                    username, content, roomName, null);

            // Validate and store
            server.storeRoomMessage(message);

            // Send response to sender
            sendResponse(ProtocolResponse.ok("MESSAGE_SENT"));

            // Broadcast to room
            server.broadcastRoomMessage(message, username);
        } catch (Exception e) {
            if (e.getMessage().equals("MESSAGE_TOO_LONG")) {
                sendResponse(ProtocolResponse.error("MESSAGE_TOO_LONG"));
            } else if (e.getMessage().equals("ROOM_NOT_FOUND")) {
                sendResponse(ProtocolResponse.error("ROOM_NOT_FOUND"));
            } else if (e.getMessage().equals("NOT_IN_ROOM")) {
                sendResponse(ProtocolResponse.error("NOT_IN_ROOM"));
            } else {
                sendResponse(ProtocolResponse.error("SERVER_ERROR"));
            }
        }
    }

    private void handleSendPrivateMessage(ProtocolRequest request) {
        if (!authenticated || username == null) {
            sendResponse(ProtocolResponse.error("NOT_AUTHENTICATED"));
            return;
        }

        try {
            JsonObject info = request.getInformation();
            String receiver = info.get("receiver").getAsString();
            String content = info.get("content").getAsString();

            // Check if receiver exists and is connected
            if (!server.isUserConnected(receiver)) {
                if (server.getRegisteredUsers().containsKey(receiver)) {
                    sendResponse(ProtocolResponse.error("USER_NOT_CONNECTED"));
                } else {
                    sendResponse(ProtocolResponse.error("USER_NOT_FOUND"));
                }
                return;
            }

            // Create and send private message
            org.example.model.Message message = new org.example.model.Message(
                    username, content, null, receiver);

            server.sendPrivateMessage(message);
            sendResponse(ProtocolResponse.ok("MESSAGE_SENT"));
        } catch (Exception e) {
            if (e.getMessage().equals("USER_NOT_CONNECTED")) {
                sendResponse(ProtocolResponse.error("USER_NOT_CONNECTED"));
            } else if (e.getMessage().equals("USER_NOT_FOUND")) {
                sendResponse(ProtocolResponse.error("USER_NOT_FOUND"));
            } else {
                sendResponse(ProtocolResponse.error("SERVER_ERROR"));
            }
        }
    }

    private void handleGetHistory(ProtocolRequest request) {
        if (!authenticated || username == null) {
            sendResponse(ProtocolResponse.error("NOT_AUTHENTICATED"));
            return;
        }

        try {
            JsonObject info = request.getInformation();
            String roomName = info.get("room").getAsString();
            String date = info.has("date") ? info.get("date").getAsString() : null;

            org.example.model.Room room = server.getRoom(roomName);
            if (room == null) {
                sendResponse(ProtocolResponse.error("ROOM_NOT_FOUND"));
                return;
            }

            java.util.List<org.example.model.Message> messages;
            if (date != null) {
                messages = room.getMessagesByDate(date);
            } else {
                messages = room.getMessagesFromLastDay();
            }

            JsonObject responseData = new JsonObject();
            responseData.addProperty("count", messages.size());

            sendResponse(ProtocolResponse.ok("HISTORY_RETRIEVED", responseData));
        } catch (Exception e) {
            sendResponse(ProtocolResponse.error("SERVER_ERROR"));
        }
    }

    // ============ Utility Methods ============

    /**
     * Sends a response to the client.
     *
     * @param response the ProtocolResponse to send
     */
    public void sendResponse(ProtocolResponse response) {
        output.println(response.toJson());
        output.flush();
    }

    /**
     * Sends a message to the client (e.g., broadcast message).
     *
     * @param messageJson the JSON message string
     */
    public void sendMessage(String messageJson) {
        if (output != null && !output.checkError()) {
            output.println(messageJson);
            output.flush();
        }
    }

    /**
     * Gets the timestamp of the last heartbeat from this client.
     *
     * @return the last heartbeat time in milliseconds
     */
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * Gets the username of this client (if authenticated).
     *
     * @return the username, or null if not authenticated
     */
    public String getUsername() {
        return username;
    }

    /**
     * Checks if this client is authenticated.
     *
     * @return true if authenticated
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Closes the client connection and cleans up resources.
     */
    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    /**
     * Cleans up resources when the client disconnects.
     */
    private void cleanup() {
        if (authenticated && username != null) {
            server.disconnectClient(username);
        }
        closeConnection();
        System.out.println("ClientHandler cleanup completed for: " + username);
    }
}
