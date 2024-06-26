package viewerDesktop;


import RemotedDesktop.RemoteServer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;

public class ScreenHandler {
    private RemoteServer remoteScreen; // L'objet RemoteScreen pour la capture d'écran à distance
    private JLabel screenLabel; // JLabel pour afficher l'image de l'écran
    private JProgressBar progressBar; // Barre de progression pour les opérations asynchrones
    private Timer screenUpdateTimer; // Timer pour mettre à jour l'écran à intervalles réguliers
    private double scaleX; // Facteur d'échelle horizontal pour l'affichage de l'écran
    private double scaleY; // Facteur d'échelle vertical pour l'affichage de l'écran

    // Constructeur prenant l'objet RemoteScreen et le cadre parent comme arguments
    public ScreenHandler(RemoteServer remoteScreen, JFrame frame) {
        this.remoteScreen = remoteScreen;
        screenLabel = new JLabel(); // Initialisation de JLabel pour afficher l'écran
        progressBar = new JProgressBar(); // Initialisation de JProgressBar pour les opérations asynchrones
        initializeScale(frame); // Initialiser les facteurs d'échelle basés sur la taille de l'écran local et à distance
    }

    // Méthode pour obtenir le JLabel de l'écran
    public JLabel getScreenLabel() {
        return screenLabel;
    }

    // Méthode pour obtenir la JProgressBar
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    // Méthode pour obtenir la taille de l'écran distant
    public Dimension getRemoteScreenSize() {
        try {

            return remoteScreen.getScreenSize();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

// Méthode pour démarrer le timer de mise à jour de l'écran
public void startScreenUpdateTimer() {
screenUpdateTimer = new Timer(200, e -> updateScreen());
screenUpdateTimer.start();
}
    // Méthode pour mettre à jour l'image de l'écran à partir des données capturées à distance
    private void updateScreen() {
        try {
            byte[] screenData = remoteScreen.captureScreen(); // Capturer les données de l'écran distant
            ByteArrayInputStream bais = new ByteArrayInputStream(screenData);
            BufferedImage image = ImageIO.read(bais); // Créer une image à partir des données capturées

            // Redimensionner l'image pour l'adapter à la taille du JLabel
            if (screenLabel.getWidth() > 0 && screenLabel.getHeight() > 0) {
                Image scaledImage = image.getScaledInstance(screenLabel.getWidth(), screenLabel.getHeight(), Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                screenLabel.setIcon(icon); // Définir l'image redimensionnée comme icône du JLabel
            }
            screenLabel.repaint(); // Redessiner le JLabel avec la nouvelle image
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour initialiser les facteurs d'échelle basés sur les tailles d'écran locales et à distance
    private void initializeScale(JFrame frame) {
        Dimension localScreenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Taille de l'écran local
        Dimension remoteScreenSize = getRemoteScreenSize(); // Taille de l'écran distant

        if (remoteScreenSize != null) {
            double widthScale = localScreenSize.getWidth() / remoteScreenSize.getWidth(); // Facteur d'échelle horizontal
            double heightScale = localScreenSize.getHeight() / remoteScreenSize.getHeight(); // Facteur d'échelle vertical
            double scale = Math.min(widthScale, heightScale); // Prendre le facteur d'échelle le plus petit
            scaleX = scale; // Affecter le facteur d'échelle horizontal
            scaleY = scale; // Affecter le facteur d'échelle vertical
        }
    }

    // Méthode pour mapper la position du curseur local à la position correspondante sur l'écran distant
    public Point mapLocalToRemoteCursor(Point localCursor, Dimension localScreen, Dimension remoteScreen) {
        double relativeX = (double) localCursor.x / localScreen.width; // Calculer la position relative X du curseur local
        double relativeY = (double) localCursor.y / localScreen.height; // Calculer la position relative Y du curseur local
        int remoteX = (int) (relativeX * remoteScreen.width); // Mapper la position X relative sur l'écran distant
        int remoteY = (int) (relativeY * remoteScreen.height); // Mapper la position Y relative sur l'écran distant
        return new Point(remoteX, remoteY); // Retourner la position du curseur sur l'écran distant
    }


}

