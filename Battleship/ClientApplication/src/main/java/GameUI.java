import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameUI extends JFrame {
    private GameClient client;
    private JTextArea textArea;
    private JTextField inputField;
    private JPanel[][] opponentGrid;
    private JPanel[][] playerGrid;
    private JLabel timerLabel;
    private Timer timer;
    private long gameDurationMillis = 600000; // 10 minutes
    private long startTime;
    private boolean isPlayerTurn;

    public GameUI(String serverAddress, int serverPort) {
        setTitle("Battleship Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        opponentGrid = new JPanel[10][10];
        initializeGrids(opponentGrid, false);
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel opponentPanel = createBoardPanel(opponentGrid, "Opposing Grid");
        leftPanel.add(opponentPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        buttonsPanel.add(new JLabel("  "));
        JButton removeShipButton = new JButton("Remove Ship");
        JButton startButton = new JButton("Start Game");

        buttonsPanel.add(removeShipButton);
        buttonsPanel.add(new JLabel("    "));
        buttonsPanel.add(startButton);
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(new JLabel(" \n "));

        removeShipButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(buttonsPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());

        playerGrid = new JPanel[10][10];
        initializeGrids(playerGrid, true);

        rightPanel.add(createBoardPanel(playerGrid, "My Board"), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea(10, 10);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        controlPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.addActionListener(e -> {
            String command = inputField.getText();
            inputField.setText("");
            client.sendCommand(command);
            if ("exit".equalsIgnoreCase(command.trim())) {
                closeWindow();
            }
        });
        controlPanel.add(inputField, BorderLayout.SOUTH);

        timerLabel = new JLabel("Time: 10:00");
        JPanel timerPanel = new JPanel();
        timerPanel.add(timerLabel);
        controlPanel.add(timerPanel, BorderLayout.NORTH);

        rightPanel.add(controlPanel, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);

        isPlayerTurn = false;
        startTime = System.currentTimeMillis();
        timer = new Timer(1000, e -> updateTimer());
        timer.start();

        try {
            client = new GameClient(serverAddress, serverPort, this::handleServerResponse);
            client.connect();
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }

    private void updateTimer() {
        if (!isPlayerTurn) {
            return;
        }

        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        long remainingTimeMillis = gameDurationMillis - elapsedTimeMillis;

        if (remainingTimeMillis <= 0) {
            timer.stop();
            timerLabel.setText("Time: 00:00");
            JOptionPane.showMessageDialog(this, "Time's up!");
            // Add logic here to handle end of the player's turn due to timeout
        } else {
            int minutes = (int) (remainingTimeMillis / 600000);
            int seconds = (int) ((remainingTimeMillis / 1000) % 60);
            timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
        }
    }

    private void initializeGrids(JPanel[][] grid, boolean isPlayer) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                grid[i][j] = new JPanel();
                grid[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                grid[i][j].setPreferredSize(new Dimension(30, 30));
                if (!isPlayer) {
                    int finalI = i;
                    int finalJ = j;
                    grid[i][j].addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            sendMove(finalI, finalJ);
                        }
                    });
                }
            }
        }
    }

    private JPanel createBoardPanel(JPanel[][] grid, String title) {
        JPanel boardPanel = new JPanel(new BorderLayout());
        JPanel gridPanel = new JPanel(new GridLayout(11, 11));

        gridPanel.add(new JLabel(""));
        for (int j = 0; j < 10; j++) {
            gridPanel.add(new JLabel(String.valueOf(j), SwingConstants.CENTER));
        }
        for (int i = 0; i < 10; i++) {
            gridPanel.add(new JLabel(String.valueOf(i), SwingConstants.CENTER));
            for (int j = 0; j < 10; j++) {
                gridPanel.add(grid[i][j]);
            }
        }

        boardPanel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
        boardPanel.add(gridPanel, BorderLayout.CENTER);
        return boardPanel;
    }

    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(response + "\n");
            System.out.println("Received response from server: " + response);

            if (response.startsWith("Enter your name")) {
                String playerName = JOptionPane.showInputDialog(this, "Enter your name:");
                if (playerName != null && !playerName.trim().isEmpty()) {
                    client.sendCommand("name " + playerName.trim());
                }
            } else if (response.startsWith("Your turn!")) {
                isPlayerTurn = true;
                startTime = System.currentTimeMillis();
                timer.start();
                JOptionPane.showMessageDialog(this, "It's your turn!");

            } else if (response.startsWith("Opponent's turn!")) {
                isPlayerTurn = false;
                timer.stop();
                JOptionPane.showMessageDialog(this, "It's the opponent's turn!");

            } else if (response.startsWith("Game over! You have lost the game.") ||
                    response.startsWith("Game over! You have won the game.(Time's up for opponent)") ||
                    response.startsWith("Game started!") ||
                    response.startsWith("Congratulations!") ||
                    response.startsWith("Need two players to start the game.") ||
                    response.startsWith("All players must place all ships before starting the game.") ||
                    response.startsWith("Game over!")) {
                JOptionPane.showMessageDialog(this, response);
            } else if (response.startsWith("Hit") || response.startsWith("Miss") || response.startsWith("Sunk")) {
                String[] parts = response.split(" ");
                String result = parts[0];
                String coordinates = parts[1] + " " + parts[2];
                updateGrid(result, coordinates);
            } else if (response.startsWith("Am afisat tabla de joc aici:")) {
                String boardState = response.substring("Am afisat tabla de joc aici: ".length());
                System.out.println("MyBoard : " + boardState);
                updatePlayerGrid(boardState);
            } else if (response.startsWith("Ships positions set!")) {
                sendDisplayBoard();
            }
        });
    }

    private void updateGrid(String result, String coordinates) {
        System.out.println("Updating grid with result: " + result + " at " + coordinates);

        String[] parts = coordinates.split(" ");
        if (parts.length >= 2) {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            JPanel targetPanel = opponentGrid[x][y];
            if ("Hit".equals(result)) {
                targetPanel.setBackground(Color.GREEN);
            } else if ("Miss".equals(result)) {
                targetPanel.setBackground(Color.RED);
            } else {
                targetPanel.setBackground(Color.YELLOW);
            }

            targetPanel.repaint();
        }
    }

    private void updatePlayerGrid(String boardState) {
        String[] rows = boardState.split("\\[");
        for (int i = 0; i < rows.length; i++) {
            rows[i] = rows[i].replaceAll("[\\[\\] ]", "");
            String[] cells = rows[i].split(",");
            for (int j = 0; j < cells.length; j++) {
                if (!cells[j].isEmpty()) {
                    int cellValue = Integer.parseInt(cells[j]);
                    JPanel targetPanel = playerGrid[i - 1][j];
                    if (cellValue == 1) {
                        targetPanel.setBackground(Color.BLUE);
                    } else {
                        targetPanel.setBackground(Color.WHITE);
                    }
                    targetPanel.repaint();
                }
            }
        }
    }

    private void sendMove(int x, int y) {
        String command = "make move " + x + " " + y;
        System.out.println("Sending move: " + command);
        client.sendCommand(command);
    }

    private void sendDisplayBoard() {
        String command = "display board";
        System.out.println("Sending command: " + command);
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
