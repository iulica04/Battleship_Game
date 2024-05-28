
public class Board {
    private static final int SIZE = 10;
    private static final int TOTAL_SHIPS = 5;
    private int[][] grid;
    private int shipsPlaced;
    public int cellsSunk;
    private int cellOccupied ;

    public Board() {
        grid = new int[SIZE][SIZE];
        shipsPlaced = 0;
        cellsSunk = 0;
        cellOccupied = 0;
    }

    // asa punem o "barca" pe board
    public boolean placeShip(int x1, int y1, int x2, int y2) {

        if (shipsPlaced >= TOTAL_SHIPS) {
            return false;
        }
        if (x1 == x2) {
            for (int y = y1; y <= y2; y++) {
                grid[x1][y] = 1;
                cellOccupied++;
            }
        } else if (y1 == y2) {
            for (int x = x1; x <= x2; x++) {
                grid[x][y1] = 1;
                cellOccupied++;
            }
        } else {
            return false;
        }
        shipsPlaced++;
        return true;
    }

    public boolean attackCell(int x, int y) {
        if (grid[x][y] == 1) {
            cellsSunk++;
            return true;
        }
        return false;
    }

    public boolean checkShipSunk(int x, int y) {
        if (grid[x][y] == 6) {
            return false;
        }

        return true;
    }

    public void updateCell(int x, int y, boolean hit) {
        grid[x][y] = hit ? 6 : -1;
    }

    public boolean allShipsPlaced() {
        return shipsPlaced == TOTAL_SHIPS;
    }
    public void clearBoard() {
        cellsSunk =0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = 0;
            }
        }
    }
    public boolean allShipsSunk() {
        return cellsSunk == cellOccupied;
    }
    public String display() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            sb.append('[');
            for (int cell : row) {
                sb.append(cell).append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(']');
        }
        return sb.toString();
    }

}