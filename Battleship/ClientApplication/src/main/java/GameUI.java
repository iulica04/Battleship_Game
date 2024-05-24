
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameUI extends JFrame {
    private GameClient client;
    private JTextArea textArea;
    private JTextField inputField;
    private JPanel[][] opponentGrid;

    public GameUI(String serverAddress, int serverPort) {
        //configurarea mainFrame
        setTitle("Battleship Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //initializarea oponentului view
        opponentGrid = new JPanel[11][11];
        initializeGrids(opponentGrid, false);

        // se adauga oponentul view la mainPanel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.add(createBoardPanel(opponentGrid, "Opposing Grid"));

        // Configurarea panoului de control
        JPanel controlPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea(20, 20);
        textArea.setEditable(false);
        controlPanel.add(new JScrollPane(textArea), BorderLayout.CENTER); // se adauga jumatatea pentru scrierea comenzilor si afisarea rasp de la server

        inputField = new JTextField();
        inputField.addActionListener(e -> {
            String command = inputField.getText();
            inputField.setText("");
            client.sendCommand(command);
            if ("exit".equalsIgnoreCase(command.trim()))
                closeWindow();
        });
        controlPanel.add(inputField, BorderLayout.SOUTH);// se adauga loc pentru scrierea comenzilor

        // se adauga panoul de control la mainPanel
        mainPanel.add(controlPanel);
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);

        // se incearca conectarea la server si initializara clientului
        try {
            client = new GameClient(serverAddress, serverPort, this::handleServerResponse);
            client.connect();
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }

    //initializarea gridului
    private void initializeGrids(JPanel[][] grid, boolean isPlayer) {
        grid[0][0] = new JPanel();
        for (int i = 1; i <= 10; i++) {
            grid[i][0] = new JPanel(new BorderLayout());
            grid[i][0].add(new JLabel(String.valueOf(i), SwingConstants.CENTER), BorderLayout.CENTER);
            grid[0][i] = new JPanel(new BorderLayout());
            grid[0][i].add(new JLabel(String.valueOf(i), SwingConstants.CENTER), BorderLayout.CENTER);
        }
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                grid[i][j] = new JPanel();
                grid[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                grid[i][j].setPreferredSize(new Dimension(30, 30));
                if (!isPlayer) {
                    int finalI = i;
                    int finalJ = j;
                    grid[i][j].addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            sendMove(finalI - 1, finalJ - 1);
                        }
                    });
                }
            }
        }
    }



    private JPanel createBoardPanel(JPanel[][] grid, String title) {
        JPanel boardPanel = new JPanel(new BorderLayout());
        JPanel gridPanel = new JPanel(new GridLayout(11, 11));
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++)
                gridPanel.add(grid[i][j]);
        }
        boardPanel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
        boardPanel.add(gridPanel, BorderLayout.CENTER);
        return boardPanel;
    }

    private JPanel createMyGridPanel(boolean isPlayer) {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new BorderLayout());
        String titleText = isPlayer ? "My Grid" : "Opponent Grid";
        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gridPanel.add(titleLabel, BorderLayout.NORTH);
        JPanel cellsPanel = new JPanel(new GridLayout(11, 11));
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if (i == 0 || j == 0) {
                    JLabel label = new JLabel();
                    if (i == 0 && j > 0)
                        label.setText(String.valueOf(j));
                    else if (j == 0 && i > 0)
                        label.setText(String.valueOf(i));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    cellsPanel.add(label);
                } else {
                    JPanel panel = new JPanel();
                    panel.setBorder(BorderFactory.createLineBorder(Color.black));
                    panel.setPreferredSize(new Dimension(20, 20));
                    cellsPanel.add(panel);
                    if (!isPlayer) {
                        int finalI = i - 1;
                        int finalJ = j - 1;
                        panel.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseClicked(java.awt.event.MouseEvent evt) {
                                sendMove(finalI, finalJ);
                            }
                        });
                    }
                }
            }
        }
        gridPanel.add(cellsPanel, BorderLayout.CENTER);
        return gridPanel;
    }

    //trateaza raspunsurile primite de la server si actualizeaza interfata grafica
    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {  //asigura o interfata grafica responsive.
            textArea.append(response + "\n"); //rasp de la server
            System.out.println("Received response from server: " + response);

            if (response.startsWith("Enter your name")) {
                String playerName = JOptionPane.showInputDialog(this, "Enter your name:");
                if (playerName != null && !playerName.trim().isEmpty())
                    client.sendCommand("name " + playerName.trim());

                //'move attack'
            } else if (response.startsWith("Game over! You have lost the game."))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("Game over! You have won the game.(Time's up for opponent)"))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("Game started!"))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("Hit") || response.startsWith("Miss") || response.startsWith("Sunk")) {
                String[] parts = response.split(" ");
                String result = parts[0];
                String coordinates = parts[1] + " " + parts[2];
                updateGrid(result, coordinates);

            } else if (response.startsWith("Game over!"))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("Congratulations!"))
                JOptionPane.showMessageDialog(this, response);
                //'start game'
            else if (response.startsWith("Game started!"))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("Need two players to start the game."))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("All players must place all ships before starting the game."))
                JOptionPane.showMessageDialog(this, response);

        });
    }

    private void updateGrid(String result, String coordinates) {
        System.out.println("Updating grid with result: " + result + " at " + coordinates);

        // Split după spațiu pentru a separa coordonatele x și y
        String[] parts = coordinates.split(" ");

        // Verificăm dacă avem suficiente părți pentru a extrage coordonatele
        if (parts.length >= 2) {
            // Extragem coordonatele x și y și le convertim în numere întregi
            int x = Integer.parseInt(parts[0]); // Scădem 1 pentru a ajusta la indexarea de la 0
            int y = Integer.parseInt(parts[1]); // Scădem 1 pentru a ajusta la indexarea de la 0

            // Actualizăm panoul corespunzător pe baza rezultatului primit
            JPanel targetPanel = opponentGrid[x][y];
            if ("Hit".equals(result))
                targetPanel.setBackground(Color.GREEN);
            else if ("Miss".equals(result))
                targetPanel.setBackground(Color.RED);
            else targetPanel.setBackground(Color.YELLOW);

            targetPanel.repaint();
        }
    }



    private void sendMove(int x, int y) {
        String command = "move attack " + (x+1) + " " + (y + 1);
        System.out.println("Sending move: " + command);
        client.sendCommand(command);
    }

    private void closeWindow() {
        client.close();
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeScreen());
    }
}
