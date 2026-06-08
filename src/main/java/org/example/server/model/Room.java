package org.example.server.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a chat room in the messaging system.
 * Each room has a name, list of connected users, and message history.
 */
public class Room {
    private String name;
    private List<Message> messages;
    private Set<String> connectedUsers;
    private long messageCount;

    /**
     * Constructs a new Room.
     *
     * @param name The room name
     */
    public Room(String name) {
        this.name = name;
        this.messages = new CopyOnWriteArrayList<>();
        this.connectedUsers = Collections.synchronizedSet(new HashSet<>());
        this.messageCount = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public synchronized void addMessage(Message message) {
        messages.add(message);
        messageCount++;
    }

    public Set<String> getConnectedUsers() {
        return new HashSet<>(connectedUsers);
    }

    public synchronized void addUser(String username) {
        connectedUsers.add(username);
    }

    public synchronized void removeUser(String username) {
        connectedUsers.remove(username);
    }

    public synchronized boolean hasUser(String username) {
        return connectedUsers.contains(username);
    }

    public synchronized int getUserCount() {
        return connectedUsers.size();
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    /**
     * Gets messages from the last 24 hours.
     *
     * @return list of messages from the last day
     */
    public List<Message> getMessagesFromLastDay() {
        java.time.LocalDateTime oneDayAgo = java.time.LocalDateTime.now().minusDays(1);
        List<Message> result = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getTimestamp().isAfter(oneDayAgo)) {
                result.add(msg);
            }
        }
        return result;
    }

    /**
     * Gets messages from a specific date.
     *
     * @param date the date in format "yyyy-MM-dd"
     * @return list of messages from that date
     */
    public List<Message> getMessagesByDate(String date) {
        java.time.LocalDate targetDate = java.time.LocalDate.parse(date);
        List<Message> result = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getTimestamp().toLocalDate().equals(targetDate)) {
                result.add(msg);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", users=" + connectedUsers.size() +
                ", messages=" + messageCount +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room room = (Room) obj;
        return name.equals(room.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

