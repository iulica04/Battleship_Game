package gameInterface;

import client.GameClient;

import javax.swing.*;
import java.awt.*;
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

    public PlayerScreen(String playerName) {

        this.playerName = playerName;

        try {
            client = new GameClient(serverAddress, serverPort, this::handleServerResponse);
            client.connect();
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }

        try {
            backgroundImage = ImageIO.read(new File("E:\\OneDrive\\Desktop\\1299.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setTitle("Player Screen");
        setSize(1000, 700);
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
        buttonPanel.setOpaque(false); // Să fie transparent, pentru a vedea imaginea de fundal
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20)); // Marginea panoului

        // Crearea butoanelor
        JButton button1 = createButton("Create Game");
        JButton button2 = createButton("Join Game");
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

    private void handleServerResponse(String response) {
        System.out.println("Received response from server: " + response);

        if (response.startsWith("waiting for your name...")) {
            client.sendCommand(playerName.trim());

            //'move attack'
        }
    }

    // Clasă personalizată pentru butoane cu colțuri rotunjite
    private class RoundedButton extends JButton {
        private Color pressedBackgroundColor;
        private Color hoverBackgroundColor;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBackground(cadetGrey);
            setForeground(tyrianPurple);
            pressedBackgroundColor = taupeGray;
            hoverBackgroundColor = mintGreen;
            init();
        }

        private void init() {
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    setBackground(hoverBackgroundColor);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    setBackground(cadetGrey);
                }
            });

            addChangeListener(e -> {
                if (getModel().isPressed()) {
                    setBackground(pressedBackgroundColor);
                } else if (getModel().isRollover()) {
                    setBackground(hoverBackgroundColor);
                } else {
                    setBackground(cadetGrey);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isArmed()) {
                g.setColor(pressedBackgroundColor);
            } else if (getModel().isRollover()) {
                g.setColor(hoverBackgroundColor);
            } else {
                g.setColor(getBackground());
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }
    }


    public static void main(String[] args) {
        new PlayerScreen("Player 1");
    }
}
