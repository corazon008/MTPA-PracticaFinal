package org.example.client.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.client.model.ClientSession;
import org.example.client.network.HeartbeatSender;
import org.example.client.network.ServerConnection;
import org.example.client.view.LoginFrame;
import org.example.client.view.MainChatFrame;
import org.example.client.view.NotificationUtils;
import org.example.client.view.PrivateChatFrame;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Main controller linking the views, the local session model, and the network layer.
 */
public class ChatController {
    private final ServerConnection connection;
    private final ClientSession session;
    private final LoginFrame loginFrame;
    private final MainChatFrame mainChatFrame;
    private final Map<String, PrivateChatFrame> privateChats;
    private HeartbeatSender heartbeatSender;

    public ChatController(ServerConnection connection, ClientSession session, LoginFrame loginFrame, MainChatFrame mainChatFrame) {
        this.connection = connection;
        this.session = session;
        this.loginFrame = loginFrame;
        this.mainChatFrame = mainChatFrame;
        this.privateChats = new HashMap<>();

        setupViewListeners();

        // Intentar conectar al servidor local en el puerto 5000
        boolean isConnected = connection.connect(
                "127.0.0.1",
                5000,
                this::processServerMessage, // Redirige les messages entrants vers votre méthode de traitement
                () -> NotificationUtils.showErrorNotification(loginFrame, "Se ha perdido la conexión con el servidor.")
        );

        if (!isConnected) {
            NotificationUtils.showErrorNotification(loginFrame, "No se pudo conectar al servidor (127.0.0.1:5000).");
        }
    }

    /**
     * Initializes all event listeners for the graphical interfaces.
     */
    private void setupViewListeners() {
        // LoginFrame Listeners
        loginFrame.addRegisterListener(e -> handleRegister());
        loginFrame.addLoginListener(e -> handleLogin());

        // MainChatFrame Listeners
        mainChatFrame.addSendButtonListener(e -> handleSendRoomMessage());
        mainChatFrame.addLoadHistoryListener(e -> handleLoadHistory());
        mainChatFrame.addPrivateChatListener(e -> handleInitiatePrivateChat());

        mainChatFrame.addRoomSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && session.isAuthenticated()) {
                handleRoomChange();
            }
        });
    }

    // UI Actions to networks requests

    private void handleRegister() {
        String username = loginFrame.getUsername();
        if (username.isEmpty()) {
            NotificationUtils.showErrorNotification(loginFrame, "El nombre de usuario no puede estar vacío.");
            return;
        }

        JsonObject info = new JsonObject();
        info.addProperty("user", username);
        sendRequest("REGISTER", info);
    }

    private void handleLogin() {
        String username = loginFrame.getUsername();
        long key = loginFrame.getAccessKey();

        if (username.isEmpty() || key == -1) {
            NotificationUtils.showErrorNotification(loginFrame, "Credenciales inválidas. Compruebe su usuario y clave.");
            return;
        }

        session.setUsername(username);
        session.setAccessKey(key);

        JsonObject info = new JsonObject();
        info.addProperty("user", username);
        info.addProperty("key", key);
        sendRequest("LOGIN", info);
    }

    private void handleRoomChange() {
        String selectedRoom = mainChatFrame.getSelectedRoom();
        if (selectedRoom.equals(session.getCurrentRoom())) {
            return;
        }

        mainChatFrame.clearChatArea();

        // Leave previous room if any
        if (session.getCurrentRoom() != null) {
            JsonObject leaveInfo = new JsonObject();
            leaveInfo.addProperty("room", session.getCurrentRoom());
            sendRequest("LEAVE_ROOM", leaveInfo);
        }

        // Join new room
        JsonObject joinInfo = new JsonObject();
        joinInfo.addProperty("room", selectedRoom);
        sendRequest("JOIN_ROOM", joinInfo);

        session.setCurrentRoom(selectedRoom);
    }

    private void handleSendRoomMessage() {
        String content = mainChatFrame.getMessageText();
        String room = mainChatFrame.getSelectedRoom();

        if (content.isEmpty() || room == null) return;

        JsonObject info = new JsonObject();
        info.addProperty("room", room);
        info.addProperty("content", content);
        sendRequest("SEND_ROOM_MSG", info);

        mainChatFrame.clearMessageInput();
    }

    private void handleLoadHistory() {
        String date = mainChatFrame.getHistoryDate();
        String room = mainChatFrame.getSelectedRoom();

        if (date.isEmpty()) {
            NotificationUtils.showErrorNotification(mainChatFrame, "Por favor, introduzca una fecha (AAAA-MM-DD).");
            return;
        }

        JsonObject info = new JsonObject();
        info.addProperty("room", room);
        info.addProperty("date", date);
        sendRequest("GET_HISTORY", info);
    }

    private void handleInitiatePrivateChat() {
        String targetUser = mainChatFrame.getPrivateTargetUser();
        if (targetUser.isEmpty() || targetUser.equals(session.getUsername())) {
            NotificationUtils.showErrorNotification(mainChatFrame, "Usuario no válido para chat privado.");
            return;
        }
        openPrivateChatWindow(targetUser);
    }

    private void handleSendPrivateMessage(String targetUser, String content, PrivateChatFrame frame) {
        if (content.isEmpty()) return;

        JsonObject info = new JsonObject();
        info.addProperty("receiver", targetUser);
        info.addProperty("content", content);
        sendRequest("SEND_PRIV_MSG", info);

        frame.appendMessage(session.getUsername(), content);
        frame.clearMessageInput();
    }

    private void sendRequest(String command, JsonObject information) {
        JsonObject request = new JsonObject();
        request.addProperty("command", command);
        request.add("information", information);

        if (connection.isConnected()) {
            connection.sendMessage(request.toString());
        } else {
            NotificationUtils.showErrorNotification(null, "No hay conexión con el servidor.");
        }
    }

    // Network responses to UI updates

    /**
     * Entry point for messages coming from the network listening thread.
     * Must be called on the Event Dispatch Thread (EDT) for UI safety.
     *
     * @param response The parsed JSON response from the server
     */
    public void processServerMessage(JsonObject response) {
        SwingUtilities.invokeLater(() -> {
            String type = response.has("type") ? response.get("type").getAsString() : "UNKNOWN";

            switch (type) {
                case "OK":
                    handleSuccessResponse(response);
                    break;
                case "ERROR":
                    handleErrorResponse(response);
                    break;
                case "SYSTEM":
                    handleSystemEvent(response);
                    break;
                case "ROOM_MESSAGE":
                    handleRoomMessageEvent(response);
                    break;
                case "PRIVATE_MESSAGE":
                    handlePrivateMessageEvent(response);
                    break;
                case "ADMIN_BROADCAST":
                    handleAdminBroadcastEvent(response);
                    break;
                default:
                    System.err.println("Tipo de mensaje no reconocido: " + type);
            }
        });
    }

    private void handleSuccessResponse(JsonObject response) {
        String message = response.get("message").getAsString();
        JsonObject data = response.has("data") ? response.getAsJsonObject("data") : null;

        switch (message) {
            case "USER_REGISTERED":
                long key = data.get("key").getAsLong();
                NotificationUtils.showInfoNotification(loginFrame,
                        "Registro exitoso. Su clave de acceso es: " + key,
                        "Registro completado");
                break;
            case "LOGIN_SUCCESS":
                session.setAuthenticated(true);
                loginFrame.setVisible(false);
                mainChatFrame.setVisible(true);
                handleRoomChange(); // Join the default selected room immediately
                heartbeatSender = new HeartbeatSender(connection);
                heartbeatSender.start();
                break;
            case "USER_IN_ROOM":
            case "HISTORY_RETRIEVED":
                if (data != null && data.has("messages")) {
                    JsonArray messages = data.getAsJsonArray("messages");
                    mainChatFrame.clearChatArea();
                    for (JsonElement msgElem : messages) {
                        JsonObject msgObj = msgElem.getAsJsonObject();
                        String sender = msgObj.get("sender").getAsString();
                        String content = msgObj.get("content").getAsString();
                        String time = msgObj.get("timestamp").getAsString();
                        mainChatFrame.appendMessage("[" + time + "] " + sender + ": " + content);
                    }
                }
                break;
            case "MESSAGE_SENT":
            case "USER_LEFT_ROOM":
            case "HEARTBEAT_ACK":
                // Silent successes, no UI interruption needed
                break;
        }
    }

    private void handleErrorResponse(JsonObject response) {
        String errorMessage = response.get("message").getAsString();
        Component parent = session.isAuthenticated() ? mainChatFrame : loginFrame;

        String translatedError = translateServerError(errorMessage);
        NotificationUtils.showErrorNotification(parent, translatedError);
    }

    private void handleSystemEvent(JsonObject response) {
        String action = response.get("action").getAsString();
        if ("USER_JOINED".equals(action)) {
            String user = response.get("user").getAsString();
            String room = response.get("room").getAsString();

            if (room.equals(session.getCurrentRoom())) {
                String notification = "El usuario '" + user + "' ha entrado en el salón.";
                mainChatFrame.appendSystemMessage(notification);
            }
        }
    }

    private void handleRoomMessageEvent(JsonObject response) {
        String room = response.get("room").getAsString();
        if (room.equals(session.getCurrentRoom())) {
            String sender = response.get("sender").getAsString();
            String content = response.get("content").getAsString();
            String time = response.get("timestamp").getAsString();
            mainChatFrame.appendMessage("[" + time + "] " + sender + ": " + content);
        }
    }

    private void handlePrivateMessageEvent(JsonObject response) {
        String sender = response.get("sender").getAsString();
        String content = response.get("content").getAsString();

        PrivateChatFrame frame = openPrivateChatWindow(sender);
        frame.appendMessage(sender, content);
    }

    private void handleAdminBroadcastEvent(JsonObject response) {
        String content = response.get("content").getAsString();
        NotificationUtils.showWarningNotification(mainChatFrame, "MENSAJE DEL SERVIDOR: " + content);
    }

    // Utilities

    private PrivateChatFrame openPrivateChatWindow(String targetUser) {
        if (privateChats.containsKey(targetUser)) {
            PrivateChatFrame existingFrame = privateChats.get(targetUser);
            existingFrame.toFront();
            return existingFrame;
        }

        PrivateChatFrame newFrame = new PrivateChatFrame(targetUser);
        newFrame.addSendButtonListener(e -> handleSendPrivateMessage(targetUser, newFrame.getMessageText(), newFrame));

        newFrame.addWindowCloseListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                privateChats.remove(targetUser); // Destroys context on close
            }
        });

        privateChats.put(targetUser, newFrame);
        newFrame.setVisible(true);
        return newFrame;
    }

    private String translateServerError(String errorCode) {
        switch (errorCode) {
            case "USERNAME_EXISTS": return "El nombre de usuario ya está en uso.";
            case "INVALID_CREDENTIALS": return "Credenciales inválidas.";
            case "USER_NOT_CONNECTED": return "El usuario destino no está conectado.";
            case "USER_NOT_FOUND": return "El usuario no existe.";
            case "MESSAGE_TOO_LONG": return "El mensaje excede los 190 caracteres.";
            case "ROOM_NOT_FOUND": return "El salón especificado no existe.";
            case "SERVER_MAINTENANCE": return "El servidor está en mantenimiento temporal.";
            default: return "Ha ocurrido un error en el servidor: " + errorCode;
        }
    }
}