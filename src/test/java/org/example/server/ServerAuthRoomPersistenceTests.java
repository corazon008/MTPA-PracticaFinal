package org.example.server;

import org.example.model.User;
import org.example.model.Room;
import org.example.persistence.PersistenceManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ServerAuthRoomPersistenceTests {

    @Before
    public void setup() {
        String dir = "target/test-data/ServerAuthRoomPersistenceTests";
        PersistenceManager.setDataDir(dir);
        PersistenceManager.initialize();
    }
    @Test
    public void testRegisterAndLoginSuccess() throws Exception {
        Server server = new Server();
        String username = "user_" + System.nanoTime() + "_" + (int)(Math.random()*10000);

        long key = server.registerUser(username);
        Assert.assertTrue(key >= 0);

        // login should succeed
        org.example.model.User u = server.loginUser(username, key);
        Assert.assertNotNull(u);
        Assert.assertEquals(username, u.getUsername());
    }

    @Test
    public void testRegisterDuplicateThrows() throws Exception {
        Server server = new Server();
        String username = "dup_" + System.nanoTime() + "_" + (int)(Math.random()*10000);

        server.registerUser(username);

        try {
            server.registerUser(username);
            Assert.fail("Expected USERNAME_EXISTS exception");
        } catch (Exception e) {
            Assert.assertEquals("USERNAME_EXISTS", e.getMessage());
        }
    }

    @Test
    public void testLoginInvalidKeyThrows() throws Exception {
        Server server = new Server();
        String username = "li_" + System.nanoTime() + "_" + (int)(Math.random()*10000);

        long key = server.registerUser(username);

        try {
            server.loginUser(username, key + 1);
            Assert.fail("Expected INVALID_CREDENTIALS exception");
        } catch (Exception e) {
            Assert.assertEquals("INVALID_CREDENTIALS", e.getMessage());
        }
    }

    @Test
    public void testJoinLeaveRoomUpdatesCounts() throws Exception {
        Server server = new Server();
        String username = "jr_" + System.nanoTime() + "_" + (int)(Math.random()*10000);

        server.registerUser(username);

        // add to predefined room IA
        server.addUserToRoom(username, "IA");
        Room room = server.getRoom("IA");
        Assert.assertNotNull(room);
        Assert.assertTrue(room.hasUser(username));
        int countAfterJoin = room.getUserCount();
        Assert.assertTrue(countAfterJoin >= 1);

        // remove and check
        server.removeUserFromRoom(username, "IA");
        Assert.assertFalse(room.hasUser(username));
    }

    @Test
    public void testPersistenceSaveAndLoadUsers() throws Exception {
        // Build a small users map and persist it, then reload and assert
        String username = "persist_" + System.nanoTime() + "_" + (int)(Math.random()*10000);
        Server server = new Server();
        server.registerUser(username);

        Map<String, org.example.model.User> users = server.getRegisteredUsers();
        // Save explicitly
        PersistenceManager.saveUsers(users);

        Map<String, org.example.model.User> loaded = PersistenceManager.loadUsers();
        Assert.assertTrue("Saved user should be present after reload", loaded.containsKey(username));
    }
}

