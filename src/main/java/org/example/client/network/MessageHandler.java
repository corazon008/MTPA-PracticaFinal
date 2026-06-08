package org.example.client.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Background thread dedicated to listening for incoming messages from the server.
 * It reads the raw JSON strings, parses them, and forwards them to the provided callback.
 */
public class MessageHandler implements Runnable {

    private final BufferedReader reader;
    private final Consumer<JsonObject> messageCallback;
    private final Runnable disconnectCallback;
    private volatile boolean running;

    /**
     * Constructs the MessageHandler.
     *
     * @param reader             The input stream reader connected to the server's socket.
     * @param messageCallback    Function to execute when a valid JSON message is received.
     * @param disconnectCallback Function to execute if the connection is lost.
     */
    public MessageHandler(BufferedReader reader, Consumer<JsonObject> messageCallback, Runnable disconnectCallback) {
        this.reader = reader;
        this.messageCallback = messageCallback;
        this.disconnectCallback = disconnectCallback;
        this.running = false;
    }

    /**
     * Stops the listening loop safely.
     */
    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        this.running = true;

        try {
            String line;
            // readLine() blocks until data is available, a newline is read, or the stream ends
            while (running && (line = reader.readLine()) != null) {
                try {
                    // Parse the raw string into a Google GSON JsonObject
                    JsonObject jsonResponse = JsonParser.parseString(line).getAsJsonObject();

                    // Forward the JSON object to the controller (or connection manager)
                    if (messageCallback != null) {
                        messageCallback.accept(jsonResponse);
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("Warning: Received malformed JSON from server -> " + line);
                }
            }
        } catch (IOException e) {
            // This exception is normal if the socket is closed intentionally by the client
            if (running) {
                System.err.println("Connection to server lost: " + e.getMessage());
            }
        } finally {
            this.running = false;
            // Notify the application that the socket is completely dead
            if (disconnectCallback != null) {
                disconnectCallback.run();
            }
        }
    }
}