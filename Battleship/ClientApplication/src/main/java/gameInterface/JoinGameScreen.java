package gameInterface;
import client.GameClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JoinGameScreen extends JFrame  {
    GameClient client;
    private JTextField idGameField; // Added to make it accessible in handleServerResponse
    String playerName;

        public JoinGameScreen(GameClient client, String playerName) {
            this.client = client;
            this.playerName = playerName;
            client.messageConsumer = this::handleServerResponse;

            //COLORS
            Color cerulean = Color.decode("#1C6989");
            Color lightBlue = Color.decode("#96C1CE");
            Color cosmicLatte = Color.decode("#FEFAEA");
            Color outerSpace = Color.decode("#455257");
            Color rawUmber = Color.decode("#956536");
            Color cadetGrey = Color.decode("#91A6AE");


            setTitle("Join Game");
            setSize(1200, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            JPanel welcomePanel = new JPanel();
            welcomePanel.setLayout(new BorderLayout());
            welcomePanel.setPreferredSize(new Dimension(800, 400));
            welcomePanel.setBackground(cerulean);

            JLabel welcomeLabel = new JLabel("Join Game!", SwingConstants.CENTER);
            welcomeLabel.setFont(new Font("Serif", Font.BOLD, 40));
            welcomeLabel.setForeground(cosmicLatte);
            welcomeLabel.setPreferredSize(new Dimension(600, 100));
            welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

            JPanel namePanel = new JPanel();
            namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
            namePanel.setBorder(new EmptyBorder(20, 60, 20, 60));
            namePanel.setBackground(lightBlue);

            JLabel nameLabel = new JLabel("Enter the id of the game you want to join:");
            nameLabel.setFont(new Font("Serif", Font.BOLD, 30));
            nameLabel.setBorder(new EmptyBorder(0, 60, 20, 0));
            nameLabel.setForeground(Color.WHITE);

            idGameField = new JTextField(20); // Initialize nameField
            idGameField.setFont(new Font("Serif", Font.PLAIN, 20));
            idGameField.setPreferredSize(new Dimension(100, 40));
            idGameField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
            namePanel.add(nameLabel);
            namePanel.add(idGameField);


            welcomePanel.add(namePanel, BorderLayout.SOUTH);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 50));
            buttonPanel.setBackground(cosmicLatte);

            JButton startButton = new JButton("Join Game");
            startButton.setFont(new Font("Serif", Font.BOLD, 20));
            startButton.setForeground(rawUmber);
            startButton.setBackground(cadetGrey);
            Border border = BorderFactory.createCompoundBorder(
                    new LineBorder(Color.BLACK, 1, true),
                    new EmptyBorder(10, 20, 10, 20)
            );
            startButton.setBorder(border);
            startButton.setPreferredSize(new Dimension(200, 50));
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String command = "join game " + idGameField.getText();
                    if ((idGameField.getText()).isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please enter an id for a game!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Send id game to the server
                        client.sendCommand(command.trim());

                    }
                }
            });
            buttonPanel.add(startButton);


            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BorderLayout());
            rightPanel.add(welcomePanel, BorderLayout.NORTH);
            rightPanel.add(buttonPanel, BorderLayout.CENTER);

            JPanel imagePanel = new JPanel();
            imagePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            imagePanel.setBackground(Color.WHITE);

            BufferedImage originalImage = null;
            try {
                originalImage = ImageIO.read(new File("Battleship/ClientApplication/src/main/resources/utils/animated-scene-fleet-of-ships-engaging-in-naval-warfare-illuminated-by-explosions-missile-impacts.bmp"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (originalImage != null) {
                JLabel imageLabel = new JLabel(new ImageIcon(originalImage));
                imagePanel.add(imageLabel);
            }

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePanel, rightPanel);
            splitPane.setDividerSize(0); // Set divider size to 0 to hide the divider

            add(splitPane, BorderLayout.CENTER);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void handleServerResponse(String response) {
            System.out.println("Received response from server: " + response);

             if (response.startsWith("Invalid game id!")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Invalid name. Please enter your name again.", "Error", JOptionPane.ERROR_MESSAGE);
                    //playerName = null; // Reset playerName
                    idGameField.setText(""); // Clear the name field
                });
            } else if (response.startsWith("You joined the game with id")) {
                SwingUtilities.invokeLater(() -> {
                    new GameUI(client, playerName);
                    dispose();
                });
            }
        }

}
