import entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private PlayerManager player1;
    private PlayerManager player2;
    private boolean player1Turn;
    private PlayerManager LosingPlayer;
    private PlayerManager WinningPlayer;
    private boolean started;
    private Timer gameTimer = new Timer();
    private long gameDurationMillis = 600000;
    private boolean settedShipsByBoth = false;
    private boolean getLastMoveHit = false;
    private boolean hasPlayerMadeMove = false;
    private boolean stillPlayerTurn = false;
    private final Object moveLock = new Object();
    private boolean gameOver = false;
    private List<PlayerManager> players = new ArrayList<>();
    private PlayerManager currentPlayer;
    private PlayerManager leavingPlayer = null;


    public Game(PlayerManager player1) {
        this.player1 = player1;
        this.player1Turn = true;
        this.started = false;
        this.players.add(player1);
    }

    public synchronized void addSecondPlayer(PlayerManager player2) {
        this.player2 = player2;
        this.players.add(player2);
        player1.sendMessage(player2.getName() + " has joined the game!");
        //player2.sendMessage("You have joined the game!");
    }

    public synchronized boolean allPlayersAreJoined() {
        return player1 != null && player2 != null;
    }

    public synchronized boolean isReadyToStart() {
        for (PlayerManager p : getPlayers()) {
            if (!p.getBoard().allShipsPlaced()) {
                settedShipsByBoth = false;
            } else {
                settedShipsByBoth = true;
            }
        }
        return player1 != null && player2 != null && settedShipsByBoth;
    }

    public synchronized void start() {
        if (isReadyToStart()) {
            this.started = true;

            player1.sendMessage("Game started!");
            player2.sendMessage("Game started!");
            player1Turn = true;

            gameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    endGame(determineWinner());
                }
            }, gameDurationMillis);

            new Thread(this::runGameLoop).start();
        }
    }

    public boolean makeMove(int x, int y, PlayerManager opponent) {
        boolean hit = opponent.getBoard().attackCell(x, y);
        opponent.getOpponentViewBoard().updateCell(x, y, hit);

        synchronized (moveLock) {
            if (opponent == player1) {
                currentPlayer = player2;
            } else {
                currentPlayer = player1;
            }
            currentPlayer.setLastMoveHit(hit);
            hasPlayerMadeMove = true;
            moveLock.notifyAll();
        }

        return hit;
    }


    private void runGameLoop() {
        while (!gameOver) {
            PlayerManager currentPlayer = player1Turn ? player1 : player2;
            PlayerManager opponentPlayer = player1Turn ? player2 : player1;

            currentPlayer.sendMessage("Your turn! Make a move!");
            currentPlayer.sendMessage(currentPlayer.displayBoard());

            opponentPlayer.sendMessage("Opponent's turn!");

            long turnStartTime = System.currentTimeMillis();

            synchronized (moveLock) {
                try {
                    moveLock.wait(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (leavingPlayer != null) {
                // endGame(determineWinner());
                return;
            }

            long turnEndTime = System.currentTimeMillis();
            if (turnEndTime - turnStartTime >= 30000) {
                currentPlayer.sendMessage("Time's up! Your turn has ended.");
                opponentPlayer.sendMessage("Time's up! Your opponent's turn has ended.");
                gameOver = true;
                LosingPlayer = currentPlayer;
                WinningPlayer = opponentPlayer;
                endGame(determineWinner());
                return;
            }

            if (hasPlayerMadeMove) {
                hasPlayerMadeMove = false;
                if (!currentPlayer.getLastMoveHit()) {
                    player1Turn = !player1Turn;
                }

                if (currentPlayer.allShipsSunk() || opponentPlayer.allShipsSunk()) {
                    gameOver = true;
                    endGame(determineWinner());
                    return;
                }
            }
        }
    }


    private void endGame(String winner) {
        if (winner.equals("It's a tie!")) {
            player1.sendMessage("Game over! It's a tie!");
            player2.sendMessage("Game over! It's a tie!");
        } else if (winner.equals("Time's up! ")) {
            if (LosingPlayer.equals(player1)) {
                player1.sendMessage("Game over due to time up! The winner is " + player2.getName());
                player2.sendMessage("Game over due to time up! The winner is " + player2.getName());
            } else {
                player1.sendMessage("Game over due to time up! The winner is " + player1.getName());
                player2.sendMessage("Game over due to time up! The winner is " + player1.getName());
            }
            player1.clearBoards();
            player2.clearBoards();
        } else if (winner.equals("Player left!")) {
            if (player1 == leavingPlayer)
                player2.sendMessage("The other player left! HERE");
            else player1.sendMessage("The other player left! Here");

        } else {
            player1.sendMessage("Game over! The winner is " + winner);
            player2.sendMessage("Game over! The winner is " + winner);
        }

        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = new Timer();
        }
        gameOver = false;
        player1Turn = true;
        LosingPlayer = null;
        WinningPlayer = null;
        settedShipsByBoth = false;
        getLastMoveHit = false;
        hasPlayerMadeMove = false;
        stillPlayerTurn = false;
        player1.clearBoards();
        player2.clearBoards();
    }


    private String determineWinner() {

        if (player1.allShipsSunk()) {
            return player1.getName();
        } else if (player2.allShipsSunk()) {
            return player2.getName();
        } else if (LosingPlayer != null) {
            return "Time's up! ";
        } else if (leavingPlayer != null) {
            return "Player left!";
        } else {
            return "It's a tie!";
        }
    }

    public synchronized void playerLeftGame(PlayerManager leavingPlayer1) {
        if (leavingPlayer1 == player1) {
            player1.sendMessage("You left the game.");
            leavingPlayer = player1;
            if (player2 != null) {
                player2.sendMessage("Player left game. Game over!");
            }
        } else if (leavingPlayer1 == player2) {
            player2.sendMessage("You left the game.");
            leavingPlayer = player2;
            if (player1 != null) {
                player1.sendMessage("Player left game. Game over!");
            }
        }
        endGame(determineWinner());

        gameOver = true;
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = new Timer();
        }
        player1.clearBoards();
        if (player2 != null) {
            player2.clearBoards();
        }
    }


    public PlayerManager getPlayer1() {
        return player1;
    }

    public PlayerManager getPlayer2() {
        return player2;
    }

    public boolean isPlayer1Turn() {
        return player1Turn;
    }

    public List<PlayerManager> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerManager> players) {
        this.players = players;
    }

    public void setPlayer1Turn(boolean turn) {
        this.player1Turn = turn;
    }
    public String getGameStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("In joc sunt urmatorii jucatori: ").append(" ");
        for (PlayerManager player : getPlayers()) {
            sb.append("- ").append(player.getName()).append(" ");
        }
        return sb.toString();
    }


}
