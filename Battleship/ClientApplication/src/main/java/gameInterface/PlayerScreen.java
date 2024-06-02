package gameInterface;

import client.GameClient;
import gameInterface.elements.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import java.awt.image.BufferedImage;

public class PlayerScreen extends JFrame {
    private BufferedImage backgroundImage;
    private GameClient client;
    private String playerName;
    private String serverAddress = "localhost";
    private int serverPort = 1502;

    Color cerulean = Color.decode("#1C6989");
    Color cadetGrey = Color.decode("#91A6AE");
    Color delftBlue = Color.decode("#414163");
    Color tyrianPurple = Color.decode("#471732");
    Color taupeGray = Color.decode("#7A7885");
    Color mintGreen = Color.decode("#CDF2EB");

    public PlayerScreen(GameClient client, String playerName) {
        this.playerName = playerName;
        this.client = client;

        try {
            backgroundImage = ImageIO.read(new File("Battleship/ClientApplication/src/main/resources/utils/1299.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setTitle("Player Screen");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new ImagePanel(); // Panou personalizat cu imagine de fundal
        mainPanel.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(1200, 100));
        titlePanel.setBackground(delftBlue);

        JLabel titleLabel = new JLabel("Welcome, " + playerName + "!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(cadetGrey);

        titlePanel.add(titleLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10)); // Centrarea butoanelor
        buttonPanel.setOpaque(false); // Sa fie transparent, pentru a vedea imaginea de fundal
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20)); // Marginea panoului

        // Crearea butoanelor
        JButton button1 = createButton("Create Game");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> new MyBoardUI(client, playerName, "create game"));
                dispose();
            }
        });
        JButton button2 = createButton("Join Game");
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> new MyBoardUI(client, playerName, "join game"));
                dispose();
            }
        });
        JButton button3 = createButton("My History");
        JButton button4 = createButton("Ranking");
        JButton button5 = createButton("Exit");

        // Adăugarea butoanelor în panou
        buttonPanel.add(button1);
        buttonPanel.add(button2);
        buttonPanel.add(button3);
        buttonPanel.add(button4);
        buttonPanel.add(button5);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH); // Mutarea panoului de butoane la baza de jos

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Metodă pentru a crea un buton cu colțuri rotunjite
    private JButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setPreferredSize(new Dimension(100, 40)); // Dimensiuni personalizate
        return button;
    }

    // Panou personalizat pentru a desena imaginea de fundal
    private class ImagePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // Clasă personalizata pentru butoane cu colturi rotunjite



}
