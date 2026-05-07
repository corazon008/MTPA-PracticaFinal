package org.example.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a user in the messaging system.
 * Each user has a unique username and an auto-generated numeric key for authentication.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private long key;
    private LocalDateTime registrationDate;
    private LocalDateTime lastHeartbeat;
    private boolean connected;

    /**
     * Constructs a new User.
     *
     * @param username The unique username
     * @param key The auto-generated numeric key for authentication
     */
    public User(String username, long key) {
        this.username = username;
        this.key = key;
        this.registrationDate = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
        this.connected = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", connected=" + connected +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}

