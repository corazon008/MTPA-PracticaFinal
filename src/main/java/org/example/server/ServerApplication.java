package org.example.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Properties;

/**
 * Main entry point for the messaging server application.
 * Provides both server network operations and admin console interface.
 */
public class ServerApplication {
    private final Server server;
    private static final int DEFAULT_PORT = 5000;
    private static final String DEFAULT_CONFIG_FILE = "server.properties";
    private volatile boolean running;

    /**
     * Constructs a ServerApplication instance.
     */
    public ServerApplication() {
        this.server = new Server();
        this.running = false;
    }

    /**
     * Starts the server and the admin console.
     *
     * @param port the port to listen on
     */
    public void start(int port) {
        running = true;

        // Start server in a separate thread
        Thread serverThread = new Thread(() -> {
            try {
                server.start(port);
            } catch (IOException e) {
                System.err.println("Server failed to start: " + e.getMessage());
                running = false;
            }
        });
        serverThread.setDaemon(false);
        serverThread.start();

        // Start admin console in main thread
        startAdminConsole();
    }

    /**
     * Starts the admin console interface for server management.
     */
    private void startAdminConsole() {
        System.out.println("\n=== Server Admin Console ===");
        System.out.println("Type 'help' for available commands\n");

        Scanner scanner = new Scanner(System.in);
        while (running) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                processAdminCommand(input);
            } catch (Exception e) {
                System.err.println("Error processing command: " + e.getMessage());
            }
        }
        scanner.close();
    }

    /**
     * Processes administrator commands.
     *
     * @param command the admin command
     */
    private void processAdminCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help":
                printHelp();
                break;
            case "stats":
                printStats();
                break;
            case "list_clients":
                listConnectedClients();
                break;
            case "kick":
                kickClient(parts);
                break;
            case "broadcast":
                broadcastMessage(parts);
                break;
            case "metrics":
                exportMetrics();
                break;
            case "stop_accept":
                stopAcceptingClients();
                break;
            case "start_accept":
                startAcceptingClients();
                break;
            case "maintenance_on":
                enableMaintenance();
                break;
            case "maintenance_off":
                disableMaintenance();
                break;
            case "shutdown":
                shutdown();
                break;
            case "users":
                listRegisteredUsers();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    /**
     * Prints help information about available commands.
     */
    private void printHelp() {
        System.out.println("\n=== Available Commands ===");
        System.out.println("help              - Display this help message");
        System.out.println("stats             - Display server statistics");
        System.out.println("list_clients      - List all connected clients");
        System.out.println("kick <user>       - Disconnect a connected client");
        System.out.println("broadcast <msg>   - Send an admin broadcast to all connected clients");
        System.out.println("metrics           - Export a metrics snapshot to file");
        System.out.println("users             - List all registered users");
        System.out.println("stop_accept       - Stop accepting new client connections");
        System.out.println("start_accept      - Start accepting new client connections");
        System.out.println("maintenance_on    - Enable maintenance mode (blocks messages)");
        System.out.println("maintenance_off   - Disable maintenance mode");
        System.out.println("shutdown          - Gracefully shutdown the server");
        System.out.println();
    }

    /**
     * Prints server statistics.
     */
    private void printStats() {
        java.util.Map<String, Object> stats = server.getStatistics();
        server.exportMetrics();

        System.out.println("\n=== Server Statistics ===");
        System.out.println("Registered Users: " + stats.get("registered_users"));
        System.out.println("Connected Users: " + stats.get("connected_users"));
        System.out.println("Total Rooms: " + stats.get("total_rooms"));

        java.util.Map<String, Integer> roomUsers =
                (java.util.Map<String, Integer>) stats.get("room_user_counts");
        System.out.println("\nUsers per Room:");
        for (java.util.Map.Entry<String, Integer> entry : roomUsers.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        java.util.Map<String, Long> roomMessages =
                (java.util.Map<String, Long>) stats.get("room_message_counts");
        System.out.println("\nMessages per Room:");
        for (java.util.Map.Entry<String, Long> entry : roomMessages.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();
    }

    /**
     * Kicks a connected client from the admin console.
     *
     * @param parts split command and arguments
     */
    private void kickClient(String[] parts) {
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            System.out.println("Usage: kick <username>\n");
            return;
        }

        String username = parts[1].trim();
        boolean kicked = server.kickClient(username);
        if (kicked) {
            System.out.println("Client kicked: " + username + "\n");
        } else {
            System.out.println("Client not connected: " + username + "\n");
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param parts split command and arguments
     */
    private void broadcastMessage(String[] parts) {
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            System.out.println("Usage: broadcast <message>\n");
            return;
        }

        server.broadcastAdminMessage(parts[1].trim());
        System.out.println("Broadcast sent to connected clients\n");
    }

    /**
     * Exports a metrics snapshot to the configured metrics file.
     */
    private void exportMetrics() {
        Path metricsFile = server.exportMetrics();
        System.out.println("Metrics exported to: " + metricsFile.toAbsolutePath() + "\n");
    }

    /**
     * Lists all connected clients.
     */
    private void listConnectedClients() {
        java.util.Map<String, org.example.model.User> users = server.getRegisteredUsers();
        System.out.println("\n=== Connected Clients ===");
        int count = 0;
        for (org.example.model.User user : users.values()) {
            if (user.isConnected()) {
                System.out.println("  - " + user.getUsername() + " (last heartbeat: "
                        + user.getLastHeartbeat() + ")");
                count++;
            }
        }
        if (count == 0) {
            System.out.println("  No clients connected");
        }
        System.out.println();
    }

    /**
     * Lists all registered users.
     */
    private void listRegisteredUsers() {
        java.util.Map<String, org.example.model.User> users = server.getRegisteredUsers();
        System.out.println("\n=== Registered Users ===");
        for (org.example.model.User user : users.values()) {
            String status = user.isConnected() ? "CONNECTED" : "OFFLINE";
            System.out.println("  - " + user.getUsername() + " [" + status + "]");
        }
        System.out.println("Total: " + users.size() + "\n");
    }

    /**
     * Stops accepting new client connections.
     */
    private void stopAcceptingClients() {
        server.stopAcceptingClients();
        System.out.println("Server is no longer accepting new connections\n");
    }

    /**
     * Starts accepting new client connections.
     */
    private void startAcceptingClients() {
        server.startAcceptingClients();
        System.out.println("Server is now accepting new connections\n");
    }

    /**
     * Enables maintenance mode.
     */
    private void enableMaintenance() {
        server.enableMaintenanceMode();
        System.out.println("Maintenance mode is now ENABLED\n");
    }

    /**
     * Disables maintenance mode.
     */
    private void disableMaintenance() {
        server.disableMaintenanceMode();
        System.out.println("Maintenance mode is now DISABLED\n");
    }

    /**
     * Gracefully shuts down the server.
     */
    private void shutdown() {
        System.out.println("Initiating server shutdown...");
        running = false;
        server.shutdown();
        System.exit(0);
    }

    /**
     * Main method to start the application.
     *
     * @param args command line arguments (optional: port number)
     */
    public static void main(String[] args) {
        loadConfiguration();
        int port = Integer.getInteger("server.port", DEFAULT_PORT);

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.err.println("Using default port: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        System.out.println("=== Messaging Server Starting ===");
        System.out.println("Port: " + port);
        System.out.println("Max Clients: 100");
        System.out.println("Heartbeat Timeout: 5 minutes");
        System.out.println("Predefined Rooms: IA, Deportes, Therian, Manga, UEMC");
        System.out.println();

        ServerApplication app = new ServerApplication();
        app.start(port);
    }

    /**
     * Loads optional configuration from a properties file into system properties.
     */
    private static void loadConfiguration() {
        String configPath = System.getProperty("server.config", DEFAULT_CONFIG_FILE);
        Path path = Paths.get(configPath);
        if (!Files.exists(path)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
            for (String name : properties.stringPropertyNames()) {
                if (System.getProperty(name) == null) {
                    System.setProperty(name, properties.getProperty(name));
                }
            }
            System.out.println("Loaded server configuration from " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Unable to load server configuration from " + path.toAbsolutePath()
                    + ": " + e.getMessage());
        }
    }
}

