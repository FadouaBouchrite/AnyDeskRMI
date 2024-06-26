package RemotedDesktop;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

// Interface RemoteServer qui étend l'interface Remote (pour la communication distante)
public interface RemoteServer extends Remote {

    // Méthode pour vérifier le mot de passe
    boolean checkPassword(String inputPassword) throws RemoteException;

    // Méthode pour obtenir la liste des messages
    ArrayList<String> getMessages() throws RemoteException;

    // Méthode pour envoyer un message
    void sendMessage(String message) throws RemoteException;

    // Méthode pour obtenir la taille de l'écran distant
    Dimension getScreenSize() throws RemoteException;

    // Méthode pour capturer l'écran distant
    byte[] captureScreen() throws RemoteException;

    // Méthodes pour simuler des actions clavier et souris
    void pressKey(int keyCode) throws RemoteException;
    void releaseKey(int keyCode) throws RemoteException;
    void typeKey(char keyChar) throws RemoteException;
    void pressMouseButton(int button) throws RemoteException;
    void releaseMouseButton(int button) throws RemoteException;
    void clickMouse(int button) throws RemoteException;
    void moveCursor(int x, int y) throws RemoteException;
    void dragMouse(int x, int y) throws RemoteException;

    // Méthodes pour envoyer et recevoir des fichiers
    void sendFile(String fileName, byte[] data) throws RemoteException;
    byte[] receiveFile(byte[] fileData, String fileName) throws RemoteException;

    // Méthode pour créer une fenêtre distante
    void createRemoteFrame() throws RemoteException;

    // Méthode pour ouvrir un sélecteur de fichiers
    FileTransferHandlerRemote openFileChooser() throws RemoteException;
}
