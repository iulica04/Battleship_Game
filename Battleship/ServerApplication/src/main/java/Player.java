import java.io.PrintWriter;
import java.net.Socket;

public class Player {
    private String name;
    private Socket socket;
    private PrintWriter out;
    //pentru fiecare player am 2 tabele
    private Board board;
    private Board opponentViewBoard;

    private boolean lastMoveHit;


    public Player(String name, Socket socket, PrintWriter out) {
        this.name = name;
        this.board = new Board();
        this.opponentViewBoard = new Board();
        this.socket = socket;
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public void sendMessage(String message) {

        System.out.println("Sending message: " + message);
        out.println(message);
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean getLastMoveHit() {
        return lastMoveHit;
    }

    public void setLastMoveHit(boolean hit) {
        this.lastMoveHit = hit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Board getBoard() {
        return board;
    }

    public Board getOpponentViewBoard() {
        return opponentViewBoard;
    }


    public String displayBoard() {
        return board.display();
    }

    public String displayOpponentView() {
        return opponentViewBoard.display();
    }

    public void clearBoards() {
        board.clearBoard();
        opponentViewBoard.clearBoard();
    }


    public boolean allShipsSunk() {
        return board.allShipsSunk();
    }

}
