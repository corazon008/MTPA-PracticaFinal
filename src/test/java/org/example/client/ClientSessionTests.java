package org.example.client;

import org.example.client.model.ClientSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ClientSession model.
 * Validates local client state management (authentication, room selection, user data).
 */
public class ClientSessionTests {

    private ClientSession session;

    @Before
    public void setup() {
        session = new ClientSession();
    }

    @Test
    public void testSessionInitialization() {
        Assert.assertFalse("New session should not be authenticated", session.isAuthenticated());
        Assert.assertEquals("Access key should be -1 by default", -1, session.getAccessKey());
        Assert.assertNull("Username should be null initially", session.getUsername());
        Assert.assertNull("Current room should be null initially", session.getCurrentRoom());
    }

    @Test
    public void testSetUsernameAndAccessKey() {
        String testUsername = "alice";
        long testKey = 12345L;

        session.setUsername(testUsername);
        session.setAccessKey(testKey);

        Assert.assertEquals("Username should be set", testUsername, session.getUsername());
        Assert.assertEquals("Access key should be set", testKey, session.getAccessKey());
    }

    @Test
    public void testAuthenticationState() {
        Assert.assertFalse("Session should not be authenticated initially", session.isAuthenticated());

        session.setAuthenticated(true);
        Assert.assertTrue("Session should be authenticated after setAuthenticated(true)", session.isAuthenticated());

        session.setAuthenticated(false);
        Assert.assertFalse("Session should not be authenticated after setAuthenticated(false)", session.isAuthenticated());
    }

    @Test
    public void testCurrentRoomSelection() {
        Assert.assertNull("Current room should be null initially", session.getCurrentRoom());

        session.setCurrentRoom("IA");
        Assert.assertEquals("Current room should be IA", "IA", session.getCurrentRoom());

        session.setCurrentRoom("Deportes");
        Assert.assertEquals("Current room should be Deportes", "Deportes", session.getCurrentRoom());
    }

    @Test
    public void testClearSession() {
        // Set up a session with data
        session.setUsername("bob");
        session.setAccessKey(9999L);
        session.setAuthenticated(true);
        session.setCurrentRoom("Manga");

        // Clear the session
        session.clearSession();

        // Verify everything is cleared
        Assert.assertNull("Username should be null after clearSession", session.getUsername());
        Assert.assertEquals("Access key should be -1 after clearSession", -1, session.getAccessKey());
        Assert.assertFalse("Session should not be authenticated after clearSession", session.isAuthenticated());
        Assert.assertNull("Current room should be null after clearSession", session.getCurrentRoom());
    }

    @Test
    public void testSessionStateConsistency() {
        // Simulate a full authentication and room join workflow
        session.setUsername("charlie");
        session.setAccessKey(54321L);
        session.setAuthenticated(true);
        session.setCurrentRoom("Therian");

        // Verify all states are consistent
        Assert.assertEquals("charlie", session.getUsername());
        Assert.assertEquals(54321L, session.getAccessKey());
        Assert.assertTrue(session.isAuthenticated());
        Assert.assertEquals("Therian", session.getCurrentRoom());

        // Simulate logout
        session.clearSession();
        Assert.assertNull("After logout, username should be null", session.getUsername());
        Assert.assertFalse("After logout, should not be authenticated", session.isAuthenticated());
    }
}

