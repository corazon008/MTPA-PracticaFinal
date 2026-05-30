package org.example.server;

import org.example.model.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Unit tests for server message handling and routing.
 */
public class ServerMessageTests {

    @Before
    public void setup() {
        String dir = "target/test-data/ServerMessageTests";
        org.example.persistence.PersistenceManager.setDataDir(dir);
        org.example.persistence.PersistenceManager.initialize();
    }

    @Test
    public void testMessageTooLongThrows() throws Exception {
        Server server = new Server();

        // Ensure user exists and is in room (use unique username to avoid persistence collisions)
        String user = "alice_" + System.nanoTime() + "_" + (int)(Math.random()*10000);
        server.registerUser(user);
        server.addUserToRoom(user, "IA");

        // Build overly long content
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('x');
        String content = sb.toString();

        Message msg = new Message(user, content, "IA", null);

        try {
            server.storeRoomMessage(msg);
            Assert.fail("Expected an exception for too long message");
        } catch (Exception e) {
            Assert.assertEquals("MESSAGE_TOO_LONG", e.getMessage());
        }
    }

    @Test
    public void testNotInRoomThrows() throws Exception {
        Server server = new Server();

        String user = "bob_" + System.nanoTime() + "_" + (int)(Math.random()*10000);
        server.registerUser(user);
        // do NOT add bob to the room

        Message msg = new Message(user, "hello", "IA", null);

        try {
            server.storeRoomMessage(msg);
            Assert.fail("Expected NOT_IN_ROOM exception");
        } catch (Exception e) {
            Assert.assertEquals("NOT_IN_ROOM", e.getMessage());
        }
    }

    @Test
    public void testSendPrivateMessageUserNotConnected() throws Exception {
        Server server = new Server();

        String alice = "alice2_" + System.nanoTime() + "_" + (int)(Math.random()*10000);
        String bob = "bob2_" + System.nanoTime() + "_" + (int)(Math.random()*10000);
        server.registerUser(alice);
        server.registerUser(bob);

        Message msg = new Message(alice, "hi bob", null, bob);

        try {
            server.sendPrivateMessage(msg);
            Assert.fail("Expected USER_NOT_CONNECTED exception");
        } catch (Exception e) {
            Assert.assertEquals("USER_NOT_CONNECTED", e.getMessage());
        }
    }

    @Test
    public void testSendPrivateMessageSuccessDelivers() throws Exception {
        Server server = new Server();

        String alice = "alice3_" + System.nanoTime() + "_" + (int)(Math.random()*10000);
        String bob = "bob3_" + System.nanoTime() + "_" + (int)(Math.random()*10000);
        server.registerUser(alice);
        server.registerUser(bob);

        // Set up a pair of sockets so we can observe what the server sends to the receiver
        try (ServerSocket ss = new ServerSocket(0)) {
            int port = ss.getLocalPort();

            // Connect client side socket
            Socket clientSocket = new Socket("127.0.0.1", port);

            // Accept server side socket (to pass into ClientHandler)
            Socket handlerSocket = ss.accept();

            // Create ClientHandler for bob using the accepted socket
            ClientHandler bobHandler = new ClientHandler(handlerSocket, server);

            // Register handler as connected client for bob
            server.connectClient(bob, bobHandler);

            // Prepare reader on the client side to capture messages
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

            // Send private message from alice to bob
            Message msg = new Message(alice, "hello bob!", null, bob);
            server.sendPrivateMessage(msg);

            // Read a line sent to bob and assert it contains expected fields
            String delivered = reader.readLine();
            Assert.assertNotNull("Expected a delivered message", delivered);
            Assert.assertTrue(delivered.contains("PRIVATE_MESSAGE"));
            Assert.assertTrue(delivered.contains("hello bob!"));

            // Clean up sockets
            reader.close();
            clientSocket.close();
            handlerSocket.close();
        }
    }
}


