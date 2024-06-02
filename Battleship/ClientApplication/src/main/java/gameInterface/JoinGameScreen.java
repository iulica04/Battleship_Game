package gameInterface;

import client.GameClient;
import gameInterface.elements.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JoinGameScreen extends JFrame {
    private GameClient client;
    private JList<String> gamesList;
    private JTextField searchField;
    Color cadetGrey = Color.decode("#91A6AE");
    Color tyrianPurple = Color.decode("#471732");
    Color taupeGray = Color.decode("#7A7885");
    Color mintGreen = Color.decode("#CDF2EB");
    Color delftBlue = Color.decode("#414163");

    public JoinGameScreen(GameClient client) {
        this.client = client;
        client.setMessageConsumer(this::handleServerResponse);

        setTitle("Join Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(1200, 100));
        titlePanel.setBackground(delftBlue);

        JLabel titleLabel = new JLabel("Available games", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(cadetGrey);

        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Panou de căutare
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        JButton searchButton = createButton("Search");
        searchButton.addActionListener(e -> searchGameById());

        // Adăugare buton de refresh
        JButton refreshButton = createButton("\u21BB");
        refreshButton.setPreferredSize(new Dimension(50, 30));
        refreshButton.addActionListener(e -> client.sendCommand("list_games"));

        JPanel searchAndRefreshPanel = new JPanel(new BorderLayout());
        searchAndRefreshPanel.add(searchField, BorderLayout.CENTER);
        searchAndRefreshPanel.add(searchButton, BorderLayout.EAST);

        searchPanel.add(searchAndRefreshPanel, BorderLayout.CENTER);
        searchPanel.add(refreshButton, BorderLayout.WEST);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        gamesList = new JList<>();
        gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(gamesList);
        gamesList.setFixedCellHeight(50);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panou de butoane
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton joinButton = createButton("Join Selected Game");
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedGame = gamesList.getSelectedValue();
                if (selectedGame != null) {
                    String[] parts = selectedGame.split(" ");
                    String gameId = parts[2];
                    String command = "join game " + gameId;
                    System.out.println("Sending command: " + command);
                    client.sendCommand(command);
                    // dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a game to join", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton randomButton = createButton("Random Game");
        randomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] games = new String[gamesList.getModel().getSize()];
                for (int i = 0; i < gamesList.getModel().getSize(); i++) {
                    games[i] = gamesList.getModel().getElementAt(i);
                }
                if (games.length > 0) {
                    String randomGame = games[(int) (Math.random() * games.length)];
                    client.sendCommand("join " + randomGame);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "No games available to join", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(joinButton);
        buttonPanel.add(randomButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);

        // Request the list of games from the server
        client.sendCommand("list_games");
    }

    private void searchGameById() {
        String gameId = searchField.getText().trim();
        if (!gameId.isEmpty()) {
            String[] games = new String[gamesList.getModel().getSize()];
            for (int i = 0; i < gamesList.getModel().getSize(); i++) {
                games[i] = gamesList.getModel().getElementAt(i);
            }
            boolean found = Arrays.stream(games).anyMatch(game -> game.contains(gameId));
            if (found) {
                String[] filteredGames = Arrays.stream(games).filter(game -> game.contains(gameId)).toArray(String[]::new);
                gamesList.setListData(filteredGames);
            } else {
                JOptionPane.showMessageDialog(this, "Game not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void handleServerResponse(String response) {
        System.out.println("Received response: " + response);
        if (response.startsWith("games_list")) {
            response = response.chars()
                    .filter(c -> c != '[' && c != ']')
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining());

            String[] games = response.substring(10).split(", ");
            SwingUtilities.invokeLater(() -> updateGamesList(games));
        } else if (response.contains("You joined the game")) {
            SwingUtilities.invokeLater(() -> new GameUI(client, client.getUsername()));
        } else {
            JOptionPane.showMessageDialog(this, response, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setPreferredSize(new Dimension(150, 40)); // Dimensiuni personalizate
        return button;
    }

    public void updateGamesList(String[] games) {
        gamesList.setListData(games);
    }
}
