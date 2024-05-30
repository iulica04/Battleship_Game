import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private Player player1;
    private Player player2;
    private boolean player1Turn;
    private Player LosingPlayer;
    private Player WinningPlayer;
    private boolean started;
    private Timer gameTimer = new Timer();
    private long gameDurationMillis = 600000;
    private boolean settedShipsByBoth = false;
      private boolean getLastMoveHit = false;
    private boolean hasPlayerMadeMove = false;
    private boolean stillPlayerTurn= false;
    private final Object moveLock = new Object();
    private boolean gameOver = false;
    private List<Player> players=new ArrayList<>();
    private Player currentPlayer;


    public Game(Player player1) {
        this.player1 = player1;
        this.player1Turn = true;
        this.started = false;
        this.players.add(player1);
    }

    public synchronized void addSecondPlayer(Player player2) {
        this.player2 = player2;
        this.players.add(player2);
        player1.sendMessage(player2.getName() + " has joined the game! Enter 'set the ships positions' to set the ships positions!");
    }

    public synchronized boolean allPlayersAreJoined() {
        return player1 != null && player2 != null;
    }

    public synchronized boolean isReadyToStart() {
        for (Player p : getPlayers()) {
            if (!p.getBoard().allShipsPlaced()) {
                settedShipsByBoth = false;
            }else{
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

            gameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    endGame(determineWinner());

                }
            }, gameDurationMillis);

            new Thread(this::runGameLoop).start();
        }
    }
    public boolean makeMove(int x, int y, Player opponent) {
        boolean hit = opponent.getBoard().attackCell(x, y);
        opponent.getOpponentViewBoard().updateCell(x, y, hit);

        synchronized (moveLock) {
            if(opponent == player1) {
                currentPlayer = player2;
            }else{
                currentPlayer = player1;
            }
            currentPlayer.setLastMoveHit(hit);  // Setăm rezultatul mutării
            hasPlayerMadeMove = true;  // Mutarea a fost făcută
            moveLock.notifyAll();      // Notificăm firul principal
        }

        return hit;
    }


    private void runGameLoop() {
        while (!gameOver) {
            Player currentPlayer = player1Turn ? player1 : player2;
            Player opponentPlayer = player1Turn ? player2 : player1;

            currentPlayer.sendMessage("Your turn! Make a move!");
            currentPlayer.sendMessage(currentPlayer.displayBoard());

            opponentPlayer.sendMessage("Opponent's turn!");

            long turnStartTime = System.currentTimeMillis();

            synchronized (moveLock) {
                try {
                    moveLock.wait(30000);  // Așteaptă până când jucătorul face o mutare sau timpul expiră
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long turnEndTime = System.currentTimeMillis();
            if (turnEndTime - turnStartTime >= 30000) {
                currentPlayer.sendMessage("Time's up! Your turn has ended.");
                gameOver = true;
                LosingPlayer = currentPlayer;
                WinningPlayer = opponentPlayer;
            }

            if (hasPlayerMadeMove) {
                hasPlayerMadeMove = false;
                if (!currentPlayer.getLastMoveHit()) {  // Schimbăm rândul doar dacă lovitura nu a fost un hit
                    player1Turn = !player1Turn;
                }
            }
        }
    }


    private void endGame(String winner) {
        if (winner.equals("It's a tie!")) {
            player1.sendMessage("Game over! It's a tie!");
            player2.sendMessage("Game over! It's a tie!");
        }else if(winner.equals("Time's up! ")) {
            if(LosingPlayer.equals(player1)) {
                player1.sendMessage("Game over due time up! The winner is " + player2.getName());
                player2.sendMessage("Game over due time up! The winner is " + player2.getName());
            }else{
                player1.sendMessage("Game over  due time up! The winner is " + player1.getName());
                player2.sendMessage("Game over  due time up! The winner is " + player1.getName());
            }
            //Daca timpul s-a scurs si s-a terminat jocul atunci se sterg toate tablele jucatorilor
            player1.clearBoards();
            player2.clearBoards();
        }
        else {
            player1.sendMessage("Game over! The winner is " + winner);
            player2.sendMessage("Game over! The winner is " + winner);
        }

        gameOver = true;
        gameTimer.cancel();
    }


    private String determineWinner() {

        if (player1.allShipsSunk()) {
            return player1.getName();
        } else if (player2.allShipsSunk()) {
            return player2.getName();
        }else if(LosingPlayer!=null){
            return "Time's up! ";
        } else{
            return "It's a tie!";
        }
    }



/*
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
    }*/



    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public boolean isPlayer1Turn() {
        return player1Turn;
    }
    public List<Player> getPlayers() {
        return players;
    }public void setPlayers(List<Player> players) {
        this.players = players;
    }
    public String getGameStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("In joc sunt urmatorii jucatori: ").append(" ");
        for (Player player : getPlayers()) {
            sb.append("- ").append(player.getName()).append(" ");
        }
        /*sb.append("| Jucatorul care a creat jocul: ").append(getGame(nextGameId).getPlayer1().getName()).append(" ");*/
        return sb.toString();
    }


}
