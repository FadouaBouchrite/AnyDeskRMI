package RemotedDesktop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

// Implémentation de l'interface RemoteServer et extension de UnicastRemoteObject pour la communication distante
public class RemoteServerImpl extends UnicastRemoteObject implements RemoteServer {
    private static final long serialVersionUID = 1L;
    int buttonToPress; // Variable pour stocker le bouton de la souris à presser
    private Robot robot; // Objet Robot pour contrôler le clavier et la souris
    private String password; // Mot de passe pour la connexion
    private List<String> messages; // Liste des messages échangés
    private JFrame frame; // Fenêtre Swing pour l'interface utilisateur
    private JTextPane chatArea; // Zone de texte pour afficher les messages
    private JTextField inputField; // Champ de texte pour saisir les messages
    private SimpleDateFormat timestampFormat; // Format pour les horodatages des messages

    // Constructeur prenant le mot de passe comme argument
    public RemoteServerImpl(String password) throws RemoteException {
        this.password = password;
        this.messages = new ArrayList<>(); // Initialisation de la liste des messages
        this.timestampFormat = new SimpleDateFormat("HH:mm:ss"); // Initialisation du format de l'horodatage
        try {
            robot = new Robot(); // Initialisation de l'objet Robot pour le contrôle
        } catch (AWTException e) {
            // En cas d'erreur lors de l'initialisation de Robot, une RemoteException est levée
            throw new RemoteException("Error initializing Robot", e);
        }
    }

    // Méthode pour vérifier le mot de passe
    @Override
    public boolean checkPassword(String inputPassword) throws RemoteException {
        return password.equals(inputPassword);
    }

    // Méthode pour créer la fenêtre distante
    @Override
    public void createRemoteFrame() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Distant Frame"); // Création de la fenêtre Swing
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Définition de l'action de fermeture
            frame.setSize(500, 400); // Définition de la taille de la fenêtre
            frame.setLocationRelativeTo(null); // Positionnement au centre de l'écran
            frame.setLayout(new BorderLayout(10, 10)); // Définition du gestionnaire de mise en page

            // Configuration de la zone de chat avec un JScrollPane pour le défilement
            chatArea = new JTextPane();
            chatArea.setContentType("text/html");
            chatArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(chatArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Chat"));
            frame.add(scrollPane, BorderLayout.CENTER);

            // Configuration du panneau d'entrée avec une bordure et un remplissage
            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            inputField = new JTextField();
            JButton sendButton = new JButton("Send");
            sendButton.addActionListener(e -> sendMessage());

            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            frame.add(inputPanel, BorderLayout.SOUTH);

            frame.setVisible(true); // Rendre la fenêtre visible
        });
    }

    // Méthode pour envoyer un message
    private void sendMessage() {
        try {
            String message = inputField.getText();
            if (!message.isEmpty()) {
                String timestamp = timestampFormat.format(new Date());
                String formattedMessage = formatMessage("remoted", message, timestamp, false); // Formatage du message
                messages.add(formattedMessage); // Ajout du message à la liste
                inputField.setText(""); // Effacement du champ de saisie
                updateChatArea(); // Mise à jour de la zone de chat
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour mettre à jour la zone de chat avec les derniers messages
    private void updateChatArea() {
        StringBuilder messagesHtml = new StringBuilder("<html><body style='font-family: Arial; font-size: 12px;'>");
        for (String message : messages) {
            messagesHtml.append(message);
        }
        messagesHtml.append("</body></html>");
        chatArea.setText(messagesHtml.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // Méthode pour formater le message pour l'affichage
    private String formatMessage(String sender, String message, String timestamp, boolean isRemote) {
        String align = isRemote ? "right" : "left";
        String color = isRemote ? "green" : "blue";
        return String.format(
                "<div style='text-align: %s;'><b>%s</b> (%s)<br><span style='color: %s;'>%s</span></div>",
                align, sender, timestamp, color, message
        );
    }

    // Méthode pour obtenir la taille de l'écran
    @Override
    public Dimension getScreenSize() throws RemoteException {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    // Méthode pour capturer l'écran
    @Override
    public byte[] captureScreen() throws RemoteException {
        BufferedImage screenImage = robot.createScreenCapture(new Rectangle(getScreenSize()));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(screenImage, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RemoteException("Error capturing screen", e);
        }
    }

    // Méthodes pour simuler des actions clavier et souris (pression, relâchement, clic, déplacement)
    @Override
    public void pressKey(int keyCode) throws RemoteException {
        robot.keyPress(keyCode);
    }

    @Override
    public void releaseKey(int keyCode) throws RemoteException {
        robot.keyRelease(keyCode);
    }

    @Override
    public void typeKey(char keyChar) throws RemoteException {
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(keyChar);
        pressKey(keyCode);
        releaseKey(keyCode);
    }

    @Override
    public void pressMouseButton(int button) throws RemoteException {
        switch (button) {
            case MouseEvent.BUTTON1:
                buttonToPress = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                buttonToPress = InputEvent.BUTTON3_DOWN_MASK;
                break;
            default:
                throw new RemoteException("Unsupported mouse button: " + button);
        }
        robot.mousePress(buttonToPress);
    }

    @Override
    public void releaseMouseButton(int button) throws RemoteException {
        int buttonToRelease;
        switch (button) {
            case MouseEvent.BUTTON1:
                buttonToRelease = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                buttonToRelease = InputEvent.BUTTON3_DOWN_MASK;
                break;
            default:
                throw new RemoteException("Unsupported mouse button: " + button);
        }
        robot.mouseRelease(buttonToRelease);
    }

    @Override
    public void clickMouse(int button) throws RemoteException {
        int buttonMask;
        switch (button) {
            case MouseEvent.BUTTON1:
                buttonMask = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case MouseEvent.BUTTON2:
                buttonMask = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                buttonMask = InputEvent.BUTTON3_DOWN_MASK;
                break;
            default:
                throw new RemoteException("Unsupported mouse button: " + button);
        }
        robot.mousePress(buttonMask);
        robot.mouseRelease(buttonMask);
    }

    @Override
    public void moveCursor(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }

    @Override
    public void dragMouse(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }




    @Override
    public ArrayList<String> getMessages() throws RemoteException {
        return new ArrayList<>(messages);
    }

    @Override
    public void sendMessage(String message) throws RemoteException {
        String timestamp = timestampFormat.format(new Date());
        String formattedMessage = formatMessage("Remote", message, timestamp, true);
        messages.add(formattedMessage);
        SwingUtilities.invokeLater(this::updateChatArea);
    }




    @Override
    public void sendFile(String fileName, byte[] data) throws RemoteException {
        try {
            Files.write(Paths.get(fileName), data);
        } catch (IOException e) {
            throw new RemoteException("Error saving file", e);
        }
    }

    public byte[] receiveFile(byte[] fileData, String fileName) throws RemoteException {
        // Obtenez le chemin vers le répertoire de travail actuel
        String currentDir = System.getProperty("user.dir");

        // Ajoutez le nom du fichier pour créer le chemin complet du fichier dans le même répertoire
        String filePath = currentDir + File.separator + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileData);
            System.out.println("File received and saved to current directory: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            throw new RemoteException("Error saving file: " + e.getMessage());
        }
        return fileData;
    }
    @Override
    public FileTransferHandlerRemote openFileChooser() throws RemoteException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to send to the viewer");
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                byte[] fileData = fis.readAllBytes();
                String fileName = selectedFile.getName();
                return new FileTransferHandlerRemote(fileData, fileName);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RemoteException("Error reading file: " + e.getMessage());
            }
        }

        return null; // Return null si aucun fichier n'est selectioné
    }
}
