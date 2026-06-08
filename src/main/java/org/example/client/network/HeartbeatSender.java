package org.example.client.network;

import com.google.gson.JsonObject;

/**
 * Thread responsible for sending periodic heartbeat signals to the server.
 * This prevents the server from disconnecting the client due to inactivity.
 */
public class HeartbeatSender implements Runnable {

    private final ServerConnection connection;
    private volatile boolean running;

    // Interval set to 1 minutes (server timeout is configured at 5 minutes)
    private static final long INTERVAL_MS = 60 * 1000;

    /**
     * Constructs the HeartbeatSender with a reference to the active connection.
     *
     * @param connection The network connection to the server
     */
    public HeartbeatSender(ServerConnection connection) {
        this.connection = connection;
        this.running = false;
    }

    /**
     * Starts the heartbeat thread.
     */
    public void start() {
        if (!running) {
            running = true;
            Thread thread = new Thread(this, "Heartbeat-Thread");
            thread.setDaemon(true); // Ensures it doesn't prevent JVM shutdown
            thread.start();
        }
    }

    /**
     * Stops the heartbeat thread safely.
     */
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Wait for the defined interval before sending the next ping
                Thread.sleep(INTERVAL_MS);

                if (connection != null && connection.isConnected()) {
                    sendHeartbeat();
                }
            } catch (InterruptedException e) {
                System.err.println("Hilo de heartbeat interrumpido.");
                Thread.currentThread().interrupt();
                break; // Exit the loop if the thread is interrupted
            }
        }
    }

    /**
     * Formats and sends the HEARTBEAT command protocol request.
     */
    private void sendHeartbeat() {
        JsonObject request = new JsonObject();
        request.addProperty("command", "HEARTBEAT");
        request.add("information", new JsonObject()); // Empty information block as per protocol

        connection.sendMessage(request.toString());
    }
}