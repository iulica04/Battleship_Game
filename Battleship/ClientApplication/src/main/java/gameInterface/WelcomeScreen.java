package gameInterface;
import client.GameClient;
import gameInterface.elements.RoundedButton;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class WelcomeScreen extends JFrame {
    private GameClient client;
    private JTextField nameField;
    private String playerName;
    private Color blue = Color.decode("#A7DDE9");
    private Color orange = Color.decode("#FFA500");
    private Color chineseViolet = Color.decode("#6E6375");
    private Color grey = Color.decode("#D3D3D3");
    private Color lightGrey = Color.decode("#F0F0F0");

    public WelcomeScreen() {
        try {
            client = new GameClient("localhost", 1502, this::handleServerResponse);
            client.connect();
        } catch (Exception e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }

        setTitle("Welcome to Battleship Game");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel backgroundImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                ImageIcon imageIcon = new ImageIcon("Battleship/ClientApplication/src/main/resources/utils/PhotoForWelcomePage.png");
                Image image = imageIcon.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);

                g.setColor(blue);
                g.fillRect(getWidth() - 400, getHeight() - 290, 400, 290);
            }
        };
        backgroundImagePanel.setLayout(null);

        JPanel titlePanel = new JPanel();
        titlePanel.setBounds(30, 20, 400, 150);
        titlePanel.setBackground(orange);
        titlePanel.setLayout(new BorderLayout());

        // Adăugarea unei margini gri
        Border lineBorder = new LineBorder(chineseViolet, 5);
        Border emptyBorder = new EmptyBorder(10, 10, 10, 10);
        titlePanel.setBorder(new CompoundBorder(lineBorder, emptyBorder));

        JLabel titleLabel = new JLabel("<html>Welcome to <br> Battleship Game!</html>", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        backgroundImagePanel.add(titlePanel);

        JPanel formPanel = new JPanel();
        formPanel.setBounds(getWidth() - 500, getHeight() - 290, 500, 290);
        formPanel.setBackground(blue);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setBounds(20, 20, 400, 50);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 30));
        nameLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        nameField = createStyledTextField(20);
        nameField.setMaximumSize(new Dimension(300, 30));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameField.setFont(new Font("Serif", Font.PLAIN, 20));

        JButton startButton = createButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> {
            playerName = nameField.getText();
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter your name", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                client.setUsername(playerName);
                client.sendCommand(playerName);
            }
        });

        JButton infoButton = createButton("More Info");
        infoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoButton.addActionListener(e -> showGameRules());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Layout for placing buttons side by side
        buttonPanel.setBackground(blue); // To match the form panel background
        buttonPanel.add(startButton);
        buttonPanel.add(infoButton);

        formPanel.add(Box.createVerticalStrut(50));
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(buttonPanel); // Add button panel instead of individual buttons

        backgroundImagePanel.add(formPanel);

        setContentPane(backgroundImagePanel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleServerResponse(String response) {
        System.out.println("Received response from server: " + response);

        if (playerName != null && response.startsWith("waiting for your name...")) {
            client.sendCommand(playerName.trim());
        } else if ("Invalid name! Try another one!".equals(response)) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Invalid name. Please enter your name again.", "Error", JOptionPane.ERROR_MESSAGE);
                playerName = null; // Reset playerName
                nameField.setText(""); // Clear the name field
            });
        } else if (response.equals("Valid name!")) {
            SwingUtilities.invokeLater(() -> {
                new PlayerScreen(client, playerName);
                dispose();
            });
        }
    }

    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setBackground(lightGrey);
        textField.setForeground(Color.BLACK);
        textField.setBorder(new CompoundBorder(
                new LineBorder(chineseViolet, 2),
                new EmptyBorder(5, 5, 5, 5)
        ));

        return textField;
    }

    private JButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setPreferredSize(new Dimension(200, 50));
        button.setFont(new Font("Serif", Font.BOLD, 20));
        if (text.equals("More Info")) {
            button.setHoverBackgroundColor(orange);
        }
        return button;
    }

    private void showGameRules() {
        String rules = "Game Rules:\n"
                + "1. Players take turns to call shots at the opponent's ships.\n"
                + "2. Each player has a grid where they place their ships.\n"
                + "3. The goal is to sink all of the opponent's ships.\n"
                + "4. The game ends when one player has no remaining ships or the time is over.";
        JOptionPane.showMessageDialog(this, rules, "Game Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeScreen());
    }
}
