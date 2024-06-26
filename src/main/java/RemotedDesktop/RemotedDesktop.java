package RemotedDesktop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.SecureRandom;

public class RemotedDesktop extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private InetAddress privateIP;
    private JTextField password;
    private JButton generatePasswordButton;
    private String chars;
    public RemotedDesktop() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        try {
            privateIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        JLabel label = new JLabel("Password:");
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(Color.BLUE);

        password = new JTextField(20);
        password.setToolTipText("Generated Password");
        password.setFont(new Font("Arial", Font.PLAIN, 18));
        password.setBackground(Color.LIGHT_GRAY);

        JButton submit = new JButton("Submit");
        submit.setFont(new Font("Arial", Font.BOLD, 15));
        submit.setBackground(Color.LIGHT_GRAY);

        generatePasswordButton = new JButton("Generate Password");
        generatePasswordButton.setFont(new Font("Arial", Font.BOLD, 15));
        generatePasswordButton.setBackground(Color.LIGHT_GRAY);
        generatePasswordButton.addActionListener(e -> password.setText(generateRandomPassword(12)));

        JTextField IPlabel = new JTextField();
        IPlabel.setText("Your Machine's IP Address is: " + privateIP.getHostAddress());
        IPlabel.setFont(new Font("Arial", Font.BOLD, 14));
        IPlabel.setForeground(Color.RED);
        IPlabel.setEditable(false);
        IPlabel.setBorder(null);
        IPlabel.setBackground(Color.WHITE);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(label, BorderLayout.WEST);
        inputPanel.add(password, BorderLayout.CENTER);
        inputPanel.add(generatePasswordButton, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(submit, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        mainPanel.add(inputPanel);
        mainPanel.add(IPlabel);

        setLayout(new BorderLayout(10, 10));
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        submit.addActionListener(this);

        setTitle("Set a Password for maintaining connection");
        setSize(450, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dispose();

        try {
            RemoteServerImpl remoteScreen = new RemoteServerImpl(password.getText());
            LocateRegistry.createRegistry(1099);
            Naming.rebind("rmi://localhost:1099/RemoteScreen", remoteScreen);
            System.out.println("Remote server is running...");


        } catch (RemoteException | MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RemotedDesktop();
    }

    private String generateRandomPassword(int length) {
        chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }
}
