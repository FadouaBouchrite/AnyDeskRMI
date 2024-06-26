package RemotedDesktop;

import java.io.Serializable;

// Définition de la classe FileTransferHandlerRemote
public class FileTransferHandlerRemote implements Serializable {
    // Déclaration des variables membres
    private byte[] fileData; // Tableau d'octets pour stocker les données du fichier
    private String fileName; // Nom du fichier

    // Constructeur prenant les données du fichier et son nom
    public FileTransferHandlerRemote(byte[] fileData, String fileName) {
        this.fileData = fileData;
        this.fileName = fileName;
    }

    // Méthode pour obtenir les données du fichier
    public byte[] getFileData() {
        return fileData;
    }

    // Méthode pour obtenir le nom du fichier
    public String getFileName() {
        return fileName;
    }
}
