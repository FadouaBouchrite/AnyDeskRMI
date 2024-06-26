package viewerDesktop;

import RemotedDesktop.FileTransferHandlerRemote;
import RemotedDesktop.RemoteServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Classe pour gérer un chat entre l'utilisateur local et l'écran distant.
 */
public class ChatHandler extends JFrame {
    private RemoteServer remoteScreen; // Interface du serveur distant
    private JTextPane chatArea; // Zone d'affichage du chat
    private JTextField inputField; // Champ de saisie des messages
    private JButton sendButton; // Bouton d'envoi des messages
    private Timer messageUpdateTimer; // Timer pour mettre à jour les messages
    private SimpleDateFormat timestampFormat; // Format pour les horodatages

    /**
     * Constructeur de ChatHandler.
     *
     * @param remoteScreen l'interface du serveur distant
     */
    public ChatHandler(RemoteServer remoteScreen) {
        this.remoteScreen = remoteScreen;
        setTitle("Remote Chat");
        setSize(500, 400); // Taille augmentée pour un meilleur agencement
        setLayout(new BorderLayout(10, 10)); // Ajout de padding autour des bords

        timestampFormat = new SimpleDateFormat("HH:mm:ss");

        // Configuration de la zone de chat avec le type de contenu HTML
        chatArea = new JTextPane();
        chatArea.setContentType("text/html");
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Chat"));
        add(scrollPane, BorderLayout.CENTER);

        // Configuration du panneau de saisie avec bordures et marges
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        sendButton = new JButton("Send");

        // Ajout de l'action listener pour le bouton d'envoi
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(inputPanel, BorderLayout.SOUTH);

        // Timer pour mettre à jour les messages toutes les secondes
        messageUpdateTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMessages();
            }
        });
        messageUpdateTimer.start();
    }

    /**
     * Méthode pour envoyer un message à l'écran distant.
     */
    private void sendMessage() {
        try {
            String message = inputField.getText();
            if (!message.isEmpty()) {
                String timestamp = timestampFormat.format(new Date());
                String formattedMessage = formatMessage("", message, timestamp, true);
                remoteScreen.sendMessage(formattedMessage);
                inputField.setText("");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode pour mettre à jour la zone de chat avec les derniers messages de l'écran distant.
     */
    private void updateMessages() {
        try {
            StringBuilder messagesHtml = new StringBuilder("<html><body style='font-family: Arial; font-size: 12px;'>");
            for (String message : remoteScreen.getMessages()) {
                messagesHtml.append(message);
            }
            messagesHtml.append("</body></html>");
            chatArea.setText(messagesHtml.toString());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode pour formater les messages en HTML pour l'affichage dans la zone de chat.
     *
     * @param sender    l'expéditeur du message
     * @param message   le contenu du message
     * @param timestamp l'horodatage du message
     * @param isViewer  true si le message provient du Viewer, false sinon
     * @return une chaîne formatée en HTML
     */
    private String formatMessage(String sender, String message, String timestamp, boolean isViewer) {
        String align = isViewer ? "right" : "left";
        String color = isViewer ? "blue" : "green";
        return String.format(
                "<div style='text-align: %s;'><b>%s</b> (%s)<br><span style='color: %s;'>%s</span></div>",
                align, sender, timestamp, color, message
        );
    }


}
