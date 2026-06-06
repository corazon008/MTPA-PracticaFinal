package org.example.client;

import org.example.client.controller.ChatController;
import org.example.client.model.ClientSession;
import org.example.client.network.ServerConnection;
import org.example.client.view.LoginFrame;
import org.example.client.view.MainChatFrame;

import javax.swing.*;

/**
 * Main entry point for the client chat application.
 * Initializes the user interface components and sets up the MVC structure.
 */
public class ClientApplication {

    public static void main(String[] args) {
        // Set system look and feel for a native UI presentation
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Warning: Unable to set native look and feel: " + e.getMessage());
        }

        // Start the graphical application on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Initialize the core architectural components
            ServerConnection connection = new ServerConnection();
            ClientSession session = new ClientSession();
            LoginFrame loginFrame = new LoginFrame();
            MainChatFrame mainChatFrame = new MainChatFrame();

            // The controller links the views, the local model, and the network layer
            ChatController controller = new ChatController(connection, session, loginFrame, mainChatFrame);

            // Launch the initial authentication window
            loginFrame.setVisible(true);
        });
    }
}