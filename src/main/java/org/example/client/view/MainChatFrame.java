package org.example.client.view;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Main graphical interface for the chat application.
 * Manages room selection, message view, historical message loading, and private chat initiation.
 */
public class MainChatFrame extends JFrame {
    private JList<String> roomList;
    private JTextArea chatArea;
    private JTextField messageInput;
    private JButton sendButton;
    private JLabel charCountLabel;
    private JTextField dateInput;
    private JButton loadHistoryButton;
    private JButton privateChatButton;
    private JTextField privateUserInput;

    /**
     * Constructs the MainChatFrame and sets up all graphical components.
     */
    public MainChatFrame() {
        setTitle("Sistema de Mensajería");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main layout definition
        setLayout(new BorderLayout(5, 5));

        // Left Panel: Predefined Rooms and Private Chat Triggers
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Salones"));

        String[] predefinedRooms = {"IA", "Deportes", "Therian", "Manga", "UEMC"};
        roomList = new JList<>(predefinedRooms);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setSelectedIndex(0);
        leftPanel.add(new JScrollPane(roomList), BorderLayout.CENTER);

        // Subsection for launching 1-to-1 private messages
        JPanel privatePanel = new JPanel(new GridLayout(3, 1, 5, 5));
        privatePanel.setBorder(BorderFactory.createTitledBorder("Mensaje Privado"));
        privateUserInput = new JTextField();
        privatePanel.add(new JLabel("Nombre del usuario:"));
        privatePanel.add(privateUserInput);
        privateChatButton = new JButton("Iniciar Chat Privado");
        privatePanel.add(privateChatButton);
        leftPanel.add(privatePanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // Center Panel: Chat History and Message Display Area
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        // Upper bar for historical messages queries
        JPanel historyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historyPanel.add(new JLabel("Buscar por fecha (AAAA-MM-DD):"));
        dateInput = new JTextField(10);
        historyPanel.add(dateInput);
        loadHistoryButton = new JButton("Cargar Historial");
        historyPanel.add(loadHistoryButton);
        centerPanel.add(historyPanel, BorderLayout.NORTH);

        // Scrollable message display log
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        centerPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Bottom Panel: Message composition and length validation
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        messageInput = new JTextField();

        // DocumentFilter implementation to enforce the strict 190 characters rule
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

        // Action panel with character counter and send button
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        charCountLabel = new JLabel("0 / 190");
        actionsPanel.add(charCountLabel);
        sendButton = new JButton("Enviar");
        actionsPanel.add(sendButton);
        bottomPanel.add(actionsPanel, BorderLayout.EAST);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Updates the dynamic counter label based on input length.
     */
    private void updateCharCount() {
        int length = messageInput.getText().length();
        charCountLabel.setText(length + " / 190");
    }

    // Interaction Data Getters
    public String getSelectedRoom() {
        return roomList.getSelectedValue();
    }

    public String getMessageText() {
        return messageInput.getText().trim();
    }

    public String getHistoryDate() {
        return dateInput.getText().trim();
    }

    public String getPrivateTargetUser() {
        return privateUserInput.getText().trim();
    }

    // UI State Manipulators
    public void clearMessageInput() {
        messageInput.setText("");
        charCountLabel.setText("0 / 190");
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void clearChatArea() {
        chatArea.setText("");
    }

    // Controller Action Listeners Wiring
    public void addSendButtonListener(ActionListener listener) {
        sendButton.addActionListener(listener);
        messageInput.addActionListener(listener); // Allows hitting enter to send
    }

    public void addRoomSelectionListener(javax.swing.event.ListSelectionListener listener) {
        roomList.addListSelectionListener(listener);
    }

    public void addLoadHistoryListener(ActionListener listener) {
        loadHistoryButton.addActionListener(listener);
    }

    public void addPrivateChatListener(ActionListener listener) {
        privateChatButton.addActionListener(listener);
    }
}