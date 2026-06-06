package org.example.client.network;

import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Manages the TCP socket connection to the server.
 * Handles establishing the connection, sending raw JSON strings, and tearing down the socket.
 */
public class ServerConnection {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private MessageHandler messageHandler;
    private volatile boolean connected;

    /**
     * Constructs an uninitialized ServerConnection.
     */
    public ServerConnection() {
        this.connected = false;
    }

    /**
     * Attempts to connect to the messaging server and starts the listening thread.
     *
     * @param host               The server's IP address or hostname
     * @param port               The server's listening port
     * @param messageCallback    The function to execute when a JSON message is received
     * @param disconnectCallback The function to execute if the connection drops
     * @return true if the connection is successfully established, false otherwise
     */
    public boolean connect(String host, int port, Consumer<JsonObject> messageCallback, Runnable disconnectCallback) {
        try {
            // Establish the TCP socket
            socket = new Socket(host, port);

            // Initialize I/O streams with UTF-8 encoding as required by the protocol
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            this.connected = true;

            // Initialize and start the background listening thread
            messageHandler = new MessageHandler(input, messageCallback, () -> {
                this.connected = false;
                if (disconnectCallback != null) {
                    disconnectCallback.run();
                }
            });

            Thread listenerThread = new Thread(messageHandler, "Server-Listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            this.connected = false;
            return false;
        }
    }

    /**
     * Sends a JSON string to the server.
     *
     * @param jsonMessage The properly formatted JSON protocol string
     */
    public void sendMessage(String jsonMessage) {
        if (connected && output != null) {
            output.println(jsonMessage);
            output.flush();
        } else {
            System.err.println("Cannot send message. Not connected to server.");
        }
    }

    /**
     * Closes the socket connection and cleans up all resources.
     */
    public void disconnect() {
        this.connected = false;

        if (messageHandler != null) {
            messageHandler.stop();
        }

        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error while disconnecting: " + e.getMessage());
        }
    }

    /**
     * Checks if the network connection is currently active.
     *
     * @return true if connected and socket is open
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}