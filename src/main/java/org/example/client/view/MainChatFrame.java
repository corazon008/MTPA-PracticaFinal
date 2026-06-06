package org.example.client.view;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Main graphical interface for the chat application.
 * Manages room selection, message view, historical message loading, and private chat initiation.
 */
public class MainChatFrame extends JFrame {
    private JList<String> roomList;
    private JTextPane chatPane;
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

        setLayout(new BorderLayout(5, 5));

        // Left Panel: Predefined Rooms and Private Chat Triggers
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Salones"));

        String[] predefinedRooms = {"IA", "Deportes", "Therian", "Manga", "UEMC"};
        roomList = new JList<>(predefinedRooms);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setSelectedIndex(0);
        leftPanel.add(new JScrollPane(roomList), BorderLayout.CENTER);

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

        JPanel historyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historyPanel.add(new JLabel("Buscar por fecha (AAAA-MM-DD):"));
        dateInput = new JTextField(10);
        historyPanel.add(dateInput);
        loadHistoryButton = new JButton("Cargar Historial");
        historyPanel.add(loadHistoryButton);
        centerPanel.add(historyPanel, BorderLayout.NORTH);

        // Scrollable message display log using JTextPane for rich text
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        centerPanel.add(new JScrollPane(chatPane), BorderLayout.CENTER);

        // Bottom Panel: Message composition and length validation
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        messageInput = new JTextField();

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

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        charCountLabel = new JLabel("0 / 190");
        actionsPanel.add(charCountLabel);
        sendButton = new JButton("Enviar");
        actionsPanel.add(sendButton);
        bottomPanel.add(actionsPanel, BorderLayout.EAST);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void updateCharCount() {
        int length = messageInput.getText().length();
        charCountLabel.setText(length + " / 190");
    }

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

    public void clearMessageInput() {
        messageInput.setText("");
        charCountLabel.setText("0 / 190");
    }

    public void clearChatArea() {
        chatPane.setText("");
    }

    /**
     * Appends a standard user message to the chat in regular font.
     *
     * @param message The standard message to display
     */
    public void appendMessage(String message) {
        appendToPane(message + "\n", Color.BLACK, false, 12);
    }

    /**
     * Appends a system notification in a discreet, Discord-like style.
     * Uses gray color, italics, and a slightly smaller font.
     *
     * @param message The system event to display
     */
    public void appendSystemMessage(String message) {
        appendToPane(" -> " + message + "\n", Color.GRAY, true, 11);
    }

    /**
     * Helper method to insert styled text at the end of the JTextPane document.
     */
    private void appendToPane(String msg, Color color, boolean italic, int fontSize) {
        StyledDocument doc = chatPane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setItalic(style, italic);
        StyleConstants.setFontSize(style, fontSize);

        try {
            doc.insertString(doc.getLength(), msg, style);
            // Auto-scroll to the bottom as new messages arrive
            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            System.err.println("Error appending text to chat: " + e.getMessage());
        }
    }

    public void addSendButtonListener(ActionListener listener) {
        sendButton.addActionListener(listener);
        messageInput.addActionListener(listener);
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