import java.util.AbstractMap;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;

/** LOGICA JOCULUI:
 * 3 scenarii de castig
 * -> un jucator loveste totae navele celuilalt jucator in timp util
 * -> timpul se termina: castigatorul va fi cel cu cele mai multe nave nelovite
 * -> jucatorii nu mai au nave, dar timpul nu a expirat  : catigatorul este ales la fel ca in cazul cand timpul expira
 *
 * Cateva reguli:
 * -> o data ce o nava este pusa pe tabla, aceasta nu poate fi remutata
 * -> jucatorul poate muta o nava per tura */
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Game {
    private Player player1;
    private Player player2;
    private Board board1;
    private Board board2;
    private boolean player1Turn;
    private boolean started;
    private Timer gameTimer = new Timer();
    private long gameDurationMillis = 600000;
    private boolean settedShips1 = false;
    private boolean settedShips2 = false;
    private boolean hasPlayerMadeMove = false;
    private final Object moveLock = new Object();
    private boolean gameOver = false;

    public Game(Player player1) {
        this.player1 = player1;
        this.board1 = new Board(10, 5);
        this.board2 = new Board(10, 5);
        this.player1Turn = true;
        this.started = false;
    }

    public synchronized void addSecondPlayer(Player player2) {
        this.player2 = player2;
        player1.sendMessage(player2.getName() + " has joined the game! Enter 'set the ships positions' to set the ships positions!");
    }

    public synchronized boolean allPlayersAreJoined() {
        return player1 != null && player2 != null;
    }

    public synchronized boolean isReadyToStart() {
        return player1 != null && player2 != null && settedShips1 && settedShips2;
    }

    public synchronized void start() {
        if (isReadyToStart()) {
            this.started = true;
            player1.sendMessage("Game started!");
            player2.sendMessage("Game started!");

            gameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    endGame(determineWinner());
                }
            }, gameDurationMillis);

            new Thread(this::runGameLoop).start();
        }
    }

    private void runGameLoop() {
        while (!gameOver) {
            Player currentPlayer = player1Turn ? player1 : player2;
            Player opponentPlayer = player1Turn ? player2 : player1;

            currentPlayer.sendMessage("Your turn! Make a move!");
            currentPlayer.sendMessage(printBoard(currentPlayer));
            opponentPlayer.sendMessage("Opponent's turn!");

            long turnStartTime = System.currentTimeMillis();
            synchronized (moveLock) {
                try {
                    moveLock.wait(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long turnEndTime = System.currentTimeMillis();
            if (turnEndTime - turnStartTime >= 30000) {
                currentPlayer.sendMessage("Time's up! Your turn has ended.");
            }

            if (hasPlayerMadeMove) {
                hasPlayerMadeMove = false;
            }

            player1Turn = !player1Turn;
        }
    }


    public void setShipsPositions(List<AbstractMap.SimpleEntry<Integer, Integer>> shipsPositions, Player player) {
        if (player.equals(player1)) {
            board1.setShipsPositions(shipsPositions);
            settedShips1 = true;
        } else {
            board2.setShipsPositions(shipsPositions);
            settedShips2 = true;
        }

    }


    public boolean makeMove(Player player, int x, int y) {
        if ((player1Turn && player.equals(player1)) || (!player1Turn && player.equals(player2))) {
            Board opponentBoard = player1Turn ? board2 : board1;
            boolean hit = opponentBoard.makeMove(x, y);
            if (opponentBoard.getNumberOfShips() == 0) {
                endGame(player.getName());
            }

            synchronized (moveLock) {
                hasPlayerMadeMove = true;
                moveLock.notifyAll();
            }

            return hit;
        } else {
            return false;
        }
    }

    public String printBoard(Player player) {
        char[][] board = player.equals(player1) ? board1.getBoard() : board2.getBoard();
        StringBuilder sb = new StringBuilder();

        sb.append("Player ").append(player.getName()).append("'s Board:\n");
        sb.append("    ");
        for (int i = 0; i < 10; i++) {
            sb.append(" ").append((char) ('A' + i)).append(" ");
        }
        sb.append("\n");

        sb.append("   +");
        for (int i = 0; i < 10; i++) {
            sb.append("---+");
        }
        sb.append("\n");

        for (int i = 0; i < 10; i++) {
            sb.append(String.format("%2d |", i + 1));
            for (int j = 0; j < 10; j++) {

                sb.append(" ").append(board[i][j]).append(" |");
            }
            sb.append("\n");

            sb.append("   +");
            for (int j = 0; j < 10; j++) {
                sb.append("---+");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private void endGame(String winner) {
        if (winner.equals("It's a tie!")) {
            player1.sendMessage("Game over! It's a tie!");
            player2.sendMessage("Game over! It's a tie!");
        } else {
            player1.sendMessage("Game over! The winner is " + winner);
            player2.sendMessage("Game over! The winner is " + winner);
        }

        gameOver = true;
        gameTimer.cancel();
    }


    private String determineWinner() {
        int player1ShipsLeft = board1.getNumberOfShips();
        int player2ShipsLeft = board2.getNumberOfShips();

        if (player1ShipsLeft > player2ShipsLeft) {
            return player1.getName();
        } else if (player2ShipsLeft > player1ShipsLeft) {
            return player2.getName();
        } else {
            return "It's a tie!";
        }
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public boolean isPlayer1Turn() {
        return player1Turn;
    }
}
