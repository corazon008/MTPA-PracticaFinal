package org.example.server.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a message in the system.
 * Messages can be sent to a room or as private messages between users.
 */
public class Message {

    private static final int MAX_MESSAGE_LENGTH = 190;

    private String sender;
    private String receiver;  // null for room messages
    private String room;       // null for private messages
    private String content;
    private LocalDateTime timestamp;

    /**
     * Constructs a new Message.
     *
     * @param sender   The username of the sender
     * @param content  The message content
     * @param room     The room name (null for private messages)
     * @param receiver The receiver username (null for room messages)
     */
    public Message(String sender, String content, String room, String receiver) {
        this.sender = sender;
        this.content = content;
        this.room = room;
        this.receiver = receiver;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Validates the message content length.
     *
     * @return true if the message is valid (<=190 chars), false otherwise
     */
    public boolean isValid() {
        return content != null && content.length() <= MAX_MESSAGE_LENGTH;
    }

    /**
     * Gets the maximum allowed message length.
     *
     * @return the maximum message length
     */
    public static int getMaxMessageLength() {
        return MAX_MESSAGE_LENGTH;
    }

    @Override
    public String toString() {
        if (room != null) {
            return "[" + timestamp + "] " + sender + " in " + room + ": " + content;
        } else {
            return "[" + timestamp + "] " + sender + " -> " + receiver + ": " + content;
        }
    }
}

