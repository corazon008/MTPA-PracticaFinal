package org.example.client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Graphical interface for user authentication and registration.
 * Provides fields for username and numeric key input.
 */
public class LoginFrame extends JFrame {
    private JTextField usernameInput;
    private JTextField keyInput;
    private JButton loginButton;
    private JButton registerButton;

    /**
     * Constructs the LoginFrame and organizes all visual subcomponents.
     */
    public LoginFrame() {
        setTitle("Conexión");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main layout with margin spacing
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Form panel for inputs
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        formPanel.add(new JLabel("Usuario:"));
        usernameInput = new JTextField();
        formPanel.add(usernameInput);

        formPanel.add(new JLabel("Clave numérica:"));
        keyInput = new JTextField();
        formPanel.add(keyInput);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Action panel for buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        loginButton = new JButton("Iniciar Sesión");
        registerButton = new JButton("Registrarse");

        actionPanel.add(registerButton);
        actionPanel.add(loginButton);

        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    // Input Data Getters
    public String getUsername() {
        return usernameInput.getText().trim();
    }

    /**
     * Retrieves the numeric key input.
     * * @return the long value of the key, or -1 if the input is empty or invalid.
     */
    public long getAccessKey() {
        String keyText = keyInput.getText().trim();
        if (keyText.isEmpty()) {
            return -1;
        }
        try {
            return Long.parseLong(keyText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // UI Status Manipulators
    public void clearInputs() {
        usernameInput.setText("");
        keyInput.setText("");
    }

    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // Controller Action Listeners Wiring
    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
        keyInput.addActionListener(listener); // Allows hitting enter in the key field to log in
    }

    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(listener);
        usernameInput.addActionListener(listener); // Allows hitting enter in the username field to register
    }
}