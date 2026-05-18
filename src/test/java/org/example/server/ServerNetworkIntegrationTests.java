package org.example.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Network-focused tests that exercise the real socket protocol handled by ClientHandler.
 */
public class ServerNetworkIntegrationTests {

    @Test
    public void testUnauthenticatedRequestReturnsNotAuthenticated() throws Exception {
        Server server = new Server();
        try (SocketTestClient client = SocketTestClient.connect(server)) {
            client.send(request("LIST_ROOMS", new JsonObject()));

            JsonObject response = client.readJson();
            Assert.assertEquals("ERROR", response.get("type").getAsString());
            Assert.assertEquals("NOT_AUTHENTICATED", response.get("message").getAsString());
        }
    }

    @Test
    public void testRegisterLoginJoinAndRoomMessageOverSocket() throws Exception {
        Server server = new Server();
        String username = unique("net_room_user");

        try (SocketTestClient client = SocketTestClient.connect(server)) {
            JsonObject registerInfo = new JsonObject();
            registerInfo.addProperty("user", username);
            client.send(request("REGISTER", registerInfo));

            JsonObject registerResponse = client.readJson();
            Assert.assertEquals("OK", registerResponse.get("type").getAsString());
            Assert.assertEquals("USER_REGISTERED", registerResponse.get("message").getAsString());
            long key = registerResponse.getAsJsonObject("data").get("key").getAsLong();

            JsonObject loginInfo = new JsonObject();
            loginInfo.addProperty("user", username);
            loginInfo.addProperty("key", key);
            client.send(request("LOGIN", loginInfo));

            JsonObject loginResponse = client.readJson();
            Assert.assertEquals("OK", loginResponse.get("type").getAsString());
            Assert.assertEquals("LOGIN_SUCCESS", loginResponse.get("message").getAsString());

            JsonObject joinInfo = new JsonObject();
            joinInfo.addProperty("room", "IA");
            client.send(request("JOIN_ROOM", joinInfo));

            JsonObject joinEvent = client.readJson();
            Assert.assertEquals("SYSTEM", joinEvent.get("type").getAsString());
            Assert.assertEquals("USER_JOINED", joinEvent.get("action").getAsString());
            Assert.assertEquals("IA", joinEvent.get("room").getAsString());
            Assert.assertEquals(username, joinEvent.get("user").getAsString());

            JsonObject joinResponse = client.readJson();
            Assert.assertEquals("OK", joinResponse.get("type").getAsString());
            Assert.assertEquals("USER_IN_ROOM", joinResponse.get("message").getAsString());
            Assert.assertEquals("IA", joinResponse.getAsJsonObject("data").get("room").getAsString());

            JsonObject messageInfo = new JsonObject();
            messageInfo.addProperty("room", "IA");
            messageInfo.addProperty("content", "hello from the network test");
            client.send(request("SEND_ROOM_MSG", messageInfo));

            JsonObject sendResponse = client.readJson();
            Assert.assertEquals("OK", sendResponse.get("type").getAsString());
            Assert.assertEquals("MESSAGE_SENT", sendResponse.get("message").getAsString());

            JsonObject broadcast = client.readJson();
            Assert.assertEquals("ROOM_MESSAGE", broadcast.get("type").getAsString());
            Assert.assertEquals("IA", broadcast.get("room").getAsString());
            Assert.assertEquals(username, broadcast.get("sender").getAsString());
            Assert.assertEquals("hello from the network test", broadcast.get("content").getAsString());
        }
    }

    @Test
    public void testPrivateMessageDeliveryOverSocket() throws Exception {
        Server server = new Server();
        String sender = unique("net_priv_sender");
        String receiver = unique("net_priv_receiver");

        try (SocketTestClient senderClient = SocketTestClient.connect(server);
             SocketTestClient receiverClient = SocketTestClient.connect(server)) {

            authenticate(senderClient, sender);
            authenticate(receiverClient, receiver);

            JsonObject privInfo = new JsonObject();
            privInfo.addProperty("receiver", receiver);
            privInfo.addProperty("content", "private hello over sockets");
            senderClient.send(request("SEND_PRIV_MSG", privInfo));

            JsonObject sendResponse = senderClient.readJson();
            Assert.assertEquals("OK", sendResponse.get("type").getAsString());
            Assert.assertEquals("MESSAGE_SENT", sendResponse.get("message").getAsString());

            JsonObject delivered = receiverClient.readJson();
            Assert.assertEquals("PRIVATE_MESSAGE", delivered.get("type").getAsString());
            Assert.assertEquals(sender, delivered.get("sender").getAsString());
            Assert.assertEquals(receiver, delivered.get("receiver").getAsString());
            Assert.assertEquals("private hello over sockets", delivered.get("content").getAsString());
        }
    }

    @Test
    public void testHeartbeatAcksAndMarksClientConnected() throws Exception {
        Server server = new Server();
        String username = unique("net_heartbeat_user");

        try (SocketTestClient client = SocketTestClient.connect(server)) {
            authenticate(client, username);

            JsonObject heartbeatInfo = new JsonObject();
            client.send(request("HEARTBEAT", heartbeatInfo));

            JsonObject heartbeatResponse = client.readJson();
            Assert.assertEquals("OK", heartbeatResponse.get("type").getAsString());
            Assert.assertEquals("HEARTBEAT_ACK", heartbeatResponse.get("message").getAsString());
            Assert.assertTrue(server.isUserConnected(username));
            Assert.assertNotNull(server.getClientHandler(username));
        }
    }

    @Test
    public void testPrivateMessageToRegisteredButOfflineUserReturnsUserNotConnected() throws Exception {
        Server server = new Server();
        String sender = unique("net_priv_sender_offline");
        String offlineReceiver = unique("net_priv_offline_receiver");

        try (SocketTestClient senderClient = SocketTestClient.connect(server)) {
            authenticate(senderClient, sender);

            server.registerUser(offlineReceiver);

            JsonObject privInfo = new JsonObject();
            privInfo.addProperty("receiver", offlineReceiver);
            privInfo.addProperty("content", "hello offline user");
            senderClient.send(request("SEND_PRIV_MSG", privInfo));

            JsonObject response = senderClient.readJson();
            Assert.assertEquals("ERROR", response.get("type").getAsString());
            Assert.assertEquals("USER_NOT_CONNECTED", response.get("message").getAsString());
        }
    }

    private static void authenticate(SocketTestClient client, String username) throws Exception {
        JsonObject registerInfo = new JsonObject();
        registerInfo.addProperty("user", username);
        client.send(request("REGISTER", registerInfo));

        JsonObject registerResponse = client.readJson();
        Assert.assertEquals("OK", registerResponse.get("type").getAsString());
        Assert.assertEquals("USER_REGISTERED", registerResponse.get("message").getAsString());
        long key = registerResponse.getAsJsonObject("data").get("key").getAsLong();

        JsonObject loginInfo = new JsonObject();
        loginInfo.addProperty("user", username);
        loginInfo.addProperty("key", key);
        client.send(request("LOGIN", loginInfo));

        JsonObject loginResponse = client.readJson();
        Assert.assertEquals("OK", loginResponse.get("type").getAsString());
        Assert.assertEquals("LOGIN_SUCCESS", loginResponse.get("message").getAsString());
    }

    private static String request(String command, JsonObject information) {
        JsonObject json = new JsonObject();
        json.addProperty("command", command);
        json.add("information", information);
        return json.toString();
    }

    private static String unique(String prefix) {
        return prefix + "_" + System.nanoTime() + "_" + (int) (Math.random() * 10000);
    }

    private static JsonObject parse(String line) {
        return JsonParser.parseString(line).getAsJsonObject();
    }

    private static final class SocketTestClient implements AutoCloseable {
        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private final Thread handlerThread;
        private final AtomicReference<Throwable> handlerFailure;

        private SocketTestClient(Socket socket, Thread handlerThread, AtomicReference<Throwable> handlerFailure) throws Exception {
            this.socket = socket;
            this.socket.setSoTimeout(3000);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.handlerThread = handlerThread;
            this.handlerFailure = handlerFailure;
        }

        static SocketTestClient connect(Server server) throws Exception {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            AtomicReference<Throwable> handlerFailure = new AtomicReference<>();

            Socket clientSocket = new Socket("127.0.0.1", port);
            Socket handlerSocket = serverSocket.accept();
            serverSocket.close();

            ClientHandler handler = new ClientHandler(handlerSocket, server);
            Thread handlerThread = new Thread(() -> {
                try {
                    handler.run();
                } catch (Throwable t) {
                    handlerFailure.set(t);
                }
            }, "client-handler-test");
            handlerThread.setDaemon(true);
            handlerThread.start();

            return new SocketTestClient(clientSocket, handlerThread, handlerFailure);
        }

        void send(String json) {
            writer.println(json);
            writer.flush();
        }

        JsonObject readJson() throws Exception {
            String line = reader.readLine();
            Assert.assertNotNull("Expected a JSON response from the server", line);
            return parse(line);
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



