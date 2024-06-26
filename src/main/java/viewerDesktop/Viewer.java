package viewerDesktop;

import RemotedDesktop.RemoteServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Viewer extends JFrame implements ActionListener {

    JTextField serverIP, password;
    private RemoteServer remoteScreen;
    private ScreenHandler screenHandler;
    private FileTransferHandlerViewer fileTransferHandler;
    private EventHandler eventHandler;
    JMenuBar menuBar = new JMenuBar();
    JPanel gridPanel; // Ajouté ici pour avoir une référence à gridPanel

    public Viewer() {



        JLabel IPlabel = new JLabel("Server IP: ");
        IPlabel.setFont(new Font("Arial", Font.BOLD, 15));
        IPlabel.setForeground(Color.BLUE);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 15));
        passwordLabel.setForeground(Color.BLUE);
        serverIP = new JTextField(20);
        serverIP.setToolTipText("Enter IP of Machine you want to Connect with!");
        serverIP.setFont(new Font("Arial", Font.PLAIN, 18));
        password = new JPasswordField(20);
        password.setToolTipText("Input Password of Machine you want to Connect with!");
        password.setFont(new Font("Arial", Font.PLAIN, 18));
        JButton submit = new JButton("Submit");
        submit.setFont(new Font("Arial", Font.BOLD, 15));
        submit.setBackground(Color.LIGHT_GRAY);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.add(IPlabel, BorderLayout.CENTER);

        JPanel panel2 = new JPanel();
        panel2.add(serverIP);

        JPanel topPanel = new JPanel();
        topPanel.add(panel1);
        topPanel.add(panel2);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout());
        panel3.add(passwordLabel, BorderLayout.CENTER);

        JPanel panel4 = new JPanel();
        panel4.add(password);

        JPanel midPanel = new JPanel();
        midPanel.add(panel3);
        midPanel.add(panel4);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(submit);

        gridPanel = new JPanel(); // Initialisé ici
        gridPanel.setLayout(new GridLayout(3, 1));
        gridPanel.add(topPanel);
        gridPanel.add(midPanel);
        gridPanel.add(bottomPanel);

        submit.addActionListener(this);

        setLayout(new BorderLayout());
        add(new JPanel().add(new JLabel(" ")), BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(new JPanel().add(new JLabel(" ")), BorderLayout.SOUTH);

        setVisible(true);
        setSize(400, 200);
        setResizable(false);
        setLocation(500, 300);
        setTitle("Enter Password to Connect!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension localScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension remoteScreenSize = screenHandler.getRemoteScreenSize();

        if (remoteScreenSize != null) {
            double widthScale = localScreenSize.getWidth() / remoteScreenSize.getWidth();
            double heightScale = localScreenSize.getHeight() / remoteScreenSize.getHeight();
            double scale = Math.min(widthScale, heightScale);
            int width = (int) (remoteScreenSize.getWidth() * scale);
            int height = (int) (remoteScreenSize.getHeight() * scale);
            setSize(width, height);
        } else {
            setSize((int) (localScreenSize.getWidth() * 0.8), (int) (localScreenSize.getHeight() * 0.8));
        }

        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton sendFileButton = new JButton("Send File");
        JButton receiveFileButton = new JButton("Receive File");
        JButton chatButton = new JButton("Chat");

        sendFileButton.setFont(new Font("Arial", Font.BOLD, 15));
        receiveFileButton.setFont(new Font("Arial", Font.BOLD, 15));
        chatButton.setFont(new Font("Arial", Font.BOLD, 15));

        sendFileButton.setBackground(Color.LIGHT_GRAY);
        receiveFileButton.setBackground(Color.LIGHT_GRAY);
        chatButton.setBackground(Color.LIGHT_GRAY);

        sendFileButton.addActionListener(e -> fileTransferHandler.sendFile());
        receiveFileButton.addActionListener(e -> {
            try {
                fileTransferHandler.receiveFile();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });
        chatButton.addActionListener(e -> {
            ChatHandler chatWindow = new ChatHandler(remoteScreen);
            chatWindow.setVisible(true);
        });

        buttonPanel.add(sendFileButton);
        buttonPanel.add(receiveFileButton);
        buttonPanel.add(chatButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(screenHandler.getScreenLabel(), BorderLayout.CENTER);
        add(screenHandler.getProgressBar(), BorderLayout.SOUTH);

        JMenu fileMenu = new JMenu("File");
        JMenuItem sendFileItem = new JMenuItem("Send File");
        JMenuItem receiveFileItem = new JMenuItem("Receive File");

        menuBar.add(fileMenu);
        fileMenu.add(sendFileItem);
        fileMenu.add(receiveFileItem);

        sendFileItem.addActionListener(e -> fileTransferHandler.sendFile());
        receiveFileItem.addActionListener(e -> {
            try {
                fileTransferHandler.receiveFile();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        eventHandler.registerListeners();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Registry registry = LocateRegistry.getRegistry(serverIP.getText(), 1099);
            remoteScreen = (RemoteServer) registry.lookup("RemoteScreen");

            if (!remoteScreen.checkPassword(password.getText())) {
                System.out.println("Entered Credentials are wrong!");
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Connection Error", JOptionPane.ERROR_MESSAGE);
            } else {
                getContentPane().removeAll(); // Retire tous les composants de la fenêtre
                System.out.println("Connection Established with Server!");

                screenHandler = new ScreenHandler(remoteScreen, this);
                fileTransferHandler = new FileTransferHandlerViewer(remoteScreen, this);
                eventHandler = new EventHandler(remoteScreen, this, screenHandler.getScreenLabel(), screenHandler.getProgressBar(), screenHandler);

                initUI();
                revalidate(); // Relayout l'interface
                repaint(); // Repeint la fenêtre
                screenHandler.startScreenUpdateTimer();
                remoteScreen.createRemoteFrame();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to remote screen: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Viewer().setVisible(true));
    }

}


