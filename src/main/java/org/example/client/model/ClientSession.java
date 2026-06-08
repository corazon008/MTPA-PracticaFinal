package org.example.client.model;

/**
 * Holds the local state of the client application.
 * Stores authentication details and current navigation context.
 */
public class ClientSession {
    private String username;
    private long accessKey;
    private boolean authenticated;
    private String currentRoom;

    /**
     * Constructs a new, unauthenticated client session.
     */
    public ClientSession() {
        this.authenticated = false;
        this.accessKey = -1;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(long accessKey) {
        this.accessKey = accessKey;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     * Clears the current session data, useful for handling disconnections or logouts.
     */
    public void clearSession() {
        this.username = null;
        this.accessKey = -1;
        this.authenticated = false;
        this.currentRoom = null;
    }
}