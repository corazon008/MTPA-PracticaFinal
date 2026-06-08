package org.example.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.server.persistence.PersistenceManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class ServerAdminObservabilityTests {

    @Before
    public void setup() {
        String dir = "target/test-data/ServerAdminObservabilityTests_" + System.nanoTime(); // to avoid loading previous test data
        PersistenceManager.setDataDir(dir);
        PersistenceManager.initialize();
    }

    @Test
    public void testBroadcastAdminMessageReachesConnectedClients() throws Exception {
        Server server = new Server();

        try (TestClient alice = TestClient.connect(server, unique("admin_broadcast_alice"));
             TestClient bob = TestClient.connect(server, unique("admin_broadcast_bob"))) {

            server.broadcastAdminMessage("maintenance in 5 minutes");

            JsonObject aliceMessage = alice.readJson();
            JsonObject bobMessage = bob.readJson();

            Assert.assertEquals("ADMIN_BROADCAST", aliceMessage.get("type").getAsString());
            Assert.assertEquals("maintenance in 5 minutes", aliceMessage.get("content").getAsString());
            Assert.assertEquals("ADMIN_BROADCAST", bobMessage.get("type").getAsString());
            Assert.assertEquals("maintenance in 5 minutes", bobMessage.get("content").getAsString());
        }
    }

    @Test
    public void testKickClientDisconnectsClient() throws Exception {
        Server server = new Server();
        String username = unique("admin_kick_user");

        try (TestClient client = TestClient.connect(server, username)) {
            Assert.assertTrue(server.isUserConnected(username));

            boolean kicked = server.kickClient(username);
            Assert.assertTrue(kicked);

            Thread.sleep(150);
            Assert.assertFalse(server.isUserConnected(username));
            Assert.assertNull("Expected the server side socket to be closed", client.readLineAllowingTimeout());
        }
    }

    @Test
    public void testExportMetricsWritesSnapshotFile() throws Exception {
        Server server = new Server();
        String username = unique("admin_metrics_user");
        server.registerUser(username);
        server.addUserToRoom(username, "IA");

        Path metricsFile = server.exportMetrics();
        Assert.assertNotNull(metricsFile);
        Assert.assertTrue(Files.exists(metricsFile));

        String content = Files.readString(metricsFile, StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains("registered_users=1"));
        Assert.assertTrue(content.contains("total_rooms="));
        Assert.assertTrue(content.contains("room_user_counts="));
    }

    private static String unique(String prefix) {
        return prefix + "_" + System.nanoTime() + "_" + (int) (Math.random() * 10000);
    }

    private static final class TestClient implements AutoCloseable {
        private final Socket socket;
        private final BufferedReader reader;
        private final Thread handlerThread;
        private final AtomicReference<Throwable> handlerFailure;

        private TestClient(Socket socket, Thread handlerThread, AtomicReference<Throwable> handlerFailure) throws Exception {
            this.socket = socket;
            this.socket.setSoTimeout(1000);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.handlerThread = handlerThread;
            this.handlerFailure = handlerFailure;
        }

        static TestClient connect(Server server, String username) throws Exception {
            server.registerUser(username);
            return connectConnected(server, username);
        }

        private static TestClient connectConnected(Server server, String username) throws Exception {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            AtomicReference<Throwable> handlerFailure = new AtomicReference<>();

            Socket clientSocket = new Socket("127.0.0.1", port);
            Socket handlerSocket = serverSocket.accept();
            serverSocket.close();

            ClientHandler handler = new ClientHandler(handlerSocket, server);
            server.connectClient(username, handler);

            Thread handlerThread = new Thread(() -> {
                try {
                    handler.run();
                } catch (Throwable t) {
                    handlerFailure.set(t);
                }
            }, "server-admin-test-client");
            handlerThread.setDaemon(true);
            handlerThread.start();

            return new TestClient(clientSocket, handlerThread, handlerFailure);
        }

        JsonObject readJson() throws Exception {
            String line = reader.readLine();
            Assert.assertNotNull("Expected a JSON message", line);
            return JsonParser.parseString(line).getAsJsonObject();
        }

        String readLineAllowingTimeout() throws Exception {
            try {
                return reader.readLine();
            } catch (SocketTimeoutException e) {
                return null;
            }
        }

        @Override
        public void close() throws Exception {
            Throwable failure = null;
            try {
                socket.close();
            } catch (Throwable t) {
                failure = t;
            }

            if (handlerThread != null) {
                handlerThread.join(1000);
            }

            if (handlerFailure.get() != null) {
                throw new AssertionError("Client handler failed", handlerFailure.get());
            }

            if (failure != null) {
                if (failure instanceof Exception) {
                    throw (Exception) failure;
                }
                throw new RuntimeException(failure);
            }
        }
    }
}




