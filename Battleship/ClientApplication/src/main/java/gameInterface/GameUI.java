package gameInterface;

import client.GameClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameUI extends JFrame {
    private GameClient client;
    //ce va fi Chat area
    private JTextArea textArea;
    private JTextField inputField;
    private JPanel[][] opponentGrid; //opponent board
    private JPanel[][] playerGrid; //player board

    public GameUI(String serverAddress, int serverPort) {
        // main farme
        setTitle("Battleship Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // opponent's view (left panel)
        opponentGrid = new JPanel[10][10];
        initializeGrids(opponentGrid, false);

        JPanel opponentPanel = createBoardPanel(opponentGrid, "Opposing Grid");

        // right panel =player's view + control panel
        JPanel rightPanel = new JPanel(new BorderLayout());

        // player's view
        playerGrid = new JPanel[10][10];
        initializeGrids(playerGrid, true);

        // Add the player's board panel to the center of the right panel
        rightPanel.add(createBoardPanel(playerGrid, "My Board"), BorderLayout.CENTER);

        // control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea(10, 10);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        controlPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);//display the chat area

        inputField = new JTextField();
        inputField.addActionListener(e -> {
            String command = inputField.getText();
            inputField.setText("");
            client.sendCommand(command);
            if ("exit".equalsIgnoreCase(command.trim()))
                closeWindow();
        });
        controlPanel.add(inputField, BorderLayout.SOUTH); // write the command

        rightPanel.add(controlPanel, BorderLayout.SOUTH);

        // Add the opponent's view and the right panel to the main panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.add(opponentPanel);
        mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);

        // Try to connect to the server and initialize the client
        try {
            client = new GameClient(serverAddress, serverPort, this::handleServerResponse);
            client.connect();
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }

    // Initialize the grid
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

        // Add row and column headers
        gridPanel.add(new JLabel("")); // Top-left corner
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

    // Handle server responses and update the UI
    private void handleServerResponse(String response) {

        SwingUtilities.invokeLater(() -> {  // Ensure UI responsiveness
            textArea.append(response + "\n");
            System.out.println("Received response from server: " + response);

            if (response.startsWith("Enter your name")) {
                String playerName = JOptionPane.showInputDialog(this, "Enter your name:");
                if (playerName != null && !playerName.trim().isEmpty())
                    client.sendCommand("name " + playerName.trim());

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

            } else if (response.startsWith("Am afisat tabla de joc aici:")) {
                String boardState = response.substring("Am afisat tabla de joc aici: ".length());
                System.out.println("MyBoard : " + boardState);
                updatePlayerGrid(boardState);

            } else if (response.startsWith("Game over!"))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("Congratulations!"))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("Need two players to start the game."))
                JOptionPane.showMessageDialog(this, response);

            else if (response.startsWith("All players must place all ships before starting the game."))
                JOptionPane.showMessageDialog(this, response);
        });
    }

    //Opponent's grid update
    private void updateGrid(String result, String coordinates) {
        System.out.println("Updating grid with result: " + result + " at " + coordinates);

        String[] parts = coordinates.split(" ");
        if (parts.length >= 2) {

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            JPanel targetPanel = opponentGrid[x][y];
            if ("Hit".equals(result))
                targetPanel.setBackground(Color.GREEN);
            else if ("Miss".equals(result))
                targetPanel.setBackground(Color.RED);
            else
                targetPanel.setBackground(Color.YELLOW);

            targetPanel.repaint();
        }
    }

    //Player's grid update => Convert boardState string (from display board command) to 2D array
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

    private void closeWindow() {
        client.close();
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeScreen());
    }
}