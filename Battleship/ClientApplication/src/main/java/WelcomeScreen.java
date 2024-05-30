import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class WelcomeScreen extends JFrame {
    public WelcomeScreen() {
        setTitle("Welcome to Battleship Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BorderLayout());
        welcomePanel.setPreferredSize(new Dimension(800, 150));
        welcomePanel.setBackground(Color.BLUE);

        JLabel welcomeLabel = new JLabel("Welcome to Battleship Game", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setPreferredSize(new Dimension(800, 100));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

        add(welcomePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 100));
        buttonPanel.setBackground(Color.white);

        // Butonul Start New Game
        JButton startButton = new JButton("Start New Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setForeground(Color.red);
        startButton.setPreferredSize(new Dimension(200, 50));
       startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String serverAddress = "localhost";
                int serverPort = 1502;
               // SwingUtilities.invokeLater(() -> new GameUI(serverAddress, serverPort));
                dispose();
            }
        });
        buttonPanel.add(startButton);

        // Butonul My Board
        JButton myBoardButton = new JButton("My Board");
        myBoardButton.setFont(new Font("Arial", Font.BOLD, 16));
        myBoardButton.setForeground(Color.blue);
        myBoardButton.setPreferredSize(new Dimension(200, 50));
        myBoardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> new MyBoardUI("localhost", 1502));
                dispose();
            }
        });
        buttonPanel.add(myBoardButton);

        add(buttonPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeScreen());
    }
}
