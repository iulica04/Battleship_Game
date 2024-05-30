package gameInterface;

public class GameBoardUi {
    private boolean[][] board;

    public GameBoardUi(int rows, int cols) {
        board = new boolean[rows][cols];
    }

    public void placeShip(int row, int col, int size, boolean isVertical) {
        if (isVertical) {
            for (int i = 0; i < size; i++) {
                board[row + i][col] = true;
            }
        } else {
            for (int i = 0; i < size; i++) {
                board[row][col + i] = true;
            }
        }
    }

    public boolean isCellOccupied(int row, int col) {
        return board[row][col];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                sb.append(board[i][j] ? "X" : "O");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
