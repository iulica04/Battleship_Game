import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {
    private char[][] board; // s = ship, h=hit ship, m=missed shot, null = empty cell

    private List<AbstractMap.SimpleEntry<Integer, Integer>> shipsPositions;
    private int boardSize;
    private int numberOfShips;

    public Board() {
    }

    public Board(int boardSize, int numberOfShips) {
        this.boardSize = boardSize;
        this.numberOfShips = numberOfShips;
        this.board = new char[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = ' ';
            }
        }
        this.shipsPositions = new ArrayList<>();
    }

    public boolean makeMove(int x, int y) {
        if (board[x][y] == 's') {
            board[x][y] = 'h';
            numberOfShips--;
            return true;
        } else if (board[x][y] == 'm') {
            return false;
        } else {
            board[x][y] = 'm';
            return false;
        }
    }

    public void setShipsPositions(List<AbstractMap.SimpleEntry<Integer, Integer>> shipsPositions) {
        this.shipsPositions = shipsPositions;
        for (AbstractMap.SimpleEntry<Integer, Integer> position : shipsPositions) {
            board[position.getKey()][position.getValue()] = 's';
        }
    }


    public int getNumberOfShips() {
        return numberOfShips;
    }

    public void setNumberOfShips(int numberOfShips) {
        this.numberOfShips = numberOfShips;
    }

    public char[][] getBoard() {
        return board;
    }

    public List<AbstractMap.SimpleEntry<Integer, Integer>> getShipsPositions() {
        return shipsPositions;
    }


    @Override
    public String toString() {
        return "Board{" +
                "board=" + Arrays.toString(board) +
                ", shipsPositions=" + shipsPositions +
                ", boardSize=" + boardSize +
                ", numberOfShips=" + numberOfShips +
                '}';
    }
}
