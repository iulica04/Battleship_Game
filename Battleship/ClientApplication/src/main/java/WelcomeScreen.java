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

        // Crearea unui JPanel pentru a conține JLabel și ajustarea dimensiunilor
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BorderLayout());
        welcomePanel.setPreferredSize(new Dimension(800, 150)); // Setăm dimensiunea preferată
        welcomePanel.setBackground(Color.BLUE); // Setăm culoarea de fundal la albastru

        JLabel welcomeLabel = new JLabel("Welcome to Battleship Game", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE); // Setăm culoarea textului la alb
        welcomeLabel.setPreferredSize(new Dimension(800, 100)); // Setăm dimensiunea preferată pentru JLabel
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

        // Adăugarea welcomePanel în partea de sus (North) a ferestrei
        add(welcomePanel, BorderLayout.NORTH);

        // Crearea unui JPanel pentru a conține butoanele și ajustarea dimensiunilor
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 100)); // Centrăm butoanele în JPanel și setăm spațiul între ele
        buttonPanel.setBackground(Color.white); // Setăm culoarea de fundal la alb

        // Butonul Start New Game
        JButton startButton = new JButton("Start New Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setForeground(Color.red); // Setăm culoarea textului la roșu
        startButton.setPreferredSize(new Dimension(200, 50)); // Setăm dimensiunea preferată pentru buton
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Când butonul este apăsat, deschide interfața principală și închide splash screen-ul
                String serverAddress = "localhost"; // Adresa serverului
                int serverPort = 1502; // Portul serverului
                SwingUtilities.invokeLater(() -> new GameUI(serverAddress, serverPort));
                dispose();
            }
        });
        buttonPanel.add(startButton);

        // Butonul My Board
        JButton myBoardButton = new JButton("My Board");
        myBoardButton.setFont(new Font("Arial", Font.BOLD, 16));
        myBoardButton.setForeground(Color.blue); // Setăm culoarea textului la albastru
        myBoardButton.setPreferredSize(new Dimension(200, 50)); // Setăm dimensiunea preferată pentru buton
        myBoardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Când butonul este apăsat, deschide interfața "My Board" și închide splash screen-ul
                SwingUtilities.invokeLater(() -> new MyBoardUI("localhost", 1502));
                dispose();
            }
        });
        buttonPanel.add(myBoardButton);

        // Adăugarea buttonPanel în partea centrală a ferestrei
        add(buttonPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null); // Centrează fereastra pe ecran
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeScreen());
    }
}
