package org.example.client.view;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for handling system and network notifications.
 * Provides static methods to display consistent pop-up alerts across the application.
 */
public class NotificationUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private NotificationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Displays a popup notification when a new user joins the current room.
     *
     * @param parentComponent The parent frame for positioning the dialog
     * @param username        The name of the user who joined
     * @param room            The name of the room
     */
    public static void showUserJoinedNotification(Component parentComponent, String username, String room) {
        String message = "El usuario '" + username + "' ha entrado en el salón '" + room + "'.";
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                "Nuevo usuario conectado",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Displays an error notification for network issues or invalid operations.
     *
     * @param parentComponent The parent frame for positioning the dialog
     * @param errorMessage    The specific error message to display
     */
    public static void showErrorNotification(Component parentComponent, String errorMessage) {
        JOptionPane.showMessageDialog(
                parentComponent,
                errorMessage,
                "Error del Sistema",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Displays a generic information notification.
     *
     * @param parentComponent The parent frame for positioning the dialog
     * @param message         The information text
     * @param title           The title of the dialog window
     */
    public static void showInfoNotification(Component parentComponent, String message, String title) {
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Displays a warning notification, useful for server maintenance alerts.
     *
     * @param parentComponent The parent frame for positioning the dialog
     * @param message         The warning text
     */
    public static void showWarningNotification(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                "Advertencia",
                JOptionPane.WARNING_MESSAGE
        );
    }
}