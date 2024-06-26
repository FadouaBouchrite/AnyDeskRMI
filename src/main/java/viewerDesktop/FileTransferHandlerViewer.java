package viewerDesktop;

import RemotedDesktop.FileTransferHandlerRemote; // Importation de l'interface de gestion de transfert de fichiers côté serveur
import RemotedDesktop.RemoteServer; // Importation de l'interface serveur distant

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

// Classe pour gérer le transfert de fichiers côté client
public class FileTransferHandlerViewer extends Component {
    private RemoteServer remoteScreen; // Référence au serveur distant
    private JFrame frame; // Référence à la fenêtre principale

    // Constructeur qui initialise la classe avec le serveur distant et la fenêtre principale
    public FileTransferHandlerViewer(RemoteServer remoteScreen, JFrame frame) {
        this.remoteScreen = remoteScreen;
        this.frame = frame;
    }

    // Méthode pour envoyer un fichier au serveur distant
    void sendFile() {
        JFileChooser fileChooser = new JFileChooser(); // Boîte de dialogue pour choisir un fichier
        int result = fileChooser.showOpenDialog(this); // Affiche la boîte de dialogue

        if (result == JFileChooser.APPROVE_OPTION) { // Si l'utilisateur approuve la sélection du fichier
            File selectedFile = fileChooser.getSelectedFile(); // Récupère le fichier sélectionné

            try (FileInputStream fis = new FileInputStream(selectedFile)) { // Flux de fichier pour lire les données
                byte[] fileData = fis.readAllBytes(); // Lit toutes les données du fichier
                remoteScreen.receiveFile(fileData, selectedFile.getName()); // Envoie les données du fichier au serveur
                JOptionPane.showMessageDialog(this, "File sent successfully.", "Success", JOptionPane.INFORMATION_MESSAGE); // Affiche un message de succès
            } catch (IOException e) { // Gère les exceptions en cas d'erreur
                JOptionPane.showMessageDialog(this, "Failed to send file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Affiche un message d'erreur
            }
        }
    }

    // Méthode pour recevoir un fichier du serveur distant
    public void receiveFile() throws RemoteException {
        SwingUtilities.invokeLater(() -> { // Exécute le code dans le thread de l'interface utilisateur
            // Ouvrir un thread pour la logique de traitement de fichier
            new Thread(() -> { // Crée un nouveau thread pour gérer la réception de fichier
                FileTransferHandlerRemote fileTransfer = null; // Déclare la variable pour la gestion de transfert de fichier
                try {
                    fileTransfer = remoteScreen.openFileChooser(); // Ouvre le sélecteur de fichiers côté serveur
                } catch (RemoteException e) {
                    e.printStackTrace(); // Affiche la trace de la pile en cas d'exception
                }

                byte[] fileData = fileTransfer.getFileData(); // Récupère les données du fichier
                String fileName = fileTransfer.getFileName(); // Récupère le nom du fichier

                // Sauvegarde les données du fichier sur le disque local avec le nom de fichier fourni
                try (FileOutputStream fos = new FileOutputStream(fileName)) { // Flux de fichier pour écrire les données
                    fos.write(fileData); // Écrit les données dans le fichier
                    JOptionPane.showMessageDialog(null, "File received: " + fileName, "Success", JOptionPane.INFORMATION_MESSAGE); // Affiche un message de succès
                } catch (IOException e) { // Gère les exceptions en cas d'erreur
                    JOptionPane.showMessageDialog(null, "Failed to save file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Affiche un message d'erreur
                }
            }).start(); // Démarre le thread
        });
    }
}
