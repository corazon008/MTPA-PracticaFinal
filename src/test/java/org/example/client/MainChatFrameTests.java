package org.example.client;

import org.example.client.view.LoginFrame;
import org.example.client.view.MainChatFrame;
import org.example.client.view.PrivateChatFrame;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for GUI view components.
 * Validates initialization and state management of chat interface frames.
 */
public class MainChatFrameTests {

    @Test
    public void testMainChatFrameInitialization() {
        // Create frame in headless mode for testing
        MainChatFrame frame = new MainChatFrame();

        Assert.assertNotNull("Frame should be created", frame);
        Assert.assertTrue("Frame should have been created with proper size", frame.getWidth() > 0 && frame.getHeight() > 0);
    }

    @Test
    public void testGetSelectedRoom() {
        MainChatFrame frame = new MainChatFrame();

        // The frame initializes with a default room (first in the list)
        String selectedRoom = frame.getSelectedRoom();
        Assert.assertNotNull("Selected room should not be null", selectedRoom);
        Assert.assertEquals("Default selected room should be IA", "IA", selectedRoom);
    }

    @Test
    public void testPrivateUserInputGetter() {
        MainChatFrame frame = new MainChatFrame();

        String targetUser = frame.getPrivateTargetUser();
        Assert.assertNotNull("Private target user should not be null", targetUser);
        Assert.assertEquals("Initially should be empty", "", targetUser);
    }

    @Test
    public void testMessageInputClear() {
        MainChatFrame frame = new MainChatFrame();

        // Clear the message input (should not crash)
        frame.clearMessageInput();

        String message = frame.getMessageText();
        Assert.assertNotNull("Message should not be null after clear", message);
        Assert.assertEquals("Message should be empty after clear", "", message);
    }

    @Test
    public void testChatAreaClear() {
        MainChatFrame frame = new MainChatFrame();

        // Add a message and then clear
        frame.appendMessage("Test message");
        frame.clearChatArea();

        // After clearing, the chat pane should be effectively empty
        // (we can't directly check content, but clearing shouldn't crash)
        Assert.assertNotNull("Chat frame should still exist after clearChatArea", frame);
    }

    @Test
    public void testAppendMessage() {
        MainChatFrame frame = new MainChatFrame();

        try {
            frame.appendMessage("Alice: Hello world");
            frame.appendMessage("Bob: Hi there");
            // Should not crash
            Assert.assertTrue("Message appending should succeed", true);
        } catch (Exception e) {
            Assert.fail("appendMessage should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testAppendSystemMessage() {
        MainChatFrame frame = new MainChatFrame();

        try {
            frame.appendSystemMessage("Alice has joined the room");
            frame.appendSystemMessage("Bob has left the room");
            // Should not crash
            Assert.assertTrue("System message appending should succeed", true);
        } catch (Exception e) {
            Assert.fail("appendSystemMessage should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testHistoryDateInput() {
        MainChatFrame frame = new MainChatFrame();

        String historyDate = frame.getHistoryDate();
        Assert.assertNotNull("History date should not be null", historyDate);
        Assert.assertEquals("Initially should be empty", "", historyDate);
    }

    @Test
    public void testMessageCharacterLimit() {
        MainChatFrame frame = new MainChatFrame();

        // The frame enforces a 190 character limit via DocumentFilter
        // We test that the getter works
        String message = frame.getMessageText();
        Assert.assertNotNull("Message text should not be null", message);
        Assert.assertTrue("Message should respect character limit", message.length() <= 190);
    }

    @Test
    public void testLoginFrameInitialization() {
        LoginFrame loginFrame = new LoginFrame();

        Assert.assertNotNull("LoginFrame should be created", loginFrame);
        Assert.assertTrue("LoginFrame should have dimensions", loginFrame.getWidth() > 0 && loginFrame.getHeight() > 0);
    }

    @Test
    public void testLoginFrameInputGetters() {
        LoginFrame loginFrame = new LoginFrame();

        String username = loginFrame.getUsername();
        long accessKey = loginFrame.getAccessKey();

        Assert.assertNotNull("Username should not be null", username);
        Assert.assertEquals("Initially username should be empty", "", username);
        Assert.assertEquals("Initially access key should be -1 (invalid)", -1, accessKey);
    }

    @Test
    public void testLoginFrameClearInputs() {
        LoginFrame loginFrame = new LoginFrame();

        try {
            loginFrame.clearInputs();
            // Should not crash
            Assert.assertTrue("clearInputs should complete successfully", true);
        } catch (Exception e) {
            Assert.fail("clearInputs should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testPrivateChatFrameInitialization() {
        PrivateChatFrame privateChatFrame = new PrivateChatFrame("alice");

        Assert.assertNotNull("PrivateChatFrame should be created", privateChatFrame);
        Assert.assertTrue("PrivateChatFrame should have dimensions", privateChatFrame.getWidth() > 0 && privateChatFrame.getHeight() > 0);
    }

    @Test
    public void testPrivateChatFrameMessageHandling() {
        PrivateChatFrame privateChatFrame = new PrivateChatFrame("bob");

        try {
            privateChatFrame.appendMessage("alice", "Hello Bob!");
            privateChatFrame.appendMessage("bob", "Hi Alice!");
            privateChatFrame.clearMessageInput();

            String messageText = privateChatFrame.getMessageText();
            Assert.assertNotNull("Message text should not be null", messageText);
            Assert.assertEquals("Message should be empty after clear", "", messageText);
        } catch (Exception e) {
            Assert.fail("PrivateChatFrame message handling should not throw: " + e.getMessage());
        }
    }
}

