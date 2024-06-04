package gameInterface;
import client.GameClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import gameInterface.elements.RoundedButton;

public class MyBoardUI extends JFrame {
    private GameClient client;
    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 30; // Dimensiunea fiecarei celule
    private JPanel gridPanel;
    private JPanel[][] cells;
    private ShipPanel[] ships;
    private int placedShipCount = 0;
    String playerName;
    String task;
    Color cadetGrey = Color.decode("#91A6AE");
    Color delftBlue = Color.decode("#414163");
    Color tyrianPurple = Color.decode("#471732");
    Color taupeGray = Color.decode("#7A7885");
    Color brown = Color.decode("#D4A276");
    Color mintGreen = Color.decode("#CDF2EB");

    public MyBoardUI(GameClient client, String playerName, String task) {
        this.client = client;
        this.playerName = playerName;
        this.task = task;
        client.setMessageConsumer(this::handleServerResponse);


        setTitle("My Board");
        setSize(700, 700);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(1200, 100));
        titlePanel.setBackground(delftBlue);

        JLabel titleLabel = new JLabel("Drag the ships tot the board!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(cadetGrey);

        titlePanel.add(titleLabel, BorderLayout.CENTER);


        // Creates the grid (MyBoard)
        gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        gridPanel.setSize(new Dimension(500, 500));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        gridPanel.setBackground(brown);
        cells = new JPanel[GRID_SIZE][GRID_SIZE];

        // Dupa crearea gridului (MyBoard) si a adaugarii celulelor în gridPanel
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setTransferHandler(new ShipTransferHandler());
                cell.setBackground(Color.CYAN);
                new DropTarget(cell, DnDConstants.ACTION_MOVE, new ShipDropTargetListener(), true);
                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        // Ships Panel
        JPanel shipsPanel = new JPanel();
        shipsPanel.setLayout(new BoxLayout(shipsPanel, BoxLayout.Y_AXIS));
        shipsPanel.setSize(new Dimension(260, 1200));
        // Ship types
        ships = new ShipPanel[9];
        ships[0] = new ShipPanel("Ship 1 DOWN", 1);
        ships[1] = new ShipPanel("Ship 2 DOWN", 2);
        ships[2] = new ShipPanel("Ship 3 DOWN", 3);
        ships[3] = new ShipPanel("Ship 4 DOWN", 4);
        ships[4] = new ShipPanel("Ship 5 DOWN", 5);
        ships[5] = new ShipPanel("Ship 2 UP", 2);
        ships[6] = new ShipPanel("Ship 3 UP", 3);
        ships[7] = new ShipPanel("Ship 4 UP", 4);
        ships[8] = new ShipPanel("Ship 5 UP", 5);

        for (ShipPanel ship : ships) {
            shipsPanel.add(ship);
            DragSource ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(ship, DnDConstants.ACTION_MOVE, new ShipDragGestureListener());
        }

        //Buttons Panel (Edit)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 10)); // Centrarea butoanelor
        buttonPanel.setOpaque(true); // Sa fie transparent, pentru a vedea imaginea de fundal
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20)); // Marginea panoului

        buttonPanel.add(new JLabel("Edit your board:  "));
        buttonPanel.setFont(new Font("Serif", Font.BOLD, 10));
        JButton removeShipButton = createButton("Remove Ship");
        JButton startButton = createButton("Start Game");

        buttonPanel.add(removeShipButton);
        buttonPanel.add(new JLabel(" "));
        buttonPanel.add(new JLabel("  If you are ready: "));
        buttonPanel.add(startButton);
        buttonPanel.setBackground(Color.LIGHT_GRAY);

        //MAIN PANEL
        add(titlePanel, BorderLayout.NORTH); // for the title
        add(gridPanel, BorderLayout.CENTER);
        add(shipsPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        setBackground(brown);


        setLocationRelativeTo(null); // for centering the window
        setVisible(true);

        // Add action listener to the remove ship button
        removeShipButton.addActionListener(e -> {
            String message = "Select a cell from the ship you want to remove.";
            JOptionPane.showMessageDialog(MyBoardUI.this, message);

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    JPanel cell = cells[i][j];
                    cell.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int row = cell.getParent().getComponentZOrder(cell) / GRID_SIZE;
                            int col = cell.getParent().getComponentZOrder(cell) % GRID_SIZE;

                            // Verify if the cell has a ship and if there are ships placed
                            if (cellHasShip(row, col) && placedShipCount > 0) {
                                clearShipCells(row, col);
                                placedShipCount--;
                                // Dupa ce am sters barca, eliminam listenerul de mouse de pe celule pentru a se face doar o stergere
                                removeMouseListeners();
                            } else {
                                JOptionPane.showMessageDialog(MyBoardUI.this, "This cell is not part of a ship.");
                            }
                        }
                    });
                }
            }
        });

        startButton.addActionListener(e -> {
            if (placedShipCount >= 5) {
                dispose();
                if (task.equals("create game")) {
                    SwingUtilities.invokeLater(() -> new GameUI(client, playerName));
                    handleServerResponse("create game");
                } else {
                    SwingUtilities.invokeLater(() -> new JoinGameScreen(client));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Not all ships placed!");
            }
        });


    }
    private JButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setPreferredSize(new Dimension(100, 40)); // Dimensiuni personalizate
        return button;
    }

    // Metoda pentru a elimina toti listenerii de mouse de pe celule dupa ce o barca a fost stearsa
    private void removeMouseListeners() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JPanel cell = cells[i][j];
                MouseListener[] listeners = cell.getMouseListeners();
                for (MouseListener listener : listeners) {
                    cell.removeMouseListener(listener);
                }
            }
        }
    }

    private boolean cellHasShip(int row, int col) {
        // Verificam daca celula este colorata cu alta culoare decât CYAN
        return !cells[row][col].getBackground().equals(Color.CYAN);

    }

    // Metoda pentru a sterge o nava
    private void clearShipCells(int x, int y) {
        Color shipColor = cells[x][y].getBackground();
        int rowStart = x;
        int colStart = y;
        int rowEnd = x;
        int colEnd = y;
        if (shipColor.equals(Color.GRAY)) {
            // Nava este orientata vertical
            while (rowEnd + 1 < GRID_SIZE && cells[rowEnd + 1][colStart].getBackground().equals(Color.GRAY)) {
                rowEnd++;
            }
        } else if (shipColor.equals(Color.LIGHT_GRAY)) {
            // Nava este orientata orizontal
            while (colEnd + 1 < GRID_SIZE && cells[rowStart][colEnd + 1].getBackground().equals(Color.LIGHT_GRAY)) {
                colEnd++;
            }
        }
        sendCommandRemoveShip(rowStart, colStart, rowEnd, colEnd);
        for (int i = rowStart; i <= rowEnd; i++) {
            for (int j = colStart; j <= colEnd; j++) {
                cells[i][j].setBackground(Color.CYAN);
            }
        }
    }

    private void sendCommandRemoveShip(int x1, int y1, int x2, int y2) {
        String command = "delete ship " + x1 + " " + y1 + " " + x2 + " " + y2;
        // System.out.println("Sending command: " + command);
        client.sendCommand(command);
    }


    private class ShipPanel extends JPanel {
        private String name;
        private int size;
        private final int SQUARE_SIZE = 50; // Dimensiunea fiecarui patrat

        public ShipPanel(String name, int size) {
            this.name = name;
            this.size = size;

            setPreferredSize(new Dimension(260, 60));

            setBackground(Color.LIGHT_GRAY);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JLabel label = new JLabel(name);
            add(label);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    JComponent comp = (JComponent) e.getSource();
                    TransferHandler handler = comp.getTransferHandler();
                    handler.exportAsDrag(comp, e, TransferHandler.MOVE);
                }
            });

            setTransferHandler(new ShipTransferHandler());
        }

        public String getName() {
            return name;
        }

        public int getShipSize() {
            return size;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (size == 1) {
                int x = 90;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    g.setColor(Color.GRAY);
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);

                    x += SQUARE_SIZE;
                }

            } else if (size == 2) {
                int x = 70;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    g.setColor(Color.GRAY);
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                    x += SQUARE_SIZE;
                }

            } else if (size == 3) {
                int x = 40;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    g.setColor(Color.GRAY);
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                    x += SQUARE_SIZE;
                }

            } else if (size == 4) {
                int x = 25;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    g.setColor(Color.GRAY);
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                    x += SQUARE_SIZE;
                }

            } else if (size == 5) {
                int x = 5;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    g.setColor(Color.GRAY);
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                    x += SQUARE_SIZE;
                }

            }
            g2d.dispose();
        }
    }

    private class ShipDragGestureListener implements DragGestureListener {
        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            Cursor cursor = null;
            ShipPanel ship = (ShipPanel) dge.getComponent();
            StringSelection text = new StringSelection(ship.getName() + ":" + ship.getShipSize());
            dge.startDrag(cursor, text, new ShipDragSourceListener());
        }
    }

    private class ShipDragSourceListener implements DragSourceListener {
        @Override
        public void dragEnter(DragSourceDragEvent dsde) {}

        @Override
        public void dragOver(DragSourceDragEvent dsde) {}

        @Override
        public void dropActionChanged(DragSourceDragEvent dsde) {}

        @Override
        public void dragExit(DragSourceEvent dse) {}

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (dsde.getDropSuccess()) {
                ShipPanel ship = (ShipPanel) dsde.getDragSourceContext().getComponent();
                ship.setVisible(false);
            }
        }
    }

    private class ShipDropTargetListener extends Component implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {}

        @Override
        public void dragOver(DropTargetDragEvent dtde) {}

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {}

        @Override
        public void dragExit(DropTargetEvent dte) {}

        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    String[] parts = data.split(":");
                    String shipName = parts[0];
                    int shipSize = Integer.parseInt(parts[1]);

                    JPanel cell = (JPanel) dtde.getDropTargetContext().getComponent();
                    Point location = cell.getLocation();
                    int row = location.y / (gridPanel.getHeight() / GRID_SIZE);
                    int col = location.x / (gridPanel.getWidth() / GRID_SIZE);

                    // Checks if all ships have been placed
                    if (placedShipCount >= 5) {
                        checkGameStatus();
                        return;
                    }

                    // Checks if the ship can be placed
                    if (placedShipCount <= 5) {
                        if (row + shipSize <= GRID_SIZE && shipName.endsWith("UP") && isNotNearOtherBoat(row, col, row + shipSize - 1, col)) {

                            for (int i = 0; i < shipSize; i++) {
                                cells[row + i][col].setBackground(Color.GRAY);
                            }

                            String shipPosition = String.format("%d %d %d %d", row, col, row + shipSize - 1, col);
                            //   System.out.println("Ship position: " + shipPosition);
                            sendCommandSetPositions(row, col, row + shipSize - 1, col, shipName);
                            placedShipCount++;

                            dtde.dropComplete(true);

                        } else if (col + shipSize <= GRID_SIZE && shipName.endsWith("DOWN") && isNotNearOtherBoat(row, col, row, col + shipSize - 1)) {
                            for (int i = 0; i < shipSize; i++) {
                                cells[row][col + i].setBackground(Color.LIGHT_GRAY);
                            }

                            String shipPosition = String.format("%d %d %d %d", row, col, row, col + shipSize - 1);
                            // System.out.println("Ship position: " + shipPosition);
                            sendCommandSetPositions(row, col, row, col + shipSize - 1, shipName);
                            placedShipCount++;

                            dtde.dropComplete(true);
                        }
                    } else {
                        dtde.rejectDrop();
                    }


                }
            } catch (Exception e) {
                dtde.rejectDrop();
                e.printStackTrace();
            }
        }
    }

    private class ShipTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            ShipPanel ship = (ShipPanel) c;
            return new StringSelection(ship.getName() + ":" + ship.getShipSize());
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
    }

    private void checkGameStatus() {
        if (placedShipCount >= 5) {
            //    System.out.println("All ships placed! You may now play the game!");
            JOptionPane.showMessageDialog(this, "All ships placed!");
        } else {
            //   System.out.println("Not all ships placed!");
            JOptionPane.showMessageDialog(this, "Not all ships placed!");
        }
    }
    private boolean isNotNearOtherBoat(int x1, int y1, int x2, int y2) {
        for (int i = x1 - 1; i <= x2 + 1; i++) {
            for (int j = y1 - 1; j <= y2 + 1; j++) {
                if (i >= 0 && i < GRID_SIZE && j >= 0 && j < GRID_SIZE) {
                    if (!cells[i][j].getBackground().equals(Color.CYAN)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void sendCommandSetPositions(int x1, int y1, int x2, int y2, String shipName) {
        String command = "place ship " + x1 + " " + y1 + " " + x2 + " " + y2;
        client.sendCommand(command);
    }


    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            //    System.out.println("Received response from server: " + response);

            if (response.startsWith("Enter your name")) {
                // playerName = JOptionPane.showInputDialog(this, "Enter your name:");
                if (playerName != null && !playerName.trim().isEmpty()) {
                    //   client.sendCommand("name " + playerName.trim());
                }
            } else if (response.startsWith("You can't place more than 5 ships.")) {
                //    System.out.println("Ship placed.");

            } else if (response.startsWith("create game")) {
                sendCommandCreateGame();
                //  System.out.println("Game created");
            }

        });
    }

    private void sendCommandCreateGame() {
        String command = "create game";
        // System.out.println("Sending move: " + command);
        client.sendCommand(command);
    }

  /*  public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlayerScreen(client, playerName));
    }*/
}