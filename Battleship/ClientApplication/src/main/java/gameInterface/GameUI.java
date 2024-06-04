package gameInterface;

import client.GameClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import gameInterface.elements.RoundedButton;



public class GameUI extends JFrame {
    private GameClient client;
    private JPanel[][] opponentGrid;
    private JPanel[][] playerGrid;
    JPanel titlePanel;
    JLabel titleLabel;
    JPanel timerPanel;
    private JLabel timerLabel;
    private Timer timer;
    private long gameDurationMillis = 30000; // 1 minutes

    private long startTime;
    private String playerName;
    private boolean isPlayerTurn = false;

    private JLabel statusLabel;
    Color cadetGrey = Color.decode("#91A6AE");
    Color delftBlue = Color.decode("#414163");
    Color greenStart = Color.decode("#34be76");
    Color redStop = Color.decode("#ff2c2c");
    Color mintGreen = Color.decode("#CDF2EB");
    int idGame = 0;
    boolean startGame = false;
    public boolean gameOver = false;


    public GameUI(GameClient client, String playerName) {

        this.playerName = playerName;
        this.client = client;
        client.setMessageConsumer(this::handleServerResponse);

        setTitle("Battleship Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        /////////////////////TITLE PANEL
        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(1200, 70));
        titlePanel.setBackground(delftBlue);

         titleLabel = new JLabel("Good luck!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 30));
        titleLabel.setForeground(cadetGrey);

        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Initialize status label
        statusLabel = new JLabel("", SwingConstants.CENTER);
       // statusLabel = new JLabel("Game " + idGame + " : Waiting for an opponent...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Serif", Font.BOLD, 20));
        statusLabel.setForeground(Color.white);
        titlePanel.add(statusLabel, BorderLayout.SOUTH);

        //TIMER
        timerLabel = new JLabel("Time: 00:30");
        timerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        timerLabel.setForeground(Color.RED);
        timerPanel = new JPanel();
        timerPanel.add(timerLabel);
        timerPanel.setBackground(delftBlue);
        titlePanel.add(timerPanel, BorderLayout.EAST);

       /////////////////////////LEFT PANEL
        opponentGrid = new JPanel[10][10];
        initializeGrids(opponentGrid, false);
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel opponentPanel = createBoardPanel(opponentGrid, "Opposing Grid");
        leftPanel.add(opponentPanel, BorderLayout.CENTER);

        //////////////////////RIGHT PANEL
        JPanel rightPanel = new JPanel(new BorderLayout());
        playerGrid = new JPanel[10][10];
        initializeGrids(playerGrid, true);
        rightPanel.add(createBoardPanel(playerGrid, "My Board"), BorderLayout.CENTER);


        //Buttons Panel (Edit)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 10)); // Centrarea butoanelor
        buttonPanel.setOpaque(true); // Sa fie transparent, pentru a vedea imaginea de fundal
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20)); // Marginea panoului

        buttonPanel.add(new JLabel("If you are ready:  "));
        buttonPanel.setFont(new Font("Serif", Font.BOLD, 10));
        JButton startGameButton = createButton("Start Game");
        JButton goToPlayerScreenButton = createButton("Player Screen");
        buttonPanel.add(startGameButton);
        buttonPanel.add(new JLabel("    "));
        buttonPanel.add(new JLabel("Go back to Player Screen:   "));
        buttonPanel.add(goToPlayerScreenButton);
        buttonPanel.setBackground(Color.LIGHT_GRAY);

        goToPlayerScreenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendCommand("player left game");
                resetGame();
                SwingUtilities.invokeLater(() -> new PlayerScreen(client, playerName));
                dispose();
            }
        });

        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendStartGame();
                startGame = true;
            }
        });


        ///FINAL
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setBackground(mintGreen);

        setVisible(true);

        startTime = System.currentTimeMillis();
        timer = new Timer(1000, e -> updateTimer());



        handleServerResponse("display board");
    }
    private JButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setPreferredSize(new Dimension(100, 40)); // Dimensiuni personalizate
        return button;
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
            JOptionPane.showMessageDialog(this, "Time's up! You lost the game!");
            isPlayerTurn = false;
        } else {
            int minutes = (int) (remainingTimeMillis / 60000);
            int seconds = (int) ((remainingTimeMillis % 60000) / 1000);
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
                            }else if(startGame == false){
                                JOptionPane.showMessageDialog(null, "The game didn't start!You can't make a move.");
                            }else {
                                if (isPlayerTurn == true) {
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
            if (response != null && !gameOver) {
                System.out.println("Received response from server GameUi: " + response);

                if (response.startsWith("Your turn! Make a move!")) {
                    isPlayerTurn = true;
                    startTime = System.currentTimeMillis();
                    timer.start();

                } else if (response.startsWith("Opponent's turn!")) {
                    isPlayerTurn = false;
                    timer.stop();

                } else if (response.startsWith("Game started!")) {
                    statusLabel.setText("Game started!");
                    startGame = true;
                    titlePanel.setBackground(greenStart);
                    timerPanel.setBackground(greenStart);
                    titleLabel.setForeground(Color.black);

                } else if (response.startsWith("Need two players to start the game.") ||
                        response.startsWith("All players must place all ships before starting the game.")) {
                        statusLabel.setText(response);

                } else if (!gameOver && response.equals("Time's up! Your turn has ended.")) {
                    gameOver = true;
                    isPlayerTurn = false;
                    statusLabel.setText("Time's up! Your turn has ended. You lost the game!");
                    resetGame();
                    sendGameOver();
                    exitDoor("Battleship/ClientApplication/src/main/resources/utils/lose.jpg","Time's up! Your turn has ended. You lost the game!");


                } else if (!gameOver && response.equals("Time's up! Your opponent's turn has ended.")) {
                    gameOver = true;
                    isPlayerTurn = false;
                    statusLabel.setText("You have WON! You opponent time has ended!");
                    resetGame();
                    sendGameOver();
                    exitDoor("Battleship/ClientApplication/src/main/resources/utils/winner.jpg","You have WON! You opponent time has ended!");


                } /*else if (response.contains("Game over! The winner is") ||
                        response.contains("Game over due time up!")) {
                    gameOver = true;
                    isPlayerTurn = false;
                    statusLabel.setText(response);
                    resetGame();
                    sendGameOver();
                    exitDoor("Battleship/ClientApplication/src/main/resources/utils/lose.jpg",response);


                }*/ else if (response.contains("Congratulations!")) {
                    //ultima mutare
                    String[] parts = response.split(" ");
                    String result = parts[0];
                    String coordinates = parts[1] + " " + parts[2];
                    updateGrid(result, coordinates);

                    isPlayerTurn = false;
                    gameOver = true;
                    statusLabel.setText("Congratulations! You have WON the game!");
                    resetGame();
                    sendGameOver();
                    exitDoor("Battleship/ClientApplication/src/main/resources/utils/winner.jpg","Congratulations! You have WON the game!");


                } else if (response.contains("All your ships have been sunk!")) {
                    //ultima mutare
                    String[] parts = response.split(" ");
                    int x = Integer.parseInt(parts[5]);
                    int y = Integer.parseInt(parts[6]);
                    boolean hit = parts[3].equals("hit");
                    updatePlayerGridOpponent(x, y, hit);

                    isPlayerTurn = false;
                    gameOver = true;
                    statusLabel.setText("All your ships have been sunk! You lost the game!");
                    resetGame();
                    sendGameOver();
                    exitDoor("Battleship/ClientApplication/src/main/resources/utils/lose.jpg","All your ships have been sunk! You lost the game!");


                } else if(response.startsWith("Game over! It's a tie!")){
                    isPlayerTurn = false;
                    gameOver = true;
                    statusLabel.setText("Game over! It's a tie! It lasted too long!");
                    resetGame();
                    sendGameOver();
                    exitDoor("Battleship/ClientApplication/src/main/resources/utils/it'satie.jpg","Game over! It's a tie! It lasted too long!");


                }else if (response.startsWith("Hit") || response.startsWith("Miss")) {
                    String[] parts = response.split(" ");
                    String result = parts[0];
                    String coordinates = parts[1] + " " + parts[2];
                    updateGrid(result, coordinates);

                } else if (response.startsWith("Am afisat tabla de joc aici:")) {
                    String boardState = response.substring("Am afisat tabla de joc aici: ".length());
                    updatePlayerGrid(boardState);

                } else if (response.startsWith("display board")) {
                    sendDisplayBoard();

                } else if (response.startsWith("Your ship was")) {
                    //asa colorez pe myBoard ul opponentului cu lovitura mea
                    String[] parts = response.split(" ");
                    int x = Integer.parseInt(parts[5]);
                    int y = Integer.parseInt(parts[6]);
                    boolean hit = parts[3].equals("hit");
                    updatePlayerGridOpponent(x, y, hit);

                } else if (response.contains(" has joined the game!")) {
                    statusLabel.setText(response);
                    statusLabel.setText("The other player has joined the game! Now, you can START THE GAME!!!!!!!");

                } else if (response.contains("You joined the game with id")) {
                    String[] parts = response.split(" ");
                    int idGame = Integer.parseInt(parts[6]);
                    String playerName = parts[8];
                    statusLabel.setText("Game " + idGame + ": The game is full, " + playerName + " be ready  to play!");

                } else if (response.startsWith("Game created with")) {
                    String[] parts = response.split(" ");
                    idGame = Integer.parseInt(parts[3]);
                    statusLabel.setText("Game " + idGame + " : Waiting for an opponent...");

                }else if(response.startsWith("The other player left!")){
                    resetGame();
                    exitDoor("Battleship/ClientApplication/src/main/resources/utils/winner.jpg",response);
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
        timerLabel.setText("Time: 00:30");
       // sendClearBords();
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
    private void sendGameOver( ) {
        String command = "game over";
        System.out.println("Sending command: " + command);
        client.sendCommand(command);
    }

    private void exitDoor(String path, String response){
        timer.stop();
        ImageIcon gameOverIcon = new ImageIcon(path);
        JButton playerScreenButton = createButton("Player Screen");
        JOptionPane optionPane = new JOptionPane(
                null,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                gameOverIcon,
                new Object[]{playerScreenButton}
        );

        playerScreenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    new PlayerScreen(client, playerName);
                    dispose(); // Închide fereastra GameUI
                    optionPane.setValue(JOptionPane.CLOSED_OPTION); // Închide JOptionPane
                });
            }
        });

        JDialog dialog = optionPane.createDialog(this, response);
        dialog.setVisible(true);
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
