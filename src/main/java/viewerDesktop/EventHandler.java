package viewerDesktop;

import RemotedDesktop.RemoteServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;

/**
 * Gère les événements clavier et souris pour les envoyer à un écran distant via RMI.
 */
public class EventHandler implements KeyListener, MouseListener, MouseMotionListener {
    private RemoteServer remoteScreen; // Interface de l'écran distant
    private JFrame frame; // Fenêtre principale de l'application
    private JLabel screenLabel; // Label affichant l'écran distant
    private JProgressBar progressBar; // Barre de progression de l'application
    private ScreenHandler screenHandler; // Gestionnaire d'écran pour le mapping des coordonnées

    /**
     * Construit un EventHandler.
     *
     * @param remoteScreen  l'interface de l'écran distant
     * @param frame         la fenêtre principale de l'application
     * @param screenLabel   le label affichant l'écran distant
     * @param progressBar   la barre de progression de l'application
     * @param screenHandler le gestionnaire d'écran pour le mapping des coordonnées
     */
    public EventHandler(RemoteServer remoteScreen, JFrame frame, JLabel screenLabel, JProgressBar progressBar, ScreenHandler screenHandler) {
        this.remoteScreen = remoteScreen;
        this.frame = frame;
        this.screenLabel = screenLabel;
        this.progressBar = progressBar;
        this.screenHandler = screenHandler;
    }

    /**
     * Enregistre les écouteurs d'événements sur la fenêtre.
     */
    public void registerListeners() {
        frame.addKeyListener(this);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (remoteScreen != null) {
            try {
                remoteScreen.pressKey(e.getKeyCode()); // Envoie l'événement de pression de touche au serveur distant
            } catch (RemoteException ex) {
                showError("Erreur lors de l'envoi de la pression de touche à l'écran distant: ", ex); // Affiche un message d'erreur en cas d'exception
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (remoteScreen != null) {
            try {
                remoteScreen.releaseKey(e.getKeyCode()); // Envoie l'événement de relâchement de touche au serveur distant
            } catch (RemoteException ex) {
                showError("Erreur lors de l'envoi du relâchement de touche à l'écran distant: ", ex); // Affiche un message d'erreur en cas d'exception
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Non nécessaire
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (remoteScreen != null) {
            try {
                remoteScreen.pressMouseButton(e.getButton()); // Envoie l'événement de pression de bouton de souris au serveur distant
            } catch (RemoteException ex) {
                showError("Erreur lors de l'envoi de la pression de souris à l'écran distant: ", ex); // Affiche un message d'erreur en cas d'exception
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (remoteScreen != null) {
            try {
                remoteScreen.releaseMouseButton(e.getButton()); // Envoie l'événement de relâchement de bouton de souris au serveur distant
            } catch (RemoteException ex) {
                showError("Erreur lors de l'envoi du relâchement de souris à l'écran distant: ", ex); // Affiche un message d'erreur en cas d'exception
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Non nécessaire
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        handleMouseMovement(e, "déplacement du curseur"); // Gère le déplacement du curseur
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        handleMouseMovement(e, "glisser le curseur"); // Gère le glissement du curseur
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Non nécessaire
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Non nécessaire
    }

    /**
     * Gère les mouvements et les glissements de la souris en mappant les coordonnées locales aux coordonnées de l'écran distant.
     *
     * @param e       l'événement de la souris
     * @param action  description de l'action pour les messages d'erreur
     */
    private void handleMouseMovement(MouseEvent e, String action) {
        if (remoteScreen != null && screenHandler != null) {
            try {
                Point localPoint = e.getPoint(); // Récupère le point local de la souris
                Insets insets = frame.getInsets(); // Récupère les bordures de la fenêtre
                int dx = -insets.left; // Ajuste les coordonnées pour tenir compte des bordures
                int dy = -insets.top + progressBar.getHeight() - frame.getRootPane().getHeight() + screenLabel.getHeight();
                localPoint.translate(dx, dy); // Traduit le point en tenant compte des bordures et des éléments de l'interface utilisateur
                Dimension localSize = screenLabel.getSize(); // Récupère la taille de l'écran local
                Dimension remoteSize = remoteScreen.getScreenSize(); // Récupère la taille de l'écran distant
                if (localSize != null && remoteSize != null) {
                    Point remotePoint = screenHandler.mapLocalToRemoteCursor(localPoint, localSize, remoteSize); // Mappe les coordonnées locales aux coordonnées de l'écran distant
                    remoteScreen.moveCursor(remotePoint.x, remotePoint.y); // Déplace le curseur à la position distante
                }
            } catch (RemoteException ex) {
                showError("Erreur " + action + " sur l'écran distant: ", ex); // Affiche un message d'erreur en cas d'exception
            }
        }
    }

    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     *
     * @param message le message d'erreur
     * @param ex      l'exception
     */
    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(frame, message + ex.getMessage(), "Erreur de l'écran distant", JOptionPane.ERROR_MESSAGE); // Affiche une boîte de dialogue avec le message d'erreur
        ex.printStackTrace(); // Affiche la trace de la pile pour le débogage
    }
}
