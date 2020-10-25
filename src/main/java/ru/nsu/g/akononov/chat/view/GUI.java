package ru.nsu.g.akononov.chat.view;

import ru.nsu.g.akononov.chat.model.Node;
import ru.nsu.g.akononov.chat.model.message.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.util.Date;

public class GUI implements Observer {
    private final Style userMessageStyle;
    private final Style ownMessageStyle;

    private final Node node;

    private final JFrame appFrame = new JFrame();

    private final JTextField messageField = new JTextField("");
    private final JPanel bottomsPanel = new JPanel(new BorderLayout());
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JTextPane messagesLog = new JTextPane();


    public GUI(Node node) {
        this.node = node;
        EventQueue.invokeLater(this::setSwingSettings);

        userMessageStyle = messagesLog.addStyle("", null);
        StyleConstants.setForeground(userMessageStyle, Color.BLACK);

        ownMessageStyle = messagesLog.addStyle("", null);
        StyleConstants.setForeground(ownMessageStyle, Color.GRAY);
    }

    private void addLogPanel() {
        MutableAttributeSet set = new javax.swing.text.SimpleAttributeSet(messagesLog.getParagraphAttributes());
        StyleConstants.setLineSpacing(set, (float) +0.5);
        messagesLog.setParagraphAttributes(set, false);

        messagesLog.setText(" Welcome to the ChatTree!\n");
        messagesLog.setEditable(false);

        JScrollPane jScrollPane = new JScrollPane(messagesLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(jScrollPane, BorderLayout.CENTER);
    }

    private void addMessageField() {
        messageField.setEditable(true);
        messageField.addActionListener(e -> {
            String textMessage = messageField.getText();
            if (textMessage != null) {
                node.sendMessage(textMessage);
                printOwnMessage(textMessage);
            }

            messageField.setText(null);
        });
        bottomsPanel.add(messageField, BorderLayout.CENTER);
    }

    private EmptyBorder getBorder() {
        int borderSize = appFrame.getHeight() / 50;
        return new EmptyBorder(borderSize, borderSize, borderSize, borderSize);
    }

    private synchronized void setSwingSettings() {
        appFrame.setTitle(node.getUserName());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        appFrame.setSize(screenSize.width / 4, screenSize.height / 2);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setLocationRelativeTo(null);

        addLogPanel();
        addMessageField();

        mainPanel.setBorder(getBorder());

        mainPanel.add(bottomsPanel, BorderLayout.SOUTH);

        appFrame.getContentPane().add(mainPanel);
        appFrame.setVisible(true);
    }


    @Override
    public void printUserMessage(Message message) {
        printInLog(message.toString(), userMessageStyle);
    }

    public void printOwnMessage(String message) {
        printInLog(" [" + Message.getFormattedDate(new Date()) + "] You : " + message, ownMessageStyle);
    }

    public void printInLog(String text, Style style) {
        StyledDocument doc = messagesLog.getStyledDocument();
        SwingUtilities.invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), text + "\n", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
}
