package gameInterface;

import client.GameClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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
    private String playerName;
    private boolean isPlayerTurn = false;
    private boolean gameOver = false;
    private JLabel statusLabel;

    public GameUI(GameClient client, String playerName) {

        this.playerName = playerName;
        this.client = client;
        client.messageConsumer= this::handleServerResponse;


        setTitle("Battleship Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        opponentGrid = new JPanel[10][10];
        initializeGrids(opponentGrid, false);
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel opponentPanel = createBoardPanel(opponentGrid, "Opposing Grid");
        leftPanel.add(opponentPanel, BorderLayout.CENTER);

        // Initialize status label
        statusLabel = new JLabel("Waiting for an opponent...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Serif", Font.BOLD, 16));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        buttonsPanel.add(new JLabel("  "));
        JButton startGameButton = new JButton("Start Game");
        JButton goToPlayerScreenButton = new JButton("Go to Player Screen");
        goToPlayerScreenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                 SwingUtilities.invokeLater(() -> new PlayerScreen(client, playerName));
                dispose();
            }
        });
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendStartGame();
            }
        });

        buttonsPanel.add(startGameButton);
        buttonsPanel.add(new JLabel("    "));
        buttonsPanel.add(goToPlayerScreenButton);
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(statusLabel);
        buttonsPanel.add(new JLabel(" \n "));
        buttonsPanel.add(new JLabel(" \n "));

        startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        goToPlayerScreenButton.setAlignmentX(Component.CENTER_ALIGNMENT);

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


        startTime = System.currentTimeMillis();
        timer = new Timer(1000, e -> updateTimer());
        timer.start();

        handleServerResponse("display board");

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
            JOptionPane.showMessageDialog(this, "Time's up!You lost the game!");
            isPlayerTurn = false;
        } else {
            int minutes = (int) (remainingTimeMillis / 600000) % 60;
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
                            if (gameOver == true) {
                                JOptionPane.showMessageDialog(null, "Game Over!");
                            }else {
                                if(isPlayerTurn == true) {
                                    sendMove(finalI, finalJ);
                                } else {
                                    JOptionPane.showMessageDialog(null, "It's not your turn!");
                                }
                            }
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
            if (response != null) {
                textArea.append(response + "\n");
                System.out.println("Received response from server: " + response);

                if (response.startsWith("Your turn! Make a move!")) {
                    isPlayerTurn = true;
                    startTime = System.currentTimeMillis();
                    timer.start();
                } else if (response.startsWith("Opponent's turn!")) {
                    isPlayerTurn = false;
                    timer.stop();
                } else if (response.startsWith("Game started!")) {
                    JOptionPane.showMessageDialog(this, response);
                } else if (response.startsWith("Need two players to start the game.") ||
                        response.startsWith("All players must place all ships before starting the game.")) {
                    JOptionPane.showMessageDialog(this, response);
                } else if (!gameOver && response.equals("Time's up! Your turn has ended.")) {
                    isPlayerTurn = false;
                    gameOver = true;
                    JOptionPane.showMessageDialog(this, "Time's up! Your turn has ended. You lost the game!");
                    resetGame();
                } else if (!gameOver && response.equals("Time's up! Your opponent's turn has ended.")) {
                    isPlayerTurn = false;
                    gameOver = true;
                    JOptionPane.showMessageDialog(this, "Time's up! Your turn has ended. You have WON!");
                    resetGame();
                } else if (response.contains("Game over! The winner is") ||
                        response.contains("Game over due time up!")) {
                    isPlayerTurn = false;
                    gameOver = true;
                    JOptionPane.showMessageDialog(this, response);
                    resetGame();
                } else if (response.contains("Congratulations!")) {
                    String[] parts = response.split(" ");
                    String result = parts[0];
                    String coordinates = parts[1] + " " + parts[2];
                    updateGrid(result, coordinates);
                    isPlayerTurn = false;
                    gameOver = true;
                    JOptionPane.showMessageDialog(this, response);
                    resetGame();
                } else if (response.contains("All your ships have been sunk!")) {
                    String[] parts = response.split(" ");
                    int x = Integer.parseInt(parts[5]);
                    int y = Integer.parseInt(parts[6]);
                    boolean hit = parts[3].equals("hit");
                    updatePlayerGridOpponent(x, y, hit);
                    isPlayerTurn = false;
                    gameOver = true;
                    JOptionPane.showMessageDialog(this, response);
                    resetGame();
                } else if (response.startsWith("Hit") || response.startsWith("Miss")) {
                    String[] parts = response.split(" ");
                    String result = parts[0];
                    String coordinates = parts[1] + " " + parts[2];
                    updateGrid(result, coordinates);
                } else if (response.startsWith("Am afisat tabla de joc aici:")) {
                    String boardState = response.substring("Am afisat tabla de joc aici: ".length());
                    System.out.println("MyBoard : " + boardState);
                    updatePlayerGrid(boardState);
                } else if (response.startsWith("display board")) {
                    sendDisplayBoard();
                } else if (response.startsWith("Your ship was")) {
                    String[] parts = response.split(" ");
                    int x = Integer.parseInt(parts[5]);
                    int y = Integer.parseInt(parts[6]);
                    boolean hit = parts[3].equals("hit");
                    updatePlayerGridOpponent(x, y, hit);
                } else if (response.contains(" has joined the game!")) {
                    statusLabel.setText(response);
                    isPlayerTurn = true;
                    //sendStartGame();
                } else if (response.contains("You have joined the game!")) {
                    statusLabel.setText(response);
                }
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

            if(isPlayerTurn == true){
                if ("Hit".equals(result)) {
                    targetPanel.setBackground(Color.GREEN);
                    isPlayerTurn= true;
                } else if ("Miss".equals(result)) {
                    targetPanel.setBackground(Color.RED);
                    isPlayerTurn=false;
                }
            }else{
                JOptionPane.showMessageDialog(this, "It's not your turn!");

            }
            targetPanel.repaint();
        }
    }
    private void updatePlayerGridOpponent(int x, int y, boolean hit) {
        JPanel targetPanel = playerGrid[x][y];

        if (hit == true) {
            targetPanel.setBackground(Color.RED);
        } else if (hit == false) {
            targetPanel.setBackground(Color.PINK);
        }

        targetPanel.repaint();

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
    private void resetGame() {
        timer.stop();
        gameOver = false;
        isPlayerTurn = false;
        startTime = 0;
        timerLabel.setText("Time: 10:00");
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
    private void sendStartGame() {
        String command = "start game";
        System.out.println("Sending command: " + command);
        client.sendCommand(command);
    }
    private void sendClearBords() {
        String command = "clear boards";
        System.out.println("Sending command: " + command);
        client.sendCommand(command);
    }

    private void closeWindow() {
        client.close();
        dispose();
        System.exit(0);
    }

}
