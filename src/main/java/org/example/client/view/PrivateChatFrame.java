package org.example.client.view;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

/**
 * Graphical interface for a one-on-one private chat session.
 * The conversation context is closed and discarded when the window is closed.
 */
public class PrivateChatFrame extends JFrame {
    private final String targetUser;
    private JTextArea chatArea;
    private JTextField messageInput;
    private JButton sendButton;
    private JLabel charCountLabel;

    /**
     * Constructs the PrivateChatFrame for a specific target user.
     *
     * @param targetUser The username of the remote recipient
     */
    public PrivateChatFrame(String targetUser) {
        this.targetUser = targetUser;
        setTitle("Chat Privado con " + targetUser);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(5, 5));

        // Message display text area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Bottom panel for composition
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        messageInput = new JTextField();

        // Enforce the 190 characters length limit rule
        AbstractDocument messageDocument = (AbstractDocument) messageInput.getDocument();
        messageDocument.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if ((fb.getDocument().getLength() + string.length()) <= 190) {
                    super.insertString(fb, offset, string, attr);
                    updateCharCount();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                if ((fb.getDocument().getLength() - length + text.length()) <= 190) {
                    super.replace(fb, offset, length, text, attrs);
                    updateCharCount();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
                updateCharCount();
            }
        });

        bottomPanel.add(messageInput, BorderLayout.CENTER);

        // Actions panel with counter and button
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        charCountLabel = new JLabel("0 / 190");
        actionsPanel.add(charCountLabel);
        sendButton = new JButton("Enviar");
        actionsPanel.add(sendButton);

        bottomPanel.add(actionsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Updates the text length indicator dynamically.
     */
    private void updateCharCount() {
        int length = messageInput.getText().length();
        charCountLabel.setText(length + " / 190");
    }

    // Interaction Data Getters
    public String getTargetUser() {
        return targetUser;
    }

    public String getMessageText() {
        return messageInput.getText().trim();
    }

    // UI State Manipulators
    public void clearMessageInput() {
        messageInput.setText("");
        charCountLabel.setText("0 / 190");
    }

    public void appendMessage(String sender, String content) {
        chatArea.append("[" + sender + "]: " + content + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // Controller Action Listeners Wiring
    public void addSendButtonListener(ActionListener listener) {
        sendButton.addActionListener(listener);
        messageInput.addActionListener(listener); // Support hitting enter to send
    }

    public void addWindowCloseListener(WindowAdapter adapter) {
        addWindowListener(adapter);
    }
}