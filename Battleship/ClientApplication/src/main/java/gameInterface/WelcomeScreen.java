package gameInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WelcomeScreen extends JFrame {
    public WelcomeScreen() {

        //COLORS
        Color cerulean = Color.decode("#1C6989");
        Color lightBlue = Color.decode("#96C1CE");
        Color cosmicLatte = Color.decode("#FEFAEA");
        Color outerSpace = Color.decode("#455257");
        Color rawUmber = Color.decode("#956536");
        Color cadetGrey = Color.decode("#91A6AE");


        setTitle("Welcome to Battleship Game");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BorderLayout());
        welcomePanel.setPreferredSize(new Dimension(800, 400));
        welcomePanel.setBackground(cerulean);

        JLabel welcomeLabel = new JLabel("Welcome to Battleship Game!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 40));
        welcomeLabel.setForeground(cosmicLatte);
        welcomeLabel.setPreferredSize(new Dimension(600, 100));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBorder(new EmptyBorder(20, 60, 20, 60));
        namePanel.setBackground(lightBlue);

        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setFont(new Font("Serif", Font.BOLD, 30));
        nameLabel.setBorder(new EmptyBorder(0, 60, 20, 0));
        nameLabel.setForeground(Color.WHITE);

        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Serif", Font.PLAIN, 20));
        nameField.setPreferredSize(new Dimension(100, 40));
        nameField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
        namePanel.add(nameLabel);
        namePanel.add(nameField);


        welcomePanel.add(namePanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 50));
        buttonPanel.setBackground(cosmicLatte);

        JButton startButton = new JButton("Start Game");
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
                String playerName = nameField.getText();
                if (playerName.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter your name", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    SwingUtilities.invokeLater(() -> new PlayerScreen(playerName));
                    dispose();
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
            originalImage = ImageIO.read(new File("resources/utils/animated-scene-fleet-of-ships-engaging-in-naval-warfare-illuminated-by-explosions-missile-impacts.bmp"));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeScreen());
    }
}
